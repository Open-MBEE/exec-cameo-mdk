package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Operation;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ExportOperation extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Operation element;
    
    public ExportOperation(Operation e) {
        super("ExportOperation", "Commit operation", null, null);
        this.element = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        JSONArray infos = new JSONArray();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            JSONObject elementOb = ExportUtility.fillId(e, null);
            elementOb.put("specialization", ExportUtility.fillOperationSpecialization((Operation)e, null));
            infos.add(elementOb);
        }
        commit(infos, "Operation");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        JSONArray elements = new JSONArray();
        JSONObject elementOb = ExportUtility.fillId(element, null);
        elementOb.put("specialization", ExportUtility.fillOperationSpecialization((Operation)element, null));
        elements.add(elementOb);
        commit(elements, "Operation");
    }
}
