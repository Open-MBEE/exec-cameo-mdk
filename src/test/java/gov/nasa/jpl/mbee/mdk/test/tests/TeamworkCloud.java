//package gov.nasa.jpl.mbee.mdk.test.tests;
//
//import junit.framework.Test;
//import junit.framework.TestSuite;
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//
//public class TeamworkCloud extends MagicDrawTests {
//    public static void main(String[] args) throws Exception
//    {
//        twc_SetDefaults();
//        parseArgs(args);
//        //System.out.println(" --------------- In main method");
//        JUnitCore jCore = new JUnitCore();
//        Result results = jCore.run(TeamworkCloud.class);
//        printJunitResults(results);
//        System.exit(0);
//    }
//
//    public TeamworkCloud (String testMethodToRun, String testName)
//    {
//        super(testMethodToRun, testName);
//        //setDoNotUseSilentMode(false);
//        System.setProperty("jsse.enableSNIExtension", "false");
//        setSkipMemoryTest(true);
//        //setDoNotUseSilentMode(true);
//    }
//
//    public static Test suite() throws Exception
//    {
//
//
//        TestSuite suite = new TestSuite();
//
//
////        suite.addTest(new TeamworkCloud("openProject", "Open Project"));
//  //      suite.addTest(new TeamworkCloud("add100Blocks", "Add 100 Blocks"));
////        suite.addTest(new TeamworkCloud("deleteCreatedElements", "Delete all elements created"));
//
////        suite.addTest(new TeamworkCloud("openProject", "Open Project"));
////        suite.addTest(new TeamworkCloud("add100Blocks", "Add 100 Blocks"));
// //       suite.addTest(new TeamworkCloud("deleteCreatedElements", "Delete all elements created"));
//
//
////        suite.addTest(new TeamworkCloud("add1000Blocks", "Add 1000 Blocks"));
////        suite.addTest(new TeamworkCloud("add100Diagrams", "Add 100 Diagrams"));
////        suite.addTest(new TeamworkCloud("deleteCreatedElements", "Delete all elements created"));
////
////        suite.addTest(new TeamworkCloud("saveProjectOutput", "SaveProject"));
////        suite.addTest(new TeamworkCloud("closeProject", "Close Project"));
//
//        //suite.addTest(new TeamworkCloud("closeProject", "Close Project"));
//
//        //suite.addTest(new TeamworkCloud("twcLogin", "Logging in to TWC"));
//
//        suite.addTest( new TeamworkCloud("twcs_Login", "Login to Teamwork Cloud"));
//        suite.addTest( new TeamworkCloud("twcs_AddProjectTest", "Add New project to Teamwork Cloud"));
//        suite.addTest( new TeamworkCloud("twcs_OpenProjectTest", "Open Project from Teamwork Cloud"));
//        suite.addTest( new TeamworkCloud("twcs_AddDeleteElementsTest1", "Add/Commit/Delete/Commit 100 blocks"));
//        suite.addTest( new TeamworkCloud("twcs_AddDeleteElementsTest2", "Add/Commit/Delete/Commit 1000 blocks"));
//        suite.addTest( new TeamworkCloud("twcs_AddDeleteElementsTest3", "Add/Commit/Delete/Commit 100 diagrams"));
//        suite.addTest( new TeamworkCloud("twcs_UpdateProjectTest", "Update Project from Teamwork Cloud"));
//
//        suite.addTest( new TeamworkCloud("twcs_CreateBranchTest", "Create a Project Branch"));
//        suite.addTest( new TeamworkCloud("twcs_OpenBranchTest", "Open a Project Branch"));
//        suite.addTest( new TeamworkCloud("twcs_LockModelTest", "Lock Entire Model Recursively"));
//        suite.addTest( new TeamworkCloud("twcs_UnlockModelTest", "Unlock Entire Model Recursively"));
//        suite.addTest( new TeamworkCloud("twcs_Logout", "Log out of Teamwork Cloud"));
//
//        return suite;
//    }
//}
