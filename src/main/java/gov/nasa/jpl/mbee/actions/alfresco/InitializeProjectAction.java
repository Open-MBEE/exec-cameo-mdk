package gov.nasa.jpl.mbee.actions.alfresco;

import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.GUILog;

public class InitializeProjectAction extends MDAction {

    private static final long serialVersionUID = 1L;
    
    public static final String actionid = "InitializeProject";
    
    public InitializeProjectAction() {
        super(actionid, "Initialize Project", null, null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();

        JSONObject result = new JSONObject();
        result.put("name", Application.getInstance().getProject().getName());
        String json = result.toJSONString();

        gl.log(json);
        String url = ViewEditUtils.getUrl(false);
        if (url == null) {
            return;
        }
        url += "/javawebscripts/sites/europa/projects/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
       // gl.log("*** Starting export view comments ***");
        PostMethod pm = new PostMethod(url);
        try {
            gl.log("[INFO] Sending...");
            pm.setRequestHeader("Content-Type", "application/json;charset=utf-8");
            pm.setRequestEntity(JsonRequestEntity.create(json));
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url);
            int code = client.executeMethod(pm);
            if (ViewEditUtils.showErrorMessage(code))
                return;
            String response = pm.getResponseBodyAsString();
            // gl.log(response);
            if (response.equals("NotFound"))
                gl.log("[ERROR] There are some views that are not exported yet, export the views first, then the comments");
            else if (response.equals("ok"))
                gl.log("[INFO] Export Successful.");
            else
                gl.log(response);

        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        } finally {
            pm.releaseConnection();
        }

    }
}
