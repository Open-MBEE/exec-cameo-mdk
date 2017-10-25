package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import gov.nasa.jpl.mbee.mdk.model.docmeta.DocumentMeta;

/**
 * If you find an occasion where you want to use this, let me know.
 *
 * @author dlam
 */
public class DBBook extends DBHasContent {

    private String subtitle;

    private boolean removeBlankPages;
    private boolean useDefaultStylesheet;

    private DocumentMeta metadata;

    public DocumentMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(DocumentMeta metadata) {
        this.metadata = metadata;
    }

    public DBBook() {
        subtitle = "";
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean getRemoveBlankPages() {
        return removeBlankPages;
    }

    public boolean getUseDefaultStylesheet() {
        return useDefaultStylesheet;
    }

    public void setSubtitle(String s) {
        subtitle = s;
    }

    public void setRemoveBlankPages(Boolean s) {
        removeBlankPages = s;
    }

    public void setUseDefaultStylesheet(boolean s) {
        useDefaultStylesheet = s;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
}
