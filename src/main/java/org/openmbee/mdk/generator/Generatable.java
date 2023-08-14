package org.openmbee.mdk.generator;

import com.nomagic.magicdraw.actions.MDAction;
import org.openmbee.mdk.docgen.docbook.DocumentElement;
import org.openmbee.mdk.model.Query;

import java.util.List;

/**
 * <p>
 * Interface for all DocGen queries.
 * </p>
 *
 * @see Query for writing java extensions
 */
public interface Generatable {

    void initialize();

    void parse();

    List<DocumentElement> visit(boolean forViewEditor, String outputDir);

    List<MDAction> getActions();

}
