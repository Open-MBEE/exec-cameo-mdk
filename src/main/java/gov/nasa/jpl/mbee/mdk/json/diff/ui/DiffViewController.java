package gov.nasa.jpl.mbee.mdk.json.diff.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class DiffViewController implements Initializable {
    private final JsonNode source, target, patch;

    public DiffViewController(JsonNode source, JsonNode target, JsonNode patch) {
        this.source = source;
        this.target = target;
        this.patch = patch;
    }

    @FXML
    TreeTableView<DiffLineItem> treeTableView;
    @FXML
    TreeTableColumn<DiffLineItem, Object> keyTreeTableColumn;
    @FXML
    TreeTableColumn<DiffLineItem, JsonNode> sourceValueTreeTableColumn, targetValueTreeTableColumn;

    private final List<BiConsumer<TreeTableCell<DiffLineItem, JsonNode>, JsonNode>> valueCellFormatters = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeItem<DiffLineItem> root = buildTree(null, "", source, target, patch, null);
        treeTableView.setRoot(root);

        keyTreeTableColumn.setCellValueFactory(param -> {
            if (param.getValue() == null || param.getValue().getValue() == null) {
                return null;
            }
            return new ReadOnlyObjectWrapper<>(param.getValue().getValue().getKey());
        });
        keyTreeTableColumn.setCellFactory(new Callback<TreeTableColumn<DiffLineItem, Object>, TreeTableCell<DiffLineItem, Object>>() {
            @Override
            public TreeTableCell<DiffLineItem, Object> call(TreeTableColumn<DiffLineItem, Object> param) {
                return new TreeTableCell<DiffLineItem, Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(null);
                        this.setGraphic(null);
                        this.getStyleClass().clear();
                        this.setContextMenu(null);

                        if (item == null || empty) {
                            return;
                        }
                        if (getTreeTableRow() == null || getTreeTableRow().getTreeItem() == null) {
                            return;
                        }
                        DiffLineItem lineItem = getTreeTableRow().getTreeItem().getValue();
                        if (lineItem == null) {
                            return;
                        }
                        if (item instanceof String) {
                            setText("\"" + item + "\"");
                        }
                        else if (item instanceof Integer) {
                            setText("[" + item + "]");
                        }
                        getStyleClass().add("table-cell");
                        if (lineItem.getPatchObjects().stream().anyMatch(patch -> "remove".equals(patch.path("op").asText()) ||
                                "move".equals(patch.path("op").asText()) && lineItem.getPath().equals(patch.path("from").asText()))) {
                            getStyleClass().add("table-cell-remove");
                        }
                        else if (lineItem.getPatchObjects().stream().anyMatch(patch -> "add".equals(patch.path("op").asText()) ||
                                "move".equals(patch.path("op").asText()) && lineItem.getPath().equals(patch.path("path").asText()) ||
                                "copy".equals(patch.path("op").asText()) && lineItem.getPath().equals(patch.path("path").asText()))) {
                            getStyleClass().add("table-cell-add");
                        }

                        MenuItem copyMenuItem = new MenuItem("Copy");
                        copyMenuItem.setOnAction(event -> {
                            ClipboardContent content = new ClipboardContent();
                            content.putString(item.toString());
                            Clipboard.getSystemClipboard().setContent(content);
                        });
                        ContextMenu menu = new ContextMenu(copyMenuItem);
                        this.setContextMenu(menu);
                    }
                };
            }
        });

        sourceValueTreeTableColumn.setCellValueFactory(param -> {
            if (param.getValue() == null || param.getValue().getValue() == null) {
                return null;
            }
            return new ReadOnlyObjectWrapper<>(param.getValue().getValue().getSourceValue());
        });
        sourceValueTreeTableColumn.setCellFactory(new Callback<TreeTableColumn<DiffLineItem, JsonNode>, TreeTableCell<DiffLineItem, JsonNode>>() {
            @Override
            public TreeTableCell<DiffLineItem, JsonNode> call(TreeTableColumn<DiffLineItem, JsonNode> param) {
                return new TreeTableCell<DiffLineItem, JsonNode>() {
                    @Override
                    protected void updateItem(JsonNode item, boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(null);
                        this.setGraphic(null);
                        this.getStyleClass().clear();
                        this.setContextMenu(null);

                        if (item == null || empty) {
                            return;
                        }
                        if (getTreeTableRow() == null || getTreeTableRow().getTreeItem() == null) {
                            return;
                        }
                        DiffLineItem lineItem = getTreeTableRow().getTreeItem().getValue();
                        if (lineItem == null) {
                            return;
                        }
                        getStyleClass().add("table-cell");
                        if (item.isContainerNode()) {
                            setText(StringUtils.capitalize(item.getNodeType().name().toLowerCase()) + "[" + item.size() + "]");
                            getStyleClass().add("table-cell-faint");
                        }
                        else if (item.isValueNode()) {
                            String text = item.asText();
                            setText(item.isTextual() ? "\"" + text + "\"" : text);
                            if (lineItem.getPatchObjects().stream().anyMatch(patch -> "remove".equals(patch.path("op").asText()) ||
                                    "move".equals(patch.path("op").asText()) && lineItem.getPath().equals(patch.path("from").asText()) ||
                                    "replace".equals(patch.path("op").asText()))) {
                                getStyleClass().add("table-cell-remove");
                            }

                            MenuItem copyMenuItem = new MenuItem("Copy");
                            copyMenuItem.setOnAction(event -> {
                                ClipboardContent content = new ClipboardContent();
                                content.putString(item.asText());
                                Clipboard.getSystemClipboard().setContent(content);
                            });
                            ContextMenu menu = new ContextMenu(copyMenuItem);
                            this.setContextMenu(menu);
                        }

                        getValueCellFormatters().forEach(consumer -> consumer.accept(this, item));
                    }
                };
            }
        });

        targetValueTreeTableColumn.setCellValueFactory(param -> {
            if (param.getValue() == null || param.getValue().getValue() == null) {
                return null;
            }
            return new ReadOnlyObjectWrapper<>(param.getValue().getValue().getTargetValue());
        });
        targetValueTreeTableColumn.setCellFactory(new Callback<TreeTableColumn<DiffLineItem, JsonNode>, TreeTableCell<DiffLineItem, JsonNode>>() {
            @Override
            public TreeTableCell<DiffLineItem, JsonNode> call(TreeTableColumn<DiffLineItem, JsonNode> param) {
                return new TreeTableCell<DiffLineItem, JsonNode>() {
                    @Override
                    protected void updateItem(JsonNode item, boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(null);
                        this.setGraphic(null);
                        this.getStyleClass().clear();
                        this.setContextMenu(null);

                        if (item == null || empty) {
                            return;
                        }
                        if (getTreeTableRow() == null || getTreeTableRow().getTreeItem() == null) {
                            return;
                        }
                        DiffLineItem lineItem = getTreeTableRow().getTreeItem().getValue();
                        if (lineItem == null) {
                            return;
                        }
                        getStyleClass().add("table-cell");
                        if (item.isContainerNode()) {
                            setText(StringUtils.capitalize(item.getNodeType().name().toLowerCase()) + "[" + item.size() + "]");
                            getStyleClass().add("table-cell-faint");
                        }
                        else if (item.isValueNode()) {
                            String text = item.asText();
                            setText(item.isTextual() ? "\"" + text + "\"" : text);
                            if (lineItem.getPatchObjects().stream().anyMatch(patch -> "add".equals(patch.path("op").asText()) ||
                                    "move".equals(patch.path("op").asText()) && lineItem.getPath().equals(patch.path("path").asText()) ||
                                    "copy".equals(patch.path("op").asText()) && lineItem.getPath().equals(patch.path("path").asText()) ||
                                    "replace".equals(patch.path("op").asText()))) {
                                getStyleClass().add("table-cell-add");
                            }

                            MenuItem copyMenuItem = new MenuItem("Copy");
                            copyMenuItem.setOnAction(event -> {
                                ClipboardContent content = new ClipboardContent();
                                content.putString(item.asText());
                                Clipboard.getSystemClipboard().setContent(content);
                            });
                            ContextMenu menu = new ContextMenu(copyMenuItem);
                            this.setContextMenu(menu);
                        }

                        getValueCellFormatters().forEach(consumer -> consumer.accept(this, item));
                    }
                };
            }
        });
    }

    private static TreeItem<DiffLineItem> buildTree(Object key, String path, JsonNode source, JsonNode target, JsonNode patch, TreeItem<DiffLineItem> tree) {
        if (path == null) {
            path = "";
        }
        if (key != null) {
            String pathSuffix;
            if (key instanceof String) {
                pathSuffix = (String) key;
            }
            else if (key instanceof Integer) {
                pathSuffix = Integer.toString((Integer) key);
            }
            else {
                pathSuffix = key.toString();
            }
            path += "/" + pathSuffix.replace("~", "~0").replace("/", "~1");
        }

        DiffLineItem lineItem = new DiffLineItem();
        lineItem.setKey(key);
        lineItem.setPath(path);
        lineItem.setSourceValue(source);
        lineItem.setTargetValue(target);

        String newPath = path;
        Set<JsonNode> patchObjects = StreamSupport.stream(patch.spliterator(), true).filter(node -> {
            if (!node.isObject()) {
                return false;
            }
            if (newPath.equals(node.path("path").asText())) {
                return true;
            }
            if (newPath.equals(node.path("from").asText())) {
                return true;
            }
            if ("add".equals(node.path("op").asText())) {
                String[] pathSegments = newPath.split("/");
                for (int i = pathSegments.length - 1; i > 1; i--) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 1; j < i; j++) {
                        sb.append("/").append(pathSegments[j]);
                    }
                    if (sb.toString().equals(node.path("path").asText())) {
                        return true;
                    }
                }
            }
            return false;
        }).collect(Collectors.toSet());
        lineItem.setPatchObjects(patchObjects);
        TreeItem<DiffLineItem> treeItem = new TreeItem<>(lineItem);
        treeItem.setExpanded(true);
        if (tree != null) {
            tree.getChildren().add(treeItem);
        }
        Set<Object> keySet = new LinkedHashSet<>();
        Iterator<String> iterator = source.fieldNames();
        while (iterator.hasNext()) {
            keySet.add(iterator.next());
        }
        iterator = target.fieldNames();
        while (iterator.hasNext()) {
            keySet.add(iterator.next());
        }
        if (source.isArray()) {
            IntStream.range(0, source.size()).forEach(keySet::add);
        }
        if (target.isArray()) {
            IntStream.range(0, target.size()).forEach(keySet::add);
        }
        for (Object newKey : keySet) {
            JsonNode newSourceValue, newTargetValue;
            if (newKey instanceof String) {
                newSourceValue = source.path((String) newKey);
                newTargetValue = target.path((String) newKey);
            }
            else if (newKey instanceof Integer) {
                newSourceValue = source.path((Integer) newKey);
                newTargetValue = target.path((Integer) newKey);
            }
            else {
                newSourceValue = MissingNode.getInstance();
                newTargetValue = MissingNode.getInstance();
            }
            buildTree(newKey, path, newSourceValue, newTargetValue, patch, treeItem);
        }
        return treeItem;
    }

    public TreeTableView<DiffLineItem> getTreeTableView() {
        return treeTableView;
    }

    public TreeTableColumn<DiffLineItem, Object> getKeyTreeTableColumn() {
        return keyTreeTableColumn;
    }

    public TreeTableColumn<DiffLineItem, JsonNode> getSourceValueTreeTableColumn() {
        return sourceValueTreeTableColumn;
    }

    public TreeTableColumn<DiffLineItem, JsonNode> getTargetValueTreeTableColumn() {
        return targetValueTreeTableColumn;
    }

    public List<BiConsumer<TreeTableCell<DiffLineItem, JsonNode>, JsonNode>> getValueCellFormatters() {
        return valueCellFormatters;
    }

    public static class DiffLineItem {
        private Object key;
        private String path;
        private JsonNode sourceValue, targetValue;
        private Set<JsonNode> patchObjects;

        public Object getKey() {
            return key;
        }

        public void setKey(Object key) {
            this.key = key;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public JsonNode getSourceValue() {
            return sourceValue;
        }

        public void setSourceValue(JsonNode sourceValue) {
            this.sourceValue = sourceValue;
        }

        public JsonNode getTargetValue() {
            return targetValue;
        }

        public void setTargetValue(JsonNode targetValue) {
            this.targetValue = targetValue;
        }

        public Set<JsonNode> getPatchObjects() {
            return patchObjects;
        }

        public void setPatchObjects(Set<JsonNode> patchObjects) {
            this.patchObjects = patchObjects;
        }
    }
}
