package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import gov.nasa.jpl.mbee.mdk.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.util.ArrayList;
import java.util.List;

public class CombinedMatrix extends Table {

    private List<String> headers;
    private List<Stereotype> outgoing;
    private List<Stereotype> incoming;
    private boolean skipIfNoDoc;
    private int nameColumn = 1;
    private int docColumn = 2;

    public CombinedMatrix() {
        setSortElementsByName(true);
    }

    public void setSkipIfNoDoc(boolean b) {
        skipIfNoDoc = b;
    }

    public void setHeaders(List<String> d) {
        headers = d;
    }

    public void setOutgoing(List<Stereotype> s) {
        outgoing = s;
    }

    public void setIncoming(List<Stereotype> s) {
        incoming = s;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<Stereotype> getOutgoing() {
        return outgoing;
    }

    public List<Stereotype> getIncoming() {
        return incoming;
    }

    public boolean isSkipIfNoDoc() {
        return skipIfNoDoc;
    }

    public int getNameColumn() {
        return nameColumn;
    }

    public void setNameColumn(int nameColumn) {
        this.nameColumn = nameColumn;
    }

    public int getDocColumn() {
        return docColumn;
    }

    public void setDocColumn(int docColumn) {
        this.docColumn = docColumn;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore()) {
            return res;
        }
        DBTable dbTable = new DBTable();
        List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
        if (!getHeaders().isEmpty()) {
            List<DocumentElement> first = new ArrayList<DocumentElement>();
            hs.add(first);
            for (String h : getHeaders()) {
                first.add(new DBText(h));
            }
            dbTable.setCols(first.size());
        }
        else {
            List<DocumentElement> first = new ArrayList<DocumentElement>();
            hs.add(first);
            /*
             * first.add(new DBText("Name")); if (isIncludeDoc()) first.add(new
             * DBText("Description"));
             */
            for (Property p : getStereotypeProperties()) {
                first.add(new DBText(p.getName()));
            }
            for (Stereotype s : getOutgoing()) {
                first.add(new DBText(s.getName()));
            }
            for (Stereotype s : getIncoming()) {
                first.add(new DBText(s.getName()));
            }
            if (getNameColumn() < getDocColumn()) {
                first.add(getNameColumn() - 1, new DBText("Name"));
                if (isIncludeDoc()) {
                    first.add(getDocColumn() - 1, new DBText("Description"));
                }
            }
            else {
                if (isIncludeDoc()) {
                    first.add(getDocColumn() - 1, new DBText("Description"));
                }
                first.add(getNameColumn() - 1, new DBText("Name"));
            }
            dbTable.setCols(first.size());
        }
        dbTable.setHeaders(hs);

        List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
        List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
        for (Object o : targets) {
            Element e = o instanceof Element ? (Element) o : null;
            if (isSkipIfNoDoc() && (e == null || ModelHelper.getComment(e).trim().isEmpty())) {
                continue;
            }
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            if (e == null) {
                continue;
            }
            for (Property p : getStereotypeProperties()) {
                row.add(Common.getStereotypePropertyEntry(e, p, this));
            }
            for (Stereotype s : getOutgoing()) {
                List<Element> blah = new ArrayList<Element>();
                blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 1, true, 1));
                row.add(Common.getTableEntryFromObject(blah));
            }
            for (Stereotype s : getIncoming()) {
                List<Element> blah = new ArrayList<Element>();
                blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 2, true, 1));
                row.add(Common.getTableEntryFromObject(blah));
            }
            DocumentElement name = null;
            DocumentElement doc = null;
            if (e instanceof NamedElement) {
                if (!forViewEditor) {
                    name = new DBText(DocGenUtils.addInvisibleSpace(DocGenUtils.fixString(((NamedElement) e)
                            .getName())));
                }
                else {
                    name = new DBParagraph(((NamedElement) e).getName(), e, From.NAME);
                }
            }
            else {
                name = new DBParagraph(e.getHumanName());
            }
            doc = new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION);
            if (getNameColumn() < getDocColumn()) {
                row.add(getNameColumn() - 1, name);
                if (isIncludeDoc()) {
                    row.add(getDocColumn() - 1, doc);
                }
            }
            else {
                if (isIncludeDoc()) {
                    row.add(getDocColumn() - 1, doc);
                }
                row.add(getNameColumn() - 1, name);
            }
            body.add(row);
        }
        dbTable.setBody(body);
        if (colwidths != null && colwidths.isEmpty()) {
            colwidths.add(".4*");
        }
        setTableThings(dbTable);
        res.add(dbTable);
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        // TODO Auto-generated method stub
        Integer nameColumn = (Integer) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.combinedMatrixStereotype, "nameColumn", 1);
        Integer docColumn = (Integer) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.combinedMatrixStereotype, "docColumn", 2);
        nameColumn = nameColumn < 1 ? 1 : nameColumn;
        docColumn = docColumn < 1 ? 2 : docColumn;
        setHeaders((List<String>) GeneratorUtils.getListProperty(dgElement, DocGenProfile.headersChoosable,
                "headers", new ArrayList<String>()));
        setOutgoing((List<Stereotype>) GeneratorUtils.getListProperty(dgElement,
                DocGenProfile.stereotypedRelChoosable, "outgoingStereotypedRelationships",
                new ArrayList<Stereotype>()));
        setIncoming((List<Stereotype>) GeneratorUtils.getListProperty(dgElement,
                DocGenProfile.stereotypedRelChoosable, "incomingStereotypedRelationships",
                new ArrayList<Stereotype>()));
        setSkipIfNoDoc((Boolean) GeneratorUtils.getObjectProperty(dgElement, DocGenProfile.docSkippable,
                "skipIfNoDoc", false));
        setNameColumn(nameColumn);
        setDocColumn(docColumn);
    }

}
