package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import gov.nasa.jpl.mbee.mdk.docgen.table.EditableTable;
import gov.nasa.jpl.mbee.mdk.docgen.table.PropertiesTable;
import gov.nasa.jpl.mbee.mdk.model.actions.EditPropertiesTableAction;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Utils2;

import java.util.*;

public class PropertiesTableByAttributes extends HierarchicalPropertiesTable {

    private List<Stereotype> splitStereotype;
    private List<Stereotype> systemIncludeStereotype;
    private List<Stereotype> systemExcludeStereotype;
    private List<String> systemIncludeTypeName;
    private List<String> systemExcludeTypeName;
    private List<String> systemIncludeName;
    private List<String> systemExcludeName;
    private int systemAssociationType;
    private boolean consolidateTypes;
    private boolean showMultiplicity;

    public void setSplitStereotype(List<Stereotype> splitStereotype) {
        this.splitStereotype = splitStereotype;
    }

    public void setSystemIncludeStereotype(List<Stereotype> systemIncludeStereotype) {
        this.systemIncludeStereotype = systemIncludeStereotype;
    }

    public void setSystemExcludeStereotype(List<Stereotype> systemExcludeStereotype) {
        this.systemExcludeStereotype = systemExcludeStereotype;
    }

    public void setSystemIncludeTypeName(List<String> systemIncludeTypeName) {
        this.systemIncludeTypeName = systemIncludeTypeName;
    }

    public void setSystemExcludeTypeName(List<String> systemExcludeTypeName) {
        this.systemExcludeTypeName = systemExcludeTypeName;
    }

    public void setSystemIncludeName(List<String> systemIncludeName) {
        this.systemIncludeName = systemIncludeName;
    }

    public void setSystemExcludeName(List<String> systemExcludeName) {
        this.systemExcludeName = systemExcludeName;
    }

    public void setSystemAssociationType(int systemAssociationType) {
        this.systemAssociationType = systemAssociationType;
    }

    public void setConsolidateTypes(boolean consolidateTypes) {
        this.consolidateTypes = consolidateTypes;
    }

    public void setShowMultiplicity(boolean showMultiplicity) {
        this.showMultiplicity = showMultiplicity;
    }

    public void setDoRollup(boolean doRollup) {
    }

    public void setRollupProperty(List<String> rollupProperty) {
    }

    public EditableTable getEditableTable() {
        PropertiesTable ahh = new PropertiesTable(topIncludeStereotype, topExcludeStereotype, topIncludeName,
                topExcludeName, topIncludeTypeName, topExcludeTypeName, topOrder, systemIncludeStereotype,
                systemExcludeStereotype, systemIncludeName, systemExcludeName, systemIncludeTypeName,
                systemExcludeTypeName, splitStereotype, maxDepth, systemAssociationType, topAssociationType,
                Utils2.asList(this.targets, Element.class), includeInherited);
        ahh.doMainThings();
        return ahh.getEt();
    }

    /**
     * this should really leverage the editable table instead of creating the db
     * table from scratch need to check editable table to make sure it obeys all
     * options even then, still need to do some massaging to get the headers
     * right and to add in any extra columns like documentation, stereotype
     * properties, etc need to revisit once editable table can handle more
     * things
     */
    private List<DocumentElement> getDocumentElement() {
        List<DocumentElement> res1 = new ArrayList<DocumentElement>();
        if (this.ignore) {
            return res1;
        }
        List<List<Element>> elementsList = new ArrayList<List<Element>>();
        if (this.loop) {
            for (Object e : this.targets) {
                if (e instanceof Class) {
                    List<Element> blah = new ArrayList<Element>();
                    blah.add((Class) e);
                    elementsList.add(blah);
                }
            }
        }
        else {
            List<Element> blah = new ArrayList<Element>();
            for (Object e : this.targets) {
                if (e instanceof Class) {
                    blah.add((Class) e);
                }
            }
            elementsList.add(blah);
        }

        int tableCount = 0;

        for (List<Element> elements : elementsList) {
            PropertiesTable ahh = new PropertiesTable(topIncludeStereotype, topExcludeStereotype,
                    topIncludeName, topExcludeName, topIncludeTypeName, topExcludeTypeName, topOrder,
                    systemIncludeStereotype, systemExcludeStereotype, systemIncludeName, systemExcludeName,
                    systemIncludeTypeName, systemExcludeTypeName, splitStereotype, maxDepth,
                    systemAssociationType, topAssociationType, elements, includeInherited);
            ahh.doMainThings();
            List<String> colspecs = ahh.getColspecs();
            int headerSize = 1 + ahh.getNumPropertyHeaders() + this.stereotypeProperties.size();
            if (this.includeDoc) {
                headerSize++;
            }
            if (this.showMultiplicity) {
                headerSize++;
            }

            DBTable t = new DBTable();
            if (this.titles != null && this.titles.size() > tableCount) {
                t.setTitle(titlePrefix + this.titles.get(tableCount) + titleSuffix);
            }
            else if (!elements.isEmpty()) {
                t.setTitle(titlePrefix + ((Class) (elements.get(0))).getName() + titleSuffix);
            }
            if (this.captions != null && this.captions.size() > tableCount && showCaptions) {
                t.setCaption(this.captions.get(tableCount));
            }

            // set title!
            t.setCols(headerSize);
            List<DBColSpec> colSpecs = new ArrayList<DBColSpec>();
            int curcol = 2;
            if (this.includeDoc) {
                curcol = 3;
            }
            if (this.showMultiplicity) {
                curcol++;
            }
            for (String colspec : colspecs) {
                colSpecs.add(new DBColSpec(curcol, colspec));
                curcol++;
            }
            t.setColspecs(colSpecs);
            t.setHeaders(getTHead(ahh.getHeaders()));

            List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
            if (this.consolidateTypes) {
                for (Element c : elements) {
                    Map<Class, Integer> typeUnits = ahh.getConsolidated().get(c).get(c);
                    getHierarchyConsolidated((Class) c, 1, typeUnits, ahh.getConsolidated().get(c), body,
                            colspecs);
                }
            }
            else {
                for (Element c : elements) {
                    getHierarchy((Class) c, 1, ahh.getFilteredStructures().get(c), body, colspecs);
                }

            }
            t.setBody(body);
            res1.add(t);
            tableCount++;
        }

        return res1;

    }

    private void getDocAndProps(Class e, int numunit, List<DocumentElement> row, List<String> colspecs) {
        if (this.includeDoc) {
            row.add(new DBParagraph(ModelHelper.getComment(e)));
        }
        if (this.showMultiplicity) {
            row.add(new DBText(Integer.toString(numunit)));
        }
        for (String col : colspecs) {
            String value = PropertiesTable.getPropertyValue(e, Arrays.asList(col.split(";")),
                    includeInherited);
            if (this.floatingPrecision > 0) {
                row.add(new DBText(Utils.floatTruncate(value, this.floatingPrecision)));
            }
            else {
                row.add(new DBText(value));
            }
        }

    }

    private void getStereotypeProps(Element e, List<DocumentElement> row) {
        for (Property p : this.stereotypeProperties) {
            row.add(Common.getStereotypePropertyEntry(e, p, this));
        }
    }

    private void getHierarchyConsolidated(Class e, int curdepth, Map<Class, Integer> typeUnits,
                                          Map<Class, Map<Class, Integer>> consolidated, List<List<DocumentElement>> body,
                                          List<String> colspecs) {
        List<DocumentElement> row = new ArrayList<DocumentElement>();
        body.add(row);
        String name = DocGenUtils.getIndented(e.getName(), curdepth);
        if (curdepth == 1) {
            name += "<emphasis role=\"bold\">" + name + "</emphasis>";
        }
        row.add(new DBText(name));
        Integer numunit = typeUnits.get(e);
        if (numunit == null) {
            numunit = 1;
        }
        getDocAndProps(e, numunit, row, colspecs);
        getStereotypeProps(e, row);
        body.add(row);
        Set<Class> set = consolidated.get(e).keySet();
        List<Element> list = Utils.sortByName(Utils2.asList(set, Element.class));
        for (Element ee : list) {
            getHierarchyConsolidated((Class) ee, curdepth + 1, consolidated.get(e), consolidated, body,
                    colspecs);
        }

    }

    private void getHierarchy(NamedElement e, int curdepth, Map<Class, Map<Property, Class>> childMap,
                              List<List<DocumentElement>> body, List<String> colspecs) {
        List<DocumentElement> row = new ArrayList<DocumentElement>();

        Class type = null;
        if (e instanceof Property) {
            type = (Class) ((Property) e).getType();
        }
        else {
            type = (Class) e;
        }
        String name = DocGenUtils.getIndented(e.getName(), curdepth);
        if (this.showType && e instanceof Property && ((Property) e).getType() != null) {
            name += " (" + ((Property) e).getType().getName() + ")";
        }
        if (curdepth == 1) {
            name = "<emphasis role=\"bold\">" + name + "</emphasis>";
        }
        row.add(new DBText(name));
        Integer numunit = 1;
        if (e instanceof Property) {
            numunit = Utils.getMultiplicity((Property) e);
        }
        getDocAndProps(type, numunit, row, colspecs);
        getStereotypeProps(type, row);
        body.add(row);

        Set<Property> set = childMap.get(type).keySet();
        List<Element> list = Utils.sortByName(Utils2.asList(set, Element.class));
        for (Element ee : list) {
            getHierarchy((NamedElement) ee, curdepth + 1, childMap, body, colspecs);
        }
    }

    private List<List<DocumentElement>> getTHead(List<List<Map<String, String>>> headers) {
        List<List<DocumentElement>> res = new ArrayList<List<DocumentElement>>();
        int count = 1;
        int headerRowSize = headers.size();
        int moreRowsSize = headerRowSize - 1;
        for (List<Map<String, String>> row : headers) {
            List<DocumentElement> headerRow = new ArrayList<DocumentElement>();
            if (count == 1) {
                DBTableEntry entry = new DBTableEntry();
                if (moreRowsSize > 0) {
                    entry.setMorerows(moreRowsSize);
                }
                headerRow.add(entry);
            }
            if (count == 1 && this.includeDoc) {
                DBTableEntry entry = new DBTableEntry();
                if (moreRowsSize > 0) {
                    entry.setMorerows(moreRowsSize);
                }
                entry.addElement(new DBText("Description"));
                headerRow.add(entry);
            }
            if (count == 1 && this.showMultiplicity) {
                DBTableEntry entry = new DBTableEntry();
                if (moreRowsSize > 0) {
                    entry.setMorerows(moreRowsSize);
                }
                entry.addElement(new DBText("Multiplicity"));
                headerRow.add(entry);
            }
            for (Map<String, String> headerProp : row) {
                DBTableEntry entry = new DBTableEntry();
                if (headerProp.containsKey("morerows")) {
                    entry.setMorerows(Integer.parseInt(headerProp.get("morerows")));
                }
                if (headerProp.containsKey("namest")) {
                    entry.setNamest(headerProp.get("namest"));
                }
                if (headerProp.containsKey("nameend")) {
                    entry.setNameend(headerProp.get("nameend"));
                }
                String prop = headerProp.get("name");
                if (this.showType) {
                    prop += " (" + headerProp.get("type") + ")";
                }
                entry.addElement(new DBText(prop));
                headerRow.add(entry);
            }
            if (count == 1) {
                for (Property sp : this.stereotypeProperties) {
                    DBTableEntry entry = new DBTableEntry();
                    if (moreRowsSize > 0) {
                        entry.setMorerows(moreRowsSize);
                    }
                    String prop = sp.getName();
                    if (this.showType && sp.getType() != null) {
                        prop += " (" + sp.getType().getName() + ")";
                    }
                    entry.addElement(new DBText(prop));
                    headerRow.add(entry);
                }
            }
            res.add(headerRow);
            count++;
        }
        if (headers.isEmpty()) {
            List<DocumentElement> headerRow = new ArrayList<DocumentElement>();
            headerRow.add(new DBText(""));
            if (this.includeDoc) {
                headerRow.add(new DBText("Description"));
            }
            if (this.showMultiplicity) {
                headerRow.add(new DBText("Multiplicity"));
            }
            for (Property sp : this.stereotypeProperties) {
                String prop = sp.getName();
                if (this.showType && sp.getType() != null) {
                    prop += " (" + sp.getType().getName() + ")";
                }
                headerRow.add(new DBText(prop));
            }
            res.add(headerRow);
        }
        return res;
    }

    public List<Stereotype> getSplitStereotype() {
        return splitStereotype;
    }

    public List<Stereotype> getSystemIncludeStereotype() {
        return systemIncludeStereotype;
    }

    public List<Stereotype> getSystemExcludeStereotype() {
        return systemExcludeStereotype;
    }

    public List<String> getSystemIncludeTypeName() {
        return systemIncludeTypeName;
    }

    public List<String> getSystemExcludeTypeName() {
        return systemExcludeTypeName;
    }

    public List<String> getSystemIncludeName() {
        return systemIncludeName;
    }

    public List<String> getSystemExcludeName() {
        return systemExcludeName;
    }

    public int getSystemAssociationType() {
        return systemAssociationType;
    }

    public boolean isConsolidateTypes() {
        return consolidateTypes;
    }

    public boolean isShowMultiplicity() {
        return showMultiplicity;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore()) {
            return res;
        }
        if (forViewEditor) {
            EditableTable et = getEditableTable();
            DBTable dtable = Utils.getDBTableFromEditableTable(et, true);
            dtable.setStyle(getStyle());
            res.add(dtable);
        }
        else {
            List<DocumentElement> results = getDocumentElement();
            for (DocumentElement de : results) {
                if (de instanceof DBTable) {
                    ((DBTable) de).setStyle(getStyle());
                }
            }
            res.addAll(results);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();

        List<Stereotype> splitStereotype = (List<Stereotype>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                DocGenProfile.propertiesTableByAttributesStereotype, "splitStereotype",
                DocGenProfile.PROFILE_NAME, new ArrayList<Stereotype>());
        List<Stereotype> systemIncludeStereotype = (List<Stereotype>) GeneratorUtils.getStereotypePropertyValue(
                dgElement, DocGenProfile.propertiesTableByAttributesStereotype, "systemIncludeStereotype",
                DocGenProfile.PROFILE_NAME, new ArrayList<Stereotype>());
        List<Stereotype> systemExcludeStereotype = (List<Stereotype>) GeneratorUtils.getStereotypePropertyValue(
                dgElement, DocGenProfile.propertiesTableByAttributesStereotype, "systemExcludeStereotype",
                DocGenProfile.PROFILE_NAME, new ArrayList<Stereotype>());
        List<String> systemIncludeTypeName = DocGenUtils
                .getElementNames((Collection<NamedElement>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                        DocGenProfile.propertiesTableByAttributesStereotype, "systemIncludeTypeName",
                        DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        List<String> systemExcludeTypeName = DocGenUtils
                .getElementNames((Collection<NamedElement>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                        DocGenProfile.propertiesTableByAttributesStereotype, "systemExcludeTypeName",
                        DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        List<String> systemIncludeName = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getStereotypePropertyValue(dgElement, DocGenProfile.propertiesTableByAttributesStereotype,
                        "systemIncludeName", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        List<String> systemExcludeName = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getStereotypePropertyValue(dgElement, DocGenProfile.propertiesTableByAttributesStereotype,
                        "systemExcludeName", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        Integer systemAssociationType = (Integer) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.propertiesTableByAttributesStereotype, "systemAssociationType", DocGenProfile.PROFILE_NAME, 0);
        Boolean consolidateTypes = (Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.propertiesTableByAttributesStereotype, "consolidateTypes", DocGenProfile.PROFILE_NAME, false);
        Boolean showMultiplicity = (Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.propertiesTableByAttributesStereotype, "showMultiplicity", DocGenProfile.PROFILE_NAME, false);
        Boolean doRollup = (Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.propertiesTableByAttributesStereotype, "doRollup", DocGenProfile.PROFILE_NAME, false);
        List<String> rollupProperty = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getStereotypePropertyValue(dgElement, DocGenProfile.propertiesTableByAttributesStereotype,
                        "rollupProperty", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));

        setSplitStereotype(splitStereotype);
        setSystemIncludeStereotype(systemIncludeStereotype);
        setSystemExcludeStereotype(systemExcludeStereotype);
        setSystemIncludeName(systemIncludeName);
        setSystemExcludeName(systemExcludeName);
        setSystemIncludeTypeName(systemIncludeTypeName);
        setSystemExcludeTypeName(systemExcludeTypeName);
        setSystemAssociationType(systemAssociationType);
        setConsolidateTypes(consolidateTypes);
        setShowMultiplicity(showMultiplicity);
        setDoRollup(doRollup);
        setRollupProperty(rollupProperty);
    }

    @Override
    public List<MDAction> getActions() {
        List<MDAction> res = new ArrayList<MDAction>();
        res.add(new EditPropertiesTableAction(this));
        return res;
    }

}
