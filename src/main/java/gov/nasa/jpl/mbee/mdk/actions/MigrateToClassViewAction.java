package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.HierarchyMigrationVisitor;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * given a viewpoint composition hierarchy, makes the views, and have them
 * conform to the respective viewpoints
 *
 * @author dlam
 */
public class MigrateToClassViewAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element doc;

    public static final String DEFAULT_ID = "MigrateToClassViews";

    public MigrateToClassViewAction(Element e) {
        super(DEFAULT_ID, "Migrate to Class Views", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentGenerator dg = new DocumentGenerator(doc, null, null);
        Document dge = dg.parseDocument(false, true, true);
        List packages = new ArrayList();
        packages.add(Package.class);
        Element owner = (Element) Utils.getUserSelection(packages, "Pick a package to create under");

        if (owner != null) {
            Boolean preserveId = Utils.getUserYesNoAnswer("Preserve Ids? (This will swap the ids of the existing views and new class views created.)");
            boolean preserve = false;
            if (preserveId != null && preserveId) {
                preserve = true;
            }
            SessionManager.getInstance().createSession("docgen migration");
            try {
                HierarchyMigrationVisitor hmv = new HierarchyMigrationVisitor(owner, preserve);
                dge.accept(hmv);
                if (preserve && hmv.changeIdFailed()) {
                    Application.getInstance().getGUILog().log("[ERROR] Not all existing views are editable, cannot preserve ids, aborted.");
                    throw new Exception("failed cannot preserve ids on old document migrations");
                }
                SessionManager.getInstance().closeSession();
                Application.getInstance().getGUILog().log("[INFO] Done (note previous 'nosection' views are now views under the parent view).");
            } catch (Exception ex) {
                SessionManager.getInstance().cancelSession();
                Utils.printException(ex);
            }

        }

    }
}
