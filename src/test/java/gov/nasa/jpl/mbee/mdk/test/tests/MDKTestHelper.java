package gov.nasa.jpl.mbee.mdk.test.tests;

import java.io.*;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.Properties;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork.application.BranchData;
import com.nomagic.magicdraw.teamwork2.ITeamworkService;
import com.nomagic.magicdraw.teamwork2.ServerLoginInfo;
import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
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

    private static String username;
    private static String password;
    private static String teamworkServer;
    private static String teamworkPort;
    private static String twCloudServer;
    private static String twCloudPort;


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

    private static void loadCredentials(File credentials, String credentialsAppend)
            throws IOException {
        InputStream propertiesFileStream = new FileInputStream(credentials);
        if (propertiesFileStream == null) {
            throw new IOException("Credentials stream is invalid.");
        }
        Properties prop = new Properties();
        prop.load(propertiesFileStream);
        if (prop.containsKey("user.name" + credentialsAppend)) {
            username = prop.getProperty("user.name" + credentialsAppend);
            password = prop.getProperty("user.pass" + credentialsAppend);
        } else {
            username = prop.getProperty("user.name") + credentialsAppend;
            password = prop.getProperty("user.pass") + credentialsAppend;
        }
        teamworkServer = prop.getProperty("tw.url");
        teamworkPort = prop.getProperty("tw.port");
        twCloudServer = prop.getProperty("twc.url");
        twCloudPort = prop.getProperty("twc.port");
        if (teamworkServer != null && teamworkServer.indexOf("//") != -1)
            teamworkServer = teamworkServer.substring(teamworkServer.indexOf("//") + 2);
        if (teamworkServer != null && teamworkServer.lastIndexOf(':') != -1)
            teamworkServer = teamworkServer.substring(0, teamworkServer.lastIndexOf(':'));
    }

    /**********************************************
     *
     * Test case methods
     *
     **********************************************/

    public static String loadLocalProject(File projectFile, File credentials, String credentialsAppend)
            throws IOException {
        loadCredentials(credentials, credentialsAppend);
        if (username == null || password == null) {
            String message = "Credentials file did not contain the following: "
                    + (username == null ? "user.name " : "") + (password == null ? "user.pass " : "");
            throw new IOException(message);
        }
        MDKHelper.setMMSLoginCredentials(username, password);

        MagicDrawHelper.openProject(projectFile);
        return username;
    }

    public static String teamworkLoadProject(String projectId, String branch, File credentials, String credentialsAppend)
            throws IOException {
        loadCredentials(credentials, credentialsAppend);
        if (username == null || password == null || teamworkServer == null || teamworkPort == null) {
            String message = "Credentials file did not contain the following: "
                    + (username == null ? "user.name " : "") + (password == null ? "user.pass " : "")
                    + (teamworkServer == null ? "tw.url " : "") + (teamworkPort == null ? "tw.port " : "");
            throw new IOException(message);
        }
        MDKHelper.setMMSLoginCredentials(username, password);

        SessionInfo session = TeamworkUtils.loginWithSession(teamworkServer, Integer.parseInt(teamworkPort),
                username, password);
        if (session == null) {
            throw new IOException("Credentials file did not provide valid credentials for Teamwork.");
        }

        ProjectDescriptor projectDescriptor = TeamworkUtils.getRemoteProjectDescriptor(projectId);
        if (projectDescriptor == null) {
            throw new FileNotFoundException("[FAILURE] Unable to find Teamwork projectId " + projectId);
        }

        if (branch != null && !branch.isEmpty()) {
            String fqName = projectDescriptor.getRepresentationString() + "/" + branch;
            List<BranchData> branchData = new ArrayList<>();
            branchData.addAll(TeamworkUtils.getBranches(projectDescriptor));
            projectDescriptor = null;
            for (int i = 0; i < branchData.size(); i++) {
                ProjectDescriptor temp = TeamworkUtils.getRemoteProjectDescriptor(branchData.get(i).getBranchId());
                if (temp.getRepresentationString().equals(fqName)) {
                    projectDescriptor = temp;
                    break;
                }
                branchData.addAll(TeamworkUtils.getBranches(temp));
            }
            if (projectDescriptor == null) {
                throw new FileNotFoundException("[FAILURE] Unable to find Teamwork project branch " + fqName);
            }
        }

        Application.getInstance().getProjectsManager().loadProject(projectDescriptor, true);
        return username;
    }

    public static String twCloudLoadProject(String projectId, String branch, File credentials, String credentialsAppend)
            throws IOException {
        loadCredentials(credentials, credentialsAppend);
        if (username == null || password == null || twCloudServer == null || twCloudPort == null) {
            String message = "Credentials file did not contain the following: "
                + (username == null ? "user.name " : "") + (password == null ? "user.pass " : "")
                + (twCloudServer == null ? "twc.url " : "") + (twCloudPort == null ? "twc.port " : "");
            throw new IOException(message);
        }

        MDKHelper.setMMSLoginCredentials(username, password);

        ITeamworkService twcService = EsiUtils.getTeamworkService();
        twcService.login(new ServerLoginInfo(twCloudServer + ":" + Integer.parseInt(twCloudPort), username, password, true), false);
        if (!twcService.isConnected()) {
            throw new IOException("Credentials file did not provide valid credentials for TeamworkCloud.");
        }

        ProjectDescriptor projectDescriptor;
        try {
            projectDescriptor = EsiUtils.getTeamworkService().getProjectDescriptorById(projectId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
        if (projectDescriptor == null) {
            throw new FileNotFoundException("[FAILURE] Unable to find TeamworkCloud projectId " + projectId);
        }


        if (branch != null && !branch.isEmpty()) {
            String fqName = projectDescriptor.getRepresentationString() + "/" + branch;
            projectDescriptor = EsiUtils.getDescriptorForBranch(projectDescriptor, branch);
            if (projectDescriptor == null) {
                throw new FileNotFoundException("[FAILURE] Unable to find TeamworkCloud project branch " + fqName);
            }
        }

        Application.getInstance().getProjectsManager().loadProject(projectDescriptor, true);
        return username;
    }

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

//    public static ObjectNode getMmsElement(Element e)
//            throws ServerException, IOException, URISyntaxException {
//        return MMSUtils.getElement(Project.getProject(e), e.getID());
//    }
//
//    public static ObjectNode getMmsElementByID(String s)
//            throws ServerException, IOException, URISyntaxException {
//        return MMSUtils.getElementById(s, Application.getInstance().getProject());
//    }
//
//    public static ObjectNode getMmsElements(Collection<Element> elements)
//            throws ServerException, IOException, URISyntaxException {
//        return MMSUtils.getElements(elements, Application.getInstance().getProject(), null);
//    }
//
//    public static ObjectNode getMmsElementsByID(Collection<String> cs)
//            throws ServerException, IOException, URISyntaxException {
//        return MMSUtils.getElementsById(cs, Application.getInstance().getProject(), null);
//    }

    public static void deleteMmsElements(Collection<Element> deleteTargets) {
        ObjectNode response = null;
        try {
            response = MDKHelper.deleteMmsElements(deleteTargets, Application.getInstance().getProject());
        } catch (IOException | URISyntaxException | ServerException e) {
            e.printStackTrace();
        }
        // do something with response if you want
    }

    public static void postMmsElements(Collection<Element> postTargets) {
        ObjectNode response = null;
        try {
            response = MDKHelper.postMmsElements(postTargets, Application.getInstance().getProject());
        } catch (IOException | URISyntaxException | ServerException e) {
            e.printStackTrace();
        }
        // do something with response if you want
    }

    /**
     * Convenience method for confirmSiteWritePermissions(string, string) to check if a project
     * is editable by the logged in user. Uses the url and site information stored in the currently
     * open project.
     *
     * @return true if the site lists "editable":"true" for the logged in user, false otherwise
     * or when no project is open or project lacks url and site specifications
     */
    @Deprecated
    //TODO @DONBOT migrate off of site
    public static boolean hasSiteEditPermission() {
        try {
            Project proj = Application.getInstance().getProject();
            return MMSUtils.isSiteEditable(proj, proj.getName());
        } catch (ServerException | URISyntaxException | IOException e) {
            MagicDrawHelper.generalMessage("[ERROR] Unable to check site permissions. Reason:" + e.getMessage());
            return false;
        }
    }

}