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
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
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
import org.python.google.common.collect.Lists;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class exposes MDK operations for use in external programs.
 */
public class MDKHelper {

    private static MDKValidationWindow validationWindow;

    public static MDKValidationWindow getManualValidationWindow() {
        return validationWindow;
    }

    /**
     * Updates the MDKValidationWindow object with the latest delta sync results, or sets window to null if there are no results.
     */
    public static MDKValidationWindow getCoordinatedSyncValidationWindow() {
        CoordinatedSyncProjectEventListenerAdapter cspela = MMSSyncPlugin.getInstance().getCoordinatedSyncProjectEventListenerAdapter();
        if (cspela == null) {
            return null;
        }
        DeltaSyncRunner dsr = cspela.getDeltaSyncRunner();
        if (dsr == null) {
            return null;
        }
        List<ValidationSuite> vss = dsr.getValidations();
        if (vss.isEmpty()) {
            return null;
        }
        validationWindow = new MDKValidationWindow(vss);
        return validationWindow;
    }


    public static void updateManualValidationWindow(List<ValidationSuite> vss) {
        validationWindow = new MDKValidationWindow(vss);
    }

    /************************************************************
     *
     * MDK Methods
     *
     ************************************************************/

    /**
     * Checks if entire project is initialized; if not does nothing
     *
     * @throws Exception
     */
    public static boolean checkInitialization() throws Exception {
        if (validationWindow == null) {
            validateModelRoot();
        }
        return validationWindow.listPooledViolations("INITIALIZATION") == 0;
    }

    /**
     * Checks if entire project is initialized; if not initializes project
     * without committing model
     *
     * @throws Exception
     */
    public static boolean confirmInitialization() throws Exception {
        if (validationWindow == null) {
            validateModelRoot();
        }
        // if there are initialization violations
        if (validationWindow.listPooledViolations("INITIALIZATION") != 0) {
            // process initializations, if possible
            validationWindow.commitMDChangesToMMS("INITIALIZATION");
            mmsUploadWait();

            // re-validate and re-check
            validateModelRoot();
            if (validationWindow.listPooledViolations("INITIALIZATION") != 0) {
                // if not clear now, other errors in project
                return false;
            }
        }
        // validation contains no initialization violations
        return true;
    }

    /**
     * Logs onto mms using the supplied username and password Does not generate
     * or interact with mmslogin dialog window
     *
     * @param username
     * @param password
     */
    public static boolean loginToMMS(final String username, final String password) {
        TicketUtils.setUsernameAndPassword(username, password);
        return TicketUtils.loginToMMS();
    }

    /**
     * Sets boolean that can disabled popups and redirect their messages to the GUI log.
     *
     * @param disabled true to redirect popups to gui log, false to renable normal popup behavior
     */
    public static void setPopupsDisabled(boolean disabled) {
        Utils.setPopupsDisabled(disabled);
    }

    /**
     * Sets the supplied username and password in memory. Does not validate its accuracy.
     *
     * @param username
     * @param password
     */
    public static void setMMSLoginCredentials(String username, String password) {
        TicketUtils.setUsernameAndPassword(username, password);
    }

    /**
     * Causes program to pause execution until all added commit operations
     * have been completed.
     */
    public static boolean mmsUploadWait() {
        if (OutputQueue.getInstance().getCurrent() != null) {
            int elements = OutputQueue.getInstance().getCurrent().getNumElements();
            Iterator<Request> queueIterator = OutputQueue.getInstance().iterator();
            while (queueIterator.hasNext()) {
                elements += queueIterator.next().getNumElements();
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

    /**********************************************************************************
     *
     * Single element MDK Actions
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
        GenerateViewPresentationAction gvpa = new GenerateViewPresentationAction(Lists.newArrayList(doc), recurse);
        validationWindow = new MDKValidationWindow(gvpa.updateAction());
    }

    /**
     * Executes "Generate Views and Commit to MMS" action on the selected
     * element
     *
     * @param doc Selected Document Element.
     */
    @Deprecated
    public static void generateViewsAndCommitToMMS(Element doc) {
        //OneClickUpdateDoc ocud = new OneClickUpdateDoc(Lists.newArrayList(doc));
        //validationWindow = new MDKValidationWindow(ocud.updateAction());
    }

    /**
     * Executes "Validate Model" on specified element
     *
     * @param validateTarget element that the validation is to be performed upon
     */
    public static void validateModel(Element validateTarget) {
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
    public static void validateModelRoot() {
        validateModel(ElementFinder.getModelRoot());
    }

    /**
     * Executes "Validate View Hierarchy" on specified element
     *
     * @param validateTarget element that the validation is to be performed upon
     */
    @Deprecated
    public static void validateViewHierarchy(Element validateTarget) {
        //ValidateViewRunner vvr = new ValidateViewRunner(validateTarget, false, true, true);
        //ProgressStatusRunner.runWithProgressStatus(vvr, "Validating Views", true, 0);
        //validationWindow = new MDKValidationWindow(vvr.getValidations());
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
     * @param elements The element you want to delete on the MMS
     * @throws IllegalStateException
     * @throws ServerException
     */
    public static ObjectNode deleteMmsElements(Collection<Element> elements, Project project) throws IllegalStateException, IOException, URISyntaxException, ServerException {
        ObjectNode objectNode = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elementsArrayNode = objectNode.putArray("elements");
        elements.forEach(element -> elementsArrayNode.add(Converters.getElementToIdConverter().apply(element)));
        objectNode.put("source", "magicdraw");
        objectNode.put("mmsVersion", MDKPlugin.VERSION);

        HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.DELETE, MMSUtils.getServiceWorkspacesSitesElementsUri(project));
        return MMSUtils.sendMMSRequest(request);
    }

    /**
     * Sends a POST request to MMS with the element JSON, creating or updating the element as appropriate.
     *
     * @param elementsNode The JSONObject of the element to create or update.
     *                    Generally acquired through ExportUtility.fillElement(element, null)
     * @throws IllegalStateException
     */
    @Deprecated
    // TODO Move to MMSUtils @donbot
//    public static void postMmsElement(ObjectNode elementsNode) throws IllegalStateException {
//        if (elementsNode == null) {
//            throw new IllegalStateException("No element json specified to export to MMS");
//        }
//
//        Project proj = Application.getInstance().getProject();
//        if (proj == null) {
//            throw new IllegalStateException("No project opened.");
//        }
//
//        URIBuilder requestUri = MMSUtils.getServiceWorkspacesUri();
//        String url = ExportUtility.getPostElementsUrl();
//        if (requestUri == null) {
//            throw new IllegalStateException("Project does not have MMS URL configured.");
//        }
//
//
//
//        JSONArray elems = new JSONArray();
//        elems.add(elementsNode);
//        JSONObject send = new JSONObject();
//        send.put("elements", elems);
//
//        String response = ExportUtility.send(url, send.toJSONString(), false, true);
//        if (response == null) {
//            throw new IllegalStateException("Invalid send formatting.");
//        }
//    }


    /**
     * Convenience method for confirmSiteWritePermissions(string, string) to check if a project
     * is editable by the logged in user. Uses the url and site information stored in the currently
     * open project.
     *
     * @return true if the site lists "editable":"true" for the logged in user, false otherwise
     * or when no project is open or project lacks url and site specifications
     */
    public static boolean hasSiteEditPermission() throws ServerException, IOException, URISyntaxException {
        Project project = Application.getInstance().getProject();
        return MMSUtils.isSiteEditable(project, MMSUtils.getSiteName(project));
    }


    /**********************************************************************************
     *
     * Model wide MDK Actions
     *
     **********************************************************************************/

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

    /**
     * Executes "Generate All Documents and Commit" action
     */
    @Deprecated
    public static void generateAllDocumentsAndCommitToMMS() {
        generateAllDocuments();
    }

    public static void generateAllDocuments() {
        UpdateAllDocumentsAction uad = new UpdateAllDocumentsAction();
        validationWindow = new MDKValidationWindow(uad.updateAction());
    }

    /**
     * Starts MMS Auto Sync
     */
    @Deprecated
    public static void startAutoSync() {
    }

    /**
     * Stops MMS Auto Sync
     */
    @Deprecated
    public static void stopAutoSync() {
    }

    /**
     * Executes "Update From MMS" action
     */
    @Deprecated
    public static void updateFromMMS() {
        //UpdateFromJMS ufjms = new UpdateFromJMS(false);
        //validationWindow = new MDKValidationWindow(ufjms.updateAction());
    }

    /**
     * Executes "Commit to MMS" action
     */
    @Deprecated
    public static void commitToMMS() {
        updateAndCommitToMMS();
    }

    /**
     * Executes "Commit to MMS" action
     */
    @Deprecated
    public static void updateAndCommitToMMS() {
        //UpdateFromJMS ufjms = new UpdateFromJMS(true);
        //validationWindow = new MDKValidationWindow(ufjms.updateAction());
    }

    /**
     * Executes "Commit to MMS with Deletes" action
     */
    @Deprecated
    public static void updateAndCommitWithDeletesToMMS() {
        //UpdateFromJMSAndCommitWithDelete ufjmsacwd = new UpdateFromJMSAndCommitWithDelete();
        //validationWindow = new MDKValidationWindow(ufjmsacwd.updateAction());
    }

    public static Changelog<String, Element> getInMemoryElementChangelog(Project project) {
        return LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener().getInMemoryLocalChangelog();
    }

    public static boolean isLoginDialogDisabled() {
        return Utils.isPopupsDisabled();
    }

    public static void setLoginDialogDisabled(boolean loginDialogDisabled) {
        Utils.setPopupsDisabled(loginDialogDisabled);
    }
}