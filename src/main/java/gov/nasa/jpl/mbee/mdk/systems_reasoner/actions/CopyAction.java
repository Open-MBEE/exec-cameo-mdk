package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

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

    }

}
