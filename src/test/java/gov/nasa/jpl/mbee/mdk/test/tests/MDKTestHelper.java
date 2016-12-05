package gov.nasa.jpl.mbee.mdk.test.tests;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.teamwork.common.users.SessionInfo;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * Helper API for MDK tests
 *
 * @author Tommy Hang, Aaron Black, Ryan Oillataguerre
 *
 */
public abstract class MDKTestHelper {

    /**********************************************
     *
     * Helper methods for test case setup or reporting
     *
     **********************************************/

    public static String setMmsCredentials(String location, String append) throws IOException {
        String mmsUsername = "";
        String mmsPassword = "";
        InputStream propertesFileStream = MDKTestHelper.class.getResourceAsStream(location);
        if (propertesFileStream == null) {
            throw new IOException(location + " is not a file and cannot be loaded.");
        }
        Properties prop = new Properties();
        // load a properties file
        prop.load(propertesFileStream);
        // set appropriate fields
        if (prop.containsKey("user.name" + append)) {
            mmsUsername = prop.getProperty("user.name" + append);
            mmsPassword = prop.getProperty("user.pass" + append);
        } else {
            mmsUsername = prop.getProperty("user.name") + append;
            mmsPassword = prop.getProperty("user.pass") + append;
        }
        if (mmsUsername.isEmpty() || mmsPassword.isEmpty()) {
            throw new IOException(Paths.get(location).toString() + " did not contain a user.name or user.pass field.");
        }
        MDKHelper.setMMSLoginCredentials(mmsUsername, mmsPassword);

        //
        return mmsUsername;
    }

    /**
     * Logs in to Teamwork server at teamworkServer:teamworkPort using teamworkUsername and teamworkPassword
     */
    public static String setMmsAndTeamworkCredentials(String location, String append) throws IOException {
        String teamworkServer;
        String teamworkPort;
        String teamworkUsername;
        String teamworkPassword;

        if (!Paths.get(location).toFile().exists()) {
            throw new IOException(Paths.get(location).toString() + " is not a file and cannot be loaded.");
        }
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(location);
        ) {
            if (prop.containsKey("user.name" + append)){
                teamworkUsername = prop.getProperty("user.name" + append);
                teamworkPassword = prop.getProperty("user.pass" + append);
            }
            else {
                teamworkUsername = prop.getProperty("user.name") + append;
                teamworkPassword = prop.getProperty("user.pass") + append;
            }
            teamworkServer = prop.getProperty("tw.url");
            if (teamworkServer.indexOf("//") != -1)
                teamworkServer = teamworkServer.substring(teamworkServer.indexOf("//") + 2);
            if (teamworkServer.lastIndexOf(':') != -1)
                teamworkServer = teamworkServer.substring(0, teamworkServer.lastIndexOf(':'));
            teamworkPort = prop.getProperty("tw.port");

        } catch (IOException ioe) {
            // only using try/catch formulation so we can utilize auto-closeable
            throw ioe;
        }
        if (teamworkUsername.isEmpty() || teamworkPassword.isEmpty()) {
            throw new IOException(Paths.get(location).toString() + " did not contain a user.name or user.pass field.");
        }
        MDKHelper.setMMSLoginCredentials(teamworkUsername, teamworkPassword);

        SessionInfo session = TeamworkUtils.loginWithSession(teamworkServer, Integer.parseInt(teamworkPort),
                teamworkUsername, teamworkPassword);
        if (session == null) {
            throw new IOException(Paths.get(location).toString() + " did not provide valid credentials for Teamwork.");
        }
        return teamworkUsername;
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

    public static void exportNamedGUILog(String location, String name) {
        String guiLog = Application.getInstance().getGUILog().getLoggedMessages();
        try {
            File logDir = new File (location);
            if (!logDir.exists() && !logDir.isDirectory()) {
                logDir.mkdir();
            }
            PrintWriter writer = new PrintWriter(name, "UTF-8");
            writer.println(guiLog);
            writer.close();
        } catch (Exception e) {
            System.out.println("exportNamedGUILog error: Save GUI Log to file object failed: " + e.toString());
        }
    }

    public static void waitXSeconds(int s) {
        try {
            Thread.sleep(s * 1000);
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
    public static boolean confirmManualValidationElementViolation(String violationType, Element targetElement) {
        System.out.println("Executing confirmElementViolation");
        boolean found = false;
        for (ValidationRuleViolation vrv : MDKHelper.getValidationWindow().getPooledValidations(violationType)) {
            found = (vrv.getElement().equals(targetElement) || vrv.getElement().getOwner().equals(targetElement));
            if (found)
                break;
        }
        return found;
    }

    /**
     * Confirms that the element stored in targetElement is referenced by a validation of type
     * specified in violationType.
     */
    public static boolean confirmCoordinatedSyncElementViolation(String violationType, Element targetElement) {
        System.out.println("Executing confirmElementViolation");
        boolean found = false;
        MDKHelper.loadCoordinatedSyncValidations();
        for (ValidationRuleViolation vrv : MDKHelper.getValidationWindow().getPooledValidations(violationType)) {
            found = (vrv.getElement().equals(targetElement) || vrv.getElement().getOwner().equals(targetElement));
            if (found)
                break;
        }
        return found;
    }

    /**
     * Opens Teamwork MD project with name stored in teamworkProject
     */
    public static boolean teamworkOpenProject(String teamworkProject) {
        System.out.println("Executing openTeamworkProject");
        try {
            ProjectDescriptor projectDescriptor = TeamworkUtils
                    .getRemoteProjectDescriptorByQualifiedName(teamworkProject);
            if (projectDescriptor == null) {
                return false;
            }
            ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
            projectsManager.loadProject(projectDescriptor, true);
        } catch (RemoteException e) {
            return false;
        }
        return true;
    }

    /**
     * Opens Teamwork MD project branch with name stored in teamworkProject and branch stored in teamworkBranchName
     */
    public static boolean teamworkOpenProjectBranch(String teamworkProject, String teamworkBranchName) {
        System.out.println("Executing openTeamworkProject");
        try {
            String qualifiedName = TeamworkUtils.generateProjectQualifiedName(teamworkProject,
                    new String[] { teamworkBranchName });
            ProjectDescriptor projectDescriptor = TeamworkUtils
                    .getRemoteProjectDescriptorByQualifiedName(qualifiedName);

            if (projectDescriptor == null) {
                return false;
            }
            ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
            projectsManager.loadProject(projectDescriptor, true);
        } catch (RemoteException e) {
            return false;
        }
        return true;
    }

    /**
     *  Releases all locks in the model and commits changes
     *  Deprecated - Use teamworkCommitReleaseLocks instead, as that will include a commit message.
     */
    @Deprecated
    public static void teamworkUnlockModelAndCommit() {
        System.out.println("Executing teamworkUnlockModelAndCommit");
        TeamworkUtils.unlockElement(Application.getInstance().getProject(), ElementFinder.getModelRoot(), true, false,
                true);
    }

    /**
     * Releases all locks in the model and discards changes
     */
    public static void teamworkUnlockElementsAndDiscard(String teamworkUsername) {
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
    public static void teamworkUnlockModelAndDiscard() {
        System.out.println("Executing teamworkUnlockModelAndDiscard");
        TeamworkUtils.unlockElement(Application.getInstance().getProject(), ElementFinder.getModelRoot(), true, true, true);
    }

    /**
     * Executes "Update Project" action
     */
    public static void teamworkUpdateProject() {
        System.out.println("Executing teamworkUpdateProject");
        TeamworkUtils.updateProject(Application.getInstance().getProject());
    }

    /**********************************************************************************
     *
     * MMS REST Interactions
     *
     **********************************************************************************/

    public static ObjectNode getMmsElement(Element e)
            throws ServerException, IOException, URISyntaxException {
        return MMSUtils.getElement(e, Application.getInstance().getProject());
    }

    public static ObjectNode getMmsElementByID(String s)
            throws ServerException, IOException, URISyntaxException {
        return MMSUtils.getElementById(s, Application.getInstance().getProject());
    }

    public static ObjectNode getMmsElements(Collection<Element> elements)
            throws ServerException, IOException, URISyntaxException {
        return MMSUtils.getElements(elements, Application.getInstance().getProject(), null);
    }

    public static ObjectNode getMmsElementsByID(Collection<String> cs)
            throws ServerException, IOException, URISyntaxException {
        return MMSUtils.getElementsById(cs, Application.getInstance().getProject(), null);
    }

    public static void deleteMmsElements(Collection<Element> deleteTargets) {
        ObjectNode requestBody = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elements = requestBody.putArray("elements");
        for (Element delTarget : deleteTargets) {
            ObjectNode curElement = JacksonUtils.getObjectMapper().createObjectNode();
            curElement.put(MDKConstants.SYSML_ID_KEY, Converters.getElementToIdConverter().apply(delTarget));
            elements.add(curElement);
        }
        requestBody.put("source", "magicdraw");
        requestBody.put("mmsVersion", MDKPlugin.VERSION);
        URIBuilder requestUri = MMSUtils.getServiceWorkspacesSitesElementsUri(Application.getInstance().getProject());
        try {
            MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.DELETE, requestUri, requestBody));
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected failure processing request. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void postMmsElemens(Collection<Element> postTargets) {
        //todo
        Project project = Application.getInstance().getProject();
        ObjectNode requestBody = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elements = requestBody.putArray("elements");
        for (Element pstTarget : postTargets) {
            elements.add(Converters.getElementToJsonConverter().apply(pstTarget, project));
        }
        requestBody.put("source", "magicdraw");
        requestBody.put("mmsVersion", MDKPlugin.VERSION);
        URIBuilder requestUri = MMSUtils.getServiceWorkspacesSitesElementsUri(project);
        try {
            MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, requestBody));
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected failure processing request. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convenience method for confirmSiteWritePermissions(string, string) to check if a project
     * is editable by the logged in user. Uses the url and site information stored in the currently
     * open project.
     *
     * @return true if the site lists "editable":"true" for the logged in user, false otherwise
     * or when no project is open or project lacks url and site specifications
     */
    public static boolean hasSiteEditPermission() {
        try {
            Project proj = Application.getInstance().getProject();
            return MMSUtils.isSiteEditable(proj, MMSUtils.getSiteName(proj));
        } catch (ServerException | URISyntaxException | IOException e) {
            MagicDrawHelper.generalMessage("[ERROR] Unable to check site permissions. Reason:" + e.getMessage());
            return false;
        }
    }

}