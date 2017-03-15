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
package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.actions.ExportImage;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ImageValidator {

    private Map<String, ObjectNode> images;
    private ValidationSuite suite = new ValidationSuite("images");
    private ValidationRule rule = new ValidationRule("Image Outdated", "Diagram is outdated", ViolationSeverity.ERROR);
    private Map<String, ObjectNode> allImages;

    public ImageValidator(Map<String, ObjectNode> images, Map<String, ObjectNode> allImages) {
        this.images = images;
        this.allImages = allImages;
        if (allImages == null) {
            this.allImages = new HashMap<>();
        }
        this.allImages.putAll(images);
        suite.addValidationRule(rule);
    }

    public void validate(Project project) {
        URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
        if (requestUri == null) {
            return;
        }
        for (String key : images.keySet()) {
            // customize request
            Element e = Converters.getIdToElementConverter().apply(key, project);

            JsonNode value;

            String cs = "";
            if ((value = images.get(key).get("cs")) != null && value.isTextual()) {
                cs = value.asText();
            }
            requestUri.setParameter("cs", cs);

            String extension = "";
            if ((value = images.get(key).get("extension")) != null && value.isTextual()) {
                extension = value.asText();
            }
            requestUri.setParameter("extension", extension);

            // do request
            try {
                HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri);
                request.setHeader("Accept", "image/" + extension);
                MMSUtils.sendMMSRequest(project, request);
            } catch (IOException | URISyntaxException e1) {
                Application.getInstance().getGUILog().log("[ERROR] Exception occurred while validating images. Image validation cancelled. Reason: " + e1.getMessage());
                e1.printStackTrace();
                return;
            } catch (ServerException e1) {
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[IMAGE] This image is outdated on the web.");
                v.addAction(new ExportImage(e, allImages));
            }
        }
    }

    public ValidationSuite getSuite() {
        return suite;
    }

    public ValidationRule getRule() {
        return rule;
    }
}
