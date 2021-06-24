package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.mdk.generator.Document2PDFWriter;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 * Action to generate a PDF file from a DocBook xml file
 * @author mwilson
 */

public class GeneratePDFFromDocBookDocument extends GeneratePDF {

    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = GeneratePDFFromDocBookDocument.class.getSimpleName();
    private File docBookDefaultDir = null;
    
    public GeneratePDFFromDocBookDocument(String id, String name, Element view) {
    	super(id, name, view);
    }
    public GeneratePDFFromDocBookDocument(Element view) {
        this(DEFAULT_ID, "PDF from Docbook", view);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
		
    	//1. ask for the docbook xml file
		File docbookFile = askForDocBookFile("Select a DocBook XML file to convert...", "Select");
		if ( docbookFile == null) return; //cancelled
			
		//2. ask for the output pdf file
		File outputPdfFile = askForPDFFile();
		if ( outputPdfFile != null) {
			//3. pick a stylesheet(.xsl)
			if (checkForStyleSheetXMLFile()) 
				generate(docbookFile, xslDefaultFile, outputPdfFile);
		}
    }
    
    protected File askForDocBookFile(String title, String buttonText) {
    	File docbookFile = fileSelect(title, new File(docBookDefaultDir, view.getHumanName().substring(view.getHumanType().length()).trim() + ".xml"), buttonText, new FileNameExtensionFilter("XML", "xml"));
    	if (docbookFile == null) return null; //cancelled
        docBookDefaultDir = docbookFile.getParentFile();
        //rename to .xml if not
    	if ( !docbookFile.getName().endsWith(".xml")) 
    		docbookFile = new File( docbookFile + ".xml");
        return docbookFile;
    }
    //DocBook XML file -> PDF
    protected void generate(File inputDocBook, File docbookXslFo, File outputPdfFile)  {
		ProgressStatusRunner.runWithProgressStatus(new Document2PDFWriter(inputDocBook, docbookXslFo, outputPdfFile, pluginDirectory), "DocGen", true, 0);
    }

}
