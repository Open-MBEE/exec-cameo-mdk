package org.openmbee.mdk.validation;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.validation.RuleViolationResult;

public interface IRuleViolationAction {

    void setAnnotation(Annotation anno);

    void setRuleViolationResult(RuleViolationResult rvr);

    void setValidationWindowRun(ValidationWindowRun vwr);
}
