package gov.nasa.jpl.mbee.mdk.test.tests.integration;

import gov.nasa.jpl.mbee.mdk.test.tests.MDKTestCase;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


/**
 * @author ablack
 * @JIRA MAGICDRAW-263
 *
 */
public class CoordinatedSyncMMSElementCreate extends MDKTestCase
{
    
    static String testName = "CoordinatedSyncMDElementCreate";
    static String targetSysMLID;
    static Element targetPackage;
    static Element targetElement;
    
    public static void main(String[] args) throws Exception 
    {   
        parseArgs(args);
        System.out.println("In main method");
        JUnitCore jCore = new JUnitCore();
        Result results = jCore.run(CoordinatedSyncMMSElementCreate.class);
        printJunitResults(results);
        System.exit(0);
    }

    public CoordinatedSyncMMSElementCreate (String testMethodToRun, String testName) 
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
        
        suite.addTest(new CoordinatedSyncMMSElementCreate("executeTest", "execute test"));
        
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
        targetPackage = MagicDrawHelper.createPackage("CreatePkg", ElementFinder.getModelRoot());
        MagicDrawHelper.closeSession();
        super.saveUpdatedProject();
    }

    public void executeTest() {
        //confirm MMS authorization
        super.confirmMMSPermissions();
        
        //create / change / delete
        // create element without md session so it's not tracked in model, export its json to mms to create it and trigger pending local update
        MDKHelper.setSyncTransactionListenerDisabled(true);
        MagicDrawHelper.createSession();
        Element mmsElement = MagicDrawHelper.createDocument("MMSCreatedDoc", targetPackage);
        JSONObject jsob = ExportUtility.fillElement(mmsElement, null);
        MagicDrawHelper.cancelSession();
        MDKHelper.setSyncTransactionListenerDisabled(false);
        try {
            MDKHelper.postMmsElement(jsob);
        } catch (IllegalStateException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        // save model to pull changes
        super.saveUpdatedProject();

        // confirm import
        targetElement = ElementFinder.getElement("Document", "MMSCreatedDoc");
        assertTrue(targetElement.getID().equals(jsob.get("sysmlid")));
        assertTrue(targetElement.getOwner().equals(targetPackage));        
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
