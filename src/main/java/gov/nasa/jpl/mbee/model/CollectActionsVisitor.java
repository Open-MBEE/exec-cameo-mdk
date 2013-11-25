package gov.nasa.jpl.mbee.model;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;

/**
 * this should collect all the queries that can result in some context menu
 * being displayed when user right clicks on the view userscripts, editable
 * tables, etc
 * 
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
