package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.generator.ViewInstancesOrganizer;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class OrganizeViewInstancesAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private Element            doc;
    private boolean recurse;
    public static final String actionid = "OrganizeViewInstances";
    public static final String recurseActionid = "OrganizeViewInstancesR";


    public OrganizeViewInstancesAction(Element e, boolean recurse) {
        super(recurse ? recurseActionid : actionid, recurse ? "Organize Views": "Organize View", null, null);
        doc = e;
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!Utils.recommendUpdateFromTeamwork())
            return;
        ViewInstancesOrganizer vg = new ViewInstancesOrganizer(doc, recurse, true, null);
        ProgressStatusRunner.runWithProgressStatus(vg, "Organizing View(s)...", true, 0);
    }
}