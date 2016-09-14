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
package gov.nasa.jpl.mgss.mbee.docgen.validation;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidationRule {
    private String name;
    private ViolationSeverity severity;
    private List<ValidationRuleViolation> violations;
    private String description;

    public ValidationRule(String name, String description, ViolationSeverity severity) {
        this.name = name;
        this.severity = severity;
        this.description = description;
        violations = new ArrayList<ValidationRuleViolation>();
    }

    public ValidationRuleViolation addViolation(ValidationRuleViolation v) {
        violations.add(v);
        return v;
    }

    public ValidationRuleViolation addViolation(Element e, String comment) {
        return addViolation(e, comment, false);
    }

    public ValidationRuleViolation addViolation(Element e, String comment, boolean reported) {
        return addViolation(new ValidationRuleViolation(e, comment, reported));
    }

    public List<ValidationRuleViolation> addViolations(Collection<ValidationRuleViolation> viols) {
        if (viols != null) {
            for (ValidationRuleViolation v : viols) {
                addViolation(v);
            }
        }
        return violations;
    }

    public String getName() {
        return name;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public List<ValidationRuleViolation> getViolations() {
        return violations;
    }

    public String getDescription() {
        return description;
    }
}
