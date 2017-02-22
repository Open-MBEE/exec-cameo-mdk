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
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.validation.actions.AddInheritanceToAssociationAction;
import gov.nasa.jpl.mbee.mdk.validation.actions.RedefineAttributeAction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SpecializeStructuresAction extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final boolean recursionMode;
    private final boolean individualMode;
    private Classifier classifier;
    private ArrayList<Namespace> recursionList;
    private boolean isValidationMode = false;

    public SpecializeStructuresAction(final Classifier classifier, boolean isValidationMode, String id, boolean isRecursive, boolean isIndividual) {
        super(id, classifier);
        this.classifier = classifier;
        recursionList = new ArrayList<>();
        this.isValidationMode = isValidationMode;
        this.recursionMode = isRecursive;
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

            Classifier specific = (Classifier) CopyPasting.copyPasteElement(classifier, container);

            ArrayList<NamedElement> members = new ArrayList<>();
            for(NamedElement ne : specific.getOwnedMember()){
                members.add(ne);
            }
            // specific.getOwnedMember().clear();
            for(NamedElement member : members){
                if(member instanceof RedefinableElement) {
                    System.out.println(member.getClassType().getName() + " removing " + member.getName());
                    specific.getOwnedMember().remove(member);
                    member.dispose();
                }
            }


            Utils.createGeneralization(classifier, specific);
            for (final NamedElement ne : specific.getInheritedMember()) { // Exclude Classifiers for now -> Should Aspect Blocks be Redefined?
                if (ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf() && !(ne instanceof Classifier)) {
                    final RedefinableElement redefEl = (RedefinableElement) ne;
                                     RedefineAttributeAction action = new RedefineAttributeAction(specific, redefEl, recursionMode, null, individualMode);
                                    action.run();
                    }
            }
            SessionManager.getInstance().closeSession();
            checkAssociationsForInheritance(specific, classifier);
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
