package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBBook;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBSerializeVisitor;
import gov.nasa.jpl.mbee.mdk.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;

import java.io.*;

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
            gl.log("Output directory: " + dir.getAbsolutePath());
            SessionManager.getInstance().createSession(Application.getInstance().getProject(), DocBookOutputVisitor.class.getSimpleName());
            DocBookOutputVisitor visitor = new DocBookOutputVisitor(false, dir.getAbsolutePath());
            dge.accept(visitor);
            SessionManager.getInstance().closeSession(Application.getInstance().getProject());
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
