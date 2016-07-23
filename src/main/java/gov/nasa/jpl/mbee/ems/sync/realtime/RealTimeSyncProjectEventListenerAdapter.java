package gov.nasa.jpl.mbee.ems.sync.realtime;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.ems.sync.delta.DeltaSyncRunner;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;

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
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }
        Application.getInstance().getGUILog().log("Opened " + project.getID());
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }
        // ...
    }

    @Override
    public void projectActivated(Project project) {
        projectOpened(project);
    }

    @Override
    public void projectClosed(Project project) {
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }
        Application.getInstance().getGUILog().log("Closed " + project.getID());
        // ...
        projectMappings.remove(project.getID());
        Utils.guilog("[INFO] Sync stopped for project " + project.getName());
    }

    @Override
    public void projectDeActivated(Project project) {
        projectClosed(project);
    }

    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        // TODO Handle isTeamworkProject && !savedInServer @Ivan
        /*boolean tempDisabled = true;
        if (tempDisabled) {
            return;
        }*/
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }
        Application.getInstance().getGUILog().log("Pre-saving " + project.getID());
        boolean save = MDKOptionsGroup.getMDKOptions().isSaveChanges();
        if (!save) {
            return;
        }
        if (!StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem")) {
            return;
        }
        DeltaSyncRunner msr = new DeltaSyncRunner(true, true);
        ProgressStatusRunner.runWithProgressStatus(msr, "Delta Sync", true, 0);
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
        private volatile boolean disabled;

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
}
