package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.validation.ViewValidator;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateViewRecursiveAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private Element view;
    public static final String actionid = "ValidateViewRecursiveAlfresco";
    
    public ValidateViewRecursiveAction(Element e) {
        super(actionid, "Validate View With VE (Recursive)", null, null);
        view = e;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        ViewValidator vv = new ViewValidator(view, true);
        vv.validate();
        vv.showWindow();
    }
}
