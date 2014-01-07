package gov.nasa.jpl.mbee.alfresco.validation.actions;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.alfresco.ExportUtility;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.DBEditDocwebVisitor;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ExportView extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element view;
    private GUILog gl = Application.getInstance().getGUILog();
    
    public ExportView(Element e) {
        super("ExportView", "Export view", null, null);
        this.view = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        Collection<Annotation> toremove = new ArrayList<Annotation>();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            if (exportView(e)) {
                toremove.add(anno);
            }
        }
        if (!toremove.isEmpty()) {
            this.removeViolationsAndUpdateWindow(toremove);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (exportView(view)) {
            this.removeViolationAndUpdateWindow();
        }
    }
    
    private boolean exportView(Element view) {
        DocumentGenerator dg = new DocumentGenerator(view, null, null);
        Document dge = dg.parseDocument(true, false);
        (new PostProcessor()).process(dge);
        boolean document = false;
        
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGen3Profile.documentViewStereotype, "Document Profile");
        if (StereotypesHelper.hasStereotypeOrDerived(view, documentView))
            document = true;

        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null)
            return false;

        DBAlfrescoVisitor visitor2 = new DBAlfrescoVisitor(false);
        book.accept(visitor2);
        /*int numElements = visitor2.getNumberOfElements();
        if (numElements > 10000) {
            Boolean cont = Utils.getUserYesNoAnswer("Alert! You're about to publish " + numElements
                    + " elements in a view, this may take about " + numElements / 1000
                    + " minutes to complete if you're doing initial loading, do you want to continue?");
            if (cont == null || !cont) {
                return false;
            }
        }*/
        JSONObject elementsjson = visitor2.getElements();
        gl.log(elementsjson.toJSONString());
        //send elements first, then view info
        JSONObject viewjson = visitor2.getViews();
        gl.log(viewjson.toJSONString());
        
        String url = ViewEditUtils.getUrl(false);

        // first post view information View Editor
        String baseurl = url + "/rest/views/" + view.getID();
        // Upload images to view editor (JSON keys are specified in
        // DBEditDocwebVisitor
        gl.log("[INFO] Updating Images...");
        Map<String, JSONObject> images = visitor2.getImages();
        boolean isAlfresco = true;
       
        for (String key: images.keySet()) {
            String filename = (String)images.get(key).get("abspath");
            String cs = (String)images.get(key).get("cs");
            String extension = (String)images.get(key).get("extension");

            File imageFile = new File(filename);
            
            baseurl = url + "/artifacts/magicdraw/" + key + "?cs=" + cs + "&extension=" + extension;
           
            // check whether the image already exists
            GetMethod get = new GetMethod(baseurl);
            int status = 0;
            try {
                HttpClient client = new HttpClient();
                ViewEditUtils.setCredentials(client, baseurl);
                gl.log("[INFO] Checking if imagefile exists... " + key + "_cs" + cs + extension);
                client.executeMethod(get);

                status = get.getStatusCode();
            } catch (Exception ex) {
                //printStackTrace(ex, gl);
            } finally {
                get.releaseConnection();
            }

            if (status == HttpURLConnection.HTTP_OK) {
                gl.log("[INFO] Image file already exists, not uploading");
            } else {
                PostMethod post = new PostMethod(baseurl);
                try {
                    if (isAlfresco) {
                        Part[] parts = {new FilePart("content", imageFile)};
                        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
                    } else {
                        post.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(imageFile),
                                imageFile.length()));
                    }
                    HttpClient client = new HttpClient();
                    ViewEditUtils.setCredentials(client, baseurl);
                    gl.log("[INFO] Did not find image, uploading file... " + key + "_cs" + cs + extension);
                    client.executeMethod(post);

                    status = post.getStatusCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        gl.log("[ERROR] Could not upload image file to view editor");
                    }
                } catch (Exception ex) {
                    //printStackTrace(ex, gl);
                } finally {
                    post.releaseConnection();
                }
            }
        }

        // clean up the local images
        visitor2.removeImages();
        return true;
    }
    
}
