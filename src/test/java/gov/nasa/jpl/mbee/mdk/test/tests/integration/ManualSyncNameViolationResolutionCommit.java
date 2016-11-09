//package gov.nasa.jpl.mbee.mdk.test.tests.integration;
//
//<<<<<<< Updated upstream
//import gov.nasa.jpl.mbee.mdk.test.framework.MDKTestCase;
//=======
//import gov.nasa.jpl.mbee.mdk.test.tests.MDKTestHelper;
//>>>>>>> Stashed changes
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//
//import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
//
///**
// * @author ablack
// * @JIRA MAGICDRAW-312
// *
// */
//public class ManualSyncNameViolationResolutionCommit extends MDKTestHelper
//{
//    //global test vars
//    static String testName = "ManualSyncNameViolationResolutionCommit";
//    static String targetSysMLID;
//    static Element targetPackage;
//    static Element targetElement;
//
//    public static void main(String[] args) throws Exception
//    {
//        parseArgs(args);
//        System.out.println("In main method");
//        JUnitCore jCore = new JUnitCore();
//        Result results = jCore.run(ManualSyncNameViolationResolutionCommit.class);
//        printJunitResults(results);
//        System.exit(0);
//    }
//
//    public ManualSyncNameViolationResolutionCommit (String testMethodToRun, String testName)
//    {
//        super(testMethodToRun, testName);
//        setDoNotUseSilentMode(true);
//        System.setProperty("jsse.enableSNIExtension", "false");
//        setSkipMemoryTest(true);
//        setDoNotUseSilentMode(true);
//    }
//    /*
//    public static Test suite() throws Exception
//    {
//        TestSuite suite = new TestSuite();
//
//        suite.addTest(new ManualSyncNameViolationResolutionCommit("executeTest", "execute test"));
//
//        return suite;
//    }
//
//    @Override
//    protected void setUpTest() throws Exception {
//        super.setUpTest();
//        // do setup here
//
//        // load credentials from file
//        loadCredentials("");
//
//        // set MMS credentials
//        MDKHelper.setMMSLoginCredentials(teamworkUsername, teamworkPassword);
//
//        //open project
//        projectName = "CoordinatedSyncTest";
//        testProject = testRoot + "/mdk/resource/" + projectName + ".mdzip";
//        super.openProject();
//
//        //clean and prepare test environment
//        MagicDrawHelper.createSession();
//        MagicDrawHelper.deleteEditableContainerChildren(ElementFinder.getModelRoot());
//        MagicDrawHelper.closeSession();
//
//        //make sure expected stuff is in place
//        MagicDrawHelper.createSession();
//        targetPackage = MagicDrawHelper.createPackage("UpdatePkg", ElementFinder.getModelRoot());
//        targetElement = MagicDrawHelper.createDocument("UpdateDoc", targetPackage);
//        targetSysMLID = targetElement.getID();
//        MagicDrawHelper.closeSession();
//        super.saveUpdatedProject();
//    }
//
//    public void executeTest() {
//        //testing vars
//        String newName = "ChangedDoc";
//
//        //confirm mms permissions
//        super.confirmMMSPermissions();
//
//        //create / change / delete
//        MagicDrawHelper.createSession();
//        NamedElement target = (NamedElement) targetElement;
//        target.setName(newName);
//        MagicDrawHelper.closeSession();
//
//        //manual sync element
//        MDKHelper.validateModel(targetPackage);
//
//        //confirm violation
//        Collection<Element> violationTargets = new ArrayList<Element>();
//        violationTargets.add(targetElement);
//        if (!MDKHelper.getManualValidationWindow().confirmElementValidationTypeResult("[NAME]", violationTargets).isEmpty()) {
//            fail("Element not found in validation results");
//        }
//
//        //resolve violation
//        try {
//            if (!MDKHelper.getManualValidationWindow().commitSpecificTypeMDChangesToMMS("[NAME]", violationTargets).isEmpty()) {
//                fail("Element not found in validation results");
//            } else {
//                //wait for mms to process element
//                super.wait5();
//            }
//        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//            fail("Unable to resolve violation. Exception thrown: " + e.getMessage());
//        }
//
//        //confirm resolution via rest
//        target = (NamedElement) ElementFinder.getElementByID(targetSysMLID);
//        if (!target.getName().equals(newName)) {
//            fail("Element name was not set per expected resolution.");
//        }
//    }
//
//    @Override
//    protected void tearDownTest() throws Exception {
//        super.tearDownTest();
//        // do tear down here
//
//        //close project
//        super.closeProject();
//    }
//    */
//}
