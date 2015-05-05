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
package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.dgview.ColSpec;
import gov.nasa.jpl.mbee.dgview.FromProperty;
import gov.nasa.jpl.mbee.dgview.Image;
import gov.nasa.jpl.mbee.dgview.List;
import gov.nasa.jpl.mbee.dgview.ListItem;
import gov.nasa.jpl.mbee.dgview.MDEditableTable;
import gov.nasa.jpl.mbee.dgview.Paragraph;
import gov.nasa.jpl.mbee.dgview.Table;
import gov.nasa.jpl.mbee.dgview.TableEntry;
import gov.nasa.jpl.mbee.dgview.TableRow;
import gov.nasa.jpl.mbee.dgview.Text;
import gov.nasa.jpl.mbee.dgview.ViewElement;
import gov.nasa.jpl.mbee.dgview.util.DgviewSwitch;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.PropertyEnum;

import java.util.ArrayList;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DgviewDBSwitch extends DgviewSwitch<DocumentElement> {

    private boolean forViewEditor;

    public DgviewDBSwitch(boolean vieweditor) {
        super();
        forViewEditor = vieweditor;
    }

    public DgviewDBSwitch() {
        super();
        forViewEditor = false;
    }

    private void setVe(ViewElement ve, DocumentElement de) {
        if (ve.getFromElementId() != null && !ve.getFromElementId().equals("")) {
            de.setFrom((Element)Application.getInstance().getProject().getElementByID(ve.getFromElementId()));
            if (ve.getFromProperty() != null) {
                if (ve.getFromProperty() == FromProperty.NAME)
                    de.setFromProperty(From.NAME);
                else if (ve.getFromProperty() == FromProperty.DOCUMENTATION)
                    de.setFromProperty(From.DOCUMENTATION);
                else
                    de.setFromProperty(From.DVALUE);
            }
        }
        de.setId(ve.getId());
        de.setTitle(ve.getTitle());
    }
    
    @Override
    public DocumentElement caseColSpec(ColSpec object) {
        DBColSpec res = new DBColSpec();
        res.setColname(object.getColname());
        res.setColnum(object.getColnum());
        if (object.getColwidth() != null && !object.getColwidth().equals(""))
            res.setColwidth(object.getColwidth());
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseImage(Image object) {
        DBImage res = new DBImage();
        if (object.getCaption() != null && !object.getCaption().equals(""))
            res.setCaption(object.getCaption());
        res.setDoNotShow(object.isDoNotShow());
        res.setGennew(object.isGennew());
        BaseElement i = Application.getInstance().getProject().getElementByID(object.getDiagramId());
        if (i instanceof Diagram)
            res.setImage((Diagram)i);
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseList(List object) {
        DBList res = new DBList();
        for (ViewElement ve: object.getChildren()) {
            res.addElement(this.doSwitch(ve));
        }
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseListItem(ListItem object) {
        DBListItem res = new DBListItem();
        for (ViewElement ve: object.getChildren()) {
            res.addElement(this.doSwitch(ve));
        }
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseParagraph(Paragraph object) {
        DBParagraph res = new DBParagraph();
        res.setText(object.getText());
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseTable(Table object) {
        DBTable res = new DBTable();
        java.util.List<java.util.List<DocumentElement>> headers = new java.util.ArrayList<java.util.List<DocumentElement>>();
        java.util.List<java.util.List<DocumentElement>> body = new java.util.ArrayList<java.util.List<DocumentElement>>();
        if (object.getBody() != null) {
            for (TableRow row: object.getBody()) {
                java.util.List<DocumentElement> newrow = new java.util.ArrayList<DocumentElement>();
                body.add(newrow);
                for (ViewElement ve: row.getChildren()) {
                    newrow.add(this.doSwitch(ve));
                }
            }
        }
        if (object.getHeaders() != null) {
            for (TableRow row: object.getHeaders()) {
                java.util.List<DocumentElement> newrow = new java.util.ArrayList<DocumentElement>();
                headers.add(newrow);
                for (ViewElement ve: row.getChildren()) {
                    newrow.add(this.doSwitch(ve));
                }
            }
        }
        res.setBody(body);
        res.setHeaders(headers);
        res.setCaption(object.getCaption());
        if (object.getCols() == 0) {
            int max = 0;
            for (java.util.List<DocumentElement> row: body) {
                if (row.size() > max)
                    max = row.size();
            }
            res.setCols(max);
        } else
            res.setCols(object.getCols());
        if (object.getColspecs() != null) {
            java.util.List<DBColSpec> colspecs = new java.util.ArrayList<DBColSpec>();
            for (ColSpec cs: object.getColspecs())
                colspecs.add((DBColSpec)this.doSwitch(cs));
            res.setColspecs(colspecs);
        }
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseTableEntry(TableEntry object) {
        DBTableEntry res = new DBTableEntry();
        if (object.getMorerows() != 0)
            res.setMorerows(object.getMorerows());
        if (object.getNameend() != null && !object.getNameend().equals(""))
            res.setNameend(object.getNameend());
        if (object.getNamest() != null && !object.getNamest().equals(""))
            res.setNamest(object.getNamest());
        for (ViewElement ve: object.getChildren()) {
            res.addElement(this.doSwitch(ve));
        }
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseText(Text object) {
        DBText res = new DBText();
        res.setText(object.getText());
        setVe(object, res);
        return res;
    }

    @Override
    public DocumentElement caseMDEditableTable(MDEditableTable object) {
        DBTable table = null;
        EditableTable et = convertEditableTable(object);
        DBTable ettable = null;
        boolean addline = object.isAddLineNum();
        if (object.getMergeCols() != null && !object.getMergeCols().isEmpty() && !forViewEditor) {
            ettable = Utils.getDBTableFromEditableTable(et, addline,
                    object.getMergeCols().toArray((new Integer[1])));
        } else
            ettable = Utils.getDBTableFromEditableTable(et, addline);
        table = (DBTable)this.caseTable(object);
        if (object.getBody() != null && !object.getBody().isEmpty()) {
            ettable.setBody(table.getBody());
            ettable.setCols(table.getCols());
        }
        if (object.getHeaders() != null && !object.getHeaders().isEmpty())
            ettable.setHeaders(table.getHeaders());
        ettable.setCaption(table.getCaption());
        ettable.setColspecs(table.getColspecs());
        ettable.setTitle(table.getTitle());
        ettable.setStyle(table.getStyle());
        ettable.setId(table.getId());
        return ettable;
    }

    public static EditableTable convertEditableTable(MDEditableTable object) {
        java.util.List<java.util.List<Object>> body = new ArrayList<java.util.List<Object>>();
        java.util.List<java.util.List<PropertyEnum>> e = new ArrayList<java.util.List<PropertyEnum>>();

        for (TableRow tr: object.getGuiBody()) {
            java.util.List<Object> row = new ArrayList<Object>();
            java.util.List<PropertyEnum> rowe = new ArrayList<PropertyEnum>();
            for (ViewElement ve: tr.getChildren()) {
                if (ve.getFromElementId() != null && !ve.getFromElementId().equals("")) {
                    Element element = (Element)Application.getInstance().getProject()
                            .getElementByID(ve.getFromElementId());
                    row.add(element);
                    if (ve.getFromProperty() == FromProperty.DOCUMENTATION)
                        rowe.add(PropertyEnum.DOC);
                    else if (ve.getFromProperty() == FromProperty.NAME)
                        rowe.add(PropertyEnum.NAME);
                    else
                        rowe.add(PropertyEnum.VALUE);
                } else {
                    if (ve instanceof Paragraph)
                        row.add(((Paragraph)ve).getText());
                    else if (ve instanceof Text)
                        row.add(((Text)ve).getText());
                    else
                        row.add("cannot be rendered");
                    rowe.add(PropertyEnum.NAME);
                }
            }
            body.add(row);
            e.add(rowe);
        }

        EditableTable et = new EditableTable(object.getTitle(), body, object.getGuiHeaders(), null, e,
                object.getPrecision());
        et.setEditableCol(object.getEditable());
        et.prepareTable();
        return et;
    }
}
