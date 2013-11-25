package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.ViewEditorProfile;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ProjectExporter {

    private Element    project;
    private JSONObject v2v;
    private JSONObject volumes;
    private JSONObject v2d;
    private JSONArray  docs;
    private JSONArray  projectVolumes;
    private GUILog     log;

    public ProjectExporter(Element project) {
        this.project = project;
        v2v = new JSONObject();
        volumes = new JSONObject();
        v2d = new JSONObject();
        docs = new JSONArray();
        projectVolumes = new JSONArray();
        log = Application.getInstance().getGUILog();
    }

    @SuppressWarnings("unchecked")
    public void export() {
        handleProject();
        JSONObject res = new JSONObject();
        res.put("volume2volumes", v2v);
        res.put("name", ((NamedElement)project).getName());
        res.put("volume2documents", v2d);
        res.put("projectVolumes", projectVolumes);
        res.put("documents", docs);
        res.put("volumes", volumes);
        String post = res.toJSONString();
        // log.log(post);
        String url = ViewEditUtils.getUrl();
        if (url == null || url.equals(""))
            return;
        url += "/rest/projects/" + project.getID();
        PostMethod pm = new PostMethod(url);
        try {
            pm.setRequestHeader("Content-Type", "application/json");
            pm.setRequestEntity(JsonRequestEntity.create(post));
            // Protocol easyhttps = new Protocol("https", new
            // EasySSLProtocolSocketFactory(), 443);
            // Protocol.registerProtocol("https", easyhttps);
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url);
            int code = client.executeMethod(pm);
            if (ViewEditUtils.showErrorMessage(code))
                return;
            String response = pm.getResponseBodyAsString();
            if (response.equals("ok"))
                log.log("[INFO] Export Successful.");
            else
                log.log(response);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            pm.releaseConnection();
        }
    }

    @SuppressWarnings("unchecked")
    private void handleVolume(Element v) {
        volumes.put(v.getID(), ((NamedElement)v).getName());
        JSONArray vvols = new JSONArray();
        for (Element vol: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(v,
                ViewEditorProfile.hasVolume, 1, false, 1)) {
            handleVolume(vol);
            vvols.add(vol.getID());
        }
        v2v.put(v.getID(), vvols);
        JSONArray vdocs = new JSONArray();
        for (Element d: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(v,
                ViewEditorProfile.hasDocumentView, 1, false, 1)) {
            String did = handleDocument(d);
            if (did != null)
                vdocs.add(did);
        }
        for (Element d: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(v,
                ViewEditorProfile.inVolume, 2, false, 1)) {
            vdocs.add(d.getID());
            docs.add(d.getID());
        }
        v2d.put(v.getID(), vdocs);
    }

    @SuppressWarnings("unchecked")
    private void handleProject() {
        List<Element> vols = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(project,
                ViewEditorProfile.hasVolume, 1, false, 1);
        for (Element vol: vols) {
            handleVolume(vol);
            projectVolumes.add(vol.getID());
        }
    }

    @SuppressWarnings("unchecked")
    private String handleDocument(Element d) {
        String docid = (String)StereotypesHelper.getStereotypePropertyFirst(d, ViewEditorProfile.document,
                ViewEditorProfile.docId);
        if (docid != null) {
            docs.add(docid);
        }
        return docid;
    }
}
