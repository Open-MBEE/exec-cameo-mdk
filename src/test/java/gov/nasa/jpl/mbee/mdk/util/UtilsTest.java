
package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.tests.MagicDrawTestRunner;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
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
public class UtilsTest {

    private static Project project;

    public UtilsTest() {
    }

    @BeforeClass
    public static void setupProject() throws IOException, ServerException, URISyntaxException {
        ClassLoader classLoader = UtilsTest.class.getClassLoader();
        File testProjectFile = File.createTempFile("prj", ".mdzip");
        IOUtils.copy(classLoader.getResourceAsStream("CSyncTest.mdzip"), new FileOutputStream(testProjectFile));

        MDKOptionsGroup.getMDKOptions().setDefaultValues();
        MDKOptionsGroup.getMDKOptions().setLogJson(true);
        MagicDrawHelper.openProject(testProjectFile);
        project = Application.getInstance().getProject();
    }

    /********************************************** Direct Stereotype Utils **********************************************/

    @Test
    public void _utils_getConformsStereotype() {
        BaseElement test = Utils.getConformStereotype(project);
        Assert.assertNotNull(test);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Conform"));
    }

    @Test
    public void _utils_getExposeStereotype() {
        BaseElement test = Utils.getExposeStereotype(project);
        Assert.assertNotNull(test);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("Expose"));
    }

    @Test
    public void _utils_getElementGroupStereotype() {
        BaseElement test = Utils.getElementGroupStereotype(project);
        Assert.assertNotNull(test);
        System.out.println(((Stereotype)test).getName());
        Assert.assertTrue(((Stereotype)test).getName().equals("ElementGroup"));
    }

    @Test
    public void _utils_getViewStereotype() {
        BaseElement test = Utils.getViewStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Stereotype)test).getName().equals("View"));
    }

    @Test
    public void _utils_getViewpointStereotype() {
        BaseElement test = Utils.getViewpointStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Stereotype)test).getName().equals("Viewpoint"));
    }

    @Test
    public void _utils_getAspectStereotype() {
        BaseElement test = Utils.getAspectStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Stereotype)test).getName().equals("aspect"));
    }

    @Test
    public void _utils_getCharacterizesStereotype() {
        BaseElement test = Utils.getCharacterizesStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Stereotype)test).getName().equals("characterizes"));
    }

    @Test
    public void _utils_getDocumentStereotype() {
        BaseElement test = Utils.getDocumentStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Stereotype)test).getName().equals("Document"));
    }

    @Test
    public void _utils_getProductStereotype() {
        BaseElement test = Utils.getProductStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Stereotype)test).getName().equals("Product"));
    }

    @Test
    public void _utils_getViewClassStereotype() {
        BaseElement test = Utils.getViewClassStereotype(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Stereotype)test).getName().equals("view"));
    }

    /********************************************** Direct Property Utils **********************************************/

    @Test
    public void _utils_getGeneratedFromViewProperty() {
        BaseElement test = Utils.getGeneratedFromViewProperty(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Property)test).getName().equals("generatedFromView"));
    }

    @Test
    public void _utils_getGeneratedFromElementProperty() {
        BaseElement test = Utils.getGeneratedFromElementProperty(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Property)test).getName().equals("generatedFromElement"));
    }

    @Test
    public void _utils_getViewElementsProperty() {
        BaseElement test = Utils.getViewElementsProperty(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Property)test).getName().equals("elements"));
    }

    /********************************************** Direct Component Utils **********************************************/

    @Test
    public void _utils_getSiteCharacterizationComponent() {
        BaseElement test = Utils.getSiteCharacterizationComponent(project);
        Assert.assertNotNull(test);
        Assert.assertTrue(((Component)test).getName().equals("Site Characterization"));
    }

    @AfterClass
    public static void closeProject() throws IOException {
        MagicDrawHelper.closeProject();
    }

}
