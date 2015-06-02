package gov.nasa.jpl.mbee;

import java.util.ArrayList;

import gov.nasa.jpl.mbee.actions.ems.CloseAutoSyncAction;
import gov.nasa.jpl.mbee.actions.ems.EMSLoginAction;
import gov.nasa.jpl.mbee.actions.ems.EMSLogoutAction;
import gov.nasa.jpl.mbee.actions.ems.SendProjectVersionAction;
import gov.nasa.jpl.mbee.actions.ems.StartAutoSyncAction;
import gov.nasa.jpl.mbee.actions.ems.UpdateFromJMS;
import gov.nasa.jpl.mbee.actions.ems.UpdateWorkspacesAction;
import gov.nasa.jpl.mbee.actions.ems.ValidateMountStructureAction;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SpecializeAction;
import gov.nasa.jpl.mbee.lib.MDUtils;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class SRConfigurator implements BrowserContextAMConfigurator {

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
            category = new MDActionsCategory("SRMain", "Reasons Systemer");
            category.setNested(true);
            manager.addCategory(0, category);
        }
        if (target instanceof Class) {
        	category.addAction(new SpecializeAction(target));
        }
        category.setUseActionForDisable(true);
        if (category.isEmpty()) {
        	// copy of a hacked thing from DocGenConfigurator line 183-ish
        	final MDAction mda = new MDAction(null, null, null, "null");
        	mda.updateState();
        	mda.setEnabled(false);
        	category.addAction(mda);
        }
    }
}
