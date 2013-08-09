package gov.nasa.jpl.mbee.patternloader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.valueprovider.SmartListenerConfigurationProvider;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * Validation class for comparing the styles in the pattern diagram to the styles
 * currently on the diagram.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternValidation implements ElementValidationRuleImpl, SmartListenerConfigurationProvider {
    /**
     * First method invoked.
     * 
     * @param project		a project of the constraint.
     * @param constraint	constraint which defines validation rules.
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
     * @param project    project of the constraint.
     * @param constraint constraint that defines validation rules.
     * @param elements   collection of elements to validate.
     * @return			 a set of <code>Annotation</code> objects which specify invalid objects.
     */
	@Override
	public Set<Annotation> run(Project project, Constraint constraint, Collection<? extends Element> elements) {
		return null;
	}

    /**
     * Invoked when this instance is no longer needed.
     */
	@Override
	public void dispose() {
	}
}