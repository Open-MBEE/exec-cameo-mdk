package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

import java.util.*;

public class CreateInstanceAction extends GenericRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NAME = "Create Instance";

    private Classifier classifier;
    private Element context;
    private String name;
    private boolean createSlots;
    private InstanceSpecification instance;

    private static int safetyNet;

    private static Map<Property, InstanceSpecification> createdInstances = new HashMap<Property, InstanceSpecification>();

    public CreateInstanceAction(final Classifier classifier, final Element context, final boolean createSlots) {
        this(classifier, context, createSlots, DEFAULT_NAME);
    }

    public CreateInstanceAction(final Classifier classifier, final Element context, final boolean createSlots, String name) {
        super(name);
        this.classifier = classifier;
        this.context = context;
        this.createSlots = createSlots;
    }

    public static InstanceSpecification createInstance(final Element instanceable, final Element context, final boolean createSlots) {
        safetyNet = 0;
        return createInstance(instanceable, context, createSlots, new HashMap<Classifier, InstanceSpecification>());
    }

    public static InstanceSpecification createInstance(final Element instanceable, final Element context, final boolean createSlots, final Map<Classifier, InstanceSpecification> traveled) {
        final Classifier classifier;
        if (instanceable instanceof Classifier) {
            createdInstances.clear();
            classifier = (Classifier) instanceable;
        }
        else if (instanceable instanceof Property && ((Property) instanceable).getType() instanceof Classifier) {
            classifier = (Classifier) ((Property) instanceable).getType();
        }
        else {
            return null;
        }

        if (classifier.isAbstract()) {
            Application.getInstance().getGUILog().log(classifier.getQualifiedName() + " is marked as abstract. Skipping instantiation.");
            return null;
        }

        //System.out.println("Instancing " + ((Property) instanceable).getQualifiedName() + " | Traveled: " + traveled.size());
        if (traveled.containsKey(classifier)) {
            Application.getInstance().getGUILog().log("Caught circular reference for " + ((Property) instanceable).getQualifiedName() + ". Skipping instantiation and applying circular reference.");
            return traveled.get(classifier);
            //return null;
        }
        System.out.println("Classifier: " + classifier.getQualifiedName());

        String prefix = "";
        Element owner = context;
        if (context instanceof InstanceSpecification) {
            prefix = ((InstanceSpecification) context).getName() + ".";
            owner = context.getOwner();
        }

        final InstanceSpecification instance = Application.getInstance().getProject().getElementsFactory().createInstanceSpecificationInstance();
        traveled.put(classifier, instance);
        /*if (instanceable instanceof Property) {
            traveled.put((Property) instanceable, instance);
			System.out.println("Adding to traveled: " + ((Property) instanceable).getQualifiedName() + " [" + traveled.size() + "]");
		}*/

        instance.setName(prefix + (instanceable instanceof Property ? ((Property) instanceable).getName() : classifier.getName()));
        instance.getClassifier().add(classifier);

        if (!owner.canAdd(instance)) {
            Application.getInstance().getGUILog().log("Cannot add instance specification to " + (owner instanceof NamedElement ? ((NamedElement) owner).getQualifiedName() : owner) + ".");
            return null;
        }
        instance.setOwner(owner);

        final List<NamedElement> inheritedMembers = new ArrayList<NamedElement>();
        inheritedMembers.addAll(classifier.getInheritedMember());

		/*if (safetyNet++ > 1000) {
            Application.getInstance().getGUILog().log("Exceeded safety net max instantiations. Aborting...");
			throw new RuntimeException("Show me stack");
			//return instance;
		}*/
        if (createSlots) {
            //final Map<Property, InstanceSpecification> clonedMap = new HashMap<Property, InstanceSpecification>(traveled);
            //System.out.println("Clone size: " + clonedMap.size());

            CreateSlotsAction.createSlots(instance, createSlots, new HashMap<Classifier, InstanceSpecification>(traveled));
        }
        return instance;
    }

    @Override
    public void run() {
        instance = createInstance(classifier, context, createSlots);
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
