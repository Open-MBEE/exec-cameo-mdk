package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ModelExportRunner;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportView;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ExportViewAction extends MDAction {

    private static final long serialVersionUID = 1L;

    private Element start;
    private boolean recursive;
    public static final String actionid = "ExportView";
    
    public ExportViewAction(Element e, boolean recursive) {
        super(recursive ? "ExportViewRecursive" : "ExportView", recursive ? "Commit Views" : "Commit View", null, null);
        this.recursive = recursive;
        start = e;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Boolean b = Utils.getUserYesNoAnswer("Are you sure you want to commit view(s)? This will only commit the view structure and not model data.");
        if (b == null || !b)
            return;
        ExportView action = new ExportView(start, recursive, false, "");
        action.actionPerformed(null);
    }

}
