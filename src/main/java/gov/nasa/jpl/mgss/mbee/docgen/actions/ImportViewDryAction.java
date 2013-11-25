package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ImportViewDryAction extends MDAction {

    private Element            doc;
    public static final String actionid = "ImportViewDry";

    public ImportViewDryAction(Element e) {
        super(actionid, "Validate Sync", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentValidator dv = new DocumentValidator(doc);
        dv.validateDocument();
        dv.printErrors();
        if (dv.isFatal())
            return;
        Boolean recurse = Utils.getUserYesNoAnswer("Check recursively?");
        if (recurse == null)
            return;
        ImportViewAction.doImportView(doc, false, recurse, null);
    }
}
