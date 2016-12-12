
package gov.nasa.jpl.mbee.mdk.test.tests;

import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.tests.MagicDrawTestRunner;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
public class UtilsGetStereotypeTeamworkCloudTest {

    private static String projectId = "ID_10_20_16_12_01_59_AM__58f6714a_157e0e07e65__7ffd_LMC_055004_192_168_1_27";
    private static String credentials = "../resources/test/mms.properties";
    private static File credentialsFile;


    public UtilsGetStereotypeTeamworkCloudTest() {
    }

    @BeforeClass
    public static void setupProject() throws IOException, ServerException, URISyntaxException {
        MDKOptionsGroup.getMDKOptions().setDefaultValues();
        MDKOptionsGroup.getMDKOptions().setLogJson(true);

        credentialsFile = File.createTempFile("creds", ".properties");
        credentialsFile.deleteOnExit();
        Files.copy(Paths.get(credentials), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        MDKTestHelper.twCloudLoadProject(projectId, null, credentialsFile, "");
    }

    /********************************************** Direct Stereotype Utils **********************************************/

    @Test
    public void _utils_getConformsStereotype() {
        BaseElement test = Utils.getConformsStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Conform"));
    }

    @Test
    public void _utils_get18ExposeStereotype() {
        BaseElement test = Utils.get18ExposeStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Expose"));
    }

    @Test
    public void _utils_getElementGroupStereotype() {
        BaseElement test = Utils.getElementGroupStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("ElementGroup"));
    }

    @Test
    public void _utils_getViewStereotype() {
        BaseElement test = Utils.getViewStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("View"));
    }

    @Test
    public void _utils_getViewpointStereotype() {
        BaseElement test = Utils.getViewpointStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Viewpoint"));
    }

    @Test
    public void _utils_getAccountableForStereotype() {
        BaseElement test = Utils.getAccountableForStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("accountableFor"));
    }

    @Test
    public void _utils_getApprovesStereotype() {
        BaseElement test = Utils.getApprovesStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("approves"));
    }

    @Test
    public void _utils_getAspectStereotype() {
        BaseElement test = Utils.getAspectStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("aspect"));
    }

    @Test
    public void _utils_getCharacterizesStereotype() {
        BaseElement test = Utils.getCharacterizesStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("characterizes"));
    }

    @Test
    public void _utils_getConcursStereotype() {
        BaseElement test = Utils.getConcursStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("concurs"));
    }

    @Test
    public void _utils_getDirectedConnectorStereotype() {
        BaseElement test = Utils.getDirectedConnectorStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("DirectedConnector"));
    }

    @Test
    public void _utils_getDocumentStereotype() {
        BaseElement test = Utils.getDocumentStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Document"));
    }

    @Test
    public void _utils_getJobStereotype() {
        BaseElement test = Utils.getJobStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Job"));
    }

    @Test
    public void _utils_getPrecedesStereotype() {
        BaseElement test = Utils.getPrecedesStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("precedes"));
    }

    @Test
    public void _utils_getProjectStaffStereotype() {
        BaseElement test = Utils.getProjectStaffStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("ProjectStaff"));
    }

    @Test
    public void _utils_getRoleStereotype() {
        BaseElement test = Utils.getRoleStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Role"));
    }

    @Test
    public void _utils_getTicketStereotype() {
        BaseElement test = Utils.getTicketStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Ticket"));
    }

    @Test
    public void _utils_getCommentStereotype() {
        BaseElement test = Utils.getCommentStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Comment"));
    }

    @Test
    public void _utils_getSysML14ConformsStereotype() {
        BaseElement test = Utils.getSysML14ConformsStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Conforms"));
    }

    @Test
    public void _utils_getExposeStereotype() {
        BaseElement test = Utils.getExposeStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Expose"));
    }

    @Test
    public void _utils_getProductStereotype() {
        BaseElement test = Utils.getProductStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Product"));
    }

    @Test
    public void _utils_getViewClassStereotype() {
        BaseElement test = Utils.getViewClassStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("view"));
    }

    @Test
    public void _utils_getPresentsStereotype() {
        BaseElement test = Utils.getPresentsStereotype();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("presents"));
    }

    /********************************************** Direct Property Utils **********************************************/

    @Test
    public void _utils_getGeneratedFromViewProperty() {
        BaseElement test = Utils.getGeneratedFromViewProperty();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Property);
        System.out.println(((Property)test).getName());
        Assert.assertTrue(((Property)test).getName().equals("generatedFromView"));
    }

    @Test
    public void _utils_getGeneratedFromElementProperty() {
        BaseElement test = Utils.getGeneratedFromElementProperty();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Property);
        System.out.println(((Property)test).getName());
        Assert.assertTrue(((Property)test).getName().equals("generatedFromElement"));
    }

    @Test
    public void _utils_getViewElementsProperty() {
        BaseElement test = Utils.getViewElementsProperty();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Property);
        System.out.println(((Property)test).getName());
        Assert.assertTrue(((Property)test).getName().equals("elements"));
    }

    /********************************************** Direct Component Utils **********************************************/

    @Test
    public void _utils_getSiteCharacterizationComponent() {
        BaseElement test = Utils.getSiteCharacterizationComponent();
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Component);
        System.out.println(((Component)test).getName());
        Assert.assertTrue(((Component)test).getName().equals("Site Characterization"));
    }


    @AfterClass
    public static void closeProject() throws IOException {
        MagicDrawHelper.closeProject();
        EsiUtils.getTeamworkService().logout();
    }

}