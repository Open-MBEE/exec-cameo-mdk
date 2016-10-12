package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.*;

public class GenerateViewPresentationAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "GenerateViewPresentation";
    public static final String RECURSE_DEFAULT_ID = "GenerateViewPresentationR";

    private List<ValidationSuite> vss = new ArrayList<>();
    private List<Element> elements;
    private boolean recurse;


    public GenerateViewPresentationAction(List<Element> elements, boolean recurse) {
        super(recurse ? RECURSE_DEFAULT_ID : DEFAULT_ID, "Generate View" + (recurse ? "s" : ""), null, null);
        this.elements = elements;
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Utils.recommendUpdateFromTeamwork();
        updateAction();
    }

    public List<ValidationSuite> updateAction() {
        Stereotype viewStereotype = Utils.getViewStereotype(),
                elementGroupStereotype = Utils.getElementGroupStereotype();


        Set<Element> processedElements = new HashSet<>(elements.size());
        Queue<Element> queuedElements = new LinkedList<>(elements);

        while (!queuedElements.isEmpty()) {
            Element element = queuedElements.remove();
            if (processedElements.contains(element)) {
                Application.getInstance().getGUILog().log("Detected duplicate element reference. Skipping generation for " + element.getID() + ".");
                continue;
            }
            if (StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype)) {
                ViewPresentationGenerator vg = new ViewPresentationGenerator(element, recurse, true, null, null, processedElements);
                ProgressStatusRunner.runWithProgressStatus(vg, "Generating View" + (recurse ? "s" : "") + " - " + ((element instanceof NamedElement && ((NamedElement) element).getName() != null) ? ((NamedElement) element).getName() : "<>"), true, 0);
                if (vg.isFailure()) {
                    break;
                }
                vss.addAll(vg.getValidations());
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
        return vss;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}
