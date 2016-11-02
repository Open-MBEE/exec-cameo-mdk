/*******************************************************************************
 * Copyright (c) <2016>, California Institute of Technology ("Caltech").
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

package gov.nasa.jpl.mbee.mdk.api;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.MMSSyncPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.actions.EMSLoginAction;
import gov.nasa.jpl.mbee.mdk.ems.actions.EMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.ems.actions.GenerateViewPresentationAction;
import gov.nasa.jpl.mbee.mdk.ems.actions.UpdateAllDocumentsAction;
import gov.nasa.jpl.mbee.mdk.ems.sync.coordinated.CoordinatedSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.ems.sync.delta.DeltaSyncRunner;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncProjectEventListenerAdapter.LocalSyncProjectMapping;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.ems.sync.manual.ManualSyncRunner;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.TicketUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class exposes MDK operations for use in external programs.
 */
public class MDKHelper {

    /************************************************************
     *
     * General Helper Methods
     *
     ************************************************************/

    public static Changelog<String, Element> getInMemoryElementChangelog(Project project) {
        return LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener().getInMemoryLocalChangelog();
    }

    /**
     * Causes program to pause execution until all added commit operations
     * have been completed.
     */
    public static boolean mmsUploadWait() {
        if (OutputQueue.getInstance().getCurrent() != null) {
            int elements = OutputQueue.getInstance().getCurrent().getNumElements();
            for (Request request : OutputQueue.getInstance()) {
                elements += request.getNumElements();
            }
            MagicDrawHelper.generalMessage("Uploading: " + elements + " Elements");
            while (true) {
                if (OutputQueue.getInstance().getCurrent() == null) {
                    MagicDrawHelper.generalMessage("Upload complete");
                    return true;
                }
                else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }
        else {
            return false;
        }
    }

    /**
     * Sets boolean that can disabled popups and redirect their messages to the GUI log.
     *
     * @param disabled true to redirect popups to gui log, false to renable normal popup behavior
     */
    public static void setPopupsDisabled(boolean disabled) {
        Utils.setPopupsDisabled(disabled);
    }

    public static void setSyncTransactionListenerDisabled(boolean enable) {
        Project project = Application.getInstance().getProject();
        LocalSyncProjectMapping lspm = LocalSyncProjectEventListenerAdapter.getProjectMapping(project);
        if (lspm == null) {
            throw new IllegalStateException("LocalSyncProjectMapping is null");
        }
        LocalSyncTransactionCommitListener lstcl = lspm.getLocalSyncTransactionCommitListener();
        if (lstcl == null) {
            throw new IllegalStateException("LocalSyncTransactionCommitListener is null");
        }
        lstcl.setDisabled(enable);
    }

    public static boolean isPopupsDisabled() {
        return Utils.isPopupsDisabled();
    }

    /************************************************************
     *
     * MMS Login Methods
     *
     ************************************************************/

    /**
     * Sets the supplied username and password in memory. Does not validate its accuracy.
     *
     * @param username Username for MMS login
     * @param password Password for MMS login
     */
    public static void setMMSLoginCredentials(String username, String password) {
        TicketUtils.setUsernameAndPassword(username, password);
    }

    /**
     * Logs onto mms using the supplied username and password Does not generate
     * or interact with mmslogin dialog window
     *
     * @param username Username for MMS login
     * @param password Password for MMS login
     */
    public static boolean loginToMMS(final String username, final String password) {
        TicketUtils.setUsernameAndPassword(username, password);
        return new EMSLoginAction().loginAction(Application.getInstance().getProject());
    }

    /**
     * Logs onto mms using the supplied username and password Does not generate
     * or interact with mmslogin dialog window
     *
     */
    public static void logoutOfMMS() {
        new EMSLogoutAction().logoutAction();
    }

    /************************************************************
     *
     * MDK Validation Window Access Methods
     *
     ************************************************************/

    private static MDKValidationWindow validationWindow;

    /**
     * Gets the currently stored validationWindow accessor. This might be a CSync or manual validation suite, depending
     * on what operation was last performed.
     *
     * @return the currently stored validationWindow accessor
     */
    public static MDKValidationWindow getValidationWindow() {
        return validationWindow;
    }

    /**
     * Updates the MDKValidationWindow object with the latest delta sync results, or sets to null if
     * there are no results.
     */
    public static void loadCoordinatedSyncValidations() {
        CoordinatedSyncProjectEventListenerAdapter cspela = MMSSyncPlugin.getInstance().getCoordinatedSyncProjectEventListenerAdapter();
        if (cspela == null) {
            return;
        }
        DeltaSyncRunner dsr = cspela.getDeltaSyncRunner();
        if (dsr == null) {
            return;
        }
        List<ValidationSuite> vss = dsr.getValidations();
        if (vss.isEmpty()) {
            return;
        }
        validationWindow = new MDKValidationWindow(vss);
    }

    /**
     * Updates the stored validationWindow accessor with a new suite of validations. Not many use cases for this as of 2.4
     *
     * @param vss The new validation suite to load validationWindow accessor
     */
    @Deprecated
    public static void updateValidationWindow(List<ValidationSuite> vss) {
        validationWindow = new MDKValidationWindow(vss);
    }

    /************************************************************
     *
     * Model Initialization Methods
     *
     ************************************************************/

    /**
     * Checks if entire project is initialized; if not does nothing
     *
     */
    public static boolean checkInitialization() {
        if (validationWindow == null) {
            manualValidateModel();
        }
        return validationWindow.listPooledViolations("Project Existence") == 0;
    }

    /**
     * Checks if entire project is initialized; if not initializes project
     * without committing model
     *
     */
    public static boolean confirmInitialization()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (validationWindow == null) {
            manualValidateModel();
        }
        // if there are initialization violations
        if (validationWindow.listPooledViolations("Project Existence") != 0) {
            // process initializations, if possible
            validationWindow.initializeProject();
            mmsUploadWait();

            // re-validate and re-check
            manualValidateModel();
            if (validationWindow.listPooledViolations("Project Existence") != 0) {
                // if not clear now, other errors in project
                return false;
            }
        }
        // validation contains no initialization violations
        return true;
    }

    /**********************************************************************************
     *
     * MDK User Actions
     *
     **********************************************************************************/

    /**
     * Executes "Generate View(s)" MDK element action on the selected element
     *
     * @param doc     Document element
     * @param recurse Select true to generate all views under this element, false to
     *                generate only the view for the selected element
     */
    public static void generateViews(Element doc, Boolean recurse) {
        List<Element> documents = new ArrayList<>(1);
        documents.add(doc);
        GenerateViewPresentationAction gvpa = new GenerateViewPresentationAction(documents, recurse);
        validationWindow = new MDKValidationWindow(gvpa.updateAction());
    }

    public static void generateAllDocuments() {
        UpdateAllDocumentsAction uad = new UpdateAllDocumentsAction();
        validationWindow = new MDKValidationWindow(uad.updateAction());
    }

    /**
     * Executes "Validate Model" on specified element
     *
     * @param validateTarget element that the validation is to be performed upon
     */
    public static void manualValidateElement(Element validateTarget) {
        Collection<Element> sync = new ArrayList<Element>();
        sync.add(validateTarget);
        ManualSyncRunner manualSyncRunner = new ManualSyncRunner(sync, Application.getInstance().getProject(), true, 0);
        ProgressStatusRunner.runWithProgressStatus(manualSyncRunner, "Manual Sync", true, 0);
        Application.getInstance().getGUILog().log("Validated");
        validationWindow = new MDKValidationWindow(manualSyncRunner.getValidationSuite());
    }

    /**
     * Executes "Validate Model" on model root
     */
    public static void manualValidateModel() {
        manualValidateElement(ElementFinder.getModelRoot());
    }

    /**********************************************************************************
     *
     * MMS REST Interactions
     *
     **********************************************************************************/

    public static ObjectNode getMmsElement(Element e, Project project) throws IOException, ServerException, URISyntaxException {
        return MMSUtils.getElement(e, project);
    }

    public static ObjectNode getMmsElementByID(String s, Project project) throws IOException, ServerException, URISyntaxException {
        return MMSUtils.getElementById(s, project);
    }

    public static ObjectNode getMmsElements(Collection<Element> elements, Project project) throws ServerException, IOException, URISyntaxException {
        return MMSUtils.getElements(elements, project, null);
    }

    public static ObjectNode getMmsElementsByID(Collection<String> cs, Project project) throws ServerException, IOException, URISyntaxException {
        return MMSUtils.getElementsById(cs, project, null);
    }

    /**
     * Sends a DELETE request to MMS for the indicated element.
     *
     * @param elementsToDelete Collection of elements you want to directly delete on the MMS
     * @throws IllegalStateException
     * @throws ServerException
     */
    public static ObjectNode deleteMmsElements(Collection<Element> elementsToDelete, Project project)
            throws IOException, URISyntaxException, ServerException {
        ObjectNode requestBody = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elements = requestBody.putArray("elements");
        for (Element delTarget : elementsToDelete) {
            ObjectNode curElement = JacksonUtils.getObjectMapper().createObjectNode();
            curElement.put(MDKConstants.SYSML_ID_KEY, Converters.getElementToIdConverter().apply(delTarget));
            elements.add(curElement);
        }
        requestBody.put("source", "magicdraw");
        requestBody.put("mmsVersion", MDKPlugin.VERSION);
        HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.DELETE,
                MMSUtils.getServiceWorkspacesSitesElementsUri(project), requestBody);
        return MMSUtils.sendMMSRequest(request);
    }

    /**
     * Sends a POST request to MMS with the element JSON, creating or updating the element as appropriate.
     *
     * @param elementsToPost Collection of elements you want to directly post on the MMS
     * @throws IllegalStateException
     */
    public static ObjectNode postMmsElement(Collection<Element> elementsToPost, Project project)
            throws IOException, URISyntaxException, ServerException {
        ObjectNode requestBody = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elements = requestBody.putArray("elements");
        for (Element postTarget : elementsToPost) {
            elements.add(Converters.getElementToJsonConverter().apply(postTarget, project));
        }
        requestBody.put("source", "magicdraw");
        requestBody.put("mmsVersion", MDKPlugin.VERSION);
        HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST,
                MMSUtils.getServiceWorkspacesSitesElementsUri(project), requestBody);
        return MMSUtils.sendMMSRequest(request);
    }

    /**
     * Convenience method for confirmSiteWritePermissions(string, string) to check if a project
     * is editable by the logged in user. Uses the url and site information stored in the currently
     * open project.
     *
     * @return true if the site lists "editable":"true" for the logged in user, false otherwise
     * or when no project is open or project lacks url and site specifications
     */
    public static boolean isSiteEditable() throws ServerException, IOException, URISyntaxException {
        Project project = Application.getInstance().getProject();
        return MMSUtils.isSiteEditable(project, MMSUtils.getSiteName(project));
    }



}