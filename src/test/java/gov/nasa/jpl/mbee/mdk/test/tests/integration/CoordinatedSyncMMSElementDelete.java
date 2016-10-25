package gov.nasa.jpl.mbee.mdk.test.tests.integration;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.mdk.test.tests.MDKTestCase;


/**
 * @author ablack
 * @JIRA MAGICDRAW-263
 *
 */
public class CoordinatedSyncMMSElementDelete extends MDKTestCase 
{
    
    static String testName = "CoordinatedSyncMDElementDelete";
    static Element targetPackage;
    static Element targetElement;
    
    public static void main(String[] args) throws Exception 
    {   
        parseArgs(args);
        System.out.println("In main method");
        JUnitCore jCore = new JUnitCore();
        Result results = jCore.run(CoordinatedSyncMMSElementDelete.class);
        printJunitResults(results);
        System.exit(0);
    }

    public CoordinatedSyncMMSElementDelete (String testMethodToRun, String testName) 
    {
        super(testMethodToRun, testName);
        setDoNotUseSilentMode(true);
        System.setProperty("jsse.enableSNIExtension", "false");
        setSkipMemoryTest(true);
        setDoNotUseSilentMode(true);    
    }
/*
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();        
        
        suite.addTest(new CoordinatedSyncMMSElementDelete("executeTest", "execute test"));
        
        return suite;
    }
    
    @Override
    protected void setUpTest() throws Exception {
        super.setUpTest();
        // do setup here
        
        // load credentials from file
        loadCredentials("");

        // set MMS credentials
        MDKHelper.setMMSLoginCredentials(teamworkUsername, teamworkPassword);
        
        //open project
        projectName = "CoordinatedSyncTest";
        testProject = testRoot + "/mdk/resource/" + projectName + ".mdzip";
        super.openProject();

        //clean and prepare test environment
        MagicDrawHelper.createSession();
        MagicDrawHelper.deleteEditableContainerChildren(ElementFinder.getModelRoot());
        MagicDrawHelper.closeSession();
        
        //make sure expected stuff is in place
        MagicDrawHelper.createSession();
        targetPackage = MagicDrawHelper.createPackage("DeletePkg", ElementFinder.getModelRoot());
        targetElement = MagicDrawHelper.createDocument("DeleteDoc", targetPackage);
        MagicDrawHelper.setElementDocumentation(targetElement, "Initial documentation.");
        MagicDrawHelper.closeSession();
        super.saveUpdatedProject();
    }

    public void executeTest() {
        //confirm mms permissions
        super.confirmMMSPermissions();
        
        //create / change / delete
        try {
            MDKHelper.deleteMmsElement(targetElement);
        } catch (IllegalStateException e) {
            fail("Document does not exist to remove.");
        }
        
        //confirm still exists locally
        if (ElementFinder.getElement("Document", "DeleteDoc", targetPackage) == null) {
            fail("Element removed from MD before coordinated sync occurred.");
        }
        
        // save model to accept changes
        super.saveUpdatedProject();
        
        // confirm element removal
        if (ElementFinder.getElement("Document", "DeleteDoc", targetPackage) != null) {
            fail("Element not removed from MD after coordinated sync occurred.");
        }
     }
    
    @Override
    protected void tearDownTest() throws Exception {
        super.tearDownTest();
        // do tear down here
        
        //close project
        super.closeProject();
    }
  */
}
