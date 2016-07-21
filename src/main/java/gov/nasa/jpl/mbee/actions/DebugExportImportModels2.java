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
package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.Configurator;
import gov.nasa.jpl.mbee.ems.ModelExporter;
import gov.nasa.jpl.mbee.lib.MDUtils;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DebugExportImportModels2 extends MDAction {

    private static final long serialVersionUID = 1L;

    private Element start;
    
    public static final String actionid = "Debug";
    
    public DebugExportImportModels2() {
        super(actionid, "(Debug Export)", null, null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        Collection<Element> selectedElements = MDUtils.getSelection(e, Configurator.isLastContextDiagram());

        if (  selectedElements.toArray()[0] instanceof Element)
            start = (Element)selectedElements.toArray()[0];
        int depth = 0;
        boolean packageOnly = false;
        
        ModelExporter me;
        //GUILog gl = Application.getInstance().getGUILog();
        
        if (start == Application.getInstance().getProject().getModel()) {
            me = new ModelExporter(Application.getInstance().getProject(), depth, packageOnly);
            System.out.println(start);
        } else {
            System.out.println(start);
            Set<Element> root = new HashSet<Element>();
            root.add(start);
            me = new ModelExporter(root, depth, packageOnly, Application.getInstance().getProject().getPrimaryProject());
        }
        JSONObject result = me.getResult();
        String json = result.toJSONString();
        JSONObject result1 = me.getEMFResult();
        String jsonemf = result1.toJSONString();

        System.out.println(json);
        System.out.println("EMF");
        System.out.println(jsonemf);
    }
}
