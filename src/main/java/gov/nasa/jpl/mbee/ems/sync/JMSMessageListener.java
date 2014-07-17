package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class JMSMessageListener implements MessageListener {

    private Project project;
    
    public JMSMessageListener(Project project) {
        this.project = project;
    }
    
    @Override
    public void onMessage(Message msg) {
        try {
            TextMessage message = (TextMessage)msg;
            JSONObject ob = (JSONObject)JSONValue.parse(message.getText());
            final JSONArray updated = (JSONArray)ob.get("updatedElements");

            Runnable runnable = new Runnable() {
                public void run() {
                    SessionManager sm = SessionManager.getInstance();
                    sm.createSession("mms sync change");
                    try {
                        for (Object element: updated) {
                            Element ele = ExportUtility.getElementFromID((String)((JSONObject)element).get("sysmlid"));
                            if (ele == null) {
                                Application.getInstance().getGUILog().log("element not found from mms sync change");
                                continue;
                            }
                            String newName = (String)((JSONObject)element).get("name");
                            if (ele instanceof NamedElement && !((NamedElement)ele).getName().equals(newName)) 
                                ((NamedElement)ele).setName(newName);
                        }
                        sm.closeSession();
                    } catch (Exception e) {
                       
                       sm.cancelSession();
                    } 
                }
            };
            project.getRepository().invokeAfterTransaction(runnable);
            
        } catch (Exception e) {
            
        }
        
    }

}
