
package gov.nasa.jpl.mbee.mdk.test.tests;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.test.elements.MDKProject;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ablack
 * @JIRA MAGICDRAW-263
 *
 */
public class UtilsGetStereotypeTeamworkCloudTest {

    private Project project = new MDKProject(false);

    public UtilsGetStereotypeTeamworkCloudTest() {
    }

//    @BeforeClass
//    public static void setupProject() throws IOException, ServerException, URISyntaxException {
//        MDKOptionsGroup.getMDKOptions().setDefaultValues();
//        MDKOptionsGroup.getMDKOptions().setLogJson(true);
//
//        credentialsFile = File.createTempFile("creds", ".properties");
//        credentialsFile.deleteOnExit();
//        Files.copy(Paths.get(credentials), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        MDKTestHelper.twCloudLoadProject(projectId, null, credentialsFile, "");
//    }

    /********************************************** Direct Stereotype Utils **********************************************/

    @Test
    public void _utils_getConformsStereotype() {
        BaseElement test = Utils.getConformsStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Conform"));
    }

    @Test
    public void _utils_get18ExposeStereotype() {
        BaseElement test = Utils.get18ExposeStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Expose"));
    }

    @Test
    public void _utils_getElementGroupStereotype() {
        BaseElement test = Utils.getElementGroupStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("ElementGroup"));
    }

    @Test
    public void _utils_getViewStereotype() {
        BaseElement test = Utils.getViewStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("View"));
    }

    @Test
    public void _utils_getViewpointStereotype() {
        BaseElement test = Utils.getViewpointStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Viewpoint"));
    }

    @Test
    public void _utils_getAccountableForStereotype() {
        BaseElement test = Utils.getAccountableForStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("accountableFor"));
    }

    @Test
    public void _utils_getApprovesStereotype() {
        BaseElement test = Utils.getApprovesStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("approves"));
    }

    @Test
    public void _utils_getAspectStereotype() {
        BaseElement test = Utils.getAspectStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("aspect"));
    }

    @Test
    public void _utils_getCharacterizesStereotype() {
        BaseElement test = Utils.getCharacterizesStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("characterizes"));
    }

    @Test
    public void _utils_getConcursStereotype() {
        BaseElement test = Utils.getConcursStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("concurs"));
    }

    @Test
    public void _utils_getDirectedConnectorStereotype() {
        BaseElement test = Utils.getDirectedConnectorStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("DirectedConnector"));
    }

    @Test
    public void _utils_getDocumentStereotype() {
        BaseElement test = Utils.getDocumentStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Document"));
    }

    @Test
    public void _utils_getJobStereotype() {
        BaseElement test = Utils.getJobStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Job"));
    }

    @Test
    public void _utils_getPrecedesStereotype() {
        BaseElement test = Utils.getPrecedesStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("precedes"));
    }

    @Test
    public void _utils_getProjectStaffStereotype() {
        BaseElement test = Utils.getProjectStaffStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("ProjectStaff"));
    }

    @Test
    public void _utils_getRoleStereotype() {
        BaseElement test = Utils.getRoleStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Role"));
    }

    @Test
    public void _utils_getTicketStereotype() {
        BaseElement test = Utils.getTicketStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Ticket"));
    }

    @Test
    public void _utils_getCommentStereotype() {
        BaseElement test = Utils.getCommentStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Comment"));
    }

    @Test
    public void _utils_getSysML14ConformsStereotype() {
        BaseElement test = Utils.getSysML14ConformsStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Conforms"));
    }

    @Test
    public void _utils_getExposeStereotype() {
        BaseElement test = Utils.getExposeStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Expose"));
    }

    @Test
    public void _utils_getProductStereotype() {
        BaseElement test = Utils.getProductStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("Product"));
    }

    @Test
    public void _utils_getViewClassStereotype() {
        BaseElement test = Utils.getViewClassStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("view"));
    }

    @Test
    public void _utils_getPresentsStereotype() {
        BaseElement test = Utils.getPresentsStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Stereotype);
        Assert.assertTrue(((Stereotype)test).getName().equals("presents"));
    }

    /********************************************** Direct Property Utils **********************************************/

    @Test
    public void _utils_getGeneratedFromViewProperty() {
        BaseElement test = Utils.getGeneratedFromViewProperty(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Property);
        Assert.assertTrue(((Property)test).getName().equals("generatedFromView"));
    }

    @Test
    public void _utils_getGeneratedFromElementProperty() {
        BaseElement test = Utils.getGeneratedFromElementProperty(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Property);
        Assert.assertTrue(((Property)test).getName().equals("generatedFromElement"));
    }

    @Test
    public void _utils_getViewElementsProperty() {
        BaseElement test = Utils.getViewElementsProperty(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Property);
        Assert.assertTrue(((Property)test).getName().equals("elements"));
    }

    /********************************************** Direct Component Utils **********************************************/

    @Test
    public void _utils_getSiteCharacterizationComponent() {
        BaseElement test = Utils.getSiteCharacterizationComponent(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(test instanceof Component);
        Assert.assertTrue(((Component)test).getName().equals("Site Characterization"));
    }


//    @AfterClass
//    public static void closeProject() throws IOException {
//        MagicDrawHelper.closeProject();
//        EsiUtils.getTeamworkService().logout();
//    }

}