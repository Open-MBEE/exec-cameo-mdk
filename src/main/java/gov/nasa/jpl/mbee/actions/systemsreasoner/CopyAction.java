package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.api.incubating.json.JsonConverters;
import gov.nasa.jpl.mbee.ems.ImportException;
import gov.nasa.jpl.mbee.ems.emf.EMFImporter2;
import gov.nasa.jpl.mbee.lib.Changelog;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;

public class CopyAction extends SRAction {

    /**
     * Defunct. Was for testing purposes only.
     */
    private static final long serialVersionUID = 1L;
    public static final String actionid = "Import JSON";

    public CopyAction(Element element) {
        super(actionid, element);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
