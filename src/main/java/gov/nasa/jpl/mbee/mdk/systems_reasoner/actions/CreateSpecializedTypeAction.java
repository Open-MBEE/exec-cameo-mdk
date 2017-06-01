package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

import java.lang.Class;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateSpecializedTypeAction extends GenericRuleViolationAction {

    public static final List<Class<? extends Classifier>> UNSPECIALIZABLE_CLASSIFIERS = new ArrayList<Class<? extends Classifier>>();

    static {
        UNSPECIALIZABLE_CLASSIFIERS.add(DataType.class);
        UNSPECIALIZABLE_CLASSIFIERS.add(PrimitiveType.class);
    }

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NAME = "Create Specialized Classifier";

    private Property property;
    private Classifier parent;
    private boolean redefineAttributes;
    private String name;
    private boolean isIndividual;


    public CreateSpecializedTypeAction(final Property property, final Classifier parent, final boolean redefineAttributes, final String name, boolean isIndividual) {
        super(name);
        this.property = property;
        this.parent = parent;
        this.redefineAttributes = redefineAttributes;
        this.name = name;
        this.isIndividual = isIndividual;
    }

    public static final void createSpecializedType(final Property property, final Classifier parent, final boolean redefineAttributes, boolean isIndividual) {
        createSpecializedType(property, parent, redefineAttributes, new ArrayList<RedefinableElement>(), new ArrayList<Classifier>(), isIndividual);
    }

    public static final boolean createSpecializedType(final StructuralFeature redefinedAttribute, final Classifier parent, final boolean redefineAttributes, final List<RedefinableElement> traveled, List<Classifier> visited, boolean isIndividual) {
        if (!parent.isEditable()) {
            Application.getInstance().getGUILog().log(parent.getQualifiedName() + " is not editable. Skipping creating specialization.");
            return true;
        }
        if (redefinedAttribute.getType() instanceof Classifier && !(redefinedAttribute.getType() instanceof Property)) {
            boolean hasTraveled = false;
            if (traveled.contains(redefinedAttribute)) {
                hasTraveled = true;
            }
            else {
                for (final RedefinableElement redefinedProperty : redefinedAttribute.getRedefinedElement()) {
                    if (traveled.contains(redefinedProperty)) {
                        hasTraveled = true;
                        break;
                    }
                }
            }
            if (hasTraveled) {
                Application.getInstance().getGUILog().log("[WARNING] Detected circular reference at " + redefinedAttribute.getQualifiedName() + ". Stopping recursion.");
                return false;
            }

            traveled.add(redefinedAttribute);
            for (final RedefinableElement re : redefinedAttribute.getRedefinedElement()) {
                if (re instanceof RedefinableElement) {
                    traveled.add(re);
                }
            }
            final Classifier general = (Classifier) redefinedAttribute.getType();
            if (visited.contains(general)) {
                Application.getInstance().getGUILog().log("[WARNING] Detected circular reference. Type  " + general.getQualifiedName() + " referenced by " + redefinedAttribute.getQualifiedName() + " was already visited. Stopping recursion.");
                return false;
            }

            Type special = null;
            if (!isIndividual && getExistingSpecial(redefinedAttribute) != null) {
                special = getExistingSpecial(redefinedAttribute);
            }
            else {
                // special = createSpecializedClassifier(general, parent, redefinedAttribute);
                SpecializeStructureAction speca = new SpecializeStructureAction(general, false, "", redefineAttributes, isIndividual);
                special = speca.createSpecialClassifier(parent, traveled, visited);
            }
            if (special == null) {
                return true;
            }

            redefinedAttribute.setType(special);
            if (redefineAttributes) {
                if (special instanceof Classifier) {
                    for (final NamedElement ne : ((Classifier) special).getInheritedMember()) {
                        if (ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf()) {
                            SetOrCreateRedefinableElementAction.redefineRedefinableElement((Classifier) special, (RedefinableElement) ne, true, traveled, visited, isIndividual);
                        }
                    }
                }
            }
        }
        return true;
    }

    private static Type getExistingSpecial(StructuralFeature structuralFeature) {
        Set<Type> types = new HashSet<Type>();
        Element owner = structuralFeature.getOwner();
        for (RedefinableElement redef : structuralFeature.getRedefinedElement()) {
            if (redef instanceof TypedElement) {
                types.add(((TypedElement) redef).getType());
                //System.out.println("Found type: "+((TypedElement) redef).getType().getName() +" id  "+ ((TypedElement) redef).getType().toString() + " for SF " + structuralFeature.getName() + "  " + structuralFeature.toString());
            }
        }
        for (Element oe : owner.getOwnedElement()) {
            if (oe instanceof Property) {
                if (!oe.equals(structuralFeature)) {
                    for (RedefinableElement redef : ((Property) oe).getRedefinedElement()) {
                        if (redef instanceof TypedElement) {
                            if (types.contains(((TypedElement) redef).getType())) {
                                //System.out.println("Found type: "+((TypedElement) oe).getType().getName() +" id  "+ ((TypedElement) oe).getType().toString() + " for SF " + redef.getName() + "  " + redef.toString());
                                return ((Property) oe).getType();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public static final Classifier createSpecializedClassifier(final Classifier general, final Classifier parent, final StructuralFeature structuralFeature) {
        for (final Class<? extends Classifier> c : UNSPECIALIZABLE_CLASSIFIERS) {
            if (c.isAssignableFrom(general.getClass())) {
                Application.getInstance().getGUILog()
                        .log("[WARNING] " + (structuralFeature != null ? structuralFeature.getQualifiedName() : "< >") + " is a " + c.getSimpleName() + ", which is not specializable.");
                return null;
            }
        }
        // System.out.println(general.getQualifiedName());
        //   final Classifier special = (Classifier) CopyPasting.copyPasteElement(general, parent);

        // Collection<?> emptyCollection = new ArrayList<String>();
        // special.getOwnedMember().retainAll(emptyCollection);
        //  special.getGeneralization().retainAll(emptyCollection);


        Classifier specific = (Classifier) CopyPasting.copyPasteElement(general, parent);

        ArrayList<NamedElement> members = new ArrayList<>();
        for (NamedElement ne : specific.getOwnedMember()) {
            members.add(ne);
        }
        for (NamedElement member : members) {
            if (member instanceof RedefinableElement) {
                specific.getOwnedMember().remove(member);
                member.dispose();
            }
        }
        specific.getGeneralization().clear();
        SpecializeClassifierAction.specialize(specific, general);
        return specific;
    }

    @Override
    public void run() {
        CreateSpecializedTypeAction.createSpecializedType(property, parent, redefineAttributes, isIndividual);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSessionName() {
        return "create specialized classifier";
    }
}
