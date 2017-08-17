package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Instance2BSTAction extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_ID = "Convert Instance to BST";
    public List<InstanceSpecification> instances;


    public Instance2BSTAction(List<InstanceSpecification> instances) {
        super(DEFAULT_ID);
        this.instances = instances;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession(DEFAULT_ID);
        for (final InstanceSpecification instance : instances) {
            if (!instance.hasClassifier()) {
                Application.getInstance().getGUILog().log("Instance " + instance.getQualifiedName() + " has no classifiers. Skipping BST conversion.");
                continue;
            }
            instance2BST(instance);
        }
        SessionManager.getInstance().closeSession();
    }

    public static Classifier instance2BST(final InstanceSpecification instance) {
        return instance2BST(instance, null, null, new HashMap<InstanceSpecification, Classifier>());
    }

    public static Classifier instance2BST(final InstanceSpecification instance, final InstanceSpecification lastInstance, final Element owner, final Map<InstanceSpecification, Classifier> traveled) {
        if (traveled.containsKey(instance)) {
            //Application.getInstance().getGUILog().log("Detected circular reference: " + instance.getQualifiedName() + ". Skipping conversion.");
            return traveled.get(instance);
        }
        //System.out.println("Traveled to " + instance.getQualifiedName());
        //traveled.put(instance);

        final Classifier classifier = instance.getClassifier().get(0);
        final Classifier specific = (Classifier) CopyPasting.copyPasteElement(classifier, instance.getOwner(), true);
        if (specific == null) {
            return null;
        }
        for (final Generalization generalization : specific.getGeneralization()) {
            generalization.dispose();
        }
        for (final NamedElement ne : specific.getOwnedMember()) {
            ne.dispose();
        }

        for (final Classifier c : instance.getClassifier()) {
            SpecializeClassifierAction.specialize(specific, c);
        }
        if (owner != null && owner.canAdd(specific)) {
            specific.setOwner(owner);
        }
        traveled.put(instance, specific);

        specific.setName(lastInstance != null && instance.getName().startsWith(lastInstance.getName() + ".") ? instance.getName().replaceFirst(lastInstance.getName() + ".", "") : instance.getName());
        specific.setName(specific.getName().substring(0, 1).toUpperCase() + specific.getName().substring(1, specific.getName().length()));
        for (final Slot slot : instance.getSlot()) {
            if (slot.getDefiningFeature() instanceof Property) {
                final Property property = (Property) slot.getDefiningFeature();
                final Property redefinedAttribute = (Property) SetOrCreateRedefinableElementAction.redefineRedefinableElement(specific, property, true, false);

                if (!slot.hasValue()) {
                    continue;
                }
                if (slot.getValue().size() == 1) {
                    final ValueSpecification value = slot.getValue().get(0);
                    processAttribute(redefinedAttribute, value, instance, specific, traveled);
                    //Application.getInstance().getGUILog().log("Slot " + slot.getOwningInstance().getQualifiedName() + "::" + slot.getDefiningFeature().getName() + " has more than one value. Only applying the first to converted BST default value.");
                }
                else {
                    for (final ValueSpecification value : slot.getValue()) {
                        final Property subsettingProperty = Application.getInstance().getProject().getElementsFactory().createPropertyInstance();
                        subsettingProperty.setOwner(specific);
                        processAttribute(subsettingProperty, value, instance, specific, traveled);
                        if (subsettingProperty.getType() != null && subsettingProperty.getType() instanceof Classifier) {
                            final String name = subsettingProperty.getType().getName();
                            subsettingProperty.setName((name.substring(0, 1).toLowerCase() + name.substring(1, name.length())).replaceAll(" ", ""));
                        }
                        subsettingProperty.getSubsettedProperty().add(redefinedAttribute);
                    }
                }
            }
        }
        return specific;
    }

    public static void processAttribute(final Property property, final ValueSpecification value, final InstanceSpecification lastInstance, final Element owner, final Map<InstanceSpecification, Classifier> traveled) {
        //final ValueSpecification value = slot.getValue().get(0);
        if (value instanceof InstanceValue && ((InstanceValue) value).getInstance() != null) {
            final Classifier nestedClassifier = instance2BST(((InstanceValue) value).getInstance(), lastInstance, owner, traveled);
            property.setType(nestedClassifier);
        }
        else if (value instanceof ElementValue && ((ElementValue) value).getElement() instanceof Classifier) {
            property.setType((Classifier) ((ElementValue) value).getElement());
        }
        else {
            final ValueSpecification defaultValue = (ValueSpecification) CopyPasting.copyPasteElement(value, null, false);
            if (defaultValue == null) {
                return;
            }
            property.setDefaultValue(defaultValue);
        }
    }

}
