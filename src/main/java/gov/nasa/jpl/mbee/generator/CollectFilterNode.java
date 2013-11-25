package gov.nasa.jpl.mbee.generator;

import java.util.Collection;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CollectFilterNode {
    ActivityNode              n;
    List<Element>             result; // output of running the collect filter
                                      // action
    Collection<List<Element>> inputs; // collect filter action can have multiple
                                      // inputs

    public CollectFilterNode(ActivityNode a) {
        n = a;
    }

    public List<Element> getResult() {
        return result;
    }

    public ActivityNode getNode() {
        return n;
    }

    public void setResult(List<Element> r) {
        result = r;
    }

    public void setInputs(Collection<List<Element>> c) {
        inputs = c;
    }

}
