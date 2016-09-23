package gov.nasa.jpl.mbee.mdk.validation.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

public class DeleteElementAction extends GenericRuleViolationAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Element element;
    private String name;

    public DeleteElementAction(final Element element, final String name) {
        super(name);
        this.element = element;
        this.name = name;
    }

    @Override
    public void run() {
        if (!element.isEditable()) {
            Application.getInstance().getGUILog().log((element instanceof NamedElement ? ((NamedElement) element).getQualifiedName() : element.toString()) + " is not editable. Skipping deletion.");
            return;
        }
        element.dispose();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSessionName() {
        return "delete element";
    }
}
