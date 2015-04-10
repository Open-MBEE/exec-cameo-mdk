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
package gov.nasa.jpl.mbee.actions.docgen;

import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.HierarchyMigrationVisitor;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/**
 * given a viewpoint composition hierarchy, makes the views, and have them
 * conform to the respective viewpoints
 * 
 * @author dlam
 * 
 */
public class MigrateToClassViewAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element            doc;

    public static final String actionid = "MigrateToClassViews";

    public MigrateToClassViewAction(Element e) {
        super(actionid, "Migrate to Class Views", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentGenerator dg = new DocumentGenerator(doc, null, null);
        Document dge = dg.parseDocument(false, true, true);
        List packages = new ArrayList();
        packages.add(Package.class);
        Element owner = (Element)Utils.getUserSelection(packages , "Pick a package to create under");
        
        if (owner != null) {
            Boolean preserveId = Utils.getUserYesNoAnswer("Preserve Ids? (This will swap the ids of the existing views and new class views created.)");
            boolean preserve = false;
            if (preserveId != null && preserveId)
                preserve = true;
            SessionManager.getInstance().createSession("docgen migration");
            try {
                HierarchyMigrationVisitor hmv = new HierarchyMigrationVisitor(owner, preserve);
                dge.accept(hmv);
                if (preserve && hmv.changeIdFailed()) {
                    Application.getInstance().getGUILog().log("[ERROR] Not all existing views are editable, cannot preserve ids, aborted.");
                    throw new Exception("failed cannot preserve ids on old document migrations");
                }
                SessionManager.getInstance().closeSession();
                Application.getInstance().getGUILog().log("[INFO] Done (note previous 'nosection' views are now views under the parent view).");
            } catch (Exception ex) {
                SessionManager.getInstance().cancelSession();
                Utils.printException(ex);
            }
            
        }
        
    }
}
