package org.openmbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import org.openmbee.mdk.SysMLExtensions;
import org.openmbee.mdk.docgen.docbook.DBColSpec;
import org.openmbee.mdk.docgen.docbook.DBTable;
import org.openmbee.mdk.util.GeneratorUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Table extends Query {

    protected boolean includeDoc;
    protected List<Property> stereotypeProperties;
    protected List<String> captions;
    protected boolean showCaptions;
    protected String style;
    protected List<String> colwidths;
    protected boolean transpose;
    protected boolean hideHeaders;
    protected boolean showIfEmpty;
    protected boolean excludeFromList;

    public void setIncludeDoc(boolean d) {
        includeDoc = d;
    }

    public boolean isIncludeDoc() {
        return includeDoc;
    }

    public void setStereotypeProperties(List<Property> p) {
        stereotypeProperties = p;
    }

    public void setCaptions(List<String> c) {
        captions = c;
    }

    public void setShowCaptions(boolean b) {
        showCaptions = b;
    }

    public Boolean isShowCaptions() {
        return showCaptions;
    }

    public List<String> getCaptions() {
        return captions;
    }

    public List<Property> getStereotypeProperties() {
        return stereotypeProperties;
    }

    public void setStyle(String s) {
        style = s;
    }

    public String getStyle() {
        return style;
    }

    public void setColwidths(List<String> colwidths) {
        this.colwidths = colwidths;
    }

    public List<String> getColwidths() {
        return colwidths;
    }

    public boolean isTranspose() {
        return transpose;
    }

    public void setTranspose(boolean transpose) {
        this.transpose = transpose;
    }

    public boolean isShowIfEmpty() {
        return showIfEmpty;
    }

    public void setShowIfEmpty(boolean showIfEmpty) {
        this.showIfEmpty = showIfEmpty;
    }

    public boolean isExcludeFromList() {
        return excludeFromList;
    }

    public void setExcludeFromList(boolean excludeFromList) {
        this.excludeFromList = excludeFromList;
    }

    public boolean isHideHeaders() {
        return hideHeaders;
    }

    public void setHideHeaders(final boolean hideHeaders) {
        this.hideHeaders = hideHeaders;
    }

    protected void setTableThings(DBTable dbTable) {
        String title = "";
        if (getTitles() != null && getTitles().size() > 0) {
            title = getTitles().get(0);
        }
        title = getTitlePrefix() + title + getTitleSuffix();
        dbTable.setTitle(title);

        if (getCaptions() != null && getCaptions().size() > 0 && isShowCaptions()) {
            dbTable.setCaption(getCaptions().get(0));
        }

        dbTable.setStyle(getStyle());

        List<DBColSpec> cslist = new ArrayList<DBColSpec>();
        if (getColwidths() != null && !getColwidths().isEmpty()) {
            int i = 1;
            for (String s : getColwidths()) {
                DBColSpec cs = new DBColSpec(i);
                cs.setColwidth(s);
                cslist.add(cs);
                i++;
            }
            dbTable.setColspecs(cslist);
        }
        dbTable.setTranspose(transpose);
        dbTable.setHideHeaders(hideHeaders);
        dbTable.setShowIfEmpty(showIfEmpty);
        dbTable.setExcludeFromList(excludeFromList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        SysMLExtensions.TableStereotype s = profile.table();
        setCaptions((List<String>) GeneratorUtils.getStereotypePropertyValue(dgElement, s.getCaptionsProperty(), new ArrayList<String>()));
        setShowCaptions((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, s.getShowCaptionsProperty(), true));
        setStereotypeProperties((List<Property>) GeneratorUtils.getStereotypePropertyValue(dgElement, s.getStereotypePropertiesProperty(), new ArrayList<Property>()));
        setIncludeDoc((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, s.getIncludeDocProperty(), false));
        setStyle((String) GeneratorUtils.getStereotypePropertyFirst(dgElement, s.getStyleProperty(), null));
        setColwidths((List<String>) GeneratorUtils.getStereotypePropertyValue(dgElement, s.getColwidthsProperty(), new ArrayList<String>()));
        setTranspose((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, s.getTransposeProperty(), false));
        setHideHeaders((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, s.getHideHeadersProperty(), false));
        setShowIfEmpty((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, s.getShowIfEmptyProperty(), false));
        setExcludeFromList((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, s.getExcludeFromListProperty(), false));
    }
}
