package gov.nasa.jpl.mbee.mdk.test.tests.integration;

import gov.nasa.jpl.mbee.mdk.test.framework.MDKTestCase;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


/**
 * @author ablack
 * @JIRA MAGICDRAW-263
 *
 */
public class CoordinatedSyncMDElementUpdate extends MDKTestCase
{
    
    static String testName = "CoordinatedSyncMDElementUpdate";
    static Element targetPackage;
    static Element targetElement;
    
    public static void main(String[] args) throws Exception 
    {   
        parseArgs(args);
        System.out.println("In main method");
        JUnitCore jCore = new JUnitCore();
        Result results = jCore.run(CoordinatedSyncMDElementUpdate.class);
        printJunitResults(results);
        System.exit(0);
    }

    public CoordinatedSyncMDElementUpdate (String testMethodToRun, String testName) 
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
        
        suite.addTest(new CoordinatedSyncMDElementUpdate("executeTest", "execute test"));
        
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
        targetPackage = MagicDrawHelper.createPackage("UpdatePkg", ElementFinder.getModelRoot());
        targetElement = MagicDrawHelper.createDocument("UpdateDoc", targetPackage);
        MagicDrawHelper.setElementDocumentation(targetElement, "Initial documentation.");
        MagicDrawHelper.closeSession();
        super.saveUpdatedProject();
    }

    public void executeTest() {
        
        String updatedDocumentation = "Modified documentation";

        //confirm mms permissions
        super.confirmMMSPermissions();
        
        //create / change / delete
        MagicDrawHelper.createSession();
        MagicDrawHelper.setElementDocumentation(targetElement, updatedDocumentation);
        MagicDrawHelper.closeSession();
                
        // save model to push changes
        super.saveUpdatedProject();
        
        // confirm via rest
        JSONObject jo = MDKHelper.getMmsElement(targetElement);
        assertTrue(jo.get("documentation").equals(updatedDocumentation));
        
        // confirm multiple elements
//        ArrayList<Element> targets = new ArrayList<Element>();
//        targets.add(document);
//        JSONObject jo2 = null;
//        try {
//            jo2 = MDKHelper.getManyMMSJSON(targets);
//        } catch (ServerException se) {
//            System.out.println(se.getMessage() + " | " + se.getResponse());
//        }
//        JSONArray arrayObj = (JSONArray) jo2.get("elements");
//        assert(updatedDocumentation == ((JSONObject) arrayObj.get(0)).get("documentation"));
        
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
