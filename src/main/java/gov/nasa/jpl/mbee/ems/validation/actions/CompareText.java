package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;


import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CompareText extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    private String webDoc;
    private String modelDoc;
    private JSONObject result;
    
    public CompareText(Element e, String web, String model, JSONObject result) {
        super("Compare Text", "Compare Text", null, null);
        this.element = e;
        this.webDoc = web;
        this.modelDoc = model;
        this.result = result;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JTabbedPane webpane = new JTabbedPane();
        JTabbedPane modelpane = new JTabbedPane();
        JTextArea web = new JTextArea(webDoc);
        web.setEditable(false);
        web.setLineWrap(true);
        JTextArea model = new JTextArea(modelDoc);
        model.setEditable(false);
        model.setLineWrap(true);
        JScrollPane webp = new JScrollPane(web);
        webp.setName("Web");
        webpane.addTab("Web", webp);
        JScrollPane modelp = new JScrollPane(model);
        modelp.setName("Model");
        modelpane.addTab("Model", modelp);
        JSplitPane top = new JSplitPane();
        
        JDialog show = new JDialog(MDDialogParentProvider.getProvider().getDialogParent());
        show.setTitle("Comparison");
        show.setSize(600, 600);
        show.getContentPane().add(top);
        top.setDividerLocation(.5);
        top.setLeftComponent(modelpane);
        top.setRightComponent(webpane);
        show.setVisible(true);
    }

}
