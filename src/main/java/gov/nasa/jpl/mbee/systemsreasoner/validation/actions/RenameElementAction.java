package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;

public class RenameElementAction extends GenericRuleViolationAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private NamedElement source, target;
    private String name;

    public RenameElementAction(final NamedElement source, final NamedElement target, final String name) {
        super(name);
        this.source = source;
        this.target = target;
        this.name = name;
    }

    @Override
    public void run() {
        if (!target.isEditable()) {
            Application.getInstance().getGUILog().log(target.getQualifiedName() + " is not editable. Skipping rename.");
            return;
        }
        target.setName(source.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSessionName() {
        return "rename element";
    }
}
