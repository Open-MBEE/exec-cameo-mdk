package gov.nasa.jpl.mbee;

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
	
	private SRAction specAction;
	private SRAction despecAction;
	private SRAction valAction;
	private SRAction copyAction;
	private SRAction instAction;
	
	
    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        return 0; //medium
    }

    @Override
    public void configure(ActionsManager manager, Tree tree) {
    	if (tree.getSelectedNodes().length > 1)
    		return;
    	Node no = tree.getSelectedNode();
    	if (no == null)
    		return;
    	Object o = no.getUserObject();
    	if(!(o instanceof Element))
    		return;
    	Element target = (Element) o;
        ActionsCategory category = (ActionsCategory)manager.getActionFor("SRMain");
        if (category == null) {
            category = new MDActionsCategory("SRMain", "Reasons Systemer", null, ActionsGroups.APPLICATION_RELATED);
            category.setNested(true);
            manager.addCategory(0, category);
        }
        
    	category.addAction(specAction);
    	category.addAction(despecAction);
    	category.addAction(valAction);
    	category.addAction(copyAction);
    	category.addAction(instAction);
        
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
//        	instAction = new CreateInstanceAction(clazz);
        	
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
        
        // disable all if not editable target
        
        if (!(target.isEditable())) {
        	for (NMAction s: category.getActions()) {
        		SRAction sra = (SRAction) s;
        		sra.disable("Not Editable");
        	}
        }
        
        
        //TODO: clean up the actions in the category
        for (NMAction s: category.getActions()) {
        	System.out.println(s.getID());
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
}
