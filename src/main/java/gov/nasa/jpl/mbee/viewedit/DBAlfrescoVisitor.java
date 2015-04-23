package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.ems.ExportUtility;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class DBAlfrescoVisitor extends DBAbstractVisitor {

    protected JSONObject elements;
    private JSONObject   views;
    private Stack<JSONArray>          curContains;  //MDEV #674 -- change to a Stack of JSONArrays
    private Stack<JSONArray>          sibviews; //sibling views
    private Stack<Set<String>>        viewElements;
    private Map<String, JSONObject>   images;        
    protected boolean                 recurse;
    private GUILog                    gl;
    private static String             FILE_EXTENSION = ".svg";
    private Stereotype view = Utils.getViewStereotype();
    private Stereotype viewpoint = Utils.getViewpointStereotype();
    private Map<From, String> sourceMapping;
    private JSONObject                view2view;
    private JSONArray                 noSections = new JSONArray();
    private boolean doc;
    protected Set<Element> elementSet = new HashSet<Element>();
    
    public DBAlfrescoVisitor(boolean recurse) {
        elements = new JSONObject();
        views = new JSONObject();
        curContains = new Stack<JSONArray>();
        sibviews = new Stack<JSONArray>();
        viewElements = new Stack<Set<String>>();

        this.recurse = recurse;
        gl = Application.getInstance().getGUILog();
        images = new HashMap<String, JSONObject>();
        sourceMapping = new HashMap<From, String>();
        sourceMapping.put(From.DOCUMENTATION, "documentation");
        sourceMapping.put(From.DVALUE, "value");
        sourceMapping.put(From.NAME, "name");
        view2view = new JSONObject();

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
            doc = true; 
            Element docview = book.getFrom();
            startView(docview);
            JSONObject entry = new JSONObject();
            entry.put("source", docview.getID());
            entry.put("sourceProperty", sourceMapping.get(From.DOCUMENTATION));
            entry.put("type", "Paragraph");
            entry.put("sourceType", "reference");
            curContains.peek().add(entry);
            //endView(docview);
        }
        if (recurse || !doc) {
            for (DocumentElement de: book.getChildren()) {
                de.accept(this);
                if (!recurse)
                    break;
            }
        }
        if (doc)
            endView(book.getFrom());
        sibviews.pop();
    }

    @Override
    public void visit(DBColSpec colspec) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBImage image) {
        //need to populate view elements with elements in image
        JSONObject entry = new JSONObject();
        JSONObject imageEntry = new JSONObject();
        //for (Element e: Project.getProject(image.getImage()).getDiagram(image.getImage()).getUsedModelElements(false)) {
        //    addToElements(e);
        //}
        addToElements(image.getImage());
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
        //gl.log("Exporting diagram to: " + svgDiagramFile.getAbsolutePath());

        // keep record of all images found
        imageEntry.put("cs", String.valueOf(cs));
        imageEntry.put("abspath", svgDiagramFile.getAbsolutePath());
        imageEntry.put("extension", FILE_EXTENSION);
        images.put(svgFilename, imageEntry);

        //MDEV #674 -- Update the type and id: was hard coded.
        //
        entry.put("type", "Image");
        entry.put("sysmlid", image.getImage().getID());
        curContains.peek().add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        DBAlfrescoListVisitor l = new DBAlfrescoListVisitor(recurse, elements);
        list.accept(l);
        curContains.peek().add(l.getObject());
        viewElements.peek().addAll(l.getListElements());
        elementSet.addAll(l.getElementSet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject entry = getJSONForDBParagraph(para);
        curContains.peek().add(entry);
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJSONForDBParagraph(DBParagraph para) {
        JSONObject entry = new JSONObject();
        if (para.getFrom() != null && para.getFromProperty() != null) {
            this.addToElements(para.getFrom());
            entry.put("sourceType", "reference");
            entry.put("source", ExportUtility.getElementID(para.getFrom()));
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
        curContains.peek().add(entry);
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJSONForDBText(DBText text) {
        JSONObject entry = new JSONObject();
        if (text.getFrom() != null && text.getFromProperty() != null) {
            this.addToElements(text.getFrom());
            entry.put("sourceType", "reference");
            entry.put("source", ExportUtility.getElementID(text.getFrom()));
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
            
            
            for (DocumentElement de: section.getChildren()) {
                // if (recurse || !(de instanceof DBSection))
                if (!recurse && de instanceof DBSection && ((DBSection)de).isView())
                    break;
                de.accept(this);
            }
            //sibviews.pop();
            if (section.isNoSection())
                noSections.add(eview.getID());
            endView(eview);
        } else {
        	
        	//JJS -- MDEV #674.
        	//NOTE: for a Section, add a element with type = "Section"
        	//and the name is set to the section title.
        	//Create an array to hold the elements contained within
        	//this section.
        	//
        	Element eSection = section.getFrom();
        	JSONObject newSection = new JSONObject();
        	
        	newSection.put("type", "Section");
        	newSection.put("name", section.getTitle());
        	
        	JSONArray secArray = new JSONArray();
        	newSection.put("contains", secArray);
        	this.curContains.peek().add(newSection);
        	this.curContains.push(secArray);
        	
            for (DocumentElement de: section.getChildren()) {
                de.accept(this);
            }
            //Remove the current JSONArray from
            //the stack.
            //
            this.curContains.pop();
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
        curContains.peek().add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTable table) {
        DBAlfrescoTableVisitor v = new DBAlfrescoTableVisitor(this.recurse, elements);
        table.accept(v);
        curContains.peek().add(v.getObject());
        viewElements.peek().addAll(v.getTableElements());
        elementSet.addAll(v.getElementSet());
    }
        
    @SuppressWarnings("unchecked")
    protected void startView(Element e) {
        JSONObject view = new JSONObject();
        JSONObject specialization = new JSONObject();
        
        //MDEV #673
        //Update code to create a specialization
        //object and then insert appropriate
        //sub-elements in that specialization object.
        //
        if (StereotypesHelper.hasStereotypeOrDerived(e, Utils.getProductStereotype()))
            specialization.put("type", "Product");
        else
            specialization.put("type", "View");
        view.put("specialization", specialization);
        String id = e.getID();
        view.put("sysmlid", id);
        views.put(id, view);
        Set<String> viewE = new HashSet<String>();
        viewElements.push(viewE);
        //JJS : may need to make this a Stack
        JSONArray contains = new JSONArray();
        specialization.put("contains", contains);
        this.curContains.push(contains);
        addToElements(e);
        //MDEV-443 add view exposed elements to view elements
        for (Element exposed: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                DocGen3Profile.queriesStereotype, 1, false, 1))
            addToElements(exposed);
        sibviews.peek().add(e.getID());
        JSONArray childViews = new JSONArray();
        sibviews.push(childViews);
    }
    
    @SuppressWarnings("unchecked")
    protected void endView(Element e) {
        JSONArray viewEs = new JSONArray();
        viewEs.addAll(viewElements.pop());
        //MDEV #673: update code to use the
        //specialization element.
        //
        JSONObject view = (JSONObject)views.get(e.getID());
        JSONObject specialization = (JSONObject)view.get("specialization");
        
        specialization.put("displayedElements", viewEs);
        specialization.put("allowedElements", viewEs);
        if (recurse && !doc)
        	specialization.put("childrenViews", sibviews.peek());
        view2view.put(e.getID(), sibviews.pop());
        this.curContains.pop();
    }

    @SuppressWarnings("unchecked")
    protected void addToElements(Element e) {
        if (!ExportUtility.shouldAdd(e))
            return;
        if (!viewElements.empty())
            viewElements.peek().add(ExportUtility.getElementID(e));
        if (elements.containsKey(e.getID()))
            return;
        elementSet.add(e);
        JSONObject elementInfo = new JSONObject();
        ExportUtility.fillElement(e, elementInfo);
        elements.put(e.getID(), elementInfo);
        /*if (e instanceof DirectedRelationship) {
            JSONObject sourceInfo = new JSONObject();
            JSONObject targetInfo = new JSONObject();
            Element source = ModelHelper.getClientElement(e);
            Element target = ModelHelper.getSupplierElement(e);
            ExportUtility.fillElement(source, sourceInfo);
            ExportUtility.fillElement(target, targetInfo);
            elements.put(source.getID(), sourceInfo);
            elements.put(target.getID(), targetInfo);
        }*/
        //if (e instanceof Property || e instanceof Slot)
        //   elements.putAll(ExportUtility.getReferencedElements(e));
    }
    
    public JSONObject getElements() {
        return elements;
    }
    
    public JSONObject getViews() {
        return views;
    }
    
    public JSONObject getHierarchy() {
        return view2view;
    }
    
    public JSONArray getNosections() {
        return noSections;
    }
    
    public Set<Element> getElementSet() {
        return elementSet;
    }
}

