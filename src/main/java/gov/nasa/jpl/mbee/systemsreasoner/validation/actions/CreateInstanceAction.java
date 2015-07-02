package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Namespace;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

public class CreateInstanceAction extends GenericRuleViolationAction {
	
	/**
	 *  ADD CIRCULAR REFERENCE DETECTION
	 */
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_NAME = "Create Instance";
	
	private Classifier classifier;
	private Element context;
	private boolean createSpecializedType;
	private String name;
	
	private static Map<Property, InstanceSpecification> createdInstances = new HashMap<Property, InstanceSpecification>();

	public CreateInstanceAction(final Classifier classifier, final Element context) {
		this(classifier, context, DEFAULT_NAME);
	}
	
	public CreateInstanceAction(final Classifier classifier, final Element context, String name) {
		super(name, name, null, null);
		this.classifier = classifier;
		this.context = context;
	}
	
	public static InstanceSpecification createInstance(final Element instanceable, final Element context) {
		return createInstance(instanceable, context, new ArrayList<Property>());
	}
	
	public static InstanceSpecification createInstance(final Element instanceable, final Element context, final List<Property> traveled) {
		final Classifier classifier;
		if (instanceable instanceof Classifier) {
			createdInstances.clear();
			classifier = (Classifier) instanceable;
		}
		else if (instanceable instanceof Property && ((Property) instanceable).getType() instanceof Classifier) {
			//System.out.println("Instancing " + ((Property) instanceable).getQualifiedName());
			if (traveled.contains(instanceable)) {
				Application.getInstance().getGUILog().log("Caught circular reference for " + ((Property) instanceable).getQualifiedName() + ". Skipping instantiation.");
				return null;
			}
			traveled.add((Property) instanceable);
			classifier = (Classifier) ((Property) instanceable).getType();
		}
		else {
			return null;
		}
		
		if (classifier.isAbstract()) {
			//Application.getInstance().getGUILog().log(arg0);
		}
		
		String prefix = "";
		Element owner = context;
		if (context instanceof InstanceSpecification) {
			prefix = ((InstanceSpecification) context).getName() + ".";
			owner = context.getOwner();
		}
		
		final InstanceSpecification instance = Application.getInstance().getProject().getElementsFactory().createInstanceSpecificationInstance();
		instance.setName(prefix + (instanceable instanceof Property ? ((Property) instanceable).getName() : classifier.getName()));
		instance.getClassifier().add(classifier);
		
		if (!owner.canAdd(instance)) {
			Application.getInstance().getGUILog().log("Cannot add instance specification to " + (owner instanceof NamedElement ? ((NamedElement) owner).getQualifiedName() : owner) + ".");
			return null;
		}
		instance.setOwner(owner);
		
		final List<NamedElement> inheritedMembers = new ArrayList<NamedElement>();
		inheritedMembers.addAll(classifier.getInheritedMember());
		
		createSlots(instance, classifier, new ArrayList<Property>(traveled));
		return instance;
	}
	
	public static Map<Property, Slot> createSlots(final InstanceSpecification instance, final Classifier classifier, final List<Property> traveled) {
		final Map<Property, Slot> propertySlotMapping = new HashMap<Property, Slot>();
		final Map<Property, Slot> implicitCache = new HashMap<Property, Slot>();
		final List<Property> properties = new ArrayList<Property>();
		properties.addAll(classifier.getAttribute());
		
		for (final NamedElement inheritedMember : classifier.getInheritedMember()) {
			boolean toAdd = true;
			for (final Property property : properties) {
				if (property.hasRedefinedElement() && property.getRedefinedElement().contains(inheritedMember)) {
					toAdd = false;
					break;
				}
			}
			if (toAdd && inheritedMember instanceof Property) {
				properties.add((Property) inheritedMember);
			}
		}
		
		int loop = 0;
		while (!properties.isEmpty() && loop < 1000) {
			//System.out.print("Loop: " + ++loop + " : " + properties.size() + " - ");
			/*for (final Property property : properties) {
				System.out.print(property.getQualifiedName() + ", ");
			}
			System.out.println();*/
			final ListIterator<Property> propertyIterator = properties.listIterator();
			while (propertyIterator.hasNext()) {
				final Property property = propertyIterator.next();
				final List<Property> subsettingProperties = new ArrayList<Property>();
				if (property.has_propertyOfSubsettedProperty()) {
					for (final Property subsettingProperty : property.get_propertyOfSubsettedProperty()) {
						if (subsettingProperty.getClassifier().equals(classifier)) {
							subsettingProperties.add(subsettingProperty);
						}
					}
				}
				if (!subsettingProperties.isEmpty()) {
					/*System.out.println("I AM SUBSETTED: " + property.getQualifiedName());
					for (final Property subsettingProperty : property.get_propertyOfSubsettedProperty()) {
						System.out.println(subsettingProperty.getQualifiedName());
					}*/
					boolean missingDependency = false;
					for (final Property subsettingProperty : subsettingProperties) {
						if (!implicitCache.containsKey(subsettingProperty)) {
							//System.out.println("Missing property! " + subsettingProperty.getQualifiedName() + " : " + property.getQualifiedName());
							missingDependency = true;
							break;
						}
					}
					if (missingDependency) {
						/*System.out.println("Cache [" + implicitCache.size() + "]: ");
						for (final Map.Entry<Property, Slot> entrySet : implicitCache.entrySet()) {
							System.out.println("[" + entrySet.getKey().getQualifiedName() + "]=" + entrySet.getValue().getID());
						}
						if (traveled.contains(property)) {
							System.out.println("REMOVING " + property.getQualifiedName());
							propertyIterator.remove();
						}
						System.out.println("CONTINUING " + property.getQualifiedName());*/
						continue;
					}
					final Slot slot = Application.getInstance().getProject().getElementsFactory().createSlotInstance();
					slot.setDefiningFeature(property);
					for (final Property subsettingProperty : subsettingProperties) {
						for (final ValueSpecification vs : implicitCache.get(subsettingProperty).getValue()) {
							final ValueSpecification clonedValueSpec = (ValueSpecification) CopyPasting.copyPasteElement(vs, slot, false);
							slot.getValue().add(clonedValueSpec);
						}
					}
					slot.setOwningInstance(instance);
				}
				//else if (classifier.getAttribute().contains(property)) {
				else {
					final Slot slot = createSlot(property, instance, traveled);
					//System.out.println("Created " + slot.getDefiningFeature().getQualifiedName() + " [" + implicitCache.size() + "]");
					propertySlotMapping.put(property, slot);
					implicitCache.put(property, slot);
					for (final Property redefinedProperty : flattenRedefinedProperties(property)) {
						implicitCache.put(redefinedProperty, slot);
					}
					//System.out.println("New Cache Size: " + implicitCache.size());
				}
				propertyIterator.remove();
				
			}
		}
		return propertySlotMapping;
	}
	
	public static List<Property> flattenRedefinedProperties(final Property property) {
		final List<Property> redefinedProperties = new ArrayList<Property>();
		for (final Property redefinedProperty : property.getRedefinedProperty()) {
			redefinedProperties.addAll(flattenRedefinedProperties(redefinedProperty));
		}
		return redefinedProperties;
	}
	
	public static Slot createSlot(final Property property, final InstanceSpecification instance, final List<Property> traveled) {
		final Slot slot = Application.getInstance().getProject().getElementsFactory().createSlotInstance();
		slot.setDefiningFeature(property);
		createValueSpecification(property, instance, slot, traveled);
		slot.setOwningInstance(instance);
		return slot;
	}
	
	public static ValueSpecification createValueSpecification(final Property property, final InstanceSpecification instance, final Slot slot, final List<Property> traveled) {
		if (property.getDefaultValue() != null) {
			final ValueSpecification clonedValue = (ValueSpecification) CopyPasting.copyPasteElement(property.getDefaultValue(), slot, false);
			slot.getValue().add(clonedValue);
			return clonedValue;
		}
		else if (property.getType() != null && property.getType() instanceof Classifier) {
			final InstanceSpecification nestedInstance = createInstance(property, instance, traveled);
			if (nestedInstance == null) {
				return null;
			}
			final InstanceValue instanceValue = Application.getInstance().getProject().getElementsFactory().createInstanceValueInstance();
			instanceValue.setInstance(nestedInstance);
			instanceValue.setOwner(slot);
			slot.getValue().add(instanceValue);
			return instanceValue;
		}
		return null;
	}

	@Override
	public void run() {
		createInstance(classifier, context);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionName() {
		return "create instance";
	}
}
