package gov.nasa.jpl.mbee.generator.validation.actions;

import gov.nasa.jpl.mbee.generator.ViewInstanceUtils;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;
import java.util.Collection;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

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
            Expression ex = ViewInstanceUtils.getViewOrSectionExpression(e);
            if (ex == null)
                continue;
            if (!ex.isEditable()) {
                Utils.guilog("[ERROR] " + ((NamedElement)e).getQualifiedName() + " is not editable, skipping.");
                continue;
            }
            ex.getOperand().clear();
        }
        SessionManager.getInstance().closeSession();
        Utils.guilog("[INFO] Successful.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Expression ex = ViewInstanceUtils.getViewOrSectionExpression(viewOrSection);
        if (ex == null)
            return;
        if (!ex.isEditable()) {
            Utils.guilog("[ERROR] Element is not editable.");
            return;
        }
        SessionManager.getInstance().createSession("fix duplicate references");
        ex.getOperand().clear();
        SessionManager.getInstance().closeSession();
        Utils.guilog("[INFO] Successful.");
    }
}

