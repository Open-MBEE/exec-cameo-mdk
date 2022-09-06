package gov.nasa.jpl.mbee.mdk.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProjectSettings {

    private static final Cache<Project, String> PROFILE_SERVER_CACHE = CacheBuilder.newBuilder().weakKeys().maximumSize(100).expireAfterAccess(10, TimeUnit.MINUTES).build();

    public static final String
            MMS_HOST_URL = "mms.hostUrl",
            MMS_BASE_PATH = "mms.basePath",
            VE_HOST_URL = "ve.hostUrl",
            VE_BASE_PATH = "ve.basePath";


    public static ObjectNode getProjectSettings(Project project) {
        Package model = project.getPrimaryModel();
        Comment comment = new ArrayList<>(model.getOwnedComment()).get(0);

        String s = StringUtils.substring(comment.getBody(),StringUtils.indexOf(comment.getBody(),"{"),StringUtils.lastIndexOf(comment.getBody(),"}")+1);
        if (s == null) {
            Application.getInstance().getGUILog().log("[WARNING] Unable to retrieve MDK settings from model documentation");
            return null;
        }
        ObjectNode settingsNode;
        try {
            settingsNode = JacksonUtils.parseJsonString(s);
        } catch (IOException ioException) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to retrieve MDK settings from model documentation, your JSON may be malformed check the following message for details on how to fix the issue:");
            Application.getInstance().getGUILog().log(ioException.getMessage());
            return null;
        }
        return settingsNode;
    }

    public static String get(Project project, String settingName, Boolean useDefault) {
        JsonNode settings = ProjectSettings.getProjectSettings(project);
        if (settings != null) {
            String[] fields = settingName.split("\\.");
            for (String field : fields) {
                if (settings != null) {
                    settings = settings.get(field);
                }
            }
            if (settings != null && settings.isTextual()) {
                return settings.asText();
            }
        }
        return useDefault ? getDefault(project, settingName) : null;
    }

    public static String getOrDefault(Project project, String settingName) {
        return get(project,settingName,true);
    }

    public static String get(Project project, String settingName) {
        return get(project,settingName,false);
    }

    private static String getDefault(Project project, String settingName) {
        switch (settingName) {
            case VE_HOST_URL:
                return getOrDefault(project,MMS_HOST_URL);
            case VE_BASE_PATH:
                return "/alfresco/mmsapp/mms.html#";
            case MMS_HOST_URL:
                return getStereotypeUrl(project);
            case MMS_BASE_PATH:
                return "";
        }

        return "Not Found";
    }

    public static String getMmsUrl(Project project) {
        return getOrDefault(project, MMS_HOST_URL);
    }

    /**
     * @param project
     * @return
     * @throws IllegalStateException
     */
    public static String getStereotypeUrl(Project project) throws IllegalStateException {
        String urlString;
        if (project == null) {
            throw new IllegalStateException("Project is null.");
        }
        Element primaryModel = project.getPrimaryModel();
        if (primaryModel == null) {
            throw new IllegalStateException("Model is null.");
        }

        if (StereotypesHelper.hasStereotype(primaryModel, "ModelManagementSystem")) {
            urlString = (String) StereotypesHelper.getStereotypePropertyFirst(primaryModel, "ModelManagementSystem", "MMS URL");
        }
        else if (ProjectUtilities.isStandardSystemProfile(project.getPrimaryProject())) {
            urlString = PROFILE_SERVER_CACHE.getIfPresent(project);
            if (urlString == null) {
                urlString = JOptionPane.showInputDialog("Specify server URL for standard profile.", null);
            }
            if (urlString == null || urlString.trim().isEmpty()) {
                return null;
            }
            PROFILE_SERVER_CACHE.put(project, urlString);
        }
        else {
            Utils.showPopupMessage("The root element does not have the ModelManagementSystem stereotype.\nPlease apply it and specify the server information.");
            return null;
        }
        if (urlString == null || urlString.isEmpty()) {
            return null;
        }
        return urlString.trim();
    }

}
