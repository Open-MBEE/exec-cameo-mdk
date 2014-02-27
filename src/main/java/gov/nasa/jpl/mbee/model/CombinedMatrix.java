/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class CombinedMatrix extends Table {

    private List<String>     headers;
    private List<Stereotype> outgoing;
    private List<Stereotype> incoming;
    private boolean          skipIfNoDoc;
    private int              nameColumn = 1;
    private int              docColumn  = 2;

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
        if (getIgnore())
            return res;
        DBTable dbTable = new DBTable();
        List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
        if (!getHeaders().isEmpty()) {
            List<DocumentElement> first = new ArrayList<DocumentElement>();
            hs.add(first);
            for (String h: getHeaders())
                first.add(new DBText(h));
            dbTable.setCols(first.size());
        } else {
            List<DocumentElement> first = new ArrayList<DocumentElement>();
            hs.add(first);
            /*
             * first.add(new DBText("Name")); if (isIncludeDoc()) first.add(new
             * DBText("Description"));
             */
            for (Property p: getStereotypeProperties())
                first.add(new DBText(p.getName()));
            for (Stereotype s: getOutgoing())
                first.add(new DBText(s.getName()));
            for (Stereotype s: getIncoming())
                first.add(new DBText(s.getName()));
            if (getNameColumn() < getDocColumn()) {
                first.add(getNameColumn() - 1, new DBText("Name"));
                if (isIncludeDoc())
                    first.add(getDocColumn() - 1, new DBText("Description"));
            } else {
                if (isIncludeDoc())
                    first.add(getDocColumn() - 1, new DBText("Description"));
                first.add(getNameColumn() - 1, new DBText("Name"));
            }
            dbTable.setCols(first.size());
        }
        dbTable.setHeaders(hs);

        List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
        List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
        for (Object o: targets) {
            Element e = o instanceof Element ? (Element)o : null;
            if (isSkipIfNoDoc() && (e == null || ModelHelper.getComment(e).trim().equals("")))
                continue;
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            if ( e == null ) {
                continue;
            }
            for (Property p: getStereotypeProperties())
                row.add(Common.getStereotypePropertyEntry(e, p));
            for (Stereotype s: getOutgoing()) {
                List<Element> blah = new ArrayList<Element>();
                blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 1, true, 1));
                row.add(Common.getTableEntryFromObject(blah));
            }
            for (Stereotype s: getIncoming()) {
                List<Element> blah = new ArrayList<Element>();
                blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 2, true, 1));
                row.add(Common.getTableEntryFromObject(blah));
            }
            DocumentElement name = null;
            DocumentElement doc = null;
            if (e instanceof NamedElement) {
                if (!forViewEditor)
                    name = new DBText(DocGenUtils.addInvisibleSpace(DocGenUtils.fixString(((NamedElement)e)
                            .getName())));
                else
                    name = new DBParagraph(((NamedElement)e).getName(), e, From.NAME);
            } else
                name = new DBParagraph(e.getHumanName());
            doc = new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION);
            if (getNameColumn() < getDocColumn()) {
                row.add(getNameColumn() - 1, name);
                if (isIncludeDoc())
                    row.add(getDocColumn() - 1, doc);
            } else {
                if (isIncludeDoc())
                    row.add(getDocColumn() - 1, doc);
                row.add(getNameColumn() - 1, name);
            }
            body.add(row);
        }
        dbTable.setBody(body);
        if (colwidths != null && colwidths.isEmpty())
            colwidths.add(".4*");
        setTableThings(dbTable);
        res.add(dbTable);
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        // TODO Auto-generated method stub
        Integer nameColumn = (Integer)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.combinedMatrixStereotype, "nameColumn", 1);
        Integer docColumn = (Integer)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.combinedMatrixStereotype, "docColumn", 2);
        nameColumn = nameColumn < 1 ? 1 : nameColumn;
        docColumn = docColumn < 1 ? 2 : docColumn;
        setHeaders((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.headersChoosable,
                "headers", new ArrayList<String>()));
        setOutgoing((List<Stereotype>)GeneratorUtils.getListProperty(dgElement,
                DocGen3Profile.stereotypedRelChoosable, "outgoingStereotypedRelationships",
                new ArrayList<Stereotype>()));
        setIncoming((List<Stereotype>)GeneratorUtils.getListProperty(dgElement,
                DocGen3Profile.stereotypedRelChoosable, "incomingStereotypedRelationships",
                new ArrayList<Stereotype>()));
        setSkipIfNoDoc((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.docSkippable,
                "skipIfNoDoc", false));
        setNameColumn(nameColumn);
        setDocColumn(docColumn);
    }

}
