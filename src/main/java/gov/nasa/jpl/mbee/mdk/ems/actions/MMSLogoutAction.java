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
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.MMSSyncPlugin;
import gov.nasa.jpl.mbee.mdk.ems.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.ems.sync.status.SyncStatusConfigurator;
import gov.nasa.jpl.mbee.mdk.lib.TicketUtils;

import java.awt.event.ActionEvent;

public class MMSLogoutAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Logout";

    private MMSLoginAction login;

    public MMSLogoutAction() {
        super(DEFAULT_ID, "Logout", null, null);
    }

    public void setLoginAction(MMSLoginAction login) {
        this.login = login;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logoutAction(Application.getInstance().getProject());
    }

    public static void logoutAction(Project project) {
        TicketUtils.clearTicket(project);
        Application.getInstance().getGUILog().log("[INFO] MMS logout complete.");
        ActionsStateUpdater.updateActionsState();
        if (!JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener().isDisabled()) {
            MMSSyncPlugin.getInstance().getJmsSyncProjectEventListenerAdapter().closeJMS(project);
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - Reverting to offline mode. All changes will be saved in the model until reconnected. Reason: You must be logged into MMS.");
        }
        SyncStatusConfigurator.getSyncStatusAction().update();
    }

}
