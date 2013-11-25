package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ProjectExporter;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class OrganizeViewEditorAction extends MDAction {

    public static final String actionid = "OrganizeViewEditor";
    private Element            project;

    public OrganizeViewEditorAction(Element project) {
        super(actionid, "Organize View Editor", null, null);
        this.project = project;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        (new ProjectExporter(project)).export();
    }
}
