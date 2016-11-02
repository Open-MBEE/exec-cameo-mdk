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
public class CoordinatedSyncMDElementCreate extends MDKTestCase
{
    //global test vars
    static String testName = "CoordinatedSyncMDElementCreate";
    static String targetSysMLID;
    static Element targetPackage;
    static Element targetElement;
    
    public static void main(String[] args) throws Exception 
    {   
        parseArgs(args);
        System.out.println("In main method");
        JUnitCore jCore = new JUnitCore();
        Result results = jCore.run(CoordinatedSyncMDElementCreate.class);
        printJunitResults(results);
        System.exit(0);
    }

    public CoordinatedSyncMDElementCreate (String testMethodToRun, String testName) 
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
        
        suite.addTest(new CoordinatedSyncMDElementCreate("executeTest", "execute test"));
        
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
        //testing vars
        String updatedDocumentation = "New documentation";
        String specType = "Product";

        //confirm mms permissions
        super.confirmMMSPermissions();
        
        //create / change / delete
        MagicDrawHelper.createSession();
        targetElement = MagicDrawHelper.createDocument("CreateDoc", targetPackage);
        MagicDrawHelper.setElementDocumentation(targetElement, updatedDocumentation);
        MagicDrawHelper.closeSession();
                
        // save model to push changes
        super.saveUpdatedProject();
        
        // confirm via rest
        JSONObject jo = MDKHelper.getMmsElement(targetElement);
        if (!jo.get("sysmlid").equals(targetElement.getID())) {
            fail("Returned element sysmlid does not match returned json");
        }
        JSONObject joSpec = (JSONObject) jo.get("specialization");
        if (!joSpec.get("type").equals(specType)) {
            fail("Returned element type does not match " + specType);
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
