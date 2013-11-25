package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public abstract class Table extends Query {

    protected boolean        includeDoc;
    protected List<Property> stereotypeProperties;
    protected List<String>   captions;
    protected boolean        showCaptions;
    protected String         style;
    protected List<String>   colwidths;

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

    protected void setTableThings(DBTable dbTable) {
        String title = "";
        if (getTitles() != null && getTitles().size() > 0)
            title = getTitles().get(0);
        title = getTitlePrefix() + title + getTitleSuffix();
        dbTable.setTitle(title);

        if (getCaptions() != null && getCaptions().size() > 0 && isShowCaptions())
            dbTable.setCaption(getCaptions().get(0));

        dbTable.setStyle(getStyle());

        List<DBColSpec> cslist = new ArrayList<DBColSpec>();
        if (getColwidths() != null && !getColwidths().isEmpty()) {
            int i = 1;
            for (String s: getColwidths()) {
                DBColSpec cs = new DBColSpec(i);
                cs.setColwidth(s);
                cslist.add(cs);
                i++;
            }
            dbTable.setColspecs(cslist);
        }
    }

    @Override
    public void initialize() {
        setCaptions((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.hasCaptions,
                "captions", new ArrayList<String>()));
        setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.hasCaptions,
                "showCaptions", true));
        setStereotypeProperties((List<Property>)GeneratorUtils
                .getListProperty(dgElement, DocGen3Profile.stereotypePropertyChoosable,
                        "stereotypeProperties", new ArrayList<Property>()));
        setIncludeDoc((Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.documentationChoosable, "includeDoc", false));
        setStyle((String)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.tableStereotype, "style",
                null));
        setColwidths((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.tableStereotype,
                "colwidths", new ArrayList<String>()));
    }
}
