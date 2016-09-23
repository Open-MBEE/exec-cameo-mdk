package gov.nasa.jpl.mbee.mdk.validation.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

public class SpecializeClassifierAction extends GenericRuleViolationAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NAME = "Specialize Classifier";

    private Classifier specific, general;

    public SpecializeClassifierAction(final Classifier specific, final Classifier general) {
        super(DEFAULT_NAME);
        this.specific = specific;
        this.general = general;
    }

    public static final Generalization specialize(final Classifier specific, final Classifier general) {
        final Generalization generalization = Application.getInstance().getProject().getElementsFactory().createGeneralizationInstance();
        generalization.setSpecific(specific);
        generalization.setGeneral(general);
        generalization.setOwner(specific);
        // special.getGeneral().add(general);
        return generalization;
    }

    @Override
    public void run() {
        if (!specific.isEditable()) {
            Application.getInstance().getGUILog().log(specific.getQualifiedName() + " is not editable. Skipping specialization.");
            return;
        }
        SpecializeClassifierAction.specialize(specific, general);
    }

    @Override
    public String getName() {
        return DEFAULT_NAME;
    }

    @Override
    public String getSessionName() {
        return "specialize classifier";
    }
}
