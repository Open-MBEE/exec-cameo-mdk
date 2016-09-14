package gov.nasa.jpl.mbee.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

public class CompareHierarchy extends RuleViolationAction implements
        AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    private JSONObject web;
    private JSONObject model;

    public CompareHierarchy(Element e, JSONObject web, JSONObject model) {
        super("Compare Hierarchy", "Compare Hierarchy", null, null);
        this.element = e;
        this.web = web;
        this.model = model;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {

    }

    protected class ViewNode extends DefaultMutableTreeNode {
        ViewNode(Object e) {
            super();
            this.setUserObject(e);
        }

        @Override
        public String toString() {
            Object e = this.getUserObject();
            if (e instanceof String) {
                return (String) e;
            }
            if (!(e instanceof NamedElement)) {
                return "";
            }
            return ((NamedElement) e).getName();
        }
    }

    private ViewNode makeNode(String viewid, JSONObject o) {
        if (o == null || o.isEmpty()) {
            return new ViewNode("Doesn't Exist");
        }
        if (o.containsKey(viewid)) {
            Element e = ExportUtility.getElementFromID(viewid);
            ViewNode vn = null;
            if (e == null) {
                vn = new ViewNode(viewid);
            }
            else {
                vn = new ViewNode(e);
            }
            JSONArray children = (JSONArray) o.get(viewid);
            for (Object child : children) {
                ViewNode childNode = makeNode((String) child, o);
                if (childNode != null) {
                    vn.add(childNode);
                }
            }
            return vn;
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ViewNode modelRoot = makeNode(element.getID(), model);
        ViewNode webRoot = makeNode(element.getID(), web);

        JTree web = new JTree(webRoot);
        for (int i = 0; i < web.getRowCount(); i++) {
            web.expandRow(i);
        }
        web.setEditable(false);
        JTree model = new JTree(modelRoot);
        for (int i = 0; i < model.getRowCount(); i++) {
            model.expandRow(i);
        }

        JTabbedPane webPane = new JTabbedPane();
        JScrollPane swebPane = new JScrollPane(web);
        webPane.add("Web", swebPane);
        JTabbedPane modelPane = new JTabbedPane();
        JScrollPane smodelPane = new JScrollPane(model);
        modelPane.add("Model", smodelPane);

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
