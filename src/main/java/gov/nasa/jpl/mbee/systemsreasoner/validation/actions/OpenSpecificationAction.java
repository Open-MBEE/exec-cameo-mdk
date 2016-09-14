package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import com.nomagic.magicdraw.ui.dialogs.specifications.SpecificationDialogManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;

public class OpenSpecificationAction extends GenericRuleViolationAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NAME = "Specification";

    private Element element;

    public OpenSpecificationAction(final Element element) {
        super(DEFAULT_NAME);
        this.element = element;
    }

    @Override
    public void run() {
        SpecificationDialogManager.getManager().editSpecification(element);
    }

    @Override
    public String getName() {
        return DEFAULT_NAME;
    }

    @Override
    public String getSessionName() {
        return DEFAULT_NAME;
    }
}
