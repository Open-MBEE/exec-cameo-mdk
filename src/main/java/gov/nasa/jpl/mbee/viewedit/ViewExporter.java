/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.actions.vieweditor.ImportViewAction;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ViewExporter implements RunnableWithProgress {

    private Document                    dge;
    private Element                     doc;
    private boolean                     recurse;
    private boolean                     force;
    private String                      url;
    private DocumentValidator           dv;
    private ValidationSuite             vs   = new ValidationSuite("Changed Elements");
    private ValidationRule              vr   = new ValidationRule("Changed Name",
                                                     "Name of element has been changed on VE",
                                                     ViolationSeverity.INFO);
    private ValidationRule              vr2  = new ValidationRule("Changed Doc",
                                                     "Doc of element has been changed on VE",
                                                     ViolationSeverity.INFO);
    private ValidationRule              vr3  = new ValidationRule("Changed Value",
                                                     "Default Value of element has been changed on VE",
                                                     ViolationSeverity.INFO);
    private Collection<ValidationSuite> cvs  = new ArrayList<ValidationSuite>();
    private GUILog                      gl   = Application.getInstance().getGUILog();
    private boolean                     alfresco;
    private String                      user = Utils.getUsername();

    public ViewExporter(Document dge, Element doc, boolean recurse, boolean force, String url,
            DocumentValidator dv) {
        this.dge = dge;
        this.doc = doc;
        this.recurse = recurse;
        this.force = force;
        this.url = url;
        this.dv = dv;
        if (url != null) {
            this.alfresco = url.contains("service");
        }
    }

    /**
     * Private utility for dumping the stack trace out to GUILog
     */
    private void printStackTrace(Exception ex, GUILog gl) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        gl.log(sw.toString()); // stack trace as a string
        ex.printStackTrace();
    }

    @Override
    public void run(ProgressStatus arg0) {
        vs.addValidationRule(vr);
        vs.addValidationRule(vr2);
        vs.addValidationRule(vr3);
        cvs.add(vs);
        arg0.setIndeterminate(true);
        if (url == null)
            return;
        if (recurse) {
            DocumentGenerator dg = new DocumentGenerator(doc, dv, null);
            Document dge = dg.parseDocument(true, true, false);
            ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
            dge.accept(vhv);
            JSONObject res = vhv.getResult();
            JSONObject views = (JSONObject)res.get("views");
            for (Object viewid: views.keySet()) {
                Element view = (Element)Application.getInstance().getProject().getElementByID((String)viewid);
                if (!postView(view, false))
                    return;
            }
            String post = res.toJSONString();
            String posturl = url + "/rest/views/" + doc.getID() + "/hierarchy";
            PostMethod pm = new PostMethod(posturl);
            try {
                pm.setRequestHeader("Content-Type", "application/json");
                pm.setRequestEntity(JsonRequestEntity.create(post));
                HttpClient client = new HttpClient();
                ViewEditUtils.setCredentials(client, posturl);
                // gl.log(post);
                gl.log("[INFO] Sending View Hierarchy...");
                int code = client.executeMethod(pm);
                if (ViewEditUtils.showErrorMessage(code))
                    return;
                String response = pm.getResponseBodyAsString();
                if (response.equals("ok"))
                	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
                	//
                    gl.log("[INFO] Commit Successful.");
                else
                    gl.log(response);
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                gl.log(sw.toString()); // stack trace as a string
                ex.printStackTrace();
            } finally {
                if (pm != null)
                    pm.releaseConnection();
            }

        } else {
            postView(doc, recurse);
        }
        if (vs.hasErrors()) {
        	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
        	//
            Utils.displayValidationWindow(cvs, "View CommitResults (Changed Elements)");
            gl.log("[INFO] See changed element info in validation window.");
        }
        // if synchronizing views
        if (!force) {
            ImportViewAction.doImportView(doc, true, recurse, url);
        }
    }

    private boolean postView(Element view, boolean rec) {
        DocumentGenerator dg = new DocumentGenerator(view, dv, null);
        dge = dg.parseDocument(true, rec, false);
        (new PostProcessor()).process(dge);
        boolean document = false;

        String baseurl = url;

        // first post view information View Editor
        baseurl += "/rest/views/" + view.getID();
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGen3Profile.documentViewStereotype, "Document Profile");
        if (StereotypesHelper.hasStereotypeOrDerived(view, documentView))
            document = true;

        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null)
            return false;
        
        baseurl += "?user=" + user;

        DBEditDocwebVisitor v = new DBEditDocwebVisitor(rec, alfresco);
        book.accept(v);
        int numElements = v.getNumberOfElements();
        if (numElements > 10000 && alfresco) {
            Boolean cont = Utils.getUserYesNoAnswer("Alert! You're about to publish " + numElements
                    + " elements in a view, this may take about " + numElements / 1000
                    + " minutes to complete if you're doing initial loading, do you want to continue?");
            if (cont == null || !cont) {
                return false;
            }
        }
        String json = v.getJSON();
        // gl.log(json);
        if (rec || document || force) {
            baseurl += "&";
            List<String> params = new ArrayList<String>();
            if (rec)
                params.add("recurse=true");
            if (document)
                params.add("doc=true");
            if (force)
                params.add("force=true");
            if (dge.isProduct())
                params.add("product=true");
            baseurl += Utils.join(params, "&");
        }

        PostMethod pm = new PostMethod(baseurl);
        try {
            pm.setRequestHeader("Content-Type", "application/json;charset=utf-8");
            pm.setRequestEntity(JsonRequestEntity.create(json));
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, baseurl);
            gl.log("[INFO] Sending...");
            int code = client.executeMethod(pm);
            if (ViewEditUtils.showErrorMessage(code))
                return false;
            String response = pm.getResponseBodyAsString();
            if (response.equals("ok"))
            	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
            	//
                gl.log("[INFO] Commit Successful.");
            else if (response.startsWith("[")) {

                for (Object o: (JSONArray)JSONValue.parse(response)) {
                    String mdid = (String)((JSONObject)o).get("mdid");
                    String type = (String)((JSONObject)o).get("type");
                    BaseElement be = Application.getInstance().getProject().getElementByID(mdid);
                    if (be != null && be instanceof Element) {
                        if (type.equals("name"))
                            vr.addViolation((Element)be, "name changed on VE");
                        else if (type.equals("doc"))
                            vr.addViolation((Element)be, "doc changed on VE");
                        else
                            vr.addViolation((Element)be, "default value changed on VE");
                    }
                }

            	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
            	//
                gl.log("[INFO] Commit Successful.");

            } else
                gl.log(response);
        } catch (Exception ex) {
            printStackTrace(ex, gl);
        } finally {
            pm.releaseConnection();
        }

        // Upload images to view editor (JSON keys are specified in
        // DBEditDocwebVisitor
        gl.log("[INFO] Updating Images...");
        Map<String, JSONObject> images = v.getImages();
        boolean isAlfresco = false;
        if (url.indexOf("service") >= 0) {
            isAlfresco = true;
        }
        for (String key: images.keySet()) {
            String filename = (String)images.get(key).get("abspath");
            String cs = (String)images.get(key).get("cs");
            String extension = (String)images.get(key).get("extension");

            File imageFile = new File(filename);
            if (isAlfresco) {
                baseurl = url + "/artifacts/magicdraw/" + key + "?cs=" + cs + "&extension=" + extension;
            } else {
                baseurl = url + "/rest/images/" + key + "?cs=" + cs + "&extension=" + extension;
            }

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
                printStackTrace(ex, gl);
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
                    printStackTrace(ex, gl);
                } finally {
                    post.releaseConnection();
                }
            }
        }

        // clean up the local images
        v.removeImages();
        return true;
    }
}
