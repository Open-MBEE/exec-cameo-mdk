package gov.nasa.jpl.mbee.mdk.actions.ems;

import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;

import java.awt.event.ActionEvent;

public class UpdateWorkspacesAction extends MMSAction {
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


