package gov.nasa.jpl.mbee.mdk.generator.validation.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.generator.PresentationElementUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.awt.event.ActionEvent;
import java.util.Collection;

public class ClearAllReferencesAction extends MDAction implements AnnotationAction {

    private static final long serialVersionUID = 1L;
    private Element viewOrSection;
    private Element view;

    public ClearAllReferencesAction(Element viewOrSection, Element view) {
        super("ClearAllReferences", "Remove All References", null, null);
        //JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        this.viewOrSection = viewOrSection;
        this.view = view;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> arg0) {
        SessionManager.getInstance().createSession("fix duplicate references");
        for (Annotation a : arg0) {
            Element e = (Element) a.getTarget();
            Expression ex = PresentationElementUtils.getViewOrSectionExpression(e);
            if (ex == null) {
                continue;
            }
            Constraint c = Utils.getViewConstraint(e);
            if (!ex.isEditable()) {
                Utils.guilog("[ERROR] " + (c == null ? c.getQualifiedName() : ((NamedElement) e).getQualifiedName()) + " is not editable, skipping.");
                continue;
            }
            ex.getOperand().clear();
        }
        SessionManager.getInstance().closeSession();
        Utils.guilog("[INFO] Successful.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Expression ex = PresentationElementUtils.getViewOrSectionExpression(viewOrSection);
        Constraint c = Utils.getViewConstraint(viewOrSection);
        if (ex == null) {
            return;
        }
        if (!ex.isEditable()) {
            Utils.guilog("[ERROR] " + (c == null ? c.getQualifiedName() : ((NamedElement) viewOrSection).getQualifiedName()) + " is not editable.");
            return;
        }
        SessionManager.getInstance().createSession("fix duplicate references");
        ex.getOperand().clear();
        SessionManager.getInstance().closeSession();
        Utils.guilog("[INFO] Successful.");
    }
}

