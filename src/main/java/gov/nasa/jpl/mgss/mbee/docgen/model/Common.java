package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class Common {

	public static DBTableEntry getStereotypePropertyEntry(Element e, Property p, boolean forViewEditor) {
		List<Object> results = Utils.getStereotypePropertyValues(e, p);
		return getEntryFromList(results, false, forViewEditor);
	}
	
	public static DBTableEntry getEntryFromList(List<Object> results, boolean simple, boolean forViewEditor) {
		DBTableEntry entry = new DBTableEntry();
		if (simple) {
			DBSimpleList sl = new DBSimpleList();
			sl.setContent(results);
			entry.addElement(sl);
		} else {
			DBList parent = new DBList();
			for (Object o: results) {
				if (forViewEditor)
					parent.addElement(new DBText(DocGenUtils.fixString(o, false)));
				else
					parent.addElement(new DBText(DocGenUtils.addDocbook(DocGenUtils.fixString(o))));
			}
			entry.addElement(parent);
		}
		
		return entry;
		
	}
}
