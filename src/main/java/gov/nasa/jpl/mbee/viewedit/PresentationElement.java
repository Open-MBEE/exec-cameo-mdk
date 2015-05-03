package gov.nasa.jpl.mbee.viewedit;

import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;

public class PresentationElement {
    public enum PEType {PARA, IMAGE, TABLE, LIST, SECTION};
    private InstanceSpecification instance;
    private JSONObject newspec;
    private PEType type;
    private Element view; //view that generated this pe, can be null if not generated
    private String name;
    private PresentationElement parent; //section if applicable, otherwise null, use view
    private List<PresentationElement> children;
    
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
}
