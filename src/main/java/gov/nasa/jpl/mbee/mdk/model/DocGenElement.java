package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.SysMLExtensions;

public abstract class DocGenElement implements IDocGenElement {

    protected boolean ignore;
    protected boolean loop;
    protected String titleSuffix;
    protected String titlePrefix;
    protected boolean useContextNameAsTitle;
    /**
     * this is usually the call behavior action element in a viewpoint method
     */
    protected Element dgElement; //the call behavior action/structured activity that correspond to this/ the view element if a view section
    protected SysMLExtensions profile;
    public DocGenElement() {
        ignore = false;
        loop = false;
        titleSuffix = "";
        titlePrefix = "";
        useContextNameAsTitle = false;
    }

    public boolean getIgnore() {
        return ignore;
    }

    public void setIgnore(boolean i) {
        ignore = i;
    }

    public void setTitleSuffix(String s) {
        titleSuffix = s;
    }

    public void setTitlePrefix(String s) {
        titlePrefix = s;
    }

    public void setUseContextNameAsTitle(boolean b) {
        useContextNameAsTitle = b;
    }

    public void setDgElement(Element e) {
        dgElement = e;
        profile = SysMLExtensions.getInstance(e);
    }

    public String getTitlePrefix() {
        return titlePrefix;
    }

    public String getTitleSuffix() {
        return titleSuffix;
    }

    public boolean getUseContextNameAsTitle() {
        return useContextNameAsTitle;
    }

    public Element getDgElement() {
        return dgElement;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean getLoop() {
        return this.loop;
    }

    public void setProfile(SysMLExtensions profile) {
        this.profile = profile;
    }

    protected String toStringStart() {
        return getClass().getSimpleName() + "(";
    }

    protected String toStringEnd() {
        return ")";
    }

    @Override
    public String toString() {
        return toStringStart() + toStringEnd();
    }
}
