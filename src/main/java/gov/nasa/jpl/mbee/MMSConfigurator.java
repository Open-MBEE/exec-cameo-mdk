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
package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.actions.ems.*;
import gov.nasa.jpl.mbee.lib.MDUtils;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;

public class MMSConfigurator implements AMConfigurator {

    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void configure(ActionsManager manager) {
        NMAction category = manager.getActionFor("MMSMAIN");
        if (category == null) {
            category = new MDActionsCategory("MMSMAIN", "MMS");
            ((ActionsCategory)category).setNested(true);
            manager.addCategory((ActionsCategory)category);
            EMSLogoutAction logout = new EMSLogoutAction();
            EMSLoginAction login = new EMSLoginAction();
            login.setLogoutAction(logout);
            logout.setLoginAction(login);
            category.addAction(logout);
            category.addAction(login);
            MDActionsCategory validateCategory = new MDActionsCategory("MMSMAINVALIDATE", "Validate");
            validateCategory.setNested(true);
            category.addAction(validateCategory);
            validateCategory.addAction(new ValidateModulesAction());
            validateCategory.addAction(new ValidateBranchesAction());
            
            /*MDActionsCategory sync = new MDActionsCategory("MMSMAINSYNC", "Update and Commit");
            sync.setNested(true);
            category.addAction(sync);
            sync.addAction(new UpdateFromJMS(false));
            sync.addAction(new UpdateFromJMS(true));
            sync.addAction(new UpdateFromJMSAndCommitWithDelete());
            sync.addAction(new UpdateAllDocs());*/

            category.addAction(new UpdateAllDocs());
            
            category.addAction(new SendProjectVersionAction());
            category.addAction(new MigrationCategory());
            if (MDUtils.isDeveloperMode()) {
                category.addAction(new UpdateWorkspacesAction());
            }
        }
        ActionsStateUpdater.updateActionsState();
    }
}
