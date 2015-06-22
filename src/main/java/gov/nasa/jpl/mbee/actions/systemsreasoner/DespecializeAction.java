package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilter;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilterImpl;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;

public class DespecializeAction extends SRAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String actionid = "Depecialize";
	public List<Classifier> classifiers;
	
	public DespecializeAction(Classifier classifier) {
        this(Utils2.newList(classifier));
	}
	
	public DespecializeAction(List<Classifier> classifiers) {
		super(actionid);
		this.classifiers = classifiers;
	}
	
	@Override
    public void actionPerformed(ActionEvent e) {
		final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
		types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
		
		final Set<Classifier> elements = new HashSet<Classifier>();
		for (final Classifier c : classifiers) {
			elements.addAll(c.getGeneral());
		}
		
		/*if (!classifiers.isEmpty()) {
			final List<Classifier>
			for (final Generalization g1 : classifiers.get(0).getGeneralization()) {
				boolean isCommon = true;
				for (final Classifier c2 : classifiers.subList(1, classifiers.size())) {
					boolean foundTargetMatch = false;
					for (final Generalization g2 : c2.getGeneralization()) {
						if (g2.getTarget().equals(g1.getTarget())) {
							foundTargetMatch = true;
							break;
						}
					}
					if (!foundTargetMatch) {
						isCommon = false;
						break;
					}
				}
				if (isCommon) {
					g1.getTarget()
				}
			}
		}*/
		
		final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
		final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
		final TypeFilter tf = new TypeFilterImpl(types) {
			@Override
			public boolean accept(BaseElement baseElement, boolean checkType) {
				return baseElement != null && super.accept(baseElement, checkType) && elements.contains(baseElement);
			}
		};
		final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
		ElementSelectionDlgFactory.initMultiple(dlg, sei, new TypeFilterImpl(), tf, new ArrayList<Class<?>>(), new ArrayList<Object>());
		Utils.disableSingleSelection(dlg);
		dlg.setSelectionMode(SelectionMode.MULTIPLE_MODE);
		
		if (dlg != null) {
			dlg.setVisible(true);
			if (dlg.isOkClicked() && dlg.getSelectedElements() != null && !dlg.getSelectedElements().isEmpty()) {
				SessionManager.getInstance().createSession("deleting generalizations");
				for (final BaseElement be : dlg.getSelectedElements()) {
					if (be instanceof Classifier) {
						for (final Classifier specific : classifiers) {
							for (final Generalization g : specific.getGeneralization()) {
								if (g.getGeneral().equals(be)) {
									g.refDelete();
									break;
								}
							}
							//specific.getGeneral().remove(be);
							//Utils.createGeneralization((Classifier) be, specific);
						}
					}
				}
				SessionManager.getInstance().closeSession();
				new ValidateAction(classifiers).actionPerformed(null);
			}
		}
	}
}
