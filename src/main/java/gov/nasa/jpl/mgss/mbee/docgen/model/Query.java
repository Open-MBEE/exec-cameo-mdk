package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.Generatable;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * <p>
 * This class should be extended if writing java extensions, or any of its
 * subclass like Table
 * </p>
 * 
 * @author dlam
 * 
 */
public abstract class Query extends DocGenElement implements Generatable {
    /**
     * The elements passed into this query. These are magicdraw elements
     * resulting from collect/filter/sort actions
     */
    protected List<Element> targets;
    protected List<String>  titles;
    protected boolean       sortElementsByName = false;

    public void setTargets(List<Element> t) {
        targets = t;
    }

    public List<Element> getTargets() {
        return targets;
    }

    public void setTitles(List<String> t) {
        titles = t;
    }

    public List<String> getTitles() {
        return titles;
    }

    public boolean isSortElementsByName() {
        return sortElementsByName;
    }

    public void setSortElementsByName(boolean sortElementsByName) {
        this.sortElementsByName = sortElementsByName;
    }

    /**
     * This is called after the query object has been constructed and the
     * targets and dgElement fields are set
     */
    @Override
    public void initialize() {

    }

    /**
     * This method must be overidden by subclasses to return the result of the
     * query
     */
    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        return new ArrayList<DocumentElement>();
    }

    /**
     * This is called after initialize
     */
    @Override
    public void parse() {

    }

    /**
     * <p>
     * These actions will show up as menu items under View Interaction, if the
     * user right clicks on a view that will execute this query
     * </p>
     * <p>
     * targets and dgElement would have been filled
     * </p>
     */
    @Override
    public List<MDAction> getActions() {
        return new ArrayList<MDAction>();
    }

    @Override
    public void accept(IModelVisitor visitor) {
        visitor.visit(this);
    }
}
