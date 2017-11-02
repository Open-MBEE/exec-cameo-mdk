package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.google.common.collect.Lists;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.api.SRConstants;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AspectSelectionAction extends SRAction {
    public static final String DEFAULT_ID = "Add & Realize Aspect(s)";

    private final Classifier classifier;

    public AspectSelectionAction(Classifier classifier) {
        super(DEFAULT_ID);
        this.classifier = classifier;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = Application.getInstance().getProject();
        if (project == null) {
            return;
        }
        Stereotype aspectStereotype = (Stereotype) Converters.getIdToElementConverter().apply(SRConstants.ASPECT_STEREOTYPE_ID, project);
        if (aspectStereotype == null) {
            Application.getInstance().getGUILog().log("[ERROR] Aspect stereotype not found. Please add SysML Extensions as a used project. Aborting aspect creation.");
            return;
        }
        List<java.lang.Class<?>> types = Lists.newArrayList(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);

        Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
        ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
        SelectElementTypes set = new SelectElementTypes(types, types, null, null);
        SelectElementInfo sei = new SelectElementInfo(true, false, project.getPrimaryModel(), true);

        ElementSelectionDlgFactory.initMultiple(dlg, set, sei, Collections.emptyList());
        dlg.show();
        if (!dlg.isOkClicked()) {
            return;
        }

        SessionManager.getInstance().createSession(project, "Creating aspect");
        Set<Classifier> initialAspects = classifier.getClientDependency().stream().filter(dependency -> StereotypesHelper.hasStereotypeOrDerived(dependency, aspectStereotype))
                .map(DirectedRelationship::getTarget).filter(targets -> targets.size() == 1).map(targets -> targets.iterator().next()).filter(target -> target instanceof Classifier).map(target -> (Classifier) target).collect(Collectors.toSet());
        Set<Classifier> finalAspects = dlg.getSelectedElements().stream().filter(element -> element instanceof Classifier).map(element -> (Classifier) element).collect(Collectors.toSet());
        Set<Classifier> addedAspects = new HashSet<>(finalAspects);
        addedAspects.removeAll(initialAspects);

        addedAspects.forEach(aspect -> {
            Utils.createDependencyWithStereotype(classifier, aspect, aspectStereotype);
            new AspectRemedyAction(classifier, aspect).run();
        });
        SessionManager.getInstance().closeSession(project);
    }
}
