package gov.nasa.jpl.mbee.generator;

import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSerializeVisitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

/**
 * runs the generation as a runnable to not stall magicdraw main thread, also
 * allow user to cancel
 * 
 * @author dlam
 * 
 */
public class DocumentWriter implements RunnableWithProgress {

    private Document dge;
    private File     realfile;
    private File     dir;
    private boolean  genNewImage;

    public DocumentWriter(Document dge, File realfile, boolean genNewImage, File dir) {
        this.dge = dge;
        this.realfile = realfile;
        this.dir = dir;
        this.genNewImage = genNewImage;
    }

    @Override
    public void run(ProgressStatus arg0) {
        GUILog gl = Application.getInstance().getGUILog();
        arg0.setIndeterminate(true);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(realfile));
            gl.log("output dir: " + dir.getAbsolutePath());
            DocBookOutputVisitor visitor = new DocBookOutputVisitor(false, dir.getAbsolutePath());
            dge.accept(visitor);
            DBBook book = visitor.getBook();
            if (book != null) {
                // List<DocumentElement> books = dge.getDocumentElement();
                DBSerializeVisitor v = new DBSerializeVisitor(genNewImage, dir, arg0);
                book.accept(v);
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                // writer.write("<!DOCTYPE book [\n<!ENTITY % sgml.features \"IGNORE\">\n<!ENTITY % xml.features \"INCLUDE\">\n<!ENTITY % dbcent PUBLIC \"-//OASIS//ENTITIES DocBook Character Entities\nV4.4//EN\" \"dbcentx.mod\">\n%dbcent;\n]>");
                writer.write(v.getOut());
            }
            writer.flush();
            writer.close();
            gl.log("Generation Finished");
        } catch (IOException ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }

}
