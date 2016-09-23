package gov.nasa.jpl.mbee.mdk.actions.ems;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.awt.event.ActionEvent;

public class CreateHoldingBinAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "Login";


    public CreateHoldingBinAction() {
        super(actionid, "Create Holding Bin", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project proj = Utils.getProject();
        String projectId = proj.getPrimaryProject().getProjectID();
        String holdingBinId = "holding_bin_" + projectId;
        Element holdingBin = ExportUtility.getElementFromID(holdingBinId);
        if (holdingBin != null) {
            Utils.guilog("[INFO] Holding bin already exists.");
            return;
        }
        proj.getCounter().setCanResetIDForObject(true);

        SessionManager.getInstance().createSession("create holding bin");
        Package hb = proj.getElementsFactory().createPackageInstance();
        hb.setOwner(proj.getModel());
        hb.setName("holding_bin");
        hb.setID(holdingBinId);
        SessionManager.getInstance().closeSession();
        Utils.guilog("[INFO] Holding bin created.");
    }

}
