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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class SetOrCreateRedefinableElementAction extends GenericRuleViolationAction {
    private static final String DEFAULT_NAME = "Redefine Attribute";

    private Classifier subClassifier;
    private RedefinableElement re;
    private String name;
    private boolean isIndividual;
    private boolean isRecursive;

    public SetOrCreateRedefinableElementAction(final Classifier targetForRedefEl, final RedefinableElement elementToBeRedefined, boolean isIndividual) {
        this(targetForRedefEl, elementToBeRedefined, false, DEFAULT_NAME, isIndividual);
    }

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
        }
        redefinedElement.getRedefinedElement().removeAll(getRedefinedElementsRecursively(elementToBeRedefined, new HashSet<>()));
        redefinedElement.getRedefinedElement().add(elementToBeRedefined);

        if (elementToBeRedefined instanceof Property) {
            if (((Property) elementToBeRedefined).getAssociation() != null) {
                if (!existingAssociationInheritsFromGeneralAssociation(redefinedElement, (Property) elementToBeRedefined)) {
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
        if (redefinedElement instanceof Property) {
            Association association = ((Property) redefinedElement).getAssociation();
            Association general = elementToBeRedefined.getAssociation();
            if (association != null) {
                return eventuallyInherits(association, general);
            }
            else {
                return false;
            }

        }
        return false;
    }

    private static boolean eventuallyInherits(Classifier association, Classifier general) {
        if (association.getGeneral().contains(general)) {
            return true;
        }
        else {
            for (Classifier specific : association.getGeneral()) {
                if (eventuallyInherits(specific, general)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static RedefinableElement findExistingRedefiningElement(Classifier subClassifier, RedefinableElement elementToBeRedefined) {
        RedefinableElement existingRedefiningElement = null;
        for (NamedElement p : subClassifier.getOwnedMember()) {
            if (p instanceof RedefinableElement && SRValidationSuite.doesEventuallyRedefine((RedefinableElement) p, elementToBeRedefined)) {
                existingRedefiningElement = (RedefinableElement) p;
                break;
            }
            else if (p instanceof RedefinableElement) {
                if (isMatchingTypedElement((RedefinableElement) p, elementToBeRedefined)) {
                    existingRedefiningElement = (RedefinableElement) p;
                    break;
                }
                else if (p instanceof Connector && elementToBeRedefined instanceof Connector) {
                    Connector c1 = (Connector) p;
                    Connector c2 = (Connector) elementToBeRedefined;
                    if (c1.getEnd() == null || c2.getEnd() == null) {
                        continue;
                    }
                    if (c1.getEnd().size() != 2 || c2.getEnd().size() != 2) {
                        continue;
                    }
                    if (c1.getEnd().stream().anyMatch(e -> !(e.getRole() instanceof RedefinableElement)) || c2.getEnd().stream().anyMatch(e -> !(e.getRole() instanceof RedefinableElement))) {
                        continue;
                    }
                    if (IntStream.range(0, 2).anyMatch(i -> !isMatchingTypedElement((RedefinableElement) c1.getEnd().get(i).getRole(), (RedefinableElement) c2.getEnd().get(i).getRole()))) {
                        continue;
                    }
                    existingRedefiningElement = (RedefinableElement) p;
                    break;
                }
            }
        }
        return existingRedefiningElement;
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

    static void createInheritingAssociation(Property generalProperty, Classifier classifierOfnewProperty, Property newProperty) {
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

    private static Set<RedefinableElement> getRedefinedElementsRecursively(RedefinableElement redefinableElement, Set<RedefinableElement> set) {
        if (set.add(redefinableElement)) {
            redefinableElement.getRedefinedElement().forEach(redefinedElement -> getRedefinedElementsRecursively(redefinedElement, set));
        }
        return set;
    }

    private static boolean isMatchingTypedElement(RedefinableElement p, RedefinableElement elementToBeRedefined) {
        Set<RedefinableElement> flattenedRedefinedElements = getRedefinedElementsRecursively(elementToBeRedefined, new HashSet<>());
        if (p.getRedefinedElement().stream().anyMatch(flattenedRedefinedElements::contains)) {
            return true;
        }
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
