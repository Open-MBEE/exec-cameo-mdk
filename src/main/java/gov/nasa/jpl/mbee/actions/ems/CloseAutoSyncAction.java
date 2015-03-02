package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.sync.AutoSyncProjectListener;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

public class CloseAutoSyncAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "CloseAutoSync";

    public CloseAutoSyncAction() {
        super(actionid, "Stop Dynamic Sync", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = Application.getInstance().getProject();
        AutoSyncProjectListener.close(project, true);
    }

}

