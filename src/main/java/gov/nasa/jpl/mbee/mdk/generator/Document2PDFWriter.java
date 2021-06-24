package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.task.ProgressStatus;
import java.io.*;

/**
 * runs the generation as a runnable to not stall magicdraw main thread, also
 * allow user to cancel
 * 
 * Document(xml) -> PDF
 *
 * @author mwilson
 */
public class Document2PDFWriter extends DocumentAndPDFWriter {

    protected File docbook;
    protected File docbookXslFo;
    protected File outputFile;

    public Document2PDFWriter(File docbook, File docbookXslFo, File outputFile, File pluginDirectory) {
    	super(docbook, docbookXslFo, outputFile, pluginDirectory);
    }
    @Override
    public void run(ProgressStatus arg0) {
        arg0.setIndeterminate(true);
        arg0.setDescription("Generating a PDF file...");
       	document2Pdf();
    }
}
