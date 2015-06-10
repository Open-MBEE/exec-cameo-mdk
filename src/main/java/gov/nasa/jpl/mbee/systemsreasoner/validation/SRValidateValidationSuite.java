package gov.nasa.jpl.mbee.systemsreasoner.validation;

import java.util.Collection;
import java.util.List;

import gov.nasa.jpl.mbee.ems.validation.actions.ExportName;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportName;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.CloneAttributeAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.DeleteElementAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RedefineAttributeAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RenameElementAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RetypeElementAction;
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;

public class SRValidateValidationSuite extends ValidationSuite implements Runnable {
	
	private static final String NAME = "Specialize";
	private List<Classifier> classes;
	
	private static final ValidationRule
		generalMissingRule = new ValidationRule("Missing General", "General is missing in generalization", ViolationSeverity.ERROR),
		generalNotClassRule = new ValidationRule("General Not Class", "General is not of type class", ViolationSeverity.ERROR),
		attributeMissingRule = new ValidationRule("Missing Owned Attribute", "Owned attribute is missing", ViolationSeverity.ERROR),
		attributeNameRule = new ValidationRule("Naming Inconsistency", "Attribute names are inconsistent", ViolationSeverity.WARNING),
		attributeTypeRule = new ValidationRule("Type Inconsistency", "Attribute types are inconsistent", ViolationSeverity.WARNING),
		orphanAttributeRule = new ValidationRule("Potential Orphan", "First degree attribute is never redefined", ViolationSeverity.WARNING);
		// ADD BELOW!!
	
	{
		this.addValidationRule(generalMissingRule);
		this.addValidationRule(generalNotClassRule);
		this.addValidationRule(attributeMissingRule);
		this.addValidationRule(attributeNameRule);
		this.addValidationRule(attributeTypeRule);
		this.addValidationRule(orphanAttributeRule);
		//this.addValidationRule(idRule);
	}

	public SRValidateValidationSuite(final Classifier clazz) {
		this(Utils2.newList(clazz));
	}
	
	public SRValidateValidationSuite(final List<Classifier> classes) {
		super(NAME);
		this.classes = classes;
	}

	@Override
	public void run() {
		for (final ValidationRule vr : this.getValidationRules()) {
			vr.getViolations().clear();
		}
		
		for (final Classifier clazz : classes) {
			for (final NamedElement ne : clazz.getInheritedMember()) {
				if (ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf()) {
					final RedefinableElement redefinableElement = (RedefinableElement) ne;
					RedefinableElement redefiningElement = null;
					for (final Property p : clazz.getAttribute()) {
						if (p instanceof RedefinableElement && ((RedefinableElement) p).getRedefinedElement().contains(redefinableElement)) {
							redefiningElement = (RedefinableElement) p;
							break;
						}
					}
					if (redefiningElement == null) {
						//System.out.println("Found needed redefinition: " + re);
						final ValidationRuleViolation v = new ValidationRuleViolation(clazz, attributeMissingRule.getDescription() + ": " + redefinableElement.getQualifiedName());
			            v.addAction(new RedefineAttributeAction(clazz, redefinableElement));
			            //v.addAction(vdiff);
			            attributeMissingRule.addViolation(v);
					}
					else {
						if ((redefiningElement.getName() == null && redefinableElement.getName() != null) || (redefiningElement.getName() != null && !redefiningElement.getName().equals(redefinableElement.getName()))) {
							final ValidationRuleViolation v = new ValidationRuleViolation(redefiningElement, attributeNameRule.getDescription() + ": [GENERAL] " + redefinableElement.getName() + " - [SPECIFIC] " + redefiningElement.getName());
							v.addAction(new RenameElementAction(redefinableElement, redefiningElement, "Update Specific"));
							v.addAction(new RenameElementAction(redefiningElement, redefinableElement, "Update General"));
							attributeNameRule.addViolation(v);
						}
						if (redefiningElement instanceof TypedElement && redefinableElement instanceof TypedElement) {
							final TypedElement redefiningTypedElement = (TypedElement) redefiningElement;
							final TypedElement redefinableTypedElement = (TypedElement) redefinableElement;
							
							if ((redefiningTypedElement.getType() == null && redefinableTypedElement.getType() != null) || (redefiningTypedElement.getType() != null && !redefiningTypedElement.getType().equals(redefinableTypedElement.getType()))) {
								final ValidationRuleViolation v = new ValidationRuleViolation(redefiningTypedElement, attributeTypeRule.getDescription() + ": [GENERAL] " + (redefinableTypedElement.getType() != null ? redefinableTypedElement.getType().getQualifiedName() : "null") + " - [SPECIFIC] " + (redefiningTypedElement.getType() != null ? redefiningTypedElement.getType().getQualifiedName() : "null"));
								v.addAction(new RetypeElementAction(redefinableTypedElement, redefiningTypedElement, "Update Specific"));
								v.addAction(new RetypeElementAction(redefiningTypedElement, redefinableTypedElement, "Update General"));
								attributeTypeRule.addViolation(v);
							}
						}
					}
					
				}
			}
			
			for (final Property p : clazz.getAttribute()) {
				System.out.println("asdf: " + p.getQualifiedName());
				System.out.println("V1: " + Boolean.toString(p instanceof RedefinableElement));
				System.out.println("V2: " + Boolean.toString(!((RedefinableElement) p).hasRedefinedElement()));
				for (final RedefinableElement re : ((RedefinableElement) p).getRedefinedElement()) {
					System.out.println("RE: " + re.getQualifiedName());
				}
				System.out.println("V3: " + Boolean.toString(!((RedefinableElement) p).has_redefinableElementOfRedefinedElement()));
				if (p instanceof RedefinableElement && !((RedefinableElement) p).has_redefinableElementOfRedefinedElement()) {
					// Cannot be replaced by hasRedefinedElement, which returns true even after generalization has been deleted.
					boolean hasRedefinedElement = false;
					for (final RedefinableElement re : ((RedefinableElement) p).getRedefinedElement()) {
						System.out.println("CLASSES: " + ((Classifier) p.getOwner()).getGeneral());
						if (re.getOwner() != null && p.getOwner() != null && p.getOwner() instanceof Classifier && ((Classifier) p.getOwner()).getGeneral().contains(re.getOwner())) {
							hasRedefinedElement = true;
							break;
						}
					}
					if (!hasRedefinedElement) {
						final ValidationRuleViolation v = new ValidationRuleViolation(p, orphanAttributeRule.getDescription() + ": " + p.getQualifiedName());
						v.addAction(new DeleteElementAction(p, "Delete Attribute"));
						orphanAttributeRule.addViolation(v);
					}
				}
			}
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
