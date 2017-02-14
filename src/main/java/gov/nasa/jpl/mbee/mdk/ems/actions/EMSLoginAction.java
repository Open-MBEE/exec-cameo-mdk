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
package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import gov.nasa.jpl.mbee.mdk.MMSSyncPlugin;
import gov.nasa.jpl.mbee.mdk.lib.TicketUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.awt.event.ActionEvent;

public class EMSLoginAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Login";

    private EMSLogoutAction logout;

    public EMSLoginAction() {
        super(DEFAULT_ID, "Login", null, null);
    }

    public void setLogoutAction(EMSLogoutAction logout) {
        this.logout = logout;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        loginAction();
    }

    public static boolean loginAction() {
        Project project = Application.getInstance().getProject();
        if (project == null) {
            Utils.showPopupMessage("You need to have a project open first!");
            return false;
        }
        if (project.isRemote() && (TeamworkUtils.getLoggedUserName() == null && EsiUtils.getTeamworkService().getConnectedUser() == null)) {
            Utils.showPopupMessage("You need to be logged in to Teamwork Cloud first!");
            return false;
        }

        if (!TicketUtils.loginToMMS()) {
//            Application.getInstance().getGUILog().log("[WARNING] Unable to log in to MMS with the supplied credentials. Please try to login again.");
            return false;
        }
        ActionsStateUpdater.updateActionsState();
        Application.getInstance().getGUILog().log("[INFO] MMS login complete.");
        for (Project p : Application.getInstance().getProjectsManager().getProjects()) {
            MMSSyncPlugin.getInstance().getJmsSyncProjectEventListenerAdapter().closeJMS(p);
            MMSSyncPlugin.getInstance().getJmsSyncProjectEventListenerAdapter().initializeJMS(p);
        }

        return true;
    }

}
