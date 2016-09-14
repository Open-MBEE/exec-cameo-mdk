package gov.nasa.jpl.mbee.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.ems.ImportException;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

public class ImportViewConstraint extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private NamedElement element;
    private JSONObject spec;
    private JSONObject result;

    public ImportViewConstraint(NamedElement e, JSONObject spec, JSONObject result) {
        //JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        //
        super("ImportViewConstraint", "Accept View Constraint", null, null);
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
        executeMany(annos, "Change view constraints");
    }

    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            NamedElement e = (NamedElement) anno.getTarget();
            Constraint c = Utils.getViewConstraint(e);
            if ((c != null && !c.isEditable()) || !e.isEditable()) {
                Utils.guilog("[ERROR] View " + e.getQualifiedName() + " or its constraint isn't editable");
                return false;
            }
            JSONObject resultOb = ((Map<String, JSONObject>) result.get("elementsKeyed")).get(e.getID());
            try {
                ImportUtility.setViewConstraint(e, (JSONObject) resultOb.get("specialization"));
            } catch (ImportException ex) {
                Utils.guilog("[ERROR] " + ex.getMessage());
                return false;
            }
        }
        else {
            try {
                ImportUtility.setViewConstraint(element, spec);
            } catch (ImportException ex) {
                Utils.guilog("[ERROR] " + ex.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Constraint c = Utils.getViewConstraint(element);
        if ((c != null && !c.isEditable()) || !element.isEditable()) {
            Utils.guilog("[ERROR] View or view constraint is not editable!");
            return;
        }
        execute("Change view constraint");
    }
}
