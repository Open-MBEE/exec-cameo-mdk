package gov.nasa.jpl.mbee.mdk.actions.ems;

import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;

import java.awt.event.ActionEvent;

public class SendProjectVersionAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "SendProjectVersion";

    public SendProjectVersionAction() {
        super(actionid, "Send Project Version", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ExportUtility.sendProjectVersion();
    }

}
