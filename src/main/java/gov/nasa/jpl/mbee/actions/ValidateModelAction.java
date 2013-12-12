package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.alfresco.validation.ModelValidator;
import gov.nasa.jpl.mbee.alfresco.validation.ResultHolder;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateModelAction extends MDAction {

    private Element start;
    public static final String actionid = "ValidateModel";
    
    public ValidateModelAction(Element e) {
        super(actionid, "Validate Model", null, null);
        start = e;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String url = ViewEditUtils.getUrl();
        if (url == null) {
            return;
        }
        url += "/javawebscripts/sites/europa/projects/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();;
        GetMethod gm = new GetMethod(url);
        try {
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url);
            int code = client.executeMethod(gm);
            if (ViewEditUtils.showErrorMessage(code))
                return;
            String json = gm.getResponseBodyAsString();
            JSONObject result = (JSONObject)JSONValue.parse(json);
            ResultHolder.lastResults = result;
            ModelValidator validator = new ModelValidator(start, result);
            validator.validate();
            validator.showWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            gm.releaseConnection();
        }
    }
}
