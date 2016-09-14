package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.queue.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;

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
