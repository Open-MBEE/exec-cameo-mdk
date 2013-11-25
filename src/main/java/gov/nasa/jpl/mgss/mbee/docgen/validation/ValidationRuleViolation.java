package gov.nasa.jpl.mgss.mbee.docgen.validation;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidationRuleViolation {

    private Element e;

    public Element getElement() {
        return e;
    }

    public void setElement(Element e) {
        this.e = e;
    }

    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private boolean reported;

    public boolean isReported() {
        return reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    public ValidationRuleViolation(Element e, String comment) {
        this.e = e;
        this.comment = comment;
        this.reported = false;
    }

    public ValidationRuleViolation(Element e, String comment, boolean reported) {
        this(e, comment);
        this.reported = reported;
    }
}
