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
package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.NMAction;
import gov.nasa.jpl.mbee.mdk.ocl.actions.OclQueryAction;

import java.lang.reflect.Method;

public class OclEvaluatorPlugin extends MDPlugin {

    public OclEvaluatorPlugin() {
        this(OclQueryAction.class);
    }

    public OclEvaluatorPlugin(Class<? extends NMAction> cls) {
        super(cls);
    }

    @Override
    public void initConfigurations() {
        Method method = getNmActionMethod();

        String category = "MDK";
        String diagramContext = "BaseDiagramContext";
        addConfiguration("MainMenu", "", OclQueryAction.actionText, category, method, this);
        addConfiguration("ContainmentBrowserContext", "", OclQueryAction.actionText, category, method, this);
        addConfiguration(diagramContext, "Class Diagram", OclQueryAction.actionText, category, method,
                this);
        addConfiguration(diagramContext, "Activity Diagram", OclQueryAction.actionText, category,
                method, this);
        addConfiguration(diagramContext, "SysML Block Definition Diagram", OclQueryAction.actionText,
                category, method, this);
        addConfiguration(diagramContext, "SysML Internal Block Diagram", OclQueryAction.actionText,
                category, method, this);
        addConfiguration(diagramContext, "DocGen 3 View Diagram", OclQueryAction.actionText, category,
                method, this);
        addConfiguration(diagramContext, "DocGen 3 Diagram", OclQueryAction.actionText, category,
                method, this);
        addConfiguration(diagramContext, "View Diagram", OclQueryAction.actionText, category, method,
                this);
        addConfiguration(diagramContext, "Viewpoint Method Diagram", OclQueryAction.actionText, category, method,
                this);
        addConfiguration(diagramContext, "DocumentView", OclQueryAction.actionText, category, method,
                this);
    }

}
