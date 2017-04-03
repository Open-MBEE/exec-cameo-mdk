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
package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.manual.ManualSyncActionRunner;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class CommitProjectAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public static final String DEFAULT_ID = CommitProjectAction.class.getSimpleName();
    public static final String COMMIT_MODEL_DEFAULT_ID = DEFAULT_ID + "_Commit_Model";

    private final Project project;
    private final boolean shouldCommitModel;

    public CommitProjectAction(Project project) {
        this(project, false, false);
    }

    public CommitProjectAction(Project project, boolean shouldCommitModel) {
        this(project, shouldCommitModel, false);
    }

    public CommitProjectAction(Project project, boolean shouldCommitModel, boolean isDeveloperAction) {
        super(shouldCommitModel ? COMMIT_MODEL_DEFAULT_ID : DEFAULT_ID, "Commit Project" + (shouldCommitModel ? " and Model" : "") + (isDeveloperAction ? " [DEVELOPER]" : ""), null, null);
        this.project = project;
        this.shouldCommitModel = shouldCommitModel;
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
        commitAction();
    }

    public String commitAction() {
        // '{"elements": [{"sysmlId": "123456", "name": "vetest", "type": "Project"}]}' -X POST "http://localhost:8080/alfresco/service/orgs/vetest/projects"
        String org = null;

        // get orgs uri to check orgs / post project
        URIBuilder requestUri = MMSUtils.getServiceOrgsUri(project);
        if (requestUri == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS orgs url. Project commit cancelled.");
            return null;
        }

        // check for existing org, use that if it exists instead of prompting to select one
        try {
            org = MMSUtils.getMmsOrg(project);
            // a null result here just means the project isn't on mms
        } catch (IOException | URISyntaxException | ServerException e1) {
            Application.getInstance().getGUILog().log("[ERROR] Exception occurred while checking for project org on MMS. Project commit cancelled. Reason: " + e1.getMessage());
            e1.printStackTrace();
            return null;
        }

        // check for org options if one isn't specified in the project already
        ObjectNode response;
        if (org == null || org.isEmpty()) {
            try {
                response = JacksonUtils.parseJsonObject(MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri)));
            } catch (IOException | URISyntaxException | ServerException e1) {
                Application.getInstance().getGUILog().log("[ERROR] Exception occurred while getting MMS orgs. Project commit cancelled. Reason: " + e1.getMessage());
                e1.printStackTrace();
                return null;
            }
            ArrayList<String> mmsOrgsList = new ArrayList<>();
            if (response != null) {
                JsonNode arrayNode;
                if ((arrayNode = response.get("orgs")) != null && arrayNode.isArray()) {
                    for (JsonNode orgNode : arrayNode) {
                        JsonNode value;
                        if ((value = orgNode.get(MDKConstants.NAME_KEY)) != null && value.isTextual()) {
                            mmsOrgsList.add(value.asText());
                        }
                    }
                }
            }
            String[] mmsOrgs = mmsOrgsList.toArray(new String[mmsOrgsList.size()]);
            if (mmsOrgs.length > 0) {
                JFrame selectionDialog = new JFrame();
                org = (String) JOptionPane.showInputDialog(selectionDialog, "Select MMS org:",
                        "MMS Org Selector", JOptionPane.QUESTION_MESSAGE, null, mmsOrgs, mmsOrgs[0]);
            }
            else {
                Application.getInstance().getGUILog().log("[WARNING] No orgs were returned from MMS.");
            }
            if ((org == null || org.isEmpty()) && MDUtils.isDeveloperMode()) {
                org = new CommitOrgAction(project).commitAction();
            }
        }

        if (org == null || org.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to commit project without an org. Project commit cancelled.");
            return null;
        }

        // update request with project post path
        requestUri.setPath(requestUri.getPath() + "/" + org + "/projects");
        Collection<ObjectNode> projects = new LinkedList<>();
        projects.add(MMSUtils.getProjectObjectNode(project));

        // do project post request
        try {
            File sendData = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, projects, MMSUtils.JsonBlobType.PROJECT);
            response = JacksonUtils.parseJsonObject(MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, sendData, ContentType.APPLICATION_JSON)));
            // we don't need to process this response, just make sure the request comes back without exception
        } catch (IOException | URISyntaxException | ServerException e1) {
            Application.getInstance().getGUILog().log("[ERROR] Exception occurred while posting project to MMS. Project commit cancelled. Reason: " + e1.getMessage());
            e1.printStackTrace();
            return null;
        }
        // update master ref
        // TODO @donbot enable this ref post after master is updatable
//        new CommitBranchAction("master", project, EsiUtils.getCurrentBranch(project.getPrimaryProject()), false).commitAction();
        // do model post
        if (shouldCommitModel) {
            RunnableWithProgress temp = new ManualSyncActionRunner<>(CommitClientElementAction.class, Collections.singletonList(project.getPrimaryModel()), project, -1);
            ProgressStatusRunner.runWithProgressStatus(temp, "Model Initialization", true, 0);
        }
        return Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
    }
}

