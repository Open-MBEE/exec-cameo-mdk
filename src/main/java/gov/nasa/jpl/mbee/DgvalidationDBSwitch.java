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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.dgvalidation.Rule;
import gov.nasa.jpl.mbee.dgvalidation.Severity;
import gov.nasa.jpl.mbee.dgvalidation.Suite;
import gov.nasa.jpl.mbee.dgvalidation.Violation;
import gov.nasa.jpl.mbee.dgvalidation.util.DgvalidationSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

public class DgvalidationDBSwitch extends DgvalidationSwitch<Object> {

    @Override
    public Object caseRule(Rule object) {
        ViolationSeverity vs = null;
        if (object.getSeverity() == Severity.DEBUG) {
            vs = ViolationSeverity.DEBUG;
        }
        else if (object.getSeverity() == Severity.ERROR) {
            vs = ViolationSeverity.ERROR;
        }
        else if (object.getSeverity() == Severity.FATAL) {
            vs = ViolationSeverity.FATAL;
        }
        else if (object.getSeverity() == Severity.INFO) {
            vs = ViolationSeverity.INFO;
        }
        else {
            vs = ViolationSeverity.WARNING;
        }
        ValidationRule res = new ValidationRule(object.getName(), object.getDescription(), vs);
        for (Violation v : object.getViolations()) {
            res.addViolation((ValidationRuleViolation) this.doSwitch(v));
        }
        return res;
    }

    @Override
    public Object caseViolation(Violation object) {
        if (object.getElementId() != null) {
            ValidationRuleViolation res = new ValidationRuleViolation((Element) Application.getInstance()
                    .getProject().getElementByID(object.getElementId()), object.getComment());
            return res;
        }
        return null;
    }

    @Override
    public Object caseSuite(Suite object) {
        ValidationSuite res = new ValidationSuite(object.getName());
        for (Rule r : object.getRules()) {
            res.addValidationRule((ValidationRule) this.doSwitch(r));
        }
        res.setOwnSection(object.isOwnSection());
        res.setShowDetail(object.isShowDetail());
        res.setShowSummary(object.isShowSummary());
        return res;
    }

}
