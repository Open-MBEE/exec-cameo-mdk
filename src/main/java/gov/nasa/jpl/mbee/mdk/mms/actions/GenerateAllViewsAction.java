package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateAllViewsAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "GenerateAllDocs";

    private List<ValidationSuite> vss = new ArrayList<ValidationSuite>();

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
        Set<Element> docs = getProjectDocuments(project);
        ViewPresentationGenerator vg = new ViewPresentationGenerator(docs, project, false);
        ProgressStatusRunner.runWithProgressStatus(vg, "Generating All Views", true, 0);
        vss.addAll(vg.getValidations());
        return vss;
    }

    private Set<Element> getProjectDocuments(Project project) {
        Stereotype documentView = Utils.getViewClassStereotype(project);
        Set<Element> projectViews = new HashSet<>();
        for (InstanceSpecification is : documentView.get_instanceSpecificationOfClassifier()) {
            Element owner = is.getOwner();
            if (!ProjectUtilities.isElementInAttachedProject(owner) && StereotypesHelper.hasStereotypeOrDerived(owner, documentView) && owner instanceof Class) {
                projectViews.add(owner);
            }
        }
        if (projectViews.isEmpty()) {
            Application.getInstance().getGUILog().log("No documents or views found in this project");
        }
        return projectViews;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}