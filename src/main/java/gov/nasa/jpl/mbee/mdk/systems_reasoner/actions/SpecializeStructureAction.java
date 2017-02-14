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
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.Action;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.validation.actions.RedefineAttributeAction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpecializeStructureAction extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Specialize Structure";
    private Classifier classifier;
    private ArrayList<Namespace> recursionList;
    private boolean isValidationMode = false;

    public SpecializeStructureAction(final Classifier classifier, boolean isValidationMode) {
        super(DEFAULT_ID, classifier);
        this.classifier = classifier;
        recursionList = new ArrayList<>();
        this.isValidationMode = isValidationMode;
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
            ArrayList<Element> generals = new ArrayList<>();
            generals.add(classifier);
            getAllSubElementsRecursive(generals, classifier);
            Classifier specific = (Classifier) CopyPasting.copyPasteElement(classifier, container);
          //  System.out.println(specific.getName() );
          //  specific.getGeneral().forEach(item -> System.out.println(item.getName()));
            specific.getOwnedMember().clear();
            Utils.createGeneralization(classifier, specific);
            for (final NamedElement ne : specific.getInheritedMember()) { // Exclude Classifiers for now -> Should Aspect Blocks be Redefined?
                if (ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf() && !(ne instanceof Classifier)) {
                    final RedefinableElement redefEl = (RedefinableElement) ne;
                    if (ne instanceof Property) {
                        if (redefEl instanceof TypedElement) {
                            RedefineAttributeAction action = new RedefineAttributeAction(specific, redefEl, false, null);
                            action.run();
                        }
                    }
                }

            }




            SessionManager.getInstance().closeSession();

            // ValidateAction.validate((Element) specifics);
        }
    }

    private void getAllSubElementsRecursive(ArrayList<Element> copyList, Namespace currentElement) {
        for (NamedElement feat : currentElement.getOwnedMember()) {
            if (feat instanceof Property) {
                Property prop = (Property) feat;
                if (!copyList.contains(prop)) {
                    copyList.add(prop);
                }
                if (prop.getAssociation() != null && !copyList.contains(prop.getAssociation())) {
                    copyList.add(prop.getAssociation());
                }
                if (prop.isComposite() && prop.getType() != null) {
                    Type targ = prop.getType();
                    if (targ instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) {
                        if (!recursionList.contains(targ)) {
                            copyList.add(targ);
                            recursionList.add((Classifier) targ);
                            getAllSubElementsRecursive(copyList, (Classifier) targ);
                        }
                    }
                }
            }
            else if (feat instanceof Namespace) {
                copyList.add(feat);
                for (NamedElement ne : ((Namespace) feat).getOwnedMember()) {
                    if (ne instanceof Namespace) {
                        if (!recursionList.contains(ne)) {
                            copyList.add(ne);
                            recursionList.add((Namespace) ne);
                            getAllSubElementsRecursive(copyList, (Namespace) ne);
                        }
                    }
                }
            }
        }

    }
}
