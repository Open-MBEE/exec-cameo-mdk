package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.task.ProgressStatus;
import gov.nasa.jpl.mbee.mdk.model.Document;

import java.io.File;


/**
 * runs the generation as a runnable to not stall magicdraw main thread, also
 * allow user to cancel
 * <p>
 * MD Model -> Document
 *
 * @author dlam, mwilson
 */
public class DocumentWriter extends DocumentAndPdfWriter {

    public DocumentWriter(Document dge, File realfile, boolean genNewImage) {
        super(dge, genNewImage, realfile);
    }

    @Override
    public void run(ProgressStatus status) {
        status.setIndeterminate(true);
        status.setDescription("Generating a DocBook XML file...");
        mdModel2Document(status);
    }
}
