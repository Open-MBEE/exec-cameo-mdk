package gov.nasa.jpl.mbee.generator;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;

/**
 * <p>
 * Interface for all DocGen queries.
 * </p>
 * 
 * @see gov.nasa.jpl.mbee.model.Query for writing java extensions
 * 
 */
public interface Generatable {

    public void initialize();

    public void parse();

    public List<DocumentElement> visit(boolean forViewEditor, String outputDir);

    public List<MDAction> getActions();

}
