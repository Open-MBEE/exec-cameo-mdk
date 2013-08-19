package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DiagramTableTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class GenericTable extends Table {

	private List<String> headers;
	private boolean skipIfNoDoc;
	
	@SuppressWarnings("unchecked")
	public List<List<DocumentElement>> getHeaders(Diagram d, List<String> columnIds, DiagramTableTool dtt) {
		List<List<DocumentElement>> res = new ArrayList<List<DocumentElement>>();
		if (this.headers != null && !this.headers.isEmpty()) {
			List<DocumentElement> row = new ArrayList<DocumentElement>();
			for (String h: this.headers)
				row.add(new DBText(h));
			res.add(row);
		} else if (StereotypesHelper.hasStereotype(d, DocGen3Profile.headersChoosable)) {
			List<DocumentElement> row = new ArrayList<DocumentElement>();
			for (String h: (List<String>)StereotypesHelper.getStereotypePropertyValue(d, DocGen3Profile.headersChoosable, "headers"))
				row.add(new DBText(h));
			res.add(row);
		} else {
			List<DocumentElement> row = new ArrayList<DocumentElement>();
			int count = 0;
			for (String s: dtt.getColumnNames(d, columnIds)) {
				if (count == 0) {
					count++;
					continue;
				}
				row.add(new DBText(s));
			}
			res.add(row);
		}
		return res;
		
	}
	
	public List<List<DocumentElement>> getBody(Diagram d, List<Element> rowElements, List<String> columnIds, DiagramTableTool dtt, boolean forViewEditor) {
		List<List<DocumentElement>> res = new ArrayList<List<DocumentElement>>();
		for (Element e: rowElements) {
			if (skipIfNoDoc && ModelHelper.getComment(e).trim().equals(""))
				continue;
			List<DocumentElement> row = new ArrayList<DocumentElement>();
			int count = 0;
			for (String cid: columnIds) {
				if (count == 0) {
					count++;
					continue;
				}
				row.add(Common.getTableEntryFromList(getTableValues(dtt.getCellValue(d, e, cid)), false, forViewEditor));
			}
			res.add(row);
		}
		return res;
	}
	
	@SuppressWarnings("rawtypes")
	public List<Object> getTableValues(Object o) {
    	List<Object> res = new ArrayList<Object>();
    	if (o instanceof Object[]) {
    		Object[] a = (Object[])o;
    		for (int i = 0; i < a.length; i++) {
    			res.addAll(getTableValues(a[i]));
    		}
    	} else if (o instanceof Collection) {
    		for (Object oo: (Collection)o)
    		res.addAll(getTableValues(oo));
    	}
    	else if (o != null)
    		res.add(o);
    	return res;
    }
	
	public void setSkipIfNoDoc(boolean b) {
		skipIfNoDoc = b;
	}
	
	public void setHeaders(List<String> h) {
		headers = h;
	}

	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}

	@Override
	public void initialize(ActivityNode an, List<Element> in) {
		setCaptions((List<String>)GeneratorUtils.getListProperty(an, DocGen3Profile.hasCaptions, "captions", new ArrayList<String>()));
		setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.hasCaptions, "showCaptions", true));
		setHeaders((List<String>)GeneratorUtils.getListProperty(an, DocGen3Profile.headersChoosable, "headers", new ArrayList<String>()));
		setSkipIfNoDoc((Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.docSkippable, "skipIfNoDoc", false));
		setStyle((String)GeneratorUtils.getObjectProperty(an, DocGen3Profile.tableStereotype, "style", null));
	}

	@Override
	public void parse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DocumentElement visit(boolean forViewEditor) {
		// TODO Auto-generated method stub
		return null;
	}
}
