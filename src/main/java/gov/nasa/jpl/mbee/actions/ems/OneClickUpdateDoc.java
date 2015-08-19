package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.sync.ManualSyncRunner;
import gov.nasa.jpl.mbee.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class OneClickUpdateDoc extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "OneClickUpdate";
    private Element doc;
    
    public OneClickUpdateDoc(Element doc) {
        super(actionid, "Update, Generate, and Commit", null, null);
        this.doc = doc;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ae) {
        ManualSyncRunner msr = new ManualSyncRunner(false);
        ProgressStatusRunner.runWithProgressStatus(msr, "Updating project from MMS", true, 0);
        if (msr.getFailure()) {
            Utils.guilog("[ERROR] Update from MMS was not completed");
            return;
        }
        ViewPresentationGenerator vg = new ViewPresentationGenerator(doc, true);
        ProgressStatusRunner.runWithProgressStatus(vg, "Generating View(s)...", true, 0);
        if (vg.getFailure()) {
            Utils.guilog("[ERROR] View generation was not completed");
            return;
        }
        ManualSyncRunner msr2 = new ManualSyncRunner(true, true);
        ProgressStatusRunner.runWithProgressStatus(msr2, "Committing project to MMS", true, 0);
    }
}