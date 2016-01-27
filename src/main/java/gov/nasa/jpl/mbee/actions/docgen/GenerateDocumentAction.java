/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.actions.docgen;

import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.generator.DocumentWriter;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.model.Document;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFileChooser;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * generates docgen 3 document
 * 
 * @author dlam
 * 
 */
public class GenerateDocumentAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element            doc;
    public static final String actionid = "GenerateDocument";
    private static GUILog gl = Application.getInstance().getGUILog();


    public GenerateDocumentAction(Element e) {
        super(actionid, "Generate DocGen 3 Document", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	try {
    		File savefile = fileSelect();
            if (savefile != null) {
            	generate(savefile);
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }
    
    private File fileSelect() {
    	// DELETE THESE LINES
    	File ref = new File("/Users/brower/git/ems-rci/mdk/reference");
    	//
        JFileChooser choose = new JFileChooser(ref); // change back to null
        choose.setDialogTitle("Save to output xml...");
        int retval = choose.showSaveDialog(null);
        if (retval == JFileChooser.APPROVE_OPTION)
        	return choose.getSelectedFile();
        return null;
    }
    
    public void generate(File savefile) {
        DocumentValidator dv = new DocumentValidator(doc);

    	dv.validateDocument();
        if (dv.isFatal()) {
            dv.printErrors();
            return;
        }
        
        DocumentGenerator dg = new DocumentGenerator(doc, dv, null);
        Document dge = dg.parseDocument();
        boolean genNewImage = dge.getGenNewImage();
        (new PostProcessor()).process(dge);
        
        String userName = savefile.getName();
		String filename = userName;
		if (userName.length() < 4 || !userName.endsWith(".xml"))
			filename = userName + ".xml";
		File dir = savefile.getParentFile();
		File realfile = new File(dir, filename);
        ProgressStatusRunner.runWithProgressStatus(new DocumentWriter(dge, realfile, genNewImage,
                dir), "Generating DocGen 3 Document...", true, 0);
        dv.printErrors();
    }
    
}
