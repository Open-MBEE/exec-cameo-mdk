package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

import java.util.ArrayList;

public class AspectRemedyAction extends GenericRuleViolationAction {
    public static final String DEFAULT_ID = "Realize Aspect";
    private final Classifier classifier, aspect;

    public AspectRemedyAction(Classifier classifier, Classifier aspect) {
        super(DEFAULT_ID);
        this.classifier = classifier;
        this.aspect = aspect;
    }

    public void run() {
        Classifier realization = new SpecializeStructureAction(aspect, false, null, true, true).createSpecialClassifier(classifier, new ArrayList<>(), new ArrayList<>());
        if (realization != null) {
            Property property = Project.getProject(classifier).getElementsFactory().createPropertyInstance();
            property.setClassifier(classifier);
            property.setType(realization);
        }
    }
}
