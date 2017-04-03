package gov.nasa.jpl.mbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * validates docgen 3 doc - checks for loops, duplicate dependencies, etc
 *
 * @author dlam
 */
public class ValidateOldDocgen extends MDAction {

    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "ValidateOldDocuments";

    public ValidateOldDocgen() {
        super(DEFAULT_ID, "Find Old DocGen Documents", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = Application.getInstance().getProject();
        ValidationSuite vs = new ValidationSuite("Old DocGen Documents");
        ValidationRule vr = new ValidationRule("Old DocGen Document", "Old DocGen Document", ViolationSeverity.ERROR);
        vs.addValidationRule(vr);
        Stereotype ps = Utils.getProductStereotype(project);
        if (ps == null) {
            return;
        }
        List<Element> elements = Utils.collectOwnedElements(project.getPrimaryModel(), 0);
        List<Element> docs = Utils.filterElementsByStereotype(elements, ps, true, true);
        List<Element> projDocs = new ArrayList<>();
        for (Element doc : docs) {
            if (!ProjectUtilities.isElementInAttachedProject(doc) && doc instanceof Package) {
                projDocs.add(doc);
            }
        }
        if (projDocs.isEmpty()) {
            Application.getInstance().getGUILog().log("No Old Documents Found");
            return;
        }
        for (Element doc : projDocs) {
            ValidationRuleViolation v = new ValidationRuleViolation(doc, "[OLD] Document is old format");
            v.addAction(new MigrateOldDocgen(doc));
            vr.addViolation(v);
        }
        List<ValidationSuite> vss = new ArrayList<>();
        vss.add(vs);
        Utils.displayValidationWindow(project, vss, "Old DocGen Documents");
    }
}
