package gov.nasa.jpl.mbee.generator;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CleanViewHierarchyAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private Element            doc;
    public static final String actionid = "CleanViewHierarchy";


    public CleanViewHierarchyAction(Element e) {
        super(actionid, "Clean View Hierarchy", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	//need to add in the action
//        ViewPresentationValidator vg = new ViewPresentationValidator(doc);
//        vg.cleanHierarchy();
    }
}
