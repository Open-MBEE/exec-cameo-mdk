package gov.nasa.jpl.mbee.stylesaver;

import gov.nasa.jpl.mbee.stylesaver.validationfixes.FixNotSaved;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.EnvironmentLockManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * Validation class that provides a run() method for checking that each diagram
 * in the project was saved before.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class PreviouslySavedValidation implements ElementValidationRuleImpl,
        SmartListenerConfigurationProvider {
    /**
     * First method invoked.
     * 
     * @param project
     *            a project of the constraint.
     * @param constraint
     *            constraint which defines validation rules.
     */
    @Override
    public void init(Project project, Constraint constraint) {
    }

    /**
     * Returns a map of classes and smart listener configurations.
     * 
     * @return a <code>Map</code> of smart listener configurations.
     */
    @Override
    public Map<Class<? extends Element>, Collection<SmartListenerConfig>> getListenerConfigurations() {
        Map<Class<? extends Element>, Collection<SmartListenerConfig>> configMap = new HashMap<Class<? extends Element>, Collection<SmartListenerConfig>>();
        SmartListenerConfig config = new SmartListenerConfig();
        SmartListenerConfig nested = config.listenToNested("ownedOperation");
        nested.listenTo("name");
        nested.listenTo("ownedParameter");

        Collection<SmartListenerConfig> configs = Collections.singletonList(config);
        configMap.put(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, configs);
        configMap.put(Interface.class, configs);
        return configMap;
    }

    /**
     * Executes the rule.
     * 
     * @param project
     *            project of the constraint.
     * @param constraint
     *            constraint that defines validation rules.
     * @param elements
     *            collection of elements to validate.
     * @return a set of <code>Annotation</code> objects which specify invalid
     *         objects.
     */
    @Override
    public Set<Annotation> run(Project project, Constraint constraint, Collection<? extends Element> elements) {
        Set<Annotation> result = new HashSet<Annotation>();

        boolean wasLocked = EnvironmentLockManager.isLocked();
        try {
            EnvironmentLockManager.setLocked(true);

            // Note that validation rule has to constrain elements to Diagrams
            // otherwise scope will break
            Collection<DiagramPresentationElement> diagCollection = StyleSaverUtils
                    .findDiagramPresentationElements(elements);

            Stereotype workingStereotype = StyleSaverUtils.getWorkingStereotype(project);

            for (DiagramPresentationElement diag: diagCollection) {
                // check that the working stereotype is usable
                if (StyleSaverUtils.isGoodStereotype(diag, workingStereotype)) {
                    // get the style currently in the tag
                    String tagStyle = (String)StereotypesHelper.getStereotypePropertyFirst(diag.getElement(),
                            workingStereotype, "style");

                    // there is nothing in this style tag yet
                    if (tagStyle == null) {
                        // add a fix -- save the style
                        NMAction styleAdd = new FixNotSaved(diag);

                        List<NMAction> actionList = new ArrayList<NMAction>();
                        actionList.add(styleAdd);

                        // create the annotation
                        Annotation annotation = new Annotation(diag, constraint, actionList);
                        result.add(annotation);
                    }
                }
            }
        } finally {
            EnvironmentLockManager.setLocked(wasLocked);
        }

        return result;
    }

    /**
     * Invoked when this instance is no longer needed.
     */
    @Override
    public void dispose() {
    }
}
