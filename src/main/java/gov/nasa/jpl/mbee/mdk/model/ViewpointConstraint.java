package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils2;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;

import java.util.ArrayList;
import java.util.List;

public class ViewpointConstraint extends Query {

    @SuppressWarnings("unused")
    private Boolean iterate;
    @SuppressWarnings("unused")
    private String expression;
    private Boolean report;
    private DocumentValidator dv;

    public ViewpointConstraint(DocumentValidator dv) {
        super();
        this.dv = dv;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        // construct a temporary validation suite from the global one to
        // generate docbook output for one constraint.
        ValidationSuite vs = new ValidationSuite(((NamedElement) dgElement).getName());
        ValidationRule rule = new ValidationRule(((NamedElement) dgElement).getName(), "Viewpoint Constraint",
                ViolationSeverity.WARNING);
        vs.addValidationRule(rule);
        if (dv != null) {
            ValidationRule r = dv.getViewpointConstraintRule();
            if (r instanceof ConstraintValidationRule) {
                rule.addViolations(((ConstraintValidationRule) r).getViolations(dgElement));
            }
            // if (expression != null) {
            // if (iterate) {
            // for (Element e: targets) {
            //
            // }
            // } else {
            //
            // }

            if (report && !Utils2.isNullOrEmpty(rule.getViolations())) {
                return vs.getDocBook();
            }
        }
        // }

        return new ArrayList<DocumentElement>();
    }

    @Override
    public void initialize() {
        iterate = (Boolean) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.viewpointConstraintStereotype, "iterate", true);
        expression = (String) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.viewpointConstraintStereotype, "expression", "");
        report = (Boolean) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.viewpointConstraintStereotype, "validationReport", false);
    }

}
