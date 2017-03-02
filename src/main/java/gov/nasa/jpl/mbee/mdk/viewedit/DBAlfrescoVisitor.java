package gov.nasa.jpl.mbee.mdk.viewedit;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import gov.nasa.jpl.mbee.mdk.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.api.docgen.presentation_elements.PresentationElementEnum;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import gov.nasa.jpl.mbee.mdk.generator.PresentationElementInfo;
import gov.nasa.jpl.mbee.mdk.generator.PresentationElementInstance;
import gov.nasa.jpl.mbee.mdk.generator.PresentationElementUtils;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.model.Section;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class DBAlfrescoVisitor extends DBAbstractVisitor {

    private JSONObject views = new JSONObject();
    private Stack<JSONArray> curContains = new Stack<>();  //MDEV #674 -- change to a Stack of JSONArrays
    private Stack<JSONArray> sibviews = new Stack<>(); //sibling views (array of view ids)
    private Stack<List<Element>> sibviewsElements = new Stack<>();
    private Stack<Set<String>> viewElements = new Stack<>(); //ids of view elements
    private Map<String, ObjectNode> images = new HashMap<>();
    protected boolean recurse;
    private GUILog gl = Application.getInstance().getGUILog();
    private static String FILE_EXTENSION = ".svg";

    private Map<From, String> sourceMapping = new HashMap<>();
    private JSONObject view2view = new JSONObject(); //parent view id to array of children view ids (from sibviews)
    private Map<Element, List<Element>> view2viewElements = new HashMap<>();
    private JSONArray noSections = new JSONArray();
    private boolean doc;
    protected Set<Element> elementSet = new HashSet<>();

    //for ems 2.2 reference tree
    // these are linked hash maps to make recursive sense in ViewPresentationGenerator
    private Map<Element, JSONArray> view2elements = new LinkedHashMap<>();
    private Map<Element, List<PresentationElementInstance>> view2pe = new LinkedHashMap<>();
    private Map<Element, List<PresentationElementInstance>> view2peOld = new LinkedHashMap<>();
    private Stack<Element> currentView = new Stack<>();
    private Stack<PresentationElementInstance> currentSection = new Stack<>(); //if currently in section, sections cannot cross views
    private Stack<List<InstanceSpecification>> currentInstanceList = new Stack<>();
    private Stack<List<InstanceSpecification>> currentTableInstances = new Stack<>();
    private Stack<List<InstanceSpecification>> currentListInstances = new Stack<>();
    private Stack<List<InstanceSpecification>> currentParaInstances = new Stack<>();
    private Stack<List<InstanceSpecification>> currentSectionInstances = new Stack<>();
    private Stack<List<InstanceSpecification>> currentImageInstances = new Stack<>();
    private Stack<List<InstanceSpecification>> currentManualInstances = new Stack<>();
    private Stack<List<InstanceSpecification>> currentUnusedInstances = new Stack<>();
    private Stack<List<PresentationElementInstance>> newpe = new Stack<>();

    private boolean main = false; //for ems 2.2 reference tree, only consider generated pe from main view and 
    //not nested tables/lists since those are embedded in json blob, main is false for Table and List Visitor

    private InstanceSpecification viewDocHack = null;
    private PresentationElementUtils viu = new PresentationElementUtils();

    public DBAlfrescoVisitor(boolean recurse) {
        this(recurse, false);
    }

    public DBAlfrescoVisitor(boolean recurse, boolean main) {
        this.recurse = recurse;
        sourceMapping.put(From.DOCUMENTATION, "documentation");
        sourceMapping.put(From.DVALUE, "value");
        sourceMapping.put(From.NAME, "name");
        this.main = main;
    }

    /**
     * Simple getter for images
     */
    public Map<String, ObjectNode> getImages() {
        return images;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBBook book) {
        JSONArray childviews = new JSONArray();
        sibviews.push(childviews);
        sibviewsElements.push(new ArrayList<Element>());

        if (book.getFrom() != null) {
            doc = true;
            Element docview = book.getFrom();
            startView(docview);
            for (DocumentElement de : book.getChildren()) {
                if (de instanceof DBSection && ((DBSection) de).isView()) {
                    break;
                }
                de.accept(this);
            }
        }
        if (recurse || !doc) {
            for (DocumentElement de : book.getChildren()) {
                if (de instanceof DBSection && ((DBSection) de).isView()) {
                    de.accept(this);
                    if (!recurse) {
                        break;
                    }
                }
            }
        }
        if (doc) {
            endView(book.getFrom());
        }
        sibviews.pop();
        sibviewsElements.pop();
    }

    @Override
    public void visit(DBColSpec colspec) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBImage image) {
        //need to populate view elements with elements in image
        JSONObject entry = new JSONObject();
        ObjectNode imageEntry = JacksonUtils.getObjectMapper().createObjectNode();
        //for (Element e: Project.getProject(image.getImage()).getDiagram(image.getImage()).getUsedModelElements(false)) {
        //    addToElements(e);
        //}
        // export image - also keep track of exported images
        DiagramPresentationElement diagram = Application.getInstance().getProject()
                .getDiagram(image.getImage());
        String svgFilename = Converters.getElementToIdConverter().apply(image.getImage());

        // create image file
        String userhome = System.getProperty("user.home");
        File directory = null;
        if (userhome != null) {
            directory = new File(userhome + File.separator + "mdkimages");
        }
        else {
            directory = new File("mdkimages");
        }
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
            byte[] data = new byte[(int) f.length()];
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
        String svgCrcFilename = Converters.getElementToIdConverter().apply(image.getImage()) + "_latest" + FILE_EXTENSION;
        //gl.log("Exporting diagram to: " + svgDiagramFile.getAbsolutePath());

        // keep record of all images found
        imageEntry.put("cs", String.valueOf(cs));
        imageEntry.put("abspath", svgDiagramFile.getAbsolutePath());
        imageEntry.put("extension", FILE_EXTENSION);
        images.put(svgFilename, imageEntry);

        //MDEV #674 -- Update the type and id: was hard coded.
        //
        entry.put("type", "Image");
        entry.put(MDKConstants.ID_KEY, Converters.getElementToIdConverter().apply(image.getImage()));
        entry.put("title", image.getTitle());
        curContains.peek().add(entry);

        //for ems 2.2 reference tree
        if (!main) {
            return;
        }
        InstanceSpecification i = null;
        if (!currentImageInstances.peek().isEmpty()) {
            i = currentImageInstances.peek().remove(0);
            currentInstanceList.peek().remove(0);
        }

        PresentationElementInstance parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElementInstance ipe = new PresentationElementInstance(i, entry, PresentationElementEnum.IMAGE, currentView.peek(), (image.getTitle() == null ? "image" : image.getTitle()), parentSec, null);
        newpe.peek().add(ipe);
        Application.getInstance().getProject().getElementsFactory().createInstanceValueInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        DBAlfrescoListVisitor l = new DBAlfrescoListVisitor(recurse);
        list.accept(l);
        curContains.peek().add(l.getObject());
        viewElements.peek().addAll(l.getListElements());
        elementSet.addAll(l.getElementSet());

        //for ems 2.2 reference tree
        if (!main) {
            return;
        }
        InstanceSpecification i = null;
        if (!currentListInstances.peek().isEmpty()) {
            i = currentListInstances.peek().remove(0);
            currentInstanceList.peek().remove(0);
        }

        PresentationElementInstance parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElementInstance ipe = new PresentationElementInstance(i, l.getObject(), PresentationElementEnum.LIST, currentView.peek(), "list", parentSec, null);
        newpe.peek().add(ipe);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject entry = getJSONForDBParagraph(para);
        curContains.peek().add(entry);

        //for ems 2.2 reference tree
        if (!main) {
            return;
        }
        InstanceSpecification i = null;
        if (!currentParaInstances.peek().isEmpty()) {
            i = currentParaInstances.peek().remove(0);
            currentInstanceList.peek().remove(0);
        }

        PresentationElementInstance parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElementInstance ipe = new PresentationElementInstance(i, entry, PresentationElementEnum.PARAGRAPH, currentView.peek(), "paragraph", parentSec, null);
        newpe.peek().add(ipe);
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJSONForDBParagraph(DBParagraph para) {
        JSONObject entry = new JSONObject();
        if (para.getFrom() != null && para.getFromProperty() != null) {
            entry.put("sourceType", "reference");
            entry.put("source", Converters.getElementToIdConverter().apply(para.getFrom()));
            entry.put("sourceProperty", sourceMapping.get(para.getFromProperty()));
        }
        else {
            entry.put("sourceType", "text");
            entry.put("text", DocGenUtils.addP(DocGenUtils.fixString(para.getText(), false)));
        }
        entry.put("nonEditable", para.isEditable() != null && !para.isEditable());
        entry.put("type", "Paragraph");
        return entry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBText text) {
        JSONObject entry = getJSONForDBText(text);
        curContains.peek().add(entry);

        //for ems 2.2 reference tree
        if (!main) {
            return;
        }
        InstanceSpecification i = null;
        if (!currentParaInstances.peek().isEmpty()) {
            i = currentParaInstances.peek().remove(0);
            currentInstanceList.peek().remove(0);
        }

        PresentationElementInstance parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElementInstance ipe = new PresentationElementInstance(i, entry, PresentationElementEnum.PARAGRAPH, currentView.peek(), "paragraph", parentSec, null);
        newpe.peek().add(ipe);
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getJSONForDBText(DBText text) {
        JSONObject entry = new JSONObject();
        if (text.getFrom() != null && text.getFromProperty() != null) {
            entry.put("sourceType", "reference");
            entry.put("source", Converters.getElementToIdConverter().apply(text.getFrom()));
            entry.put("sourceProperty", sourceMapping.get(text.getFromProperty()));
        }
        else {
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

            for (DocumentElement de : section.getChildren()) {
                // if (recurse || !(de instanceof DBSection))
                if (!recurse && de instanceof DBSection && ((DBSection) de).isView()) {
                    break;
                }
                de.accept(this);
                addManualInstances(false);
            }
            //sibviews.pop();
            if (section.isNoSection()) {
                noSections.add(Converters.getElementToIdConverter().apply(eview));
            }
            endView(eview);
        }
        else {
            startSection(section);
            for (DocumentElement de : section.getChildren()) {
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

    @Override
    public void visit(DBTomSawyerDiagram  tomSawyerDiagram) {
      //  super.visit(tomSawyerDiagram);
      //`  tomSawyerDiagram.accept();
        JSONObject entry = new JSONObject();
       // entry.put("sourceType", "text");
        entry.put("type", "Tsp");
        entry.put("tstype" , tomSawyerDiagram.getShortType().toString());
        // here enter a list of all the elements we need.
        JSONArray elements = new JSONArray();
        for(Element elem : tomSawyerDiagram.getElements()) {
            elements.add(Converters.getElementToIdConverter().apply(elem));
        }

        entry.put("elements", elements);

        curContains.peek().add(entry);

        InstanceSpecification i = null;
        if (!currentTableInstances.peek().isEmpty()) {
            i = currentTableInstances.peek().remove(0);
            currentInstanceList.remove(i);
        }

        PresentationElementInstance parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElementInstance ipe = new PresentationElementInstance(i, entry, PresentationElementEnum.TABLE, currentView.peek(), "tomsawyer_diagram", parentSec, null);
        System.out.println(entry.toJSONString());
        newpe.peek().add(ipe);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTable table) {
        DBAlfrescoTableVisitor v = new DBAlfrescoTableVisitor(this.recurse);
        table.accept(v);
        curContains.peek().add(v.getObject());
        viewElements.peek().addAll(v.getTableElements());
        elementSet.addAll(v.getElementSet());

        //for ems 2.2 reference tree
        if (!main) {
            return;
        }
        InstanceSpecification i = null;
        if (!currentTableInstances.peek().isEmpty()) {
            i = currentTableInstances.peek().remove(0);
            currentInstanceList.peek().remove(0);
        }

        PresentationElementInstance parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        PresentationElementInstance ipe = new PresentationElementInstance(i, v.getObject(), PresentationElementEnum.TABLE, currentView.peek(), table.getTitle() != null ? table.getTitle() : "table", parentSec, null);
        newpe.peek().add(ipe);
    }

    @SuppressWarnings("unchecked")
    public void startView(Element e) {
        JSONObject view = new JSONObject();
//        JSONObject specialization = new JSONObject();

        //MDEV #673
        //Update code to create a specialization
        //object and then insert appropriate
        //sub-elements in that specialization object.
        //
        if (StereotypesHelper.hasStereotypeOrDerived(e, Utils.getProductStereotype())) {
            view.put("type", "Product");
        }
        else {
            view.put("type", "View");
        }
        String id = Converters.getElementToIdConverter().apply(e);
        view.put(MDKConstants.ID_KEY, id);
        views.put(id, view);
        Set<String> viewE = new HashSet<String>();
        viewElements.push(viewE);
        //JJS : may need to make this a Stack
        JSONArray contains = new JSONArray();
        view.put("contains", contains);
        this.curContains.push(contains);
        //MDEV-443 add view exposed elements to view elements
        /*for (Element exposed: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                DocGen3Profile.queriesStereotype, 1, false, 1))
            addToElements(exposed);*/
        sibviews.peek().add(Converters.getElementToIdConverter().apply(e));
        sibviewsElements.peek().add(e);
        JSONArray childViews = new JSONArray();
        sibviews.push(childViews);
        sibviewsElements.push(new ArrayList<Element>());

        //for ems 2.2 reference tree
        currentView.push(e);
        List<PresentationElementInstance> viewChildren = new ArrayList<PresentationElementInstance>();
        newpe.push(viewChildren);
        view2pe.put(e, viewChildren);
        view2peOld.put(e, new ArrayList<PresentationElementInstance>());

        processCurrentInstances(e, e);
        if (currentInstanceList.peek().isEmpty()) { //new view, add view doc hack
            PresentationElementInstance hack = new PresentationElementInstance(null, null, null, e, null, null, null);
            hack.setManual(true);
            hack.setViewDocHack(true);
            newpe.peek().add(hack);
        }
        addManualInstances(false);
    }

    @SuppressWarnings("unchecked")
    public void endView(Element e) {
        JSONArray viewEs = new JSONArray();
        viewEs.addAll(viewElements.pop());
        //MDEV #673: update code to use the
        //specialization element.
        //
        JSONObject view = (JSONObject) views.get(Converters.getElementToIdConverter().apply(e));

        view.put("displayedElements", viewEs);
        view.put("allowedElements", viewEs);
        if (recurse && !doc) {
            view.put("childrenViews", sibviews.peek());
        }
        view2view.put(Converters.getElementToIdConverter().apply(e), sibviews.pop());
        view2viewElements.put(e, sibviewsElements.pop());
        this.curContains.pop();

        //for ems 2.2 reference tree
        view2elements.put(e, viewEs);
        addManualInstances(true);
        processUnusedInstances(e);
        List<PresentationElementInstance> pes = newpe.pop();
        if (pes.isEmpty()) {
            //new view with nothing, auto add a pe that with cf that points to view doc
            PresentationElementInstance hack = new PresentationElementInstance(null, null, null, e, null, null, null);
            hack.setManual(true);
            hack.setViewDocHack(true);
            pes.add(hack);
        }
        currentView.pop();
        currentManualInstances.pop();
        currentImageInstances.pop();
        currentSectionInstances.pop();
        currentParaInstances.pop();
        currentListInstances.pop();
        currentTableInstances.pop();
        currentInstanceList.pop();
        currentUnusedInstances.pop();
    }

    protected void startSection(DBSection section) {
        JSONObject newSection = new JSONObject();

        newSection.put("type", "Section");
        newSection.put("name", section.getTitle());

        JSONArray secArray = new JSONArray();
        newSection.put("contains", secArray);
        this.curContains.peek().add(newSection);
        this.curContains.push(secArray);

        //for ems 2.2 reference tree
        InstanceSpecification sec = null;
        Element loopElement = null;
        if (section.getDgElement() instanceof Section) {
            if (((Section) section.getDgElement()).getLoopElement() != null) {
                loopElement = ((Section) section.getDgElement()).getLoopElement();
                sec = findInstanceForSection(loopElement);
            }
            else {
                sec = findInstanceForSection(null);
            }
        }
        if (sec != null) {
            currentInstanceList.peek().remove(sec);
            currentSectionInstances.peek().remove(sec);
        }

        PresentationElementInstance parentSec = currentSection.isEmpty() ? null : currentSection.peek();
        List<PresentationElementInstance> secChildren = new ArrayList<PresentationElementInstance>();
        PresentationElementInstance pe = new PresentationElementInstance(sec, newSection, PresentationElementEnum.SECTION, currentView.peek(), section.getTitle() != null ? section.getTitle() : "section", parentSec, secChildren);
        pe.setLoopElement(loopElement);
        newpe.peek().add(pe);
        currentSection.push(pe);
        newpe.push(secChildren);
        processCurrentInstances(sec, currentView.peek());
        addManualInstances(false);
    }

    protected void endSection(DBSection section) {
        this.curContains.pop();

        //for ems 2.2 reference tree
        addManualInstances(true);
        processUnusedInstances(currentView.peek());
        newpe.pop();
        currentSection.pop();
        currentManualInstances.pop();
        currentImageInstances.pop();
        currentSectionInstances.pop();
        currentParaInstances.pop();
        currentListInstances.pop();
        currentTableInstances.pop();
        currentInstanceList.pop();
        currentUnusedInstances.pop();
    }

    @Deprecated
    public JSONObject getViews() {
        return views;
    }

    public JSONObject getHierarchy() {
        return view2view;
    }

    public Map<Element, List<Element>> getHierarchyElements() {
        return view2viewElements;
    }

    public JSONArray getNosections() {
        return noSections;
    }

    public Map<Element, JSONArray> getView2Elements() {
        return view2elements;
    }

    public Set<Element> getElementSet() {
        return elementSet;
    }

    public Map<Element, List<PresentationElementInstance>> getView2Pe() {
        return view2pe;
    }

    public Map<Element, List<PresentationElementInstance>> getView2Unused() {
        return view2peOld;
    }

    private void processCurrentInstances(Element viewOrSection, Element view) {
        PresentationElementInfo info = viu.getCurrentInstances(viewOrSection, view);
        currentInstanceList.push(info.getAll());
        currentImageInstances.push(info.getImages());
        currentTableInstances.push(info.getTables());
        currentParaInstances.push(info.getParas());
        currentListInstances.push(info.getLists());
        currentSectionInstances.push(info.getSections());
        currentManualInstances.push(info.getManuals());
        currentUnusedInstances.push(info.getUnused());
        if (info.getViewDocHack() != null) {
            viewDocHack = info.getViewDocHack();
        }
    }

    private InstanceSpecification findInstanceForSection(Element e) {
        if (e != null) {
            for (InstanceSpecification is : currentSectionInstances.peek()) {
                for (Element el : is.getOwnedElement()) {
                    if (el instanceof Slot && ((Slot) el).getDefiningFeature().getName().equals("generatedFromElement") &&
                            !((Slot) el).getValue().isEmpty() && ((Slot) el).getValue().get(0) instanceof ElementValue &&
                            ((ElementValue) ((Slot) el).getValue().get(0)).getElement() == e) {
                        return is;
                    }
                }
            }
            return null;
        }
        for (InstanceSpecification is : currentSectionInstances.peek()) {
            boolean loop = false;
            for (Element el : is.getOwnedElement()) {
                if (el instanceof Slot && ((Slot) el).getDefiningFeature().getName().equals("generatedFromElement")) {
                    loop = true;
                }
                break;
            }
            if (loop) {
                continue;
            }
            return is;
        }
        return null;
    }

    private void addManualInstances(boolean all) {
        List<InstanceSpecification> instances = currentInstanceList.peek();
        List<InstanceSpecification> manuals = currentManualInstances.peek();
        while (!instances.isEmpty() && manuals.contains(instances.get(0))) {
            InstanceSpecification is = instances.get(0);
            PresentationElementInstance pe = new PresentationElementInstance(is, null, null, null, null, null, null);
            pe.setManual(true);
            if (is == viewDocHack) {
                pe.setViewDocHack(true);
                pe.setView(currentView.peek());
                viewDocHack = null;
            }
            newpe.peek().add(pe);
            manuals.remove(is);
            instances.remove(is);
        }
        if (all) {
            for (InstanceSpecification is : new ArrayList<InstanceSpecification>(manuals)) {
                PresentationElementInstance pe = new PresentationElementInstance(is, null, null, null, null, null, null);
                pe.setManual(true);
                if (is == viewDocHack) {
                    pe.setViewDocHack(true);
                    pe.setView(currentView.peek());
                    viewDocHack = null;
                }
                newpe.peek().add(pe);
                manuals.remove(is);
                instances.remove(is);
            }
        }
    }

    private void processUnusedInstances(Element v) {
        for (InstanceSpecification is : currentTableInstances.peek()) {
            view2peOld.get(v).add(new PresentationElementInstance(is, null, PresentationElementEnum.TABLE, v, is.getName(), null, null));
        }
        for (InstanceSpecification is : currentListInstances.peek()) {
            view2peOld.get(v).add(new PresentationElementInstance(is, null, PresentationElementEnum.LIST, v, is.getName(), null, null));
        }
        for (InstanceSpecification is : currentParaInstances.peek()) {
            view2peOld.get(v).add(new PresentationElementInstance(is, null, PresentationElementEnum.PARAGRAPH, v, is.getName(), null, null));
        }
        for (InstanceSpecification is : currentImageInstances.peek()) {
            view2peOld.get(v).add(new PresentationElementInstance(is, null, PresentationElementEnum.IMAGE, v, is.getName(), null, null));
        }
        for (InstanceSpecification is : currentSectionInstances.peek()) {
            view2peOld.get(v).add(new PresentationElementInstance(is, null, PresentationElementEnum.SECTION, v, is.getName(), null, null));
        }
        for (InstanceSpecification is : currentUnusedInstances.peek()) {
            view2peOld.get(v).add(new PresentationElementInstance(is, null, null, v, is.getName(), null, null));
        }
    }
}

