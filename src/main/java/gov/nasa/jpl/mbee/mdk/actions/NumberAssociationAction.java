package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.model.AssociationNumberingVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.event.ActionEvent;


/**
 * number dependencies based on the section they're pointing to
 *
 * @author dlam
 */
public class NumberAssociationAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Class doc;
    public static final String DEFAULT_ID = "NumberAssociations";

    public NumberAssociationAction(Class e) {
        super(DEFAULT_ID, "Number View Associations", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        DocumentGenerator dg = new DocumentGenerator(doc, null, null);
        Document dge = dg.parseDocument(false, true, true);
        try {
            SessionManager.getInstance().createSession("number dependencies");
            AssociationNumberingVisitor hmv = new AssociationNumberingVisitor();
            dge.accept(hmv);
            SessionManager.getInstance().closeSession();
            gl.log("[INFO] Done");
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        }
    }
}
