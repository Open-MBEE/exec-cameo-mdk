package gov.nasa.jpl.mbee.mdk.validation.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
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
    private boolean recursion;
    private String name;
    private boolean isIndividual;

    public SetOrCreateRedefinableElementAction(final Classifier clazz, final RedefinableElement re, boolean isIndividual) {
        this(clazz, re, false, DEFAULT_NAME, isIndividual);
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
        this.recursion = recursion;
        this.name = name;
        this.isIndividual = isIndividual;
    }

    public SetOrCreateRedefinableElementAction() {
        super(DEFAULT_NAME);
    }

    public static RedefinableElement redefineRedefinableElement(final Classifier subClassifier, final RedefinableElement re, final boolean createSpecializedType, boolean isIndividual) {
        return redefineRedefinableElement(subClassifier, re, createSpecializedType, new ArrayList<RedefinableElement>(), new ArrayList<Classifier>(), isIndividual);
    }

    public static RedefinableElement redefineRedefinableElement(final Classifier subClassifier, final RedefinableElement elementToBeRedefined, final boolean createSpecializedType, final List<RedefinableElement> traveled, List<Classifier> visited, boolean isIndividual) {
        if (elementToBeRedefined.isLeaf()) {
            Application.getInstance().getGUILog().log(elementToBeRedefined.getQualifiedName() + " is a leaf. Cannot redefine further.");
        }
        if (!subClassifier.isEditable()) {
            Application.getInstance().getGUILog().log(subClassifier.getQualifiedName() + " is not editable. Skipping redefinition.");
            return null;
        }

        RedefinableElement redefinedElement = null;
        for (NamedElement p : subClassifier.getOwnedMember()) {
            if (p instanceof RedefinableElement && ((RedefinableElement) p).getRedefinedElement().contains(elementToBeRedefined)) {
                redefinedElement = (RedefinableElement) p;
                break;
            } else if (p instanceof RedefinableElement && ((RedefinableElement) p).getRedefinedElement().isEmpty()) {
                if (isMatchingStructuralFeature(p, elementToBeRedefined)) {
                    redefinedElement = (RedefinableElement) p;
                    redefinedElement.getRedefinedElement().add(elementToBeRedefined);
                    break;
                } else if (p instanceof Connector && elementToBeRedefined instanceof Connector) {
                    if (((Connector) p).getEnd() != null && ((Connector) elementToBeRedefined).getEnd() != null) {
                        if (((Connector) p).getEnd().get(0).getRole() != null && ((Connector) elementToBeRedefined).getEnd().get(0).getRole() != null) {
                            if (isMatchingStructuralFeature(((Connector) p).getEnd().get(0).getRole(), (((Connector) elementToBeRedefined).getEnd().get(0).getRole()))) {
                                if (((Connector) p).getEnd().size() > 1) {
                                    if (isMatchingStructuralFeature(((Connector) p).getEnd().get(1).getRole(), (((Connector) elementToBeRedefined).getEnd().get(1).getRole()))) {
                                        redefinedElement = (RedefinableElement) p;
                                        redefinedElement.getRedefinedElement().add(elementToBeRedefined);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        Application.getInstance().getGUILog().log("[WARNING] Behavioral Features (Operations and Receptions) are not handled.");
                    }
                }
            }
        }
        if (redefinedElement == null) {
            redefinedElement = (RedefinableElement) CopyPasting.copyPasteElement(elementToBeRedefined, subClassifier, false);
            redefinedElement.getRedefinedElement().removeAll(elementToBeRedefined.getRedefinedElement());
            redefinedElement.getRedefinedElement().add(elementToBeRedefined);
        }

        if (createSpecializedType && redefinedElement instanceof Property && ((TypedElement) redefinedElement).getType() != null) {
            CreateSpecializedTypeAction.createSpecializedType((Property) redefinedElement, subClassifier, true, traveled, visited, isIndividual);
        }
        return redefinedElement;

//        else {
//            Application.getInstance().getGUILog().log(elementToBeRedefined.getQualifiedName() + " has already been redefined in " + subClassifier.getQualifiedName() + ".");
//            return null;
//        }
    }

    private static boolean isMatchingStructuralFeature(NamedElement p, NamedElement elementToBeRedefined) {
        if (p.getName().equals(elementToBeRedefined.getName())) {
            if (p instanceof TypedElement && elementToBeRedefined instanceof TypedElement) {
                if (((TypedElement) p).getType() != null) {
                    if (((TypedElement) p).getType().equals(((TypedElement) elementToBeRedefined).getType())) {
                        return true;
                    }
                } else if (((TypedElement) elementToBeRedefined).getType() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run() {
        redefineRedefinableElement(subClassifier, re, recursion, isIndividual);
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
