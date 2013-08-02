package gov.nasa.jpl.mbee.stylesaver;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.lang.Class;
import java.util.*;

/**
 * Validation class that provides a run() method for comparing the styles in the view tag
 * to the styles currently on the diagram.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class StyleValidation implements ElementValidationRuleImpl, SmartListenerConfigurationProvider {
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
		Set<Annotation> result = new HashSet<Annotation>();
		
		// get all the diagrams in the project
		Collection<DiagramPresentationElement> diagCollection = project.getDiagrams();
		
		Stereotype workingStereotype = StylerUtils.getWorkingStereotype(project);
		int badStereotype = 0;
		
		// iterate over each diagram checking for matched tag styling and actual styling
		for(DiagramPresentationElement diag : diagCollection) {
			if(StylerUtils.isGoodStereotype(diag, workingStereotype)) {
		    	// get the style currently in the tag
		    	Object tagStyleObj = StereotypesHelper.getStereotypePropertyFirst(diag.getElement(), workingStereotype, "style");
		    	String tagStyle = StereotypesHelper.getStereotypePropertyStringValue(tagStyleObj);
		    	
		        if((tagStyle == null) || (tagStyle.equals(""))) {
			        continue;
			    }
		        
		        // get the style currently on the diagram by performing a dummy save
		        String currStyle = ViewSaver.save(project, diag, true);
		        
		        // compare styles and add annotation if they do not match
		        if(!tagStyle.equals(currStyle)) {
		            // add a fix for the style mismatch -- update the tag
		            NMAction styleMismatchUpdate = new FixStyleMismatchUpdate(diag);
		            
		            // add a fix for the style mismatch -- restore the styles
		            NMAction styleMismatchRestore = new FixStyleMismatchRestore(diag);
		            
		            List<NMAction> actionList = new ArrayList<NMAction>();
		            actionList.add(styleMismatchUpdate);
		            actionList.add(styleMismatchRestore);
		            
		            // create the annotation
					Annotation annotation = new Annotation(diag, constraint, actionList);
			        result.add(annotation);
		        }
			} else {
				badStereotype++;
			}
		}
		
		if(badStereotype > 0) {
			Application.getInstance().getGUILog().log("WARNING: The style-tagged " + workingStereotype.getName() + " stereotype is not applied" +
													  " to one or more diagrams in this project. Style validation cannot validate such diagrams.");
		}
		
		Application.getInstance().getGUILog().log("Style validation done.");

        return result;
    }
    
    /**
     * Invoked when this instance is no longer needed.
     */
    @Override
	public void dispose() {
    }
}