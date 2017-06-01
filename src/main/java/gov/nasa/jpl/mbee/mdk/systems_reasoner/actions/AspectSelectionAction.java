package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.validation.actions.AspectRemedyAction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class AspectSelectionAction extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Aspects";
    private Classifier classToAddAspect = null;
    public List<Classifier> classifiers;
    private static Generalization createdGeneralization = null;

    public AspectSelectionAction(Classifier classifier) {
        super(DEFAULT_ID);
        this.classToAddAspect = classifier;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);

        final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
        final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
        SessionManager.getInstance().createSession(Application.getInstance().getProject(), "Creating aspect.");
        final SelectElementTypes set = new SelectElementTypes(types, types, null, null);
        final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);

        ArrayList<Classifier> aspects = new ArrayList<>();
        findAspectsOfClassifier(aspects);
        ElementSelectionDlgFactory.initMultiple(dlg, set, sei, aspects);

        dlg.show();

        // Check if the user has clicked "Ok".
        if (dlg.isOkClicked()) {
            List<BaseElement> selected = dlg.getSelectedElements();
            Element aspectStereotype = Converters.getIdToElementConverter().apply("_18_0_2_407019f_1449688347122_736579_14412", Application.getInstance().getProject());
            for (BaseElement be : selected) {
                System.out.println(be.getHumanName());
                if (aspectStereotype != null && aspectStereotype instanceof Stereotype) {
                    Utils.createDependencyWithStereotype(classToAddAspect, (Element) be, (Stereotype) aspectStereotype);
                    AspectRemedyAction ara = new AspectRemedyAction(classToAddAspect, (Classifier) be);
                    ara.run();
                }
            }

        }


        SessionManager.getInstance().closeSession(Application.getInstance().getProject());
    }

    public boolean findAspectsOfClassifier(ArrayList<Classifier> aspects) {
        boolean aspectDefinitionFound = false;

        for (Dependency d : classToAddAspect.getClientDependency()) {
            boolean aspectFound = false;
            Classifier aspect = null;
            Stereotype s = StereotypesHelper.getAppliedStereotypeByString(d, "aspect");
            if (s != null) {
                aspectDefinitionFound = true;
                for (Element el : d.getTarget()) {
                    if (el instanceof Classifier) {
                        aspect = (Classifier) el;
                        for (Element ownedElement : classToAddAspect.getOwnedElement()) {
                            if (ownedElement instanceof Property) {
                                Type type = ((TypedElement) ownedElement).getType();
                                if (type instanceof Classifier) {
                                    if ((hasInheritanceFromTo((Classifier) type, aspect))) {
                                        aspects.add((Classifier) type);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return aspectDefinitionFound;
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
