package gov.nasa.jpl.mbee.mdk.test.integration;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.tests.MagicDrawTestCase;
import com.nomagic.magicdraw.tests.MagicDrawTestRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Collection;

import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.test.MDKTestCase;

import org.json.simple.JSONObject;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


/**
 * @author ablack
 * @JIRA MAGICDRAW-263
 *
 */
@RunWith(MagicDrawTestRunner.class)
public class CoordinatedSyncConflictMDDeleteMMSUpdate extends MDKTestCase
{
    
    static String testName = "CoordinatedSyncMDElementUpdate";
    static Element targetPackage;
    static Element targetElement;

    @Test
    public void name() throws Exception {
        Assert.assertTrue(true);
    }


    public static void main(String[] args) throws Exception
    {   
        System.out.println("In main method");
        JUnitCore jCore = new JUnitCore();
        Result results = jCore.run(CoordinatedSyncConflictMDDeleteMMSUpdate.class);
        printJunitResults(results);
        System.exit(0);
    }

    public CoordinatedSyncConflictMDDeleteMMSUpdate (String testMethodToRun, String testName) 
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
        
        suite.addTest(new CoordinatedSyncConflictMDDeleteMMSUpdate("executeTest", "execute test"));
        
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
        
        String updatedMMSDocumentation = "Changed documentation.";
        
        String targetSysmlID = targetElement.getID();

        //confirm mms permissions
        super.confirmMMSPermissions();
        
        //update mms element without md session so it's not tracked in model, export its json to mms to update it and trigger pending
        MDKHelper.setSyncTransactionListenerDisabled(true);
        MagicDrawHelper.createSession();
        MagicDrawHelper.setElementDocumentation(targetElement, updatedMMSDocumentation);
        JSONObject jsob = ExportUtility.fillElement(targetElement, null);
        MagicDrawHelper.cancelSession();
        MDKHelper.setSyncTransactionListenerDisabled(false);
        try {
            MDKHelper.postMmsElement(jsob);
        } catch (IllegalStateException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        //confirm mms element update
        JSONObject jo = MDKHelper.getMmsElement(targetElement);
        assertTrue(jo.get("documentation").equals(updatedMMSDocumentation));
        
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
        
        // save model to push changes
        super.saveUpdatedProject();
        
        //confirm conflict found and recorded properly
        boolean foundViolation = false;
        searchViolations: for (ValidationRuleViolation vrv : MDKHelper.getCoordinatedSyncValidationWindow().getPooledValidations("[EXIST ON MMS]")) {
            if (vrv.getComment().contains(targetSysmlID)) {
                foundViolation = true;
                break searchViolations;
            }
        }
        if (!foundViolation) {
            fail("Conflict for target element not reported in violations.");
        }
        Collection<Element> syncElements = ElementFinder.getElement("Package", "__MMSSync__").getOwnedElement();
        for (Element se : syncElements) {
            if (!MagicDrawHelper.getElementDocumentation(se).contains(targetSysmlID)) {
                fail("Conflict not recorded in MMSSync element " + se.getHumanName());
            }
        }
    }
    
    @Override
    protected void tearDownTest() throws Exception {
        super.tearDownTest();
        // do tear down here
        
        //clear pending messages
//        MDKHelper.getCoordinatedSyncValidationWindow().commitMDChangesToMMS("[EXIST ON MMS]");
        super.saveUpdatedProject();
        
        //close project
        super.closeProject();
    }

    */
}
