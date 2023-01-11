package gov.nasa.jpl.mbee.mdk.model;

import gov.nasa.jpl.mbee.mdk.model.docmeta.DocumentMeta;

import java.lang.reflect.Field;

public class Document extends Container {

    private DocumentMeta metadata;

    private String header;
    private String footer;
    private String subheader;
    private String subfooter;

    private boolean RemoveBlankPages;
    private boolean UseDefaultStylesheet;

    private boolean chunkFirstSections;
    private int chunkSectionDepth;
    private int tocSectionDepth;

    private boolean genNewImage;

    private boolean product;

    public boolean isProduct() {
        return product;
    }

    public void setProduct(boolean product) {
        this.product = product;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public boolean getChunkFirstSections() {
        return chunkFirstSections;
    }

    public void setChunkFirstSections(boolean chunkFirstSections) {
        this.chunkFirstSections = chunkFirstSections;
    }

    public int getChunkSectionDepth() {
        return chunkSectionDepth;
    }

    public void setChunkSectionDepth(int chunkSectionDepth) {
        this.chunkSectionDepth = chunkSectionDepth;
    }

    public int getTocSectionDepth() {
        return tocSectionDepth;
    }

    public void setTocSectionDepth(int tocSectionDepth) {
        this.tocSectionDepth = tocSectionDepth;
    }

    public boolean getUseDefaultStylesheet() {
        return UseDefaultStylesheet;
    }

    public boolean getRemoveBlankPages() {
        return RemoveBlankPages;
    }


    public void setRemoveBlankPages(Boolean s) {
        RemoveBlankPages = s == null ? false : s;
    }

    public void setUseDefaultStylesheet(boolean s) {
        UseDefaultStylesheet = s;
    }

    public Document() {
        chunkFirstSections = false;
        chunkSectionDepth = 20;
        tocSectionDepth = 20;
    }

    public boolean getGenNewImage() {
        return genNewImage;
    }

    public void setGenNewImage(boolean n) {
        genNewImage = n;
    }

    public DocumentMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(DocumentMeta metadata) {
        this.metadata = metadata;
    }

    @Override
    public void accept(IModelVisitor v) {
        v.visit(this);
    }

    @Override
    public String toStringStart() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toStringStart());
        for (Field f : getClass().getFields()) {
            if (f.getDeclaringClass().equals(getClass().getSuperclass())) {
                continue;
            }
            try {
                sb.append("," + f.getName() + "=" + f.get(this));
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
//        sb.append( super.toStringEnd() );
        return sb.toString();
    }

}
