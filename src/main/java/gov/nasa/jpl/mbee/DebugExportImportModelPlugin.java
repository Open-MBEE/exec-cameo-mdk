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

import gov.nasa.jpl.mbee.actions.DebugExportImportModels2;
import gov.nasa.jpl.mbee.lib.Debug;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

import com.nomagic.actions.NMAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DebugExportImportModelPlugin extends MDPlugin {

    // OclQueryAction action = null;
    /**
   * 
   */
    public DebugExportImportModelPlugin() {
        this(DebugExportImportModels2.class);
    }

    public DebugExportImportModelPlugin(Class<? extends NMAction> cls) {
        super(cls);
    }

    // unused -- TODO -- remove after testing
    public static void doIt(ActionEvent event, Element element) {
        DebugExportImportModels2 action = new DebugExportImportModels2();
        action.actionPerformed(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.mgss.mbee.docgen.MDPlugin#initConfigurations()
     */
    @Override
    public void initConfigurations() {
        // //Debug.turnOn();
        // if ( !MDUtils.isDeveloperMode() ) {
        // Debug.outln(
        // "OclEvaluatorPlugin will be hidden since MD is not in developer mode."
        // );
        // return;
        // }
        Debug.outln("initializing xx!");

        // Method method = ClassUtils.getMethodsForName(
        // OclEvaluatorPlugin.class, "doIt")[ 0 ];
        // TODO -- shouldn't have to look this method up and pass it--just get
        // rid of
        // method argument in addConfiguration calls below.
        Method method = getNmActionMethod();

        String category = "MDK";
        addConfiguration("MainMenu", "", "Debug Export/Improt1", category, method, this);
        addConfiguration("ContainmentBrowserContext", "", "(Debug Export/Improt)", category, method,
                this);
        addConfiguration("BaseDiagramContext", "Class Diagram", "Debug Export/Improt3", category,
                method, this);
        addConfiguration("BaseDiagramContext", "Activity Diagram", "Debug Export/Improt4", category,
                method, this);
        addConfiguration("BaseDiagramContext", "SysML Block Definition Diagram",
                "Debug Export/Improt5", category, method, this);
        addConfiguration("BaseDiagramContext6", "SysML Internal Block Diagram",
                "Debug Export/Improt", category, method, this);
        addConfiguration("BaseDiagramContext", "DocGen 3 View Diagram", "Debug Export/Improt7",
                category, method, this);
        addConfiguration("BaseDiagramContext", "DocGen 3 Diagram", "Debug Export/Improt8",
                category, method, this);
        addConfiguration("BaseDiagramContext", "View Diagram", "Debug Export/Improt9",
                category, method, this);
        addConfiguration("BaseDiagramContext", "DocumentView", "Debug Export/Improt`0",
                category, method, this);
    }

}
