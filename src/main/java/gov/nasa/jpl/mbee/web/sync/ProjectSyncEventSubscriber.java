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

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.transaction.TransactionCommitListener;

/**
 * Subscribes a listener to comment change events within a project. Create
 * whenever a project is opened, and unsubscribe when the project is closed.
 */
public class ProjectSyncEventSubscriber {
    private final Project                   project;
    private final TransactionCommitListener listener;

    /** Registers a listener for transaction commits. */
    public ProjectSyncEventSubscriber(Project project) {
        this.project = project;
        listener = new CommentChangeListener();
    }

    /** Call when the project is opened. */
    public void subscribe() {
        project.getRepository().getTransactionManager().addTransactionCommitListener(listener);
    }

    /** Called by listener when the project is closed. */
    public void unsubscribe() {
        project.getRepository().getTransactionManager().removeTransactionCommitListener(listener);
    }

    // If you want a "smart listener", do it like this:
    // (but take care managing sessions and threads!)
    //
    // SmartListenerConfig listenerConfig = new SmartListenerConfig();
    // listenerConfig.listenTo("ID");
    // listenerConfig.listenTo(PropertyNames.BODY);
    // project.getSmartEventSupport().registerConfig(
    // Comment.class,
    // Collections.singletonList(listenerConfig),
    // listener);

}
