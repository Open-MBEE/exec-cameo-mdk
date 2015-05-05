package gov.nasa.jpl.mbee.ems.sync;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.uml.transaction.MDTransactionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.transaction.TransactionManager;

/*
 * This class is responsible for taking action when a project is opened.
 * This class does the following when instantiated:
 *   1. Create a transaction manager
 *   2. Create a TransactionCommitListener object
 *   3. Add the listener to the transaction manager object 
 *   4. Create a JMS topic and connection to that topic
 *   5. Store that connection so we keep track of the connections to JMS.
 *   
 */
public class AutoSyncProjectListener extends ProjectEventListenerAdapter {

    private static final String CONNECTION = "Connection";
    public static final String LISTENER = "AutoSyncCommitListener";
    private static final String SESSION = "Session";
    private static final String CONSUMER = "MessageConsumer";
    public static final String JMSLISTENER = "JmsListener";
    public static final String CONFLICTS = "Conflicts";
    public static final String FAILED = "Failed";
    public static final String UPDATES = "Updates";
    
    private static final String MSG_SELECTOR_PROJECT_ID = "projectId";
    private static final String MSG_SELECTOR_WS_ID = "workspace";
    public static Logger log = Logger.getLogger(AutoSyncProjectListener.class);

    public static String getJMSUrl() {
        String url = ExportUtility.getUrl();
        if (url != null) {
            if (url.startsWith("https://"))
                url = url.substring(8);
            else if (url.startsWith("http://"))
                url = url.substring(7);
            int index = url.indexOf(":");
            if (index != -1)
                url = url.substring(0, index);
            if (url.endsWith("/alfresco/service"))
                url = url.substring(0, url.length() - 17);
            url = "tcp://" + url + ":61616";
        } 
        return url;
    }

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss");
    public static List<Element> getSyncElement(Project project, boolean create, String prefix) {
        List<Element> elements = new ArrayList<Element>();
        String folderId = project.getPrimaryProject().getProjectID();
        folderId += "_sync";
        Element folder = ExportUtility.getElementFromID(folderId);
        if (folder == null) {
            if (!create)
                return elements;
            project.getCounter().setCanResetIDForObject(true);
            folder = project.getElementsFactory().createPackageInstance();
            folder.setOwner(project.getModel());
            ((Package)folder).setName("__ProjectSync__");
            folder.setID(folderId);
        } else {
            if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
                if (TeamworkUtils.getLoggedUserName() != null && create)
                    TeamworkUtils.lockElement(project, folder, true);
            }
        }
        Class failed = null;
        String last = "";
        for (Element e: folder.getOwnedElement()) {
            if (e instanceof Class) {
                String name = ((Class)e).getName();
                if (name.startsWith(prefix)) {
                    if (name.compareTo(last) >= 0) {
                        failed = (Class)e;
                        last = name;
                    }
                    elements.add(e);
                }
            }
        }
        if ((failed == null || !failed.isEditable()) && create) {
            failed = project.getElementsFactory().createClassInstance();
            failed.setOwner(folder);
            failed.setName(prefix + "_" + df.format(new Date()));
        }
        if (elements.contains(failed))
            elements.remove(failed);
        if (failed != null)
            elements.add(0, failed);
        return elements;
    }
    
    public static void setUpdatesOrFailed(Project project, JSONObject o, String type) {
        List<Element> es = getSyncElement(project, true, type);
        ((NamedElement)es.get(0)).setName(type + "_" + df.format(new Date()));
        ModelHelper.setComment(es.get(0), (o == null) ? "{\"deleted\":[], \"changed\":[], \"added\":[]}" : o.toJSONString());
        for (int i = 1; i < es.size(); i++) {
            Element toremove = es.get(i);
            if (toremove.isEditable()) {
                try {
                    ModelElementsManager.getInstance().removeElement(toremove);
                } catch (ReadOnlyElementException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static JSONObject getUpdatesOrFailed(Project project, String type) {
        List<Element> es = getSyncElement(project, false, type);
        if (es.isEmpty())
            return null;
        JSONObject update = new JSONObject();
        update.put("deleted", new JSONArray());
        update.put("changed", new JSONArray());
        update.put("added", new JSONArray());
        Set<String> deleted = new HashSet<String>();
        Set<String> changed = new HashSet<String>();
        Set<String> added = new HashSet<String>();
        for (Element e: es) {
            try {
                JSONObject updates = (JSONObject)JSONValue.parse(ModelHelper.getComment(e));
                deleted.addAll((JSONArray)updates.get("deleted"));
                changed.addAll((JSONArray)updates.get("changed"));
                added.addAll((JSONArray)updates.get("added"));
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        ((JSONArray)update.get("deleted")).addAll(deleted);
        ((JSONArray)update.get("changed")).addAll(changed);
        ((JSONArray)update.get("added")).addAll(added);
        return update;
    }
    
    public static void setConflicts(Project project, JSONObject o) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (o == null)
            projectInstances.remove(CONFLICTS);
        else
            projectInstances.put(CONFLICTS, o);
        List<Element> es = getSyncElement(project, true, "conflict");
        ((NamedElement)es.get(0)).setName("conflict_" + df.format(new Date()));
        ModelHelper.setComment(es.get(0), (o == null) ? "{\"elements\":[]}" : o.toJSONString());
        for (int i = 1; i < es.size(); i++) {
            Element toremove = es.get(i);
            if (toremove.isEditable()) {
                try {
                    ModelElementsManager.getInstance().removeElement(toremove);
                } catch (ReadOnlyElementException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static JSONObject getConflicts(Project project) {
        List<Element> es = getSyncElement(project, false, "conflict");
        if (es.isEmpty())
            return null;
        JSONObject update = new JSONObject();
        update.put("elements", new JSONArray());
        Set<String> elements = new HashSet<String>();
        for (Element e: es) {
            try {
                JSONObject updates = (JSONObject)JSONValue.parse(ModelHelper.getComment(e));
                elements.addAll((JSONArray)updates.get("elements"));
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        ((JSONArray)update.get("elements")).addAll(elements);
        return update;
        
        /*
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        JSONObject toreturn = (JSONObject)projectInstances.get(CONFLICTS);
        if (toreturn == null) {
            List<Element> es = getSyncElement(project, false, "conflict");
            if (es.isEmpty())
                return null;
            try {
                JSONObject ob = (JSONObject)JSONValue.parse(ModelHelper.getComment(es.get(0)));
                return ob;
            } catch (Exception ex) {
                log.error("", ex);
                return null;
            }
        } else
            return toreturn;*/
    }
    /*
    public static void setFailed(Project project, JSONObject o) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (o == null)
            projectInstances.remove(FAILED);
        else
            projectInstances.put(FAILED, o);
        Element e = getSyncElement(project, true, "error");
        ((NamedElement)e).setName("error_" + df.format(new Date()));
        ModelHelper.setComment(e, (o == null) ? "{\"added\":[], \"changed\":[], \"deleted\":[]}" : o.toJSONString());
    }
    
    public static JSONObject getFailed(Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        JSONObject toreturn = (JSONObject)projectInstances.get(FAILED);
        if (toreturn == null) {
            Element e = getSyncElement(project, false, "error");
            if (e == null)
                return null;
            try {
                JSONObject ob = (JSONObject)JSONValue.parse(ModelHelper.getComment(e));
                return ob;
            } catch (Exception ex) {
                log.error("", ex);
                return null;
            }
        } else
            return toreturn;
    }*/
    
    @SuppressWarnings("unchecked")
    public static Map<String, Set<String>> getJMSChanges(Project project) {
        Map<String, Set<String>> changes = new HashMap<String, Set<String>>();
        Set<String> changedIds = new HashSet<String>();
        Set<String> deletedIds = new HashSet<String>();
        Set<String> addedIds = new HashSet<String>();
        changes.put("changed", changedIds);
        changes.put("deleted", deletedIds);
        changes.put("added", addedIds);
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER)) {// || projectInstances.containsKey(LISTENER)) {
            Application.getInstance().getGUILog().log("[INFO] Autosync is currently on, you cannot do a manual update/commit while autosync is on.");
            return null; //autosync is on, should turn off first
        }
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        String url = getJMSUrl();
        if (url == null) {
            Application.getInstance().getGUILog().log("[ERROR] cannot get server url");
            return null;
        }
        if (wsID == null) {
            Application.getInstance().getGUILog().log("[ERROR] cannot get server workspace that corresponds to this project branch");
            return null;
        }
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        
        JSONObject previousFailed = getUpdatesOrFailed(project, "error");
        if (previousFailed != null) {
            addedIds.addAll((List<String>)previousFailed.get("added"));
            deletedIds.addAll((List<String>)previousFailed.get("deleted"));
            changedIds.addAll((List<String>)previousFailed.get("changed"));
        }
        JSONObject previousConflicts = getConflicts(project);
        if (previousConflicts != null) {
            changedIds.addAll((List<String>)previousConflicts.get("elements"));
        }
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            String subscriberId = projectID + "/" + wsID; //getSubscriberId(project);
            connection = connectionFactory.createConnection();
            connection.setClientID(subscriberId);// + (new Date()).toString());
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            Topic topic = session.createTopic("master");
            String messageSelector = constructSelectorString(projectID, wsID);
            consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            connection.start();
            Message m = consumer.receive(1000);
            while (m != null) {
                TextMessage message = (TextMessage)m;
                log.info("From JMS (Manual receive): " + message.getText());
                JSONObject ob = (JSONObject) JSONValue.parse(message.getText());
                boolean magicdraw = false;
                if (ob.get("source") != null && ob.get("source").equals("magicdraw")) {
                    //m = consumer.receive(1000);
                    magicdraw = true;
                    //continue;
                }
                JSONObject ws2 = (JSONObject) ob.get("workspace2");
                final JSONArray updated = (JSONArray) ws2.get("updatedElements");
                final JSONArray added = (JSONArray) ws2.get("addedElements");
                final JSONArray deleted = (JSONArray) ws2.get("deletedElements");
                final JSONArray moved = (JSONArray) ws2.get("movedElements");
                for (Object e: updated) {
                    String id = (String)((JSONObject)e).get("sysmlid");
                    if (!magicdraw) 
                        changedIds.add(id);
                    deletedIds.remove(id);
                }
                for (Object e: added) {
                    String id = (String)((JSONObject)e).get("sysmlid");
                    if (!magicdraw) 
                        addedIds.add(id);
                    deletedIds.remove(id);
                }
                for (Object e: moved) {
                    String id = (String)((JSONObject)e).get("sysmlid");
                    if (!magicdraw) 
                        changedIds.add(id);
                    deletedIds.remove(id);
                }
                for (Object e: deleted) {
                    String id = (String)((JSONObject)e).get("sysmlid");
                    if (!magicdraw)
                        deletedIds.add(id);
                    addedIds.remove(id);
                    changedIds.remove(id);
                }
                m.acknowledge();
                m = consumer.receive(1000);
            }
            SessionManager sm = SessionManager.getInstance();
            sm.createSession("mms delayed sync change logs");
            try {
                setUpdatesOrFailed(project, null, "error");
                setConflicts(project, null);
                sm.closeSession();
            } catch (Exception e) {
                sm.cancelSession();
            }
            return changes;
        } catch (Exception e) {
            log.error("JMS (Manual receive): ", e);
            Application.getInstance().getGUILog().log("[ERROR] getting changes from mms failed: " + e.getMessage());
            return null;
        } finally {
            try {
            if (consumer != null)
                consumer.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
            } catch (JMSException e) {
            }
        }
    }
    
    public static void initDurable(Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        
        // Check if the keywords are found in the current project. If so, it
        // indicates that this JMS subscriber has already been init'ed.
        //
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER)) {// || projectInstances.containsKey(LISTENER)) {
            return;
        }
        String url = getJMSUrl();
        if (url == null) {
            Application.getInstance().getGUILog().log("[ERROR] sync initialization failed - cannot get server url");
            return;
        }
        if (wsID == null) {
            Application.getInstance().getGUILog().log("[ERROR] sync initialization failed - cannot get server workspace that corresponds to this project branch");
            return;
        }
        Integer webVersion = ExportUtility.getAlfrescoProjectVersion(ExportUtility.getProjectId(project));
        Integer localVersion = ExportUtility.getProjectVersion(project);
        if (localVersion != null && !localVersion.equals(webVersion)) {
            Application.getInstance().getGUILog().log("[ERROR] autosync not allowed - project versions currently don't match - project may be out of date");
            return;
        }
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            String user = TeamworkUtils.getLoggedUserName();
            if (user == null) {
                Application.getInstance().getGUILog().log("[ERROR] You must be logged into teamwork - autosync will not start");
                return;
            }
            Collection<Element> lockedByUser = TeamworkUtils.getLockedElement(project, user);
            Collection<Element> lockedByAll = TeamworkUtils.getLockedElement(project, null);
            lockedByAll.removeAll(lockedByUser);
            for (Element locked: lockedByAll) {
                if (!ProjectUtilities.isElementInAttachedProject(locked)) {
                    Application.getInstance().getGUILog().log("[ERROR] Another user has locked part of the project - autosync will not start");
                    return;
                }
            }
            //if (!lockedByUser.equals(lockedByAll)) {
            //    Application.getInstance().getGUILog().log("[ERROR] Another user has locked part of the project - autosync will not start");
            //    return;
            //}
            for (Element e: project.getModel().getOwnedElement()) {
                if (ProjectUtilities.isElementInAttachedProject(e))
                    continue;
                if (!TeamworkUtils.lockElement(project, e, true)) {
                    Application.getInstance().getGUILog().log("[ERROR] cannot lock project - autosync will not start");
                    return;
                }
            }
        }
        try {
            AutoSyncCommitListener listener = (AutoSyncCommitListener)projectInstances.get(LISTENER);
            if (listener == null) {
                listener = new AutoSyncCommitListener(true); 
                MDTransactionManager transactionManager = (MDTransactionManager) project.getRepository()
                    .getTransactionManager();
                listener.setTm(transactionManager);
                transactionManager.addTransactionCommitListenerIncludingUndoAndRedo(listener);
                projectInstances.put(LISTENER, listener);
            }
            listener.setAuto(true);

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            String subscriberId = projectID + "/" + wsID; //getSubscriberId(project);
            Connection connection = connectionFactory.createConnection();
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException e) {
                    Application.getInstance().getGUILog().log(e.getMessage());
                    log.error(e.getMessage(), e);
                    //if (e instanceof LostServerConnection) {
                        
                    //}
                }
            });
            connection.setClientID(subscriberId);// + (new Date()).toString());
            // connection.setExceptionListener(this);
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            Topic topic = session.createTopic("master");

            String messageSelector = constructSelectorString(projectID, wsID);
            
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            JMSMessageListener jmslistener = new JMSMessageListener(project);
            consumer.setMessageListener(jmslistener);
            connection.start();
            projectInstances.put(JMSLISTENER, jmslistener);
            projectInstances.put(CONNECTION, connection);
            projectInstances.put(SESSION, session);
            projectInstances.put(CONSUMER, consumer);

            Application.getInstance().getGUILog().log("[INFO] sync initiated");
        }
        catch (Exception e) {
            log.error("", e);
            Application.getInstance().getGUILog().log("[ERROR] sync initialization failed: " + e.getMessage());
        }
    }

    public static String getSubscriberId(Project proj) {
        String projId = ExportUtility.getProjectId(proj);
        String ws = ExportUtility.getTeamworkBranch(proj);
        if (ws == null)
            ws = "master";
        return projId + "/" + ws;
    }

    public static String constructSelectorString(String projectID, String workspaceID) {
        StringBuilder selectorBuilder = new StringBuilder();

        //selectorBuilder.append("(").append(MSG_SELECTOR_WS_ID).append("='").append(workspaceID).append("')");

         selectorBuilder.append("(").append(MSG_SELECTOR_PROJECT_ID).append(" = '").append(projectID).append("')")
         .append(" AND ").append("(").append(MSG_SELECTOR_WS_ID).append(" = '").append(workspaceID).append("')");

        String outputMsgSelector = selectorBuilder.toString();
        selectorBuilder.delete(0, selectorBuilder.length());

        return outputMsgSelector;
    }

    public static void close(Project project, boolean keepDelayedSync) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances == null)
            return;
        AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances.get(LISTENER);
        if (listener != null) {
            if (keepDelayedSync)
                listener.setAuto(false);
            else
                project.getRepository().getTransactionManager().removeTransactionCommitListener(listener);
        }
        if (keepDelayedSync)
            saveAutoSyncErrors(project);
        Connection connection = (Connection) projectInstances.remove(CONNECTION);
        Session session = (Session) projectInstances.remove(SESSION);
        MessageConsumer consumer = (MessageConsumer) projectInstances.remove(CONSUMER);
        projectInstances.remove(JMSLISTENER);
        try {
            if (consumer != null)
                consumer.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
        }
        catch (Exception e) {
            log.error("", e);
        }
        Application.getInstance().getGUILog().log("[INFO] sync ended");
    }

    public static AutoSyncCommitListener getCommitListener(Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances == null)
            return null;
        AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances.get(LISTENER);
        return listener;
    }
    
    @Override
    public void projectOpened(Project project) {
        Map<String, Object> projectInstances = new HashMap<String, Object>();
        ProjectListenerMapping.getInstance().put(project, projectInstances);
        //add commit listener here
        AutoSyncCommitListener listener = new AutoSyncCommitListener(false); //change to just set auto to true in existing listener
        MDTransactionManager transactionManager = (MDTransactionManager) project.getRepository()
                .getTransactionManager();
        listener.setTm(transactionManager);
        transactionManager.addTransactionCommitListenerIncludingUndoAndRedo(listener);
        projectInstances.put(LISTENER, listener);
        JSONObject previousUpdates = getUpdatesOrFailed(project, "update");
        if (previousUpdates != null) {
            for (String added: (List<String>)previousUpdates.get("added")) {
                Element e = ExportUtility.getElementFromID(added);
                if (e != null)
                    listener.getAddedElements().put(added, e);
            }
            for (String changed: (List<String>)previousUpdates.get("changed")) {
                Element e = ExportUtility.getElementFromID(changed);
                if (e != null)
                    listener.getChangedElements().put(changed, e);
            }
            for (String deleted: (List<String>)previousUpdates.get("deleted")) {
               listener.getDeletedElements().put(deleted, null);
            }
        }
    }

    @Override
    public void projectClosed(Project project) {
        close(project, false);
        ProjectListenerMapping.getInstance().remove(project);
    }
    
    @SuppressWarnings("unchecked")
    private static void saveAutoSyncErrors(Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER) || projectInstances.containsKey(JMSLISTENER)) {
            //autosync is on
            JMSMessageListener j = (JMSMessageListener)projectInstances.get(JMSLISTENER);
            if (j != null) {
                Set<String> cannotAdd = new HashSet<String>(j.getCannotAdd());
                Set<String> cannotChange = new HashSet<String>(j.getCannotChange());
                Set<String> cannotDelete = new HashSet<String>(j.getCannotDelete());
                JSONObject failed = getUpdatesOrFailed(project, "error");
                if (failed == null) {
                    failed = new JSONObject();
                    failed.put("added", new JSONArray());
                    failed.put("changed", new JSONArray());
                    failed.put("deleted", new JSONArray());
                }
                    JSONArray failedAdd = (JSONArray)failed.get("added");
                    JSONArray failedChange = (JSONArray)failed.get("changed");
                    JSONArray failedDelete = (JSONArray)failed.get("deleted");
                    cannotAdd.addAll(failedAdd);
                    cannotChange.addAll(failedChange);
                    cannotDelete.addAll(failedDelete);
                    failedAdd.clear();
                    failedChange.clear();
                    failedDelete.clear();
                    failedAdd.addAll(cannotAdd);
                    failedChange.addAll(cannotChange);
                    failedDelete.addAll(cannotDelete);
                    SessionManager sm = SessionManager.getInstance();
                    sm.createSession("save autosync error");
                    try {
                        setUpdatesOrFailed(project, failed, "error");
                        sm.closeSession();
                    } catch (Exception e) {
                        log.error("", e);
                        sm.cancelSession();
                    }     
            }
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public void saveLocalUpdates(Project project) {
        AutoSyncCommitListener listener = getCommitListener(project);
        JSONObject previousUpdates = getUpdatesOrFailed(project, "update");
        Set<String> added = new HashSet<String>();
        Set<String> changed = new HashSet<String>();
        Set<String> deleted = new HashSet<String>();
        if (previousUpdates != null) {
            added.addAll((JSONArray)previousUpdates.get("added"));
            changed.addAll((JSONArray)previousUpdates.get("changed"));
            deleted.addAll((JSONArray)previousUpdates.get("deleted"));
        }
        added.addAll(listener.getAddedElements().keySet());
        changed.addAll(listener.getChangedElements().keySet());
        deleted.addAll(listener.getDeletedElements().keySet());
        JSONObject notSaved = new JSONObject();
        JSONArray addeda = new JSONArray();
        JSONArray updateda = new JSONArray();
        JSONArray deleteda = new JSONArray();
        addeda.addAll(added);
        updateda.addAll(changed);
        deleteda.addAll(deleted);
        notSaved.put("added", addeda);
        notSaved.put("changed", updateda);
        notSaved.put("deleted", deleteda);
        SessionManager sm = SessionManager.getInstance();
        sm.createSession("mms delayed sync change logs");
        try {
            setUpdatesOrFailed(project, notSaved, "update");
            sm.closeSession();
        } catch (Exception e) {
            log.error("", e);
            sm.cancelSession();
        }        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        saveAutoSyncErrors(project);
        saveLocalUpdates(project);
    }
    
    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER) || projectInstances.containsKey(JMSLISTENER)) {
            //autosync is on
            ExportUtility.sendProjectVersion();
        }
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject()) && savedInServer) {
            String folderId = project.getPrimaryProject().getProjectID();
            folderId += "_sync";
            Element folder = ExportUtility.getElementFromID(folderId);
            if (folder != null)
                TeamworkUtils.unlockElement(project, folder, true, true, true);
        }
    }
}
