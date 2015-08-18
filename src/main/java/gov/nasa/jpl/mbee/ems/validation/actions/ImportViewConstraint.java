package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

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
            Element e = (Element)anno.getTarget();
            Constraint c = Utils.getViewConstraint(e);
            if (c != null && !c.isEditable()) {
                Utils.guilog("[ERROR] " + c.get_representationText() + " isn't editable");
                return false;
            }
            JSONObject resultOb = (JSONObject)((Map<String, JSONObject>)result.get("elementsKeyed")).get(e.getID());
            ImportUtility.setViewConstraint(e, (JSONObject)resultOb.get("specialization"));
        } else {
            ImportUtility.setViewConstraint(element, spec);
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Constraint c = Utils.getViewConstraint(element);
        if (c != null && !c.isEditable()) {
            Utils.guilog("[ERROR] " + c.getQualifiedName() + " is not editable!");
            return;
        }
        execute("Change view constraint");
    }
}
