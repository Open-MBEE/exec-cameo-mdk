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

    private JSONObject result = new JSONObject();
    private Map<Element, List<Element>> resultElements = new HashMap<Element, List<Element>>();
    private Stack<JSONArray> curChildren = new Stack<JSONArray>();
    private Stack<List<Element>> curChildrenElements = new Stack<List<Element>>();
    private JSONArray nosections = new JSONArray();

    @SuppressWarnings("unchecked")
    public JSONObject getResult() {
        JSONObject res = new JSONObject();
        res.put("views", result);
        res.put("noSections", nosections);
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Document doc) {
        if (doc.getDgElement() != null) {
            curChildren.push(new JSONArray());
            curChildrenElements.push(new ArrayList<Element>());
        }
        visitChildren(doc);
        if (doc.getDgElement() != null) {
            result.put(Converters.getElementToIdConverter().apply(doc.getDgElement()), curChildren.pop());
            resultElements.put(doc.getDgElement(), curChildrenElements.pop());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Section sec) {
        if (sec.isView()) {
            if (sec.isNoSection()) {
                nosections.add(Converters.getElementToIdConverter().apply(sec.getDgElement()));
            }
            if (!curChildren.isEmpty()) {
                curChildren.peek().add(Converters.getElementToIdConverter().apply(sec.getDgElement()));
                curChildrenElements.peek().add(sec.getDgElement());
            }
            curChildren.push(new JSONArray());
            curChildrenElements.push(new ArrayList<>());
        }
        visitChildren(sec);
        if (sec.isView()) {
            result.put(Converters.getElementToIdConverter().apply(sec.getDgElement()), curChildren.pop());
            resultElements.put(sec.getDgElement(), curChildrenElements.pop());
        }
    }

    public JSONObject getView2View() {
        return result;
    }

    public Map<Element, List<Element>> getView2ViewElements() {
        return resultElements;
    }

    public JSONArray getNosections() {
        return nosections;
    }
}
