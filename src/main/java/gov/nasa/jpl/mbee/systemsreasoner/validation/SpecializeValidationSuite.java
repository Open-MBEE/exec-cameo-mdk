package gov.nasa.jpl.mbee.systemsreasoner.validation;

import java.util.Collection;

import gov.nasa.jpl.mbee.ems.validation.actions.ExportName;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportName;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.CloneAttributeAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RedefineAttributeAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RenameElementAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;

public class SpecializeValidationSuite extends ValidationSuite implements Runnable {
	
	private static final String NAME = "Specialize";
	private Class clazz;
	
	private static final ValidationRule
		generalMissingRule = new ValidationRule("Missing General", "General is missing in generalization", ViolationSeverity.ERROR),
		generalNotClassRule = new ValidationRule("General Not Class", "General is not of type class", ViolationSeverity.ERROR),
		attributeMissingRule = new ValidationRule("Missing Owned Attribute", "Owned attribute is missing", ViolationSeverity.ERROR),
		attributeNameRule = new ValidationRule("Naming Inconsistency", "Attribute names are inconsistent", ViolationSeverity.WARNING);

	{
		this.addValidationRule(generalMissingRule);
		this.addValidationRule(generalNotClassRule);
		this.addValidationRule(attributeMissingRule);
		this.addValidationRule(attributeNameRule);
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
		
		for (final NamedElement ne : clazz.getInheritedMember()) {
			if (ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf()) {
				final RedefinableElement re = (RedefinableElement) ne;
				RedefinableElement redefinedElement = null;
				for (final Property p : clazz.getAttribute()) {
					if (p instanceof RedefinableElement && ((RedefinableElement) p).getRedefinedElement().contains(re)) {
						redefinedElement = (RedefinableElement) p;
						break;
					}
				}
				if (redefinedElement == null) {
					//System.out.println("Found needed redefinition: " + re);
					final ValidationRuleViolation v = new ValidationRuleViolation(clazz, attributeMissingRule.getDescription() + ": " + re.getQualifiedName());
		            v.addAction(new RedefineAttributeAction(clazz, re));
		            //v.addAction(vdiff);
		            attributeMissingRule.addViolation(v);
				}
				else if ((redefinedElement.getName() == null && re.getName() != null) || (redefinedElement.getName() != null && !redefinedElement.getName().equals(re.getName()))) {
					final ValidationRuleViolation v = new ValidationRuleViolation(redefinedElement, attributeNameRule.getDescription() + ": [GENERAL] " + re.getName() + " - [SPECIFIC] " + redefinedElement.getName());
					v.addAction(new RenameElementAction(redefinedElement, re, "Update General"));
					v.addAction(new RenameElementAction(re, redefinedElement, "Update Specific"));
					attributeNameRule.addViolation(v);
				}
				
			}
			//System.out.println("AAA: " + ne.getQualifiedName());
		}
		
		/*for (final RedefinableElement re : clazz.get_redefinableElementOfRedefinitionContext()) {
			System.out.println(re.getQualifiedName());
		}*/
		
		/*for (final Generalization g : clazz.getGeneralization()) {
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
			for (final RedefinableElement re : superClass) {
				if (p instanceof RedefinableElement) {
					boolean isRedefined = false;
					for (final Classifier c : ((RedefinableElement) p).getRedefinitionContext()) {
						System.out.println("Comparing " + c.getQualifiedName() + " with " + clazz.getQualifiedName());
						if (c.equals(clazz)) {
							isRedefined = true;
							break;
						}
					}
					if (!isRedefined) {
						System.out.println("Found needed redefinition: " + p);
						final ValidationRuleViolation v = new ValidationRuleViolation(clazz, propertyMissingRule.getDescription() + ": " + p.getQualifiedName());
			            v.addAction(new RedefineAttributeAction(clazz, p));
			            //v.addAction(vdiff);
			            propertyMissingRule.addViolation(v);
					}
				}
			}
		}*/
	}

}
