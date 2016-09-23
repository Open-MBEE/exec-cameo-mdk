package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.ImportUtility;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

public class ImportConstraint extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Constraint element;
    private JSONObject spec;
    private JSONObject result;

    public ImportConstraint(Constraint e, JSONObject spec, JSONObject result) {
        //JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        //
        super("ImportConstraint", "Accept constraint", null, null);
        this.element = e;
        this.spec = spec;
        this.result = result;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        executeMany(annos, "Change Constraint");
    }

    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            Element e = (Element) anno.getTarget();
            if (!e.isEditable()) {
                Utils.guilog("[ERROR] " + e.get_representationText() + " isn't editable");
                return false;
            }
            JSONObject resultOb = ((Map<String, JSONObject>) result.get("elementsKeyed")).get(e.getID());
            try {
                ImportUtility.setConstraintSpecification((Constraint) e, (JSONObject) resultOb);
//                ImportUtility.setConstraintSpecification((Constraint) e, (JSONObject) resultOb.get("specialization"));
            } catch (ImportException ex) {
                Utils.guilog("[ERROR] " + ex.getMessage());
                return false;
            }
        }
        else {
            try {
                ImportUtility.setConstraintSpecification(element, spec);
            } catch (ImportException ex) {
                Utils.guilog("[ERROR] " + ex.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!element.isEditable()) {
            Utils.guilog("[ERROR] Element is not editable!");
            return;
        }
        execute("Change constraint");
    }
}
