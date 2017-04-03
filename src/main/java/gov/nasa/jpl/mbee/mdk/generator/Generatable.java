package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.actions.MDAction;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;

import java.util.List;

/**
 * <p>
 * Interface for all DocGen queries.
 * </p>
 *
 * @see gov.nasa.jpl.mbee.mdk.model.Query for writing java extensions
 */
public interface Generatable {

    void initialize();

    void parse();

    List<DocumentElement> visit(boolean forViewEditor, String outputDir);

    List<MDAction> getActions();

}
