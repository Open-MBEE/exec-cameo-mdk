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
package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ViewpointConstraint extends Query {

    private Boolean           iterate;
    private String            expression;
    private Boolean           report;
    private DocumentValidator dv;

    public ViewpointConstraint(DocumentValidator dv) {
        super();
        this.dv = dv;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        // construct a temporary validation suite from the global one to
        // generate docbook output for one constraint.
        ValidationSuite vs = new ValidationSuite(((NamedElement)dgElement).getName());
        ValidationRule rule = new ValidationRule(((NamedElement)dgElement).getName(), "Viewpoint Constraint",
                ViolationSeverity.WARNING);
        vs.addValidationRule(rule);
        ValidationRule r = dv.getViewpointConstraintRule();
        if (r instanceof ConstraintValidationRule) {
            rule.addViolations(((ConstraintValidationRule)r).getViolations(dgElement));
        }
        // if (expression != null) {
        // if (iterate) {
        // for (Element e: targets) {
        //
        // }
        // } else {
        //
        // }

        if (report && !Utils2.isNullOrEmpty(rule.getViolations())) {
            return vs.getDocBook();
        }
        // }

        return new ArrayList<DocumentElement>();
    }

    @Override
    public void initialize() {
        iterate = (Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.viewpointConstraintStereotype, "iterate", true);
        expression = (String)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.viewpointConstraintStereotype, "expression", "");
        report = (Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.viewpointConstraintStereotype, "validationReport", false);
    }

}
