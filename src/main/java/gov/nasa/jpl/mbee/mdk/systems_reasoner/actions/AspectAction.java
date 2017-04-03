package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Utils2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class AspectAction extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Add Aspect";
    private List<Classifier> classifiers;
    private Project project;
    private static Generalization createdGeneralization;

    public AspectAction(Classifier classifier) {
        this(Utils2.newList(classifier));
    }

    public AspectAction(List<Classifier> classifiers) {
        super(DEFAULT_ID);
        this.classifiers = classifiers;
        this.project = Project.getProject(classifiers.iterator().next());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);

        final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
        final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
        SessionManager.getInstance().createSession("Creating aspect.");
        final SelectElementTypes set = new SelectElementTypes(types, types, null, null);
        final SelectElementInfo sei = new SelectElementInfo(true, false, project.getPrimaryModel().getOwner(), true);
        boolean aspectDefinitionFound = false;
        for (Classifier aspected : classifiers) {
            for (Dependency d : aspected.getClientDependency()) {
                boolean aspectFound = false;
                Classifier aspect = null;
                Stereotype s = StereotypesHelper.getAppliedStereotypeByString(d, "aspect");
                if (s != null) {
                    aspectDefinitionFound = true;
                    for (Element el : d.getTarget()) {
                        if (el instanceof Classifier) {
                            aspect = (Classifier) el;
                            for (Element ownedElement : aspected.getOwnedElement()) {
                                if (ownedElement instanceof Property) {
                                    Type type = ((TypedElement) ownedElement).getType();
                                    if (type instanceof Classifier) {
                                        if ((hasInheritanceFromTo((Classifier) type, aspect))) {
                                            aspectFound = true;
                                        }
                                    }
                                }
                                else if (ownedElement instanceof CallBehaviorAction) {
                                    Behavior b = ((CallBehaviorAction) ownedElement).getBehavior();
                                    if (b.getGeneral().contains(el)) {
                                        aspectFound = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!aspectFound) {
                        AspectRemedyAction ara = new AspectRemedyAction(aspected, aspect);
                        ara.run();
                    }
                }
            }
        }

        if (!aspectDefinitionFound) {
            ElementSelectionDlgFactory.initMultiple(dlg, set, sei, new ArrayList<Object>());
            dlg.setSelectionMode(SelectionMode.MULTIPLE_MODE);
            if (dlg != null) {
                dlg.setVisible(true);
                if (dlg.isOkClicked() && dlg.getSelectedElements() != null && !dlg.getSelectedElements().isEmpty()) {
                    final List<Classifier> aspectedClasses = new ArrayList<>();

                    for (final BaseElement be : dlg.getSelectedElements()) {
                        if (be instanceof Classifier) {
                            final Classifier aspect = (Classifier) be;
                            for (final Classifier aspected : classifiers) {
                                Stereotype aspectSt = Utils.getStereotype(project, "aspect");
                                Utils.createDependencyWithStereotype(aspected, aspect, aspectSt);
                                aspectedClasses.add(aspected);
                                AspectRemedyAction ara = new AspectRemedyAction(aspected, aspect);
                                ara.run();
                            }
                        }
                    }

                    ValidateAction.validate(aspectedClasses);
                }
            }
        }
        SessionManager.getInstance().closeSession();
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
