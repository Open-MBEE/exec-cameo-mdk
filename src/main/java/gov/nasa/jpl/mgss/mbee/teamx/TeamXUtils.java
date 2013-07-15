package gov.nasa.jpl.mgss.mbee.teamx;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;

/**
 * teamx once wanted some powerpoint generation using velocity and very specific model structure
 * have no idea what these are now, bjorn was the one who led it, it was more like a proof of concept
 * use with TeamXDocTemplate.pptx in svn
 * @author dlam
 *
 */
public class TeamXUtils {

	public static String getElementText(org.jsoup.nodes.Element e, Comment c) {
		 String s = HtmlUtils.getElementText(e);
		 String temp = new String(s);
		 List<Integer> refs = new ArrayList<Integer>();
		 while (true) {
			 int i = temp.indexOf('[');
			 if (i > 0) {
				 int j = temp.indexOf(']', i);
				 if (j > 0) {
					 String num = temp.substring(i + 1, j);
					 try {
						 refs.add(Integer.parseInt(num));
						 temp = temp.substring(j);
						 continue;
					 } catch (Exception ex) {
						 temp = temp.substring(j);
						 continue;
					 }
				 } else
					 break;
			 } else
				 break;
		 }
		
		 List<String> refValues = getReferencedValues(c, refs);
		 for (int i = 0; i < refs.size(); i++) {
			 s = s.replaceFirst("\\[" + refs.get(i) + "\\]", refValues.get(i));
		 }

		 return s;
	}
	
	private static List<String> getReferencedValues(Comment c, List<Integer> refs) {
		List<String> res = new ArrayList<String>();
		Property firstprop = null;
		for (Element e: c.getAnnotatedElement()) {
			if (e instanceof Property) {
				firstprop = (Property)e;
				break;
			}
		}
		for (Integer i: refs) {
			res.add(getReferencedValue(firstprop, i, 1));
		}
		while (res.size() < refs.size())
			res.add("");
		return res;
	}
	
	private static String getReferencedValue(Property first, int i, int cur) {
		if (cur == i) {
			String type = (first.getType() == null || isNative(first.getType()) ? "" : first.getType().getName());
			if (type.equals(""))
				return first.getDefault();
			return first.getDefault() + " " + type;
		}
		for (DirectedRelationship dr: first.get_directedRelationshipOfSource()) {
			if (dr instanceof Dependency && ModelHelper.getSupplierElement(dr) instanceof Property)
				return getReferencedValue((Property)ModelHelper.getSupplierElement(dr), i, cur + 1);
		}
		return "";
	}
	
	private static boolean isNative(Type t) {
		if (!StereotypesHelper.hasStereotypeOrDerived(t, "ValueType"))
			return true;
		String name = t.getName();
		if (name.equals("Integer") || name.equals("Boolean") || name.equals("Real") || name.equals("Complex") || name.equals("String") || name.equals("UnlimitedNatural"))
			return true;
		return false;
	}
	
	public static List<Element> getTeamXOrder(Collection<Package> packages) {
		//printToLog("getTeamXOrder: " + packages.size());
		List<Element> res = new ArrayList<Element>();
		if (packages.size() != 0) {
			
		
			Package first = findChairPackage(packages.iterator().next());
			if (first != null) {
				List<Package> chairPackages = findChairPackages(first);
				for (Package chairp: chairPackages)
					res.addAll(getChairOrder(chairp));
			}
		}
		return res;
	}
	
	public static String getSystem(Element e) {
		if (e.getOwner() instanceof Package)
			return ((Package)e.getOwner()).getName();
		return "";
	}
	
	public static String getSubtitle(Element e) {
		String title = "";
		if (e instanceof Diagram)
			title += ((Diagram) e).getName();
		else if (e instanceof Comment) {
			if (StereotypesHelper.getFirstVisibleStereotype(e) != null)
				title += StereotypesHelper.getFirstVisibleStereotype(e).getName();
			List<Class> options = getOptions((Comment)e);
			if (options.isEmpty())
				title += " - Option 1";
			else
				title += " - ";
			for (int i = 0; i < options.size(); i++) {
				title += options.get(i).getName();
				if (i < options.size() -1)
					title += ", ";
			}
		}
		return title;
	}
	
	private static List<Class> getOptions(Comment e) {
		List<Class> res = new ArrayList<Class>();
		for (Element annotated: e.getAnnotatedElement()) {
			if (annotated instanceof Class && StereotypesHelper.hasStereotypeOrDerived(annotated, "Option")) {
				res.add((Class)annotated);
			}
		}
		return res;
	}
	
	private static List<Element> getChairOrder(Package p) {
		//printToLog("in getChairOrder: " + p.getQualifiedName());
		List<Element> res = new ArrayList<Element>();
		res.addAll(getComments(p));
		res.addAll(getDiagrams(p));
		return res;
	}
	
	private static List<Element> getDiagrams(Package p) {
		//printToLog("in getDiagrams: " + p.getQualifiedName());
		List<Element> res = new ArrayList<Element>();
		for (Element e: p.getOwnedDiagram()) {
			if (StereotypesHelper.hasStereotypeOrDerived(e, "ignore"))
				continue;
			res.add(e);
		}
		return res;
	}
	
	private static List<Element> getComments(Package p) {
		//printToLog("in getComments: " + p.getQualifiedName());
		List<Element> res = new ArrayList<Element>();
		Comment first = getFirstComment(p);
		boolean found = false;
		if (first != null)
			found = true;
		
		while (found) {
			found = false;
			res.add(first);
			for (Element e: first.getAnnotatedElement()) {
				if (e instanceof Comment) {
					first = (Comment)e;
					found = true;
					break;
				}
			}
		}
		return res;
	}
	
	private static Comment getFirstComment(Package p) {
		//printToLog("in getFirstComment: " + p.getQualifiedName()); 
		Comment acomment = null;
		for (Element e: p.getOwnedElement()) {
			if (e instanceof Comment && StereotypesHelper.hasStereotypeOrDerived(e, "Generic")) {
				acomment = (Comment)e;
				break;
			}
		}
		boolean found = false;
		if (acomment != null)
			found = true;
		while (found) {
			found = false;
			for (Comment c: acomment.get_commentOfAnnotatedElement()) {
				acomment = c;
				found = true;
				break;
			}
		}
		return acomment;
	}
	
	private static List<Package> findChairPackages(Package p) {
		//printToLog("in findChairPackages: " + p.getQualifiedName());
		List<Package> res = new ArrayList<Package>();
		boolean found = false;
		Package first = findFirstPackage(p);
		if (first != null)
			found = true;
		while (found) {
			res.add(first);
			found = false;
			for (DirectedRelationship dr: first.get_directedRelationshipOfSource()) {
				Element next = ModelHelper.getSupplierElement(dr);
				if (dr instanceof Dependency && next instanceof Package && StereotypesHelper.hasStereotypeOrDerived(next, "chair package")) {
					first = (Package)next;
					found = true;
					break;
				}
			}
		}
		return res;
	}
	
	private static Package findFirstPackage(Package p) {
		//printToLog("in findFirstPackage: " + p.getQualifiedName());
		for (DirectedRelationship dr: p.get_directedRelationshipOfTarget()) {
			Element source = ModelHelper.getClientElement(dr);
			if (dr instanceof Dependency && source instanceof Package && StereotypesHelper.hasStereotypeOrDerived(source, "chair package"))
				return findFirstPackage((Package)source);
		}
		return p;
	}
	
	private static Package findChairPackage(Package p) {
		//printToLog("in findChairPackage: " + p.getQualifiedName());
		if (StereotypesHelper.hasStereotypeOrDerived(p, "chair package"))
			return p;
		for (Package nested: p.getNestedPackage()) {
			Package find = findChairPackage(nested);
			if (find != null)
				return find;
		}
		return null;
	}

}
