package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.awt.event.ActionEvent;

public class CopyAction extends SRAction {

    /**
     * Defunct. Was for testing purposes only.
     */
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Import JSON";

    public CopyAction(Element element) {
        super(DEFAULT_ID, element);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
