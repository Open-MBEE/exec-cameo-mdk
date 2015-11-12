package gov.nasa.jpl.mbee.viewedit;

import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;

public class PresentationElement {
    public enum PEType {PARA, IMAGE, TABLE, LIST, SECTION};
    private InstanceSpecification instance; //existing instance, null if no existing instance to use
    private JSONObject newspec; //the to be json
    private PEType type;
    private Element view; //view that generated this pe, can be null if not generated
    private String name;
    private PresentationElement parent; //section if applicable, otherwise null, use view
    private List<PresentationElement> children; //if section
    private Element loopElement; //if section is generated from model element from docgen
    private boolean manual = false; //if manual is true, just use existing instance, not generated from docgen
    private boolean viewDocHack = false; //if opaque para view doc generated, change it so it's not opaque and point to itself with a transclusion to view instead
    
    public PresentationElement(InstanceSpecification instance, JSONObject spec, PEType type, Element view, String name, PresentationElement parent, List<PresentationElement> children) {
        this.instance = instance;
        this.newspec = spec;
        this.type = type;
        this.view = view;
        this.name = name;
        this.parent = parent;
        this.children = children;
    }
    
    public InstanceSpecification getInstance() {
        return instance;
    }
    public void setInstance(InstanceSpecification instance) {
        this.instance = instance;
    }
    public JSONObject getNewspec() {
        return newspec;
    }
    public void setNewspec(JSONObject newspec) {
        this.newspec = newspec;
    }
    public PEType getType() {
        return type;
    }
    public void setType(PEType type) {
        this.type = type;
    }
    public Element getView() {
        return view;
    }
    public void setView(Element view) {
        this.view = view;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public PresentationElement getParent() {
        return parent;
    }
    public void setParent(PresentationElement parent) {
        this.parent = parent;
    }
    public List<PresentationElement> getChildren() {
        return children;
    }
    public void setChildren(List<PresentationElement> children) {
        this.children = children;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public Element getLoopElement() {
        return loopElement;
    }

    public void setLoopElement(Element loopElement) {
        this.loopElement = loopElement;
    }

    public boolean isViewDocHack() {
        return viewDocHack;
    }

    public void setViewDocHack(boolean viewDocHack) {
        this.viewDocHack = viewDocHack;
    }
}
