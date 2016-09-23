package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;

import java.awt.event.ActionEvent;
import java.util.Collection;

public class ExportOwnedAttribute extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;

    public ExportOwnedAttribute(Element e) {
        super("ExportOwnedAttribute", "Commit Owned Attributes", null, null);
        this.element = e;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        JSONArray elements = new JSONArray();
        for (Annotation anno : annos) {
            Element e = (Element) anno.getTarget();
            elements.add(ExportUtility.fillOwnedAttribute(e, null));
        }
        commit(elements, "Owned Attribute");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        JSONArray elements = new JSONArray();
        elements.add(ExportUtility.fillOwnedAttribute(element, null));
        commit(elements, "Owned Attribute");
    }

}
