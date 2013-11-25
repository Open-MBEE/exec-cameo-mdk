package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.dgvalidation.Rule;
import gov.nasa.jpl.mbee.dgvalidation.Severity;
import gov.nasa.jpl.mbee.dgvalidation.Suite;
import gov.nasa.jpl.mbee.dgvalidation.Violation;
import gov.nasa.jpl.mbee.dgvalidation.util.DgvalidationSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DgvalidationDBSwitch extends DgvalidationSwitch<Object> {

    @Override
    public Object caseRule(Rule object) {
        ViolationSeverity vs = null;
        if (object.getSeverity() == Severity.DEBUG)
            vs = ViolationSeverity.DEBUG;
        else if (object.getSeverity() == Severity.ERROR)
            vs = ViolationSeverity.ERROR;
        else if (object.getSeverity() == Severity.FATAL)
            vs = ViolationSeverity.FATAL;
        else if (object.getSeverity() == Severity.INFO)
            vs = ViolationSeverity.INFO;
        else
            vs = ViolationSeverity.WARNING;
        ValidationRule res = new ValidationRule(object.getName(), object.getDescription(), vs);
        for (Violation v: object.getViolations()) {
            res.addViolation((ValidationRuleViolation)this.doSwitch(v));
        }
        return res;
    }

    @Override
    public Object caseViolation(Violation object) {
        if (object.getElementId() != null) {
            ValidationRuleViolation res = new ValidationRuleViolation((Element)Application.getInstance()
                    .getProject().getElementByID(object.getElementId()), object.getComment());
            return res;
        }
        return null;
    }

    @Override
    public Object caseSuite(Suite object) {
        ValidationSuite res = new ValidationSuite(object.getName());
        for (Rule r: object.getRules()) {
            res.addValidationRule((ValidationRule)this.doSwitch(r));
        }
        res.setOwnSection(object.isOwnSection());
        res.setShowDetail(object.isShowDetail());
        res.setShowSummary(object.isShowSummary());
        return res;
    }

}
