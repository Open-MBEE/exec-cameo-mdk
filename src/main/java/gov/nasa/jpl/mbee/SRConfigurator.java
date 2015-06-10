package gov.nasa.jpl.mbee;

import java.util.ArrayList;

import gov.nasa.jpl.mbee.actions.systemsreasoner.CopyAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.CreateInstanceAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.DespecializeAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.ValidateAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SpecializeAction;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class SRConfigurator implements BrowserContextAMConfigurator {
	
	ValidateAction validateAction = null;
	SpecializeAction specAction = null;
	DespecializeAction despecAction = null;
	CopyAction copyAction = null;
	CreateInstanceAction instAction = null;

    @Override
    public int getPriority() {
        // TODO Auto-generated method stuååb
        return 0; //medium
    }

    @Override
    public void configure(ActionsManager manager, Tree tree) {
    	
    	// refresh the actions for every new click (or selection)
    	validateAction = null;
    	specAction = null;
    	despecAction = null;
    	copyAction = null;
    	instAction = null;
    	
        ActionsCategory category = (ActionsCategory)manager.getActionFor("SRMain");
        if (category == null) {
            category = new MDActionsCategory("SRMain", "Reasons Systemer", null, ActionsGroups.APPLICATION_RELATED);
            category.setNested(true);
            manager.addCategory(0, category);
        }
    	
    	if (tree.getSelectedNodes().length > 1) {
    		category = handleMultipleNodes(category, tree.getSelectedNodes());
    	} else {
    		category = handleSingleNode(category, tree.getSelectedNode());
    	}
    	
    	category.addAction(validateAction);        
    	category.addAction(specAction);
    	category.addAction(despecAction);
    	category.addAction(copyAction);
    	category.addAction(instAction);
        
        // Clear out the category of unused actions
        for (NMAction s: category.getActions()) {
        	if (s == null)
        		category.removeAction(s);
        }
    	
        category.setUseActionForDisable(true);
        
        if (category.isEmpty()) {
        	final MDAction mda = new MDAction(null, null, null, "null");
        	mda.updateState();
        	mda.setEnabled(false);
        	category.addAction(mda);
        }
    }
    
    public ActionsCategory handleMultipleNodes(ActionsCategory category, Node[] nodes) {
    	
    	ArrayList<Classifier> classes = new ArrayList<Classifier>();
    	for (Node node: nodes) {
	    	if (node != null) {
		    	Object o = node.getUserObject();
		    	if(o instanceof Classifier) {
		    		classes.add((Classifier) o);
		    	}
	    	}
    	}
    	
    	// if nothing in classes, disable category and return it
    	if (classes.isEmpty()) {
    		category = disableCategory(category);
    		return category;
    	}
    	
    	// otherwise, add the classes to the ValidateAction action
		validateAction = new ValidateAction(classes);
		
		// if any of the classifiers are not editable, disable the validate
		for (Classifier clf: classes) {
			if (!(clf.isEditable())) {
				validateAction.disable();
			}
		}
		
		// add the action to the actions category
		category.addAction(validateAction);
		
		specAction = new SpecializeAction(classes);
		category.addAction(specAction);
		
		despecAction = new DespecializeAction(classes);
		category.addAction(despecAction);
		
    	return category;
    }
    
    public ActionsCategory handleSingleNode(ActionsCategory category, Node node) {
    	
    	if (node == null)
    		return category;
    	Object o = node.getUserObject();
    	if(!(o instanceof Element))
    		return category;
    	Element target = (Element) o;
    	
        // Disable all if not editable target, add error message
        if (!(target.isEditable())) {
        	category = disableCategory(category);
        	return category;
        }
        
    	// check target instanceof
        if (target instanceof Activity) {
        	Activity active = (Activity) target;
        	copyAction = new CopyAction(active);
        }
        if (target instanceof Class) {
        	Class clazz = (Class) target;
        	validateAction = new ValidateAction(clazz);
        	specAction = new SpecializeAction(clazz);
        	despecAction = new DespecializeAction(clazz);
        	copyAction = new CopyAction(clazz);
        	instAction = new CreateInstanceAction(clazz);
        	
        	if (clazz.getGeneralization().isEmpty()) {
        		despecAction.disable("No Generalizations");
        	}
        }
        if (target instanceof Classifier) {
        	Classifier clazzifier = (Classifier) target;
        	copyAction = new CopyAction(clazzifier);
        }
        
        return category;
    }
    
    public ActionsCategory disableCategory(ActionsCategory category) {
    	// once all the categories are disabled, the action category will be disabled
    	// this is defined in the configure method: category.setNested(true);
    	for (NMAction s: category.getActions()) {
    		SRAction sra = (SRAction) s;
    		sra.disable("Not Editable");
        }
    	return category;
    }
}
