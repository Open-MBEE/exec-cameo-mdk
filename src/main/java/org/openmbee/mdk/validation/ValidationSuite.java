package org.openmbee.mdk.validation;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.openmbee.mdk.docgen.docbook.*;

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
