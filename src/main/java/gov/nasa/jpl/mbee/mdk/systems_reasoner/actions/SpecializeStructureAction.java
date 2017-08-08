package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.Class;
import java.util.ArrayList;
import java.util.List;

public class SpecializeStructureAction extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final List<Class<? extends Classifier>> UNSPECIALIZABLE_CLASSIFIERS = new ArrayList<Class<? extends Classifier>>();

    static {
        UNSPECIALIZABLE_CLASSIFIERS.add(DataType.class);
        UNSPECIALIZABLE_CLASSIFIERS.add(PrimitiveType.class);
    }

    private final boolean isRecursive;
    private final boolean individualMode;
    private Classifier classifier;
    private ArrayList<Namespace> recursionList;
    private boolean isValidationMode = false;

    public SpecializeStructureAction(final Classifier classifier, boolean isValidationMode, String id, boolean isRecursive, boolean isIndividual) {
        super(id, classifier);
        this.classifier = classifier;
        recursionList = new ArrayList<>();
        this.isValidationMode = isValidationMode;
        this.isRecursive = isRecursive;
        this.individualMode = isIndividual;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        final List<java.lang.Class<?>> types = new ArrayList<>();
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package.class);
        types.add(Model.class);

        final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
        final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
        dlg.setTitle("Select container for generated elements:");
        final SelectElementTypes set = new SelectElementTypes(null, types, null, null);
        final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
        ElementSelectionDlgFactory.initSingle(dlg, set, sei, classifier.getOwner());


        dlg.setSelectionMode(SelectionMode.SINGLE_MODE);
        if (!isValidationMode) {
            dlg.setVisible(true);
        }
        if (isValidationMode || dlg.isOkClicked() && dlg.getSelectedElement() != null && dlg.getSelectedElement() instanceof Namespace) {
            SessionManager.getInstance().createSession("Create BST");
            Namespace container;
            if (isValidationMode) {
                container = (Namespace) classifier.getOwner();
            }
            else {
                container = (Namespace) dlg.getSelectedElement();
            }

            Classifier specific = createSpecialClassifier(container, new ArrayList<RedefinableElement>(), new ArrayList<Classifier>());
            SessionManager.getInstance().closeSession();

            checkAssociationsForInheritance(specific, classifier);
        }
    }

    public Classifier createSpecialClassifier(Namespace container, List<RedefinableElement> traveled, List<Classifier> visited) {

        for (final Class<? extends Classifier> c : UNSPECIALIZABLE_CLASSIFIERS) {
            if (c.isAssignableFrom(classifier.getClass())) {
//                Application.getInstance().getGUILog()
//                        .log("[WARNING] " + (structuralFeature != null ? structuralFeature.getQualifiedName() : "< >") + " is a " + c.getSimpleName() + ", which is not specializable.");
                return null;
            }
        }

        Classifier specific = (Classifier) CopyPasting.copyPasteElement(classifier, container, true);
        if (specific == null) {
            Application.getInstance().getGUILog().log("[ERROR] Failed to create specialized classifier for " + Converters.getElementToHumanNameConverter().apply(classifier) + " in " + Converters.getElementToHumanNameConverter().apply(container) + ". Aborting specialization.");
            return null;
        }
        visited.add(specific);
        visited.add(classifier);
        specific.getGeneralization().clear();
        ArrayList<RedefinableElement> redefinedElements = new ArrayList<RedefinableElement>();
        for (NamedElement namedElement : specific.getOwnedMember()) {
            if (namedElement instanceof RedefinableElement && !((RedefinableElement) namedElement).isLeaf() && !(namedElement instanceof Classifier)) {
                redefinedElements.add((RedefinableElement) namedElement);
                ((RedefinableElement) namedElement).getRedefinedElement().clear();
            }
        }
        Utils.createGeneralization(classifier, specific);


        for (final NamedElement ne : specific.getInheritedMember()) { // Exclude Classifiers for now -> Should Aspect Blocks be Redefined?
            if (ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf() && !(ne instanceof Classifier)) {
                final RedefinableElement elementToBeRedefined = (RedefinableElement) ne;
                SetOrCreateRedefinableElementAction.redefineRedefinableElement(specific, elementToBeRedefined, traveled, visited, individualMode, isRecursive);
                //redefinedElements.add(elementToBeRedefined);
            }
        }
//        for (RedefinableElement redefinedElement : redefinedElements) {
//            redefinedElement.dispose();
//        }
        return specific;
    }

    private void deleteDiagrams(Namespace specific, ArrayList<Diagram> diagrams) {
        for (NamedElement ne : specific.getOwnedMember()) {
            if (ne instanceof Diagram) {
                diagrams.add((Diagram) ne);
            }
            else if (ne instanceof Namespace) {
                for (NamedElement nam : ((Namespace) ne).getOwnedMember()) {
                    deleteDiagrams((Namespace) nam, diagrams);
                }
            }
        }
    }


    private void checkAssociationsForInheritance(Classifier classifier, Classifier general) {
        assocRule:
        for (Element child : classifier.getOwnedElement()) {
            if (child instanceof Property) {
                Type partType = ((Property) child).getType();
                for (Element superChild : general.getOwnedElement()) {
                    if (superChild instanceof Property) {
                        Type superPartType = ((Property) superChild).getType();
                        if (partType != null) {
                            if (partType.equals(superPartType)) {
                                if (hasAnAssociation(superChild)) {
                                    if (hasInheritanceFromTo(((Property) child).getAssociation(), ((Property) superChild).getAssociation())) {
                                        break assocRule;
                                    }
                                    else {
                                        AddInheritanceToAssociationAction action = new AddInheritanceToAssociationAction(((Property) child).getAssociation(), ((Property) superChild).getAssociation());
                                        action.actionPerformed(null);
                                    }
                                }
                            }
                            else if (partType instanceof Classifier) {
                                if (((Classifier) partType).getGeneral().contains(superPartType)) {
                                    if (hasInheritanceFromTo(((Property) child).getAssociation(), ((Property) superChild).getAssociation())) {
                                        break assocRule;
                                    }
                                    else {
                                        AddInheritanceToAssociationAction action = new AddInheritanceToAssociationAction(((Property) child).getAssociation(), ((Property) superChild).getAssociation());
                                        action.actionPerformed(null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean hasAnAssociation(Element superChild) {
        return ((Property) superChild).getAssociation() != null;

    }

    private boolean hasInheritanceFromTo(Classifier classifier, Classifier general) {
        if (classifier != null) {
            return ModelHelper.getGeneralClassifiersRecursivelly(classifier).contains(general);
        }
        else {
            return false;
        }
    }

}
