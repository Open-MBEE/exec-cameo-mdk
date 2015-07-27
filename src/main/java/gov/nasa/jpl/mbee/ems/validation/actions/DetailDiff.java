package gov.nasa.jpl.mbee.ems.validation.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DetailDiff extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
	
	private static final long serialVersionUID = 1L;
	private JSONObject modelData;
    private JSONObject webData;
    private Map<String, ArrayList<JSONTreeNode>> keyMap = new HashMap<String, ArrayList<JSONTreeNode>>();
    
    private String modelName = "MD Model";
    private String webName = "MMS Web";

	public DetailDiff(Element element, JSONObject webData) {
		super("DetailDiff", "Detail Diff", null, null);
		this.modelData = ExportUtility.fillElement(element, null);
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
			if (this.hasTitle()) {
				if (keyMap.containsKey(key)) {
					ArrayList<JSONTreeNode> nodes = keyMap.get(key);
					nodes.add(this);
				} else {
					ArrayList<JSONTreeNode> nodes = new ArrayList<JSONTreeNode>();
					nodes.add(this);
					keyMap.put(key, nodes);
				}
			}
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
		// select the top element and diff that one
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		construct();
	}
	
	private JSONTreeNode buildNode(JSONTreeNode current, Object value) {
		// current is the current node
		// it has already been initialized
		if (value instanceof JSONArray) {
			for (Object val: ((JSONArray)value)) {
				JSONTreeNode arrayVal = buildNode(new JSONTreeNode(), val);
				arrayVal.setKey(current.getKey());
				current.add(arrayVal);
			}
		} else if (value instanceof JSONObject) {
			for (Object item: ((JSONObject)value).entrySet()) {
				Map.Entry entry = (Map.Entry) item;
				JSONTreeNode entryVal = buildNode(new JSONTreeNode(entry.getKey().toString()), entry.getValue());
				current.add(entryVal);
			}
		} else {
			if (current.hasTitle()) {
				current.setUserObject(current.getTitle() + " : " + value.toString());
			} else {
				current.setUserObject(value.toString());
			}
		}
		return current;
	}

	
	private JTabbedPane buildPane(String name, final JTree tree, final JTree opposite) {
				
//		tree = new JTree(node);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		
		tree.addTreeSelectionListener( new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				JSONTreeNode currentNode = (JSONTreeNode) tree.getLastSelectedPathComponent();
				String key = currentNode.getKey();
				ArrayList<JSONTreeNode> nodes = keyMap.get(key);
				for (JSONTreeNode node: nodes) {
					if (!node.equals(currentNode)) {
						opposite.setSelectionPath(new TreePath(node.getPath()));
					}
				}
			}
			
		});
		
		JTabbedPane pane = new JTabbedPane();
		pane.add(name, tree);
		return pane;
	}
	
	private void construct() {
		keyMap.clear();
		
//		JButton preview = new JButton("Preview");
//		JButton submit = new JButton("Submit");
//		JButton cancel = new JButton("Cancel");
//		
//		JPanel buttPanel = new JPanel();
//        buttPanel.add(preview);
//        buttPanel.add(submit);
//        buttPanel.add(cancel);

        // these trees have to know about each other
        
		JSONTreeNode modelNode = buildNode(new JSONTreeNode(modelName), modelData);
		JSONTreeNode webNode = buildNode(new JSONTreeNode(webName), webData);

		final JTree modelTree = new JTree(modelNode);
		modelTree.setName(modelName);
		final JTree webTree = new JTree(webNode);
		webTree.setName(webName);
		
		// build the selection ButtonGroup
//		JRadioButton modelToggle = new JRadioButton("Commit Instance", false);
//		JRadioButton webToggle = new JRadioButton("Accept Instance", true);
//		final ButtonGroup selection = new ButtonGroup();
//		selection.add(modelToggle);
//		selection.add(webToggle);
		
		// build each of the panes
		JTabbedPane modelPane = buildPane(modelName, modelTree, webTree);
		JTabbedPane webPane = buildPane(webName, webTree, modelTree);
		
		JPanel modelPanel = new JPanel();
		modelPanel.setLayout(new BoxLayout(modelPanel, BoxLayout.Y_AXIS));
		modelPanel.add(modelPane);
//		modelPanel.add(modelToggle, BorderLayout.SOUTH);
		JPanel webPanel = new JPanel();
		webPanel.setLayout(new BoxLayout(webPanel, BoxLayout.Y_AXIS));
		webPanel.add(webPane);
//		webPanel.add(webToggle, BorderLayout.SOUTH);
		
        // splitpane holds both JSON trees represented in JTree form
        JSplitPane split = new JSplitPane();
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        split.setLeftComponent(modelPanel);
        split.setRightComponent(webPanel);
        
        // JPanel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(split);
//        panel.add(buttPanel, BorderLayout.SOUTH);
        
        // show is the JDialog that holds everything (in its ContentPane)
        final JDialog show = new JDialog();
        show.setTitle("Comparison");
        show.setSize(1200, 600);
        show.getContentPane().add(panel);
        show.setVisible(true);
        
        // add listeners
        
//        cancel.addActionListener( new ActionListener() {
//        		public void actionPerformed(ActionEvent e) {
//        			show.dispose();
//        		}
//        });
//        
//        submit.addActionListener( new ActionListener() {
//        		public void actionPerformed(ActionEvent e) {
//        			System.out.println("submit");
//        		}
//        });
//        
//        preview.addActionListener( new ActionListener() {
//        		public void actionPerformed(ActionEvent e) {
//        			System.out.println(selection.getSelection());
//        			previewChange(selection.getSelection());
//        		}
//        });
        
	}
	
	private void previewChange(ButtonModel model) {
		System.out.println(model);
	}
	
}
