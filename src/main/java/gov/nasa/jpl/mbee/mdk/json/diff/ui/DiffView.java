package gov.nasa.jpl.mbee.mdk.json.diff.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DiffView extends VBox {
    private final DiffViewController controller;

    public DiffView(JsonNode source, JsonNode target, JsonNode patch) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DiffView.fxml"));
        loader.setController(controller = new DiffViewController(source, target, patch));
        loader.setRoot(this);
        loader.load();
        this.getStylesheets().add(getClass().getResource("DiffView.css").toExternalForm());
    }

    public DiffViewController getController() {
        return controller;
    }
}
