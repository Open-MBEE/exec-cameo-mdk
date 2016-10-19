package gov.nasa.jpl.mbee.mdk.test.integration;

import java.util.ArrayList;
import java.util.Collection;

import gov.nasa.jpl.mbee.mdk.test.MDKTestCase;
import org.json.simple.JSONObject;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author ablack
 * @JIRA MAGICDRAW-263
 *
 */
public class CoordinatedSyncExtraneousConflictMDDeleteMMSDelete extends MDKTestCase
{
    
    static String testName = "CoordinatedSyncMDElementUpdate";
    static Element targetPackage;
    static Element targetElement;
    
    public static void main(String[] args) throws Exception 
    {   
        parseArgs(args);
        System.out.println("In main method");
        JUnitCore jCore = new JUnitCore();
        Result results = jCore.run(CoordinatedSyncExtraneousConflictMDDeleteMMSDelete.class);
        printJunitResults(results);
        System.exit(0);
    }

    public CoordinatedSyncExtraneousConflictMDDeleteMMSDelete (String testMethodToRun, String testName) 
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
        
        suite.addTest(new CoordinatedSyncExtraneousConflictMDDeleteMMSDelete("executeTest", "execute test"));
        
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
        targetPackage = MagicDrawHelper.createPackage("ConflictPkg", ElementFinder.getModelRoot());
        targetElement = MagicDrawHelper.createDocument("ConflictDoc", targetPackage);
        MagicDrawHelper.setElementDocumentation(targetElement, "Initial documentation.");
        MagicDrawHelper.closeSession();
        super.saveUpdatedProject();
}

    public void executeTest() {
        
        //confirm mms permissions
        super.confirmMMSPermissions();
        
        String targetSysmlID = targetElement.getID();
        
        //delete local element
        MagicDrawHelper.createSession();
        try {
            MagicDrawHelper.deleteMDElement(targetElement);
        } catch (ReadOnlyElementException e) {
            MagicDrawHelper.cancelSession();
            fail("Unable to delete element id " + targetElement.getID());
        }
        MagicDrawHelper.closeSession();
        //confirm local delete
        assertNull(ElementFinder.getElement("Document", "UpdateDoc", targetPackage));
        
        //delete mms element
        try {
            MDKHelper.deleteMmsElement(targetElement);
        } catch (IllegalStateException e) {
            fail("Unable to delete element, or element already deleted");
        }
        //confirm mms delete
        JSONObject jo = MDKHelper.getMmsElement(targetElement);
        assertNull(jo);
        
        // save model to push changes
        super.saveUpdatedProject();
        
        //confirm conflict treated as extraneous
        Collection<Element> violationTargets = new ArrayList<Element>();
        violationTargets.add(targetElement);
        if (MDKHelper.getCoordinatedSyncValidationWindow().confirmElementValidationResult(violationTargets).isEmpty()) {
            fail("Extraneous conflict recorded for target element.");
        }
        Collection<Element> syncElements = ElementFinder.getElement("Package", "__MMSSync__").getOwnedElement();
        for (Element se : syncElements) {
            if (MagicDrawHelper.getElementDocumentation(se).contains(targetSysmlID)) {
                fail("Conflict not recorded in MMSSync element " + se.getHumanName());
            }
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
