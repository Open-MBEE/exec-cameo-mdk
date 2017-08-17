package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBTable;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;

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
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        setCaptions((List<String>) GeneratorUtils.getStereotypePropertyValue(dgElement, DocGenProfile.hasCaptions,
                "captions", DocGenProfile.PROFILE_NAME, new ArrayList<String>()));
        setShowCaptions((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.hasCaptions,
                "showCaptions", DocGenProfile.PROFILE_NAME, true));
        setStereotypeProperties((List<Property>) GeneratorUtils
                .getStereotypePropertyValue(dgElement, DocGenProfile.stereotypePropertyChoosable,
                        "stereotypeProperties", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        setIncludeDoc((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.documentationChoosable, "includeDoc", DocGenProfile.PROFILE_NAME, false));
        setStyle((String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tableStereotype, "style",
                DocGenProfile.PROFILE_NAME, null));
        setColwidths((List<String>) GeneratorUtils.getStereotypePropertyValue(dgElement, DocGenProfile.tableStereotype,
                "colwidths", DocGenProfile.PROFILE_NAME, new ArrayList<String>()));
        setTranspose((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tableStereotype,
                "transpose", DocGenProfile.PROFILE_NAME, false));
        setHideHeaders((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tableStereotype,
                "hideHeaders", DocGenProfile.PROFILE_NAME, false));
        setShowIfEmpty((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tableStereotype,
                "showIfEmpty", DocGenProfile.PROFILE_NAME, false));
    }
}
