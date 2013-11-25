package gov.nasa.jpl.mgss.mbee.docgen.model.ui;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.impl.PackageImpl;

public class TreeNode {

    private final String       name;
    private final NamedElement elem;
    private List<TreeNode>     children = new ArrayList<TreeNode>();
    private boolean            state1;
    private boolean            state2;
    private boolean            state3;

    public TreeNode(String name, NamedElement elem) {
        this.name = name;
        this.elem = elem;
    }

    public NamedElement getElem() {
        return elem;
    }

    public boolean isState1() {
        return state1;
    }

    public boolean isState2() {
        return state2;
    }

    public boolean isState3() {
        return state3;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void setState1(boolean state1) {
        this.state1 = state1;
    }

    public void setState2(boolean state2) {
        this.state2 = state2;
    }

    public void setState3(boolean state3) {
        this.state3 = state3;
    }

    public boolean isPackage() {
        return elem != null && (elem instanceof PackageImpl);
    }

}
