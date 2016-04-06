package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ValidateViewRunner;
import gov.nasa.jpl.mbee.ems.sync.ManualSyncRunner;
import gov.nasa.jpl.mbee.generator.ViewInstanceUtils;
import gov.nasa.jpl.mbee.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class UpdateAllDocs extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "GenerateAllAndCommit";
    
    public UpdateAllDocs() {
        super(actionid, "Generate All Documents and Commit to MMS", null, null);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!Utils.recommendUpdateFromTeamwork())
            return;
        updateAction();
    }
    
    public void updateAction() {
        ManualSyncRunner msr = new ManualSyncRunner(false, false);
        ProgressStatusRunner.runWithProgressStatus(msr, "Updating project from MMS", true, 0);
        if (msr.getFailure()) {
            Utils.guilog("[ERROR] Update from MMS was not completed");
            return;
        }
        
        Set<Element> docs = getProjectDocuments();
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        ViewInstanceUtils viu = new ViewInstanceUtils();
        for (Element doc: docs) {
            ViewPresentationGenerator vg = new ViewPresentationGenerator(doc, true, msr.getCannotChange(), false, viu);
            ProgressStatusRunner.runWithProgressStatus(vg, "Generating Document " + ((NamedElement)doc).getName() + "...", true, 0);
            vss.addAll(vg.getValidations());
            if (vg.getFailure()) {
                Utils.guilog("[ERROR] Document generation was not completed");
                Utils.displayValidationWindow(vss, "View Generation and Images Validation");
                return;
            }
            ValidateViewRunner vvr = new ValidateViewRunner(doc, false, true, false);
            ProgressStatusRunner.runWithProgressStatus(vvr, "Validating View Hierarchy", true, 0);
            vss.addAll(vvr.getValidations());
        }
        Utils.displayValidationWindow(vss, "View Generation and Images Validation");
        ManualSyncRunner msr2 = new ManualSyncRunner(true, false);
        ProgressStatusRunner.runWithProgressStatus(msr2, "Committing project to MMS", true, 0);
    }
    
    private Set<Element> getProjectDocuments() {
        Stereotype documentView = Utils.getProductStereotype();
        List<Stereotype> products = new ArrayList<Stereotype>();
        for (Element el: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(documentView, Generalization.class, 2, 0)) {
            if (el instanceof Stereotype)
                products.add((Stereotype)el);
        }
        products.add(documentView);
        Set<Element> projDocs = new HashSet<Element>();
        for (Stereotype product: products) {
            for (InstanceSpecification is: product.get_instanceSpecificationOfClassifier()) {
                Element owner = is.getOwner();
                if (!ProjectUtilities.isElementInAttachedProject(owner) && StereotypesHelper.hasStereotypeOrDerived(owner, documentView) && owner instanceof Class)
                    projDocs.add(owner);
            }
        }
        
        /*
        List<Element> elements = Utils.collectOwnedElements(Application.getInstance().getProject().getModel(), 0);
        List<Element> docs = Utils.filterElementsByStereotype(elements, documentView, true, true);
        for (Element doc: docs) {
            if (!ProjectUtilities.isElementInAttachedProject(doc) && doc instanceof Class)
                projDocs.add(doc);
        }
        */
        if (projDocs.isEmpty()) {
            Application.getInstance().getGUILog().log("No Documents Found in this project");
        }
        return projDocs;
    }
}