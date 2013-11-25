package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.generator.ViewStructureValidator;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * checks view to viewpoint composition hierarchy - if the view composition
 * hierarchy violates the viewpoint composition
 * 
 * @author dlam
 * 
 */
@SuppressWarnings("serial")
public class ValidateViewStructureAction extends MDAction {

    public static final String actionid = "ValidateViewStructure";
    private Element            view;

    public ValidateViewStructureAction(Element e) {
        super(actionid, "Validate Viewpoint Conformance", null, null);
        view = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        try {
            ViewStructureValidator vsv = new ViewStructureValidator(view);
            vsv.validate(view);
            vsv.printErrors();
            gl.log("Finished");
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }
}
