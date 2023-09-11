package org.openmbee.mdk.mms.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import org.openmbee.mdk.MDKPlugin;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;

public class ElementDiffAction extends MDAction {
    public static final String NAME = "Display Differences";

    private final JsonNode clientElement, serverElement, patch;
    private final Project project;

    public ElementDiffAction(JsonNode clientElement, JsonNode serverElement, JsonNode patch, Project project) {
        super(ElementDiffAction.class.getSimpleName(), NAME, null, null);
        this.clientElement = clientElement;
        this.serverElement = serverElement;
        this.patch = patch;
        this.project = project;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        if (!MDKPlugin.isJavaFXSupported()) {
            JOptionPane.showMessageDialog(Application.getInstance().getMainFrame(), "The " + NAME + " feature requires JavaFX. JavaFX Library not found.");
            return;
        }
        try {
            Class<?> clazz = Class.forName("org.openmbee.mdk.json.diff.ui.MDKDiffView");
            Constructor<?> constructor = clazz.getConstructor(JsonNode.class, JsonNode.class, JsonNode.class, Project.class);
            Runnable runnable = (Runnable) constructor.newInstance(clientElement, serverElement, patch, project);
            runnable.run();
        } catch (Exception | Error e) {
            System.err.println("[WARNING] Failed to initialize JavaFX application. JavaFX functionality is disabled.");
            e.printStackTrace();
        }
    }
}
