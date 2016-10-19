package gov.nasa.jpl.mbee.mdk.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.tests.MagicDrawTestCase;
import com.nomagic.teamwork.common.users.SessionInfo;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * MDK Test that commits/imports elements using the validation violation
 * export/import commands
 * 
 * @author Tommy Hang, Ryan Oillataguerre, Aaron Black
 *
 */
public abstract class MDKTestCase extends MagicDrawTestCase {
	protected static String testRoot = "";
	protected static String mdRoot = "";
	protected static String protractomrLocation = "/usr/local/bin";
	protected static String credentialsLocation = "ems-config/autotest.properties";
	
	protected static String testName = "";
	protected static String projectName = "";
	
	protected static String testProject = "";
	protected static String referenceProject = "";
	protected static String outputProject = "";
	protected static String logFile = "";
	protected static String branchName = "";
	protected static String baseFile = "";
	protected static String outputFile = "";
	protected static String log = "";

	protected static String teamworkServer = "";
	protected static String teamworkPort = "";
	protected static String teamworkUsername = "";
	protected static String teamworkPassword = "";
	protected static String teamworkProject = "";
	protected static String teamworkBranchName = "";
	protected static String teamworkBranchDescription = "";

	protected static String workspace = "";
	protected static String date = "";
	
	protected static String protractorTestName = "";

	protected static String mmsUsername = "";
	protected static String mmsPassword = "";

	protected static int numRunners = 0;
	protected static String runnerFile = "";

	protected static Element targetElement = null;
	protected static Element targetElement2 = null;
	protected static Element storedElement = null;
	protected static List<Element> targetElements = new ArrayList<>();

	protected static String targetString = "";
	protected static String violationType = "";

	protected static boolean abort = false;
	protected static boolean twlogin = false;
	protected static boolean message = true;
	protected static boolean skipGUILog = false;
	protected static boolean idCheck = false;
	protected static boolean order = false;

	protected static int argIndex = 0;

	public static ArrayList<String> documentList = new ArrayList<>();

	public MDKTestCase(String testMethodToRun, String testName) {
		super(testMethodToRun, testName);
	}

	@Override
	protected void setUpTest() throws Exception {
		super.setUpTest();
        // do setup here
		
		MDKHelper.setLoginDialogDisabled(true);
	}

	@Override
	protected void tearDownTest() throws Exception {
		super.tearDownTest();
        // do tear down here
		
		exportNamedGUILog();
	}

	/**********************************************
	 * 
	 * Helper methods for test case setup or reporting
	 *
	 **********************************************/

	public static void parseArgs(String[] args) {
		// iteration of argIndex is handled by following code to account for
		// variable length arguments with whitespace
		for (argIndex = 0; argIndex < args.length;) {
			if (args[argIndex].startsWith("--")) {
				switch (args[argIndex]) {
				case "--doclist":
					String csvDocumentList = buildArgString(args);
					System.out.print("Document list: ");
					for (String document : csvDocumentList.split(", ")) {
						documentList.add(document);
						System.out.print(document + " ");
					}
					System.out.println();
					break;
				default:
					System.out.println("Invalid flag passed: " + argIndex + " " + args[argIndex]);
				}
			} else if (args[argIndex].startsWith("-")) {
				switch (args[argIndex]) {
				case "-crdlc":
					credentialsLocation = buildArgString(args);
					break;
				case "-d":
					date = buildArgString(args);
					break;
				case "-mdrt":
					mdRoot = buildArgString(args);
					if (mdRoot.equals("/"))
						mdRoot = "";
					break;
				case "-mmspsd":
					mmsPassword = buildArgString(args);
					break;
				case "-mmsusr":
					mmsUsername = buildArgString(args);
					break;
				case "-ptrlc":
//					protractorLocation = buildArgString(args);
//					if (protractorLocation.equals("/"))
//						protractorLocation = "";					
					break;
				case "-ptrnm":
					protractorTestName = buildArgString(args);
					break;
				case "-runner":
					runnerFile = buildArgString(args);
					break;
				case "-tstnm":
					testName = buildArgString(args);
					break;
				case "-tstrt":
					testRoot = buildArgString(args);
					if (testRoot.equals("/"))
						testRoot = "";
					break;
				case "-twprj":
					teamworkProject = buildArgString(args);
					break;
				case "-twsrv":
					teamworkServer = buildArgString(args);
					break;
				case "-twprt":
					teamworkPort = buildArgString(args);
					break;
				case "-twusr":
					teamworkUsername = buildArgString(args);
					break;
				case "-twpsd":
					teamworkPassword = buildArgString(args);
					break;
				case "-wkspc":
					workspace = buildArgString(args);
					break;
				default:
					System.out.println("Invalid flag passed: " + argIndex + " " + args[argIndex++]);
				}
			} else {
				System.out.println("Invalid parameter passed: " + argIndex + " " + args[argIndex]);
			}
			argIndex++;
		}
	}
	
	protected static void validateTestRoot() {
	       if (testRoot.equals("")) {
	            if (System.getenv().containsKey("WORKSPACE"))
	                testRoot = System.getenv().get("WORKSPACE");
	            else
	                testRoot = Paths.get("").toAbsolutePath().toString();
	        }
	}

	private static String buildArgString(String[] args) {
		StringBuilder spacedArgument = new StringBuilder("");
		while ((argIndex + 1) < args.length && !args[argIndex + 1].startsWith("-")) {
			spacedArgument.append(args[++argIndex]);
			spacedArgument.append(" ");
		}
		spacedArgument.setLength(spacedArgument.length() - 1);
		return spacedArgument.toString();
	}

	protected static void loadCredentials() {
		loadCredentials("");
	}
	
	protected static void loadCredentials(String append) {
	    validateTestRoot();
		String credsLoc = testRoot + credentialsLocation;
		if (!Paths.get(credsLoc).toFile().exists())
			credsLoc = credentialsLocation;
		if (!Paths.get(credsLoc).toFile().exists())
			credsLoc = "/opt/local/" + credentialsLocation;
		if (!Paths.get(credsLoc).toFile().exists())
			System.out.println("Unable to find credentials file");
		Properties prop = new Properties();
		try (InputStream input = new FileInputStream(credsLoc);
		) {
			// load a properties file
			prop.load(input);
			// set appropriate fields
			if (teamworkServer.equals("")) {
				teamworkServer = prop.getProperty("tw.url");
				if (teamworkServer.indexOf("//") != -1)
					teamworkServer = teamworkServer.substring(teamworkServer.indexOf("//") + 2);
				if (teamworkServer.lastIndexOf(':') != -1)
					teamworkServer = teamworkServer.substring(0, teamworkServer.lastIndexOf(':'));
			}
			if (teamworkPort.equals(""))
				teamworkPort = prop.getProperty("tw.port");
			if (teamworkUsername.equals("")) {
				if (prop.containsKey("user.name" + append)){
					teamworkUsername = prop.getProperty("user.name" + append);
					teamworkPassword = prop.getProperty("user.pass" + append);
				}
				else {
					teamworkUsername = prop.getProperty("user.name") + append;
					teamworkPassword = prop.getProperty("user.pass") + append;
				}
			}
			if (mmsUsername.equals("")) {
				mmsUsername = teamworkUsername;
				mmsPassword = teamworkPassword;
			}
		} catch (IOException ioe) {
			System.out.println("IOException mms: " + ioe.toString());
		}
	}

	public static void printJunitResults(Result results) {
		System.out.println("Ran " + results.getRunCount() + " Tests");
		System.out.println(results.getRunCount() - results.getFailureCount() + " Tests Passed");
		if (results.getFailureCount() != 0) {
			System.out.println(results.getFailureCount() + " FAILURES!!!");
		}
		for (Failure fails : results.getFailures()) {
			if (fails.getDescription() != null)
				System.out.println("Description: " + fails.getDescription());
			if (fails.getMessage() != null)
				System.out.println("Message: " + fails.getMessage());
			if (fails.getException() != null)
				System.out.println("Exception: " + fails.getException());
			if (fails.getTrace() != null)
				System.out.println("Trace: " + fails.getTrace());
		}
	}

	/**
	 * Exports the MagicDraw gui console log to a file, incorporating the
	 * testName, located in the working directory or specified test root
	 * directory. As this text is already html formatted, created file is saved
	 * as an html file and can be displayed in browsers immediately.
	 */

	public void exportNamedGUILog() {
		if (skipGUILog)
			return;
		String guiLog = Application.getInstance().getGUILog().getLoggedMessages();
		try {
			File logDir = new File (testRoot + "/logs/");
			if (!logDir.exists() && !logDir.isDirectory()) {
				logDir.mkdir();
			}
			PrintWriter writer = new PrintWriter(testRoot + "/logs/" + testName + "GUILog.html", "UTF-8");
			writer.println(guiLog);
			writer.close();
		} catch (Exception e) {
			System.out.println("exportNamedGUILog error: Save GUI Log to file object failed: " + e.toString());
		}
	}
	
	/**
	 * Deletes all local elements in MD and then triggers a deletion of all
	 * elements in MMS. Generally used to wipe a site after a test, in
	 * preparataion for a new test. 
	 * 
	 * DO NOT SAVE THE REFERENCE PROJECT AFTER THIS OPERATION
	 */
	public void cleanMMS() {
		System.out.println("Executing *cleanMMS*");
		deleteLocalMDElements();
		validateModelRoot();
		deleteAllMMSElementsNotFoundInMD();
		uploadWait();
	}

	/**
	 * 
	 */
	public void resetTest() {
		System.out.println("Executing *resetTest*");
		deleteLocalMDElements();
		validateModelRoot();
		deleteAllMMSElementsNotFoundInMD();
		uploadWait();
		closeProject();
		openProject();
		mmsDirectLogin();
		validateModelRoot();
		commitAllMDChangesToMMS();
		uploadWait();
		closeProject();
	}

	/**
	 * 
	 */
	@Deprecated
	public void resetTeamworkTest() {
		System.out.println("Executing resetTeamworkTest");
		teamworkOpenProject();
		teamworkLockModel();
		mmsDirectLogin();
		deleteLocalMDElements();
		updateAndCommitWithDeletesToMMS();
		uploadWait();
		teamworkUnlockModelAndDiscard();
		closeProject();
		teamworkOpenProject();
		mmsDirectLogin();
		validateModelRoot();
		commitAllMDChangesToMMS();
		uploadWait();
		teamworkCommit();
		teamworkUnlockModelAndDiscard();
		closeProject();
		teamworkLogout();
	}

	public void wait5seconds() {
		try {
			Thread.sleep(5000);
		}
		catch (Exception e) {}
	}

	/**********************************************
	 * 
	 * Test case methods
	 *
	 **********************************************/

	/**
	 * Accepts all validated changes from MMS into MD
	 */	
	public void acceptAllMMSChangesIntoMD() {
		System.out.println("Executing acceptAllMMSChangesIntoMD");
		try {
			MDKHelper.getManualValidationWindow().acceptAllMMSChangesIntoMD();
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}
	}
	
	/**
	 * Accepts validations from MMS into MD for the elements contained in targetElements
	 * of the type specified in violationType
	 */
	public void acceptSpecificTypedMMSChangesIntoMD() {
		System.out.println("Executing acceptSpecificMMSChangesIntoMD");
		try {
			assertTrue(MDKHelper.getManualValidationWindow().acceptSpecificTypeMDChangesToMMS(violationType, targetElements).isEmpty());
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}

	}

	/**
	 * Accepts validations from MMS into MD of type specified in violationType
	 */
	public void acceptTypedMMSChangesIntoMD() {
		System.out.println("Executing acceptSpecifiedMMSChangesIntoMMD for " + violationType);
		try {
			MDKHelper.getManualValidationWindow().acceptMMSChangesIntoMD(violationType);
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}
	}

	/**
	 * Checks if the project has been initialized. Passes test if project has been.
	 */
	public void checkIfInitialized() {
		System.out.println("Executing checkIfInitialized");
		try {
			assertTrue(MDKHelper.checkInitialization());
		}
		catch (Exception e) {
			fail("FAILURE!!! Exception thrown: " + e.toString());
		}
	}

	/**
	 * Checks if the project has been initialized. Passes test if project has NOT been.
	 */
	public void checkIfNotInitialized() {
		System.out.println("Executing checkIfNotInitialized");
		try {
			assertFalse(MDKHelper.checkInitialization());
		}
		catch (Exception e) {
			fail("FAILURE!!! Exception thrown: " + e.toString());
		}
	}

	/**
	 * Closes a local MD project
	 */
	public void closeProject() {
		System.out.println("Executing closeProject");
		super.closeProject(Application.getInstance().getProject());
	}

	/**
	 * Commits all validated changes from MD to MMS
	 */	
	public void commitAllMDChangesToMMS() {
		System.out.println("Executing commitAllMDChangesToMMS");
		try {
			MDKHelper.getManualValidationWindow().commitAllMDChangesToMMS();
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}
	}

	/**
	 * Commits validations from MD to MMS for the elements contained in targetElements
	 * of the type specified in violationType
	 */
	public void commitSpecificTypedMMSChangesIntoMD() {
		System.out.println("Executing commitSpecificTypedMMSChangesIntoMD");
		try {
			assertTrue(MDKHelper.getManualValidationWindow().commitSpecificTypeMDChangesToMMS(violationType, targetElements).isEmpty());
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}

	}

	/**
	 * Commits validations from MD to MMS of type specified in violationType
	 */
	public void commitTypedMDChangesToMMS() {
		System.out.println("Executing commitSpecifiedMDChangesToMMS for " + violationType);
		try {
			MDKHelper.getManualValidationWindow().commitMDChangesToMMS(violationType);
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}
	}

	/**
	 * Confirms that the comment text in targetElement equals the text in targetString
	 */
	public void compareElementComments() {
		System.out.println("Executing compareElementComments");
//		System.out.println(Utils.stripHtmlWrapper(MagicDrawHelper.getElementDocumentation(targetElement)) + " | " + targetString);
		assertTrue(MagicDrawHelper.getElementDocumentation(targetElement).equals(targetString));
	}

	/**
	 * Confirms that the property value text in targetElement equals the text in targetString.
	 * Generally, targetElement needs to be found by first finding the element and then finding its
	 * property.
	 */
	public void comparePropertyValue() {
		System.out.println("Executing comparePropertyValue");
		assertTrue(MagicDrawHelper.getPropertyValue(targetElement).equals(targetString));
	}
	
	/**
	 * Confirms that the element stored in targetElement is referenced by a validation of type
	 * specified in violationType.
	 */
	public void confirmElementViolation() {
		System.out.println("Executing confirmElementViolation");
		boolean found = false;
		for (ValidationRuleViolation vrv : MDKHelper.getManualValidationWindow().getPooledValidations(violationType)) {
			found = (vrv.getElement().equals(targetElement) || vrv.getElement().getOwner().equals(targetElement));
			if (found) 
				break;
		}
		assertTrue(found);
	}

	/**
	 * Confirms that the element stored in targetElement is referenced by a validation of type
	 * specified in violationType.
	 */
	public void confirmNoElementViolation() {
		System.out.println("Executing confirmNoElementViolation");
		boolean found = false;
		for (ValidationRuleViolation vrv : MDKHelper.getManualValidationWindow().getPooledValidations(violationType)) {
			found = (vrv.getElement().equals(targetElement) || vrv.getElement().getOwner().equals(targetElement));
			if (found) 
				break;
		}
		assertFalse(found);
	}

	/**
	 * Confirms that the project has been initialized, attempting to initialize if it has not been.
	 * Passes test as long it is or could be initialized.
	 */
	public void confirmInitialization() {
		System.out.println("Executing confirmInitialization");
		try {
			assertTrue(MDKHelper.confirmInitialization());
		}
		catch (Exception e) {
			fail("FAILURE!!! Exception thrown: " + e.toString());
		}		
	}
	
	public void confirmMMSPermissions() {
        System.out.println("executing confirmMMSPermissions");
        if (!MDKHelper.loginToMMS(teamworkUsername, teamworkPassword)) {
            fail("FAILURE: Invalid credentials");
        }
        if (!MDKHelper.hasSiteEditPermission()) {
            fail("FAILURE: No project write permissions");
        }
	}

	/**
	 * Copies targetElement into targetElement2
	 */	
	public void copyElement() {
		System.out.println("Executing copyElement");
		storedElement = MagicDrawHelper.copyElementToTarget(targetElement, targetElement2);
		assertNotNull(storedElement);
	}

	/**
	 * Creates the validated elements found in MMS but not on MD
	 */	
	public void createAllMMSElementsNotFoundInMD() {
		System.out.println("Executing createAllMMSElementsNotFoundInMD");
		try {
			MDKHelper.getManualValidationWindow().createAllMMSElementsNotFoundInMD();
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}
	}

	/**
	 * Deletes the validated elements found in MMS but not MD
	 */	
	public void deleteAllMDElementsNotFoundOnMMS() {
		System.out.println("Executing deleteAllMDElementsNotFoundOnMMS");
		try {
			MDKHelper.getManualValidationWindow().deleteAllMDElementsNotFoundOnMMS();
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}
	}

	/**
	 * Deletes the validated elements from MMS that are not found in MD.
	 */	
	public void deleteAllMMSElementsNotFoundInMD() {
		System.out.println("Executing deleteAllMMSElementsNotFoundInMD");
		try {
			MDKHelper.getManualValidationWindow().deleteAllMMSElementsNotFoundInMD();
		}
		catch (Exception e) {

		}
	}

	/**
	 * Deletes targetElement from MD, and all children
	 */
	public void deleteElementOnMD() {
		System.out.println("Executing deleteElementOnMD");
		MagicDrawHelper.createSession();
		try {
			MagicDrawHelper.deleteMDElement(targetElement);
		} catch (Exception e) {
		    MagicDrawHelper.cancelSession();
			fail ("FAILURE: Unable to delete " + targetElement.getHumanName());
		}
		MagicDrawHelper.closeSession();
	}

	/**
	 * Deletes all editable container children of root, excluding modules and
	 * other problematic packages
	 */
	public void deleteLocalMDElements() {
		System.out.println("Executing deleteLocalMDElements");
        MagicDrawHelper.createSession();
        try {
            MagicDrawHelper.deleteLocalMDElements();
        } catch (Exception e) {
            MagicDrawHelper.cancelSession();
            fail ("FAILURE: Unable to delete " + targetElement.getHumanName());
        }
        MagicDrawHelper.closeSession();
	}

	/**
	 * Creates the validated elements found on MD but not MMS
	 */	
	public void exportAllMDElementsToMMS() {
		System.out.println("Executing exportAllMDElementsToMMS");
		try {
			MDKHelper.getManualValidationWindow().exportAllMDElementsToMMS();
		}
		catch (Exception e) {
			fail("Exception occurred during validation commit: " + e.toString());
		}
	}

	/**
	 * Executes the "Generate All Documents and Commit to MMS" action
	 */
	public void generateAllDocumentsAndCommitToMMS() {
		System.out.println("Executing generateAllDocumentsAndCommitToMMS");
		MDKHelper.generateAllDocumentsAndCommitToMMS();
	}

	/**
	 * Executes the "Generate View" action on targetElement
	 */
	public void generateView() {
		System.out.println("Executing generateViews");
		MDKHelper.generateViews(targetElement, false);
	}

	/**
	 * Executes the "Generate Views" action on targetElement
	 */
	public void generateViews() {
		System.out.println("Executing generateViews");
		MDKHelper.generateViews(targetElement, true);
	}
	
	public void generateAllViews() {
		System.out.println("Executing generateAllViews");
		List<Element> documents = ElementFinder.findElementsByType("Document");
		if (documents == null) {
			return;
		}
		for (Element document : documents) {
			MDKHelper.generateViews(document, false);
		}
	}

	@Deprecated
	/**
	 * Executes the "Generate Views and Commit to MMS" action on targetElement
	 *   Not compatible with MDK 2.4+
	 */
	public void generateViewsAndCommit() {
		System.out.println("Executing generateViewsAndCommit");
//		MDKHelper.generateViewsAndCommitToMMS(targetElement);
		
		//TODO
		// remove this after migrating old tests to new 2.4 compatible structures
		if (ProjectUtilities.isFromTeamworkServer(Application.getInstance().getProject().getPrimaryProject())) {
		    teamworkCommit();
		}
		else {
		    saveUpdatedProject();
		}
		MDKHelper.generateViews(targetElement, true);
		MDKHelper.mmsUploadWait();
	}
	
	@Deprecated
	/**
	 * Logs in to MMS using mmsUsername and mmsPassword
	 *   not compatible with MDK 2.4+
	 */
	public void mmsDirectLogin() {
		System.out.println("Executing mmsDirectLogin");
		assertTrue(MDKHelper.loginToMMS(mmsUsername, mmsPassword));
	}

	@Deprecated
	/**
	 * Logs in to MMS using mmsUsername and mmsPassword, interacting with the
	 * pop-up window
	 */
	public void mmsOldLogin() {
//		System.out.println("Executing mmsLogin");
//		MDKHelper.mdkOldLogin(mmsUsername, mmsPassword);
	}
	
	/**
	 * Sets the documentation of targetElement to targetString. Does not attempt to lock element first.
	 */
	public void modifyElementDocumentation() {
		System.out.println("Executing modifyDocumentation");
		MagicDrawHelper.createSession();
		MagicDrawHelper.setElementDocumentation(targetElement, targetString);
		MagicDrawHelper.closeSession();
	}

	/**
	 * Opens local MD project stored in testProject
	 */
	public void openProject() {
		System.out.println("Executing openProject");
		//		setDoNotUseSilentMode(true);
		assertNotNull(super.openProject(testProject, true));
	}

	/**
	 * Renames element stored in targetElement to name stored in targetString 
	 */
	public void renameElement() {
		System.out.println("Executing renameStoredElement");
		if (targetElement instanceof NamedElement)
			MagicDrawHelper.setElementName((NamedElement)targetElement, targetString);
	}

	/**
	 * Saves local MD project to file indicated by newProject.
	 */
	public void saveProjectOutput() {
		System.out.println("Executing saveProjectOutput");
		File save = new File(outputProject);
		try {
			super.saveProject(Application.getInstance().getProject(), save);
		} catch (Exception e) {
			fail("FAILURE: Exception thrown when attempting to save project to indicated file: " + e.toString());
		}
	}

	/**
	 * Saves local MD project as xml for reference to file indicated by newProject.
	 */
	public void saveProjectReference() {
		System.out.println("Executing saveProjectReference");
		File save = new File(referenceProject);
		try {
			super.saveProject(Application.getInstance().getProject(), save);
		} catch (Exception e) {
			fail("FAILURE: Exception thrown when attempting to save project to indicated reference file: " + e.toString());
		}
	}

	/**
	 * Saves updated project to original file
	 */
	public void saveUpdatedProject() {
		System.out.println("Executing saveUpdatedProject");
		File save = new File(testProject);
		try {
			super.saveProject(Application.getInstance().getProject(), save);
		} catch (Exception e) {
			fail("FAILURE: Exception thrown when attempting to save project to indicated reference file: " + e.toString());
		}
		if (!MDKHelper.mmsUploadWait()) {
		    System.out.println("Did not find elements in the output queue to wait on");
		}
	}
	
	/**
	 * Pre-specifies the credentials to use to log on to MMS when the project is opened.
	 */
	
	public void setMMSCredentials() {
	    System.out.println("Executing setMMSCredentials");
	    MDKHelper.setMMSLoginCredentials(mmsUsername, mmsPassword);
	}

	/**
	 * Commits Teamwork project to server, with commit message stored in targetString
	 * Keeps locks
	 */
	public void teamworkCommit() {
		System.out.println("Executing teamworkCommit");
		boolean success = TeamworkUtils.commitProject(Application.getInstance().getProject(), targetString);
		assertTrue(success);
	}

	/**
	 * Commits Teamwork project to server, with commit message stored in targetString
	 * 
	 * NOTE - Only releases locks on NON-NEW elements due to a limitation with the MD API
	 * Recommend that you call teamworkUnlockModelAndDiscard after this call to ensure complete
	 * remove of locks.
	 */
	public void teamworkCommitReleaseLocks() {
		System.out.println("Executing teamworkCommitReleaseLocks");
		assertTrue(MagicDrawHelper.teamworkCommitReleaseLocks(teamworkUsername, targetString));
	}

	/**
	 * Creates a Teamwork branch of a project at teamworkBranchName with the description
	 * stored in teamworkBranchDescription
	 */
	public void teamworkCreateBranch() {	
		try {
			ProjectDescriptor projectDescriptor = TeamworkUtils.getRemoteProjectDescriptor(TeamworkUtils.getRemoteProjectIDByQualifiedName(teamworkProject));
			TeamworkUtils.branchProject(projectDescriptor, null, teamworkBranchName, teamworkBranchDescription);
		}
		catch (RemoteException e) {
			fail("FAILURE: Creating a teamwork branch threw the following exception: " + e.toString());
		}
	}

	/**
	 * Executes "Lock Elements for Edit" on targetElement 
	 */
	public void teamworkLockElement() {
		System.out.println("Executing teamworkLockElementRecursively");
		boolean success = TeamworkUtils.lockElement(Application.getInstance().getProject(), targetElement, false);
		assertTrue(success);
	}

	/**
	 * Executes "Lock Elements for Edit Recursively" on targetElement 
	 */
	public void teamworkLockElementRecursively() {
		System.out.println("Executing teamworkLockElementRecursively");
		boolean success = TeamworkUtils.lockElement(Application.getInstance().getProject(), targetElement, true);
		assertTrue(success);
	}

	/**
	 * Executes "Lock Elements for Edit Recursively" on the model root
	 */
	public void teamworkLockModel() {
		System.out.println("Executing teamworkLockModel");
		boolean success = TeamworkUtils.lockElement(Application.getInstance().getProject(), ElementFinder.getModelRoot(),
				true);
		assertTrue(success);
	}

	/**
	 * Logs in to Teamwork server at teamworkServer:teamworkPort using teamworkUsername and teamworkPassword
	 */
	public void teamworkLogin() {
		System.out.println("Executing teamworkLogin");
		setDoNotUseSilentMode(true);
		SessionInfo session = TeamworkUtils.loginWithSession(teamworkServer, Integer.parseInt(teamworkPort),
				teamworkUsername, teamworkPassword);
		if (session == null) {
			abort = true;
			Application.getInstance().getGUILog().log("FAILURE: Unable to log in to Teamwork.");
		}
		else
			twlogin = true;
	}

	/**
	 * Logs out of the logged in teamwork account
	 */
	public void teamworkLogout() {
		System.out.println("Executing teamworkLogout");
		TeamworkUtils.logout();
		twlogin = false;
	}

	/**
	 * Opens Teamwork MD project with name stored in teamworkProject 
	 */
	public void teamworkOpenProject() {
		System.out.println("Executing openTeamworkProject");
		try {
			ProjectDescriptor projectDescriptor = TeamworkUtils
					.getRemoteProjectDescriptorByQualifiedName(teamworkProject);
			if (projectDescriptor == null) {
				abort = true;
				fail("FAILURE: Unable to open Teamwork project");
			}
			ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
			projectsManager.loadProject(projectDescriptor, true);
		} catch (RemoteException e) {
			abort = true;
			fail("FAILURE: Exception thrown when attempting to open indicated teamwork project: " + e.toString());
		}
	}

	/**
	 * Opens Teamwork MD project branch with name stored in teamworkProject and branch stored in teamworkBranchName 
	 */
	public void teamworkOpenProjectBranch() {
		System.out.println("Executing openTeamworkProject");
		try {
			String qualifiedName = TeamworkUtils.generateProjectQualifiedName(teamworkProject,
					new String[] { teamworkBranchName });
			ProjectDescriptor projectDescriptor = TeamworkUtils
					.getRemoteProjectDescriptorByQualifiedName(qualifiedName);

			if (projectDescriptor == null) {
				abort = true;
				fail("FAILURE: Unable to open Teamwork project");
			}
			ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
			projectsManager.loadProject(projectDescriptor, true);
		} catch (RemoteException e) {
			abort = true;
			fail("FAILURE: Exception thrown when attempting to open indicated teamwork project: " + e.toString());
		}
	}

	@Deprecated
	/**
	 *  Use teamworkCommitReleaseLocks instead, as that will include a commit message.
	 * 
	 *  Releases all locks in the model and commits changes
	 */
	public void teamworkUnlockModelAndCommit() {
		System.out.println("Executing teamworkUnlockModelAndCommit");
		TeamworkUtils.unlockElement(Application.getInstance().getProject(), ElementFinder.getModelRoot(), true, false,
				true);
	}

	/**
	 * Releases all locks in the model and discards changes
	 */
	public void teamworkUnlockElementsAndDiscard() {
		System.out.println("Executing teamworkUnlockElementsAndDiscard");
		List<Element> lockedElements = new ArrayList<Element>(); 
		lockedElements.addAll(TeamworkUtils.getLockedElement(Application.getInstance().getProject(), teamworkUsername));
		for (Element lock : lockedElements) {
			System.out.println("+" + lock.getHumanName());
			TeamworkUtils.unlockElement(Application.getInstance().getProject(), lock, false, true);
		}
	}

	/**
	 * Releases all locks in the model and discards changes
	 */
	public void teamworkUnlockModelAndDiscard() {
		System.out.println("Executing teamworkUnlockModelAndDiscard");
		TeamworkUtils.unlockElement(Application.getInstance().getProject(), ElementFinder.getModelRoot(), true, true, true);
	}
	
	/**
	 * Executes "Update Project" action
	 */
	public void teamworkUpdateProject() {
		System.out.println("Executing teamworkUpdateProject");
		TeamworkUtils.updateProject(Application.getInstance().getProject());
	}
	
	/**
	 * Executes standard teamwork exist functions
	 */
	public void teamworkWrapUp() {
		System.out.println("Executing *teamworkWrapUp*");
		skipGUILog = true;
		if (targetString.equals(""))
			targetString = "Automated teamwork unlock";
		teamworkCommitReleaseLocks();
		closeProject();
		teamworkLogout();
	}

	/**
	 * Executes "MMS -> Update and Commit -> Update From MMS" action. 
	 */
	public void updateFromMMS() {
		System.out.println("Executing updateFromMMS");
		try {
			MDKHelper.updateFromMMS();
			MDKHelper.mmsUploadWait();
		} catch (Exception e) {
			fail("FAILURE: Update from MMS failed: " + e.toString());
		}
	}

	/**
	 * Confirms that changes have been imported from the MMS
	 */

	//	public void updateFromMMSConfirmChanges() {
	//		System.out.println("Executing updateFromMMSConfirmChanges");
	//		try {
	//			assertTrue(MDKHelper.updateFromMMSConfirmChanges());
	//		} catch (Exception e) {
	//			fail("FAILURE: Update from MMS failed: " + e.toString());
	//		}
	//	}

	/**
	 * Confirms that no changes have been imported from the MMS
	 */

	//	public void updateFromMMSConfirmNoChanges() {
	//		System.out.println("Executing updateFromMMSConfirmNoChanges");
	//		try {
	//			assertFalse(MDKHelper.updateFromMMSConfirmChanges());
	//		} catch (Exception e) {
	//			fail("FAILURE: Update from MMS failed: " + e.toString());
	//		}
	//	}

	/**
	 * Executes "MMS -> Update and Commit -> Commit With Deletes to MMS" action.
	 */
	public void updateAndCommitWithDeletesToMMS() {
		System.out.println("Executing updateAndCommitWithDeletesToMMS");
		try {
			MDKHelper.updateAndCommitWithDeletesToMMS();
			MDKHelper.mmsUploadWait();
		} catch (Exception e) {
			fail("FAILURE: Update and Commit to MMS failed: " + e.toString());
		}
	}

	/**
	 * Executes "MMS -> Update and Commit -> Commit to MMS" action.
	 */
	public void updateAndCommitToMMS() {
		System.out.println("Executing updateAndCommitToMMS");
		try {
			MDKHelper.updateAndCommitToMMS();
			MDKHelper.mmsUploadWait();
		} catch (Exception e) {
			fail("FAILURE: Update and Commit to MMS failed: " + e.toString());
		}
	}

	/**
	 * Causes the test runner to wait until the output queue has been cleared
	 * before continuing on with subsequent tests. Not a failable test.
	 */
	public void uploadWait() {
		System.out.println("Executing uploadWait");
		Application.getInstance().getGUILog().log("Waiting for Upload");
		MDKHelper.mmsUploadWait();
	}
	
	/**
	 * Executes the "Validate Model" action on targetElement
	 */
	public void validateModelAtElement() {
		System.out.println("Executing validateModelAtElement");
		MDKHelper.validateModel(targetElement);
	}

	/**
	 * Executes "Validate Model" at model root
	 */
	public void validateModelRoot() {
		System.out.println("Executing validateModel");
		MDKHelper.validateModelRoot();
	}

	/**
	 * Executes "Validate View Hierarchy" at element stored in targetElement
	 */
	public void validateViewHierarchy() {
		System.out.println("Executing validateViewHierarchy");
		//		validationWindow = new MDKValidationWindow(MDKHelper.validateViewHierarchy(MDKHelper.findElement(targetType1, targetName1)));
		MDKHelper.validateViewHierarchy(targetElement);

	}

	/**********************************************
	 * 
	 * Methods for examining test MMS behaviors
	 *
	 **********************************************/

	public void wait5() {
		System.out.println("Executing wait5");
		for (int i = 0; i < 500; i++) {
    		try {
    			Thread.sleep(10);
    		}
    		catch (Exception ignored) {
    		}
		}
	}
	
	public void listValidations() {
		System.out.println("Executing listValidations");
		for (ValidationSuite vs : MDKHelper.getManualValidationWindow().getValidations()) {
			if (vs == null) {
				System.out.println("Null suite.");
			}
			else if (vs.hasErrors()) {
				for (ValidationRule vr : vs.getValidationRules()) {
					if (!vr.getViolations().isEmpty()) {
						System.out.println(vr.getName() + " : " + vr.getDescription());
						for (ValidationRuleViolation vrv : vr.getViolations()) {
							System.out.println("  " + (vrv.getElement() != null ? vrv.getElement().getHumanName() : "null") + " : " + vrv.getComment());
						}
					}
				}
			}
		}
	}

	public void listPooledValidations() {
		try {
			MDKHelper.getManualValidationWindow().listPooledViolations();
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}