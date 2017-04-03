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
package gov.nasa.jpl.mbee.mdk.validation;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;

import java.util.ArrayList;
import java.util.List;

/**
 * if showSummary is true, will show summary of each rule in the suite, the
 * rulel's severity, and number of counts if showDetail is true, will show
 * detail table of all element violations in the form of rule, element,
 * description of violation (optional) if both are true, detail table will come
 * after summary table the title for the tables are suite name + Summary and
 * suite name + detail if ownSection is true, the 2 tables will be put in its
 * own section, with title suite name results
 *
 * @author dlam
 */
public class ValidationSuite {

    private boolean showSummary;
    private boolean showDetail;
    private boolean ownSection;
    private List<ValidationRule> rules;
    private String name;

    public ValidationSuite(String name) {
        this.name = name;
        rules = new ArrayList<ValidationRule>();
        showSummary = true;
        showDetail = true;
        ownSection = false;
    }

    public void addValidationRule(ValidationRule rule) {
        rules.add(rule);
    }

    public void setShowSummary(boolean b) {
        this.showSummary = b;
    }

    public void setShowDetail(boolean b) {
        this.showDetail = b;
    }

    public void setOwnSection(boolean b) {
        this.ownSection = b;
    }

    public List<ValidationRule> getValidationRules() {
        return rules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public boolean hasErrors() {
        for (ValidationRule rule : rules) {
            if (!rule.getViolations().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public List<DocumentElement> getDocBook() {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (showSummary) {
            DBTable summary = new DBTable();
            summary.setTitle(name + " Summary");
            summary.setCols(4);
            List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
            List<DocumentElement> header = new ArrayList<DocumentElement>();
            headers.add(header);
            header.add(new DBText("Validation Rule"));
            header.add(new DBText("Description"));
            header.add(new DBText("Severity"));
            header.add(new DBText("Violations Count"));
            summary.setHeaders(headers);
            List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
            for (ValidationRule vr : rules) {
                List<DocumentElement> rule = new ArrayList<DocumentElement>();
                rule.add(new DBText(vr.getName()));
                rule.add(new DBParagraph(vr.getDescription()));
                rule.add(new DBText(vr.getSeverity().toString()));
                rule.add(new DBText(Integer.toString(vr.getViolations().size())));
                body.add(rule);
            }
            summary.setBody(body);
            res.add(summary);
        }
        if (showDetail) {
            DBTable detail = new DBTable();
            detail.setTitle(name + " Detail");
            detail.setCols(3);
            List<List<DocumentElement>> dheaders = new ArrayList<List<DocumentElement>>();
            List<DocumentElement> dheader = new ArrayList<DocumentElement>();
            dheaders.add(dheader);
            dheader.add(new DBText("Validation Rule"));
            dheader.add(new DBText("Element"));
            dheader.add(new DBText("Description"));
            detail.setHeaders(dheaders);
            List<List<DocumentElement>> dbody = new ArrayList<List<DocumentElement>>();
            for (ValidationRule vr : rules) {
                for (ValidationRuleViolation vrv : vr.getViolations()) {
                    List<DocumentElement> rule = new ArrayList<DocumentElement>();
                    rule.add(new DBText(vr.getName()));
                    if (vrv.getElement() instanceof NamedElement) {
                        rule.add(new DBParagraph(((NamedElement) vrv.getElement()).getQualifiedName()));
                    }
                    else {
                        rule.add(new DBText("Unnamed Element"));
                    }
                    rule.add(new DBParagraph(vrv.getComment()));
                    dbody.add(rule);
                }
            }
            detail.setBody(dbody);
            res.add(detail);
        }
        if (ownSection) {
            DBSection section = new DBSection();
            section.setTitle(name + " Validation Results");
            section.addElements(res);
            res.clear();
            res.add(section);
        }
        return res;
    }
}
