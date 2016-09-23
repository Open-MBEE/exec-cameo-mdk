package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.ems.ImportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ReferenceException;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

public class ImportAssociation extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Association element;
    private JSONObject spec;
    private JSONObject result;

    public ImportAssociation(Association e, JSONObject spec, JSONObject result) {
        //JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        //
        super("ImportAssociation", "Accept association", null, null);
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
        executeMany(annos, "Change associations");
    }

    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            Element e = (Element) anno.getTarget();
            String name = e.get_representationText();
            if (e instanceof NamedElement) {
                name = ((NamedElement) e).getQualifiedName();
            }
            if (!e.isEditable()) {
                Utils.guilog("[ERROR] " + name + " isn't editable");
                return false;
            }
            JSONObject resultOb = ((Map<String, JSONObject>) result.get("elementsKeyed")).get(e.getID());
            try {
                ImportUtility.setAssociation((Association) e, (JSONObject) resultOb);
//                ImportUtility.setAssociation((Association) e, (JSONObject) resultOb.get("specialization"));
            } catch (ReferenceException ex) {
                Utils.guilog("[ERROR] Failed association import because references not found.");
                return false;
            }
        }
        else {
            try {
                ImportUtility.setAssociation(element, spec);
            } catch (ReferenceException ex) {
                Utils.guilog("[ERROR] Failed association import because references not found.");
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
        execute("Change association");
    }
}
