package gov.nasa.jpl.mbee.ems.sync.realtime;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncStatusConfigurator;
import gov.nasa.jpl.mbee.ems.sync.JMSMessageListener;
import gov.nasa.jpl.mbee.lib.Utils;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/22/16.
 */
public class RealTimeSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<String, RealTimeSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();

    private static RealTimeSyncProjectEventListenerAdapter instance;

    public RealTimeSyncProjectEventListenerAdapter getInstance() {
        if (instance == null) {
            instance = new RealTimeSyncProjectEventListenerAdapter();
        }
        return instance;
    }

    @Override
    public void projectOpened(Project project) {
        Application.getInstance().getGUILog().log("Opened " + project.getID());
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }
        try {
            if (realTimeSyncProjectMapping.getMessageConsumer() != null) {
                realTimeSyncProjectMapping.getMessageConsumer().close();
            }
            if (realTimeSyncProjectMapping.getSession() != null) {
                realTimeSyncProjectMapping.getSession().close();
            }
            if (realTimeSyncProjectMapping.getConnection() != null) {
                realTimeSyncProjectMapping.getConnection().close();
            }
        } catch (Exception e) {
            Utils.printException(e);
        }

    }

    @Override
    public void projectActivated(Project project) {
        projectOpened(project);
    }

    @Override
    public void projectClosed(Project project) {
        Application.getInstance().getGUILog().log("Closed " + project.getID());
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        try {
            if (realTimeSyncProjectMapping.getMessageConsumer() != null) {
                realTimeSyncProjectMapping.getMessageConsumer().close();
            }
            if (realTimeSyncProjectMapping.getSession() != null) {
                realTimeSyncProjectMapping.getSession().close();
            }
            if (realTimeSyncProjectMapping.getConnection() != null) {
                realTimeSyncProjectMapping.getConnection().close();
            }
        } catch (Exception e) {
            Utils.printException(e);
        }
        projectMappings.remove(project.getID());
        Utils.guilog("[INFO] Sync stopped for project " + project.getName());
        // TODO REVIEW ME @Ivan
        AutoSyncStatusConfigurator.getStatusAction().update(false);
    }

    @Override
    public void projectDeActivated(Project project) {
        projectClosed(project);
    }

    /*
    private void sendChanges() {
        JSONObject toSend = new JSONObject();
        JSONArray eles = new JSONArray();
        eles.addAll(elements.values());
        toSend.put("elements", eles);
        toSend.put("source", "magicdraw");
        toSend.put("mmsVersion", "2.3");
        if (!eles.isEmpty()) {
            String url = ExportUtility.getPostElementsUrl();
            if (url != null) {
                Request r = new Request(url, toSend.toJSONString(), "POST", false, eles.size(), "Autosync Changes");
                gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue.getInstance().offer(r);
            }
        }
        if (!deletes.isEmpty()) {
            String deleteUrl = ExportUtility.getUrlWithWorkspace();
            JSONObject send = new JSONObject();
            JSONArray elements = new JSONArray();
            send.put("elements", elements);
            send.put("source", "magicdraw");
            send.put("mmsVersion", "2.3");
            for (String id : deletes) {
                JSONObject eo = new JSONObject();
                eo.put("sysmlid", id);
                elements.add(eo);
            }
            gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue.getInstance().offer(new Request(deleteUrl + "/elements", send.toJSONString(), "DELETEALL", false, elements.size(), "Autosync Deletes"));
        }
    }
    */

    public static RealTimeSyncProjectMapping getProjectMapping(Project project) {
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = projectMappings.get(project.getID());
        if (realTimeSyncProjectMapping == null) {
            projectMappings.put(project.getID(), realTimeSyncProjectMapping = new RealTimeSyncProjectMapping());
        }
        return realTimeSyncProjectMapping;
    }

    public static class RealTimeSyncProjectMapping {
        private Connection connection;
        private Session session;
        private MessageConsumer messageConsumer;
        private JMSMessageListener jmsMessageListener;
        private volatile boolean disabled;

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public Session getSession() {
            return session;
        }

        public void setSession(Session session) {
            this.session = session;
        }

        public MessageConsumer getMessageConsumer() {
            return messageConsumer;
        }

        public void setMessageConsumer(MessageConsumer messageConsumer) {
            this.messageConsumer = messageConsumer;
        }

        public JMSMessageListener getJmsMessageListener() {
            return jmsMessageListener;
        }

        public void setJmsMessageListener(JMSMessageListener jmsMessageListener) {
            this.jmsMessageListener = jmsMessageListener;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
}
