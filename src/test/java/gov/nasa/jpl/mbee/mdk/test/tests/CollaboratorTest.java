//package gov.nasa.jpl.mbee.mdk.test.tests;
//
//import com.jprofiler.api.agent.Controller;
//import com.nomagic.magicdraw.tests.MagicDrawTestCase;
//
//import junit.framework.Test;
//import junit.framework.TestSuite;
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//import org.junit.runner.notification.Failure;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.Date;
//
///**
// * Created by brower on 7/14/16.
// */
//public class CollaboratorTest extends MagicDrawTestCase {
//
//    protected static String alfrescoURL;
//    protected static String alfrescoUser;
//    protected static String alfrescoPassword;
//    protected static String cmd;
//    protected static String testRoot;
//    protected static String mdRoot;
//    protected static String teamworkUsername;
//    protected static String teamworkPassword;
//    protected static String teamworkServer;
//    protected static String snapshotRoot;
//    protected static boolean profiling = false;   //flag to turn on profiling
//    protected static boolean twproj = false;    //use teamwork server
//    protected static boolean twcproj = false;    //use teamwork cloud server
//    protected static String overwriteDocument = "false";    //overwrite existing document
//    protected static String enableComments = "false";    //enable comments
//    protected static String imageType = "PNG";
//    protected static String collaboratorScope = "Model";
//    protected static String collaboratorTemplate = "Entire Model";
//    protected static String publishTo = "Repository/Shared";
//    protected static String documentName;
//    protected static String projectLocation;
//    protected static int argIndex = 0;
//    protected static boolean defaultsSet = false;
//
//
//    /*   Data structure to hold results for test rails  */
//
//    private static int         caseId=0;       //the case id to post to testrails
//    private static boolean     passed=true;   //the test status, defaults to failed
//    private static String      comment = "";
//    private static String      testSuiteName = "";
//    private static long        elapsedTime=0;  //elapsed time for the test, milliseconds
//    private static long        startTime=0;      //time test started
//    private static long        endTime=0;        //time test ended
//
//
//        public CollaboratorTest(String testMethodToRun, String testName) {
//        super(testMethodToRun, testName);
//        setDoNotUseSilentMode(true);
//        System.setProperty("jsse.enableSNIExtension", "false");
//        setSkipMemoryTest(true);
//        setDoNotUseSilentMode(true);
//    }
//
//    public static void main(String[] args) {
//        setDefaults();
////        parseArgs(args);
//        System.out.println("In main method");
//        JUnitCore jCore = new JUnitCore();
//        Result results = jCore.run(CollaboratorTest.class);
//        printJunitResults(results);
//        System.exit(0);
//    }
//
//    public static Test suite() throws Exception {
//
//        TestSuite suite = new TestSuite();
//        if (twcproj == false) {
//            suite.addTest(new CollaboratorTest("ccs_TestCase1", "No scope, Entire Model, " + imageType + ", local project"));
//            suite.addTest(new CollaboratorTest("ccs_TestCase2", "No scope, SysML, " + imageType + ", local project"));
//            suite.addTest(new CollaboratorTest("ccs_TestCase3", collaboratorScope + " scope, Entire model, " + imageType + ", local project"));
//            suite.addTest(new CollaboratorTest("ccs_TestCase4", "No scope, " + collaboratorTemplate + ", " + imageType + ", local project"));
//        }   else {
//            suite.addTest(new CollaboratorTest("ccs_TestCase1", "No scope, Entire Model, " + imageType + ", TWC project"));
//            suite.addTest(new CollaboratorTest("ccs_TestCase2", "No scope, SysML, " + imageType + ", TWC project"));
//            suite.addTest(new CollaboratorTest("ccs_TestCase3", collaboratorScope +" scope, Entire model, " + imageType + ", TWC project"));
//            suite.addTest(new CollaboratorTest("ccs_TestCase4", "No scope, " + collaboratorTemplate  +", " + imageType + ", TWC project"));
//        }
//
//        return suite;
//    }
//
//    public static void setDefaults() {
//        if (!defaultsSet) {
//            alfrescoURL = "";
//            alfrescoUser = "";
//            alfrescoPassword = "";
//            teamworkUsername = "twcTest";
//            teamworkPassword = "letmein";
//            teamworkServer = "https://cae-teamworkcloud-uat.jpl.nasa.gov:10000";    //
//            snapshotRoot = "";
//            profiling = false;   //flag to turn on profiling
//            twproj = false;    //use teamwork server
//            twcproj = false;    //use teamwork cloud server
//            overwriteDocument = "true";    //overwrite existing document
//            enableComments = "true";    //enable comments
//            imageType = "PNG";
//            collaboratorScope = "Model";
//            collaboratorTemplate = "Entire Model";
//            publishTo = "Repository/Shared";
//            documentName = "MyDocument";
//            projectLocation = "test";   //Name of project to publish
//            defaultsSet = true;
//        }
//    }
//
//    public static void publishCommandLine() throws IOException{
//        testRoot = Paths.get("").toAbsolutePath().toString();
//        String propertiesFileName = testRoot + "/mdk/resource/collaborator/test1.properties";
//        try {
//            System.out.println("Trying to write the template file");
//            setPublishDefaults();
//             CollaboratorUtils.writeTemplate(propertiesFileName);
//            System.out.println("Succesfully wrote template file");
//        }catch (Exception e) {
//            passed = false;
//            comment = "Error writing properties file " + propertiesFileName;
//            System.out.println("Error writing the properties file " + propertiesFileName);
//            e.printStackTrace();
//
//
//
//        }
//        mdRoot = CollaboratorUtils.getMDInstallDirectory();
//
//        String dir = "/" + mdRoot + "/plugins/com.nomagic.collaborator.publisher/";
//        String osname = System.getProperty("os.name");
//        if (osname.contains("Windows")) {
//            cmd = "./publish_183_or_later.bat -properties " + propertiesFileName;
//        } else {
//            cmd = "./publish_183_or_later.sh -properties " + propertiesFileName;
//        }
//
//        //System.out.println("Environment variable test " + CollaboratorUtils.getMDInstallDirectory());
//
//
//        startProfiling();
//        CollaboratorUtils.publish(cmd, dir);
//        takeSnapshot("Command_Line_Publishing");
//        stopProfiling();
//
//    }
//
//    public static void setPublishDefaults() throws Exception    {
//        CollaboratorUtils.setAlfresco_repository_url(alfrescoURL);
//        CollaboratorUtils.setUsername(alfrescoUser);
//        CollaboratorUtils.setPassword(alfrescoPassword);
//        CollaboratorUtils.setTeamworkProject(twproj);
//        CollaboratorUtils.setTwcProject(twcproj);
//        CollaboratorUtils.setServer_url(teamworkServer);
//        CollaboratorUtils.setServer_username(teamworkUsername);
//        CollaboratorUtils.setServer_password(teamworkPassword);
//        CollaboratorUtils.setLocation_root(publishTo);
//        CollaboratorUtils.setDiagram_image_type(imageType);
//        CollaboratorUtils.setOverwrite_existing_document(overwriteDocument);
//        CollaboratorUtils.setComments_enabled(enableComments);
//        CollaboratorUtils.setScope(collaboratorScope);
//        CollaboratorUtils.setTemplate_document_name(collaboratorTemplate);
//        CollaboratorUtils.setFull_document_name(documentName);
//        CollaboratorUtils.setProject_location(projectLocation);
//    }
//
//    public static void publishClient() {
//        System.out.println("~~ MD API does not currently support publishing programmatically from MagicDraw ~~");
//    }
//    /**
//     *  Activates profiling, invoking features which are desired in jProfiler
//     */
//    public static void startProfiling() {
//        if (profiling == true) {
//            Controller.startCPURecording(true);
//            Controller.startAllocRecording(true);
//            Controller.startThreadProfiling();
//        }
//
//    }
//
//    /**
//     *  Stops profiling operations, stopping the features invoked by startProfiling()
//     */
//    public static void stopProfiling() {
//        if (profiling == true) {
//            Controller.stopCPURecording();
//            Controller.stopAllocRecording();
//            Controller.stopThreadProfiling();
//        }
//    }
//
//    /**
//     * Outputs the snapshot of the profiling operation
//     * @param filename  File name where the snapshot information is saved
//     */
//    public static void takeSnapshot(String filename) {
//        if (profiling == true) {
//            Controller.saveSnapshot(new File(snapshotRoot + filename + ".jps"));
//        }
//    }
//
//    /**
//     *   Preps the fields for the creation of template.properties
//     *   if publishScope is blank, it will default to "Model"
//     *   if modelTemplate is blank it will default to "Entire Model"
//     *   projectName is either the full path to the model.mdzip file, or the projectname in teamwork server/teamwork cloud
//     *   if imageFormat is blank it will default to PNG
//     *   projectLocation is 0 for Local project, 1 for Teamwork Cloud, and 2 for Teamwork Server
//     *
//     */
//    public void setupTestCase(String publishScope, String modelTemplate, String projectName, String imageFormat) {
//        if (publishScope == "") {
//            collaboratorScope= "Model";
//        }   else {
//            collaboratorScope = publishScope;
//        }
//        if (modelTemplate == "") {
//            collaboratorTemplate= " Entire Model";
//        }   else {
//            collaboratorTemplate = modelTemplate;
//        }
//        if (projectName == "") {
//            fail("Project name is missing");
//        }   else {
//            projectLocation = projectName;
//        }
//        if (imageFormat == "") {
//            imageType = "PNG";
//        }   else {
//            imageType = imageFormat;
//        }
//    }
//
//
//   /*  functions named ccs_xxxxxxx are containers to run different test cases
//        This is the area where we initialize the values for the testResult object which contains the data for TestRails
//     */
//
//    /**
//     *  Test case for entire model template, no limited scope
//     */
//    public void ccs_TestCase1() {
//        String testDescription;
//        if (twcproj) {
//            testDescription = "Entire model, TWC project, " + imageType;
//        }   else {
//            testDescription = "Entire model, local project, " + imageType;
//
//        }
//        setDefaults();
//        setupTestCase("Model", "Entire Model", projectLocation,imageType);
//        initializeTestResult( 1, testDescription );
//        try {
//            publishCommandLine();
//        } catch (Exception e)    {
//            System.out.println("Error publishing ccs_TestCase1");
//            passed=false;
//            comment="Exception publishing ccs_TestCase1";
//        }
//        finalizeTestResult();
//    }
//
//
//    /**
//     *  Test case for SYSML template, no limited scope
//     */
//    public void ccs_TestCase2() {
//        String testDescription;
//        if (twcproj) {
//            testDescription = "SysML, TWC project, " + imageType;
//        }   else {
//            testDescription = "SysML, local project, " + imageType;
//
//        }
//        setDefaults();
//        setupTestCase("Model", "SysML", projectLocation, imageType);
//        initializeTestResult( 2, testDescription );
//        try {
//            publishCommandLine();
//        } catch (Exception e)    {
//            System.out.println("Error publishing ccs_TestCase2");
//            passed=false;
//            comment="Exception publishing ccs_TestCase2";
//        }
//        finalizeTestResult();
//    }
//
//
//    /**
//     *  Test case for Entire model, limited scope
//     */
//
//    public void ccs_TestCase3() {
//        String testDescription;
//        if (twcproj) {
//            testDescription = "Entire model, " + collaboratorScope + ", TWC project, " + imageType;
//        }   else {
//            testDescription = "Entire model, " + collaboratorScope + ", local project, " + imageType;
//
//        }
//        setDefaults();
//        setupTestCase(collaboratorScope, "Entire Model", projectLocation, imageType);
//        initializeTestResult( 3, testDescription );
//        try {
//            publishCommandLine();
//        } catch (Exception e)    {
//            System.out.println("Error publishing ccs_TestCase3");
//            passed=false;
//            comment="Exception publishing ccs_TestCase3";
//        }
//        finalizeTestResult();
//    }
//    /**
//     *  Test case for Entire model, custom template
//     */
//
//    public void ccs_TestCase4() {
//        String testDescription;
//        if (twcproj) {
//            testDescription =  "Entire model, " + collaboratorTemplate + " template, TWC project, " + imageType;
//        }   else {
//            testDescription =  "Entire model, " + collaboratorTemplate + " template, local project, " + imageType;
//        }
//        setDefaults();
//        setupTestCase("Model", collaboratorTemplate , projectLocation, imageType);
//        initializeTestResult( 4, testDescription );
//        try {
//            publishCommandLine();
//        } catch (Exception e)    {
//            System.out.println("Error publishing ccs_TestCase4");
//            passed=false;
//            comment="Exception publishing ccs_TestCase1";
//        }
//        finalizeTestResult();
//    }
//
//
//
//   /*   General functions for parsing and outputting results */
//
//
//    public static void printJunitResults(Result results) {
//        System.out.println("Ran " + results.getRunCount() + " Tests");
//        System.out.println(results.getRunCount() - results.getFailureCount() + " Tests Passed");
//        if (results.getFailureCount() != 0) {
//            System.out.println(results.getFailureCount() + " FAILURES!!!");
//        }
//        for (Failure fails : results.getFailures()) {
//            if (fails.getDescription() != null)
//                System.out.println("Description: " + fails.getDescription());
//            if (fails.getMessage() != null)
//                System.out.println("Message: " + fails.getMessage());
//            if (fails.getException() != null)
//                System.out.println("Exception: " + fails.getException());
//            if (fails.getTrace() != null)
//                System.out.println("Trace: " + fails.getTrace());
//        }
//    }
//
//    /*  Functions to handle data for the TestRail framework reporting   */
//
//    /**
//     * Initialize the data structure containing the test result metrics
//     * @param caseID
//     */
//    protected  void initializeTestResult(int caseID, String strtestSuiteName)  {
//        caseId = caseID;
//        testSuiteName = strtestSuiteName;
//        startTime = new Date().getTime();
//        comment = "";
//        passed = true;
//    }
//
//    /**
//     *  Finalizes the computation of the test results, aggregating data suitable for subnission to TestRails
//     *  Also checks if the test faileed, and if so executes the fail() method to notify jUnit that the test failed
//     */
//    protected  void finalizeTestResult()  {
//        endTime = new Date().getTime();
//        elapsedTime = endTime - startTime;
//        /*  This is temporary output to see the results for the posting functions
//            Once the framework is in place, the call to TestRails will be placed here
//         */
//        System.out.println("\tCase: " + caseId + "\t Name: " + testSuiteName + "\tPassed: "+ passed + "\tComments: " + comment + "\t Elapsed: " + elapsedTime);
//        /*  Place a one second pause between cycles to allow caches to clear - MD was displaying errors  */
//        try {
//            Thread.currentThread().sleep(1000);
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        /* if the test failed, return a fail so that jUnit knows it failed  */
//        if (passed == false)
//            fail(comment);
//    }
//
//    public static void parseArgs(String[] args) {
//        // iteration of argIndex is handled by following code to account for
//        // variable length arguments with whitespace
//        for (argIndex = 0; argIndex < args.length;) {
//            if (args[argIndex].startsWith("--")) {
//                switch (args[argIndex]) {
//                    case "--profile":
//                        profiling = true;
//                        break;
//                    case "--twproj":
//                        twproj = true;
//                        break;
//                    case "--twcproj":
//                        twcproj = true;
//                        break;
//                    case "--overwrite":
//                        overwriteDocument = "true";
//                        break;
//                    case "--comments":
//                        enableComments = "true";
//                        break;
//                    default:
//                        System.out.println("Invalid flag passed: " + argIndex + " " + args[argIndex]);
//                }
//            } else if (args[argIndex].startsWith("-")) {
//                switch (args[argIndex]) {
//                    case "-alfurl":
//                        alfrescoURL =  buildArgString(args);
//                        break;
//                    case "-alfusr":
//                        alfrescoUser =  buildArgString(args);
//                        break;
//                    case "-alfpwd":
//                        alfrescoPassword =  buildArgString(args);
//                        break;
//                    case "-twsrv":
//                        teamworkServer = buildArgString(args);
//                        break;
//                    case "-twusr":
//                        teamworkUsername = buildArgString(args);
//                        break;
//                    case "-twpwd":
//                        teamworkPassword = buildArgString(args);
//                        break;
//                    case "-locroot":
//                        publishTo = buildArgString(args);
//                        break;
//                    case "-imgtype":
//                        imageType = buildArgString(args);
//                        break;
//                    case "-scope":
//                        collaboratorScope = buildArgString(args);
//                        break;
//                    case "-template":
//                        collaboratorTemplate = buildArgString(args);
//                        break;
//                    case "-docname":
//                        documentName = buildArgString(args);
//                        break;
//                    case "-projloc":
//                        projectLocation = buildArgString(args);
//                        break;
//
//                    default:
//                        System.out.println("Invalid flag passed: " + argIndex + " " + args[argIndex++]);
//                }
//            } else {
//                System.out.println("Invalid parameter passed: " + argIndex + " " + args[argIndex]);
//            }
//            argIndex++;
//        }
//        if (testRoot.equals("")) {
//            if (System.getenv().containsKey("WORKSPACE"))
//                testRoot = System.getenv().get("WORKSPACE");
//            else
//                testRoot = Paths.get("").toAbsolutePath().toString();
//        }
//    }
//
//    private static String buildArgString(String[] args) {
//        StringBuilder spacedArgument = new StringBuilder("");
//        while ((argIndex + 1) < args.length && !args[argIndex + 1].startsWith("-")) {
//            spacedArgument.append(args[++argIndex]);
//            spacedArgument.append(" ");
//        }
//        spacedArgument.setLength(spacedArgument.length() - 1);
//        return spacedArgument.toString();
//    }
//
//
//    /*  These are the test suites, preficed by ccs_  */
//}
