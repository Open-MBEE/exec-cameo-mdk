package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;

import java.util.ArrayList;
import java.util.List;

public class RedefineAttributeAction extends GenericRuleViolationAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NAME = "Redefine Attribute";

    private Classifier subClassifier;
    private RedefinableElement re;
    private boolean createSpecializedType;
    private String name;

    public RedefineAttributeAction(final Classifier clazz, final RedefinableElement re) {
        this(clazz, re, false, DEFAULT_NAME);
    }

    /**
     * @param targetForRedefEl
     * @param elementToBeRedefined
     * @param createSpecializedType
     * @param name
     */
    public RedefineAttributeAction(final Classifier targetForRedefEl, final RedefinableElement elementToBeRedefined, final boolean createSpecializedType, final String name) {
        super(name);
        this.subClassifier = targetForRedefEl;
        this.re = elementToBeRedefined;
        this.createSpecializedType = createSpecializedType;
        this.name = name;
    }

    public static RedefinableElement redefineAttribute(final Classifier subClassifier, final RedefinableElement re, final boolean createSpecializedType) {
        return redefineAttribute(subClassifier, re, createSpecializedType, new ArrayList<Property>());
    }

    public static RedefinableElement redefineAttribute(final Classifier subClassifier, final RedefinableElement elementToBeRedefined, final boolean createSpecializedType, final List<Property> traveled) {
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
            }
        }
        if (redefinedElement == null) {
            redefinedElement = (RedefinableElement) CopyPasting.copyPasteElement(elementToBeRedefined, subClassifier, false);
//			if (redefinedElement instanceof Namespace) {
//				Collection<?> emptyCollection = new ArrayList<String>();
//				((Namespace) redefinedElement).getOwnedMember().retainAll(emptyCollection); 
//			}
            redefinedElement.getRedefinedElement().add(elementToBeRedefined);
            if (createSpecializedType && redefinedElement instanceof Property && redefinedElement instanceof TypedElement && ((TypedElement) redefinedElement).getType() != null) {
                CreateSpecializedTypeAction.createSpecializedType((Property) redefinedElement, subClassifier, true, traveled);
            }
            return redefinedElement;
        }
        else {
            Application.getInstance().getGUILog().log(elementToBeRedefined.getQualifiedName() + " has already been redefined in " + subClassifier.getQualifiedName() + ".");
            return null;
        }
    }

    @Override
    public void run() {
        redefineAttribute(subClassifier, re, createSpecializedType);
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
