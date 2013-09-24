package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.List;

public class JavaExtensionTest extends Query {

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        DBParagraph para = new DBParagraph("This is from a java extension");
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        res.add(para);
        return res;
    }
      
}
