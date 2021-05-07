package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
//import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBBook;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBSerializeVisitor;
import gov.nasa.jpl.mbee.mdk.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;

import java.io.*;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.PageSequenceResults;


/**
 * runs the generation as a runnable to not stall magicdraw main thread, also
 * allow user to cancel
 *
 * MD Model -> Document(xml) -> PDF
 * @author DavidWillard, Miyako Wilson
 */
public class DocumentAndPDFWriter implements RunnableWithProgress {

	private Document dge;
	private boolean genNewImage;
    private File docbook;
    private File docbookXslFo;
    private File outputFile;
    private File pluginDirectory;

    public DocumentAndPDFWriter(File docbook, File docbookXslFo, File outputFile, File pluginDirectory) { 
    	this(null, false, docbook, docbookXslFo, outputFile, pluginDirectory);
    }
    public DocumentAndPDFWriter(Document dge, boolean getNewImage, File docbook, File pluginDirectory) {
    	this(dge, getNewImage, docbook, null, null, pluginDirectory);
    }
    
    public DocumentAndPDFWriter(Document dge, boolean genNewImage, File docbook, File docbookXslFo, File outputFile, File pluginDirectory) {
    	this.dge = dge;
    	this.genNewImage = genNewImage;
    	this.docbook = docbook;
    	this.docbookXslFo = docbookXslFo;
    	this.outputFile = outputFile;
    	this.pluginDirectory = pluginDirectory;
		
    }
    protected boolean mdModel2Document(ProgressStatus arg0) {

    	//MD Model -> Document
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(docbook));
            
            SessionManager.getInstance().createSession(Application.getInstance().getProject(), DocBookOutputVisitor.class.getSimpleName());
            DocBookOutputVisitor visitor = new DocBookOutputVisitor(false, docbook.getParentFile().getAbsolutePath());
            dge.accept(visitor);
            SessionManager.getInstance().closeSession(Application.getInstance().getProject());
            DBBook book = visitor.getBook();
            if (book != null) {
                DBSerializeVisitor v = new DBSerializeVisitor(genNewImage, docbook.getParentFile(), arg0);
                book.accept(v);
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                writer.write(v.getOut());
            }
            writer.flush();
            writer.close();
            Application.getInstance().getGUILog().log("A DocBook xml file is created as " + docbook.getAbsolutePath()); 
            return true;
        } catch (IOException e) {
        	Application.getInstance().getGUILog().log("[Error] Failed to generate a DocBook XML file. " + ExceptionUtils.getStackTrace(e));
            return false;
        }

    }
    //Document -> PDF
    protected void document2Pdf()  {
    	
    	ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		FopFactory fopFactory = FopFactory.newInstance();//fopConfigFile);
    	
    		//make currentloader to be mainloader (batik, fop, xmlgraphics jar need to be loaded at the same level so images to be embedded in PDF).
    		URL[] urls = new URL[1];
    		//urls[0] = (new File(ApplicationEnvironment.getInstallRoot() + File.separator + "plugins" + File.separator + "gov.nasa.jpl.mbee.mdk")).toURI().toURL();
    		urls[0] = pluginDirectory.toURI().toURL();
    		URLClassLoader mainClassLoader = new URLClassLoader(urls, Application.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(mainClassLoader);
    		
        	StreamSource docbookSrc = new StreamSource(docbook);
	
	        // create a user agent (used to tweak rendering settings on a per-run basis.
	        // we are just using defaults for now though.
	        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
	        
	        OutputStream pdfOut;
			try {
				pdfOut = new java.io.FileOutputStream(new File(outputFile.getPath()));
		        try {
		            // Construct a FOP that makes PDFs (other options are postscript, rtf, etc.)
		            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOut);
					// Setup XSLT
					Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(docbookXslFo));
		            // Pipe the FO events to FOP
		            Result res = new SAXResult(fop.getDefaultHandler());
		            //transform Docbook -> XSL-FO -> PDF
		            transformer.transform(docbookSrc, res);
		            
		            
		            FormattingResults foResults = fop.getResults();
		            // Result processing
		            java.util.List pageSequences = foResults.getPageSequences();
		            for (Object pageSequence : pageSequences) {
		                PageSequenceResults pageSequenceResults = (PageSequenceResults) pageSequence;
		                Application.getInstance().getGUILog().log("PageSequence "
		                        + (String.valueOf(pageSequenceResults.getID()).length() > 0
		                        ? pageSequenceResults.getID() : "<no id>")
		                        + " generated " + pageSequenceResults.getPageCount() + " pages.");
		            }
		            Application.getInstance().getGUILog().log("Generated " + foResults.getPageCount() + " pages in total.");
		            Application.getInstance().getGUILog().log("A PDF file is created as " + outputFile.getAbsolutePath());
		        }
		        catch (FOPException e) {
		        	e.printStackTrace();
		        	Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " +  ExceptionUtils.getStackTrace(e));
		        }
		        catch (javax.xml.transform.TransformerException ex) {
		        	ex.printStackTrace();
		        	Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " + ExceptionUtils.getStackTrace(ex));
		        }
		        catch (Exception ex) {
		        	ex.printStackTrace();
		        	Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " + ExceptionUtils.getStackTrace(ex));
		        	
		        } finally {
		            pdfOut.close();
		        } 
			} catch (Exception e) { //FileNotFoundException and IOException for pdfOut.close()
				e.printStackTrace();
				Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " + ExceptionUtils.getStackTrace(e));
			}
    	
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file." +  ExceptionUtils.getStackTrace(e));
		}

		finally {
            Thread.currentThread().setContextClassLoader(localClassLoader);
        }
    }
    @Override
    public void run(ProgressStatus arg0) {
    	arg0.setIndeterminate(true);
    	arg0.setDescription("Generating a DocBook XML file...");
		if ( mdModel2Document(arg0)) {
			arg0.setDescription("Generating a PDF file...");
			document2Pdf();
		}		
    }

    
    
    
}
