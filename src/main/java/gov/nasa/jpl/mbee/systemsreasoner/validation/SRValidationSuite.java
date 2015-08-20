package gov.nasa.jpl.mbee.systemsreasoner.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import gov.nasa.jpl.mbee.SRConfigurator;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.CreateSlotsAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.OpenSpecificationAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RedefineAttributeAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RenameElementAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.RetypeElementAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.SelectInContainmentTreeAction;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.SetRedefinitionAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import com.google.common.collect.Lists;
import com.nomagic.actions.NMAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

public class SRValidationSuite extends ValidationSuite implements Runnable {
	
	private static final String NAME = "SR Validate";
	private List<Element> elements;
	
	private static final ValidationRule
		generalMissingRule = new ValidationRule("Missing General", "General is missing in generalization", ViolationSeverity.ERROR),
		generalNotClassRule = new ValidationRule("General Not Class", "General is not of type class", ViolationSeverity.ERROR),
		attributeMissingRule = new ValidationRule("Missing Owned Attribute", "Owned attribute is missing", ViolationSeverity.ERROR),
		nameRule = new ValidationRule("Naming Inconsistency", "Names are inconsistent", ViolationSeverity.WARNING),
		attributeTypeRule = new ValidationRule("Attribute Type Inconsistency", "Attribute types are inconsistent", ViolationSeverity.WARNING),
		generalSpecificNameRule = new ValidationRule("General Specific Name Inconsistency", "General and specific names are inconsistent", ViolationSeverity.WARNING),
		//orphanAttributeRule = new ValidationRule("Potential Orphan", "First degree attribute is never redefined", ViolationSeverity.WARNING);
		instanceClassifierExistenceRule = new ValidationRule("Instance Classifier Unspecified", "Instance classifier is not specified", ViolationSeverity.ERROR),
		missingSlotsRule = new ValidationRule("Missing Slot(s) Detected", "Missing slot(s) detected", ViolationSeverity.ERROR);
	
	
	{
		this.addValidationRule(generalMissingRule);
		this.addValidationRule(generalNotClassRule);
		this.addValidationRule(attributeMissingRule);
		this.addValidationRule(nameRule);
		this.addValidationRule(attributeTypeRule);
		//this.addValidationRule(orphanAttributeRule);
		this.addValidationRule(generalSpecificNameRule);
		this.addValidationRule(instanceClassifierExistenceRule);
		this.addValidationRule(missingSlotsRule);
	}

	public SRValidationSuite(final Element element) {
		this(Lists.newArrayList(element));
	}
	
	public SRValidationSuite(final List<Element> elements) {
		super(NAME);
		this.elements = elements;
	}

	@Override
	public void run() {
		for (final ValidationRule vr : this.getValidationRules()) {
			vr.getViolations().clear();
		}
		
		final ListIterator<Element> iterator = elements.listIterator();
		while (iterator.hasNext()) {
			final Element element = iterator.next();
			
			if (element instanceof Classifier) {
				final Classifier classifier = (Classifier) element;
				
				// traverse the heirarchy down
				for (final Generalization generalization : classifier.get_generalizationOfGeneral()) {
					if (!elements.contains(generalization.getSpecific())) {
						iterator.add(generalization.getSpecific());
						iterator.previous();
					}
				}
				
				for (final InstanceSpecification instance : classifier.get_instanceSpecificationOfClassifier()) {
					if (!elements.contains(instance)) {
						iterator.add(instance);
						iterator.previous();
					}
				}
				
				for (final Classifier general : classifier.getGeneral()) {
					if (!classifier.getName().equals(general.getName())) {
						final ValidationRuleViolation v = new ValidationRuleViolation(classifier, generalSpecificNameRule.getDescription() + ": [GENERAL] " + general.getName() + " - [SPECIFIC] " + classifier.getName());
						v.addAction(new RenameElementAction(general, classifier, "Update Specific"));
						v.addAction(new RenameElementAction(classifier, general, "Update General"));
						generalSpecificNameRule.addViolation(v);
					}
				}
				
				for (final NamedElement ne : classifier.getInheritedMember()) {
					if (ne instanceof Property && ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf()) {
						final RedefinableElement redefinableElement = (RedefinableElement) ne;
						RedefinableElement redefiningElement = null;
						for (final Property p : classifier.getAttribute()) {
							if (redefinableElement instanceof Property && doesEventuallyRedefine(p, (Property) redefinableElement)) {
							//if (p instanceof RedefinableElement && ((RedefinableElement) p).getRedefinedElement().contains(redefinableElement)) {
								redefiningElement = (RedefinableElement) p;
								break;
							}
						}
						if (redefiningElement == null) {
							boolean redefinedInContext = false;
							for (final NamedElement ne2 : classifier.getInheritedMember()) {
								if (ne2 instanceof Property && ne2 instanceof RedefinableElement && doesEventuallyRedefine((Property) ne2, (Property) redefinableElement)) {
									redefinedInContext = true;
									break;
								}
							}
							if (!redefinedInContext) {
								final ValidationRuleViolation v = new ValidationRuleViolation(classifier, (redefinableElement instanceof TypedElement && ((TypedElement) redefinableElement).getType() != null ? "[TYPED] " : "") + attributeMissingRule.getDescription() + ": " + redefinableElement.getQualifiedName());
								for (final Property p : classifier.getAttribute()) {
									if (p.getName().equals(redefinableElement.getName()) && !p.hasRedefinedElement()) {
										v.addAction(new SetRedefinitionAction(p, redefinableElement, "Redefine by Name Collision"));
									}
								}
					            v.addAction(new RedefineAttributeAction(classifier, redefinableElement));
					            if (redefinableElement instanceof TypedElement && ((TypedElement) redefinableElement).getType() != null) {
					            	// intentionally showing this option even if the type isn't specializable so the user doesn't have to go through
					            	// grouping them separately to validate. It will just ignore and log if a type isn't specializable.
					            	v.addAction(new RedefineAttributeAction(classifier, redefinableElement, true, "Redefine Attribute & Specialize Types Recursively"));
					            }
					            attributeMissingRule.addViolation(v);
							}
						}
						else {
							if ((redefiningElement.getName() == null && redefinableElement.getName() != null) || (redefiningElement.getName() != null && !redefiningElement.getName().equals(redefinableElement.getName()))) {
								final ValidationRuleViolation v = new ValidationRuleViolation(redefiningElement, nameRule.getDescription() + ": [GENERAL] " + redefinableElement.getName() + " - [SPECIFIC] " + redefiningElement.getName());
								v.addAction(new RenameElementAction(redefinableElement, redefiningElement, "Update Specific"));
								v.addAction(new RenameElementAction(redefiningElement, redefinableElement, "Update General"));
								nameRule.addViolation(v);
							}
							if (redefiningElement instanceof TypedElement && redefinableElement instanceof TypedElement) {
								final TypedElement redefiningTypedElement = (TypedElement) redefiningElement;
								final TypedElement redefinableTypedElement = (TypedElement) redefinableElement;
								
								if ((redefiningTypedElement.getType() == null && redefinableTypedElement.getType() != null) || (redefiningTypedElement.getType() != null && redefiningTypedElement.getType() instanceof Classifier && redefinableTypedElement.getType() instanceof Classifier && !doesEventuallyGeneralizeTo((Classifier) redefiningTypedElement.getType(), (Classifier) redefinableTypedElement.getType()))) {
									if (redefiningTypedElement.getType() instanceof Classifier && redefinableTypedElement.getType() instanceof Classifier && ((Classifier) redefiningTypedElement.getType()).getGeneral().contains(redefinableTypedElement.getType())) {
										iterator.add(((Classifier) redefiningTypedElement.getType()));
										iterator.previous();
									}
									else {
										final ValidationRuleViolation v = new ValidationRuleViolation(redefiningTypedElement, attributeTypeRule.getDescription() + ": [GENERAL] " + (redefinableTypedElement.getType() != null ? redefinableTypedElement.getType().getQualifiedName() : "null") + " - [SPECIFIC] " + (redefiningTypedElement.getType() != null ? redefiningTypedElement.getType().getQualifiedName() : "null"));
										v.addAction(new RetypeElementAction(redefinableTypedElement, redefiningTypedElement, "Update Specific"));
										v.addAction(new RetypeElementAction(redefiningTypedElement, redefinableTypedElement, "Update General"));
										attributeTypeRule.addViolation(v);
									}
								}
							}
						}
						
					}
				}
			}
			else if (element instanceof InstanceSpecification) {
				final InstanceSpecification instance = (InstanceSpecification) element;
				
				for (final Slot slot : instance.getSlot()) {
					for (final ValueSpecification vs : slot.getValue()) {
						final InstanceSpecification i;
						if (vs instanceof InstanceValue && (i = ((InstanceValue) vs).getInstance()) != null && !elements.contains(i)) {
							iterator.add(i);
							iterator.previous();
						}
					}
				}
				
				if (!instance.hasClassifier()) {
					final ValidationRuleViolation v = new ValidationRuleViolation(instance, instanceClassifierExistenceRule.getDescription() + ": " + instance.getQualifiedName());
					v.addAction(new OpenSpecificationAction(instance));
					v.addAction(new SelectInContainmentTreeAction(instance));
					instanceClassifierExistenceRule.addViolation(v);
					continue;
				}
				
				//boolean needsReslotting = false;
				final List<Property> missingProperties = new ArrayList<Property>();
				for (final Classifier classifier : instance.getClassifier()) {
					for (final Property property : CreateSlotsAction.collectSlottableProperties(classifier)) {
						boolean isDefined = false;
						for (final Slot slot : instance.getSlot()) {
							if (slot.getDefiningFeature().equals(property)) {
								isDefined = true;
								break;
							}
						}
						if (!isDefined) {
							//needsReslotting = true;
							missingProperties.add(property);
							//break;
						}
					}
					/*if (needsReslotting) {
						break;
					}*/
				}
				//if (needsReslotting) {
				if (!missingProperties.isEmpty()) {
					String suffix = "";
					if (instance.hasSlot()) {
						suffix += ": ";
						for (int i = 0; i < missingProperties.size(); i++) {
							final Property property = missingProperties.get(i);
							suffix += property.getName() != null && !property.getName().isEmpty() ? property.getName() : "<>";
							if (i != missingProperties.size() - 1) {
								suffix += ", ";
							}
						}
					}
					final ValidationRuleViolation v = new ValidationRuleViolation(instance, (!instance.hasSlot() ? missingSlotsRule.getDescription().replaceFirst("Missing", "No") : missingSlotsRule.getDescription()) + suffix);
					v.addAction(IndeterminateProgressMonitorProxy.doubleWrap(new CreateSlotsAction(instance, false, false, false, "Create Missing Slots"), "Systems Reasoner"));
					v.addAction(IndeterminateProgressMonitorProxy.doubleWrap(new CreateSlotsAction(instance, false, false, true, "Recreate Slots"), "Systems Reasoner"));
					v.addAction(IndeterminateProgressMonitorProxy.doubleWrap(new CreateSlotsAction(instance, false, true, true, "Delete Child Instances & Recreate Slots"), "Systems Reasoner"));
					
					v.addAction(IndeterminateProgressMonitorProxy.doubleWrap(new CreateSlotsAction(instance, true, false, false, "[R] Create Missing Slots"), "Systems Reasoner"));
					v.addAction(IndeterminateProgressMonitorProxy.doubleWrap(new CreateSlotsAction(instance, true, false, true, "[R] Recreate Slots"), "Systems Reasoner"));
					v.addAction(IndeterminateProgressMonitorProxy.doubleWrap(new CreateSlotsAction(instance, true, true, true, "[R] Delete Child Instances & Recreate Slots"), "Systems Reasoner"));
					missingSlotsRule.addViolation(v);
				}
			}
		}
		
		/*for (final ValidationRule vr : this.getValidationRules()) {
			for (final ValidationRuleViolation vrv : vr.getViolations()) {
				final List<NMAction> clonedActions = Lists.newArrayList(vrv.getActions());
				vrv.getActions().clear();
				for (final NMAction action : clonedActions) {
					vrv.getActions().add(IndeterminateProgressMonitorProxy.doubleWrap(action, SRConfigurator.NAME));
				}
			}
		}*/
			
			// Was an experimental rule to help find extra information, but was abandoned as per Bjorn's advice
			
			/*for (final Property p : classifier.getAttribute()) {
				if (p instanceof RedefinableElement && !((RedefinableElement) p).has_redefinableElementOfRedefinedElement()) {
					// Cannot be replaced by hasRedefinedElement, which returns true even after generalization has been deleted.
					boolean hasRedefinedElement = false;
					for (final RedefinableElement re : ((RedefinableElement) p).getRedefinedElement()) {
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
		}*/
		
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
	
	public static boolean doesEventuallyRedefine(final Property source, final Property target) {
		if (source.getRedefinedProperty().contains(target)) {
			return true;
		}
		for (final Property p : source.getRedefinedProperty()) {
			if (doesEventuallyRedefine(p, target)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean doesEventuallyGeneralizeTo(final Classifier source, final Classifier target) {
		if (source.getGeneral().contains(target)) {
			return true;
		}
		for (final Classifier classifier : source.getGeneral()) {
			if (doesEventuallyGeneralizeTo(classifier, target)) {
				return true;
			}
		}
		return false;
	}

}
