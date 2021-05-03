package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.task.ProgressStatus;
import gov.nasa.jpl.mbee.mdk.model.Document;
import java.io.*;


/**
 * runs the generation as a runnable to not stall magicdraw main thread, also
 * allow user to cancel
 *
 * MD Model -> Document
 * @author dlam, mwilson
 */
public class DocumentWriter extends DocumentAndPDFWriter {

    public DocumentWriter(Document dge, File realfile, boolean genNewImage) {
        super(dge, genNewImage, realfile, null);
    }

    @Override
    public void run(ProgressStatus arg0) {
    	arg0.setIndeterminate(true);
    	arg0.setDescription("Generating a DocBook XML file...");
		mdModel2Document(arg0);
    }
}
