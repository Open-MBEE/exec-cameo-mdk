package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.validation.ResultHolder;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ImportComment extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Comment element;
    private String doc;
    
    public ImportComment(Comment e, String doc) {
        super("ImportComment", "Import comment", null, null);
        this.element = e;
        this.doc = doc;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject result = ResultHolder.lastResults;
        SessionManager.getInstance().createSession("Change Comments");
        Collection<Annotation> toremove = new HashSet<Annotation>();
        try {
            for (Annotation anno: annos) {
                Element e = (Element)anno.getTarget();
                if (!e.isEditable()) {
                    continue;
                }
                String resultDoc = (String)((JSONObject)((JSONObject)result.get("elementsKeyed")).get(e.getID())).get("body");
                if (resultDoc == null)
                    continue;
                ((Comment)e).setBody(Utils.addHtmlWrapper(resultDoc));
                //AnnotationManager.getInstance().remove(anno);
                toremove.add(anno);
            }
            SessionManager.getInstance().closeSession();
            //AnnotationManager.getInstance().update();
            this.removeViolationsAndUpdateWindow(toremove);
            
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!element.isEditable()) {
            Application.getInstance().getGUILog().log("[ERROR] Element is not editable!");
            return;
        }
        SessionManager.getInstance().createSession("Change comment");
        try {
            element.setBody(Utils.addHtmlWrapper(doc));
            SessionManager.getInstance().closeSession();
            //AnnotationManager.getInstance().remove(annotation);
            //AnnotationManager.getInstance().update();
            this.removeViolationAndUpdateWindow();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
    }

}
