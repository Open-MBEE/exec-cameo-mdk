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
package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.DocGenUtils;
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
import java.util.Map;
import java.util.Stack;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml2.util.UML2ModelUtil;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;

/**
 * visits the Docbook result model and collects view information and elements in
 * views to export to view editor
 * 
 * @author dlam
 * 
 */
public class DBEditDocwebVisitor extends DBAbstractVisitor {
    /*
     * { views: [{ mdid: magicdrawid, nosection: true/false, contains: [{type:
     * paragraph/image, source: magicdrawid(source element id), sourceProperty:
     * documentation/name/etc}, {type: paragraph, source: text, text: blah}] }],
     * view2view: {viewid: [viewid, viewid,...], viewid: [viewid, ...], ...},
     * elements: [{mdid: magicdrawid, name: name, documentation: blah, type:
     * type}]
     */

    protected Map<String, JSONObject> elements;
    private Map<String, JSONObject>   views;
    private JSONObject                view2view;
    private JSONArray                 curContains;
    private Stack<JSONArray>          sibviews;
    private Map<String, JSONObject>   images;                 // keep track of
                                                               // all images and
                                                               // corresponding
                                                               // metadata
    protected boolean                 recurse;
    private GUILog                    gl;
    protected boolean                 alfresco;
    private static String             FILE_EXTENSION = ".svg";

    public DBEditDocwebVisitor(boolean recurse, boolean alfresco) {
        elements = new HashMap<String, JSONObject>();
        views = new HashMap<String, JSONObject>();
        view2view = new JSONObject();
        sibviews = new Stack<JSONArray>();
        this.recurse = recurse;
        gl = Application.getInstance().getGUILog();
        images = new HashMap<String, JSONObject>();
        this.alfresco = alfresco;
    }

    @SuppressWarnings("unchecked")
    public String getJSON() {
        JSONObject out = new JSONObject();
        JSONArray aviews = new JSONArray();
        aviews.addAll(views.values());
        JSONArray aelements = new JSONArray();
        aelements.addAll(elements.values());
        out.put("views", aviews);
        out.put("elements", aelements);
        out.put("view2view", view2view);
        return out.toJSONString();

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
            String id = docview.getID();
            view2view.put(id, childviews);

            addToViews(docview, false);

            JSONObject entry = new JSONObject();
            entry.put("source", docview.getID());
            entry.put("useProperty", From.DOCUMENTATION.toString());
            entry.put("type", "Paragraph");
            curContains.add(entry);
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
        if (alfresco) {
            DBEditListVisitor l = new DBEditListVisitor(recurse, elements);
            list.accept(l);
            curContains.add(l.getObject());

        } else {
            DBHTMLVisitor html = new DBHTMLVisitor();
            list.accept(html);
            JSONObject entry = new JSONObject();
            entry.put("source", "text");
            entry.put("text", html.getOut());
            entry.put("type", "Paragraph"); // just show as html for now
            curContains.add(entry);
        }
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
            this.addToElements(para.getFrom(), false);
            entry.put("source", para.getFrom().getID());
            entry.put("useProperty", para.getFromProperty().toString());
        } else {
            entry.put("source", "text");
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
            this.addToElements(text.getFrom(), false);
            entry.put("source", text.getFrom().getID());
            entry.put("useProperty", text.getFromProperty().toString());
        } else {
            entry.put("source", "text");
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
            addToViews(eview, section.isNoSection());

            sibviews.peek().add(eview.getID());
            JSONArray childViews = new JSONArray();
            sibviews.push(childViews);
            view2view.put(eview.getID(), childViews);

            for (DocumentElement de: section.getChildren()) {
                // if (recurse || !(de instanceof DBSection))
                de.accept(this);
            }

            sibviews.pop();
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
        entry.put("source", "text");
        entry.put("text", html.getOut());
        entry.put("type", "Paragraph"); // just show it as html for now
        curContains.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTable table) {
        if (alfresco) {
            DBEditTableVisitor2 v = new DBEditTableVisitor2(this.recurse, elements);
            table.accept(v);
            curContains.add(v.getObject());
        } else {
            DBEditTableVisitor v = new DBEditTableVisitor(this.recurse, this.elements);
            table.accept(v);
            curContains.add(v.getObject());
        }
    }

    @SuppressWarnings("unchecked")
    protected void addToViews(Element e, boolean isNoSection) {
        gl.log("Processing view: " + ((NamedElement)e).getName());
        JSONObject view = new JSONObject();
        String id = e.getID();
        view.put("mdid", id);
        view.put("noSection", isNoSection);
        views.put(id, view);

        JSONArray contains = new JSONArray();
        view.put("contains", contains);
        this.curContains = contains;

        addToElements(e, true);
    }

    @SuppressWarnings("unchecked")
    protected void addToElements(Element e, boolean isview) {
        String id = e.getID();
        JSONObject o = null;
        if (elements.containsKey(id))
            o = elements.get(id);
        else {
            o = new JSONObject();
            elements.put(id, o);
        }
        if (e instanceof Slot) {
            String ss = Utils.slotValueToString((Slot)e);
            o.put("type", "Property");
            o.put("dvalue", ss);
        }
        if (e instanceof Property) {
            o.put("type", "Property");
            o.put("dvalue", UML2ModelUtil.getDefault((Property)e));
        }
        if (e instanceof NamedElement) {
            o.put("name", ((NamedElement)e).getName());
            o.put("qualifiedName", ((NamedElement)e).getQualifiedName().trim().replaceAll("::", ".")
                    .replaceAll("[^A-Za-z0-9_\\-\\. ]", "_"));
        }
        String doc = ModelHelper.getComment(e);
        if (e instanceof Comment) {
            o.put("type", "Comment");
            doc = Utils.stripHtmlWrapper(((Comment)e).getBody());
        }
        o.put("documentation", Utils.stripHtmlWrapper(doc));
        o.put("mdid", id);
        if (isview) {
            o.put("type", "View");
        }
    }
}
