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
import java.util.concurrent.TimeUnit;

public class ProjectSettings {

    private static final Cache<Project, String> PROFILE_SERVER_CACHE = CacheBuilder.newBuilder().weakKeys().maximumSize(100).expireAfterAccess(10, TimeUnit.MINUTES).build();

    public static final String
            MMS_URL = "mmsUrl",
            VE_HOST = "veHost",
            VE_PATH = "vePath";


    public static ObjectNode getProjectSettings(Project project) {
        Package model = project.getPrimaryModel();
        Comment comment = new ArrayList<>(model.getOwnedComment()).get(0);

        String s = StringUtils.substringBetween(comment.getBody(),"{","}");
        if (s == null) {
            return null;
        }
        s = "{" + s + "}";
        ObjectNode settingsNode;
        try {
            settingsNode = JacksonUtils.parseJsonString(s);
        } catch (IOException ioException) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to retrieve MDK settings from model documentation:" + ioException.getMessage());
            return null;
        }
        return settingsNode;
    }



    public static String get(Project project, String settingName, Boolean useDefault) {
        ObjectNode settingsNode = ProjectSettings.getProjectSettings(project);
        if (settingsNode != null) {
            JsonNode setting = settingsNode.get(settingName);
            if (setting != null && setting.isTextual()) {
                return setting.asText();
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
            case VE_HOST:
                return getOrDefault(project,MMS_URL);
            case VE_PATH:
                return "/alfresco/mmsapp/mms.html#";
            case MMS_URL:
                return getStereotypeUrl(project);
        }

        return "Not Found";
    }

    public static String getMmsUrl(Project project) {
        return getOrDefault(project, MMS_URL);
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
