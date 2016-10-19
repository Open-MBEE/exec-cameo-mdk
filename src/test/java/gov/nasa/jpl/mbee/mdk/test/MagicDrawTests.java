package gov.nasa.jpl.mbee.mdk.test;

/**
 * Created by cmcmilla on 7/12/16.
 */

import com.jprofiler.api.agent.Controller;
import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.teamwork2.ITeamworkService;
import com.nomagic.magicdraw.teamwork2.ServerLoginInfo;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.magicdraw.tests.MagicDrawTestCase;
import com.nomagic.magicdraw.uml.Finder;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
//import gov.nasa.jpl.mbee.api.ElementFinder;
//import gov.nasa.jpl.mbee.emsrci.utils.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

//import gov.nasa.jpl.mbee.emsrci.utils.XmlComparator;

public abstract class MagicDrawTests extends MagicDrawTestCase {
    protected static String testRoot = "";  //used in locating the credentials location
    protected static String mdRoot = "";    //magic draw installation root
    protected static String protractorLocation = "/usr/local/bin";
    protected static String credentialsLocation = "ems-config/autotest.properties";

    protected static String testName = "";      //Human readable descriptor for the method call
    protected static String projectName = "";   //not used

    protected static String snapshotRoot;   //root for profiling snapshots
    protected static String localProject = "";   //Path name to the local project
    protected static String referenceProject = "";  //not used
    protected static String outputProject = "";  //Path name for project save operation
    protected static String logFile = "";       //not used
    protected static String branchName = "";    //not used
    protected static String baseFile = "";      //not used
    protected static String outputFile = "";    //not used
    protected static String log = "";           //not used

    protected static String teamworkServer = "";  //Name of teamwork cloud server
    protected static String teamworkPort = "";    //Port on which to connect to teamwork cloud
    protected static String teamworkUsername = "";//Username for teamwork cloud
    protected static String teamworkPassword = "";//Password for teamwork cloud
    protected static String teamworkProject = ""; //Project name in teamwork cloud
    protected static String teamworkBranchName = "";  //Branch name in teamwork cloud
    protected static String teamworkBranchDescription = "";  //Description of branch name in teamwork cloud
    protected static Boolean teamworkUseSSL = true;
    protected static Boolean teamworkProfile = true;
    protected static String workspace = "";
    protected static String date = "";

    protected static String protractorTestName = "";

    protected static String mmsUsername = "";
    protected static String mmsPassword = "";

    protected static int numRunners = 0;
    protected static String runnerFile = "";


    protected static Element sourceElement = null;
    protected static Element targetElement = null;
    protected static Element storedElement = null;
    protected static List<Element> targetElements = new ArrayList<>();
    protected static Element lockedElement = null;

    protected static String targetString = "";
    protected static String violationType = "";

    protected static boolean abort = false;
    protected static boolean twlogin = false;
    protected static boolean message = true;
    protected static boolean skipGUILog = false;
    protected static boolean idCheck = false;
    protected static boolean order = false;

    protected static Package testElementHolder;
    protected static String testElementHolderID;
    protected static int argIndex = 0;
    protected static long numReps = 1;

    /*   Data structure to hold results for test rails  */

    private int     caseId=0;       //the case id to post to testrails
    private boolean passed=true;   //the test status, defaults to failed
    private String comment = "";
    private String testSuiteName = "";
    private long     elapsedTime=0;  //elapsed time for the test, milliseconds
    private long     startTime=0;      //time test started
    private long     endTime=0;        //time test ended




    /**
     * Invokes the super method extended from MagicDrawTestCase which executes the jUnit test as part of the suite
     * @param testMethodToRun   The method in the class which will execute the desired test profile
     * @param testName          Human readable descriptor for the test
     */
    public MagicDrawTests(String testMethodToRun, String testName) {
        super(testMethodToRun, testName);
    }

    @Override
    /**
     *  This method is automatically executed by the framework.  It is called to create general setup operations
     */
    protected void setUpTest() throws Exception {
//        System.out.println(" ------------------Executing setupTest()");
        super.setUpTest();
//        twcLogin();
    }

    @Override
    /**
     *  This method is automatically executed by the framework and performs final cleanup
     */
    protected void tearDownTest() throws Exception {
 //       System.out.println( " ---------------------------------------Executing tearDownTest()");
        super.tearDownTest();
 //       twcUnlockModel();
 //       twcLogout();
    }

    /**
     *  Activates profiling, invoking features which are desired in jProfiler
     */
    public void startProfiling() {
        if (teamworkProfile == true) {
            Controller.startCPURecording(true);
            Controller.startAllocRecording(true);
            Controller.startThreadProfiling();
        }

    }

    /**
     *  Stops profiling operations, stopping the features invoked by startProfiling()
     */
    public void stopProfiling() {
        if (teamworkProfile == true) {
            Controller.stopCPURecording();
            Controller.stopAllocRecording();
            Controller.stopThreadProfiling();
        }
    }

    /**
     * Outputs the snapshot of the profiling operation
     * @param filename  File name where the snapshot information is saved
     */
    public void takeSnapshot(String filename) {
        if (teamworkProfile == true) {
            Controller.saveSnapshot(new File(snapshotRoot + filename + ".jps"));
        }
    }

    /**
     * Copies a MagicDraw element - presently not used, simply a helper method
     */
    public void copyElement() {
        //System.out.println("Executing copyElement");
        if (sourceElement == null || targetElement == null) {
            passed = false;
            comment = "Failed:  Source or Target element are null (copyElement)";
        }   else {
            try {
                storedElement = MagicDrawHelper.copyAndPaste(sourceElement, targetElement);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (storedElement == null ) {
                passed = false;
                comment = "Failed:  Stored element are null (copyElement)";
            }
        }
    }

    /**
     * Deletes a MagicDraw element - presently not used, simply a helper method
     */
    public void deleteElementOnMD() {
        //System.out.println("Executing deleteElementOnMD");
        if (sourceElement == null ) {
            passed = false;
            comment = "Failed:  Source element is null (deleteElementOnMD)";
        }   else {
            try {
                MagicDrawHelper.deleteMDElement(sourceElement);
            } catch (Exception e) {
                passed = false;
                comment = "Failed:  Unable to delete element " + sourceElement.getHumanName() + " (deleteElementOnMD)";
            }
        }
    }


    /**
     * Opens the local MagicDraw project whose filename is contained in localProject
     */
    public void openProject() {
        Project project = Application.getInstance().getProject();
        if (project == null) {
            //System.out.println("Executing openProject");
            startProfiling();
            super.openProject(localProject, false);
            if (Application.getInstance().getProject() == null) {
                passed = false;
                comment = "Failed:  Project reference is null (openProject)";
            }   else {
                createElementHolder();
            }
            takeSnapshot("OpenProject");
            stopProfiling();
        }

    }

    /**
     *  Creates or references a package, named "top_level", and stores its reference in targetElement
     */
    protected void createElementHolder() {

        Project project = Application.getInstance().getProject();
        if (project == null) {
            passed = false;
            comment = "Failed:  Project reference is null (createElementHolder)";
        }   else {
            testElementHolder = Finder.byQualifiedName().find(project, "top_level");
            if (testElementHolder == null) {
                testElementHolder = MagicDrawHelper.createPackage("top_level", project.getPrimaryModel());
                if (testElementHolder == null)  {
                    passed = false;
                    comment = "Failed:  Package reference is null (createElementHolder)";
                }
            }
            testElementHolderID = testElementHolder.getID();
            targetElement = testElementHolder;
       }
       /*
        Project project = Application.getInstance().getProject();
        assertNotNull("Project is null", project);
        testElementHolder = Finder.byQualifiedName().find(project, "top_level");
        if (testElementHolder == null) {
            testElementHolder = MagicDrawHelper.createPackage("top_level", project.getPrimaryModel());
            assertNotNull("Failed to create package top_level", testElementHolder);
            testElementHolderID = testElementHolder.getID();
            targetElement = testElementHolder;
        }
        */
    }

    /**
     * Creates 100 blocks under the "top_level" package
     */
    public void add100Blocks() {

        if (passed) {
            createElementHolder();
            if (passed) {
                Element curPack;
                Class curBlock;

                for (int i = 0; i < 10; i++) {
                    if (passed) {
                        curPack = MagicDrawHelper.createPackage("Pkg_" + (i + 1), testElementHolder);
                        if (curPack == null) {
                            passed = false;
                            comment = "Failed:  Package reference is null (add100Blocks)";

                        }   else {
                            for (int j = 0; j < 10; j++) {
                                if (passed) {
                                    curBlock = MagicDrawHelper.createBlock("b" + (j + 1), curPack);
                                    if (curBlock == null) {
                                        passed = false;
                                        comment = "Failed:  Block reference is null (add100Blocks)";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /*
        createElementHolder();
        assertNotNull("Base element does not exist", testElementHolder);
        long lStartTime = new Date().getTime();
        Element curPack;
        Class curBlock;

        for (int i = 0; i < 10; i++) {
            curPack = MagicDrawHelper.createPackage("Pkg"  + (i+1), testElementHolder );
            assertNotNull("Failed to create package", curPack);
            for (int j = 0; j < 10; j++) {
                curBlock = MagicDrawHelper.createBlock("b" + (j+1), curPack);
                assertNotNull("Failed to create block",curBlock);
            }

        }
        long lEndTime = new Date().getTime();

        long difference = lEndTime - lStartTime;
        System.out.println("FUNCTION: add100Blocks() \t ELAPSED: " + difference);
        */

    }

    /**
     * Creates 1000 blocks under the "top_level" package
     */
    public void add1000Blocks() {

        if (passed) {
            createElementHolder();
            if (passed) {
                Element curPack;
                Element curPack2;
                Class curBlock;

                for (int i = 0; i < 10; i++) {
                    if (passed) {
                        curPack = MagicDrawHelper.createPackage("Pkg_" + (i + 1), testElementHolder);
                        if (curPack == null) {
                            passed = false;
                            comment = "Failed:  Package reference is null (add1000Blocks)";

                        }   else {
                            for (int j = 0; j < 10; j++) {
                                if (passed) {
                                    curPack2 = MagicDrawHelper.createPackage("t" + (j+1), curPack);
                                    if (curPack2 == null) {
                                        passed = false;
                                        comment = "Failed:  Package reference is null (add1000Blocks)";
                                    }   else {
                                        for (int k = 0; k < 10; k++) {
                                            curBlock = MagicDrawHelper.createBlock("b" + (k + 1), curPack2);
                                            if (curBlock == null) {
                                                passed = false;
                                                comment = "Failed:  Block reference is null (add1000Blocks)";
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /*
        createElementHolder();
        assertNotNull("Base element does not exist", testElementHolder);
        long lStartTime = new Date().getTime();
        Element curPack;
        Element curPack2;
        Class curBlock;

        for (int i = 0; i < 10; i++) {
            curPack = MagicDrawHelper.createPackage("Pkg_" + (i+1), testElementHolder );
            assertNotNull("Failed to create level 1 package", curPack);
            for (int j = 0; j < 10; j++) {
                curPack2 = MagicDrawHelper.createPackage("t" + (j+1), curPack);
                assertNotNull("Failed to create level 2 package", curPack2);
                for (int k = 0; k < 10; k++) {
                    curBlock = MagicDrawHelper.createBlock("b" + (k+1), curPack2);
                    assertNotNull("Failed to create block",curBlock);
                }

            }

        }
        long lEndTime = new Date().getTime();
        long difference = lEndTime - lStartTime;
        System.out.println("FUNCTION: add1000Blocks() \t ELAPSED: " + difference);
        */

    }


    /**
     * Deletes the "top_level" package, pointed to by targetElement, and all its contents
     */
    public void deleteCreatedElements() {

        if ( testElementHolder != null) {
            twcLockElementRecursively();
            try {
                 MagicDrawHelper.deleteMDElement(testElementHolder);   //testElementHolder is the package containing the elements
            } catch (Exception e) {
                passed = false;
                comment = "Failed:  Element could not be deleted (deleteCreatedElements) " + e.toString();
               // System.out.println(" **************  UNABLE TO DELETE testElementHolder - EXCEPTION TRIGGERED *********");

            }
        }   else    {
            passed = false;
            comment = "Failed:  Element to be deleted is null (deleteCreatedElements)";
            //System.out.println(" **************  UNABLE TO DELETE testElementHolder *********");
        }

        /*
        assertNotNull("Base element does not exist",testElementHolder);
        //       twcLockElement();
        long lStartTime = new Date().getTime();
        try {

            MagicDrawHelper.deleteMDElement(testElementHolder);   //testElementHolder is the package containing the elements
        } catch (Exception e) {
            System.out.println("The element cannot be deleted. It may not exist");
        }
        long lEndTime = new Date().getTime();
        long difference = lEndTime - lStartTime;
        System.out.println("FUNCTION: deleteCreatedElements() \t ELAPSED: " + difference);
        //       createElementHolder();
        */
    }

    /**
     * Creates 100 blank diagrams in the "top_level" package
     */
    public void add100Diagrams() {

        if (passed) {
            createElementHolder();
            if (passed) {
                try {
                    MagicDrawHelper.createDiagrams(100, testElementHolder);
                } catch (Exception e) {
                    passed = false;
                    comment = "Failed:  Exception (add100Diagrams) " + e.toString();
                }
            }
        }
        /*
        createElementHolder();
        assertNotNull("Base element does not exist",testElementHolder);
        long lStartTime = new Date().getTime();
        MagicDrawHelper.createDiagrams(100, testElementHolder);
        long lEndTime = new Date().getTime();
        long difference = lEndTime - lStartTime;
        System.out.println("FUNCTION: add100Diagrams() \t ELAPSED: " + difference);
*/
    }

    /**
     * Closes the currently active MagicDraw project
     */
    public void closeProject() {
        //System.out.println("Executing closeProject");
        Project prj = Application.getInstance().getProject();
        if (prj != null)
            super.closeProject(Application.getInstance().getProject());
    }

    /**
     * Renames the element pointed to by targetElemet with a name pointed to by targetString
     */
    public void renameElement() {
       // System.out.println("Executing renameStoredElement");

        if (targetElement instanceof NamedElement & targetElement != null)
            MagicDrawHelper.renameElement((NamedElement)targetElement, targetString);
    }


    /**
     * Saves the project as a local project to a file whose name is locates in outputProject
     */
    public void saveProjectOutput() {
        //System.out.println("Executing saveProjectOutput");
        if (Application.getInstance().getProject() != null ) {
            File save = new File(outputProject);
            try {
                super.saveProject(Application.getInstance().getProject(), save);
            } catch (Exception e) {
                passed = false;
                comment = "Failed:  (saveProjectOutput) " + e.toString();
            }
        }
    }






    //TEAMWORKCLOUD METHODS

    /**
     * Commits Teamwork project to server, with commit message stored in targetString
     * Keeps locks
     */
    public void twcCommit() {
        //System.out.println("Executing teamworkCommit");
        Project prj = Application.getInstance().getProject();
        if (prj != null)
           EsiUtils.commitProject(prj , targetString, null, null, false, null);

        //No current checking for if this succeeded.... Need to add this
    }

    /**
     * Commits Teamwork project to server, with commit message stored in targetString
     *
     * NOTE - Only releases locks on NON-NEW elements due to a limitation with the MD API
     * Recommend that you call teamworkUnlockModelAndDiscard after this call to ensure complete
     * remove of locks. - THIS MAY BE OUT OF DATE WITH MD & TWC/CEDW 18.4. NEEDS TO BE TESTED
     */
    public void twcCommitReleaseLocks() {
        //System.out.println("Executing teamworkCommitReleaseLocks");

        MagicDrawHelper.twcCommitReleaseLocks(teamworkUsername, targetString);

        //No current checking for if this succeeded.... Need to add this
    }

    /**
     * Creates a Teamwork branch of a project at teamworkBranchName with the description
     * stored in teamworkBranchDescription
     */
    public void twcCreateBranch() {
        try {

            ITeamworkService twcService = EsiUtils.getTeamworkService();
            ProjectDescriptor projectDescriptor = twcService.getProjectDescriptorByQualifiedName(teamworkProject);
            if (teamworkBranchName == "")
                teamworkBranchName = "branch";
            if (teamworkBranchDescription == "")
                teamworkBranchDescription = "branch Description";
            if (projectDescriptor == null) {
                passed = false;
                comment = "Failed:  Project descriptor is null (twcCreateBranch)";
            }   else {
                EsiUtils.createBranch(projectDescriptor, 1, teamworkBranchName, teamworkBranchDescription);
            }
        }
        catch (Exception e) {
            passed = false;
            comment = "Failed:  Exception (twcCreateBranch) " + e.toString();
        }
    }

    /**
     * Executes "Lock Elements for Edit" on targetElement
     */
    public void twcLockElement() {
        //System.out.println("Executing twcLockElement");
        ILockProjectService twcLocks =  EsiUtils.getLockService(Application.getInstance().getProject());

        Collection<Element> entireModel = null;
        entireModel.add(targetElement);
        boolean success = twcLocks.lockElements(entireModel, false, null);
        if (success == false ) {
            passed = false;
            comment = "Failed:  Unable to lock element (twcLockElement)";
        }

    }

    /**
     * Executes "Lock Elements for Edit Recursively" on targetElement
     */
    public void twcLockElementRecursively() {
        //System.out.println("Executing twcLockElementRecursively " + testElementHolder.getID());
        ILockProjectService twcLocks =  EsiUtils.getLockService(Application.getInstance().getProject());

        //Collection<Element> entireModel = null;
       // entireModel.add(testElementHolder);
       // boolean success = twcLocks.lockElements(entireModel, true, null);
        boolean success = twcLocks.lockElements(Collections.singleton(testElementHolder), true, null);
        if (success == false ) {
            passed = false;
            comment = "Failed:  Unable to lock element (twcLockElementRecursively)";
           // System.out.println("Failed Executing twcLockElementRecursively " + testElementHolder.getID());
        }
    }

    /**
     * Executes "Lock Elements for Edit Recursively" on the model root
     */
    public void twcLockModel() {
        //System.out.println("Executing twcLockModel");
        ILockProjectService twcLocks =  EsiUtils.getLockService(Application.getInstance().getProject());

        Element mdl = Application.getInstance().getProject().getPrimaryModel();
        boolean success = twcLocks.lockElements(Collections.singleton(mdl), true, null);
        if (success == false ) {
            passed = false;
            comment = "Failed:  Unable to lock model (twcLockModel)";
        }
    }

    /**
     *  Unlocks all currently locked elements, by unlocking the root element recursively
     */
    public void twcUnlockModel() {
        //System.out.println("Executing twcUnlockModel");
        ILockProjectService twcLocks =  EsiUtils.getLockService(Application.getInstance().getProject());

        Element mdl = Application.getInstance().getProject().getPrimaryModel();
        boolean success = twcLocks.unlockElements(Collections.singleton(mdl), true, null);
        if (success == false ) {
            passed = false;
            comment = "Failed:  Unable to unlock model (twcUnlockModel)";
        }
    }

    /**
     * Logs in to Teamwork server at teamworkServer:teamworkPort using teamworkUsername and teamworkPassword
     *
     * NOTE:  If a port is not explicitly set, the default port, 3579, is used.  Additionally, teamworkUseSSL controls if the connection is secured or not
     */
    public void twcLogin() {

        if (teamworkServer == "" || teamworkServer == null)
            twc_SetDefaults();      //required to prime defaults if main() was bypassed

        setDoNotUseSilentMode(true);
        ITeamworkService twcService = EsiUtils.getTeamworkService();
        if (twcService.isConnected()) {
        //    System.out.println(twcService.getConnectedUser() + " is already connected");
        }  else {
            if (teamworkPort.equals(""))
                teamworkPort = "3579";
            twcService.login(new ServerLoginInfo(teamworkServer + ":" + Integer.parseInt(teamworkPort),
                    teamworkUsername, teamworkPassword, teamworkUseSSL), false);
            if (twcService.isConnected()) {
                twlogin = true;
            }   else {
                passed = false;
                comment = "Failed:  unable to log in (twcLogin)";
            }
        }
    }



    /**
     * Logs out of the logged in teamwork account
     */
    public void twcLogout() {
        //System.out.println("Executing twcLogout");
        if (EsiUtils.getTeamworkService().isConnected()) {
            EsiUtils.getTeamworkService().logout();
            twlogin = false;
        }
        try {
            Thread.currentThread().sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Opens Teamwork MD project with name stored in teamworkProject, recording the elapsed time
     */
    public void twcOpenProject() {
        //System.out.println("Executing twcOpenProject");
        twcOpenProjectHelper();
        createElementHolder();
    }

    /**
     * Helper function which opens the Teamwork project
     * @return  Elapsed time in opening the project
     */
    public void twcOpenProjectHelper() {
        try {
                ITeamworkService twcService = EsiUtils.getTeamworkService();
                if (!twcService.isConnected())
                    twcLogin();
                ProjectDescriptor projectDescriptor = twcService.getProjectDescriptorByQualifiedName(teamworkProject);
                if (projectDescriptor == null) {
                    passed = false;
                    comment = "Failed: Unable to retrieve TWC Project " + teamworkProject + " by qualified name (twcOpenProjectHelper)";
                }   else {

                    ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
                    projectsManager.loadProject(projectDescriptor, true);
                }
        }

        catch (Exception e) {
            passed = false;
            comment = "Exception thrown when attempting to open indicated twc project: " + e.toString() + " (twcOpenProjectHelper)";
        }
    }

    /**
     * Calls multiple functions to perform a test to add, commit, and delete 100 blocks in teamwork cloud
     * There is a one second delay to allow for the caches in Magicdraw to be updated without generating an error when we have multiple throughput
     */
    public void twcCommitAddDelete100Blocks() {

        //System.out.println("Executing twcCommitAddDelete100Blocks ");
        add100Blocks();
        targetString = "Added pkg top_level with 100 blocks";
        twcCommit();
        deleteCreatedElements();
        targetString = "Deleted pkg top_level";
        twcCommit();

    }


    /**
     * Calls multiple functions to perform a test to add, commit, and delete 1000 blocks in teamwork cloud
     * There is a one second delay to allow for the caches in Magicdraw to be updated without generating an error when we have multiple throughput
     */

    public void twcCommitAddDelete1000Blocks() {

        add1000Blocks();
        targetString = "Added pkg top_level with 1000 blocks";
        twcCommit();
        deleteCreatedElements();
        targetString = "Deleted pkg top_level";
        twcCommit();


    }

    /**
     * Calls multiple functions to perform a test to add, commit, and delete 100 diagrams in teamwork cloud
     * There is a one second delay to allow for the caches in Magicdraw to be updated without generating an error when we have multiple throughput
     */

    public void twcCommitAddDelete100Diagrams() {
        add100Diagrams();
        targetString = "Added pkg top_level with 100 diagrams";
        twcCommit();
        deleteCreatedElements();
        targetString = "Deleted pkg top_level";
        twcCommit();

    }
    /**
     * Opens Teamwork MD project branch with name stored in teamworkProject and branch stored in teamworkBranchName
     */
    public void twcOpenProjectBranch() {
        try {
            ITeamworkService twcService = EsiUtils.getTeamworkService();

            ProjectDescriptor projectDescriptor = twcService.getProjectDescriptorByQualifiedName(teamworkProject);
            if (projectDescriptor == null) {
                passed = false;
                comment = "Failed:  Unable to find projectDescriptor (twcOpenProjectBranch)";
            }   else {
                ProjectDescriptor branchDescriptor = EsiUtils.getDescriptorForBranch(projectDescriptor, teamworkBranchName);
                if ( branchDescriptor == null ) {
                    passed = false;
                    comment = "Failed:  Unable to find branchDescriptor  (twcOpenProjectBranch)";

                }   else {
                    Application.getInstance().getProjectsManager().loadProject(branchDescriptor,true);
                }
            }
        } catch (Exception e) {
            passed = false;
            comment = "Failed:  Exception thrown when attempting to open indicated twc project: " + e.toString();
        }
    }


    /**
     * Executes "Update Project" action
     */
    public void twcUpdateProject() {
        //System.out.println("Executing twcUpdateProject");
        EsiUtils.updateProject(Application.getInstance().getProject());
    }



    /**
     * Adds local project pointed to by localProject to teamwork cloud project named teamworkProject, logging the elapsed time
     */

    public void twcAddProject() {
        //System.out.println("Executing twcAddProject()");
        if (!EsiUtils.getTeamworkService().isConnected())
            twcLogin();
        try {
            Project activeProject = Application.getInstance().getProject();
            if (activeProject == null) {
                passed = false;
                comment = "Failed:  No project exusts to be added to TWC (twcAddProjectHelper)";
            } else {
                final IPrimaryProject addedFromLocal = (IPrimaryProject) EsiUtils.addToESI(activeProject, teamworkProject);
                if (addedFromLocal == null) {
                    passed = false;
                    comment = "Failed:  Failed to add local project to Teamwork Cloud (twcAddProject)";
                }
            }
        }
        catch (Exception e) {
            passed = false;
            comment = "Failed:  Exception thrown when attempting to add indicated twc project: " + e.toString();

        }
    }


    /**
     * Deletes the teamwork project, loging the elapsed time
     */
    public void twcDeleteProject() {
        try {
            ITeamworkService twcService = EsiUtils.getTeamworkService();
            if (!twcService.isConnected())
                twcLogin();
            ProjectDescriptor projectDescriptor = twcService.getProjectDescriptorByQualifiedName(teamworkProject);
            if (projectDescriptor == null) {
                passed = false;
                comment = "Failed: Unable to get project by its descriptor's qualified name";
            } else {
                EsiUtils.deleteProject(projectDescriptor);
            }
        }
        catch (Exception e) {
            passed = false;
            comment = "Failed: Exception thrown when attempting to open indicated twc project: " + e.toString();

        }

        }


    /*  functions named twcs_xxxxxxx are containers to run different test cases
        This is the area where we initialize the values for the testResult object which contains the data for TestRails
     */

    /**
     * Test suite element which will add the TMT project into teamwork cloud
     */
    public void twcs_Login() {
        initializeTestResult( 0, "Login to Teamwork Cloud" );
        twcLogin();
        finalizeTestResult();
    }


    /**
     * Test suite element which will add the TMT project into teamwork cloud
     */
    public void twcs_AddProjectTest() {
        //System.out.println("---------- Adding project");
        initializeTestResult( 1, "Add new project to Teamwork Cloud" );
        openProject();
        twcAddProject();
        closeProject();
        finalizeTestResult();
    }

    /**
     * Test suite element which will open a project on Teamwork CLoud and download it to the local system
     */
    public void twcs_OpenProjectTest() {
        long     saveNumReps;
        saveNumReps = numReps;
        if ( numReps < 1 )
            numReps = 1;
        for(int i=0 ; i < numReps ; i++) {
            initializeTestResult( 2, "Open Project from Teamwork Cloud" );
            twcOpenProject();
            closeProject();
            finalizeTestResult();
        }
        numReps = saveNumReps;
     }



    /**
     *  Test suite which will add and delete 100 blocks into package "top_level"
     */

    public void twcs_AddDeleteElementsTest1() {
        long     saveNumReps;
        saveNumReps = numReps;
        if ( numReps < 1 )
            numReps = 1;
        Project prj = Application.getInstance().getProject();
        if (prj == null) {
            twcOpenProject();     // Open the project so we can perform the operations
        }
        if (passed) {

            for (int i = 0; i < numReps; i++) {
                initializeTestResult( 3, "Add and Delete 100 blocks");
                twcCommitAddDelete100Blocks();
                finalizeTestResult();
            }
        }
    }

    /**
     *  Test suite which will add and delete 1000 blocks into package "top_level"
     */

    public void twcs_AddDeleteElementsTest2() {
        long     saveNumReps;
        saveNumReps = numReps;
        if ( numReps < 1 )
            numReps = 1;
        Project prj = Application.getInstance().getProject();
        if (prj == null) {
            twcOpenProject();     // Open the project so we can perform the operations
        }
        if (passed) {

            for (int i = 0; i < numReps; i++) {
                initializeTestResult( 4, "Add and Delete 1000 blocks");
                twcCommitAddDelete1000Blocks();
                finalizeTestResult();
            }
        }
    }

    /**
     *  Test suite which will add and delete 100 diagrams into package "top_level"
     */

    public void twcs_AddDeleteElementsTest3() {
        long     saveNumReps;
        saveNumReps = numReps;
        if ( numReps < 1 )
            numReps = 1;
        Project prj = Application.getInstance().getProject();
        if (prj == null) {
            twcOpenProject();     // Open the project so we can perform the operations
        }
        if (passed) {

            for (int i = 0; i < numReps; i++) {
                initializeTestResult( 5, "Add and Delete 100 diagrams");
                twcCommitAddDelete100Diagrams();
                finalizeTestResult();
            }
        }
    }

    /**
     * Test suite element which will update a project from Teamwork Cloud and download it to the local system
     */
    public void twcs_UpdateProjectTest() {
        long     saveNumReps;
        saveNumReps = numReps;
        if ( numReps < 1 )
            numReps = 1;
        for(int i=0 ; i < numReps ; i++) {
            initializeTestResult( 6, "Update Project from Teamwork Cloud" );
            twcUpdateProject();
            finalizeTestResult();
        }
        numReps = saveNumReps;
    }

    /**
     * Test suite element which will create a branch of the project
     */
    public void twcs_CreateBranchTest() {
            initializeTestResult( 7, "Create Project Branch on Teamwork Cloud" );
            twcCreateBranch();
            finalizeTestResult();
    }

    public void twcs_OpenBranchTest() {
        initializeTestResult( 8, "Open Project Branch on Teamwork Cloud" );
        twcOpenProjectBranch();
        finalizeTestResult();
    }

    public void twcs_LockModelTest() {
        initializeTestResult( 9, "Lock Entire Model on Teamwork Cloud" );
        twcLockModel();
        finalizeTestResult();
    }

    public void twcs_UnlockModelTest() {
        initializeTestResult( 10, "Unock Entire Model on Teamwork Cloud" );
        twcUnlockModel();
        finalizeTestResult();
    }

    public void twcs_Logout() {
        initializeTestResult( 11, "Log out of Teamwork Cloud" );
        twcLogout();
        finalizeTestResult();
    }
    public static void twc_SetDefaults() {
        //System.out.println("Setting defaults in method twc_SetDefaults");
        //Ben's config
        teamworkServer = "localhost";  //Name of teamwork cloud server
        teamworkPort = "3579";    //Port on which to connect to teamwork cloud
        teamworkUsername = "Administrator";//Username for teamwork cloud
        teamworkPassword = "Administrator";//Password for teamwork cloud
        teamworkProject = "_blank"; //Project name in teamwork cloud
        teamworkUseSSL = false;
        localProject = "/jpltestmodels/_blank.mdzip";   //Path to local project file
        outputProject = "/jpltestmodels/_populated.mdzip";   //Path to local save  file*/
        numReps = 1;

        //Cameron's config
        /*
        teamworkServer = "cae-teamworkcloud-uat.jpl.nasa.gov";  //Name of teamwork cloud server
        teamworkPort = "10000";    //Port on which to connect to teamwork cloud
        teamworkUsername = "twcTest";//Username for teamwork cloud
        teamworkPassword = "letmein";//Password for teamwork cloud
        teamworkProject = "_blank"; //Project name in teamwork cloud
        teamworkUseSSL = true;
        localProject = "184_Test.mdzip";   //Path to local project file
        outputProject = "184_Test.mdzip";   //Path to local save  file
        */

    }
    //Tests below this line are still for Teamwork, not TWC/CEDW



    /**
     * Releases all locks in the model and discards changes
     */
    public void teamworkUnlockModelAndDiscard() {
       // System.out.println("Executing teamworkUnlockModelAndDiscard");
        TeamworkUtils.unlockElement(Application.getInstance().getProject(), ElementFinder.getModelRoot(), true, true, true);
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
                    /*
                    case "--doclist":
                        String csvDocumentList = buildArgString(args);
                        System.out.print("Document list: ");
                        for (String document : csvDocumentList.split(", ")) {
                            documentList.add(document);
                            System.out.print(document + " ");
                        }
                        System.out.println();
                        break;
                        */
                    case "--usessl":
                        teamworkUseSSL = true;
                        break;
                    case "--profile":
                        teamworkProfile = true;
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
                        protractorLocation = buildArgString(args);
                        if (protractorLocation.equals("/"))
                            protractorLocation = "";
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
                    case "-snprt":
                        snapshotRoot = buildArgString(args);
                        break;
                    case "-locprj":
                        localProject = buildArgString(args);
                        break;
                    case "-outprj":
                        outputProject  = buildArgString(args);
                        break;
                    case "-twbrnm":
                        teamworkBranchName   = buildArgString(args);
                        break;
                    case "-twbrdsc":
                        teamworkBranchDescription   = buildArgString(args);
                        break;


                    default:
                        System.out.println("Invalid flag passed: " + argIndex + " " + args[argIndex++]);
                }
            } else {
                System.out.println("Invalid parameter passed: " + argIndex + " " + args[argIndex]);
            }
            argIndex++;
        }
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

    /*  Functions to handle data for the TestRail framework reporting   */

    /**
     * Initialize the data structure containing the test result metrics
     * @param caseID
     */
    protected  void initializeTestResult(int caseID, String strtestSuiteName)  {
        caseId = caseID;
        testSuiteName = strtestSuiteName;
        startTime = new Date().getTime();
        comment = "";
        passed = true;
    }

    /**
     *  Finalizes the computation of the test results, aggregating data suitable for subnission to TestRails
     *  Also checks if the test faileed, and if so executes the fail() method to notify jUnit that the test failed
     */
    protected  void finalizeTestResult()  {
        endTime = new Date().getTime();
        elapsedTime = endTime - startTime;
        /*  This is temporary output to see the results for the posting functions
            Once the framework is in place, the call to TestRails will be placed here
         */
        System.out.println("\tCase: " + caseId + "\t Name: " + testSuiteName + "\tPassed: "+ passed + "\tComments: " + comment + "\t Elapsed: " + elapsedTime);
        /*  Place a one second pause between cycles to allow caches to clear - MD was displaying errors  */
        try {
            Thread.currentThread().sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* if the test failed, return a fail so that jUnit knows it failed  */
        if (passed == false)
            fail(comment);
    }
}
