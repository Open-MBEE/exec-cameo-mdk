package gov.nasa.jpl.mbee.mdk.validation.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

import java.util.*;
import java.lang.Class;

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
        createSpecializedType(property, parent, redefineAttributes, new ArrayList<RedefinableElement>(), isIndividual);
    }

    // NEEDS BETTER CIRCULAR DETECTION
    public static final void createSpecializedType(final StructuralFeature structuralFeature, final Classifier parent, final boolean redefineAttributes, final List<RedefinableElement> traveled, boolean isIndividual) {
        if (!parent.isEditable()) {
            Application.getInstance().getGUILog().log(parent.getQualifiedName() + " is not editable. Skipping creating specialization.");
            return;
        }
        if (structuralFeature.getType() instanceof Classifier && !(structuralFeature.getType() instanceof Property)) {
            boolean hasTraveled = false;
            if (traveled.contains(structuralFeature)) {
                hasTraveled = true;
            }
            else {
                for (final RedefinableElement redefinedProperty : structuralFeature.getRedefinedElement()){
                     if (traveled.contains(redefinedProperty)) {
                        hasTraveled = true;
                        break;
                    }
                }
            }
            if (hasTraveled) {
                Application.getInstance().getGUILog().log("Warning: Detected circular reference at " + structuralFeature.getQualifiedName() + ". Stopping recursion.");
                return;
            }
            traveled.add(structuralFeature);
            for (final RedefinableElement re : structuralFeature.getRedefinedElement()) {
                if (re instanceof RedefinableElement) {
                    traveled.add(re);
                }
            }
            final Classifier general = (Classifier) structuralFeature.getType();
            Type special = null;
            if(!isIndividual && getExistingSpecial(structuralFeature)!=null) {
               special = getExistingSpecial(structuralFeature);
            }else{
                special = createSpecializedClassifier(general, parent, structuralFeature);
            }
            if (special == null) {
                return;
            }

            structuralFeature.setType(special);
            if (redefineAttributes) {
                if(special instanceof Classifier) {
                    for (final NamedElement ne : ((Classifier) special).getInheritedMember()) {
                        if (ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf()) {
                            RedefineAttributeAction.redefineAttribute((Classifier) special, (RedefinableElement) ne, true, traveled, isIndividual);
                        }
                    }
                }
            }
        }
    }

    private static Type getExistingSpecial(StructuralFeature structuralFeature) {
        Set<Type> types = new HashSet<Type>();
        Element owner = structuralFeature.getOwner();
        for(RedefinableElement redef : structuralFeature.getRedefinedElement()){
            if(redef instanceof TypedElement){
                types.add(((TypedElement) redef).getType());
                //System.out.println("Found type: "+((TypedElement) redef).getType().getName() +" id  "+ ((TypedElement) redef).getType().toString() + " for SF " + structuralFeature.getName() + "  " + structuralFeature.toString());
            }
        }
        for(Element oe : owner.getOwnedElement()){
            if(oe instanceof Property){
                if(!oe.equals(structuralFeature)){
                for(RedefinableElement redef : ((Property) oe).getRedefinedElement()){
                    if(redef instanceof TypedElement) {
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
        for(NamedElement ne : specific.getOwnedMember()){
            members.add(ne);
        }
         for(NamedElement member : members){
            if(member instanceof RedefinableElement) {
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
