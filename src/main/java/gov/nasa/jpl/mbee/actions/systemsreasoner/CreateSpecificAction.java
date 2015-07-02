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
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Namespace;

public class CreateSpecificAction extends SRAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String actionid = "Create Specific";
	private Classifier classifier;
	
	public CreateSpecificAction(final Classifier classifier) {
        super(actionid, classifier);
        this.classifier = classifier;
	}
	
	@Override
    public void actionPerformed(ActionEvent e) {
		final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
		types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
		types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package.class);
		types.add(Model.class);

		final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
		final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
		
		final SelectElementTypes set = new SelectElementTypes(null, types, null, null);
		final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
		ElementSelectionDlgFactory.initSingle(dlg, set, sei, classifier.getOwner());
		dlg.setSelectionMode(SelectionMode.SINGLE_MODE);
		if (dlg != null) {
			dlg.setVisible(true);
			if (dlg.isOkClicked() && dlg.getSelectedElement() != null && dlg.getSelectedElement() instanceof Namespace) {
				SessionManager.getInstance().createSession("create specific");
				final Classifier instance  = (Classifier) CopyPasting.copyPasteElement(classifier, (Namespace) dlg.getSelectedElement(), true);
				instance.getOwnedMember().clear();
				Utils.createGeneralization(classifier, instance);
				SessionManager.getInstance().closeSession();
				
				// for some reason only half the members were showing up as members...
				// however spec view is just fine
				// that being said with the new name collision code there is no need to delete all members in case the user wants
				// to keep some stuff in there (comments, notes, etc.) for all specialized blocks
				// user can just use name collision to rejoin
				
				/*SessionManager.getInstance().createSession("deleting members");
				if (instance instanceof Namespace) {
					for (final NamedElement ne : ((Namespace) instance).getOwnedMember()) {
						Application.getInstance().getGUILog().log(ne.getQualifiedName());
						ne.refDelete();
					}
				}
				SessionManager.getInstance().closeSession();*/
				
				new ValidateAction(instance).actionPerformed(null);
			}
		}
	}
	
}
