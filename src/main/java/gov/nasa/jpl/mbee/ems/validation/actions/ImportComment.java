package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.validation.ResultHolder;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONArray;
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
    private JSONObject result;
    
    public ImportComment(Comment e, String doc, JSONObject result) {
        super("ImportComment", "Import comment", null, null);
        this.element = e;
        this.doc = doc;
        this.result = result;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
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
                ((Comment)e).getAnnotatedElement().clear();
                JSONArray annotatedElements = (JSONArray)((JSONObject)((JSONObject)result.get("elementsKeyed")).get(e.getID())).get("annotatedElements");
                if (annotatedElements != null) {
                    for (String eid: (List<String>)annotatedElements) {
                        Element aelement = (Element)Application.getInstance().getProject().getElementByID(eid);
                        if (aelement != null)
                            ((Comment)e).getAnnotatedElement().add(aelement);
                    }
                }
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
            element.getAnnotatedElement().clear();
            JSONArray annotatedElements = (JSONArray)((JSONObject)((JSONObject)result.get("elementsKeyed")).get(element.getID())).get("annotatedElements");
            if (annotatedElements != null) {
                for (String eid: (List<String>)annotatedElements) {
                    Element aelement = (Element)Application.getInstance().getProject().getElementByID(eid);
                    if (aelement != null)
                        element.getAnnotatedElement().add(aelement);
                }
            }
            SessionManager.getInstance().closeSession();
            //AnnotationManager.getInstance().remove(annotation);
            //AnnotationManager.getInstance().update();
            this.removeViolationAndUpdateWindow();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
    }

}
