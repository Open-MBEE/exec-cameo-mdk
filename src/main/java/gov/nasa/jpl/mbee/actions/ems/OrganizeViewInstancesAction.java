package gov.nasa.jpl.mbee.actions.ems;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.generator.ViewInstancesOrganizer;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

@Deprecated
public class OrganizeViewInstancesAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private List<Element> docs;
    private boolean recurse;
    public static final String actionid = "OrganizeViewInstances";
    public static final String recurseActionid = "OrganizeViewInstancesR";


    public OrganizeViewInstancesAction(List<Element> elements, boolean recurse) {
        super(recurse ? recurseActionid : actionid, recurse ? "Organize Views": "Organize View", null, null);
        docs = elements;
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Element doc : docs) {
            if (!Utils.recommendUpdateFromTeamwork()) {
                return;
            }
            ViewInstancesOrganizer vg = new ViewInstancesOrganizer(doc, recurse, true, null);
            ProgressStatusRunner.runWithProgressStatus(vg, "Organizing View" + (recurse ? "s" : "") + " - " + (doc instanceof NamedElement && ((NamedElement) doc).getName() != null ? ((NamedElement) doc).getName() : "<>"), true, 0);
        }
    }
}