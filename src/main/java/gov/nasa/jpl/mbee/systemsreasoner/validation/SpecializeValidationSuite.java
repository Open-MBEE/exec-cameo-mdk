package gov.nasa.jpl.mbee.systemsreasoner.validation;

import java.util.Collection;

import gov.nasa.jpl.mbee.ems.validation.actions.ExportName;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportName;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.CloneAttributeAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class SpecializeValidationSuite extends ValidationSuite implements Runnable {
	
	private static final String NAME = "Specialize";
	private Class clazz;
	
	private static final ValidationRule
		generalMissingRule = new ValidationRule("Missing General Property", "General property is missing", ViolationSeverity.ERROR),
		generalNotClassRule = new ValidationRule("General Not Class", "General property is not of type class", ViolationSeverity.ERROR),
		propertyMissingRule = new ValidationRule("Missing Property", "Property is missing", ViolationSeverity.ERROR);

	{
		this.addValidationRule(generalMissingRule);
		this.addValidationRule(generalNotClassRule);
		this.addValidationRule(propertyMissingRule);
		//this.addValidationRule(idRule);
	}

	public SpecializeValidationSuite(final Class clazz) {
		super(NAME);
		this.clazz = clazz;
	}

	@Override
	public void run() {
		for (final ValidationRule vr : this.getValidationRules()) {
			vr.getViolations().clear();
		}
		
		for (final Generalization g : clazz.getGeneralization()) {
			if (g.getGeneral() == null) {
				generalMissingRule.addViolation(g, generalMissingRule.getDescription());
				continue;
			}
			if (!(g.getGeneral() instanceof Class)) {
				generalNotClassRule.addViolation(g, generalNotClassRule.getDescription());
				continue;
			}
			final Class superClass = ((Class) g.getGeneral());
			//System.out.println(superClass.getQualifiedName());
			for (final Property p : superClass.getOwnedAttribute()) {
				System.out.println(p);
				final ValidationRuleViolation v = new ValidationRuleViolation(clazz, propertyMissingRule.getDescription() + ": " + p.getQualifiedName());
	            v.addAction(new CloneAttributeAction(clazz, p));
	            //v.addAction(vdiff);
	            propertyMissingRule.addViolation(v);
			}
		}
	}

}
