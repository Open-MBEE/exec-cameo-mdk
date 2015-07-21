package gov.nasa.jpl.mbee.ems.validation.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

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

	public DetailDiff(Element element, JSONObject webData) {
		super("DetailDiff", "Detail Diff", null, null);
		this.modelData = ExportUtility.fillElement(element, null);
		this.webData = webData;
	}
	
	private class JSONTreeNode extends DefaultMutableTreeNode {
		
		private String key;
		private String title;
		private boolean header = false;
		
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
	
	private JTabbedPane buildPane(String name, JSONObject elementData) {
		
		JSONTreeNode node = buildNode(new JSONTreeNode(name), elementData);
		
		JTree tree = new JTree(node);
		tree.setName(name);
		
		JTabbedPane pane = new JTabbedPane();
		pane.add(name, tree);
		return pane;
	}
	
	private void construct() {
		keyMap.clear();
		
		JButton preview = new JButton("Preview");
		JButton submit = new JButton("Submit");
		JButton cancel = new JButton("Cancel");
		
		JPanel buttPanel = new JPanel();
        buttPanel.add(preview);
        buttPanel.add(submit);
        buttPanel.add(cancel);

		// each JTabbedPane holds the JTree with all the json data
		// they are linked in a map
		
		JTabbedPane modelPane = buildPane("MD Model", modelData);
		JTabbedPane webPane = buildPane("MMS Web", webData);
		
		System.out.println(keyMap);

        // splitpane holds both JSON trees represented in JTree form
        JSplitPane split = new JSplitPane();
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        split.setLeftComponent(modelPane);
        split.setRightComponent(webPane);
        
        // JPanel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(split);
        panel.add(buttPanel, BorderLayout.SOUTH);
        
        // show is the JDialog that holds everything (in its ContentPane)
        final JDialog show = new JDialog();
        show.setTitle("Comparison");
        show.setSize(1200, 600);
        show.getContentPane().add(panel);
        show.setVisible(true);
        
        // add listeners
        
        cancel.addActionListener( new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			show.dispose();
        		}
        });
        
	}
	
}
