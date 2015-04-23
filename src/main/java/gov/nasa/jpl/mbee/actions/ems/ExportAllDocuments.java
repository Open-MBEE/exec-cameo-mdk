package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ViewExportRunner;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportView;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;


public class ExportAllDocuments extends MDAction {

    private static final long serialVersionUID = 1L;

    private Element start;
    public static final String actionid = "ExportAllDocuments";
    
    public ExportAllDocuments(Element e) {
        super(actionid, "Commit All Documents", null, null);
        start = e;
    }
    
    public class ExportDocumentsRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus arg0) {
            Stereotype ps = Utils.getProductStereotype();
            if (ps == null)
                return;
            List<Element> elements = Utils.collectOwnedElements(start, 0);
            List<Element> docs = Utils.filterElementsByStereotype(elements, ps, true, true);
            List<Element> projDocs = new ArrayList<Element>();
            for (Element doc: docs) {
                if (!ProjectUtilities.isElementInAttachedProject(doc))
                    projDocs.add(doc);
            }
            if (projDocs.isEmpty()) {
                Application.getInstance().getGUILog().log("No Documents Found");
                return;
            }
            for (Element doc: projDocs) {
                ExportView action = new ExportView(doc, true, false, "");
                action.performAction();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Boolean b = Utils.getUserYesNoAnswer("Are you sure you want to commit all documents and views found within? Only view structures will be commited and not model data.");
        if (b == null || !b)
            return;
        ProgressStatusRunner.runWithProgressStatus(new ExportDocumentsRunner(), "Exporting Documents", true, 0);
    }

}

