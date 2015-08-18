package gov.nasa.jpl.mbee.ems.sync;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.swing.SwingUtilities;

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
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

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

    // Members to look up JMS using JNDI
    // TODO: If any other context factories are used, need to add those JARs into class path (e.g., for weblogic)
    private static String JMS_CTX_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private static String JMS_CONN_FACTORY = "ConnectionFactory";
    private static String JMS_USERNAME = null;
    private static String JMS_PASSWORD = null;
    private static String JMS_TOPIC = "master";
    private static InitialContext ctx = null; 
        
    public static void getJMSUrl(Map<String, String> urlInfo) {
        // urlInfo necessary for backwards compatibility with 2.1 MMS, which doesn't have service call
        JSONObject jmsJson = ExportUtility.getJmsConnectionDetails();
        String url = ingestJson(jmsJson);
        if (url != null) { 
            urlInfo.put( "isFromService", "true" );
        } else {
            urlInfo.put( "isFromService", "false" );
            url = ExportUtility.getUrl();
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
        }
        urlInfo.put( "url", url );
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
            ((Package)folder).setName("__MMSSync__");
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
        	Utils.guilog("[INFO] Dynamic sync is currently on, you cannot do a manual update/commit while dynamic sync is on.");
            return null; //autosync is on, should turn off first
        }
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        Map<String, String> urlInfo = new HashMap<String, String>();
        getJMSUrl(urlInfo);
        String url = urlInfo.get( "url" );
        if (url == null) {
            Utils.guilog("[ERROR] Cannot get server url");
            return null;
        }
        if (wsID == null) {
            Utils.guilog("[ERROR] Cannot get server workspace that corresponds to this project branch");
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
            ConnectionFactory connectionFactory = createConnectionFactory(urlInfo);
            String subscriberId = projectID + "-" + wsID; // weblogic can't have '/' in id
            connection = connectionFactory.createConnection();
            connection.setClientID(subscriberId);// + (new Date()).toString());
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            // weblogic createTopic doesn't work if it already exists, unlike activemq
            Topic topic = null;
            try {
                if (ctx != null) {
                    topic = (Topic) ctx.lookup( JMS_TOPIC );
                }                    
            } catch (NameNotFoundException nnfe) {
                // do nothing (just means topic hasnt been created yet
            } finally {
                if (topic == null) {
                    topic = session.createTopic(JMS_TOPIC);
                }
            }
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
                //setUpdatesOrFailed(project, null, "error");
                setConflicts(project, null);
                sm.closeSession();
            } catch (Exception e) {
                sm.cancelSession();
            }
            return changes;
        } catch (Exception e) {
            log.error("JMS (Manual receive): ", e);
            Utils.guilog("[ERROR] Getting changes from mms failed: " + e.getMessage());
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
    
    public static void initDurable(final Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        
        // Check if the keywords are found in the current project. If so, it
        // indicates that this JMS subscriber has already been init'ed.
        //
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER)) {// || projectInstances.containsKey(LISTENER)) {
        	Utils.guilog("Dynamic sync was already started.");
        	AutosyncStatusConfigurator.getAutosyncStatusAction().update(true);
            return;
        }
        Map<String, String> urlInfo = new HashMap<String, String>();
        getJMSUrl(urlInfo);
        String url = urlInfo.get( "url" );
        if (url == null) {
            Utils.guilog("[ERROR] Sync initialization failed - cannot get server url");
            return;
        }
        if (wsID == null) {
            Utils.guilog("[ERROR] Sync initialization failed - cannot get server workspace that corresponds to this project branch");
            return;
        }
        Integer webVersion = ExportUtility.getAlfrescoProjectVersion(ExportUtility.getProjectId(project));
        Integer localVersion = ExportUtility.getProjectVersion(project);
        if (localVersion != null && !localVersion.equals(webVersion)) {
            Utils.guilog("[ERROR] Dynamic sync not allowed - project versions currently don't match - project may be out of date");
            return;
        }
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            String user = TeamworkUtils.getLoggedUserName();
            if (user == null) {
                Utils.guilog("[ERROR] You must be logged into teamwork - dynamic sync will not start");
                return;
            }
            Collection<Element> lockedByUser = TeamworkUtils.getLockedElement(project, user);
            Collection<Element> lockedByAll = TeamworkUtils.getLockedElement(project, null);
            lockedByAll.removeAll(lockedByUser);
            for (Element locked: lockedByAll) {
                if (!ProjectUtilities.isElementInAttachedProject(locked)) {
                    Utils.guilog("[ERROR] Another user has locked part of the project - dynamic sync will not start");
                    return;
                }
            }
            //if (!lockedByUser.equals(lockedByAll)) {
            //    Utils.guilog("[ERROR] Another user has locked part of the project - autosync will not start");
            //    return;
            //}
            for (Element e: project.getModel().getOwnedElement()) {
                if (ProjectUtilities.isElementInAttachedProject(e))
                    continue;
                if (!TeamworkUtils.lockElement(project, e, true)) {
                    Utils.guilog("[ERROR] Cannot lock project - dynamic sync will not start");
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

            ConnectionFactory connectionFactory = createConnectionFactory(urlInfo);
            String subscriberId = projectID + "-" + wsID; // weblogic can't have '/' in id
            Connection connection = connectionFactory.createConnection();
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException e) {
                    Utils.guilog(e.getMessage());
                    log.error(e.getMessage(), e);
                    //if (e instanceof LostServerConnection) {
                        
                    //}
                    SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							Utils.guilog("[WARNING] Dynamic sync interruppted.");
							AutoSyncProjectListener.close(project, true);
						}
                    });
                }
            });
            connection.setClientID(subscriberId);// + (new Date()).toString());
            // connection.setExceptionListener(this);
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            Topic topic = null;
            try {
                if (ctx != null) {
                    topic = (Topic) ctx.lookup( JMS_TOPIC );
                }                    
            } catch (NameNotFoundException nnfe) {
                // do nothing (just means topic hasnt been created yet
            } finally {
                if (topic == null) {
                    topic = session.createTopic(JMS_TOPIC);
                }
            }

            String messageSelector = constructSelectorString(projectID, wsID);
            
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            JMSMessageListener jmslistener = new JMSMessageListener(project);
            consumer.setMessageListener(jmslistener);
            connection.start();
            projectInstances.put(JMSLISTENER, jmslistener);
            projectInstances.put(CONNECTION, connection);
            projectInstances.put(SESSION, session);
            projectInstances.put(CONSUMER, consumer);

            Utils.guilog("[INFO] sync initiated");
            AutosyncStatusConfigurator.getAutosyncStatusAction().update(true);
            
        }
        catch (Exception e) {
            log.error("", e);
            Utils.guilog("[ERROR] Sync initialization failed: " + e.getMessage());
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
        Utils.guilog("[INFO] Sync ended");
        AutosyncStatusConfigurator.getAutosyncStatusAction().update(false);
    }

    public static AutoSyncCommitListener getCommitListener(Project project) {
    	if (project == null)
    		return null;
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
        /*JSONObject previousUpdates = getUpdatesOrFailed(project, "update");
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
        }*/
    }

    @Override
    public void projectClosed(Project project) {
        close(project, false);
        ProjectListenerMapping.getInstance().remove(project);
    }
    
    @SuppressWarnings("unchecked")
    private static void saveAutoSyncErrors(Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances == null)
            return;
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
        
        Set<String> added = new HashSet<String>(), changed = new HashSet<String>(), deleted = new HashSet<String>();
        JSONArray previousAdded = new JSONArray(), previousChanged = new JSONArray(), previousDeleted = new JSONArray();
        final Set<String> newAdded = listener.getAddedElements().keySet(), newChanged = listener.getChangedElements().keySet(), newDeleted = listener.getDeletedElements().keySet();
        
        if (previousUpdates != null) {
        	if (previousUpdates.containsKey("added"))
        		previousAdded = (JSONArray) previousUpdates.get("added");
        	if (previousUpdates.containsKey("changed"))
        		previousChanged = (JSONArray) previousUpdates.get("changed");
        	if (previousUpdates.containsKey("deleted"))
        			previousDeleted = (JSONArray) previousUpdates.get("deleted");
        }
        
        // fix to prevent the new update block from having all three (deleted, added, changed)
        // this happens when the user creates, saves, deletes, and saves
        // previously resulted in an ID existing in deleted, added, AND changed
        for (final String s : newDeleted) {
        	previousAdded.remove(s);
        	previousChanged.remove(s);
        }
        
        // fix for when a user deletes an element, saves, then undoes the deletion (resulting in an addition), then saves
        // no modification on changed as we cannot conclusively deduce its accuracy based on the information available - taking conservative approach
        // previously resulted in an ID existing in deleted, added, AND changed
        for (final String s : newAdded) {
        	previousDeleted.remove(s);
        }
        
        added.addAll(previousAdded);
        changed.addAll(previousChanged);
        deleted.addAll(previousDeleted);
        
        added.addAll(newAdded);
        changed.addAll(newChanged);
        deleted.addAll(newDeleted);
        
        //Utils.guilog(newAdded.size() + " : " + newChanged.size() + " : " + newDeleted.size());
        
        /*if (previousUpdates != null) {
            added.addAll((JSONArray)previousUpdates.get("added"));
            changed.addAll((JSONArray)previousUpdates.get("changed"));
            deleted.addAll((JSONArray)previousUpdates.get("deleted"));
        }
        added.addAll(listener.getAddedElements().keySet());
        changed.addAll(listener.getChangedElements().keySet());
        deleted.addAll(listener.getDeletedElements().keySet());
        
        for (final String s : deleted) {
        	System.out.println("Removed changed: " + Boolean.toString(changed.remove(s)));
        	System.out.println("Removed added: " + Boolean.toString(added.remove(s)));
        }*/
        
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
            
            // clear to prevent memory usage from going to infinity (and beyond); should solve "memory leak"
            // never gets here upon exception
            // prevents (or at least minimized) duplication of elements existing in memory and in the persistent history
            // checked ManualSyncRunner to ensure that this should not cause an issue as it checks for both memory and historical changes
            listener.getAddedElements().clear();
            listener.getChangedElements().clear();
            listener.getDeletedElements().clear();
        } catch (Exception e) {
            log.error("", e);
            sm.cancelSession();
        }        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        if (!StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem"))
            return;
        try {
            saveAutoSyncErrors(project);
            saveLocalUpdates(project);
        } catch (Exception e) {
            log.error("", e); //potential session isn't created error if need to update from tw while commiting
        }
    }
    
    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances == null)
            return; //investigate how this is possible
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER) || projectInstances.containsKey(JMSLISTENER)) {
            //autosync is on
            ExportUtility.sendProjectVersion();
        }
        /*if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject()) && savedInServer) {
            String folderId = project.getPrimaryProject().getProjectID();
            folderId += "_sync";
            Element folder = ExportUtility.getElementFromID(folderId);
            if (folder != null)
                TeamworkUtils.unlockElement(project, folder, true, true, true);
        }*/ //unlock apparently can take a long time on big model
    }
    
    /**
     * Ingests JSON data generated from MMS server and populates JNDI members
     * 
     * @return URL string of connector
     */
    protected static String ingestJson(JSONObject jsonInput) {
        if (jsonInput == null) return null;
        JSONObject json = null;
        if (jsonInput.containsKey( "connections" )) {
            // just grab first connection
            JSONArray conns = (JSONArray)jsonInput.get( "connections" );
            for (int ii = 0; ii < conns.size(); ii++) {
                json = (JSONObject) conns.get( ii );
                if (json.containsKey( "eventType" )) {
                    if (json.get( "eventType" ).equals( "DELTA" )) 
                        break;
                }
            }
        } else {
            json = jsonInput;
        }
        String result = null;

        if (json.containsKey( "uri" )) {
            result = (String)json.get( "uri" );
        }
        if (json.containsKey( "connFactory" )) {
            JMS_CONN_FACTORY = (String)json.get( "connFactory" );
        }
        if (json.containsKey( "ctxFactory" )) {
            JMS_CTX_FACTORY = (String)json.get( "ctxFactory" );
        }
        if (json.containsKey( "password" )) {
            JMS_PASSWORD = (String)json.get( "password" );
        }
        if (json.containsKey( "username" )) {
            JMS_USERNAME = (String)json.get( "username" );
        }
        if (json.containsKey( "topicName" )) {
            JMS_TOPIC = (String)json.get( "topicName" );
        }

        return result;
    }


    /**
     * Create a connection factory based on JNDI values
     * @return
     */
    public static ConnectionFactory createConnectionFactory(Map<String, String> urlInfo) {
        boolean isFromService = urlInfo.get( "isFromService" ).equals( "true" ) ? true : false;
        String url = urlInfo.get("url");
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, JMS_CTX_FACTORY);
        properties.put(Context.PROVIDER_URL, url);
        if (JMS_USERNAME != null && JMS_PASSWORD != null) {
            properties.put(Context.SECURITY_PRINCIPAL, JMS_USERNAME);
            properties.put(Context.SECURITY_CREDENTIALS, JMS_PASSWORD);
        }
        ctx = null;
        try {
            ctx = new InitialContext(properties);
        } catch (NamingException ne) {
            // FIXME: getting java.lang.ClassNotFoundException: org.apache.activemq.jndi.ActiveMQInitialContextFactory
            //        works in debugging from Eclipse - somehow classpath doesn't work
            //        plugin has the activemq-all reference, as workaround set to false for now
            isFromService = false;
        }

        if (isFromService == false) {
            return new ActiveMQConnectionFactory(url);
        } else {
            try {
                return (ConnectionFactory) ctx.lookup(JMS_CONN_FACTORY);
            }
            catch (NamingException ne) {
                ne.printStackTrace(System.err);
                return null;
            }
        }
    }
    
}
