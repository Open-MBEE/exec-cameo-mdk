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

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ExportImage extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element element;
    private Map<String, JSONObject> images;
    
    public ExportImage(Element e, Map<String, JSONObject> images) {
    	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
    	//
       super("ExportImage", "Commit image", null, null);
        this.element = e;
        this.images = images;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            String key = e.getID();
            String filename = (String)images.get(key).get("abspath");
            String cs = (String)images.get(key).get("cs");
            String extension = (String)images.get(key).get("extension");
            String url = ExportUtility.getUrlWithWorkspace();
            if (url == null)
                return;
            String baseurl = url + "/artifacts/" + key + "?cs=" + cs + "&extension=" + extension;
            String site = ExportUtility.getSite();
            String posturl = url + "/sites/" + site + "/artifacts/" + key + "?cs=" + cs + "&extension=" + extension;

            //String baseurl = url + "/artifacts/magicdraw/" + key + "?cs=" + cs + "&extension=" + extension;
            File imageFile = new File(filename);
            PostMethod post = new PostMethod(posturl);
            try { 
                Part[] parts = {new FilePart("content", imageFile)};
                post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
                HttpClient client = new HttpClient();
                ViewEditUtils.setCredentials(client, baseurl);
                client.executeMethod(post);
                int status = post.getStatusCode();
                if (!ExportUtility.showErrors(status, post.getResponseBodyAsString(), false)) {
                    Application.getInstance().getGUILog().log("[INFO] Successful");
                }
            } catch (Exception ex) {
                Utils.printException(ex);
            } finally {
                post.releaseConnection();
            }
        }
       
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String key = element.getID();
        String filename = (String)images.get(key).get("abspath");
        String cs = (String)images.get(key).get("cs");
        String extension = (String)images.get(key).get("extension");
        String url = ExportUtility.getUrlWithWorkspace();
        if (url == null)
            return;
        String baseurl = url + "/artifacts/" + key + "?cs=" + cs + "&extension=" + extension;
        String site = ExportUtility.getSite();
        String posturl = url + "/sites/" + site + "/artifacts/" + key + "?cs=" + cs + "&extension=" + extension;

        File imageFile = new File(filename);
        PostMethod post = new PostMethod(posturl);
        try { 
            Part[] parts = {new FilePart("content", imageFile)};
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, baseurl);
            client.executeMethod(post);
            int status = post.getStatusCode();
            if (!ExportUtility.showErrors(status, post.getResponseBodyAsString(), false)) {
                Application.getInstance().getGUILog().log("[INFO] Successful");
            }
        } catch (Exception ex) {
            Utils.printException(ex);
        } finally {
            post.releaseConnection();
        }

    }
}
