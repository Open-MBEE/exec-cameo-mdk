package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Section;
import gov.nasa.jpl.mbee.viewedit.PresentationElement.PEType;
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
import java.util.ArrayList;
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class DBAlfrescoVisitor extends DBAbstractVisitor {

    protected JSONObject elements;
    private JSONObject   views;
    private Stack<JSONArray>          curContains;  //MDEV #674 -- change to a Stack of JSONArrays
    private Stack<JSONArray>          sibviews; //sibling views (array of view ids)
    private Stack<Set<String>>        viewElements; //ids of view elements
    private Map<String, JSONObject>   images;        
    protected boolean                 recurse;
    private GUILog                    gl;
    private static String             FILE_EXTENSION = ".svg";
    private Stereotype view = Utils.getViewStereotype();
    private Stereotype viewpoint = Utils.getViewpointStereotype();

    private Map<From, String> sourceMapping;
    private JSONObject                view2view; //parent view id to array of children view ids (from sibviews)
    private JSONArray                 noSections = new JSONArray();
    private boolean doc;
    protected Set<Element> elementSet = new HashSet<Element>();
    
    private Stack<Element> currentView;
    private Stack<PresentationElement> currentSection; //if currently in section, sections cannot cross views
    private Stack<List<InstanceSpecification>> currentInstanceList = new Stack<List<InstanceSpecification>>();
    private Stack<List<InstanceSpecification>> currentTableInstances = new Stack<List<InstanceSpecification>>();
    private Stack<List<InstanceSpecification>> currentListInstances = new Stack<List<InstanceSpecification>>();
    private Stack<List<InstanceSpecification>> currentParaInstances = new Stack<List<InstanceSpecification>>();
    private Stack<List<InstanceSpecification>> currentSectionInstances = new Stack<List<InstanceSpecification>>();
    private Stack<List<InstanceSpecification>> currentImageInstances = new Stack<List<InstanceSpecification>>();
    private Stack<List<InstanceSpecification>> currentManualInstances = new Stack<List<InstanceSpecification>>();
    private Stack<List<PresentationElement>> newpe = new Stack<List<PresentationElement>>();
    private Classifier paraC = Utils.getOpaqueParaClassifier();
    private Classifier tableC = Utils.getOpaqueTableClassifier();
    private Classifier listC = Utils.getOpaqueListClassifier();
    private Classifier imageC = Utils.getOpaqueImageClassifier();
    private Classifier sectionC = Utils.getSectionClassifier();
    private boolean main = false;
    
    public DBAlfrescoVisitor(boolean recurse) {
        this(recurse, false);
    }
    
    public DBAlfrescoVisitor(boolean recurse, boolean main) {
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
        this.main = main;

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
        
        if (!main)
            return;
        InstanceSpecification i = null;
        if (!currentImageInstances.peek().isEmpty()) {
            i = currentImageInstances.peek().remove(0);
            currentInstanceList.remove(i);
        }
        PresentationElement parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElement ipe = new PresentationElement(i, entry, PEType.IMAGE, currentView.peek(), "image", parentSec, null);
        newpe.peek().add(ipe);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        DBAlfrescoListVisitor l = new DBAlfrescoListVisitor(recurse, elements);
        list.accept(l);
        curContains.peek().add(l.getObject());
        viewElements.peek().addAll(l.getListElements());
        elementSet.addAll(l.getElementSet());
        
        if (!main)
            return;
        InstanceSpecification i = null;
        if (!currentListInstances.peek().isEmpty()) {
            i = currentListInstances.peek().remove(0);
            currentInstanceList.remove(i);
        }
        PresentationElement parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElement ipe = new PresentationElement(i, l.getObject(), PEType.LIST, currentView.peek(), "list", parentSec, null);
        newpe.peek().add(ipe);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject entry = getJSONForDBParagraph(para);
        curContains.peek().add(entry);
        
        if (!main)
            return;
        InstanceSpecification i = null;
        if (!currentParaInstances.peek().isEmpty()) {
            i = currentParaInstances.peek().remove(0);
            currentInstanceList.remove(i);
        }
        PresentationElement parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElement ipe = new PresentationElement(i, entry, PEType.PARA, currentView.peek(), "paragraph", parentSec, null);
        newpe.peek().add(ipe);
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
        
        if (!main)
            return;
        InstanceSpecification i = null;
        if (!currentParaInstances.peek().isEmpty()) {
            i = currentParaInstances.peek().remove(0);
            currentInstanceList.remove(i);
        }
        PresentationElement parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElement ipe = new PresentationElement(i, entry, PEType.PARA, currentView.peek(), "paragraph", parentSec, null);
        newpe.peek().add(ipe);
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
                addManualInstances(false);
            }
            //sibviews.pop();
            if (section.isNoSection())
                noSections.add(eview.getID());
            endView(eview);
        } else {
        	startSection(section);
            for (DocumentElement de: section.getChildren()) {
                de.accept(this);
                addManualInstances(false);
            }
            endSection(section);
            
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
        
        if (!main)
            return;
        InstanceSpecification i = null;
        if (!currentTableInstances.peek().isEmpty()) {
            i = currentTableInstances.peek().remove(0);
            currentInstanceList.remove(i);
        }
        PresentationElement parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElement ipe = new PresentationElement(i, v.getObject(), PEType.TABLE, currentView.peek(), table.getTitle(), parentSec, null);
        newpe.peek().add(ipe);
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
        
        currentView.push(e);
        List<PresentationElement> viewChildren = new ArrayList<PresentationElement>();
        newpe.push(viewChildren);
        Expression ex = null;
        Constraint c = findViewConstraint(e);
        if (c != null && c.getSpecification() instanceof Expression)
            ex = (Expression)c.getSpecification();
        processCurrentInstances(ex, e);
        
        addManualInstances(false);
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
        
        addManualInstances(true);
        currentView.pop();
        currentManualInstances.pop();
        currentImageInstances.pop();
        currentSectionInstances.pop();
        currentParaInstances.pop();
        currentListInstances.pop();
        currentTableInstances.pop();
        currentInstanceList.pop();
    }

    protected void startSection(DBSection section) {
        JSONObject newSection = new JSONObject();
        
        newSection.put("type", "Section");
        newSection.put("name", section.getTitle());
        
        JSONArray secArray = new JSONArray();
        newSection.put("contains", secArray);
        this.curContains.peek().add(newSection);
        this.curContains.push(secArray);
        
        
        InstanceSpecification sec = null;
        if (section.getDgElement() instanceof Section) {
            if (((Section)section.getDgElement()).getLoopElement() != null)
                sec = findInstanceForSection(((Section)section.getDgElement()).getLoopElement());
            else
                sec = findInstanceForSection(null);
        }
        if (sec != null) {
            currentInstanceList.remove(sec);
            currentSectionInstances.remove(sec);
        }
        PresentationElement parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        List<PresentationElement> secChildren = new ArrayList<PresentationElement>();
        PresentationElement pe = new PresentationElement(sec, newSection, PEType.SECTION, currentView.peek(), section.getTitle(), parentSec, secChildren);
        newpe.peek().add(pe);
        currentSection.push(pe);
        newpe.push(secChildren);
        Expression e = null;
        if (sec != null && sec.getSpecification() instanceof Expression)
            e = (Expression)sec.getSpecification();
        processCurrentInstances(e, currentView.peek());
        
        addManualInstances(false);
    }
    
    protected void endSection(DBSection section) {
        this.curContains.pop();
        
        addManualInstances(true);
        currentSection.pop();
        currentManualInstances.pop();
        currentImageInstances.pop();
        currentSectionInstances.pop();
        currentParaInstances.pop();
        currentListInstances.pop();
        currentTableInstances.pop();
        currentInstanceList.pop();
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
    
    private Constraint findViewConstraint(Element view) {
        return null;
    }
    
    private void processCurrentInstances(Expression e, Element view) {
        List<InstanceSpecification> tables = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> lists = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> sections = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> paras = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> images = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> manuals = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> all = new ArrayList<InstanceSpecification>();
        if (e != null) {
            for (ValueSpecification vs: e.getOperand()) {
                if (vs instanceof InstanceValue) {
                    InstanceSpecification is = ((InstanceValue)vs).getInstance();
                    if (!is.getClassifier().isEmpty()) {
                        List<Classifier> iscs = is.getClassifier();
                        boolean viewinstance = false;
                        for (Element el: is.getOwnedElement()) {
                            if (el instanceof Slot && ((Slot)el).getDefiningFeature().getName().equals("generatedFromView") &&
                                    !((Slot)el).getValue().isEmpty() && ((Slot)el).getValue().get(0) instanceof ElementValue &&
                                    ((ElementValue)((Slot)el).getValue().get(0)).getElement() == view)
                                viewinstance = true;
                        }
                        if (viewinstance) {//instance generated by view
                            if (iscs.contains(paraC))
                                paras.add(is);
                            else if (iscs.contains(tableC))
                                tables.add(is);
                            else if (iscs.contains(listC))
                                lists.add(is);
                            else if (iscs.contains(imageC))
                                images.add(is);
                            else if (iscs.contains(sectionC))
                                sections.add(is);
                        } else {
                            manuals.add(is);
                        }
                        all.add(is);
                    }
                }
            }
        }
        currentInstanceList.push(all);
        currentImageInstances.push(images);
        currentTableInstances.push(tables);
        currentParaInstances.push(paras);
        currentListInstances.push(lists);
        currentSectionInstances.push(sections);
        currentManualInstances.push(manuals);
    }
    
    private InstanceSpecification findInstanceForSection(Element e) {
        if (e != null) {
            for (InstanceSpecification is: currentSectionInstances.peek()) {
                for (Element el: is.getOwnedElement()) {
                    if (el instanceof Slot && ((Slot)el).getDefiningFeature().getName().equals("generatedFromElement") &&
                            !((Slot)el).getValue().isEmpty() && ((Slot)el).getValue().get(0) instanceof ElementValue &&
                            ((ElementValue)((Slot)el).getValue().get(0)).getElement() == e)
                        return is;
                }
            }
            return null;
        }
        for (InstanceSpecification is: currentSectionInstances.peek()) {
            boolean loop = false;
            for (Element el: is.getOwnedElement()) {
                if (el instanceof Slot && ((Slot)el).getDefiningFeature().getName().equals("generatedFromElement"))
                    loop = true;
                    break;
            }
            if (loop)
                continue;
            return is;
        }
        return null;
    }

    private void addManualInstances(boolean all) {
        List<InstanceSpecification> instances = currentInstanceList.peek();
        List<InstanceSpecification> manuals = currentManualInstances.peek();
        while (!instances.isEmpty() && manuals.contains(instances.get(0))) {
            InstanceSpecification is = instances.get(0);
            PresentationElement pe = new PresentationElement(is, null, null, null, null, null, null);
            pe.setManual(true);
            newpe.peek().add(pe);
            manuals.remove(is);
            instances.remove(is);
        }
        if (all) {
            for (InstanceSpecification is: manuals) {
                PresentationElement pe = new PresentationElement(is, null, null, null, null, null, null);
                pe.setManual(true);
                newpe.peek().add(pe);
                manuals.remove(is);
                instances.remove(is);
            }
        }
    }
}

