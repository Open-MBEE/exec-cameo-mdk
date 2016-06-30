package gov.nasa.jpl.mbee.actions.docgen;

import gov.nasa.jpl.mbee.generator.ViewPresentationGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.ArrayList;
import java.util.List;

public class GenerateViewPresentationAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "GenerateViewPresentation";
    public static final String recurseActionid = "GenerateViewPresentationR";

    private List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
    private Element doc;
    private boolean recurse;
    

    public GenerateViewPresentationAction(Element e, boolean recurse) {
        super(recurse ? recurseActionid : actionid, recurse ? "Generate Views": "Generate View", null, null);
        doc = e;
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!Utils.recommendUpdateFromTeamwork())
            return;
        updateAction();
    }
    
    public List<ValidationSuite> updateAction() {
        ViewPresentationGenerator vg = new ViewPresentationGenerator(doc, recurse, null, true, null, null);
        ProgressStatusRunner.runWithProgressStatus(vg, "Generating View(s)...", true, 0);
        vss.addAll(vg.getValidations());
        return vss;
    }
    
    public List<ValidationSuite> getValidations() {
    	return vss;
    }
    
}
