package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ValidateViewRunner;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateHierarchyAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element view;
    public static final String actionid = "ValidateHierarchy";
    
    public ValidateHierarchyAction(Element e) {
        super(actionid, "Validate View Hierarchy", null, null);
        view = e;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.checkBaseline()) {    
            return;
        }
        ProgressStatusRunner.runWithProgressStatus(new ValidateViewRunner(view, false, true), "Validating View Hierarchy", true, 0);
    }
}

