package gov.nasa.jpl.mbee.mdk.test.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.tests.MagicDrawTestRunner;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author ablack
 * @JIRA MAGICDRAW-263
 *
 */
@RunWith(MagicDrawTestRunner.class)
public class CoordinatedSyncConflictMDDeleteMMSUpdate {

    private static Element targetElement;
    private static Element targetPackage;
    private static String filename = "/CSyncTest.mdzip";
    private static File testProjectFile;
    private static File credentials = new File(CoordinatedSyncConflictMDDeleteMMSUpdate.class.getResource("/mms.properties").getPath());

    public CoordinatedSyncConflictMDDeleteMMSUpdate() {
    }

    @BeforeClass
    public static void setupProject() throws IOException, ServerException, URISyntaxException {
        MDKOptionsGroup.getMDKOptions().setDefaultValues();
        MDKOptionsGroup.getMDKOptions().setLogJson(true);
//        System.out.println(MDKOptionsGroup.getMDKOptions().isLogJson());
//        System.out.println(MDKOptionsGroup.getMDKOptions().isPersistChangelog());
//        System.out.println(MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled());
//        System.out.println(MDKOptionsGroup.getMDKOptions().isCoordinatedSyncEnabled());
        testProjectFile = File.createTempFile("prj", ".mdzip");
        testProjectFile.deleteOnExit();
        Files.copy(CoordinatedSyncConflictMDDeleteMMSUpdate.class.getResourceAsStream(filename), testProjectFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        MDKTestHelper.loadLocalProject(testProjectFile, credentials, "");

        MagicDrawHelper.openProject(testProjectFile);

        //clean and prepare test environment
        MagicDrawHelper.createSession();
        try {
            MagicDrawHelper.clearModel();
        } catch (ReadOnlyElementException roee)  {
            System.out.println(roee.getMessage() + ": " + roee.getElement().getHumanName());
        }
        MagicDrawHelper.closeSession();

        //make sure expected stuff is in place
        MagicDrawHelper.createSession();
        targetPackage = MagicDrawHelper.createPackage("ConflictPkg", ElementFinder.getModelRoot());
        targetElement = MagicDrawHelper.createDocument("ConflictDoc", targetPackage);
        MagicDrawHelper.setElementDocumentation(targetElement, "Initial documentation.");
        MagicDrawHelper.closeSession();
        ///
        MagicDrawHelper.saveProject(testProjectFile.getAbsolutePath());
        MDKHelper.loadCoordinatedSyncValidations();
        MDKHelper.getValidationWindow().listPooledViolations();
        MDKHelper.getValidationWindow().commitAllMDChangesToMMS();
    }

    @Test
    public void executeTest() throws IOException, ServerException, URISyntaxException {
        String updatedMMSDocumentation = "Changed documentation.";
        String targetSysmlID = targetElement.getID();

        //update mms element without md session so it's not tracked in model, export its json to mms to update it and trigger pending
        MDKHelper.setSyncTransactionListenerDisabled(true);
        MagicDrawHelper.createSession();
        MagicDrawHelper.setElementDocumentation(targetElement, updatedMMSDocumentation);
        try {
            ObjectNode jsob = Converters.getElementToJsonConverter().apply(targetElement, Application.getInstance().getProject());
            System.out.println("******");
            System.out.println(jsob.asText());
            System.out.println("******");
            Collection<Element> postElements = new ArrayList<>();
            postElements.add(targetElement);
            MDKTestHelper.postMmsElements(postElements);
        } catch (IllegalStateException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        MagicDrawHelper.cancelSession();
        MDKHelper.setSyncTransactionListenerDisabled(false);
        MDKTestHelper.waitXSeconds(5);

        //confirm mms element update
        ObjectNode jo = MDKHelper.getElement(targetElement, Application.getInstance().getProject());
        JsonNode returnedElements;
        if ((returnedElements = jo.get("elements")) != null && returnedElements.isArray()) {
            JsonNode value = null;
            for (JsonNode val : returnedElements) {
                if (val.get(MDKConstants.ID_KEY) != null && val.get(MDKConstants.ID_KEY).isTextual()
                        && val.get(MDKConstants.ID_KEY).asText().equals(targetElement.getID())) {
                    value = val;
                    break;
                }
            }
            if (value == null) {
                Assert.fail("Element not returned by MMS");
            }
            if ((value = value.get("documentation")) != null  && value.isTextual()) {
                System.out.println(value.asText());
            }
            else {
                Assert.fail("Retrieved element documentation failed or did not match");
            }
        }
        else {
            Assert.fail("Unable to retrieve elements from MMS");
        }

        //delete local element
        MagicDrawHelper.createSession();
        try {
            MagicDrawHelper.deleteMDElement(targetElement);
        } catch (ReadOnlyElementException e) {
            MagicDrawHelper.cancelSession();
            throw new IOException("Unable to delete element id " + targetElement.getID());
        }
        MagicDrawHelper.closeSession();

        //confirm local delete
        Assert.assertNull(ElementFinder.getElement("Document", "UpdateDoc", targetPackage));

        // save model to push changes
        MagicDrawHelper.saveProject(testProjectFile.getAbsolutePath());

        //confirm conflict found and recorded properly
        boolean foundViolation = false;
        MDKHelper.loadCoordinatedSyncValidations();
        MDKHelper.getValidationWindow().listPooledViolations();
        for (ValidationRuleViolation vrv : MDKHelper.getValidationWindow().getPooledValidations("Element Equivalence")) {
            if (vrv.getComment().contains(targetSysmlID)) {
                foundViolation = true;
                break;
            }
        }
        if (!foundViolation) {
            Assert.fail("Conflict for target element not reported in violations.");
        }
        Collection<Element> syncElements = ElementFinder.getElement("Package", "__MMSSync__").getOwnedElement();
        for (Element se : syncElements) {
            if (!MagicDrawHelper.getElementDocumentation(se).contains(targetSysmlID)) {
                Assert.fail("Conflict not recorded in MMSSync element " + se.getHumanName());
            }
        }

    }

    @AfterClass
    public static void closeProject() throws IOException {
        MagicDrawHelper.createSession();
        try {
            MagicDrawHelper.clearModel();
        } catch (ReadOnlyElementException e) {
            MagicDrawHelper.cancelSession();
        }
        MagicDrawHelper.closeSession();

        // save model to push changes
        MagicDrawHelper.saveProject(testProjectFile.getAbsolutePath());

        MagicDrawHelper.closeProject();
    }

    /*

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
            MDKHelper.postMmsElements(jsob);
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