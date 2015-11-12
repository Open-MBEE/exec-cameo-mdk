package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.CreateSpecializedTypeAction;

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
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class AspectAction extends SRAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String actionid = "Add Aspect";
	public List<Classifier> classifiers;
	
	public AspectAction(Classifier classifier) {
        this(Utils2.newList(classifier));
	}
	
	public AspectAction(List<Classifier> classifiers) {
		super(actionid);
		this.classifiers = classifiers;
	}
	
	@Override
    public void actionPerformed(ActionEvent e) {
		final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
		types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
		
		final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
		final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
		
		final SelectElementTypes set = new SelectElementTypes(types, types, null, null);
		final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
		ElementSelectionDlgFactory.initMultiple(dlg, set, sei, new ArrayList<Object>());
		dlg.setSelectionMode(SelectionMode.MULTIPLE_MODE);
		if (dlg != null) {
			dlg.setVisible(true);
			if (dlg.isOkClicked() && dlg.getSelectedElements() != null && !dlg.getSelectedElements().isEmpty()) {
				final List<Classifier> specials = new ArrayList<Classifier>();
				
				SessionManager.getInstance().createSession("creating association");
				for (final BaseElement be : dlg.getSelectedElements()) {
					if (be instanceof Classifier) {
						final Classifier aspect = (Classifier) be;
						for (final Classifier aspected : classifiers) {
							final Classifier special = CreateSpecializedTypeAction.createSpecializedClassifier(aspect, aspected, null);
							if (special == null) {
								Application.getInstance().getGUILog().log("Failed to specialize " + aspect.getQualifiedName() + ". Skipping aspecting.");
								continue;
							}
							specials.add(special);
							final Association association = Application.getInstance().getProject().getElementsFactory().createAssociationInstance();
							ModelHelper.setClientElement(association, aspected);
							ModelHelper.setSupplierElement(association, special);
							ModelHelper.setNavigable(ModelHelper.getFirstMemberEnd(association), true);
							ModelHelper.setNavigable(ModelHelper.getSecondMemberEnd(association), false);
							association.setOwner(aspected);
							
							/*final Property p1 = Application.getInstance().getProject().getElementsFactory().createPropertyInstance();
							p1.setAssociation(association);
							p1.setType(aspected);
							p1.setClassifier(aspected);
							ModelHelper.setClientElement(association, p1);
							
							final Property p2 = Application.getInstance().getProject().getElementsFactory().createPropertyInstance();
							p2.setAssociation(association);
							p2.setType(special);
							p2.setClassifier(association);
							ModelHelper.setNavigable(p2, false);
							ModelHelper.setClientElement(association, p2);*/
						}
					}
				}
				SessionManager.getInstance().closeSession();
				ValidateAction.validate(specials);
			}
		}
	}
}
