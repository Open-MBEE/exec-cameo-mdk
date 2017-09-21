package gov.nasa.jpl.mbee.mdk.json.diff.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.dialogs.specifications.SpecificationDialogManager;
import com.nomagic.magicdraw.uml.ElementIcon;
import com.nomagic.ui.ResizableIcon;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextFlow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class MDKDiffView extends DiffView {
    private static final Pattern ID_PATTERN = Pattern.compile("[\\w/]+Id(s)?$");

    public MDKDiffView(JsonNode source, JsonNode target, JsonNode patch, Project project) throws IOException {
        super(source, target, patch);
        this.getController().getValueCellFormatters().add((cell, item) -> {
            if (item == null || item.isNull()) {
                return;
            }
            if (!item.isValueNode()) {
                return;
            }
            if (cell.getTreeTableRow() == null || cell.getTreeTableRow().getTreeItem() == null) {
                return;
            }
            if (cell.getTreeTableRow() == null || cell.getTreeTableRow().getTreeItem() == null) {
                return;
            }
            DiffViewController.DiffLineItem lineItem = cell.getTreeTableRow().getTreeItem().getValue();
            if (lineItem == null) {
                return;
            }
            boolean isId = false;
            if (lineItem.getKey() instanceof String && ID_PATTERN.matcher((String) lineItem.getKey()).find()) {
                isId = true;
            }
            else {
                int i = lineItem.getPath().lastIndexOf('/');
                if (i < 0) {
                    return;
                }
                if (ID_PATTERN.matcher(lineItem.getPath().substring(0, i)).find()) {
                    isId = true;
                }
            }
            if (isId) {
                String id = item.asText();
                Element element = project != null ? Converters.getIdToElementConverter().apply(id, project) : null;
                ResizableIcon swingIcon = element != null ? ElementIcon.getIcon(element) : null;
                ImageView iconImageView = null;
                if (swingIcon instanceof ImageIcon) {
                    ImageIcon imageIcon = ((ImageIcon) swingIcon);
                    BufferedImage bufferedImage;
                    if (imageIcon.getImage() instanceof BufferedImage) {
                        bufferedImage = (BufferedImage) imageIcon.getImage();
                    }
                    else {
                        bufferedImage = new BufferedImage(imageIcon.getImage().getWidth(null), imageIcon.getImage().getHeight(null), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D graphics = bufferedImage.createGraphics();
                        graphics.drawImage(imageIcon.getImage(), 0, 0, null);
                        graphics.dispose();
                    }
                    iconImageView = new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
                }
                String name;
                if (element == null) {
                    name = "Not Found";
                    InputStream inputStream = getClass().getResourceAsStream("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png");
                    if (inputStream != null) {
                        iconImageView = new ImageView(new Image(inputStream));
                    }
                }
                else if (element instanceof NamedElement) {
                    name = ((NamedElement) element).getName();
                    if (name == null || name.isEmpty()) {
                        name = "<>";
                    }
                }
                else {
                    name = element.getHumanName();
                }
                Hyperlink hyperlink = new Hyperlink(name);
                if (iconImageView != null) {
                    iconImageView.setFitWidth(16);
                    iconImageView.setFitHeight(16);
                    hyperlink.setGraphic(iconImageView);
                }
                hyperlink.setDisable(element == null);
                if (element != null) {
                    hyperlink.setOnAction(event -> new Thread(() -> SpecificationDialogManager.getManager().editSpecification(element)).start());
                }
                TextFlow textFlow = new TextFlow(hyperlink);
                cell.setGraphic(textFlow);
            }
        });

        Platform.runLater(() -> {
            this.getController().getKeyTreeTableColumn().setText("Attribute");
            this.getController().getSourceValueTreeTableColumn().setText("Local Value");
            this.getController().getTargetValueTreeTableColumn().setText("MMS Value");
        });
    }
}
