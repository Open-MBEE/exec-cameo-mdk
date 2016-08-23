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

package gov.nasa.jpl.mbee.api;

import com.nomagic.magicdraw.core.Project;

import gov.nasa.jpl.mbee.MMSSyncPlugin;
import gov.nasa.jpl.mbee.actions.docgen.GenerateViewPresentationAction;
import gov.nasa.jpl.mbee.actions.ems.*;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.ems.ValidateModelRunner;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.ems.sync.coordinated.CoordinatedSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.delta.DeltaSyncRunner;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter.LocalSyncProjectMapping;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.ui.ProgressStatusRunner;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.python.google.common.collect.Lists;

/**
 * This class exposes MDK operations for use in external programs.
 * 
 */
public class MDKHelper {

	private static MDKValidationWindow validationWindow;

	public static MDKValidationWindow getValidationWindow() {
		return validationWindow;
	}

	public static void updateValidationWindow(List<ValidationSuite> vss) {
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
		if (validationWindow == null)
			validateModelRoot();
		if (validationWindow.listPooledViolations("INITIALIZATION") != 0) {
			// if not clear, project may not be initialized (or there might be
			// other errors)
			return false;
		}
		return true;
	}

	/**
	 * Checks if entire project is initialized; if not initializes project
	 * without committing model
	 * 
	 * @throws Exception
	 */
	public static boolean confirmInitialization() throws Exception {
		if (validationWindow == null)
			validateModelRoot();
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
		EMSLoginAction ela = new EMSLoginAction();
		return ela.loginAction(username, password);
	}

    /**
     * Sets the supplied username and password in memory. Does not validate its accuracy.
     *
     * @param username
     * @param password
     */
	public static void setMMSLoginCredentials(String username, String password) {
        ViewEditUtils.setUsernameAndPassword(username, password, username != null && !username.isEmpty() && password != null && !password.isEmpty());
    }

	/**
	 * Causes program to pause execution until all added commit operations
	 * have been completed.
	 */
	public static boolean mmsUploadWait() {
		if (OutputQueue.getInstance().getCurrent() != null) {
			int elements = OutputQueue.getInstance().getCurrent().getNumElements();
			Iterator<Request> queueIterator = OutputQueue.getInstance().iterator();
			while (queueIterator.hasNext())
				elements += queueIterator.next().getNumElements();
			MagicDrawHelper.generalMessage("Uploading: " + elements + " Elements");
			while (true) {
				if (OutputQueue.getInstance().getCurrent() == null) {
					MagicDrawHelper.generalMessage("Upload complete");
					return true;
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return false;
					}
				}
			}
		} else {
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
	 * @param doc
	 *            Document element
	 * @param recurse
	 *            Select true to generate all views under this element, false to
	 *            generate only the view for the selected element
	 * 
	 */
	public static void generateViews(Element doc, Boolean recurse) {
		GenerateViewPresentationAction gvpa = new GenerateViewPresentationAction(Lists.newArrayList(doc), recurse);
		validationWindow = new MDKValidationWindow(gvpa.updateAction());
	}

	/**
	 * Executes "Generate Views and Commit to MMS" action on the selected
	 * element
	 * 
	 * @param doc
	 *            Selected Document Element.
	 */
	@Deprecated
	public static void generateViewsAndCommitToMMS(Element doc) {
		//OneClickUpdateDoc ocud = new OneClickUpdateDoc(Lists.newArrayList(doc));
		//validationWindow = new MDKValidationWindow(ocud.updateAction());
	}

	/**
	 * Executes "Validate Model" on specified element
	 * 
	 * @param validateTarget
	 *            element that the validation is to be performed upon
	 */
	public static void validateModel(Element validateTarget) {
		Collection<Element> sync = new ArrayList<Element>();
		sync.add(validateTarget);
		ValidateModelRunner modelVal = new ValidateModelRunner(sync);
		ProgressStatusRunner.runWithProgressStatus(modelVal, "Validating Model", true, 0);
		Application.getInstance().getGUILog().log("Validated");
		validationWindow = new MDKValidationWindow(modelVal.getSuite());
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
	 * @param validateTarget
	 *            element that the validation is to be performed upon
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
    
    public static JSONObject getMmsElement(Element e) {
        return ModelValidator.getAlfrescoElement(e);
    }
    
    public static JSONObject getMmsElementByID(String s) {
        return ModelValidator.getAlfrescoElementByID(s);
    }
    
    public static JSONObject getMmsElements(Collection<Element> ce) throws ServerException {
        return ModelValidator.getManyAlfrescoElements(ce, null);
    }
    
    public static JSONObject getMmsElementsByID(Collection<String> cs) throws ServerException {
        return ModelValidator.getManyAlfrescoElementsByID(cs, null);
    }
    
    /**
     * Sends a DELETE request to MMS for the indicated element.
     * 
     * @param deleteTarget
     *          The element you want to delete on the MMS
     * @throws IllegalStateException
     * @throws ServerException
     */
    public static void deleteMmsElement(Element deleteTarget) throws IllegalStateException {
        Project proj = Application.getInstance().getProject();
        if (proj == null)
            throw new IllegalStateException("No project opened.");
        
        String sysmlid = deleteTarget.getID();
        if (sysmlid == null)
            throw new IllegalStateException("Element does not exist in model");

        String url = ExportUtility.getUrlWithWorkspace();
        if (url == null)
            throw new IllegalStateException("Project does not have MMS URL configured.");
        url += "/elements/" + sysmlid;
        
        String response = ExportUtility.delete(url, true);
        if (response == null)
            throw new IllegalStateException("No response received from delete method. Possible malformed url.");
    }

    /**
     * Sends a POST request to MMS with the element JSON, creating or updating the element as appropriate.
     * 
     * @param elementJSON
     *          The JSONObject of the element to create or update. 
     *          Generally acquired through ExportUtility.fillElement(element, null)
     * @throws IllegalStateException
     */
    public static void postMmsElement(JSONObject elementJSON) throws IllegalStateException {
        if (elementJSON == null)
            throw new IllegalStateException("No element json specified to export to MMS");
        
        Project proj = Application.getInstance().getProject();
        if (proj == null)
            throw new IllegalStateException("No project opened.");
        
        String url = ExportUtility.getPostElementsUrl();
        if (url == null)
            throw new IllegalStateException("Project does not have MMS URL configured.");
        
        JSONArray elems = new JSONArray();
        elems.add(elementJSON);
        JSONObject send = new JSONObject();
        send.put("elements", elems);
        
        String response = ExportUtility.send(url, send.toJSONString(), false, true);
        if (response == null)
            throw new IllegalStateException("Invalid send formatting.");
    }
    

	/**
	 * Convenience method for confirmSiteWritePermissions(string, string) to check if a project
	 * is editable by the logged in user. Uses the url and site information stored in the currently
	 * open project.
	 *
	 * @return
	 *          true if the site lists "editable":"true" for the logged in user, false otherwise
	 *          or when no project is open or project lacks url and site specifications
	 */
    public static boolean hasSiteWritePermissions() {
        try {
			Project proj = Application.getInstance().getProject();
			if (proj == null)
				return false;
			String url = ExportUtility.getUrl(proj);
			if (url == null)
				return false;
			String site = ExportUtility.getSite();
			if (site == null)
				return false;
			return ExportUtility.hasSiteWritePermissions(url, site);
        } catch (ServerException se) {
            return false;
        }
    }


    
	/**********************************************************************************
	 *
	 * Model wide MDK Actions
	 * 
	 **********************************************************************************/

    public static void disableSyncTransactionListener(boolean enable) {
        Project project = Application.getInstance().getProject();
        LocalSyncProjectMapping lspm = LocalSyncProjectEventListenerAdapter.getProjectMapping(project);
        if (lspm == null)
            throw new IllegalStateException("LocalSyncProjectMapping is null");
        LocalSyncTransactionCommitListener lstcl = lspm.getLocalSyncTransactionCommitListener();
        if (lstcl == null)
            throw new IllegalStateException("LocalSyncTransactionCommitListener is null");
        lstcl.setDisabled(enable);
    }
    
    /**
     * Updates the MDKValidationWindow object with the latest delta sync results, or sets window to null if there are no results.
     */
    public static MDKValidationWindow getCoordinatedSyncValidationWindow() {
        CoordinatedSyncProjectEventListenerAdapter cspela = MMSSyncPlugin.getInstance().getCoordinatedSyncProjectEventListenerAdapter();
        if (cspela == null)
            validationWindow = null;
        DeltaSyncRunner dsr = cspela.getDeltaSyncRunner();
        if (dsr == null)
            validationWindow = null;
        List<ValidationSuite> vss = dsr.getValidations();
        if (vss.isEmpty())
            validationWindow = null;
        validationWindow = new MDKValidationWindow(vss);
        return validationWindow;
    }
    
	/**
	 * Executes "Generate All Documents and Commit" action
	 */
	@Deprecated
	public static void generateAllDocumentsAndCommitToMMS() {
		generateAllDocuments();
	}

	public static void generateAllDocuments() {
        UpdateAllDocs uad = new UpdateAllDocs();
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
		return ViewEditUtils.isLoginDialogDisabled();
	}

	public static void setLoginDialogDisabled(boolean loginDialogDisabled) {
		ViewEditUtils.setLoginDialogDisabled(loginDialogDisabled);
	}
}