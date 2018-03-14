package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.json.diff.ui.DiffView;
import gov.nasa.jpl.mbee.mdk.json.diff.ui.MDKDiffView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;

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
        try {
            Class.forName("javafx.application.Platform");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(Application.getInstance().getMainFrame(), "The " + NAME + " feature requires JavaFX. Please add \"-Dorg.osgi.framework.bundle.parent=ext\" to the\n\"JAVA_ARGS\" line in your properties file(s) in your MagicDraw bin directory and restart.");
            return;
        }
        Platform.runLater(() -> {
            try {
                DiffView diffView = new MDKDiffView(clientElement, serverElement, patch, project);
                Scene scene = new Scene(diffView);
                Stage stage = new Stage();
                stage.setTitle("Element Differences");
                stage.setScene(scene);
                WindowAdapter windowAdapter = new WindowAdapter() {
                    @Override
                    public void windowDeactivated(java.awt.event.WindowEvent e) {
                        Platform.runLater(() -> stage.setAlwaysOnTop(false));
                    }

                    @Override
                    public void windowActivated(java.awt.event.WindowEvent e) {
                        Platform.runLater(() -> stage.setAlwaysOnTop(true));
                    }
                };
                Application.getInstance().getMainFrame().addWindowListener(windowAdapter);
                stage.setOnCloseRequest(event -> Application.getInstance().getMainFrame().removeWindowListener(windowAdapter));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
