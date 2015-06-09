package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;

public class SpecializeAction extends SRAction {
	
	public static final String actionid = "Specialize";
	public List<Classifier> classifiers;
	
	public SpecializeAction(Classifier classifier) {
        this(Utils2.newList(classifier));
	}
	
	public SpecializeAction(List<Classifier> classifiers) {
		super(actionid);
		this.classifiers = classifiers;
	}
	
	@Override
    public void actionPerformed(ActionEvent e) {
		final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
		types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
		//types.add((java.lang.Class<?>) Application.getInstance().getProject().getElementsFactory().getClassClass().getClass());
		
		final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
		final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
		
		final SelectElementTypes set = new SelectElementTypes(null, types, null, null);
		final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
		ElementSelectionDlgFactory.initMultiple(dlg, set, sei, new ArrayList<Object>());
		dlg.setSelectionMode(SelectionMode.MULTIPLE_MODE);
		if (dlg != null) {
			dlg.setVisible(true);
			if (dlg.isOkClicked() && dlg.getSelectedElements() != null && !dlg.getSelectedElements().isEmpty()) {
				SessionManager.getInstance().createSession("creating generalizations");
				for (final BaseElement be : dlg.getSelectedElements()) {
					if (be instanceof Classifier) {
						for (final Classifier specific : classifiers) {
							Utils.createGeneralization((Classifier) be, specific);
						}
					}
				}
				SessionManager.getInstance().closeSession();
				new ValidateAction(classifiers).actionPerformed(null);
			}
		}
	}
}
