package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.*;

public class GenerateViewPresentationAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "GenerateViewPresentation";
    public static final String RECURSE_DEFAULT_ID = "GenerateViewPresentationR";

    private List<ValidationSuite> vss = new ArrayList<>();
    private Set<Element> elements;
    private Project project;
    private boolean recurse;

    public GenerateViewPresentationAction(Set<Element> elements, boolean recurse) {
        super(recurse ? RECURSE_DEFAULT_ID : DEFAULT_ID, "Generate View Contents" + (recurse ? " Recursively" : ""), null, null);
        this.elements = elements;
        this.project = Project.getProject(elements.iterator().next());
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Utils.recommendUpdateFromRemote(Application.getInstance().getProject());
        updateAction();
    }

    public List<ValidationSuite> updateAction() {
        Stereotype viewStereotype = Utils.getViewStereotype(project),
                elementGroupStereotype = Utils.getElementGroupStereotype(project);

        Set<Element> processedElements = new HashSet<>(elements.size());
        Queue<Element> queuedElements = new LinkedList<>(elements);
        Set<Element> views = new LinkedHashSet<>(elements.size());

        while (!queuedElements.isEmpty()) {
            Element element = queuedElements.remove();
            if (processedElements.contains(element)) {
                Application.getInstance().getGUILog().log("Detected duplicate element reference. Skipping generation for " + Converters.getElementToIdConverter().apply(element) + ".");
                continue;
            }
            if (StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype)) {
                views.add(element);
            }
            else if (StereotypesHelper.hasStereotypeOrDerived(element, elementGroupStereotype)) {
                List members = StereotypesHelper.getStereotypePropertyValue(element, elementGroupStereotype, "member", true);
                for (Object o : members) {
                    if (o instanceof Element) {
                        queuedElements.add((Element) o);
                    }
                }
                processedElements.add(element);
            }
        }

        ViewPresentationGenerator vg = new ViewPresentationGenerator(views, project, recurse, null, processedElements);
        ProgressStatusRunner.runWithProgressStatus(vg, "Generating View" + (recurse ? "s" : ""), true, 0);
        vss.addAll(vg.getValidations());
        return vss;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}
