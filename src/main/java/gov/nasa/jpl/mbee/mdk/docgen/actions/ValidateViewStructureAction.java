package gov.nasa.jpl.mbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.generator.ViewStructureValidator;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * checks view to viewpoint composition hierarchy - if the view composition
 * hierarchy violates the viewpoint composition
 *
 * @author dlam
 */
public class ValidateViewStructureAction extends MDAction {

    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "ValidateViewStructure";
    private Element view;

    public ValidateViewStructureAction(Element e) {
        super(DEFAULT_ID, "Validate Viewpoint Conformance", null, null);
        view = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        try {
            ViewStructureValidator vsv = new ViewStructureValidator();
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
