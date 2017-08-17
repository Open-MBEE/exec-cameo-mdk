package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

import java.util.*;

public class CreateSlotsAction extends GenericRuleViolationAction {

    /**
     * ADD CIRCULAR REFERENCE DETECTION
     */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NAME = "Create Slots";

    private InstanceSpecification instance;
    private boolean recurse, deleteSlots, deleteChildInstances;
    private String name;

    private ProgressStatus progressStatus;

    public CreateSlotsAction(final InstanceSpecification instance) {
        this(instance, false, false, false);
    }

    public CreateSlotsAction(final InstanceSpecification instance, final boolean recurse, final boolean deleteSlots, final boolean deleteChildInstances) {
        this(instance, recurse, deleteSlots, deleteChildInstances, DEFAULT_NAME);
    }

    public CreateSlotsAction(final InstanceSpecification instance, final boolean recurse, final boolean deleteSlots, final boolean deleteChildInstances, final String name) {
        super(name);
        this.instance = instance;
        this.recurse = recurse;
        this.deleteSlots = deleteSlots;
        this.deleteChildInstances = deleteChildInstances;
        this.name = name;
    }

    public static List<InstanceSpecification> getChildInstancesRecursively(final Slot slot) {
        final List<InstanceSpecification> instances = new ArrayList<InstanceSpecification>();
        for (final ValueSpecification vs : slot.getValue()) {
            InstanceSpecification instance;
            if (vs instanceof InstanceValue && (instance = ((InstanceValue) vs).getInstance()) != null) {
                instances.add(instance);
                for (final Slot childSlot : instance.getSlot()) {
                    instances.addAll(getChildInstancesRecursively(childSlot));
                }
            }
        }
        return instances;
    }

    public static List<Property> collectSlottableProperties(final Classifier classifier) {
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
        return properties;
    }

    public static Map<Property, Slot> createSlots(final InstanceSpecification instance, final boolean recurse, final boolean deleteSlots, final boolean deleteChildInstances, final ProgressStatus progressStatus) {
        if (progressStatus != null) {
            progressStatus.setIndeterminate(true);
        }
        if (deleteSlots && instance.hasSlot()) {
            for (final Slot slot : instance.getSlot()) {
                if (deleteChildInstances) {
                    for (final InstanceSpecification is : getChildInstancesRecursively(slot)) {
                        is.dispose();
                    }
                }
            }
            for (final Slot slot : instance.getSlot()) {
                slot.dispose();
            }
        }
        return createSlots(instance, recurse, new HashMap<Classifier, InstanceSpecification>());
    }

    public static Map<Property, Slot> createSlots(final InstanceSpecification instance, final boolean recurse, final Map<Classifier, InstanceSpecification> traveled) {
        final Map<Property, Slot> propertySlotMapping = new HashMap<Property, Slot>();
        final Map<Property, Slot> implicitCache = new HashMap<Property, Slot>();

        for (final Classifier classifier : instance.getClassifier()) {
            int loop = 0;
            final List<Property> properties = collectSlottableProperties(classifier);
            for (final Slot slot : instance.getSlot()) {
                if (slot.getDefiningFeature() != null) {
                    properties.remove(slot.getDefiningFeature());
                }
            }
            while (!properties.isEmpty() && loop++ < 100) {
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
                            if (subsettingProperty.getClassifier() != null && subsettingProperty.getClassifier().equals(classifier)) {
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
                                if (clonedValueSpec != null) {
                                    slot.getValue().add(clonedValueSpec);
                                }
                            }
                        }
                        slot.setOwningInstance(instance);
                    }
                    //else if (classifier.getAttribute().contains(property)) {
                    else {
                        final Slot slot = createSlot(property, instance, recurse, traveled);
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

    public static Slot createSlot(final Property property, final InstanceSpecification instance, final boolean recurse, final Map<Classifier, InstanceSpecification> traveled) {
        final Slot slot = Application.getInstance().getProject().getElementsFactory().createSlotInstance();
        slot.setDefiningFeature(property);
        createValueSpecification(property, instance, slot, recurse, traveled);
        slot.setOwningInstance(instance);
        return slot;
    }

    public static ValueSpecification createValueSpecification(final Property property, final InstanceSpecification instance, final Slot slot, final boolean recurse, final Map<Classifier, InstanceSpecification> traveled) {
        if (property.getDefaultValue() != null) {
            final ValueSpecification clonedValue = (ValueSpecification) CopyPasting.copyPasteElement(property.getDefaultValue(), slot, false);
            if (clonedValue == null) {
                return null;
            }
            slot.getValue().add(clonedValue);
            return clonedValue;
        }
        else if (property.getType() != null && property.getType() instanceof Classifier) {
            //System.out.println("Nested Instance: " + property.getQualifiedName());
            final InstanceSpecification nestedInstance = CreateInstanceAction.createInstance(property, instance, recurse, traveled);
            if (nestedInstance == null) {
                //System.out.println("Nested instance is null.");
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
        if (!instance.isEditable()) {
            Application.getInstance().getGUILog().log(instance.getQualifiedName() + " is not editable. Skipping slots creation.");
            return;
        }
        createSlots(instance, recurse, deleteSlots, deleteChildInstances, progressStatus);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        super.actionPerformed(e);
        ValidateAction.validate(instance);
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        super.execute(annotations);
        ValidateAction.validate(instance);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSessionName() {
        return name;
    }
}
