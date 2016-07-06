package gov.nasa.jpl.mbee.actions.ems;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ValidateViewRunner;

import java.awt.event.ActionEvent;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateHierarchyAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private List<Element> views;
    public static final String actionid = "ValidateHierarchy";
    
    public ValidateHierarchyAction(List<Element> elements) {
        super(actionid, "Validate View Hierarchy", null, null);
        views = elements;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.checkBaseline()) {    
            return;
        }
        for (Element view : views) {
            ProgressStatusRunner.runWithProgressStatus(new ValidateViewRunner(view, false, true, true), "Validating View Hierarchy - " + (view instanceof NamedElement && ((NamedElement) view).getName() != null ? ((NamedElement) view).getName() : "<>"), true, 0);
        }
    }
}

