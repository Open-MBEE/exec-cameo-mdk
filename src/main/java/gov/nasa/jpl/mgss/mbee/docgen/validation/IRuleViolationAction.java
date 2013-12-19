package gov.nasa.jpl.mgss.mbee.docgen.validation;


import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.validation.RuleViolationResult;

public interface IRuleViolationAction {

    public void setAnnotation(Annotation anno);
    public void setRuleViolationResult(RuleViolationResult rvr);
    public void setValidationWindowRun(ValidationWindowRun vwr);
}
