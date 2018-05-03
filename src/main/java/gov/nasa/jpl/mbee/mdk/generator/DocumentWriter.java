package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.plugins.PluginDescriptor;
import com.nomagic.magicdraw.plugins.PluginUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBBook;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBSerializeVisitor;
import gov.nasa.jpl.mbee.mdk.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

/**
 * runs the generation as a runnable to not stall magicdraw main thread, also
 * allow user to cancel
 *
 * @author dlam
 */
public class DocumentWriter implements RunnableWithProgress {

    private Document dge;
    private File realfile;
    private File dir;
    private boolean genNewImage;
    private static FopFactory fopFactory = FopFactory.newInstance();

    public DocumentWriter(Document dge, File realFile, boolean genNewImage, File dir) {
        this.dge = dge;
        this.realfile = realFile;
        this.dir = dir;
        this.genNewImage = genNewImage;
    }

    @Override
    public void run(ProgressStatus arg0) {
        GUILog gl = Application.getInstance().getGUILog();
        arg0.setIndeterminate(true);

        try {
        	File tmpFile = new File(realfile.getPath() + ".tmp");
        	BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
            gl.log("Output directory: " + dir.getAbsolutePath());

            SessionManager.getInstance().createSession(Application.getInstance().getProject(), DocBookOutputVisitor.class.getSimpleName());
            DocBookOutputVisitor visitor = new DocBookOutputVisitor(false, dir.getAbsolutePath());
            dge.accept(visitor);
            SessionManager.getInstance().closeSession(Application.getInstance().getProject());
            DBBook book = visitor.getBook();
            if (book != null) {
                DBSerializeVisitor v = new DBSerializeVisitor(genNewImage, dir, arg0);
                book.accept(v);
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                writer.write(v.getOut());
            }
            writer.flush();
            writer.close();


            if(realfile.getName().endsWith(".xml")) {
            	Files.move(tmpFile.toPath(), realfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            else if(realfile.getName().endsWith(".pdf")) {
            	generatePDF(tmpFile);
            	Files.delete(tmpFile.toPath());
            }
            else {
            	throw new IOException("Unsupported Docgen extension, valid options are .xml and .pdf");
            }

            gl.log("Generation Finished");

        }
        catch (IOException | FOPException | TransformerException ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }

    private void generatePDF(File docbook) throws FOPException, TransformerException, IOException {

        //Get our plugin directory
        PluginDescriptor pd = PluginUtils.getPlugins().stream().map(Plugin::getDescriptor)
        		.filter(descriptor -> descriptor.getName().equals("Model Development Kit"))
        		.findAny()
        		.orElseThrow(() -> new IOException("Couldn't find the MDK plugin directory"));

        // the XSL FO file
        File docbookXslFo = new File(pd.getPluginDirectory() + "/docbook/fo/docbook.xsl");

        // Grab the docbook xml as input to FOP
        StreamSource docbookSrc = new StreamSource(docbook);

        // create a user agent (used to tweak rendering settings on a per-run basis.
        // we are just using defaults for now though.
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

		OutputStream pdfOut = new java.io.FileOutputStream(new File(this.realfile.getPath()));

        try {
            // Construct a FOP that makes PDFs (other options are postscript, rtf, etc.)
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOut);

			// Setup XSLT
			Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(docbookXslFo));

            // Pipe the FO events to FOP
            Result foResults = new SAXResult(fop.getDefaultHandler());

            //transform Docbook -> XSL-FO -> PDF
            transformer.transform(docbookSrc, foResults);
        } finally {
            pdfOut.close();
        }
    }
}
