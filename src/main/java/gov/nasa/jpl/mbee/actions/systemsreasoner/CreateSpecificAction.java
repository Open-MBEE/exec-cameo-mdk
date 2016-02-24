package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

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
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Feature;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Namespace;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;

public class CreateSpecificAction extends SRAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String actionid = "Create Block Specific Types";
	private Classifier classifier;
	private ArrayList<Classifier> recursionList;

	public CreateSpecificAction(final Classifier classifier) {
		super(actionid, classifier);
		this.classifier = classifier;
		recursionList = new ArrayList<Classifier>();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
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
		if (dlg != null) {
			dlg.setVisible(true);
			if (dlg.isOkClicked() && dlg.getSelectedElement() != null && dlg.getSelectedElement() instanceof Namespace) {
				SessionManager.getInstance().createSession("create specific");
				Namespace container = (Namespace) dlg.getSelectedElement();
				ArrayList<Element> copyList = new ArrayList<Element>();
				copyList.add(classifier);
				getAllSubelementsRecursive(copyList, classifier);

				// List<BaseElement> specifics = CopyPasting.copyPasteElements(copyList, container);
				List<BaseElement> specifics = CopyPasting.copyPasteElements(copyList, container, null, true, true);
				System.out.println("Copy List  _  _  _ |   specifics");
				for (int jj = 0; jj < copyList.size(); jj++) {
					System.out.println(copyList.get(jj) + "_  _  _ |" + specifics.get(jj));
				}

				int i = 0;
				for (BaseElement specific : specifics) {
					if (specific instanceof Classifier) {
						for (Generalization generalization : new ArrayList<Generalization>(((Classifier)specific).getGeneralization())) {
							generalization.dispose();
						}
						for (NamedElement ne : new ArrayList<NamedElement>((((Namespace) specific).getOwnedMember()))) {
							if (ne instanceof RedefinableElement) {
								// Dont throw away those we want to redefine.
							} else {
								ne.dispose();
							}
						}
						Utils.createGeneralization((Classifier) copyList.get(i), (Classifier) specific);
					} else if (specific instanceof RedefinableElement) {
						if (specific instanceof Property) {
							if (copyList.get(i) instanceof Property) {
								((Property) specific).getRedefinedProperty().add((Property) copyList.get(i));
							}
						}

					}

					i++;
				}

				SessionManager.getInstance().closeSession();
				// ValidateAction.validate((Element) specifics);
			}
		}
	}
	private void getAllSubelementsRecursive(ArrayList<Element> copyList, Classifier currentElement) {
		for (Feature feat : currentElement.getFeature()) {
			if (feat instanceof Property) {
				Property prop = (Property) feat;
				if (!copyList.contains(prop)) {
					copyList.add(prop);
				}
				if (prop.getAssociation() != null && !copyList.contains(prop.getAssociation())) {
					copyList.add(prop.getAssociation());
				}
				if (prop.isComposite()) { // only composition relations.
					if (prop.getType() != null) {
						Type targ = prop.getType();
						if (targ instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) {
							if (!recursionList.contains(targ)) {
								copyList.add(targ);
								recursionList.add((Classifier) targ);
								getAllSubelementsRecursive(copyList, (Classifier) targ);
							}
						}
					}
				}
			}
		}
	}
}
