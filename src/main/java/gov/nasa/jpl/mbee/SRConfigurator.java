package gov.nasa.jpl.mbee;

import java.util.ArrayList;

import gov.nasa.jpl.mbee.actions.ems.UpdateWorkspacesAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.CopyAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.CreateInstanceAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.DespecializeAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SpecializeAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.ValidateAction;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.ActionsStateUpdater;
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
	
//	private SRAction specAction;
//	private SRAction despecAction;
//	private SRAction valAction;
//	private SRAction copyAction;
//	private SRAction instAction;
	
	
    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        return 0; //medium
    }

    @Override
    public void configure(ActionsManager manager, Tree tree) {
    	    	
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
    	
        category.setUseActionForDisable(true);
        if (category.isEmpty()) {
        	final MDAction mda = new MDAction(null, null, null, "null");
        	mda.updateState();
        	mda.setEnabled(false);
        	category.addAction(mda);
        }
    }
    
    public ActionsCategory handleMultipleNodes(ActionsCategory category, Node[] nodes) {
    	    	
    	ArrayList<Element> elements = new ArrayList<Element>();
    	
    	for (Node node: nodes) {
	    	if (node != null) {
		    	Object o = node.getUserObject();
		    	if(o instanceof Element) {
		    		elements.add((Element) o);
		    	}
	    	}
    	}

		ValidateAction valAction = new ValidateAction(elements);
		category.addAction(valAction);
    	return category;
    }
    
    public ActionsCategory handleSingleNode(ActionsCategory category, Node node) {
    	
    	SpecializeAction specAction = null;
    	DespecializeAction despecAction = null;
    	ValidateAction valAction = null;
    	CopyAction copyAction = null;
    	CreateInstanceAction instAction = null;
    	
    	if (node == null)
    		return category;
    	Object o = node.getUserObject();
    	if(!(o instanceof Element))
    		return category;
    	Element target = (Element) o;
        
    	// First check target instanceof
    	
        if (target instanceof Activity) {
        	Activity active = (Activity) target;
        	copyAction = new CopyAction(active);
        }
        if (target instanceof Class) {
        	Class clazz = (Class) target;

        	specAction = new SpecializeAction(clazz);
        	despecAction = new DespecializeAction(clazz);
        	valAction = new ValidateAction(clazz);
        	copyAction = new CopyAction(clazz);
        	instAction = new CreateInstanceAction(clazz);
        	
        	if (clazz.getGeneralization().isEmpty()) {
        		String noGenError = "No Generalizations";
        		specAction.disable(noGenError);
        		despecAction.disable(noGenError);
        	}
        	

        }
        
        if (target instanceof Classifier) {
        	Classifier clazzifier = (Classifier) target;
        	copyAction = new CopyAction(clazzifier);
        }
        
        
    	category.addAction(specAction);
    	category.addAction(despecAction);
    	category.addAction(valAction);
    	category.addAction(copyAction);
    	category.addAction(instAction);
        
        // Clear out the category of unused actions
        for (NMAction s: category.getActions()) {
        	if (s == null)
        		category.removeAction(s);
        }
        
        // Disable all if not editable target, add error message
        if (!(target.isEditable())) {
        	for (NMAction s: category.getActions()) {
        		SRAction sra = (SRAction) s;
        		sra.disable("Not Editable");
        	}
        }
        
        return category;
    }
}
