package org.openmbee.mdk.validation;

import com.nomagic.actions.NMAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.ArrayList;
import java.util.List;

public class ValidationRuleViolation {

    private Element e;

    private List<NMAction> actions = new ArrayList<NMAction>();

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

    public void setActions(List<NMAction> actions) {
        this.actions = actions;
    }

    public List<NMAction> getActions() {
        return actions;
    }

    public void addAction(NMAction a) {
        actions.add(a);
    }
}
