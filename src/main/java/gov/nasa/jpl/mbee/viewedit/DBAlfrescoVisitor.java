package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.alfresco.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBAbstractVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSection;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class DBAlfrescoVisitor extends DBAbstractVisitor {

    protected JSONObject elements;
    private JSONObject   views;
    private JSONArray                 curContains;
    private Stack<JSONArray>          sibviews;
    private Stack<Set<String>>        viewElements;
    private Map<String, JSONObject>   images;        
    protected boolean                 recurse;
    private GUILog                    gl;
    private static String             FILE_EXTENSION = ".svg";
    private Stereotype view = Utils.getViewStereotype();
    private Stereotype viewpoint = Utils.getViewpointStereotype();
    private Map<From, String> sourceMapping;

    public DBAlfrescoVisitor(boolean recurse) {
        elements = new JSONObject();
        views = new JSONObject();
        sibviews = new Stack<JSONArray>();
        viewElements = new Stack<Set<String>>();
        this.recurse = recurse;
        gl = Application.getInstance().getGUILog();
        images = new HashMap<String, JSONObject>();
        sourceMapping = new HashMap<From, String>();
        sourceMapping.put(From.DOCUMENTATION, "documentation");
        sourceMapping.put(From.DVALUE, "value");
        sourceMapping.put(From.NAME, "name");
    }

    public int getNumberOfElements() {
        return elements.size();
    }

    /**
     * Simple getter for images
     */
    public Map<String, JSONObject> getImages() {
        return images;
    }

    /**
     * Utility to remove all the images
     */
    public void removeImages() {
        for (String key: images.keySet()) {
            String filename = (String)images.get(key).get("abspath");
            try {
                File file = new File(filename);

                if (!file.delete()) {
                    gl.log("[WARNING]: could not delete " + filename);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBBook book) {
        JSONArray childviews = new JSONArray();
        sibviews.push(childviews);
        
        if (book.getFrom() != null) {
            Element docview = book.getFrom();
            startView(docview);
            JSONObject entry = new JSONObject();
            entry.put("source", docview.getID());
            entry.put("sourceProperty", sourceMapping.get(From.DOCUMENTATION));
            entry.put("type", "Paragraph");
            entry.put("sourceType", "reference");
            curContains.add(entry);
            endView(docview);
        }
        for (DocumentElement de: book.getChildren()) {
            de.accept(this);
            if (!recurse)
                break;
        }
        sibviews.pop();
    }

    @Override
    public void visit(DBColSpec colspec) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBImage image) {
        JSONObject entry = new JSONObject();
        JSONObject imageEntry = new JSONObject();

        // export image - also keep track of exported images
        DiagramPresentationElement diagram = Application.getInstance().getProject()
                .getDiagram(image.getImage());
        String svgFilename = image.getImage().getID();

        // create image file
        File directory = new File("images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // export the image file
        File svgDiagramFile = new File(directory, svgFilename);
        try {
            ImageExporter.export(diagram, ImageExporter.SVG, svgDiagramFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // calculate the checksum
        long cs = 0;
        try {
            RandomAccessFile f = new RandomAccessFile(svgDiagramFile.getAbsolutePath(), "r");
            byte[] data = new byte[(int)f.length()];
            f.read(data);
            f.close();
            Checksum checksum = new CRC32();
            checksum.update(data, 0, data.length);
            cs = checksum.getValue();
        } catch (IOException e) {
            gl.log("Could not calculate checksum: " + e.getMessage());
            e.printStackTrace();
        }

        // Lets rename the file to have the hash code
        // make sure this matches what's in the View Editor ImageResource.java
        String svgCrcFilename = image.getImage().getID() + "_latest" + FILE_EXTENSION;
        gl.log("Exporting diagram to: " + svgDiagramFile.getAbsolutePath());

        // keep record of all images found
        imageEntry.put("cs", String.valueOf(cs));
        imageEntry.put("abspath", svgDiagramFile.getAbsolutePath());
        imageEntry.put("extension", FILE_EXTENSION);
        images.put(svgFilename, imageEntry);

        entry.put("text", "<p><img src=\"/editor/images/docgen/" + svgCrcFilename + "\" alt=\""
                + image.getImage().getName() + "\"/></p>");
        entry.put("source", "text");
        entry.put("type", "Paragraph");
        curContains.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        DBEditListVisitor l = new DBEditListVisitor(recurse, elements);
        list.accept(l);
        curContains.add(l.getObject());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject entry = getJSONForDBParagraph(para);
        curContains.add(entry);
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJSONForDBParagraph(DBParagraph para) {
        JSONObject entry = new JSONObject();
        if (para.getFrom() != null && para.getFromProperty() != null) {
            this.addToElements(para.getFrom());
            entry.put("sourceType", "reference");
            entry.put("source", para.getFrom().getID());
            entry.put("sourceProperty", sourceMapping.get(para.getFromProperty()));
        } else {
            entry.put("sourceType", "text");
            entry.put("text", DocGenUtils.addP(DocGenUtils.fixString(para.getText(), false)));
        }
        entry.put("type", "Paragraph");
        return entry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBText text) {
        JSONObject entry = getJSONForDBText(text);
        curContains.add(entry);
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJSONForDBText(DBText text) {
        JSONObject entry = new JSONObject();
        if (text.getFrom() != null && text.getFromProperty() != null) {
            this.addToElements(text.getFrom());
            entry.put("sourceType", "reference");
            entry.put("source", text.getFrom().getID());
            entry.put("sourceProperty", sourceMapping.get(text.getFromProperty()));
        } else {
            entry.put("sourceType", "text");
            entry.put("text", DocGenUtils.addP(DocGenUtils.fixString(text.getText(), false)));
        }
        entry.put("type", "Paragraph");
        return entry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBSection section) {
        if (section.isView()) {
            Element eview = section.getFrom();
            startView(eview);

            sibviews.peek().add(eview.getID());
            JSONArray childViews = new JSONArray();
            sibviews.push(childViews);
            for (DocumentElement de: section.getChildren()) {
                // if (recurse || !(de instanceof DBSection))
                de.accept(this);
            }
            sibviews.pop();
            endView(eview);
        } else {
            // addToViews(fake, false)
            // gen fakeid
            // sibviews.peek().add(fakeid);
            // JSONArray childViews = new JSONArray();
            // sibviews.push(childViews);
            // view2view.put(fakeid, childViews);
            // for (DocumentElement de: section.getChildren()) {
            // if (recurse || !(de instanceof DBSection))
            // de.accept(this);
            // }

            // sibviews.pop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBSimpleList simplelist) {
        DBHTMLVisitor html = new DBHTMLVisitor();
        simplelist.accept(html);
        JSONObject entry = new JSONObject();
        entry.put("sourceType", "text");
        entry.put("text", html.getOut());
        entry.put("type", "Paragraph"); // just show it as html for now
        curContains.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTable table) {
        DBEditTableVisitor2 v = new DBEditTableVisitor2(this.recurse, elements);
        table.accept(v);
        curContains.add(v.getObject());
    }

    protected void startView(Element e) {
        gl.log("Processing view: " + ((NamedElement)e).getName());
        JSONObject view = new JSONObject();
        String id = e.getID();
        view.put("id", id);
        views.put(id, view);
        Set<String> viewE = new HashSet<String>();
        viewElements.push(viewE);
        JSONArray contains = new JSONArray();
        view.put("contains", contains);
        this.curContains = contains;
        addToElements(e);
    }
    
    @SuppressWarnings("unchecked")
    protected void endView(Element e) {
        JSONArray viewEs = new JSONArray();
        viewEs.addAll(viewElements.pop());
        JSONObject view = (JSONObject)views.get(e.getID());
        view.put("displayedElements", viewEs);
        view.put("allowedElements", viewEs);
    }

    @SuppressWarnings("unchecked")
    protected void addToElements(Element e) {
        viewElements.peek().add(e.getID());
        if (elements.containsKey(e.getID()))
            return;
        JSONObject elementInfo = new JSONObject();
        ExportUtility.fillElement(e, elementInfo, view, viewpoint);
        elements.put(e.getID(), elementInfo);
    }
}

