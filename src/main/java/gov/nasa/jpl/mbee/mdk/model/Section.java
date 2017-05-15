package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.List;

/**
 * this should really be called View now
 *
 * @author dlam
 */
public class Section extends Container {
    private boolean isAppendix;
    private boolean isChapter;
    private String id;
    private boolean isView;
    private boolean isNoSection;

    private Element viewpoint; //if view, the viewpoint
    private List<Element> exposes; //if view, the elements exposed
    private Element loopElement; //if dynamic section, the element that generated this section (if looped)

    public Section() {
        isAppendix = false;
        isChapter = false;
    }

    public void isAppendix(boolean a) {
        isAppendix = a;
    }

    public boolean isAppendix() {
        return isAppendix;
    }

    public void isChapter(boolean c) {
        isChapter = c;
    }

    public boolean isChapter() {
        return isChapter;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setView(boolean b) {
        this.isView = b;
    }

    public boolean isView() {
        return this.isView;
    }

    public void setNoSection(boolean b) {
        this.isNoSection = b;
    }

    public boolean isNoSection() {
        return this.isNoSection;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public void accept(IModelVisitor v) {
        v.visit(this);
    }

    @Override
    public String toStringStart() {
        return super.toStringStart() + ",id=" + id;
    }

    public Element getViewpoint() {
        return viewpoint;
    }

    public void setViewpoint(Element viewpoint) {
        this.viewpoint = viewpoint;
    }

    public List<Element> getExposes() {
        return exposes;
    }

    public void setExposes(List<Element> exposes) {
        this.exposes = exposes;
    }

    public Element getLoopElement() {
        return loopElement;
    }

    public void setLoopElement(Element loopElement) {
        this.loopElement = loopElement;
    }

}
