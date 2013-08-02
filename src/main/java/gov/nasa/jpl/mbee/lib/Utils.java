package gov.nasa.jpl.mbee.lib;

import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTableModel;
import gov.nasa.jpl.mgss.mbee.docgen.table.PropertyEnum;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementDlg;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.SelectElementsDlg;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.validation.RuleViolationResult;
import com.nomagic.magicdraw.validation.ValidationResultProvider;
import com.nomagic.magicdraw.validation.ValidationRunData;
import com.nomagic.magicdraw.validation.ui.ValidationResultsWindowManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallOperationAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Operation;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

/**
 * This class contains utility methods for collection and filtering on magicdraw elements.<br/>
 * All collection and filter methods will return an empty list if there's nothing matching the criteria.<br/>
 * For filter*** methods, if the criteria collection is empty, then the include flag will 
 * determine whether you get an empty list or a list containing all the source elements.<br/>
 * Element collections passed in will never be modified or returned. All methods return new collections.
 * 
 * For methods that change the model in some way, no check are performed on whether element is editable or not.
 * Sessions are also not created. The caller will need to manage sessions themselves and wrap any call in a session.
 * @author dlam
 * Changelog: 
 * displayValidationWindow(Collection<ValidationSuite> vss) edited, 02/14/2013. Peter Di Pasquale
 */
public class Utils {
	private Utils() {}
	
	/**
	 * if multiplicity is not a range, returns the number
	 * if upper limit is infinity and lower limit > 0, returns lower
	 * else returns 1
	 * @param p
	 * @return
	 */
	public static int getMultiplicity(Property p) {
		int lower = p.getLower();
		int upper = p.getUpper();
		if (lower == upper)
			return lower;
		if (upper == -1 && lower > 0)
			return lower;
		return 1;
	}
	
	
	/**
	 * This does not change the passed in elements collection, it returns a new list
	 * Compared to just using Set, this preserves the order of the passed in collection
	 * @param elements
	 * @return
	 */
	public static List<Element> removeDuplicates(Collection<Element> elements) {
		List<Element> res = new ArrayList<Element>();
		for (Element e: elements) {
			if (!res.contains(e))
				res.add(e);
		}
		return res;
	}
	
	/**
	 * like it says
	 * @param diagrams can be a list of any model element, only diagrams will be tested and returned
	 * @param types this needs to be a list of the diagram type names. This usually shows up when you hover over the new diagram icon in magicdraw
	 * @param include whether to include the diagram type or not
	 * @return
	 */
	public static List<Diagram> filterDiagramsByDiagramTypes(Collection<Element> diagrams, List<String> types, boolean include) {
		List<Diagram> res = new ArrayList<Diagram>();
		for (Element d: diagrams) {
			if (!(d instanceof Diagram))
				continue;
			DiagramPresentationElement dpe = Application.getInstance().getProject().getDiagram((Diagram)d);
			if (types.contains(dpe.getDiagramType().getType())) {
				if (include)
					res.add((Diagram)d);
			} else if (!include)
				res.add((Diagram)d);
		}
		return res;
	}
	
	public static List<Element> filterElementsByStereotype(Collection<Element> elements, Stereotype stereotype, boolean include, boolean derived) {
		List<Element> res = new ArrayList<Element>();
		if (include) {
    		for (Element e: elements) {
    			if (derived && StereotypesHelper.hasStereotypeOrDerived(e, stereotype) || !derived && StereotypesHelper.hasStereotype(e, stereotype)) {
    				res.add(e);
    			}
    		}
    	} else {
    		for (Element e: elements) {
    			if (derived && !StereotypesHelper.hasStereotypeOrDerived(e, stereotype) || !derived && !StereotypesHelper.hasStereotype(e, stereotype)) {
    				res.add(e);
    			}
    		}
    	}
    	return res;
	}
	
	public static List<Element> filterElementsByStereotypeString(Collection<Element> elements, String stereotype, boolean include, boolean derived) {
		List<Element> res = new ArrayList<Element>();
		if (include) {
    		for (Element e: elements) {
    			if (derived && StereotypesHelper.hasStereotypeOrDerived(e, stereotype) || !derived && StereotypesHelper.hasStereotype(e, stereotype)) {
    				res.add(e);
    			}
    		}
    	} else {
    		for (Element e: elements) {
    			if (derived && !StereotypesHelper.hasStereotypeOrDerived(e, stereotype) || !derived && !StereotypesHelper.hasStereotype(e, stereotype)) {
    				res.add(e);
    			}
    		}
    	}
    	return res;
	}
	
	/**
	 * 
	 * @param elements
	 * @param stereotypes
	 * @param include whether to include or exclude elements with given stereotypes
	 * @param derived whether to consider derived stereotypes
	 * @return
	 */
    public static List<Element> filterElementsByStereotypes(Collection<Element> elements, Collection<Stereotype> stereotypes, boolean include, boolean derived) {
    	List<Element> res = new ArrayList<Element>();
    	if (stereotypes.isEmpty() && include)
    		return res;
    	if (stereotypes.isEmpty() && !include) {
    		res.addAll(elements);
    		return res;
    	}
    	if (include) {
    		for (Element e: elements) {
    			if (derived && StereotypesHelper.hasStereotypeOrDerived(e, stereotypes) || !derived && StereotypesHelper.hasStereotype(e, stereotypes)) {
    				res.add(e);
    			}
    		}
    	} else {
    		for (Element e: elements) {
    			if (derived && !StereotypesHelper.hasStereotypeOrDerived(e, stereotypes) || !derived && !StereotypesHelper.hasStereotype(e, stereotypes)) {
    				res.add(e);
    			}
    		}
    	}
    	return res;
    }
    
    /**
     * 
     * @param elements this can be a collection of any model element, only NamedElement will be tested and returned
     * @param names
     * @param include
     * @return
     */
    public static List<Element> filterElementsByNames(Collection<Element> elements, Collection<String> names, boolean include) {
    	List<Element> res = new ArrayList<Element>();
    	if (names.isEmpty() && include)
    		return res;
    	if (names.isEmpty() && !include) {
    		res.addAll(elements);
    		return res;
    	} 	
    	if (include) {
    		for (Element e: elements) {
    			if (e instanceof NamedElement && names.contains(((NamedElement)e).getName()))
    				res.add(e);
    		}
    	} else {
    		for (Element e: elements) {
    			if (e instanceof NamedElement && !names.contains(((NamedElement)e).getName()))
    				res.add(e);
    		}
    	}
    	return res;
    }
    
    /**
     * matches name of elements against a collect of regular expression strings, see java.util.regex.Pattern for regex patterns
     * @param elements
     * @param regex
     * @param include
     * @return
     */
    public static List<Element> filterElementsByNameRegex(Collection<Element> elements, Collection<String> regex, boolean include) {
    	List<Element> res = new ArrayList<Element>();
    	if (regex.isEmpty() && include)
    		return res;
    	if (regex.isEmpty() && !include) {
    		res.addAll(elements);
    		return res;
    	} 	
    	List<Pattern> patterns = new ArrayList<Pattern>();
    	for (String s: regex) {
    		patterns.add(Pattern.compile(s));
    	}
    	for (Element e: elements) {
    		boolean matched = false;
    		if (e instanceof NamedElement) {
    			String name = ((NamedElement)e).getName();
    			for (Pattern p: patterns) {
    				if (p.matcher(name).matches()) {
    					matched = true;
    					break;
    				}
    			}	
    			if ((include && matched) || (!include && !matched))
    				res.add(e);
    		}
    	}
    	return res;
    }
    
    /**
     * 
     * @param elements
     * @param javaClasses
     * @param include
     * @return
     */
    public static List<Element> filterElementsByJavaClasses(Collection<Element> elements, Collection<java.lang.Class<?>> javaClasses, boolean include) { 
    	List<Element> res = new ArrayList<Element>();
    	if (javaClasses.isEmpty() && !include) {
    		res.addAll(elements);
    		return res;
    	}
    	if (javaClasses.isEmpty() && include)
    		return res;

    	if (include) {
    		for (Element e: elements) {
    			for (java.lang.Class<?> c: javaClasses) {
    				if (c.isInstance(e)) {
    					res.add(e);
    					break;
    				}
    			}
    		}
    	} else {
    		for (Element e: elements) {
    			boolean add = true;
    			for (java.lang.Class<?> c: javaClasses) {
    				if (c.isInstance(e)) {
    					add = false;
    					break;
    				}
    			}
    			if (add)
    				res.add(e);
    		}
    	}
    	return res;
    }
    
    public static List<Element> filterElementsByJavaClass(Collection<Element> elements, java.lang.Class<?> javaClass, boolean include) {
    	List<Element> res = new ArrayList<Element>();
    	if (include) {
    		for (Element e: elements) {
    			if (javaClass.isInstance(e))
    				res.add(e);
    		}
    	} else {
    		for (Element e: elements) {
    			if (javaClass.isInstance(e))
    				continue;
    			res.add(e);
    		}
    	}
    	return res;
    }
    
    /**
     * 
     * @param elements
     * @param metaclass this is the Class model element from magicdraw's uml profile
     * @param include
     * @return
     */
    public static List<Element> filterElementsByMetaclass(Collection<Element> elements, Class metaclass, boolean include) {
    	java.lang.Class<?> javaClass = StereotypesHelper.getClassOfMetaClass(metaclass);
    	return filterElementsByJavaClass(elements, javaClass, include);
    }
    
    /**
     * 
     * @param elements
     * @param metaclasses these are the metaclass element classes from magicdraw's uml profile
     * @param include whether to include elements with given metaclasses or not, this will always include derived
     * @return
     */
    public static List<Element> filterElementsByMetaclasses(Collection<Element> elements, Collection<Class> metaclasses, boolean include) { 
    	List<java.lang.Class<?>> javaClasses = new ArrayList<java.lang.Class<?>>();
    	for (Class c: metaclasses)
    		javaClasses.add(StereotypesHelper.getClassOfMetaClass(c));
    	return filterElementsByJavaClasses(elements, javaClasses, include);
    }
    
    /**
     * collect the owner of the element recursively up to depth (intermediate owners will be returned also)
     * @param e
     * @param depth
     * @return
     */
    public static List<Element> collectOwners(Element e, int depth) {
    	List<Element> res = new ArrayList<Element>();
    	collectRecursiveOwners(e, res, depth, 1);
    	return res;
    }
    
    private static void collectRecursiveOwners(Element e, List<Element> all, int depth, int current) {
    	if (depth != 0 && current > depth)
    		return;
    	Element owner = e.getOwner();
    	if (owner != null) {
    		all.add(owner);
    		collectRecursiveOwners(owner, all, depth, current+1);
    	}
    }
    /**
     * Collects ownedElements recursively, returned collection will not include source
     * @param e
     * @param depth collect to what level of depth - 0 is infinite
     * @return
     */
    public static List<Element> collectOwnedElements(Element e, int depth) {
    	List<Element> res = new ArrayList<Element>();
    	collectRecursiveOwnedElements(e, res, depth, 1);
    	return res;
    }
    
    private static void collectRecursiveOwnedElements(Element e, List<Element> all, int depth, int current) {
    	if (depth != 0 && current > depth)
    		return;
        Collection<Element> owned = e.getOwnedElement();
        for (Element o:owned) {
        	//if (e instanceof NamedElement && ((NamedElement)e).getName().startsWith("base_"))
        		//continue;
        	all.add(o);
        	collectRecursiveOwnedElements(o, all, depth, current+1);
        }       
    }
    
    /**
     * @param e needs to be a Classifier, else empty list will be returned
     * @param depth
     * @param kind
     * @return
     */
    public static List<Element> collectAssociatedElements(Element e, int depth, AggregationKind kind) {
    	List<Element> res = new ArrayList<Element>();
    	if (e instanceof Classifier)
    		collectRecursiveAssociatedElements((Classifier)e, res, depth, 1, kind);
    	return res;
    }
    
    private static void collectRecursiveAssociatedElements(Classifier e, List<Element> all, int depth, int current, AggregationKind kind) {
    	if (depth != 0 && current > depth)
    		return;
        Collection<Property> owned = e.getAttribute();
        for (Property o:owned) {
        	if (o.getAggregation() != kind)
        		continue;
        	if (o.getType() == null)
        		continue;
        	all.add(o.getType());
        	if (o.getType() instanceof Classifier)
        		collectRecursiveAssociatedElements((Classifier)o.getType(), all, depth, current+1, kind);
        }       
    }
    
    /**
     * This will consider all relationships that are also specializations of javaClasses
     * @param e
     * @param javaClasses this is the class of the relationships to consider
     * @param direction 0 is both, 1 is outward, 2 is inward
     * @param depth
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipJavaClasses(Element e, Collection<java.lang.Class<?>> javaClasses, int direction, int depth) {
    	List<Element> res = new ArrayList<Element>();
    	collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(e, javaClasses, direction, depth, 1, res);
    	return res;
    }
    
    private static void collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(Element e, Collection<java.lang.Class<?>> javaClasses, int direction, int depth, int curdepth, List<Element> res) {
    	if (depth != 0 && curdepth > depth)
    		return;
    	if (direction == 0 || direction == 1) {
    		for (DirectedRelationship dr: e.get_directedRelationshipOfSource()) {
    			for (java.lang.Class<?> c: javaClasses) {
    				if (c.isInstance(dr)) {
    					if (!res.contains(ModelHelper.getSupplierElement(dr))) {
        					res.add(ModelHelper.getSupplierElement(dr));
        					collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(ModelHelper.getSupplierElement(dr), javaClasses, direction, depth, curdepth+1, res);
        					break;
    					}
    				}
    			}
    		}
    	}
    	if (direction == 0 || direction == 2) {
    		for (DirectedRelationship dr: e.get_directedRelationshipOfTarget()) {
    			for (java.lang.Class<?> c: javaClasses) {
    				if (c.isInstance(dr)) {
    					if (!res.contains(ModelHelper.getClientElement(dr))) {
        					res.add(ModelHelper.getClientElement(dr));
        					collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(ModelHelper.getClientElement(dr), javaClasses, direction, depth, curdepth+1, res);
        					break;
    					}
    				}
    			}
    		}
    	}
    }
    
    /**
     * 
     * @param e
     * @param c
     * @param direction 0 is both, 1 is outward, 2 is inward
     * @param depth
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipJavaClass(Element e, java.lang.Class<?> c, int direction, int depth) {
    	List<java.lang.Class<?>> classes = new ArrayList<java.lang.Class<?>>();
    	classes.add(c);
    	return collectDirectedRelatedElementsByRelationshipJavaClasses(e, classes, direction, depth);
    }
    
    /**
     * 
     * @param e
     * @param c this is the class from magicdraw's uml profile
     * @param direction 0 is both, 1 is outward, 2 is inward
     * @param depth
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipMetaclass(Element e, Class c, int direction, int depth) {
    	java.lang.Class<?> java = StereotypesHelper.getClassOfMetaClass(c);
    	return collectDirectedRelatedElementsByRelationshipJavaClass(e, java, direction, depth);
    }
    /**
     * 
     * @param e
     * @param metaclasses these are the metaclass element classes from magicdraw's uml profile, this will always considered derived relationship metaclasses
     * @param direction 0 means both, 1 means e is the client, 2 means e is the supplier
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipMetaclasses(Element e, Collection<Class> metaclasses, int direction, int depth) {
    	List<java.lang.Class<?>> javaClasses = new ArrayList<java.lang.Class<?>>();
    	for (Class c: metaclasses)
    		javaClasses.add(StereotypesHelper.getClassOfMetaClass(c));
    	return collectDirectedRelatedElementsByRelationshipJavaClasses(e, javaClasses, direction, depth);
    }
    
    /**
     * This collects all elements related by any kind of directed relationship
     * @param e
     * @param direction 0 means both, 1 means e is the client, 2 means e is the supplier
     * @return
     */
    public static List<Element> collectDirectedRelatedElements(Element e, int direction) {
    	List<Element> res = new ArrayList<Element>();
    	if (direction == 0 || direction == 1)
    		for (DirectedRelationship dr: e.get_directedRelationshipOfSource())
    			if (!res.contains(ModelHelper.getSupplierElement(dr)))
    				res.add(ModelHelper.getSupplierElement(dr));
    	if (direction == 0 || direction == 2)
    		for (DirectedRelationship dr: e.get_directedRelationshipOfTarget())
    			if (!res.contains(ModelHelper.getClientElement(dr)))
    				res.add(ModelHelper.getClientElement(dr));
    	return res;
    }
    
    /**
     * 
     * @param e
     * @param stereotypes
     * @param direction 0 means both, 1 means e is the client, 2 means e is the supplier
     * @param derived whether to consider derived stereotypes
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipStereotypes(Element e, Collection<Stereotype> stereotypes, int direction, boolean derived, int depth) {
    	//client: 0 is both, 1 is client, 2 is supplier
    	List<Element> res = new ArrayList<Element>();
    	collectDirectedRelatedElementsByRelationshipStereotypesRecursive(e, stereotypes, direction, derived, depth, 1, res);
    	return res;
    }
    
    private static void collectDirectedRelatedElementsByRelationshipStereotypesRecursive(Element e, Collection<Stereotype> stereotypes, int direction, boolean derived, int depth, int curdepth, List<Element> res) {
    	if (depth != 0 && curdepth > depth)
    		return;
    	//client: 0 is both, 1 is client, 2 is supplier
    	if (direction == 0 || direction == 1) {
    		for (DirectedRelationship dr: e.get_directedRelationshipOfSource()) {
    			if (derived && StereotypesHelper.hasStereotypeOrDerived(dr, stereotypes) || !derived && StereotypesHelper.hasStereotype(dr, stereotypes)) {
    				if (!res.contains(ModelHelper.getSupplierElement(dr))) {
    					res.add(ModelHelper.getSupplierElement(dr));
    					collectDirectedRelatedElementsByRelationshipStereotypesRecursive(ModelHelper.getSupplierElement(dr), stereotypes, direction, derived, depth, curdepth+1, res);
    				}
    			}
    		}
    	}
    	if (direction == 0 || direction == 2) {
    		for (DirectedRelationship dr: e.get_directedRelationshipOfTarget()) {
    			if (derived && StereotypesHelper.hasStereotypeOrDerived(dr, stereotypes) || !derived && StereotypesHelper.hasStereotype(dr, stereotypes)) {
    				if (!res.contains(ModelHelper.getClientElement(dr))) {
    					res.add(ModelHelper.getClientElement(dr));
    					collectDirectedRelatedElementsByRelationshipStereotypesRecursive(ModelHelper.getClientElement(dr), stereotypes, direction, derived, depth, curdepth+1, res);
    				}
    			}
    		}
    	}
    }
    
    /**
     * 
     * @param e
     * @param stereotype
     * @param direction direction 0 means both, 1 means e is the client, 2 means e is the supplier
     * @param derived
     * @param depth
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipStereotype(Element e, Stereotype stereotype, int direction, boolean derived, int depth) {
    	List<Stereotype> s = new ArrayList<Stereotype>();
    	s.add(stereotype);
    	return collectDirectedRelatedElementsByRelationshipStereotypes(e, s, direction, derived, depth);
    }
    
    /**
     * 
     * @param e
     * @param stereotype
     * @param direction direction 0 means both, 1 means e is the client, 2 means e is the supplier
     * @param derived
     * @param depth
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipStereotypeString(Element e, String stereotype, int direction, boolean derived, int depth) {
    	Stereotype s = StereotypesHelper.getStereotype(Application.getInstance().getProject(), stereotype);
    	if (s != null)
    		return collectDirectedRelatedElementsByRelationshipStereotype(e, s, direction, derived, depth);
    	return new ArrayList<Element>();
    }
    
    public static List<Element> intersectionOfCollections(Collection<Element> ... a) {
        List<Element> i = new ArrayList<Element>();
        Set<Element> inter = new HashSet<Element>(a[0]);
        for (Collection<Element> es: a) {
        	inter.retainAll(es);
        }
        i.addAll(inter);
        return i;
    }
    
    public static List<Element> unionOfCollections(Collection<Element> ... a) {
    	List<Element> i = new ArrayList<Element>();
    	Set<Element> union = new HashSet<Element>(a[0]);
    	for (Collection<Element> c: a)
    		union.addAll(c);
    	i.addAll(union);
       	return i;
    }
    
    public static List<Element> intersectionOfCollections(Collection<? extends Collection<Element>> a) {
        List<Element> i = new ArrayList<Element>();
        Set<Element> inter = new HashSet<Element>(a.iterator().next());
        for (Collection<Element> es: a) {
        	inter.retainAll(es);
        }
        i.addAll(inter);
        return i;
    }
    
    public static List<Element> unionOfCollections(Collection<? extends Collection<Element>> a) {
    	List<Element> i = new ArrayList<Element>();
    	Set<Element> union = new HashSet<Element>(a.iterator().next());
    	for (Collection<Element> c: a)
    		union.addAll(c);
    	i.addAll(union);
       	return i;
    }
 
    /**
     * returns elements not shared
     * @param a
     * @param b
     * @return
     */
    public static List<Element> xorOfCollections(Collection<? extends Collection<Element>> cc) {
    	if (cc.size() > 1) {
    		Iterator<? extends Collection<Element>> i = cc.iterator();
    		Set<Element> a = new HashSet<Element>(i.next());
    		Set<Element> b = new HashSet<Element>(i.next());
    		Set<Element> c = new HashSet<Element>();
    		c.addAll(a);
    		c.addAll(b);
    		a.retainAll(b);
    		c.removeAll(a);
    		return new ArrayList<Element>(c);
    	}
    	return new ArrayList<Element>();
    }
    
    /**
     * returns all directed relationships where client element is source and supplier element is target
     * @param source
     * @param target
     * @return
     */
    public static List<DirectedRelationship> findDirectedRelationshipsBetween(Element source, Element target) {
    	List<DirectedRelationship> res = new ArrayList<DirectedRelationship>();
    	for (DirectedRelationship dr: source.get_directedRelationshipOfSource()) {
    		if (ModelHelper.getSupplierElement(dr) == target)
    			res.add(dr);
    	}
    	return res;
    }
    
    /**
     * Displays a dialog box that allows users to select elements from the containment tree<br/>
     * 17.0.2 seems to have a new nicer dialog box, gotta find it...
     * @param types this should be a list of java.lang.Class types (ex. Package from com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package) This constraints what the user can select
     * @param title title of the dialog box
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<BaseElement> getUserSelections(List<java.lang.Class<?>> types, String title) {
        SelectElementTypes a = new SelectElementTypes(null,types);
        //SelectElementType(display, select)
        SelectElementInfo b = new SelectElementInfo(false, false, Application.getInstance().getProject().getModel(), true);
        SelectElementsDlg z = null;
        z = new SelectElementsDlg(MDDialogParentProvider.getProvider().getDialogParent(), a, b, false, false, null);
        z.setTitle(title);
        z.setVisible(true);
        if (z.isOk())
            return (List<BaseElement>) z.getSelected();
        return null;
    }
    
    /**
     * 
     * @param types
     * @param title title of the dialog box
     * @return
     * @see getUserSelections
     */
    public static BaseElement getUserSelection(List<java.lang.Class<?>> types, String title) {
        SelectElementTypes a = new SelectElementTypes(null,types);
        //SelectElementType(display, select)
        SelectElementInfo b = new SelectElementInfo(false, false, Application.getInstance().getProject().getModel(), true);
        SelectElementDlg z = null;
        z = new SelectElementDlg(MDDialogParentProvider.getProvider().getDialogParent(), null, a, b);
        z.setTitle(title);
        z.setVisible(true);
        if (z.isOk())
        	return z.getSelected();
        return null;
    }
    
    /**
     * Given a collection of stuff, joins their string representation into one string using delimiter
     * @param s
     * @param delimiter
     * @return
     */
    public static String join(Collection<?> s, String delimiter) {
    	StringBuilder builder = new StringBuilder();
    	Iterator<?> iter = s.iterator();
    	while (iter.hasNext()) {
    		builder.append(iter.next());
    		if (!iter.hasNext())
    			break;
    		builder.append(delimiter);
    	}
    	return builder.toString();
    }
    
    /**
     * Given a list of named elements, will prompt the user to choose one and return it (selections are showne as qualified names, null if nothing chosen
     * @param title
     * @param message
     * @param elements
     * @return
     */
    public static Element getUserDropdownSelection(String title, String message, List<NamedElement> elements) {
    	String[] strings = new String[elements.size()];
    	int i = 0;
    	for (NamedElement e: elements) {
    		strings[i] = e.getQualifiedName();
    		i++;
    	}
    	Object input = JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, null, strings, null);
    	if (input != null) {
    		for (int j = 0; j < strings.length; j++) {
    			if (((String)input).equals(strings[j]))
    				return elements.get(j);
    		}
    	}
    	return null;
    }
    
    /**
     * This is similar to getUserDropdownSelection but you provide the string of what to display to user
     * @param title
     * @param message
     * @param elements what to return based on what user selected
     * @param displays what to display to user (the size of this must match the size of elements argument)
     * @return
     */
    public static String getUserDropdownSelectionForString(String title, String message, List<String> elements, List<String> displays, String initial) {
    	String[] strings = new String[elements.size()];
    	int i = 0;
    	for (String e: displays) {
    		strings[i] = e;
    		i++;
    	}
    	Object input = JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, null, strings, initial);
    	if (input != null) {
    		for (int j = 0; j < strings.length; j++) {
    			if (((String)input).equals(strings[j]))
    				return elements.get(j);
    		}
    	}
    	return null;
    }
    
    public static String getUserDropdownSelectionForString(String title, String message, List<String> elements, List<String> displays) {
    	return getUserDropdownSelectionForString(title, message, elements, displays, null);
    }
    
    /**
     * 
     * @param question
     * @return null if user hits cancel
     */
    public static Boolean getUserYesNoAnswer(String question) {
    	int res = JOptionPane.showConfirmDialog(null, question);
    	if (res == JOptionPane.YES_OPTION)
    		return true;
    	if (res == JOptionPane.NO_OPTION)
    		return false;
    	return null;
    }
    
    /**
     * return names of a collection of named elements
     * @param elements
     * @return
     */
    public static List<String> getElementNames(Collection<NamedElement> elements) {
    	List<String> names = new ArrayList<String>();
    	for (NamedElement e: elements) {
    		names.add(e.getName());
    	} 
    	return names;
    }
    
    /**
     * returns collection of model elements that's on the diagram
     * @param diagram
     * @return
     */
    public static Collection<Element> getElementsOnDiagram(Diagram diagram) {
    	return Project.getProject(diagram).getDiagram(diagram).getUsedModelElements(true);
    }
    
    /**
     * Copies all stereotypes of element a to element b if b doesn't already have it (including derived)
     * @param a
     * @param b
     */
    public static void copyStereotypes(Element a, Element b) {
    	for (Stereotype s: StereotypesHelper.getStereotypes(a))
    		if (!StereotypesHelper.hasStereotypeOrDerived(b, s))
    			StereotypesHelper.addStereotype(b, s);
    }
    
    /**
     * check if there's any directed relationship between 2 elements
     * @param from
     * @param to
     * @return
     */
    public static boolean hasDirectedRelationship(Element from, Element to) {
    	for (DirectedRelationship dr: from.get_directedRelationshipOfSource())
    		if (ModelHelper.getSupplierElement(dr) == to)
    			return true;
    	return false;
    }
    
    /**
     * Creates a generalization relationship between parent and child 
     * @param parent
     * @param child
     */
    public static void createGeneralization(Classifier parent, Classifier child) {
    	Generalization g = Project.getProject(parent).getElementsFactory().createGeneralizationInstance();
    	ModelHelper.setClientElement(g, child);
    	ModelHelper.setSupplierElement(g, parent);
    	g.setOwner(child);
    }
    
    public static void createDependency(Element from, Element to) {
    	Dependency d = Project.getProject(from).getElementsFactory().createDependencyInstance();
    	ModelHelper.setClientElement(d, from);
    	ModelHelper.setSupplierElement(d, to);
    	d.setOwner(from);
    }
    
    public static void createDependencyWithStereotype(Element from, Element to, Stereotype s) {
    	Dependency d = Project.getProject(from).getElementsFactory().createDependencyInstance();
    	ModelHelper.setClientElement(d, from);
    	ModelHelper.setSupplierElement(d, to);
    	StereotypesHelper.addStereotype(d, s);
    	d.setOwner(from);
    }
    
    public static void createDependencyWithStereotypes(Element from, Element to, Collection<Stereotype> s) {
    	Dependency d = Project.getProject(from).getElementsFactory().createDependencyInstance();
    	ModelHelper.setClientElement(d, from);
    	ModelHelper.setSupplierElement(d, to);
    	StereotypesHelper.addStereotypes(d, s);
    	d.setOwner(from);
    }
    
    public static void createDependencyWithStereotypeName(Element from, Element to, String stereotype) {
    	Dependency d = Project.getProject(from).getElementsFactory().createDependencyInstance();
    	ModelHelper.setClientElement(d, from);
    	ModelHelper.setSupplierElement(d, to);
    	StereotypesHelper.addStereotypeByString(d, stereotype);
    	d.setOwner(from);
    }
    
    public static void createDependencyWithStereotypeNames(Element from, Element to, Collection<String> stereotypes) {
    	Dependency d = Project.getProject(from).getElementsFactory().createDependencyInstance();
    	ModelHelper.setClientElement(d, from);
    	ModelHelper.setSupplierElement(d, to);
    	StereotypesHelper.addStereotypesWithNames(d, stereotypes);
    	d.setOwner(from);
    }
    /**
     * Sorts e by name, returns a new list with name ordered elements
     * @param e
     * @return
     */
    public static List<Element> sortByName(Collection<? extends Element> e) {
    	List<Element> n = new ArrayList<Element>(e);
    	Collections.sort(n, new Comparator<Element>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Element o1, Element o2) {
				if (o1 instanceof NamedElement && o2 instanceof NamedElement) {
					return ((NamedElement)o1).compareTo((NamedElement)o2);
				}
				return 0;
			}
    	});
    	return n;
    }
    
    /**
     * Get the things that have t has the type
     * @param t
     * @return
     */
    public static List<TypedElement> getPropertiesWithType(Type t) {
    	return new ArrayList<TypedElement>(t.get_typedElementOfType());
    }
    
    /**
     * get the call behavior actions that're typed by b
     * @param b
     * @return
     */
    public static List<CallBehaviorAction> getCallBehaviorActionsOfBehavior(Behavior b) {
    	return new ArrayList<CallBehaviorAction>(b.get_callBehaviorActionOfBehavior());
    }
    
    /**
     * get call operation actions that're typed by o
     * @param o
     * @return
     */
    public static List<CallOperationAction> getCallOperationActionsOfOperation(Operation o) {
    	return new ArrayList<CallOperationAction>(o.get_callOperationActionOfOperation());
    }
    
    
    public static List<ActivityPartition> getSwimlanesThatRepresentElement(Element e) {
    	return new ArrayList<ActivityPartition>(e.get_activityPartitionOfRepresents());
    }
    
    public static void log(Object o) {
    	GUILog log = Application.getInstance().getGUILog();
    	log.log(o.toString());
    }
    
    /**
     * Gets list of values for a stereotype property, supports derived properties in customizations
     * @param e
     * @param p
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Object> getStereotypePropertyValues(Element e, Property p) {
    	List<Object> res = new ArrayList<Object>();
    	Element s = p.getOwner();
    	Collection<Object> values = null;
    	if (s instanceof Stereotype) 
    		values = (Collection<Object>)StereotypesHelper.getStereotypePropertyValue(e, (Stereotype)s, p, true);
    	else {
    		Object value = null;
    		try {
    		//maybe a derived property
    			value = e.refGetValue(p.getName()); //i think this only works for derived properties, throws reflection exception if not, should enclose in try catch
    		} catch (Exception ex) {
    			
    		}
    		if (value != null) {
    			if (value instanceof Element || value instanceof String) 
    				res.add(value);
    			else if (value instanceof Collection)
    				values = (Collection)value;
    		}	
    	}
    	if (values != null) 
    		res.addAll(values);
    	return res;
    }

  public static String slotValueToString( Slot slot ) {
    List< ValueSpecification > list = slot.getValue();
    List< String > sList = new ArrayList<String>();
    String s = ""; //if slot is empty or has nothing, show as empty string
    // If there is only one element in the list, just use that element instead
    // of the list.
    for ( ValueSpecification vs : list ) {
      s = RepresentationTextCreator.getRepresentedText( vs );
      sList.add( s );
    }
    if ( sList != null && !sList.isEmpty() ) {
      if ( sList.size() == 1 ) {
        s = sList.get( 0 );
      } else {
        s = sList.toString();
      }
    }
    return s;
  }
    
    /**
     * Given a string f and integer p, if f is a floating point number, returns the string with precision p
     * <p>ex. if f is 2.31111111 and p is 2, returns 2.31
     * @param f
     * @param p
     * @return
     */
    public static String floatTruncate(String f, int p) {
    	if (p < 0)
			return f;
    	if (f.indexOf(".") != -1) {
    		try {
    			double d = Double.parseDouble(f);
    			String pattern = "#.";
    			if (p == 0)
    				pattern = "#";
    			for (int i = 0; i < p; i++)
    				pattern += "#";
    			return new DecimalFormat(pattern).format(d);
    			//return String.format("%." + Integer.toString(p) + "f", d);
    		} catch (Exception e) {
    			return f;
    		}
    	}
    	return f;
    }
    
    public static String floatTruncate(double f, int p) {
		if (p < 0)
			return Double.toString(f);
		String pattern = "#.";
		if (p == 0)
			pattern = "#";
		for (int i = 0; i < p; i++)
			pattern += "#";
		return new DecimalFormat(pattern).format(f);
    	//return String.format("%." + Integer.toString(p) + "f", f);
    }
    
    /**
     * adds line numbers to the left of an existing dbtable (simple only, will not recalculate spans)
     * this returns the original table (will change the original!)
     */
    public static DBTable addLineNumber(DBTable b) {
    	int i = 1;
    	for (List<DocumentElement> row: b.getBody()) {
    		row.add(0, new DBText(Integer.toString(i)));
    		i++;
    	}
    	for (List<DocumentElement> row: b.getHeaders()) {
    		row.add(0, new DBText(""));
    	}
    	b.setCols(b.getCols() + 1);
    	if (b.getColspecs() != null) {
    		for (DBColSpec cs: b.getColspecs()) {
    			cs.setColnum(cs.getColnum() + 1);
    		}
    	}
    	return b;
    }
    
    /**
     * For user scripts, if you have a pop up table that should look the same as the docgen output, you can just use
     * this method to get back a DBTable to pass back to the output. 
     * @param et
     * @return
     */
    public static DBTable getDBTableFromEditableTable(EditableTable et, boolean addLineNum) {
    	EditableTableModel tm = (EditableTableModel)et.getTable().getModel();
    	DBTable res = new DBTable();
    	List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
    	List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
    	int cols = tm.getColumnCount();
    	if (addLineNum)
    		cols++;
    	List<DocumentElement> header = new ArrayList<DocumentElement>();
    	headers.add(header);
    	if (addLineNum)
    		header.add(new DBText(""));
    	for (int i = 0; i < tm.getColumnCount(); i++) {
    		header.add(new DBText(tm.getColumnName(i)));
    	}
    	int rows = tm.getRowCount();
    	for (int i = 0; i < rows; i++) {
    		List<DocumentElement> row = new ArrayList<DocumentElement>();
    		body.add(row);
    		if (addLineNum)
    			row.add(new DBText(Integer.toString(i+1)));
    		for (int j = 0; j < tm.getColumnCount(); j++) {
    			PropertyEnum what = tm.getWhatToChangeAt(i, j);
    			boolean editable = tm.isCellEditable(i, j);
    			Object cell = tm.getObjectAt(i, j);
    			if (cell instanceof Element && what != null && editable) {
    				if (what == PropertyEnum.NAME)
    					row.add(new DBText(tm.getValueAt(i, j).toString(), (Element)cell, From.NAME));
    				else if (what == PropertyEnum.VALUE)
    					row.add(new DBText(tm.getValueAt(i, j).toString(), (Element)cell, From.DVALUE));
    				else
    					row.add(new DBParagraph(tm.getValueAt(i, j).toString(), (Element)cell, From.DOCUMENTATION));
    			} else
    				row.add(new DBText(tm.getValueAt(i, j).toString()));
    		}
    	}
    	res.setBody(body);
    	res.setHeaders(headers);
    	res.setCols(cols);
    	res.setTitle(et.getTitle());
    	return res;
    }
    
    /**
     * For user scripts, if you have a pop up table, but you want to merge the first x columns into a column with indentations (like properties table). this assumes that for each merging, you have empty cells until the cell to be indented. 
     * @param et the editable table. 
     * @param merge this is a variable length parameter, indicates how many columns you want to merge consecutively. For example, if you want to merge the first 2 columns and then the next 2 columnes, do getDBTableFromEditableTable(et, 2, 2)
     * @return
     */
    public static DBTable getDBTableFromEditableTable(EditableTable et, boolean addLineNum, Integer ... merge) {
    	EditableTableModel tm = (EditableTableModel)et.getTable().getModel();
    	DBTable res = new DBTable();
    	List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
    	List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
    	int total = 0;
    	int adds = 0;
    	for (Integer i: merge) {
    		total += i;
    		adds++;
    	}
    	int cols = tm.getColumnCount() - total + adds;
    	if (addLineNum)
    		cols++;
    	List<DocumentElement> header = new ArrayList<DocumentElement>();
    	headers.add(header);
    	if (addLineNum)
    		header.add(new DBText(""));
    	int curCol = 0;
    	for (Integer i: merge) {
    		header.add(new DBText(tm.getColumnName(curCol)));
    		for (int j = 0; j < i; j++)
    			curCol++;
    	}
    	for (int i=curCol; i < tm.getColumnCount(); i++)
    		header.add(new DBText(tm.getColumnName(i)));
    	int rows = tm.getRowCount();
    	for (int row = 0; row < rows; row++) {
    		List<DocumentElement> rowl = new ArrayList<DocumentElement>();
    		body.add(rowl);
    		if (addLineNum)
    			rowl.add(new DBText(Integer.toString(row+1)));
    		curCol = 0;
    		for (Integer i: merge) {
    			String s = "";
    			for (int j = 0; j < i; j++) {
    				Object val = tm.getValueAt(row, curCol);
    				if (val.toString().equals("")) {
    					s += "&#xA0;&#xA0;&#xA0;&#xA0;";
    					curCol++;
    				} else {
    					s += DocGenUtils.fixString(val.toString());
    					for (int k = j; k < i; k++)
    						curCol++;
    					break;
    				}
    			}
    			rowl.add(new DBText(s));
    		}
    		for (int j = curCol; j < tm.getColumnCount(); j++) {
    			//rowl.add(new DBText(DocGenUtils.fixString(tm.getValueAt(row, j).toString())));
    			
    			PropertyEnum what = tm.getWhatToChangeAt(row, j);
    			boolean editable = tm.isCellEditable(row, j);
    			Object cell = tm.getObjectAt(row, j);
    			if (cell instanceof Element && what != null && editable) {
    				if (what == PropertyEnum.NAME)
    					rowl.add(new DBText(tm.getValueAt(row, j).toString(), (Element)cell, From.NAME));
    				else if (what == PropertyEnum.VALUE)
    					rowl.add(new DBText(tm.getValueAt(row, j).toString(), (Element)cell, From.DVALUE));
    				else
    					rowl.add(new DBParagraph(tm.getValueAt(row,  j).toString(), (Element)cell, From.DOCUMENTATION));
    			} else
    				rowl.add(new DBText(tm.getValueAt(row, j).toString()));
    		}
    	}
    	res.setBody(body);
    	res.setHeaders(headers);
    	res.setCols(cols);
    	res.setTitle(et.getTitle());
    	return res;
    }
    
    /**
     * This will set the default value of p to value, based on what type the
     * default value currently is right now, it'll try to convert to:
     * LiteralBoolean, LiteralInteger, LiteralUnlimitedNatural, otherwise it'll be
     * a LiteralString more options possibly in future like durations, etc
     * 
     * @param p
     * @param value
     */
    public static void setPropertyValue(Property p, String value) {
    	ValueSpecification valueSpec = p.getDefaultValue();
    	ValueSpecification v = makeValueSpecification( value, valueSpec );
    	p.setDefaultValue(v);
    }

    /**
     * This will set the default value of p to value, based on what type the
     * default value currently is right now, it'll try to convert to:
     * LiteralBoolean, LiteralInteger, LiteralUnlimitedNatural, otherwise it'll be
     * a LiteralString more options possibly in future like durations, etc
     * 
     * @param slot
     * @param value
     */
    public static void setSlotValue(Slot slot, String value) {
      List< ValueSpecification > valueSpecs = slot.getValue();
      ValueSpecification v = null;
      for ( ValueSpecification valueSpec : valueSpecs ) {
        v = makeValueSpecification( value, valueSpec );
        break;
      }
      valueSpecs.clear();
      valueSpecs.add( v );
    }

    /**
     * Creates a new {@link ValueSpecification} of the same type as valueSpec but
     * with a new value to be parsed from a {@link String}. It'll try to convert
     * to: LiteralBoolean, LiteralInteger, LiteralUnlimitedNatural, otherwise
     * it'll be a LiteralString more options possibly in future like durations,
     * etc
     * 
     * @param value
     * @param ef
     * @param valueSpec
     * @return
     */
    public static ValueSpecification
        makeValueSpecification( String value,
                                ValueSpecification valueSpec ) {
      ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
      ValueSpecification v;
      try {
      	if (valueSpec instanceof LiteralBoolean) {
      		v = ef.createLiteralBooleanInstance();
      		if (value.equals("false") || value.equals("False") || value.equals("F") || value.equals("f") || value.equals("no") || value.equals("n") || value.equals("") || value.equals("FALSE") || value.equals("NO") || value.equals("No"))
      			((LiteralBoolean)v).setValue(false);
      		else
      			((LiteralBoolean)v).setValue(true);
      	} else if (valueSpec instanceof LiteralInteger) {
      		v = ef.createLiteralIntegerInstance();
      		((LiteralInteger)v).setValue(Integer.parseInt(value));
      	} else if (valueSpec instanceof LiteralUnlimitedNatural) {
      		v = ef.createLiteralUnlimitedNaturalInstance();
      		((LiteralUnlimitedNatural)v).setValue(Integer.parseInt(value));
      	} else if (valueSpec instanceof LiteralReal) {
      		v = ef.createLiteralRealInstance();
      		((LiteralReal)v).setValue(Double.parseDouble(value));
      	} else {
      		v = ef.createLiteralStringInstance();
      		((LiteralString)v).setValue(value);
      	}
      } catch (NumberFormatException e) {
        // The field is no longer a number; treat it as a string
        // REVIEW -- This may conflict with the element's type.  Should we change the type of the element?
        // TODO -- Only some of the ValueSpecifications are supported here. The
        // subclasses of ValueSpecification and supportive factory methods in
        // ElementFactory can guide the addition of types.
        v = makeValueSpecification( value, ef.createLiteralStringInstance() );
      }

      return v;
    }
    
    /**
     * if s has any xml tags it'll assume it's html
     * @param s
     * @return
     */
    public static String addHtmlWrapper(String s) {
		if (!s.startsWith("<html")
				&& (s.contains("</") || (s.contains("/>")))) {
			return "<html>\n<body>\n" + s + "</body>\n</html>";
		}
		return s;
	}

	public static final Pattern HTML_WRAPPER_START = Pattern.compile("^<html>.*<body>\\s*", Pattern.DOTALL);
	public static final Pattern HTML_WRAPPER_END = Pattern.compile("\\s*</body>.*</html>\\s*$", Pattern.DOTALL);
	
	/**
	 * Remove HTML wrapper. This might include a head element or some amount of whitespace.
	 * @param before
	 * @return
	 */
	public static String stripHtmlWrapper(String before) {
		String startRemoved = HTML_WRAPPER_START.matcher(before).replaceAll("");
		return HTML_WRAPPER_END.matcher(startRemoved).replaceAll("");
	}
	
	/**
	 * depending on includeInherited flag, gets all the attributes of e based on redefinition (if e is not a classifier it'll be ignored)
	 * if includeInherited is false, will get the owned attributes of the classifiers
	 * @param c
	 * @param includeInherited
	 * @return
	 */
	public static List<Property> getAttributes(Element e, boolean includeInherited) {
		List<Property> res = new ArrayList<Property>();
		
			if (!(e instanceof Classifier))
				return res;
			List<Property> owned = new ArrayList<Property>(((Classifier)e).getAttribute());
			res.addAll(owned);
			if (includeInherited) {
				Collection<NamedElement> inherited = new ArrayList<NamedElement>(((Classifier)e).getInheritedMember());
				List<NamedElement> inheritedCopy = new ArrayList<NamedElement>(inherited);
				for (NamedElement ne: inherited) {
					if (ne instanceof Property) {
						for (Property redef: ((Property)ne).getRedefinedProperty()) 
							inheritedCopy.remove(redef);
					} else
						inheritedCopy.remove(ne);
				}
				for (Property p: owned) 
					for (Property redef: ((Property)p).getRedefinedProperty()) 
						inheritedCopy.remove(redef);
				for (NamedElement ee: inheritedCopy)
					res.add((Property)ee);
			}
		
    	return res;
    }
	
	public static String getUsername() {
		String username;
		String teamworkUsername = TeamworkUtils.getLoggedUserName();
		if (teamworkUsername != null) {
			username = teamworkUsername;
		} else {
			username = System.getProperty("user.name", "");
		}
		return username;
	}
	
	public static void displayValidationWindow(Collection<ValidationSuite> vss, String title) {
		Project project = Application.getInstance().getProject();
        ValidationResultProvider provider = project.getValidationResultProvider();
        Collection<RuleViolationResult> results = new ArrayList<RuleViolationResult>();
        Package dummyvs = (Package)project.getElementByID("_17_0_2_407019f_1354124289134_280378_12909");
        Constraint cons = (Constraint)project.getElementByID("_17_0_2_2_f4a035d_1360957024690_702520_27755");
        
        if (dummyvs == null || cons == null)
        	return;
        Set<Element> elements = new HashSet<Element>();
		for (ValidationSuite vs: vss) {
			for (ValidationRule vr: vs.getValidationRules()) {
				EnumerationLiteral severity;
				/*
				 * Switch added by Peter Di Pasquale 02/15/2013
				 * Changelog: switch statement added, selects element highlight and icon color according to severity of error. 
				 */
				
				switch (vr.getSeverity()){
				case WARNING: 
					severity = Annotation.getSeverityLevel(project, Annotation.WARNING);
					cons = (Constraint)project.getElementByID("_17_0_2_2_f4a035d_1360957024690_702520_27755");
				break;
				case ERROR: 
					severity = Annotation.getSeverityLevel(project, Annotation.ERROR);
					cons = (Constraint) project.getElementByID("_17_0_2_407019f_1354058024392_224770_12910");
				break;
				case FATAL: 
					severity = Annotation.getSeverityLevel(project, Annotation.FATAL);
					cons = (Constraint) project.getElementByID("_17_0_2_2_f4a035d_1360957445325_901851_27756");
				break;
				case INFO: 
					severity = Annotation.getSeverityLevel(project, Annotation.INFO);
					cons = (Constraint) project.getElementByID("_17_0_2_2_f4a035d_1360957474351_901777_27765");
				break;
				default: severity = Annotation.getSeverityLevel(project, Annotation.WARNING);
				break;
				}
				for (ValidationRuleViolation vrv: vr.getViolations()) {
					Annotation anno = new Annotation(severity, vr.getName(), vrv.getComment(), vrv.getElement());
					results.add(new RuleViolationResult(anno, cons));
					elements.add(vrv.getElement());
				}
			}
		}
		EnumerationLiteral severitylevel =Annotation.getSeverityLevel(project, Annotation.WARNING);
		ValidationRunData runData = new ValidationRunData(dummyvs, false, elements, severitylevel);
//		ValidationRunData runData = new ValidationRunData(dummyvs, false, elements, Annotation.getSeverityLevel(project, Annotation.DEBUG));
		//provider.dispose();
		//provider.init();
		String id = "" + System.currentTimeMillis();
		//provider.setValidationResults(id, results);
		//provider.update();
		ValidationResultsWindowManager.updateValidationResultsWindow(id, title, runData, results);
		
	}
	
	public static String escapeString(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
	}
}
