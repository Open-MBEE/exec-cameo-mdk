package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateAllViewsAction extends MMSAction {
    public static final String DEFAULT_ID = "GenerateAllViews";

    private List<ValidationSuite> vss = new ArrayList<>();

    public GenerateAllViewsAction() {
        super(DEFAULT_ID, "Generate All Views", null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ae) {
        Project project = Application.getInstance().getProject();
        Utils.recommendUpdateFromRemote(project);
        updateAction(project);
    }

    public List<ValidationSuite> updateAction(Project project) {
        Set<Element> views = getViews(project);
        if (views.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] No views found. Skipping generation.");
            return vss;
        }
        ViewPresentationGenerator vg = new ViewPresentationGenerator(views, project, false);
        ProgressStatusRunner.runWithProgressStatus(vg, "Generating All Views", true, 0);
        vss.addAll(vg.getValidations());
        return vss;
    }

    private Set<Element> getViews(Project project) {
        Stereotype viewStereotype = Utils.getViewStereotype(project);
        if (viewStereotype == null) {
            return Collections.emptySet();
        }
        return StereotypesHelper.getExtendedElementsIncludingDerived(viewStereotype).stream().filter(view -> !ProjectUtilities.isElementInAttachedProject(view)).collect(Collectors.toSet());
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}
