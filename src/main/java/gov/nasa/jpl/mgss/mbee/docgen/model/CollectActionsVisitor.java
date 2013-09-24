package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * this should collect all the queries that can result in some context menu being displayed when user right clicks on the view
 * userscripts, editable tables, etc
 * @author dlam
 *
 */
public class CollectActionsVisitor extends AbstractModelVisitor {

    private List<MDAction> actions;
	
	
	public CollectActionsVisitor() {
	    actions = new ArrayList<MDAction>();
	}
	
	@Override
	public void visit(Query q) {
	    actions.addAll(q.getActions());
	}
	
	public List<MDAction> getActions() {
	    return actions;
	}
	
	
}
