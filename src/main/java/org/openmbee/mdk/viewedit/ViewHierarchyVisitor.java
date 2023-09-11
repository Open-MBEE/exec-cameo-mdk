package org.openmbee.mdk.viewedit;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.model.AbstractModelVisitor;
import org.openmbee.mdk.model.Document;
import org.openmbee.mdk.model.Section;

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
