package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
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
		Common.addReferenceToDBHasContent(Reference.getPropertyReference(e, p), parent);
	}

	@Override
	public void initialize() {
		Boolean showTargets = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.bulletedListStereotype, "showTargets", false);
		Boolean showSPN = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.bulletedListStereotype, "showStereotypePropertyNames", false);
		Boolean ordered = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.bulletedListStereotype, "orderedList", false);
		setShowTargets(showTargets);
		setShowStereotypePropertyNames(showSPN);
		setOrderedList(ordered);
		setIncludeDoc((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.documentationChoosable, "includeDoc", false));
		setStereotypeProperties((List<Property>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties", new ArrayList<Property>()));
	}
	
	@Override
	public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
	    List<DocumentElement> res = new ArrayList<DocumentElement>();
		if (ignore)
			return res;
		if (targets != null && !targets.isEmpty()) {
			DBList l = new DBList();
			res.add(l);
			l.setOrdered(isOrderedList());
			List< Element > targets = isSortElementsByName() ? Utils.sortByName( getTargets() ): getTargets();
			if (isShowTargets() || isIncludeDoc()) {

				for (Element e: targets) {
					DBListItem li = new DBListItem();
					l.addElement(li);
					if (isShowTargets() && e instanceof NamedElement) {
						li.addElement(new DBParagraph(((NamedElement)e).getName(), e, From.NAME));
					}
					if (isIncludeDoc() && !ModelHelper.getComment(e).equals("")) {
						li.addElement(new DBParagraph(ModelHelper.getComment(e) ,e, From.DOCUMENTATION));
					}
					if (getStereotypeProperties() != null && !getStereotypeProperties().isEmpty()) {
						if (isShowStereotypePropertyNames()) {
							DBList l2 = new DBList();
							l2.setOrdered(isOrderedList());
							li.addElement(l2);
							for (Property p: getStereotypeProperties()) {
								DBListItem li2 = new DBListItem();
								l2.addElement(li2);
								li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
								DBList l3 = new DBList();
								l3.setOrdered(isOrderedList());
								li2.addElement(l3);
								addStereotypeProperties(l3, e, p);
							}
						} else {
							DBList l2 = new DBList();
							l2.setOrdered(isOrderedList());
							li.addElement(l2);
							for (Property p: getStereotypeProperties()) {
								addStereotypeProperties(l2, e, p);
							}
						}
					}
				}
			} else {
				for (Element e: targets) {
					if (getStereotypeProperties() != null && !getStereotypeProperties().isEmpty()) {
						if (isShowStereotypePropertyNames()) {
							for (Property p: getStereotypeProperties()) {
								DBListItem li2 = new DBListItem();
								li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
								l.addElement(li2);
								DBList l3 = new DBList();
								li2.addElement(l3);
								l3.setOrdered(isOrderedList());
								addStereotypeProperties(l3, e, p);
							}
						} else {
							for (Property p: getStereotypeProperties()) {
								addStereotypeProperties(l, e, p);
							}
						}
					}
				}
			}
		}
		return res;
	}
}
