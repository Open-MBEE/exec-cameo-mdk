package gov.nasa.jpl.mbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.generator.ViewDependencyNumberer;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * number dependencies based on the section they're pointing to
 *
 * @author dlam
 */
public class NumberDependencyAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element doc;
    public static final String DEFAULT_ID = "NumberDependencies";

    public NumberDependencyAction(Element e) {
        super(DEFAULT_ID, "Number View Dependencies", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();

        try {
            SessionManager.getInstance().createSession("number dependencies");
            ViewDependencyNumberer.clearAll(doc);
            ViewDependencyNumberer.start(doc, new ArrayList<Integer>());
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }
}
