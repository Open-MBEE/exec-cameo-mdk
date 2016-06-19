package gov.nasa.jpl.mbee.actions.docgen;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.*;

public class GenerateViewPresentationAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "GenerateViewPresentation";
    public static final String recurseActionid = "GenerateViewPresentationR";

    private List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
    private List<Element> elements;
    private boolean recurse;


    public GenerateViewPresentationAction(List<Element> elements, boolean recurse) {
        super(recurse ? recurseActionid : actionid, "Generate View" + (recurse ? "s" : ""), null, null);
        this.elements = elements;
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!Utils.recommendUpdateFromTeamwork()) {
            return;
        }
        updateAction();
    }

    public List<ValidationSuite> updateAction() {
        Stereotype viewStereotype = Utils.getViewStereotype(),
                elementGroupStereotype = Utils.getElementGroupStereotype();


        Set<Element> processedSet = new HashSet<>(elements.size());
        Queue<Element> processQueue = new LinkedList<>(elements);

        while (!processQueue.isEmpty()) {
            Element element = processQueue.remove();
            if (processedSet.contains(element)) {
                Application.getInstance().getGUILog().log("Detected circular reference. Skipping " + element.getID() + ".");
                continue;
            }
            if (StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype)) {
                ViewPresentationGenerator vg = new ViewPresentationGenerator(element, recurse, null, true, null, null);
                ProgressStatusRunner.runWithProgressStatus(vg, "Generating View" + (recurse ? "s" : "") + " - " + ((element instanceof NamedElement && ((NamedElement) element).getName() != null) ? ((NamedElement) element).getName() : "<>"), true, 0);
                if (vg.isFailure()) {
                    break;
                }
                vss.addAll(vg.getValidations());
            } else if (StereotypesHelper.hasStereotypeOrDerived(element, elementGroupStereotype)) {
                List members = StereotypesHelper.getStereotypePropertyValue(element, elementGroupStereotype, "member", true);
                for (Object o : members) {
                    if (o instanceof Element) {
                        processQueue.add((Element) o);
                    }
                }
            }

            processedSet.add(element);
        }
        return vss;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}
