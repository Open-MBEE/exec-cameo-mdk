/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
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
package gov.nasa.jpl.mbee.mdk.web.sync;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Subscribes event listeners to support DocGen/DocWeb sync features. Must be
 * initialized from the plugin initializer: <code>
 * ApplicationSyncEventSubscriber.subscribe();
 * </code>
 */
@Deprecated
public class ApplicationSyncEventSubscriber extends ProjectEventListenerAdapter {

    /**
     * Makes sure application event listeners are only set up once.
     */
    private static final AtomicBoolean subscribed = new AtomicBoolean(false);

    /**
     * Keeps track of project-specific event subscribers
     */
    private static final Map<String, ProjectSyncEventSubscriber> projects = new ConcurrentHashMap<String, ProjectSyncEventSubscriber>();
    private static final Map<String, Boolean> commitStates = new ConcurrentHashMap<String, Boolean>();

    /**
     * Sets up listeners, must be called from plugin initializer.
     */
    public static void subscribe() {
        // make sure we don't try subscribing twice within an application
        if (!subscribed.compareAndSet(false, true)) {
            return;
        }

        Application.getInstance().getProjectsManager()
                .addProjectListener(new ApplicationSyncEventSubscriber());
    }

    @Override
    public void projectOpened(Project project) {
        ProjectSyncEventSubscriber p = new ProjectSyncEventSubscriber(project);
        p.subscribe();
        projects.put(project.getID(), p);
        commitStates.put(project.getID(), Boolean.TRUE);
    }

    @Override
    public void projectClosed(Project project) {
        commitStates.remove(project.getID());
        ProjectSyncEventSubscriber p = projects.remove(project.getID());
        if (p != null) {
            p.unsubscribe();
        }
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        if (savedInServer || !project.isRemote()) {
            commitStates.put(project.getID(), Boolean.TRUE);
        }
    }

    public static boolean isCommitted(Project project) {
        if (project.isDirty()) {
            return false; // this case is unambiguous
        }

        // See if project is saved locally, but not committed to the server.
        Boolean committed = commitStates.get(project.getID());
        if (committed == null) { // shouldn't happen
            return false;
        }
        return committed;
    }
}
