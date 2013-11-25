package gov.nasa.jpl.mgss.mbee.docgen.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CollectFilterMetrics implements ElementValidationRuleImpl {

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(Project arg0, Constraint arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<Annotation> run(Project project, Constraint constraint, Collection<? extends Element> elements) {
        Set<Annotation> result = new HashSet<Annotation>();
        Collection<DiagramPresentationElement> diagCollection = project.getDiagrams();

        for (DiagramPresentationElement diag: diagCollection) {
            // IF diagram is an activity
            // FOR action in the activity
            // IF is a collect and filter
            List<NMAction> actionList = new ArrayList<NMAction>();
            Annotation annotation = new Annotation(diag, constraint, actionList);
            result.add(annotation);
        }

        return result;
    }

}
