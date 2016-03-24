package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ValidateViewRunner;
import gov.nasa.jpl.mbee.ems.sync.ManualSyncRunner;
import gov.nasa.jpl.mbee.generator.ViewInstanceUtils;
import gov.nasa.jpl.mbee.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class OneClickUpdateDoc extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "OneClickUpdate";
    private Element doc;
    private static ArrayList<ValidationSuite> vss = new ArrayList<ValidationSuite>();
    
    
    public OneClickUpdateDoc(Element doc) {
        super(actionid, "Generate Views and Commit to MMS", null, null);
        this.doc = doc;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!Utils.recommendUpdateFromTeamwork())
            return;
        updateAction();
    }
    
    public List<ValidationSuite> updateAction() {
        ManualSyncRunner msr = new ManualSyncRunner(false, false);
        ProgressStatusRunner.runWithProgressStatus(msr, "Updating project from MMS", true, 0);
        if (msr.getFailure()) {
            Utils.guilog("[ERROR] Update from MMS was not completed");
            return null;
        }
        ViewPresentationGenerator vg = new ViewPresentationGenerator(doc, true, msr.getCannotChange(), true, null);
        ProgressStatusRunner.runWithProgressStatus(vg, "Generating View(s)...", true, 0);
        if (vg.getFailure()) {
            Utils.guilog("[ERROR] View generation was not completed");
            return null;
        }
        Stereotype documentView = Utils.getProductStereotype();
        if (StereotypesHelper.hasStereotypeOrDerived(doc, documentView))
            ProgressStatusRunner.runWithProgressStatus(new ValidateViewRunner(doc, false, true, true), "Validating View Hierarchy", true, 0);
        ManualSyncRunner msr2 = new ManualSyncRunner(true, false);
        ProgressStatusRunner.runWithProgressStatus(msr2, "Committing project to MMS", true, 0);
        vss.add(msr.getValidationSuite());
        return vss;
    }
}