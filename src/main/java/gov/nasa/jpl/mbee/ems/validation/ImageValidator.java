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
package gov.nasa.jpl.mbee.ems.validation;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportImage;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.net.HttpURLConnection;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ImageValidator {

    private Map<String, JSONObject> images;
    private ValidationSuite suite = new ValidationSuite("images");
    private ValidationRule rule = new ValidationRule("Image Outdated", "Diagram is outdated", ViolationSeverity.ERROR);
    
    public ImageValidator(Map<String, JSONObject> images) {
        this.images = images;
        suite.addValidationRule(rule);
    }
    
    public void validate() {
        String url = ExportUtility.getUrlWithWorkspace();
        if (url == null)
            return;
        for (String key: images.keySet()) {
            Element e = (Element)ExportUtility.getElementFromID(key);
            String cs = (String)images.get(key).get("cs");
            String extension = (String)images.get(key).get("extension");
            String id = key.replace(".", "%2E");
            String baseurl = url + "/artifacts/" + id + "?cs=" + cs + "&extension=" + extension;
           
            GetMethod get = new GetMethod(baseurl);
            int status = 0;
            try {
                HttpClient client = new HttpClient();
                ViewEditUtils.setCredentials(client, baseurl);
                client.executeMethod(get);
                status = get.getStatusCode();
            } catch (Exception ex) {
                //printStackTrace(ex, gl);
            } finally {
                get.releaseConnection();
            }

            if (status != HttpURLConnection.HTTP_OK) {
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[IMAGE] This image is outdated on the web");
                v.addAction(new ExportImage(e, images));
                rule.addViolation(v);
            }
        }
    }
    
    public ValidationSuite getSuite() {
        return suite;
    }
}
