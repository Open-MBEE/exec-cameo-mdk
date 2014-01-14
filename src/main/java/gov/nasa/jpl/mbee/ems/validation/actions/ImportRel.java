package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.validation.ResultHolder;
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ImportRel extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    
    public ImportRel(Element e) {
        super("ImportRel", "Import rel", null, null);
        this.element = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject result = ResultHolder.lastResults;
        SessionManager.getInstance().createSession("Change Rels");
        Collection<Annotation> toremove = new HashSet<Annotation>();
        try {
            for (Annotation anno: annos) {
                Element e = (Element)anno.getTarget();
                if (!e.isEditable()) {
                    continue;
                }
                String sourceId = (String)((JSONObject)((JSONObject)result.get("elementsKeyed")).get(e.getID())).get("source");
                String targetId = (String)((JSONObject)((JSONObject)result.get("elementsKeyed")).get(e.getID())).get("target");
                Element source = (Element)Application.getInstance().getProject().getElementByID(sourceId);
                Element target = (Element)Application.getInstance().getProject().getElementByID(targetId);
                ModelHelper.setClientElement(e, source);
                ModelHelper.setSupplierElement(e, target);
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
            Application.getInstance().getGUILog().log("[ERROR] " + element.getHumanName() + " is not editable!");
            return;
        }
        SessionManager.getInstance().createSession("Change Rel");
        try {
            JSONObject result = ResultHolder.lastResults;
            String sourceId = (String)((JSONObject)((JSONObject)result.get("elementsKeyed")).get(element.getID())).get("source");
            String targetId = (String)((JSONObject)((JSONObject)result.get("elementsKeyed")).get(element.getID())).get("target");
            Element source = (Element)Application.getInstance().getProject().getElementByID(sourceId);
            Element target = (Element)Application.getInstance().getProject().getElementByID(targetId);
            ModelHelper.setClientElement(element, source);
            ModelHelper.setSupplierElement(element, target);
            SessionManager.getInstance().closeSession();
            //AnnotationManager.getInstance().remove(annotation);
            //AnnotationManager.getInstance().update();
            this.removeViolationAndUpdateWindow();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
    }
}