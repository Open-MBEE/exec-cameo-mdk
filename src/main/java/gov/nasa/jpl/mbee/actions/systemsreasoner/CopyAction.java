package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.ems.EMFImporter;

import java.awt.event.ActionEvent;

public class CopyAction extends SRAction {

    /**
     * Defunct. Was for testing purposes only.
     */
    private static final long serialVersionUID = 1L;
    public static final String actionid = "Import JSON";

    public CopyAction(Element element) {
        super(actionid, element);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        EMFImporter imp = new EMFImporter(element);
        imp.createElementsFromJSON();

    }

}
