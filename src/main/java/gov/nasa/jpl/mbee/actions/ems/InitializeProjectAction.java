package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;
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
        ExportUtility.send(url, json);
    }
}
