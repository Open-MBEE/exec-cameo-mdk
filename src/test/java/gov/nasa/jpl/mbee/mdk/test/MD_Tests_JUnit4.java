package gov.nasa.jpl.mbee.mdk.test;

//import com.jprofiler.api.agent.Controller;

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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@RunWith(MagicDrawTestRunner.class)
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MD_Tests_JUnit4 {
    @Test
    public void name() throws Exception {
        Assert.assertTrue(true);
    }
}



/*
    private String[] element100Array = new String[110];
    private String[] element1000Array = new String[1110];
    private static Project project;
    private static ElementsFactory ef;
    private String projectLocation = "/Users/cmcmilla/Documents/18.4 Evaluation Models/OpenCAE 184-2.mdzip";
    //private Connection connection;

    @Before
    public void setUp() {
        //Controller.startCPURecording(true);
    }

    @Test
    public void testAdd100() {
//        try {
//            connection = ConnectionFactory.createRemoteConnection("cae-teamworkcloud-uat.jpl.nasa.gov", 8849, 10);
//            connection.close();
//        } catch (Exception e) {
//            System.out.println("This didn't work");
//        }

        Project project = loadProject(projectLocation);

        Element curPack;
        Class curBlock;
        for (int i = 0; i < 10; i++) {
            curPack = createPackage("Test" + (i+1));
            element100Array[(i*11)] = curPack.getID();
            for (int j = 0; j < 10; j++) {
                curBlock = createBlock("b" + (j+1), curPack);
                element100Array[(i*11+j+1)] = curBlock.getID();
            }

        }
        System.out.println(element100Array[1]);
        Element test;
        for (int i = 0; i < 110; i++) {
            test = (Element) project.getElementByID(element100Array[i]);
            try {
                deleteMDElement(test);
            } catch (Exception e) {
                System.out.println("Delete failed- " + i);
            }
        }
        System.out.println("Did I get here?");
        saveProject( project, projectLocation);
        System.out.println("here?");
        //Controller.saveSnapshot(new File("/Users/cmcmilla/Desktop/SnapShots/testSnapshot.jps"));
        //Controller.stopCPURecording();

        Application.getInstance().getProjectsManager().closeProject();
        System.out.println("here2?");
    }
*/


    /*
    @Test
    public void testDelete100() {
        Project project = loadProject(projectLocation);
        System.out.println(element100Array[1]);
        Element test;
        for (int i = 0; i < 55; i++) {
            test = (Element) project.getElementByID(element100Array[i]);
            try {
                deleteMDElement(test);
            } catch (Exception e) {
                System.out.println("Delete failed");
            }
        }
        //ModelElementsManager m1 = new ModelElementsManager();

        saveProject( project, projectLocation);
        Application.getInstance().getProjectsManager().closeProject();
    }
    */

    /*
    @Test
    @SuppressWarnings({"unused", "ConstantConditions"})
    public void testLoadSampleProject()
    {

        //String installRoot = Application.environment().getInstallRoot();
        //Project project = loadProject(installRoot + "samples/diagrams/class diagram.mdzip");
        //System.out.println('.');
        //String projectLocation = "/Users/cmcmilla/Documents/18.4 Evaluation Models/OpenCAE 184-2.mdzip";
        //String projectLocation = "/Users/cmcmilla/Documents/SmallTest.mdzip";
        //String projectLocation = "/Users/cmcmilla/Documents/18.4 Evaluation Models/Europa Architecture 184.mdzip";

        //System.out.println('.');
        //Project project = loadProject("/Users/cmcmilla/Documents/SmallTest.mdzip");
        Project project = loadProject(projectLocation);



        //PUT LOGIN INFO HERE, BUT WON'T KEEP IT HERE FOR NOW
        //EsiUtils.getTeamworkService().login(new ServerLoginInfo("cae-teamworkcloud-uat.jpl.nasa.gov:10000", "cmcmilla", "PASSWORD", false), false);


        //assertEquals(1, project.getModels().size());
        Element aPack = createPackage("apples");
        createBlock("testBlock", aPack);
        createBlock("testBlock2");
        createPackage("oranges");
        createPackage("bananas");

        saveProject( project, projectLocation);
        Application.getInstance().getProjectsManager().closeProject();

        assertTrue(true);
    }*/

/*
    public Element createPackage(String name, Element owner) {
        System.out.println('.');
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession("Automated changes");
        }
        //System.out.println('.');

        Project prj = Application.getInstance().getProject();
        //System.out.println('.');

        ElementsFactory ef = prj.getElementsFactory();
        //System.out.println('.');

        Package newPackage = ef.createPackageInstance();

        //System.out.println('.');
        newPackage.setName(name);
        //System.out.println('.');
        newPackage.setOwner(owner);
        //System.out.println('.');

        if (SessionManager.getInstance().isSessionCreated()) {
            //System.out.println('.');
            SessionManager.getInstance().closeSession();
        }

        return newPackage;
    }

    public Element createPackage(String name) {
        Project prj =  Application.getInstance().getProject();
        return createPackage(name, prj.getPrimaryModel());

    }

    public static Class createBlock(String name, Element owner) {
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession("Automated changes");
        }
        Project prj = Application.getInstance().getProject();
        ElementsFactory ef = prj.getElementsFactory();
        Class block = ef.createClassInstance();
        Stereotype blockstp = (Stereotype)getElementByQualifiedName("SysML::Blocks::Block");
        StereotypesHelper.addStereotype(block, blockstp);
        block.setName(name);
        block.setOwner(owner);

        if (SessionManager.getInstance().isSessionCreated()) {
            //System.out.println('.');
            SessionManager.getInstance().closeSession();
        }

        return block;

    }
    public static Class createBlock(String name) {
        Project prj = Application.getInstance().getProject();
        return createBlock(name, prj.getPrimaryModel());
    }

    @SuppressWarnings("deprecation")
    private static Project loadProject(String projectPath) {
        return MagicDrawTestCase.loadProject(projectPath);
    }

    private void saveProject(Project project, String fileName) {
        File file = new File(fileName);
        Tester m1 = new Tester();
        m1.saver(project, file);
    }

    private class Tester extends MagicDrawTestCase {
        public Tester() {
            super();
        }

        public void saver(Project project, File file) {
            super.saveProject(project, file);
        }
    }

    public static Element getElementByQualifiedName(String qualifiedName) {
        String[] path = qualifiedName.split("::");
        Element curElement = Application.getInstance().getProject().getPrimaryModel();
        for (int i = 0; i < path.length; i++) {
            curElement = findChildByName(curElement, path[i]);
            if (curElement == null)
                return null;
        }
        return curElement;
    }

    public static Element findChildByName(Element owner, String name) {
        for (Element e: owner.getOwnedElement()) {
            if (e instanceof NamedElement) {
                if (((NamedElement)e).getName().equals(name))
                    return e;
            }
        }
        return null;
    }

    public static void deleteMDElement(Element ele) throws Exception {
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession("Automated changes");
        }
        try {
            ModelElementsManager.getInstance().removeElement(ele);
        } catch (Exception e) {
            SessionManager.getInstance().cancelSession();
            throw e;
        }
        if (SessionManager.getInstance().isSessionCreated()) {
            //System.out.println('.');
            SessionManager.getInstance().closeSession();
        }
    }

    public static void publishToCC() throws IOException {
        StringBuilder diff = new StringBuilder();
        //String cmd = "/usr/local/bin/diff-pdf --verbose --output-diff=/Users/brower/git/ems-rci/mdk/output/DIFF_" + name + " " + ref + " " + out;
        String cmd = "placeholder";
        System.out.println("running command: " + cmd);

        String[] command = cmd.split(" ");
        ProcessBuilder p = new ProcessBuilder(command);
        Process p2 = p.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p2.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p2.getErrorStream()));

        // read the output from the command
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            diff.append("DIFF: " + s + "\n");
        }

        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            diff.append("ERROR: " + s + "\n");
        }

    }


}
*/