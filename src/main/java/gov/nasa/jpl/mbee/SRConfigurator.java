package gov.nasa.jpl.mbee;

import java.util.ArrayList;
import java.util.List;

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
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class SRConfigurator implements BrowserContextAMConfigurator, DiagramContextAMConfigurator {
	
	ValidateAction validateAction = null;
	SpecializeAction specAction = null;
	DespecializeAction despecAction = null;
	//CopyAction copyAction = null;
	CreateInstanceAction instAction = null;

    @Override
    public int getPriority() {
        return 0; //medium
    }

    @Override
    public void configure(ActionsManager manager, Tree tree) {
    	final List<Element> elements = new ArrayList<Element>();
    	for (final Node n : tree.getSelectedNodes()) {
    		if (n.getUserObject() instanceof Element) {
    			elements.add((Element) n.getUserObject());
    		}
    	}
    	configure(manager, elements);
    }
    
	@Override
	public void configure(ActionsManager manager, DiagramPresentationElement diagram,
            PresentationElement[] selected, PresentationElement requestor) {
		final List<Element> elements = new ArrayList<Element>();
		for (final PresentationElement pe : selected) {
			if (pe.getElement() != null) {
				elements.add(pe.getElement());
			}
		}
		configure(manager, elements);
	}
	
	protected void configure(ActionsManager manager, List<Element> elements) {
    	// refresh the actions for every new click (or selection)
    	validateAction = null;
    	specAction = null;                  
    	despecAction = null;
    	//copyAction = null;
    	instAction = null;
    	
        ActionsCategory category = (ActionsCategory)manager.getActionFor("SRMain");
        if (category == null) {
            category = new MDActionsCategory("SRMain", "Systems Reasoner", null, ActionsGroups.APPLICATION_RELATED);
            category.setNested(true);
            //manager.addCategory(0, category);
        }
        manager.removeCategory(category);
    	
    	if (elements.size() > 1) {
    		category = handleMultipleNodes(category, manager, elements);
    	} 
    	else if (elements.size() == 1) {
    		category = handleSingleNode(category, manager, elements.get(0));
    	}
    	else {
    		return;
    	}
    	
    	if (category == null) {
    		return;
    	}
    	manager.addCategory(0, category);
    	
    	category.addAction(validateAction);        
    	category.addAction(specAction);
    	category.addAction(despecAction);
    	//category.addAction(copyAction);
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
    
    public ActionsCategory handleMultipleNodes(ActionsCategory category, ActionsManager manager, List<Element> elements) {
    	ArrayList<Classifier> classes = new ArrayList<Classifier>();
    	for (Element element : elements) {
	    	if (element != null) {
		    	if (element instanceof Classifier) {
		    		classes.add((Classifier) element);
		    	}
	    	}
    	}
    	
    	// if nothing in classes, disable category and return it
    	if (classes.isEmpty()) {
    		//category = disableCategory(category);
    		return null;
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
    
    public ActionsCategory handleSingleNode(ActionsCategory category, ActionsManager manager, Element element) {
    	
    	if (element == null)
    		return null;
    	
        // Disable all if not editable target, add error message
        if (!(element.isEditable())) {
        	category = disableCategory(category);
        	return category;
        }
        
        //copyAction = new CopyAction(target);
        
    	// check target instanceof
        /*if (target instanceof Activity) {
        	Activity active = (Activity) target;
        	copyAction = new CopyAction(active);
        }*/
        if (element instanceof Classifier) {
        	Classifier classifier = (Classifier) element;
        	validateAction = new ValidateAction(classifier);
        	specAction = new SpecializeAction(classifier);
        	despecAction = new DespecializeAction(classifier);
        	//copyAction = new CopyAction(clazz);
        	instAction = new CreateInstanceAction(classifier);
        	
        	if (classifier.getGeneralization().isEmpty()) {
        		despecAction.disable("No Generalizations");
        	}
        }
        else {
        	return null;
        }
        /*if (target instanceof Classifier) {
        	Classifier clazzifier = (Classifier) target;
        	copyAction = new CopyAction(clazzifier);
        }*/
        
        return category;
    }
    
    public static ActionsCategory disableCategory(ActionsCategory category) {
    	// once all the categories are disabled, the action category will be disabled
    	// this is defined in the configure method: category.setNested(true);
    	for (NMAction s: category.getActions()) {
    		if (s instanceof SRAction) {
    			SRAction sra = (SRAction) s;
    			sra.disable("Not Editable");
    		}
        }
    	return category;
    }
}
