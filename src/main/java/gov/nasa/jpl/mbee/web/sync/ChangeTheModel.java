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
package gov.nasa.jpl.mbee.web.sync;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import javax.swing.*;

/**
 * Action wrapper that handles MagicDraw sessions, leaving the action itself to
 * be implemented in a subclass. Rather than assuming that a session either
 * exists or needs to be created, this handles both cases from a
 * "defensive programming" posture.
 */
@Deprecated
public abstract class ChangeTheModel implements Runnable {

    public abstract String getDescription();

    protected abstract void makeChange();

    /**
     * Override to do something after makeChange() session. If the change uses a
     * new session, cleanUp() is called after the session is committed.
     */
    protected void cleanUp() {
    }

    @Override
    public void run() {
        boolean cancel = false;
        boolean close = false;
        try {
            if (!SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().createSession(getDescription());
                close = true;
                cancel = true;
            }
            makeChange();
            cancel = false;
        } finally {
            if (cancel) {
                SessionManager.getInstance().cancelSession();
            }
            else if (close) {
                SessionManager.getInstance().closeSession();
            }
            cleanUp();
        }
    }

    protected String getUsername() {
        String username;
        String teamworkUsername = TeamworkUtils.getLoggedUserName();
        if (teamworkUsername != null) {
            username = teamworkUsername;
        }
        else {
            username = System.getProperty("user.name", "");
        }
        return username;
    }

    protected Stereotype getStereotype(String name) {
        Project project = Application.getInstance().getProject();
        return StereotypesHelper.getStereotype(project, name);
    }

    protected void fail(String reason) {
        JOptionPane.showMessageDialog(null, "Failed: " + reason);
    }
}
