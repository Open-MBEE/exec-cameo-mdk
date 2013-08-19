package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class BulletedList extends Table {
	private boolean orderedList;
	private boolean showTargets;
	private boolean showStereotypePropertyNames;
	
	public BulletedList() {
		orderedList = false;
		showTargets = true;
		showStereotypePropertyNames = true;
		setSortElementsByName(false);
	}
	
	public void setOrderedList(boolean b) {
		orderedList = b;
	}
	
	public void setShowTargets(boolean b) {
		showTargets = b;
	}
	
	public void setShowStereotypePropertyNames(boolean b) {
		showStereotypePropertyNames = b;
	}
	
	public boolean isOrderedList() {
		return orderedList;
	}

	public boolean isShowTargets() {
		return showTargets;
	}

	public boolean isShowStereotypePropertyNames() {
		return showStereotypePropertyNames;
	}

	public void addStereotypeProperties(DBHasContent parent, Element e, Property p) {
		List<Object> results = Utils.getStereotypePropertyValues(e, p);
		for (Object o: results) {
			if (o instanceof NamedElement)
				parent.addElement(new DBParagraph(((NamedElement)o).getName()));
			else if (o instanceof String)
				parent.addElement(new DBParagraph((String)o));
			else if (o instanceof Comment)
				parent.addElement(new DBParagraph(((Comment)o).getBody()));
			else
				parent.addElement(new DBParagraph(o.toString()));
		}
	}

	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
	}

	@Override
	public void initialize(ActivityNode an, List<Element> in) {
		Boolean showTargets = (Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.bulletedListStereotype, "showTargets", false);
		Boolean showSPN = (Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.bulletedListStereotype, "showStereotypePropertyNames", false);
		Boolean ordered = (Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.bulletedListStereotype, "orderedList", false);
		setShowTargets(showTargets);
		setShowStereotypePropertyNames(showSPN);
		setOrderedList(ordered);
		setIncludeDoc((Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.documentationChoosable, "includeDoc", false));
		setStereotypeProperties((List<Property>)GeneratorUtils.getListProperty(an, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties", new ArrayList<Property>()));
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
