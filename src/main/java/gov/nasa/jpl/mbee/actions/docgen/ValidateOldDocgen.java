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

import gov.nasa.jpl.mbee.ems.validation.actions.ExportView;
import gov.nasa.jpl.mbee.ems.validation.actions.MigrateOldDocgen;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/**
 * validates docgen 3 doc - checks for loops, duplicate dependencies, etc
 * 
 * @author dlam
 * 
 */
public class ValidateOldDocgen extends MDAction {

    private static final long serialVersionUID = 1L;
    public static final String actionid = "ValidateOldDocuments";

    public ValidateOldDocgen() {
        super(actionid, "Find Old DocGen Documents", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ValidationSuite vs = new ValidationSuite("Old DocGen Documents");
        ValidationRule vr = new ValidationRule("Old DocGen Document", "Old DocGen Document", ViolationSeverity.ERROR);
        vs.addValidationRule(vr);
        Stereotype ps = Utils.getProductStereotype();
        if (ps == null)
            return;
        List<Element> elements = Utils.collectOwnedElements(Application.getInstance().getProject().getModel(), 0);
        List<Element> docs = Utils.filterElementsByStereotype(elements, ps, true, true);
        List<Element> projDocs = new ArrayList<Element>();
        for (Element doc: docs) {
            if (!ProjectUtilities.isElementInAttachedProject(doc) && doc instanceof Package)
                projDocs.add(doc);
        }
        if (projDocs.isEmpty()) {
            Application.getInstance().getGUILog().log("No Old Documents Found");
            return;
        }
        for (Element doc: projDocs) {
            ValidationRuleViolation v = new ValidationRuleViolation(doc, "[OLD] Document is old format");
            v.addAction(new MigrateOldDocgen(doc));
            vr.addViolation(v);
        }
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(vs);
        Utils.displayValidationWindow(vss, "Old DocGen Documents");
    }
}
