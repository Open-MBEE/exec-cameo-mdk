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
package gov.nasa.jpl.mbee.ems.migrate;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import gov.nasa.jpl.mbee.api.docgen.presentation_elements.PresentationElementEnum;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class FixViewDocumentation extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "FixViewDoc";

    public FixViewDocumentation() {
        super(actionid, "Fix View Documentations", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = Application.getInstance().getProject();
        Classifier c = PresentationElementEnum.PARAGRAPH.get().apply(project);
        Classifier p = PresentationElementEnum.OPAQUE_PARAGRAPH.get().apply(project);
        if (c == null || p == null) {
            Application.getInstance().getGUILog().log("[INFO] Nothing to fix.");
            return;
        }
        ValidationSuite suite = new ValidationSuite("View Doc Classifier");
        ValidationRule nameDiff = new ValidationRule("View Doc classifier", "classifier is wrong", ViolationSeverity.ERROR);
        suite.addValidationRule(nameDiff);
        if (c.get_instanceSpecificationOfClassifier().isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Nothing to fix.");
            return;
        }
        List<InstanceSpecification> toFix = new ArrayList<InstanceSpecification>();

        for (InstanceSpecification is : c.get_instanceSpecificationOfClassifier()) {
            if (is.getName().equals("View Documentation")) {
                toFix.add(is);
            }
        }
        if (toFix.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Nothing to fix.");
            return;
        }

        SessionManager.getInstance().createSession("fix view docs");
        for (InstanceSpecification is : toFix) {
            if (is.isEditable()) {
                is.getClassifier().clear();
                is.getClassifier().add(p);
            }
            else if (!ProjectUtilities.isElementInAttachedProject(is)) {
                nameDiff.addViolation(new ValidationRuleViolation(is, "[ERROR] Not Editalbe"));
            }
        }
        SessionManager.getInstance().closeSession();
        Application.getInstance().getGUILog().log("[INFO] Done");
        List<ValidationSuite> col = new ArrayList<ValidationSuite>();
        col.add(suite);
        if (suite.hasErrors()) {
            Application.getInstance().getGUILog().log("[INFO] See validation for errors");
            Utils.displayValidationWindow(col, "Fix View Doc Classifier");
        }
    }
}