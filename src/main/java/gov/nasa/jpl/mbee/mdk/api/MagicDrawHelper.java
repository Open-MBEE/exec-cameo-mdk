/*******************************************************************************
 * Copyright (c) <2016>, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory,
 *    nor the names of its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package gov.nasa.jpl.mbee.mdk.api;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.impl.ElementsFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;


/**
 * This class has functions that uses the MDK api to test the MDK actions.
 */
public class MagicDrawHelper {

    private static Project project;
    private static ElementsFactory ef;

    /**
     * Convenience method to ensure that we always have an ElementsFactory available.
     */
    private static void initializeFactory() {
        if (ef != null) {
            return;
        }
        project = Application.getInstance().getProject();
        ef = project.getElementsFactory();
    }

    /*****************************************************************************************
     *
     * Project load / close functions
     *
     *****************************************************************************************/

    public static ProjectDescriptor openProject(File file) throws IOException {
        return openProject(file.toURI());
    }

    public static ProjectDescriptor openProject(URI uri) throws IOException {
        final ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory.createProjectDescriptor(uri);
        if (projectDescriptor == null) {
            throw new IOException(uri.toString() + " could not generate a project descriptor.");
        }
        final ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
        projectsManager.loadProject(projectDescriptor, true);
        final Project project = projectsManager.getActiveProject();
        if (project == null) {
            throw new IOException(uri.toString() + " could not be loaded into MagicDraw.");
        }
        return projectDescriptor;
    }



    public static void closeProject() {
        Application.getInstance().getProjectsManager().closeProject();
    }


    /*****************************************************************************************
     *
     * Session management functions
     *
     *****************************************************************************************/

    /**
     * Creates a MagicDraw Session. All changes to be recorded in model programmatically
     * must occur after a session is opened, and will be recorded when the session is closed.
     * A cancelled session will cause the changes to be lost.
     *
     * @throws IllegalStateException
     */
    public static void createSession() throws IllegalStateException {
        if (SessionManager.getInstance().isSessionCreated()) {
            throw new IllegalStateException("Unable to create session: a session is already open.");
        }
        SessionManager.getInstance().createSession("Programmatic changes");
        initializeFactory();
    }

    /**
     * Closes an open session, causing all programmatically completed changes in the current
     * session to be reflected in the model.
     *
     * @throws IllegalStateException
     */
    public static void closeSession() throws IllegalStateException {
        if (!SessionManager.getInstance().isSessionCreated()) {
            throw new IllegalStateException("Unable to close session: no session has been created to close.");
        }
        SessionManager.getInstance().closeSession();
    }



    /*****************************************************************************************
     *
     * Logging functions
     *
     *****************************************************************************************/

    /**
     * Prints a message to console and MD log
     */
    public static void generalMessage(String s) {
        Application instance = Application.getInstance();
        instance.getGUILog().log(s);
        System.out.println(s);
    }




}
