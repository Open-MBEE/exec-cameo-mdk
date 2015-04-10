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
package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ViewExportRunner;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
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
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ExportView extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element view;
    private boolean recurse;
    private GUILog gl = Application.getInstance().getGUILog();
    private String url;
    private String sendElementsUrl;
    private boolean exportElements;
    
    public ExportView(Element e, boolean recursive, boolean exportElements, String action) {
    	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
    	//
        //super(recursive ? "ExportViewRecursive" : "ExportView", recursive ? "Commit views hierarchically" : "Commit view", null, null);
        super(action, action, null, null);
        this.recurse = recursive;
        this.exportElements = exportElements;
        this.view = e;
        url = ExportUtility.getUrlWithWorkspace();
        sendElementsUrl = ExportUtility.getPostElementsUrl();
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        if (!ExportUtility.okToExport())
            return;
        if (url == null)
            return;
        if (sendElementsUrl == null)
            return;
        ProgressStatusRunner.runWithProgressStatus(new ViewExportRunner(this, annos), "Exporting Views", true, 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.okToExport())
            return;
        if (url == null)
            return;
        if (sendElementsUrl == null)
            return;
        ProgressStatusRunner.runWithProgressStatus(new ViewExportRunner(this, null), "Exporting View", true, 0);
    }
    
    public void performAction() {
        
        if (exportView(view)) {
            this.removeViolationAndUpdateWindow();
            //ExportUtility.sendProjectVersions();
        }
    }
    
    public void performActions(Collection<Annotation> annos) {
        Collection<Annotation> toremove = new ArrayList<Annotation>();
        
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            if (exportView(e)) {
                toremove.add(anno);
            } else
                break;
        }
        //ExportUtility.sendProjectVersions();
        if (!toremove.isEmpty()) {
            this.removeViolationsAndUpdateWindow(toremove);
        }
    }
    
    @SuppressWarnings("unchecked")
    public boolean exportView(Element view) {
        DocumentValidator dv = new DocumentValidator(view);
        dv.validateDocument();
        if (dv.isFatal()) {
            dv.printErrors(false);
            return false;
        }
        DocumentGenerator dg = new DocumentGenerator(view, dv, null);
        Document dge = null;
        boolean document = false;
        
        if (StereotypesHelper.hasStereotypeOrDerived(view, Utils.getProductStereotype()))
            document = true;
        dge = dg.parseDocument(true, recurse, false);
        (new PostProcessor()).process(dge);
        
        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null)
            return false;

        DBAlfrescoVisitor visitor2 = null;
        //if (document)
        //    visitor2 = new DBAlfrescoVisitor(true);
        //else
        visitor2 = new DBAlfrescoVisitor(recurse);
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
        //Set<Element> set = visitor2.getElementSet();
        JSONObject send = new JSONObject();
        
        if (exportElements) {
            JSONObject elementsjson = visitor2.getElements();
            JSONArray elementsArray = new JSONArray();
            elementsArray.addAll(elementsjson.values());
            send.put("elements", elementsArray);
            send.put("source", "magicdraw");
            if (url == null)
                return false;
            if (ExportUtility.send(sendElementsUrl, send.toJSONString(), null, false, false) == null)
                return false;
        }
        //send elements first, then view info
        JSONObject viewjson = visitor2.getViews();
        JSONArray viewsArray = new JSONArray();
        viewsArray.addAll(viewjson.values());
        send = new JSONObject();
        send.put("elements", viewsArray);
        send.put("source", "magicdraw");
        /*if (document) {
            String docId = view.getID();
            JSONObject doc = null;
            for (JSONObject ele: (List<JSONObject>)viewsArray) {
                if (ele.get("sysmlid").equals(docId)) {
                    doc = ele;
                    break;
                }
            }
            if (doc != null) {
                JSONObject spec = (JSONObject)doc.get("specialization");
                ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
                dge.accept(vhv);
                spec.put("view2view", ExportUtility.formatView2View(vhv.getView2View()));
                //spec.put("noSections", visitor2.getNosections());
                spec.put("type", "Product");
            }
        }*/
        Application.getInstance().getGUILog().log("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(sendElementsUrl, send.toJSONString(), viewsArray.size()));
        //if (ExportUtility.send(sendElementsUrl, send.toJSONString(), null, false) == null)
        //    return false;
        
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
            
            String baseurl = url + "/artifacts/" + key + "?cs=" + cs + "&extension=" + extension;
            String site = ExportUtility.getSite();
            String posturl = url + "/sites/" + site + "/artifacts/" + key + "?cs=" + cs + "&extension=" + extension;
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
                PostMethod post = new PostMethod(posturl);
                try {
                    if (isAlfresco) {
                        Part[] parts = {new FilePart("content", imageFile)};
                        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
                    } else {
                        post.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(imageFile),
                                imageFile.length()));
                    }
                    OutputQueue.getInstance().offer(new Request(posturl, post));
                    //HttpClient client = new HttpClient();
                    //ViewEditUtils.setCredentials(client, baseurl);
                    gl.log("[INFO] Did not find image, uploading file... " + key + "_cs" + cs + extension);
                    //client.executeMethod(post);

                    //status = post.getStatusCode();
                    //if (status != HttpURLConnection.HTTP_OK) {
                    //    gl.log("[ERROR] Could not upload image file to view editor");
                    //}
                } catch (Exception ex) {
                    //printStackTrace(ex, gl);
                } finally {
                    //post.releaseConnection();
                }
            }
        }
        //OutputQueue.getInstance().offer(new Request("", "[INFO] Export View Done", "LOG"));
        // clean up the local images
        //visitor2.removeImages();
        //gl.log("[INFO] Done");
        if (document) {//&& recurse) {
            //String docurl = url + "/javawebscripts/products";
            /*send = new JSONObject();
            JSONArray documents = new JSONArray();
            JSONObject doc = new JSONObject();
            JSONObject spec = new JSONObject();
            ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
            dge.accept(vhv);
            spec.put("view2view", ExportUtility.formatView2View(vhv.getView2View()));
            //spec.put("noSections", visitor2.getNosections());
            spec.put("type", "Product");
            doc.put("sysmlid", view.getID());
            doc.put("specialization", spec);
            documents.add(doc);
            send.put("elements", documents);*/
            //OutputQueue.getInstance().offer(new Request(sendElementsUrl, send.toJSONString()));
            //if (ExportUtility.send(sendElementsUrl, send.toJSONString(), null, false) == null)
            //   return false;
        } /*else if (recurse) {
            JSONArray views = new JSONArray();
            JSONObject view2view = visitor2.getHierarchy();
            for (Object viewid: view2view.keySet()) {
                JSONObject viewinfo = new JSONObject();
                viewinfo.put("id", viewid);
                viewinfo.put("childrenViews", view2view.get(viewid));
                views.add(viewinfo);
            }
            JSONObject send  = new JSONObject();
            send.put("views", views);
            if (!ExportUtility.send(url + "/javawebscripts/views", send.toJSONString()))
                return false;
        }
        */
        return true;
    }
    
}
