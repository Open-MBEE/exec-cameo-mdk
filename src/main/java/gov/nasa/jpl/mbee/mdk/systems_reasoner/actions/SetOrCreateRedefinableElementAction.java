package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.validation.SRValidationSuite;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

import java.util.ArrayList;
import java.util.List;

public class SetOrCreateRedefinableElementAction extends GenericRuleViolationAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NAME = "Redefine Attribute";

    private Classifier subClassifier;
    private RedefinableElement re;
    private String name;
    private boolean isIndividual;
    private boolean isRecursive;

    public SetOrCreateRedefinableElementAction(final Classifier targetForRedefEl, final RedefinableElement elementToBeRedefined, boolean isIndividual) {
        this(targetForRedefEl, elementToBeRedefined, false, DEFAULT_NAME, isIndividual);
    }

    /**
     * @param targetForRedefEl
     * @param elementToBeRedefined
     * @param recursion
     * @param name
     * @param isIndividual
     */
    public SetOrCreateRedefinableElementAction(final Classifier targetForRedefEl, final RedefinableElement elementToBeRedefined, final boolean recursion, final String name, boolean isIndividual) {
        super(name);
        this.subClassifier = targetForRedefEl;
        this.re = elementToBeRedefined;
        this.isRecursive = recursion;
        this.name = name;
        this.isIndividual = isIndividual;
    }

    public static RedefinableElement redefineRedefinableElement(final Classifier subClassifier, final RedefinableElement re, boolean isIndividual, boolean isRecursive) {
        return redefineRedefinableElement(subClassifier, re, new ArrayList<RedefinableElement>(), new ArrayList<Classifier>(), isIndividual, isRecursive);
    }

    public static RedefinableElement redefineRedefinableElement(final Classifier subClassifier, final RedefinableElement elementToBeRedefined, final List<RedefinableElement> traveled, List<Classifier> visited, boolean isIndividual, boolean isRecursive) {
        if (isNotRedefinable(subClassifier, elementToBeRedefined)) {
            return null;
        }


        RedefinableElement redefinedElement = findExistingRedefiningElement(subClassifier, elementToBeRedefined);

        if (redefinedElement == null) {
            redefinedElement = (RedefinableElement) CopyPasting.copyPasteElement(elementToBeRedefined, subClassifier, false);
            if (redefinedElement == null) {
                return null;
            }
            redefinedElement.getRedefinedElement().removeAll(elementToBeRedefined.getRedefinedElement());
            redefinedElement.getRedefinedElement().add(elementToBeRedefined);
        }

        if (elementToBeRedefined instanceof Property) {
            if (((Property) elementToBeRedefined).getAssociation() != null) {
                if(!existingAssociationInheritsFromGeneralAssociation(redefinedElement, (Property) elementToBeRedefined)) {
                    createInheritingAssociation((Property) elementToBeRedefined, subClassifier, (Property) redefinedElement);
                }
            }
        }
        if (isRecursive && redefinedElement instanceof Property && ((TypedElement) redefinedElement).getType() != null) {
            CreateSpecializedTypeAction.createSpecializedType((Property) redefinedElement, subClassifier, traveled, visited, isIndividual, isRecursive);
        }
        return redefinedElement;
    }

    private static boolean existingAssociationInheritsFromGeneralAssociation(RedefinableElement redefinedElement, Property elementToBeRedefined) {
        if(redefinedElement instanceof Property) {
            Association association = ((Property) redefinedElement).getAssociation();
            Association general = elementToBeRedefined.getAssociation();
            if(association != null) {
                return eventuallyInherits(association, general);
            }else{
                return false;
            }

        }
        return false;
    }

    private static boolean eventuallyInherits(Classifier association, Classifier general) {
        if(association.getGeneral().contains(general)){
            return true;
        }else{
            for (Classifier specific : association.getGeneral()) {
                if(eventuallyInherits(specific, general)){
                    return true;
                }
            }
            return false;
        }
    }

    private static RedefinableElement findExistingRedefiningElement(Classifier subClassifier, RedefinableElement elementToBeRedefined) {
        RedefinableElement redefinedElement = null;
        for (NamedElement p : subClassifier.getOwnedMember()) {
            if (p instanceof RedefinableElement && SRValidationSuite.doesEventuallyRedefine((RedefinableElement) p, elementToBeRedefined)) {
                redefinedElement = (RedefinableElement) p;
                break;
            }
            else if (p instanceof RedefinableElement) {// && ((RedefinableElement) p).getRedefinedElement().isEmpty()) {
                if (isMatchingTypedElement(p, elementToBeRedefined)) {
                    redefinedElement = (RedefinableElement) p;
                    redefinedElement.getRedefinedElement().add(elementToBeRedefined);
                    break;
                }
                else if (p instanceof Connector && elementToBeRedefined instanceof Connector) {
                    if (((Connector) p).getEnd() != null && ((Connector) elementToBeRedefined).getEnd() != null) {
                        if (((Connector) p).getEnd().get(0).getRole() != null && ((Connector) elementToBeRedefined).getEnd().get(0).getRole() != null) {
                            if (isMatchingTypedElement(((Connector) p).getEnd().get(0).getRole(), (((Connector) elementToBeRedefined).getEnd().get(0).getRole()))) {
                                if (((Connector) p).getEnd().size() > 1) {
                                    if (isMatchingTypedElement(((Connector) p).getEnd().get(1).getRole(), (((Connector) elementToBeRedefined).getEnd().get(1).getRole()))) {
                                        redefinedElement = (RedefinableElement) p;
                                        redefinedElement.getRedefinedElement().add(elementToBeRedefined);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    else {
                        Application.getInstance().getGUILog().log("[WARNING] Behavioral Features (Operations and Receptions) are not handled.");
                    }
                }
            }
        }
        return redefinedElement;
    }

    private static boolean isNotRedefinable(Classifier subClassifier, RedefinableElement elementToBeRedefined) {
        if (elementToBeRedefined.isLeaf()) {
            Application.getInstance().getGUILog().log(elementToBeRedefined.getQualifiedName() + " is a leaf. Cannot redefine further.");
            return true;
        }
        if (!subClassifier.isEditable()) {
            Application.getInstance().getGUILog().log(subClassifier.getQualifiedName() + " is not editable. Skipping redefinition.");
            return true;
        }
        return false;
    }

    public static void createInheritingAssociation(Property generalProperty, Classifier classifierOfnewProperty, Property newProperty) {
        Association generalAssociation = generalProperty.getAssociation();
        Association newAssociation = UMLFactory.eINSTANCE.createAssociation();
        newAssociation.setName(generalAssociation.getName());
        Property ownedEnd = UMLFactory.eINSTANCE.createProperty();
        ownedEnd.setOwner(newAssociation);
        ownedEnd.setType(classifierOfnewProperty);
        Utils.createGeneralization(generalAssociation, newAssociation);
        if (classifierOfnewProperty.getOwner() != null) {
            newAssociation.setOwner(classifierOfnewProperty.getOwner());
        }
        else {
            throw new NullPointerException("owner of classifier null!");
        }
        newAssociation.getMemberEnd().add(newProperty);
        newAssociation.getOwnedEnd().add(ownedEnd);
    }

    private static boolean isMatchingTypedElement(NamedElement p, NamedElement elementToBeRedefined) {
        if (p.getName().equals(elementToBeRedefined.getName())) {
            if (p instanceof TypedElement && elementToBeRedefined instanceof TypedElement) {
                if (((TypedElement) p).getType() != null) {
                    if (((TypedElement) p).getType().equals(((TypedElement) elementToBeRedefined).getType())) {
                        return true;
                    }
                }
                else if (((TypedElement) elementToBeRedefined).getType() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run() {
        redefineRedefinableElement(subClassifier, re, isIndividual, isRecursive);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSessionName() {
        return "redefine attribute";
    }
}
