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
package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.apache.http.client.utils.URIBuilder;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

public class ExportImage extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element element;
    private Map<String, ObjectNode> images;

    public ExportImage(Element e, Map<String, ObjectNode> images) {
        super("ExportImage", "Commit image", null, null);
        this.element = e;
        this.images = images;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    public static boolean postImage(Project project, String key, Map<String, ObjectNode> is) {
        if (is == null || is.get(key) == null) {
            Utils.guilog("[ERROR] Image data with id " + key + " not found.");
            return false;
        }
        JsonNode value;
        String filename = "";
        String cs = "";
        String extension = "";
        if ((value = is.get(key).get("abspath")) != null && value.isTextual()) {
            filename = value.asText();
        }
        if ((value = is.get(key).get("cs")) != null && value.isTextual()) {
            cs = value.asText();
        }
        if ((value = is.get(key).get("extension")) != null && value.isTextual()) {
            extension = value.asText();
        }

        URIBuilder requestUri = MMSUtils.getServiceProjectsWorkspacesElementsUri(project);

        if (requestUri == null) {
            return false;
        }
        requestUri.setParameter("cs", cs);
        requestUri.setParameter("extension", extension);

        File imageFile = new File(filename);
        try {
            Request imageRequest = new Request(requestUri, imageFile, "Image");
            imageRequest.getRequest().setHeader("Content-Type", "image/" + extension);
            OutputQueue.getInstance().offer(imageRequest);
        } catch (IOException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to commit image " + filename + ". Reason: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        for (Annotation anno : annos) {
            Element e = (Element) anno.getTarget();
            String key = e.getID();
            postImage(Project.getProject(e), key, images);
        }
        Utils.guilog("[INFO] Requests are added to queue.");

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String key = element.getID();
        if (postImage(Project.getProject(element), key, images)) {
            Utils.guilog("[INFO] Request is added to queue.");
        }
    }
}
