package gov.nasa.jpl.mgss.mbee.docgen.validation;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

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
	
	public void addViolation(ValidationRuleViolation v) {
		violations.add(v);
	}
	
	public void addViolation(Element e, String comment) {
		violations.add(new ValidationRuleViolation(e, comment));
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
