package gov.nasa.jpl.mgss.mbee.docgen;

import java.util.ArrayList;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.FromProperty;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Image;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.List;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ListItem;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.MDEditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Paragraph;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Table;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.TableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.TableRow;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Text;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.util.DgviewSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.PropertyEnum;

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
	
	public DocumentElement caseColSpec(ColSpec object) {
		DBColSpec res = new DBColSpec();
		res.setColname(object.getColname());
    	res.setColnum(object.getColnum());
    	if (object.getColwidth() != null && !object.getColwidth().equals(""))
    		res.setColwidth(object.getColwidth());
    	return res;
	}
	
	public DocumentElement caseImage(Image object) {
		DBImage res = new DBImage();
		if (object.getCaption() != null && !object.getCaption().equals(""))
			res.setCaption(object.getCaption());
    	res.setDoNotShow(object.isDoNotShow());
    	res.setGennew(object.isGennew());
    	BaseElement i = Application.getInstance().getProject().getElementByID(object.getDiagramId());
    	if (i instanceof Diagram)
    		res.setImage((Diagram)i);
    	return res;
	}

	public DocumentElement caseList(List object) {
		DBList res = new DBList();
		for (ViewElement ve: object.getChildren()) {
			res.addElement(this.doSwitch(ve));
		}
		return res;
	}
	
	public DocumentElement caseListItem(ListItem object) {
		DBListItem res = new DBListItem();
		for (ViewElement ve: object.getChildren()) {
			res.addElement(this.doSwitch(ve));
		}
		return res;
	}
	
	public DocumentElement caseParagraph(Paragraph object) {
		DBParagraph res = new DBParagraph();
		res.setText(object.getText());
		return res;
	}
	
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
		}
		else
			res.setCols(object.getCols());
		if (object.getColspecs() != null) {
			java.util.List<DBColSpec> colspecs = new java.util.ArrayList<DBColSpec>();
			for (ColSpec cs: object.getColspecs())
				colspecs.add((DBColSpec)this.doSwitch(cs));
			res.setColspecs(colspecs);
		}
		return res;
	}
	
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
		return res;
	}
	
	public DocumentElement caseText(Text object) {
		DBText res = new DBText();
		res.setText(object.getText());
		return res;
	}

	public DocumentElement caseMDEditableTable(MDEditableTable object) {
		DBTable table = null;
		EditableTable et = convertEditableTable(object);
		DBTable ettable = null;
		boolean addline = object.isAddLineNum();
		if (object.getMergeCols() != null && !object.getMergeCols().isEmpty() && !forViewEditor) {
			ettable = Utils.getDBTableFromEditableTable(et, addline, object.getMergeCols().toArray((new Integer[1])));
		} else 
			ettable = Utils.getDBTableFromEditableTable(et, addline);
		table = (DBTable)this.caseTable(object);
		if (object.getBody() != null && !object.getBody().isEmpty()) {
			ettable.setBody(table.getBody());
			ettable.setCols(table.getCols());
		} 
		if (object.getHeaders() !=  null && !object.getHeaders().isEmpty())
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
			java.util.List<Object> row =  new ArrayList<Object>();
			java.util.List<PropertyEnum> rowe =  new ArrayList<PropertyEnum>();
			for (ViewElement ve: tr.getChildren()) {
				if (ve.getFromElementId() != null && !ve.getFromElementId().equals("")) {
					Element element = (Element)Application.getInstance().getProject().getElementByID(ve.getFromElementId());
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
	
		EditableTable et = new EditableTable(object.getTitle(), body, object.getGuiHeaders(), null, e, object.getPrecision());
		et.setEditableCol(object.getEditable());
		et.prepareTable();
		return et;
	}
}
