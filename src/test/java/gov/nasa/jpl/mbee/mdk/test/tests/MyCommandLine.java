package gov.nasa.jpl.mbee.mdk.test.tests;

import com.nomagic.magicdraw.commandline.CommandLine;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;

import java.io.File;

/**
 * Created by igomes on 10/21/16.
 */
public class MyCommandLine extends CommandLine {

    public static void main(String[] args) {
        try {
            new MyCommandLine().launch(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte execute() {
        System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOO");
        // load project
        File fileToLoad = new File("/Users/igomes/Documents/Scratchpad_Donbot.mdzip");
        ProjectDescriptor descriptor = ProjectDescriptorsFactory.createProjectDescriptor(fileToLoad.toURI());
        ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
        projectsManager.loadProject(descriptor, true);

        Project project = Application.getInstance().getProject();
        System.out.println("PROJECT: " + project);

        // "save as" project
        /*
        File fileToSave = new File("myProject2.mdzip");
        ProjectDescriptor localProjectDescriptor = ProjectDescriptorsFactory.createLocalProjectDescriptor(project, fileToSave);
        projectsManager.saveProject(localProjectDescriptor, true);
        */
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void parseArgs(String[] args) throws Exception {
        System.out.println("AsdfASDFJDOfijSDOFIBSNODGIBOSUDBGOSIDFJASPFIUB");
    }
}
