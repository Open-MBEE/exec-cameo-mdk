//package gov.nasa.jpl.mbee.mdk.test.tests.integration;
//
//<<<<<<< Updated upstream
//import gov.nasa.jpl.mbee.mdk.test.framework.MDKTestCase;
//=======
//import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
//import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
//import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
//import gov.nasa.jpl.mbee.mdk.test.tests.MDKTestHelper;
//import org.junit.Test;
//>>>>>>> Stashed changes
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//
//import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
//
//
///**
// * @author ablack
// * @JIRA MAGICDRAW-263
// *
// */
//public class CoordinatedSyncMDElementCreate
//{
//    //global test vars
//    static String testName = "CoordinatedSyncMDElementCreate";
//    static String targetSysMLID;
//    static Element targetPackage;
//    static Element targetElement;
//
//    public static void main(String[] args) throws Exception
//    {
////        parseArgs(args);
//        System.out.println("In main method");
//        JUnitCore jCore = new JUnitCore();
//        Result results = jCore.run(CoordinatedSyncMDElementCreate.class);
////        printJunitResults(results);
//        System.exit(0);
//    }
//
//    public CoordinatedSyncMDElementCreate (String testMethodToRun, String testName)
//    {
//        super(testMethodToRun, testName);
//        setDoNotUseSilentMode(true);
//        System.setProperty("jsse.enableSNIExtension", "false");
//        setSkipMemoryTest(true);
//        setDoNotUseSilentMode(true);
//    }
//
//    @Test
//    public void executeTest() throws Exception {
//        // do setup here
//
//        // load credentials from file
//        MDKTestHelper.loadCredentials("");
//
//        // set MMS credentials
//        MDKHelper.setMMSLoginCredentials(teamworkUsername, teamworkPassword);
//
//        //open project
//        projectName = "CoordinatedSyncTest";
//        testProject = testRoot + "/mdk/resource/" + projectName + ".mdzip";
//        MDKTestHelper.openProject();
//
//        //clean and prepare test environment
//        MagicDrawHelper.createSession();
//        MagicDrawHelper.deleteEditableContainerChildren(ElementFinder.getModelRoot());
//        MagicDrawHelper.closeSession();
//
//        //make sure expected stuff is in place
//        MagicDrawHelper.createSession();
//        targetPackage = MagicDrawHelper.createPackage("CreatePkg", ElementFinder.getModelRoot());
//        MagicDrawHelper.closeSession();
//        MDKTestHelper.saveUpdatedProject();
//    }
//
//    public void executeTest() {
//        //testing vars
//        String updatedDocumentation = "New documentation";
//        String specType = "Product";
//
//        //confirm mms permissions
//        super.confirmMMSPermissions();
//
//        //create / change / delete
//        MagicDrawHelper.createSession();
//        targetElement = MagicDrawHelper.createDocument("CreateDoc", targetPackage);
//        MagicDrawHelper.setElementDocumentation(targetElement, updatedDocumentation);
//        MagicDrawHelper.closeSession();
//
//        // save model to push changes
//        super.saveUpdatedProject();
//
//        // confirm via rest
//        JSONObject jo = MDKHelper.getMmsElement(targetElement);
//        if (!jo.get("sysmlid").equals(targetElement.getID())) {
//            fail("Returned element sysmlid does not match returned json");
//        }
//        JSONObject joSpec = (JSONObject) jo.get("specialization");
//        if (!joSpec.get("type").equals(specType)) {
//            fail("Returned element type does not match " + specType);
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
//*/
//}
