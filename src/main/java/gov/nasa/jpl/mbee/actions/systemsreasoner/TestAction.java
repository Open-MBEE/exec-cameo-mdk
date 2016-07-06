package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Namespace;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncProjectListener;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.systemsreasoner.validation.actions.CreateInstanceAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class TestAction extends SRAction {

	public static final String actionid = "Test";

	public TestAction() {
        super(actionid, null);
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		SessionManager.getInstance().createSession(actionid);
		final InstanceSpecification instance = Application.getInstance().getProject().getElementsFactory().createInstanceSpecificationInstance();
		//instance.setOwner(Application.getInstance().getProject().getModel());
        Application.getInstance().getGUILog().log(instance.getID());
        JSONArray toSendElements = new JSONArray();
        toSendElements.add(ExportUtility.fillElement(instance, null));
        JSONObject toSendUpdates = new JSONObject();
        toSendUpdates.put("elements", toSendElements);
        toSendUpdates.put("source", "magicdraw");
        toSendUpdates.put("mmsVersion", DocGenPlugin.VERSION);
        OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), toSendUpdates.toJSONString(), "POST", true, toSendElements.size(), "Sync Changes"));
        try {
            ModelElementsManager.getInstance().removeElement(instance);
        } catch (ReadOnlyElementException e1) {
            e1.printStackTrace();
        }
        SessionManager.getInstance().closeSession();
	}
}
