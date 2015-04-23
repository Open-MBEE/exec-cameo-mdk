package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

public class SendProjectVersionAction extends MDAction {
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
