package gov.nasa.jpl.mbee.mdk.test.framework;

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
 * @author Tommy Hang, Aaron Black, Ryan Oillataguerre
 *
 */
@Deprecated
public abstract class MDKTestCase extends MagicDrawTestCase {
    protected static String testRoot = "";
    protected static String mdRoot = "";
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

    protected static Element targetElement = null;
    protected static Element targetElement2 = null;
    protected static Element storedElement = null;
    protected static List<Element> targetElements = new ArrayList<>();

    protected static String targetString = "";
    protected static String violationType = "";

    protected static boolean abort = false;
    protected static boolean twlogin = false;
    protected static boolean skipGUILog = false;

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

    public static void parseArgs(String[] args) {

    }

    /**********************************************
     *
     * Helper methods for test case setup or reporting
     *
     **********************************************/

    protected static void validateTestRoot() {
           if (testRoot.equals("")) {
                if (System.getenv().containsKey("WORKSPACE"))
                    testRoot = System.getenv().get("WORKSPACE");
                else
                    testRoot = Paths.get("").toAbsolutePath().toString();
            }
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

    /**********************************************
     *
     * Methods for examining test MMS behaviors
     *
     **********************************************/

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