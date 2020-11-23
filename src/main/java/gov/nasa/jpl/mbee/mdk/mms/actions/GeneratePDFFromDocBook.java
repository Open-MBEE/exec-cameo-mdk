package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Action to PDF from DocBook
 * 
 */

public class GeneratePDFFromDocBook extends GeneratePDFDocument {

    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = GeneratePDFFromDocBook.class.getSimpleName();
    private File docBookDefaultDir = null;
    
    public GeneratePDFFromDocBook(Element view) {
        super(DEFAULT_ID, "DocBook -> PDF", view);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
    	 try {
    		 
    		File docbookFile = fileSelect("Select a docbook(xml) ...", docBookDefaultDir, "Select", new FileNameExtensionFilter("XML", "xml"));
    		docBookDefaultDir = docbookFile.getParentFile();
         	xslDefaultFile = fileSelect("Select a style sheet(xsl) ...", xslDefaultFile, "Select", new FileNameExtensionFilter("XSL", "xsl"));
         	File outputPdfFile = fileSelect("Select an output pdf to be saved ...", pdfDefaultDir, "Save", new FileNameExtensionFilter("PDF", "pdf"));
         	pdfDefaultDir = outputPdfFile.getParentFile();
         	
         	//not end with .pdf and then append .pdf
         	if ( !outputPdfFile.getName().endsWith(".pdf"))
         		outputPdfFile = new File(outputPdfFile + ".pdf");
            
            super.generatePDF(docbookFile, xslDefaultFile, outputPdfFile);
             
         } catch (Exception ex) {
             Utils.printException(ex);
         }
    }

   
   
}
