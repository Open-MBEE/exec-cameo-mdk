//<<<<<<< Updated upstream
//package gov.nasa.jpl.mbee.mdk.test.tests.integration;
//
//import gov.nasa.jpl.mbee.mdk.test.framework.MDKTestCase;
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
//public class CoordinatedSyncConflictMDUpdateMMSDelete extends MDKTestCase
//{
//
//    static String testName = "CoordinatedSyncMDElementUpdate";
//    static Element targetPackage;
//    static Element targetElement;
//
//    public static void main(String[] args) throws Exception
//    {
//        parseArgs(args);
//        System.out.println("In main method");
//        JUnitCore jCore = new JUnitCore();
//        Result results = jCore.run(CoordinatedSyncConflictMDUpdateMMSDelete.class);
//        printJunitResults(results);
//        System.exit(0);
//    }
//
//    public CoordinatedSyncConflictMDUpdateMMSDelete (String testMethodToRun, String testName)
//    {
//        super(testMethodToRun, testName);
//        setDoNotUseSilentMode(true);
//        System.setProperty("jsse.enableSNIExtension", "false");
//        setSkipMemoryTest(true);
//        setDoNotUseSilentMode(true);
//    }
//
//    /*
//    public static Test suite() throws Exception
//    {
//        TestSuite suite = new TestSuite();
//
//        suite.addTest(new CoordinatedSyncConflictMDUpdateMMSDelete("executeTest", "execute test"));
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
//        targetPackage = MagicDrawHelper.createPackage("ConflictPkg", ElementFinder.getModelRoot());
//        targetElement = MagicDrawHelper.createDocument("ConflictDoc", targetPackage);
//        MagicDrawHelper.setElementDocumentation(targetElement, "Initial documentation.");
//        MagicDrawHelper.closeSession();
//        super.saveUpdatedProject();
//    }
//
//    public void executeTest() {
//
//        String updatedDocumentation = "Modified documentation";
//
//        //confirm mms permissions
//        super.confirmMMSPermissions();
//
//        //update local md element
//        MagicDrawHelper.createSession();
//        MagicDrawHelper.setElementDocumentation(targetElement, updatedDocumentation);
//        MagicDrawHelper.closeSession();
//        //confirm local update
//        assertTrue(MagicDrawHelper.getElementDocumentation(targetElement).equals(updatedDocumentation));
//
//        //delete mms element
//        try {
//            MDKHelper.deleteMmsElement(targetElement);
//        } catch (IllegalStateException e) {
//            fail("Unable to delete element, or element already deleted");
//        }
//        //confirm mms change
//        JSONObject jo = MDKHelper.getMmsElement(targetElement);
//        assertNull(jo);
//
//        // save model to push changes
//        referenceProject =  testRoot + "/mdk/reference/" + projectName + ".mdzip";
//        super.saveUpdatedProject();
//
//        //confirm conflict found and recorded
//        Collection<Element> violationTargets = new ArrayList<Element>();
//        violationTargets.add(targetElement);
//        if (!MDKHelper.getCoordinatedSyncValidationWindow().confirmElementValidationTypeResult("[EXIST]", violationTargets).isEmpty()) {
//            fail("Conflict for target element not reported in violations.");
//        }
//        Collection<Element> syncElements = ElementFinder.getElement("Package", "__MMSSync__").getOwnedElement();
//        for (Element se : syncElements) {
//            if (!MagicDrawHelper.getElementDocumentation(se).contains(targetElement.getID())) {
//                fail("Conflict not recorded in MMSSync element " + se.getHumanName());
//                System.out.println("Conflict not recorded in MMSSync element " + se.getHumanName());
//            }
//        }
//    }
//
//    @Override
//    protected void tearDownTest() throws Exception {
//        super.tearDownTest();
//        // do tear down here
//
//        //clear pending messages
////        MDKHelper.getCoordinatedSyncValidationWindow().commitMDChangesToMMS("[EXIST]");
//        super.saveUpdatedProject();
//
//        //close project
//        super.closeProject();
//    }
//    */
//}
//=======
////package gov.nasa.jpl.mbee.mdk.test.tests.integration;
////
////import com.nomagic.magicdraw.tests.MagicDrawTestCase;
//import gov.nasa.jpl.mbee.mdk.test.tests.MDKTestHelper;
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
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
//public class CoordinatedSyncConflictMDUpdateMMSDelete extends MagicDrawTestCase
//{
//
//    static String testName = "CoordinatedSyncMDElementUpdate";
//    static Element targetPackage;
//    static Element targetElement;
//
//    public static void main(String[] args) throws Exception
//    {
//    }
//
//    public CoordinatedSyncConflictMDUpdateMMSDelete ()
//    {
//        super("executeTest");
////        super("executeTest", testName);
////        setDoNotUseSilentMode(true);
////        System.setProperty("jsse.enableSNIExtension", "false");
////        setSkipMemoryTest(true);
////        setDoNotUseSilentMode(true);
//    }
//
//
//    public static Test suite() throws Exception
//    {
//        TestSuite suite = new TestSuite();
//        //asdt
//
//        suite.addTest(new CoordinatedSyncConflictMDUpdateMMSDelete());
//
//        return suite;
//    }
//
//    public void executeTest() {
//        assertTrue(true);
//    }
//
//        /*
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
//        targetPackage = MagicDrawHelper.createPackage("ConflictPkg", ElementFinder.getModelRoot());
//        targetElement = MagicDrawHelper.createDocument("ConflictDoc", targetPackage);
//        MagicDrawHelper.setElementDocumentation(targetElement, "Initial documentation.");
//        MagicDrawHelper.closeSession();
//        super.saveUpdatedProject();
//    }
//
//    public void executeTest() {
//
//        String updatedDocumentation = "Modified documentation";
//
//        //confirm mms permissions
//        super.confirmMMSPermissions();
//
//        //update local md element
//        MagicDrawHelper.createSession();
//        MagicDrawHelper.setElementDocumentation(targetElement, updatedDocumentation);
//        MagicDrawHelper.closeSession();
//        //confirm local update
//        assertTrue(MagicDrawHelper.getElementDocumentation(targetElement).equals(updatedDocumentation));
//
//        //delete mms element
//        try {
//            MDKHelper.deleteMmsElement(targetElement);
//        } catch (IllegalStateException e) {
//            fail("Unable to delete element, or element already deleted");
//        }
//        //confirm mms change
//        JSONObject jo = MDKHelper.getMmsElement(targetElement);
//        assertNull(jo);
//
//        // save model to push changes
//        referenceProject =  testRoot + "/mdk/reference/" + projectName + ".mdzip";
//        super.saveUpdatedProject();
//
//        //confirm conflict found and recorded
//        Collection<Element> violationTargets = new ArrayList<Element>();
//        violationTargets.add(targetElement);
//        if (!MDKHelper.getCoordinatedSyncValidationWindow().confirmElementValidationTypeResult("[EXIST]", violationTargets).isEmpty()) {
//            fail("Conflict for target element not reported in violations.");
//        }
//        Collection<Element> syncElements = ElementFinder.getElement("Package", "__MMSSync__").getOwnedElement();
//        for (Element se : syncElements) {
//            if (!MagicDrawHelper.getElementDocumentation(se).contains(targetElement.getID())) {
//                fail("Conflict not recorded in MMSSync element " + se.getHumanName());
//                System.out.println("Conflict not recorded in MMSSync element " + se.getHumanName());
//            }
//        }
//    }
//
//    @Override
//    protected void tearDownTest() throws Exception {
//        super.tearDownTest();
//        // do tear down here
//
//        //clear pending messages
////        MDKHelper.getCoordinatedSyncValidationWindow().commitMDChangesToMMS("[EXIST]");
//        super.saveUpdatedProject();
//
//        //close project
//        super.closeProject();
//    }
//    */
//}

