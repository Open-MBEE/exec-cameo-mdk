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
package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ModelExportRunner;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;

public class InitializeProjectModel extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private final Project project;
    private final boolean initOnly;

    public InitializeProjectModel(Project project, boolean initOnly) {
        super("InitializeProjectModel", initOnly ? "Initialize Project" : "Initialize Project and Model", null, null);
        this.project = project;
        this.initOnly = initOnly;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }


    @Override
    public void execute(Collection<Annotation> annos) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ObjectNode requestData = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elementsArrayNode = JacksonUtils.getObjectMapper().createArrayNode();
        requestData.set("elements", elementsArrayNode);
        requestData.put("mmsVersion", MDKPlugin.VERSION);
        requestData.put("source", "magicdraw");

        ObjectNode projectObjectNode = ExportUtility.getProjectObjectNode(project);
        elementsArrayNode.add(projectObjectNode);
        String url = ExportUtility.getUrlWithWorkspaceAndSite();
        if (url == null) {
            return;
        }
        url += "/projects";
        String response;
        try {
            response = ExportUtility.send(url, JacksonUtils.getObjectMapper().writeValueAsString(requestData), false, false);
        } catch (JsonProcessingException e1) {
            // TODO Error handle @donbot
            e1.printStackTrace();
            return;
        }
        if (response == null || response.startsWith("<html")) {
            return;
        }
        //ExportUtility.sendProjectVersion(Application.getInstance().getProject().getModel());
        if (!initOnly) {
            url = ExportUtility.getPostElementsUrl();
            if (url == null) {
                return;
            }
            String[] buttons = {"Background job on server", "Background job on magicdraw", "Abort Export"};
            //null can returns
            Boolean background = Utils.getUserYesNoAnswerWithButton("Use background export on server? You'll get an email when done.", buttons, true);
            if (background == null) {
                return;
            }
            ProgressStatusRunner.runWithProgressStatus(new ModelExportRunner(Application.getInstance().getProject().getModel(), 0, false, url, background), "Exporting Model", true, 0);

        }
    }
}

