package org.openmbee.mdk.validation;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidationRule {
    private String name;
    private ViolationSeverity severity;
    private List<ValidationRuleViolation> violations;
    private String description;

    public ValidationRule(String name, String description, ViolationSeverity severity) {
        this.name = name;
        this.severity = severity;
        this.description = description;
        violations = new ArrayList<ValidationRuleViolation>();
    }

    public ValidationRuleViolation addViolation(ValidationRuleViolation v) {
        violations.add(v);
        return v;
    }

    public ValidationRuleViolation addViolation(Element e, String comment) {
        return addViolation(e, comment, false);
    }

    public ValidationRuleViolation addViolation(Element e, String comment, boolean reported) {
        return addViolation(new ValidationRuleViolation(e, comment, reported));
    }

    public List<ValidationRuleViolation> addViolations(Collection<ValidationRuleViolation> viols) {
        if (viols != null) {
            for (ValidationRuleViolation v : viols) {
                addViolation(v);
            }
        }
        return violations;
    }

    public String getName() {
        return name;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public List<ValidationRuleViolation> getViolations() {
        return violations;
    }

    public String getDescription() {
        return description;
    }
}
