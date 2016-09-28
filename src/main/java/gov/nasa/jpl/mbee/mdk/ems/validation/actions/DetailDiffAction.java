package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DetailDiffAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private JSONObject modelData = new JSONObject();
    private JSONObject webData = new JSONObject();
    private Map<String, ArrayList<JSONTreeNode>> keyMap = new HashMap<String, ArrayList<JSONTreeNode>>();

    private String modelName = "MD Model";
    private String webName = "MMS Web";

    public DetailDiffAction(Element element, JSONObject webData) {
        this(ExportUtility.fillElement(element, null), webData);
    }

    public DetailDiffAction(JSONObject modelData, JSONObject webData) {
        super("DetailDiffAction", "Detail Diff", null, null);
        this.modelData = modelData;
        this.webData = webData;
    }

    private class JSONTreeNode extends DefaultMutableTreeNode {

        private String key;
        private String title;

        public JSONTreeNode() {
            super();
        }

        public JSONTreeNode(String key) {
            super(key);
            setTitle(key);
            setKey(key);
        }

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        private void setKey(String key) {
            this.key = key;
            // if this thing doesn't have a title, that means we can't
            // be sure that there is an equivalent on the opposite tree
            // this needs to be about 1000x better
            if (this.hasTitle()) {
                if (keyMap.containsKey(key)) {
                    ArrayList<JSONTreeNode> nodes = keyMap.get(key);
                    nodes.add(this);
                }
                else {
                    ArrayList<JSONTreeNode> nodes = new ArrayList<JSONTreeNode>();
                    nodes.add(this);
                    keyMap.put(key, nodes);
                }
            }
        }

        public boolean hasKey() {
            return (this.getKey() != null);
        }

        public boolean hasTitle() {
            return (this.getTitle() != null);
        }

        public void setTitle(String title) {
            this.title = title;
        }

    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> arg0) {
        construct();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        construct();
    }

    private JSONTreeNode buildNode(JSONTreeNode current, Object value) {
        // current is the current node
        // it has already been initialized
        if (value instanceof JSONArray) {
            for (Object val : ((JSONArray) value)) {
                JSONTreeNode arrayVal = buildNode(new JSONTreeNode(), val);
                arrayVal.setKey(switchID(current.getKey()));
                current.add(arrayVal);
            }
        }
        else if (value instanceof JSONObject) {
            for (Object item : ((JSONObject) value).entrySet()) {
                Map.Entry entry = (Map.Entry) item;
                JSONTreeNode entryVal = buildNode(new JSONTreeNode(switchID(entry.getKey())), entry.getValue());
                current.add(entryVal);
            }
        }
        else {
            current.setUserObject(process(current, value));
        }
        return current;
    }

    private String switchID(Object value) {
        if (value == null) {
            return "null";
        }
        String ret = value.toString();
        Element target = ExportUtility.getElementFromID(value.toString());
        if (target instanceof NamedElement) {
            ret = ((NamedElement) target).getQualifiedName();
        }
        if (ret == null || ret.isEmpty()) {
            ret = value.toString();
        }
        return ret;
    }

    private String process(JSONTreeNode current, Object value) {
        // reassign value if value is null
        if (value == null) {
            value = "null";
        }

        // reassign value if it is an id (but not value for sysmlid)
        if (!current.hasKey() || (current.hasKey() && !current.getKey().equals("sysmlId"))) {
            Element target = ExportUtility.getElementFromID(value.toString());
            if (target instanceof NamedElement) {
                value = ((NamedElement) target).getQualifiedName();
            }
        }

        // different userobject if the current node has a title or not
        if (current.hasTitle()) {
            return current.getTitle() + " : " + value.toString();
        }
        else {
            return value.toString();
        }
    }


    private JTabbedPane buildPane(String name, final JTree tree, final JTree opposite) {

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

//		tree.addTreeSelectionListener( new TreeSelectionListener() {
//
//			@Override
//			public void valueChanged(TreeSelectionEvent e) {
//				JSONTreeNode currentNode = (JSONTreeNode) tree.getLastSelectedPathComponent();
//				String key = currentNode.getKey();
//				ArrayList<JSONTreeNode> nodes = keyMap.get(key);
//				for (JSONTreeNode node: nodes) {
//					if (!node.equals(currentNode)) {
//						opposite.setSelectionPath(new TreePath(node.getPath()));
//						break;
//					}
//				}
//			}
//		});

        JTabbedPane pane = new JTabbedPane();
        JScrollPane spane = new JScrollPane(tree);
        pane.add(name, spane);
        return pane;
    }

    private void construct() {
        keyMap.clear();

        // these trees have to know about each other

        JSONTreeNode modelNode = buildNode(new JSONTreeNode(modelName), modelData);
        JSONTreeNode webNode = buildNode(new JSONTreeNode(webName), webData);

        final JTree modelTree = new JTree(modelNode);
        modelTree.setName(modelName);
        final JTree webTree = new JTree(webNode);
        webTree.setName(webName);

        // build each of the panes
        JTabbedPane modelPane = buildPane(modelName, modelTree, webTree);
        JTabbedPane webPane = buildPane(webName, webTree, modelTree);

        JPanel modelPanel = new JPanel();
        modelPanel.setLayout(new BoxLayout(modelPanel, BoxLayout.Y_AXIS));
        modelPanel.add(modelPane);
        JPanel webPanel = new JPanel();
        webPanel.setLayout(new BoxLayout(webPanel, BoxLayout.Y_AXIS));
        webPanel.add(webPane);

        // splitpane holds both JSON trees represented in JTree form
        JSplitPane split = new JSplitPane();
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        split.setLeftComponent(modelPanel);
        split.setRightComponent(webPanel);

        // JPanel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(split);

        // show is the JDialog that holds everything (in its ContentPane)
        final JDialog show = new JDialog();
        show.setTitle("Comparison");
        show.setSize(1200, 600);
        show.getContentPane().add(panel);
        show.setVisible(true);
    }

}
