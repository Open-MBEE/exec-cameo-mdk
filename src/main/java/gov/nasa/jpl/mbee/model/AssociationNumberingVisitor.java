package gov.nasa.jpl.mbee.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.impl.ElementsFactory;


public class AssociationNumberingVisitor extends AbstractModelVisitor {

    private List<Integer> numbers = new ArrayList<Integer>();
    private Stack<Class> parentView = new Stack<Class>();
    private ElementsFactory ef;
    
    public AssociationNumberingVisitor() {
        ef = Application.getInstance().getProject().getElementsFactory();
    }
    
    @Override
    public void visit(Document doc) {
        if (doc.getDgElement() != null) {
            Class d = (Class)doc.getDgElement();
            parentView.push(d);
            numbers.add(1);
            visitChildren(doc);
        }
    }
    
    @Override
    public void visit(Section sec) {
        if (sec.isView() && !sec.isNoSection()) {
            Class v = (Class)sec.getDgElement();
            Association a = findAssociation(v, parentView.peek());
            if (a != null && a.isEditable()) {
                a.setName(getCurrentNumber());
            } else {
                Application.getInstance().getGUILog().log("[WARNING] Association doesn't exist or is not editable for view " + v.getQualifiedName());
            }
            parentView.push(v);
            numbers.add(1);
            visitChildren(sec);
            numbers.remove(numbers.size()-1);
            parentView.pop();
        }
    }
    
    private String getCurrentNumber() {
        return StringUtils.join(numbers, '.');
    }
    
    private Association findAssociation(Class child, Class parent) {
        for (Property p: parent.getOwnedAttribute()) {
            if (p.getType() == child) {
                return p.getAssociation();
            }
        }
        return null;
    }
    
    @Override
    protected void visitChildren(Container c) {
        for (DocGenElement dge: c.getChildren()) {
            dge.accept(this);
            Integer next = numbers.remove(numbers.size()-1);
            numbers.add(next + 1);
        }
    }
}
