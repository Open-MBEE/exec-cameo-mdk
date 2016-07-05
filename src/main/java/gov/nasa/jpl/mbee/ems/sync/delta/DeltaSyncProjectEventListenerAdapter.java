package gov.nasa.jpl.mbee.ems.sync.delta;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
public class DeltaSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    public static Logger log = Logger.getLogger(DeltaSyncProjectEventListenerAdapter.class);

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss.SSSZ");
    private static DateFormat dfserver = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static void lockSyncFolder(Project project) {
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            String folderId = project.getPrimaryProject().getProjectID();
            folderId += "_sync";
            Element folder = ExportUtility.getElementFromID(folderId);
            if (folder == null)
                return;
            try {
                for (Element e : folder.getOwnedElement()) {
                    if (e instanceof Class)
                        Utils.tryToLock(project, e, true);
                }
            } catch (Exception e) {
                log.info("exception caught");
                e.printStackTrace();
            }
        }
    }

    /*
     * get sync blocks, ignore ones that have corresponding clear blocks,
     * if create is true, find one that's editable or create one and put it as the first element in the return array
     * if create is true, delete dangling clear blocks, and delete any block that has a clear block and the clear block if possible
     * only delete clear blocks if the block it's clearing can also be deleted
     * if clear is true, clear all existing blocks, delete if possible, and give back a modifiable block as first element
     */
    public static List<NamedElement> getSyncElement(Project project, boolean create, boolean clearAll, String prefix) {
        List<NamedElement> elements = new ArrayList<>();
        Map<String, NamedElement> nameMapping = new HashMap<>();
        String folderId = project.getPrimaryProject().getProjectID();
        folderId += "_sync";
        Element folder = ExportUtility.getElementFromID(folderId);
        if (folder == null) {
            if (!create)
                return elements;
            project.getCounter().setCanResetIDForObject(true);
            folder = project.getElementsFactory().createPackageInstance();
            folder.setOwner(project.getModel());
            ((Package) folder).setName("__MMSSync__");
            folder.setID(folderId);
        }

        for (Element e : folder.getOwnedElement()) {
            if (e instanceof Class) {
                String name = ((Class) e).getName();
                if (name.startsWith(prefix)) {
                    nameMapping.put(name, (NamedElement) e);
                    if (name.contains("clear"))
                        continue;
                    elements.add((NamedElement) e);
                }
            }
        }
        //nameMapping have map of name to block for all blocks with prefix
        //elements have all blocks with prefix that are not clear blocks
        List<NamedElement> canDelete = new ArrayList<>();
        for (NamedElement e : new ArrayList<>(elements)) {
            String name = e.getName();
            NamedElement clear = nameMapping.get(name + "_clear");
            if (clear != null) {
                elements.remove(e); //block has been processed
                if (e.isEditable()) {
                    canDelete.add(e);
                    if (clear.isEditable())
                        canDelete.add(clear);
                }
                continue;
            }
            //e is now a block with no clear block, potentially unprocessed block
            if (clearAll) {
                if (e.isEditable()) { //delete it
                    canDelete.add(e);
                    elements.remove(e);
                } else {            //cannot be deleted, add a clear block to indicate it's been processed
                    elements.remove(e);
                    NamedElement newClear = project.getElementsFactory().createClassInstance();
                    newClear.setName(e.getName() + "_clear");
                    newClear.setOwner(folder);
                }
            }
        }
        //find dangling clear blocks and add to canDelete
        for (String name : nameMapping.keySet()) {
            if (name.contains("_clear")) {
                String block = name.replace("_clear", "");
                if (nameMapping.get(block) == null) { //clear block is dangling
                    if (nameMapping.get(name).isEditable())
                        canDelete.add(nameMapping.get(name));
                }
            }
        }
        if (create || clearAll) {
            for (NamedElement e : canDelete) {
                try {
                    ModelElementsManager.getInstance().removeElement(e);
                } catch (ReadOnlyElementException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        //what's in elements array now are blocks that haven't been processed and should be returned, or empty if clearAll is true
        /*NamedElement editable = null;
        for (NamedElement e: elements) {
            if (e.isEditable()) {
                editable = e;
                break;
            }
        }
        if (editable != null) {
            elements.remove(editable);
            elements.add(0, editable);
        } else */
        if (create) {
            NamedElement modify = project.getElementsFactory().createClassInstance();
            modify.setOwner(folder);
            modify.setName(prefix + "_" + df.format(new Date()));
            elements.add(0, modify);
        }
        return elements;
    }

    //get a timestamp that should roughly be the last time someone pulled updates from mms using jms
    public static Date getLastDeltaTimestamp(Project project) {
        List<NamedElement> jmstimes = getSyncElement(project, false, false, "lastmms");
        Date res = new Date(100000);
        for (NamedElement e : jmstimes) {
            String name = e.getName();
            try {
                Date maybe = df.parse(name.substring(8));
                if (maybe.after(res))
                    res = maybe;
            } catch (ParseException ex) {
            }
        }
        return res;
    }

    public static void setUpdatesOrFailed(Project project, JSONObject o, String type, boolean clearAll) {
        List<NamedElement> es = getSyncElement(project, true, clearAll, type);
        es.get(0).setName(type + "_" + df.format(new Date()));
        ModelHelper.setComment(es.get(0), (o == null) ? "{\"deleted\":[], \"changed\":[], \"added\":[]}" : o.toJSONString());
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getUpdatesOrFailed(Project project, String type) {
        List<NamedElement> es = getSyncElement(project, false, false, type);
        if (es.isEmpty())
            return null;
        JSONObject update = new JSONObject();
        update.put("deleted", new JSONArray());
        update.put("changed", new JSONArray());
        update.put("added", new JSONArray());
        Set<String> deleted = new HashSet<String>();
        Set<String> changed = new HashSet<String>();
        Set<String> added = new HashSet<String>();
        for (Element e : es) {
            try {
                JSONObject updates = (JSONObject) JSONValue.parse(ModelHelper.getComment(e));
                deleted.addAll((JSONArray) updates.get("deleted"));
                changed.addAll((JSONArray) updates.get("changed"));
                added.addAll((JSONArray) updates.get("added"));
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        ((JSONArray) update.get("deleted")).addAll(deleted);
        ((JSONArray) update.get("changed")).addAll(changed);
        ((JSONArray) update.get("added")).addAll(added);
        return update;
    }

    public static void setConflicts(Project project, JSONObject o) {
        List<NamedElement> es = getSyncElement(project, true, true, "conflict");
        es.get(0).setName("conflict_" + df.format(new Date()));
        ModelHelper.setComment(es.get(0), (o == null) ? "{\"elements\":[]}" : o.toJSONString());
    }

    public static JSONObject getConflicts(Project project) {
        List<NamedElement> es = getSyncElement(project, false, false, "conflict");
        if (es.isEmpty())
            return null;
        JSONObject update = new JSONObject();
        update.put("elements", new JSONArray());
        Set<String> elements = new HashSet<String>();
        for (Element e : es) {
            try {
                JSONObject updates = (JSONObject) JSONValue.parse(ModelHelper.getComment(e));
                elements.addAll((JSONArray) updates.get("elements"));
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
        ((JSONArray) update.get("elements")).addAll(elements);
        return update;
    }

    /*public static void initDurable(final Project project) {
        DeltaSyncProjectMapping remote = getProjectMapping(project);
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();

        // Check if the keywords are found in the current project. If so, it
        // indicates that this JMS subscriber has already been init'ed.
        //
        if (deltaSyncProjectMapping.getConnection() != null || deltaSyncProjectMapping.getSession() != null || deltaSyncProjectMapping.getMessageConsumer() != null) {
            Utils.guilog("Dynamic sync was already started.");
            AutoSyncStatusConfigurator.getInstance().update(true);
            return;
        }
        Map<String, String> urlInfo = new HashMap<String, String>();
        getJMSInfo(urlInfo);
        String url = urlInfo.get("url");
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
            for (Element locked : lockedByAll) {
                if (!ProjectUtilities.isElementInAttachedProject(locked)) {
                    Utils.guilog("[ERROR] Another user has locked part of the project - dynamic sync will not start");
                    return;
                }
            }
            //if (!lockedByUser.equals(lockedByAll)) {
            //    Utils.guilog("[ERROR] Another user has locked part of the project - autosync will not start");
            //    return;
            //}
            for (Element e : project.getModel().getOwnedElement()) {
                if (ProjectUtilities.isElementInAttachedProject(e))
                    continue;
                if (!TeamworkUtils.lockElement(project, e, true)) {
                    Utils.guilog("[ERROR] Cannot lock project - dynamic sync will not start");
                    return;
                }
            }
        }
        String username = ViewEditUtils.getUsername();
        if (username == null || username.equals("")) {
            Utils.guilog("[ERROR] You must be logged into MMS first - dynamic sync will not start");
            return;
        }
        try {
            ConnectionFactory connectionFactory = createConnectionFactory(urlInfo);
            String subscriberId = projectID + "-" + wsID + "-" + username; // weblogic can't have '/' in id
            Connection connection = connectionFactory.createConnection();
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException e) {
                    Utils.guilog(e.getMessage());
                    log.error(e.getMessage(), e);
                    //if (e instanceof LostServerConnection) {

                    //}
                    // TODO REVIEW ME @Ivan
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Utils.guilog("[WARNING] Dynamic sync interrupted.");
                            //DeltaSyncProjectEventListenerAdapter.close(project, true);
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
                    topic = (Topic) ctx.lookup(JMS_TOPIC);
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

            deltaSyncProjectMapping.setJmsMessageListener(jmslistener);
            deltaSyncProjectMapping.setConnection(connection);
            deltaSyncProjectMapping.setSession(session);
            deltaSyncProjectMapping.setMessageConsumer(consumer);

            Utils.guilog("[INFO] sync initiated");
            AutoSyncStatusConfigurator.getInstance().update(true);

        } catch (Exception e) {
            log.error("", e);
            Utils.guilog("[ERROR] Sync initialization failed: " + e.getMessage());
        }
    }*/

    @Override
    public void projectClosed(Project project) {

    }

    @SuppressWarnings("unchecked")
    private static void saveAutoSyncErrors(Project project) {
        /*DeltaSyncProjectMapping deltaSyncProjectMapping = getProjectMapping(project);
        if (deltaSyncProjectMapping.getConnection() != null || deltaSyncProjectMapping.getSession() != null || deltaSyncProjectMapping.getMessageConsumer() != null || deltaSyncProjectMapping.getJmsMessageListener() != null) {
            //autosync is on
            JMSMessageListener jmsMessageListener = deltaSyncProjectMapping.getJmsMessageListener();
            if (jmsMessageListener != null) {
                Set<String> cannotAdd = new HashSet<String>(jmsMessageListener.getCannotAdd());
                Set<String> cannotChange = new HashSet<String>(jmsMessageListener.getCannotChange());
                Set<String> cannotDelete = new HashSet<String>(jmsMessageListener.getCannotDelete());
                if (cannotAdd.isEmpty() && cannotChange.isEmpty() && cannotDelete.isEmpty())
                    return;
                JSONObject failed = new JSONObject();
                JSONArray failedAdd = new JSONArray();
                failedAdd.addAll(cannotAdd);
                JSONArray failedChange = new JSONArray();
                failedChange.addAll(cannotChange);
                JSONArray failedDelete = new JSONArray();
                failedDelete.addAll(cannotDelete);
                failed.put("added", failedAdd);
                failed.put("changed", failedChange);
                failed.put("deleted", failedDelete);
                SessionManager sm = SessionManager.getInstance();
                sm.createSession("save autosync error");
                try {
                    setUpdatesOrFailed(project, failed, "error", false);
                    sm.closeSession();
                    jmsMessageListener.getCannotAdd().clear();
                    jmsMessageListener.getCannotChange().clear();
                    jmsMessageListener.getCannotDelete().clear();
                } catch (Exception e) {
                    log.error("", e);
                    sm.cancelSession();
                }
            }
        }*/
    }


    @SuppressWarnings("unchecked")
    public void saveLocalUpdates(Project project) {
        CommonSyncTransactionCommitListener listener = CommonSyncProjectEventListenerAdapter.getProjectMapping(project).getCommonSyncTransactionCommitListener();
        final Set<String> newCreated = listener.getInMemoryChangelog().get(Changelog.ChangeType.CREATED).keySet(),
                newUpdated = listener.getInMemoryChangelog().get(Changelog.ChangeType.UPDATED).keySet(),
                newDeleted = listener.getInMemoryChangelog().get(Changelog.ChangeType.DELETED).keySet();
        if (newCreated.isEmpty() && newUpdated.isEmpty() && newDeleted.isEmpty())
            return; //no need to save if nothing to save
        JSONObject notSaved = new JSONObject();
        JSONArray addeda = new JSONArray();
        JSONArray updateda = new JSONArray();
        JSONArray deleteda = new JSONArray();

        addeda.addAll(newCreated);
        updateda.addAll(newUpdated);
        deleteda.addAll(newDeleted);

        notSaved.put("added", addeda);
        notSaved.put("changed", updateda);
        notSaved.put("deleted", deleteda);

        SessionManager sm = SessionManager.getInstance();
        sm.createSession("mms delayed sync change logs");
        try {
            setUpdatesOrFailed(project, notSaved, "update", false);
            sm.closeSession();

            // clear to prevent memory usage from going to infinity (and beyond); should solve "memory leak"
            // never gets here upon exception
            // prevents (or at least minimized) duplication of elements existing in memory and in the persistent history
            // checked DeltaSyncRunner to ensure that this should not cause an issue as it checks for both memory and historical changes
            listener.getInMemoryChangelog().clear();
        } catch (Exception e) {
            log.error("", e);
            sm.cancelSession();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        boolean save = MDKOptionsGroup.getMDKOptions().isSaveChanges();
        if (!save)
            return;
        if (!StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem"))
            return;
        try {
            saveLocalUpdates(project);
            saveAutoSyncErrors(project);
        } catch (Exception e) {
            log.error("", e); //potential session isn't created error if need to update from tw while commiting
        }
    }

    /*@Override
    public void projectSaved(Project project, boolean savedInServer) {
        boolean save = MDKOptionsGroup.getMDKOptions().isSaveChanges();
        if (!save)
            return;
        DeltaSyncProjectMapping deltaSyncProjectMapping = getProjectMapping(project);
        if (deltaSyncProjectMapping.getConnection() != null || deltaSyncProjectMapping.getSession() != null || deltaSyncProjectMapping.getMessageConsumer() != null || deltaSyncProjectMapping.getJmsMessageListener() != null) {
            //autosync is on
            ExportUtility.sendProjectVersion();
        }
        /*if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject()) && savedInServer) {
            String folderId = project.getPrimaryProject().getProjectID();
            folderId += "_sync";
            Element folder = ExportUtility.getElementFromID(folderId);
            if (folder != null)
                TeamworkUtils.unlockElement(project, folder, true, true, true);
        }* / //unlock apparently can take a long time on big model
    }*/

    /*
    // TODO Figure out how these need to be post-filtered based on lastDeltaTimestamp
    @SuppressWarnings("unchecked")
    public static Changelog<String, Void> getJMSChangelog(Project project) {
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        if (wsID == null) {
            Utils.guilog("[ERROR] Cannot get server workspace that corresponds to this project branch");
            return null;
        }
        JMSUtils.JMSInfo jmsInfo = JMSUtils.getJMSInfo(Application.getInstance().getProject());
        String url = jmsInfo.getUrl();
        if (url == null) {
            Utils.guilog("[ERROR] Cannot get server url");
            return null;
        }
        String username = ViewEditUtils.getUsername();
        if (username == null || username.isEmpty()) {
            Utils.guilog("[ERROR] You must be logged into MMS first");
            return null;
        }
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        Changelog<String, Void> changelog = new Changelog<>();
        Map<String, Void> addedChanges = changelog.get(Changelog.ChangeType.CREATED),
                modifiedChanges = changelog.get(Changelog.ChangeType.UPDATED),
                deletedChanges = changelog.get(Changelog.ChangeType.DELETED);

        JSONObject previousFailed = getUpdatesOrFailed(project, "error");
        if (previousFailed != null) {
            addedChanges.keySet().addAll((List<String>) previousFailed.get("added"));
            modifiedChanges.keySet().addAll((List<String>) previousFailed.get("changed"));
            deletedChanges.keySet().addAll((List<String>) previousFailed.get("deleted"));
        }
        JSONObject previousConflicts = conflicts(project);
        if (previousConflicts != null) {
            modifiedChanges.keySet().addAll((List<String>) previousConflicts.get("elements"));
        }
        Date lastTime = getLastDeltaTimestamp(project);
        try {
            ConnectionFactory connectionFactory = JMSUtils.createConnectionFactory(jmsInfo);
            String subscriberId = projectID + "-" + wsID + "-" + username; // weblogic can't have '/' in id
            connection = connectionFactory.createConnection();
            connection.setClientID(subscriberId);// + (new Date()).toString());
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            // weblogic createTopic doesn't work if it already exists, unlike activemq
            Topic topic = null;
            try {
                if (JMSUtils.getInitialContext() != null) {
                    topic = (Topic) JMSUtils.getInitialContext().lookup(JMSUtils.JMS_TOPIC);
                }
            } catch (NameNotFoundException nnfe) {
                // do nothing (just means topic hasnt been created yet
            } finally {
                if (topic == null) {
                    topic = session.createTopic(JMSUtils.JMS_TOPIC);
                }
            }
            String messageSelector = JMSUtils.constructSelectorString(projectID, wsID);
            consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            connection.start();
            Message m = consumer.receive(10000);
            boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
            Date newTime = new Date(lastTime.getTime());
            while (m != null) {
                TextMessage message = (TextMessage) m;
                if (print)
                    log.info("From JMS (Manual receive): " + message.getText());
                JSONObject ob = (JSONObject) JSONValue.parse(message.getText());
                boolean magicdraw = false;
                if (ob.get("source") != null && ob.get("source").equals("magicdraw")) {
                    //m = consumer.receive(1000);
                    magicdraw = true;
                    //continue;
                }
                JSONObject ws2 = (JSONObject) ob.get("workspace2");
                if (ws2 == null) {
                    m.acknowledge();
                    m = consumer.receive(3000);
                    continue;
                }
                String timestamp = (String) ws2.get("timestamp");
                try {
                    if (timestamp != null) {
                        Date jmsTime = dfserver.parse(timestamp);
                        if (!jmsTime.after(lastTime)) {
                            m.acknowledge();
                            m = consumer.receive(3000);
                            continue; //ignore messages before last delta time in case someone else already processed them
                        }
                        if (jmsTime.after(newTime))
                            newTime = jmsTime;
                    }
                } catch (ParseException ex) {
                }
                final JSONArray updated = (JSONArray) ws2.get("updatedElements");
                final JSONArray added = (JSONArray) ws2.get("addedElements");
                final JSONArray deleted = (JSONArray) ws2.get("deletedElements");
                final JSONArray moved = (JSONArray) ws2.get("movedElements");
                for (Object e : updated) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!magicdraw) {
                        modifiedChanges.put(id, null);
                    }
                    deletedChanges.remove(id);
                }
                for (Object e : added) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!magicdraw) {
                        addedChanges.put(id, null);
                    }
                    deletedChanges.remove(id);
                }
                for (Object e : moved) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!magicdraw) {
                        modifiedChanges.put(id, null);
                    }
                    deletedChanges.remove(id);
                }
                for (Object e : deleted) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!magicdraw) {
                        modifiedChanges.put(id, null);
                    }
                    addedChanges.remove(id);
                    modifiedChanges.remove(id);
                }
                m.acknowledge();
                m = consumer.receive(3000);
            }
            //lockSyncFolder(project);
            SessionManager sm = SessionManager.getInstance();
            sm.createSession("mms delayed sync change logs");
            try {
                //setUpdatesOrFailed(project, null, "error");
                List<NamedElement> jmstimes = getSyncElement(project, true, true, "lastmms");
                jmstimes.get(0).setName("lastmms_" + df.format(newTime));
                sm.closeSession();
            } catch (Exception e) {
                sm.cancelSession();
            }
            return changelog;
        } catch (Exception e) {
            log.error("JMS (Manual receive): ", e);
            Utils.guilog("[ERROR] Getting changes from MMS failed, someone else may already be connected, please try again later (or check your internet connection). If error persists, please submit a JIRA on https://cae-jira.jpl.nasa.gov/projects/SSCAES/summary");
            Utils.guilog("[ERROR] Server message: " + e.getMessage());
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
     */
}
