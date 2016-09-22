package gov.nasa.jpl.mbee.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Parameter;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;

public class ExportParameter extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Parameter element;

    public ExportParameter(Parameter e) {
        super("ExportParameter", "Commit parameter", null, null);
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
        for (Annotation anno : annos) {
            Element e = (Element) anno.getTarget();
            JSONObject elementOb = ExportUtility.fillId(e, null);
            ExportUtility.fillParameterSpecialization((Parameter) e, elementOb);
            infos.add(elementOb);
        }
        commit(infos, "Parameter");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        JSONArray elements = new JSONArray();
        JSONObject elementOb = ExportUtility.fillId(element, null);
        ExportUtility.fillParameterSpecialization(element, elementOb);
        elements.add(elementOb);
        commit(elements, "Parameter");
    }
}
