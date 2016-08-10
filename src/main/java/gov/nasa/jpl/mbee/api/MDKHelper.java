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
import gov.nasa.jpl.mbee.actions.docgen.GenerateViewPresentationAction;
import gov.nasa.jpl.mbee.actions.ems.*;
import gov.nasa.jpl.mbee.ems.ValidateModelRunner;
import gov.nasa.jpl.mbee.ems.ValidateViewRunner;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter;
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
						// TODO Auto-generated catch block
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
	 * Model wide MDK Actions
	 * 
	 **********************************************************************************/

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

}