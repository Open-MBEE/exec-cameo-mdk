package gov.nasa.jpl.mbee.mdk.viewedit;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.model.AbstractModelVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.Section;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;


public class ViewHierarchyVisitor extends AbstractModelVisitor {

    private Map<Element, List<Element>> resultElements = new HashMap<Element, List<Element>>();
    private Stack<List<Element>> curChildrenElements = new Stack<List<Element>>();

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Document doc) {
        if (doc.getDgElement() != null) {
            curChildrenElements.push(new ArrayList<Element>());
        }
        visitChildren(doc);
        if (doc.getDgElement() != null) {
            resultElements.put(doc.getDgElement(), curChildrenElements.pop());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Section sec) {
        if (sec.isView()) {
            if (!curChildrenElements.isEmpty()) {
                curChildrenElements.peek().add(sec.getDgElement());
            }
            curChildrenElements.push(new ArrayList<>());
        }
        visitChildren(sec);
        if (sec.isView()) {
            resultElements.put(sec.getDgElement(), curChildrenElements.pop());
        }
    }

    public Map<Element, List<Element>> getView2ViewElements() {
        return resultElements;
    }
}
