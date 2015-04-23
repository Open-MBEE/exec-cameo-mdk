package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;

public class UpdateWorkspacesAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "UpdateWorkspaces";

    public UpdateWorkspacesAction() {
        super(actionid, "(Update Workspaces Mappings)", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ExportUtility.updateWorkspaceIdMapping();
    }
}


