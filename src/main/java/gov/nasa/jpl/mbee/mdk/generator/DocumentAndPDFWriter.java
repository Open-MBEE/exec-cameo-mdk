package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
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
import org.xml.sax.SAXException;

/**
 * runs the generation as a runnable to not stall magicdraw main thread, also
 * allow user to cancel
 *
 * MD Model -> Document(xml) -> PDF
 * @author DavidWillard, mwilson
 */
public class DocumentAndPDFWriter implements RunnableWithProgress {

	private Document dge;
	private boolean genNewImage;
    private File docbook;
    private File docbookXslFo;
    private File outputFile;
    private File fopConfigFile;

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
    	fopConfigFile = new File(pluginDirectory, "fop-conf" + File.separator + "fop.xconf");
		
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
        
    	//try {
    		FopFactory fopFactory = FopFactory.newInstance();//fopConfigFile);
		
    	 /*Thread current = Thread.currentThread();
		 ClassLoader oldLoader = current.getContextClassLoader();
		 Application.getInstance().getGUILog().log("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@OldLoader: " + oldLoader);
		 try {
			   current.setContextClassLoader(getClass().getClassLoader());
			   Application.getInstance().getGUILog().log("NewLoader: " + getClass().getClassLoader());
    		*/
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
		            Result foResults = new SAXResult(fop.getDefaultHandler());
		            //transform Docbook -> XSL-FO -> PDF
		            transformer.transform(docbookSrc, foResults);
		            Application.getInstance().getGUILog().log("A PDF file is created as " + outputFile.getAbsolutePath());
	
		        }
		        catch (FOPException e) {
		        	e.printStackTrace();
		        	Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " +  ExceptionUtils.getStackTrace(e));
		        }
		        catch (javax.xml.transform.TransformerException ex) {
		        	ex.printStackTrace();
		        	Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " + ex.getMessage());
		        	//+  ExceptionUtils.getStackTrace(ex));
		        }
		        catch (Exception ex) {
		        	ex.printStackTrace();
		        	Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " + ex.getMessage());
		        	//+  ExceptionUtils.getStackTrace(ex));
		        	System.out.println("[Error] Failed to generate a PDF file. " +  ExceptionUtils.getStackTrace(ex));
		        } finally {
		            pdfOut.close();
		        } 
			} catch (Exception e) { //FileNotFoundException and IOException for pdfOut.close()
				e.printStackTrace();
				Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. " +  e.getMessage());
				//ExceptionUtils.getStackTrace(e));
			}
    	/*} catch (SAXException e) {
			e.printStackTrace();
			Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. Please check the fop configuration file(" + fopConfigFile.getAbsolutePath() + ")." +  ExceptionUtils.getStackTrace(e));
		} catch (IOException e) {
			e.printStackTrace();
			Application.getInstance().getGUILog().log("[Error] Failed to generate a PDF file. Please check the fop configuration file(" + fopConfigFile.getAbsolutePath() + ")." +  ExceptionUtils.getStackTrace(e));
		}*/
    	
		/* } finally {
		      current.setContextClassLoader(oldLoader);
	   }*/
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
