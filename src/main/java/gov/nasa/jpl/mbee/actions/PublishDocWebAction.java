package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.web.HttpsUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * this is just a convenience action for people to publish docgen documents to
 * docweb from magicdraw this does not actually generate the document locally,
 * but tells docweb to add a generation request if document is not already on
 * docweb, docweb will add it to uncategorized under user chosen project
 * 
 * @author dlam
 * 
 */
@SuppressWarnings("serial")
public class PublishDocWebAction extends MDAction {
    private NamedElement       p;
    public static final String actionid = "Publish2DocWeb";

    public PublishDocWebAction(NamedElement p) {
        super(actionid, "Publish to Docweb", null, null);
        this.p = p;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ac) {
        HttpsUtils.allowSelfSignedCertificates();
        GUILog log = Application.getInstance().getGUILog();
        String user = TeamworkUtils.getLoggedUserName();
        if (user == null || user.equals("")) {
            JOptionPane.showMessageDialog(null, "You must be logged into teamwork!");
            return;
        }
        String url = JOptionPane
                .showInputDialog(
                        "Input/confirm the url of docweb without trailing slash (ex. https://docweb.jpl.nasa.gov/app",
                        "https://docweb.jpl.nasa.gov/app");
        JSONObject servers = null;
        JSONArray projects = null;
        String chosenServer = null;
        String chosenProject = "";
        if (url != null && !url.equals("")) {
            GetMethod pm = new GetMethod(url + "/servers/");
            GetMethod pm2 = new GetMethod(url + "/projects/");
            try {
                HttpClient client = new HttpClient();
                int code = client.executeMethod(pm);
                if (code != 200) {
                    log.log("bad: " + pm.getResponseBodyAsString());
                    return;
                }
                String response = pm.getResponseBodyAsString();
                servers = (JSONObject)JSONValue.parse(response);
                List<String> names = new ArrayList<String>();
                List<String> display = new ArrayList<String>();
                for (Object server: servers.keySet()) {
                    Object twport = servers.get(server);
                    names.add((String)server);
                    display.add((String)server + ": " + (String)twport);
                }
                chosenServer = Utils.getUserDropdownSelectionForString("Choose the server to update",
                        "Choose the teamwork server this document is from", names, display);
                if (chosenServer == null)
                    return;

                code = client.executeMethod(pm2);
                if (code != 200) {
                    log.log("bad: " + pm2.getResponseBodyAsString());
                    return;
                }
                response = pm2.getResponseBodyAsString();
                projects = (JSONArray)JSONValue.parse(response);
                chosenProject = Utils.getUserDropdownSelectionForString("Choose the project to update",
                        "Choose the project to update", projects, projects);
                if (chosenProject == null)
                    return;
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                log.log("url doesn't work");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
                log.log("cannot connect");
                return;
            } finally {
                pm.releaseConnection();
            }
        } else {
            log.log("canceled or url not good");
            return;
        }
        String preview = "False";
        if (Utils.getUserYesNoAnswer("Will this be a preview?"))
            preview = "True";
        String doc = p.getName();
        String proj = Application.getInstance().getProject().getName();
        PostMethod pm = new PostMethod(url + "/publish/");
        try {
            pm.setRequestHeader("Content-Type", PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);
            pm.addParameter("user", user);
            pm.addParameter("server", chosenServer);
            pm.addParameter("document", doc);
            pm.addParameter("project", proj);
            pm.addParameter("jplproj", chosenProject);
            pm.addParameter("preview", preview);
            HttpClient client = new HttpClient();
            client.executeMethod(pm);
            log.log(pm.getResponseBodyAsString());
        } catch (HttpException e) {
            e.printStackTrace();
            log.log("http exception");
        } catch (IOException e) {
            e.printStackTrace();
            log.log("io exception");
        } finally {
            pm.releaseConnection();
        }

    }
}
