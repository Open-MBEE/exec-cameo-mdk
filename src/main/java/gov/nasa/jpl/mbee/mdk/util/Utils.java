package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.ui.MainFrame;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.validation.RuleViolationResult;
import com.nomagic.magicdraw.validation.ValidationRunData;
import com.nomagic.magicdraw.validation.ui.ValidationResultsWindowManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import gov.nasa.jpl.mbee.mdk.docgen.table.EditableTable;
import gov.nasa.jpl.mbee.mdk.docgen.table.EditableTableModel;
import gov.nasa.jpl.mbee.mdk.docgen.table.PropertyEnum;
import gov.nasa.jpl.mbee.mdk.emf.EmfUtils;
import gov.nasa.jpl.mbee.mdk.generator.CollectFilterParser;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.ocl.GetCallOperation;
import gov.nasa.jpl.mbee.mdk.ocl.GetCallOperation.CallReturnType;
import gov.nasa.jpl.mbee.mdk.ocl.OclEvaluator;
import gov.nasa.jpl.mbee.mdk.validation.*;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility methods for collection and filtering on magicdraw
 * elements.<br/>
 * All collection and filter methods will return an empty list if there's
 * nothing matching the criteria.<br/>
 * For filter*** methods, if the criteria collection is empty, then the include
 * flag will determine whether you get an empty list or a list containing all
 * the source elements.<br/>
 * Element collections passed in will never be modified or returned. All methods
 * return new collections.
 * <p>
 * For methods that change the model in some way, no check are performed on
 * whether element is editable or not. Sessions are also not created. The caller
 * will need to manage sessions themselves and wrap any call in a session.
 *
 * @author dlam
 */
@Deprecated
public class Utils {
    public static Logger log = Logger.getLogger(Utils.class);

    private static boolean forceDialogFalse = false;
    private static boolean forceDialogTrue = false;
    private static boolean forceDialogCancel = false;
    private static boolean popupsDisabled = false;

    private Utils() {
    }

    /******************************************** Collect/Filter/Sort *******************************************************/

    /**
     * This does not change the passed in elements collection, it returns a new
     * list Compared to just using Set, this preserves the order of the passed
     * in collection
     *
     * @param elements
     * @return
     */
    public static <T> List<T> removeDuplicates(Collection<T> elements) {
        Set<T> added = new HashSet<>();
        List<T> res = new ArrayList<>();
        for (T e : elements) {
            if (!added.contains(e)) {
                res.add(e);
                added.add(e);
            }
        }
        return res;
    }


    /**
     * returns collection of model elements that's on the diagram
     *
     * @param diagram selected diagram
     * @return
     */
    public static Collection<Element> getElementsOnDiagram(Diagram diagram) {
        return Project.getProject(diagram).getDiagram(diagram).getUsedModelElements(true);
    }

    /**
     * like it says
     *
     * @param diagrams can be a list of any model element, only diagrams will be
     *                 tested and returned
     * @param types    this needs to be a list of the diagram type names. This
     *                 usually shows up when you hover over the new diagram icon in
     *                 magicdraw
     * @param include  whether to include the diagram type or not
     * @return
     */
    public static List<Diagram> filterDiagramsByDiagramTypes(Collection<Element> diagrams, List<String> types, boolean include) {
        List<Diagram> res = new ArrayList<>();
        for (Element d : diagrams) {
            Project project = Project.getProject(d);
            if (!(d instanceof Diagram)) {
                continue;
            }
            DiagramPresentationElement dpe = project.getDiagram((Diagram) d);
            if (types.contains(dpe.getDiagramType().getType())) {
                if (include) {
                    res.add((Diagram) d);
                }
            }
            else if (!include) {
                res.add((Diagram) d);
            }
        }
        return res;
    }

    public static List<Element> filterElementsByStereotype(Collection<Element> elements,
                                                           Stereotype stereotype, boolean include, boolean derived) {
        List<Element> res = new ArrayList<>();
        if (include) {
            for (Element e : elements) {
                if (derived && StereotypesHelper.hasStereotypeOrDerived(e, stereotype) || !derived
                        && StereotypesHelper.hasStereotype(e, stereotype)) {
                    res.add(e);
                }
            }
        }
        else {
            for (Element e : elements) {
                if (derived && !StereotypesHelper.hasStereotypeOrDerived(e, stereotype) || !derived
                        && !StereotypesHelper.hasStereotype(e, stereotype)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    protected static final String[] trueStrings = new String[]{"t", "true", "1", "1.0", "yes", "y"};

    public static Boolean isTrue(Object o, boolean strict) {
        if (o == null) {
            return strict ? null : false;
        }
        if (Boolean.class.isAssignableFrom(o.getClass())) {
            return (Boolean) o;
        }
        String lower = o.toString().toLowerCase();
        if (lower.equals("true")) {
            return true;
        }
        if (lower.equals("false")) {
            return false;
        }
        if (strict) {
            return null;
        }
        for (String t : trueStrings) {
            if (lower.equals(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param elements
     * @param query    ocl expression
     * @param include  whether to include element or not if query is true
     * @param iterate  whether to apply query to each element or collection as a whole
     * @return
     */
    public static List<Element> filterElementsByExpression(Collection<Element> elements, String query,
                                                           boolean include, boolean iterate) {
        List<Element> res = new ArrayList<>();
        OclEvaluator evaluator;
        if (!iterate) {
            Object o;
            DocumentValidator dv = CollectFilterParser.getValidator();
            o = DocumentValidator.evaluate(query, elements, dv, true);
            evaluator = OclEvaluator.instance;
            if (evaluator != null && (evaluator.isValid() || !Utils2.isNullOrEmpty(o))) {
                Boolean istrue = isTrue(o, false);
                if (include == (istrue == null ? false : istrue)) {
                    res.addAll(elements);
                }
            }
        }
        else {
            for (Element e : elements) {
                Object o;
                DocumentValidator dv = CollectFilterParser.getValidator();
                o = DocumentValidator.evaluate(query, e, dv, true);
                evaluator = OclEvaluator.instance;
                if (evaluator != null && (evaluator.isValid() || !Utils2.isNullOrEmpty(o))) {
                    Boolean istrue = isTrue(o, false);
                    if (include == (istrue == null ? false : istrue)) {
                        res.add(e);
                    }
                }
            }
        }
        return res;
    }

    /**
     * @param elements
     * @param stereotypes
     * @param include     whether to include or exclude elements with given stereotypes
     * @param derived     whether to consider derived stereotypes
     * @return
     */
    public static List<Element> filterElementsByStereotypes(Collection<Element> elements,
                                                            Collection<Stereotype> stereotypes, boolean include, boolean derived) {
        List<Element> res = new ArrayList<>();
        if (stereotypes.isEmpty() && include) {
            return res;
        }
        if (stereotypes.isEmpty() && !include) {
            res.addAll(elements);
            return res;
        }
        if (include) {
            for (Element e : elements) {
                if (derived && StereotypesHelper.hasStereotypeOrDerived(e, stereotypes) || !derived
                        && StereotypesHelper.hasStereotype(e, stereotypes)) {
                    res.add(e);
                }
            }
        }
        else {
            for (Element e : elements) {
                if (derived && !StereotypesHelper.hasStereotypeOrDerived(e, stereotypes) || !derived
                        && !StereotypesHelper.hasStereotype(e, stereotypes)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    /**
     * matches name of elements against a collect of regular expression strings,
     * see java.util.regex.Pattern for regex patterns
     *
     * @param elements
     * @param regex
     * @param include  element will be returned if include is true and matched, or include is false and not matched
     * @return
     */
    public static List<Element> filterElementsByNameRegex(Collection<Element> elements,
                                                          Collection<String> regex, boolean include) {
        List<Element> res = new ArrayList<>();
        if (regex.isEmpty() && include) {
            return res;
        }
        if (regex.isEmpty() && !include) {
            res.addAll(elements);
            return res;
        }
        List<Pattern> patterns = new ArrayList<>();
        for (String s : regex) {
            patterns.add(Pattern.compile(s));
        }
        for (Element e : elements) {
            boolean matched = false;
            if (e instanceof NamedElement) {
                String name = ((NamedElement) e).getName();
                for (Pattern p : patterns) {
                    if (p.matcher(name).matches()) {
                        matched = true;
                        break;
                    }
                }
                if ((include && matched) || (!include && !matched)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    /**
     * @param elements
     * @param javaClasses
     * @param include
     * @return
     */
    public static List<Element> filterElementsByJavaClasses(Collection<Element> elements,
                                                            Collection<java.lang.Class<?>> javaClasses, boolean include) {
        List<Element> res = new ArrayList<>();
        if (Utils2.isNullOrEmpty(elements) || javaClasses == null) {
            return res;
        }
        if (javaClasses.isEmpty() && !include) {
            res.addAll(elements);
            return res;
        }
        if (javaClasses.isEmpty() && include) {
            return res;
        }

        if (include) {
            for (Element e : elements) {
                for (java.lang.Class<?> c : javaClasses) {
                    if (c != null && e != null && c.isInstance(e)) {
                        res.add(e);
                        break;
                    }
                }
            }
        }
        else {
            for (Element e : elements) {
                boolean add = true;
                for (java.lang.Class<?> c : javaClasses) {
                    if (c != null && e != null && c.isInstance(e)) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    /**
     * @param elements
     * @param metaclasses these are the metaclass element classes from magicdraw's uml
     *                    profile
     * @param include     whether to include elements with given metaclasses or not,
     *                    this will always include derived
     * @return
     */
    public static List<Element> filterElementsByMetaclasses(Collection<Element> elements,
                                                            Collection<Class> metaclasses, boolean include) {
        List<java.lang.Class<?>> javaClasses = new ArrayList<>();
        for (Class c : metaclasses) {
            javaClasses.add(StereotypesHelper.getClassOfMetaClass(c));
        }
        return filterElementsByJavaClasses(elements, javaClasses, include);
    }

    /**
     * collect the owner of the element recursively up to depth (intermediate
     * owners will be returned also)
     *
     * @param e
     * @param depth collect to what level of depth - 0 is infinite
     * @return
     */
    public static List<Element> collectOwners(Element e, int depth) {
        List<Element> res = new ArrayList<>();
        collectRecursiveOwners(e, res, depth, 1);
        return res;
    }

    private static void collectRecursiveOwners(Element e, List<Element> all, int depth, int current) {
        if (depth != 0 && current > depth) {
            return;
        }
        Element owner = e.getOwner();
        if (owner != null) {
            all.add(owner);
            collectRecursiveOwners(owner, all, depth, current + 1);
        }
    }

    /**
     * Collects ownedElements recursively, returned collection will not include
     * source
     *
     * @param e
     * @param depth collect to what level of depth - 0 is infinite
     * @return
     */
    public static List<Element> collectOwnedElements(Element e, int depth) {
        List<Element> res = new ArrayList<>();
        collectRecursiveOwnedElements(e, res, depth, 1);
        return res;
    }

    private static void collectRecursiveOwnedElements(Element e, List<Element> all, int depth, int current) {
        if (depth != 0 && current > depth) {
            return;
        }
        Collection<Element> owned = e.getOwnedElement();
        for (Element o : owned) {
            all.add(o);
            collectRecursiveOwnedElements(o, all, depth, current + 1);
        }
    }

    /**
     * Get a list including all objects that are of the specified type and are o
     * or a child/grandchild of o if o is a Collection. type and the items in o
     * that are of the specified type if o is a Collection.
     *
     * @param o
     * @param type
     * @return a list of objects of the specified type or an empty list if there
     * are none
     */
    public static <T> List<T> getListOfType(Object o, java.lang.Class<T> type) {
        return getListOfType(o, type, null);
    }

    /**
     * Get a list including all objects that are of the specified type and are o
     * or a child/grandchild of o if o is a Collection. type and the items in o
     * that are of the specified type if o is a Collection.
     *
     * @param o
     * @param type
     * @param seen a list of already visited objects to avoid infinite recursion
     * @return a list of objects of the specified type or an empty list if there
     * are none
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getListOfType(Object o, java.lang.Class<T> type, Set<Object> seen) {
        List<T> res = new ArrayList<T>();
        if (type == null || o == null) {
            return res;
        }
        Pair<Boolean, Set<Object>> p = Utils2.seen(o, true, seen);
        if (p.getKey()) {
            return res;
        }
        seen = p.getValue();
        if (type.isInstance(o)) {
            res.add((T) o);
        }
        else if (o instanceof Collection) {
            for (Object obj : (Collection<?>) o) {
                res.addAll(getListOfType(obj, type, seen));
            }
        }
        return res;
    }

    /**
     * Get elements returned by evaluating a query expression on an element.
     *
     * @param element the context of the query
     * @param query   a query expression, such as OCL text in a String
     * @return a list containing the result of the query if it is an Element,
     * the Elements in the result if it is a collection, or else an
     * empty list
     */
    public static List<Element> collectByExpression(Object element, Object query) {
        List<Element> res = new ArrayList<Element>();
        Object o = null;
        DocumentValidator dv = CollectFilterParser.getValidator();
        o = DocumentValidator.evaluate(query, element, dv, true);
        OclEvaluator evaluator = OclEvaluator.instance;
        // try {
        // o = OclEvaluator.evaluateQuery(element, query);
        if (evaluator.isValid() || !Utils2.isNullOrEmpty(o)) {
            res.addAll(getListOfType(o, Element.class));
        }
        // } catch ( ParserException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return res;
    }

    /**
     * Collect all objects of type Element in each of the results of evaluating
     * the query expression on each of the elements.
     *
     * @param elements contexts for evaluating the expression
     * @param query
     * @param iterate
     * @return a List of Elements
     */
    public static List<Element> collectByExpression(List<Element> elements, String query, boolean iterate) {
        List<Element> res = new ArrayList<>();
        if (!iterate) {
            res.addAll(collectByExpression(elements, query));
        }
        else {
            for (Element e : elements) {
                res.addAll(collectByExpression(e, query));
            }
        }
        return res;
    }

    /**
     * @param e     needs to be a Classifier, else empty list will be returned
     * @param depth collect to what level of depth - 0 is infinite
     * @param kind
     * @return
     */
    public static List<Element> collectAssociatedElements(Element e, int depth, AggregationKind kind) {
        List<Element> res = new ArrayList<>();
        if (e instanceof Classifier) {
            collectRecursiveAssociatedElements((Classifier) e, res, depth, 1, kind);
        }
        return res;
    }

    private static void collectRecursiveAssociatedElements(Classifier e, List<Element> all, int depth,
                                                           int current, AggregationKind kind) {
        if (depth != 0 && current > depth) {
            return;
        }
        Collection<Property> owned = e.getAttribute();
        for (Property o : owned) {
            if (all.contains(o)) {
                continue;
            }
            if (o.getAggregation() != kind) {
                continue;
            }
            Type type = o.getType();
            if (type == null) {
                continue;
            }
            if (all.contains(type)) {
                continue;
            }
            all.add(type);
            if (type instanceof Classifier) {
                collectRecursiveAssociatedElements((Classifier) type, all, depth, current + 1, kind);
            }
        }
    }

    /**
     * This will consider all relationships that are also specializations of
     * javaClasses
     *
     * @param e
     * @param javaClasses this is the class of the relationships to consider
     * @param direction   0 is both, 1 is outward, 2 is inward
     * @param depth       collect to what level of depth - 0 is infinite
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipJavaClasses(Element e,
                                                                                        Collection<java.lang.Class<?>> javaClasses, int direction, int depth) {
        List<Element> res = new ArrayList<Element>();
        if (e == null) {
            return res;
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipJavaClasses()");
            direction = 0;
        }
        collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(e, javaClasses, direction, depth, 1,
                res);
        return res;
    }

    private static void collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(Element e,
                                                                                         Collection<java.lang.Class<?>> javaClasses, int direction, int depth, int curdepth,
                                                                                         List<Element> res) {
        if (e == null) {
            return;
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipJavaClassesRecursive()");
            direction = 0;
        }
        if (depth != 0 && curdepth > depth) {
            return;
        }
        if (direction == 0 || direction == 1) {
            for (DirectedRelationship dr : e.get_directedRelationshipOfSource()) {
                for (java.lang.Class<?> c : javaClasses) {
                    if (c.isInstance(dr)) {
                        if (!res.contains(ModelHelper.getSupplierElement(dr))) {
                            res.add(ModelHelper.getSupplierElement(dr));
                            collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(
                                    ModelHelper.getSupplierElement(dr), javaClasses, direction, depth,
                                    curdepth + 1, res);
                            break;
                        }
                    }
                }
            }
        }
        if (direction == 0 || direction == 2) {
            for (DirectedRelationship dr : e.get_directedRelationshipOfTarget()) {
                for (java.lang.Class<?> c : javaClasses) {
                    if (c.isInstance(dr)) {
                        if (!res.contains(ModelHelper.getClientElement(dr))) {
                            res.add(ModelHelper.getClientElement(dr));
                            collectDirectedRelatedElementsByRelationshipJavaClassesRecursive(
                                    ModelHelper.getClientElement(dr), javaClasses, direction, depth,
                                    curdepth + 1, res);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param e
     * @param c
     * @param direction 0 is both, 1 is outward, 2 is inward
     * @param depth     collect to what level of depth - 0 is infinite
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipJavaClass(Element e, java.lang.Class<?> c, int direction, int depth) {
        if (e == null) {
            return Utils2.newList();
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipJavaClass()");
            direction = 0;
        }
        List<java.lang.Class<?>> classes = new ArrayList<>();
        classes.add(c);
        return collectDirectedRelatedElementsByRelationshipJavaClasses(e, classes, direction, depth);
    }

    /**
     * @param e
     * @param metaclasses these are the metaclass element classes from magicdraw's uml
     *                    profile, this will always considered derived relationship
     *                    metaclasses
     * @param direction   0 means both, 1 means e is the client, 2 means e is the
     *                    supplier
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipMetaclasses(Element e,
                                                                                        Collection<Class> metaclasses, int direction, int depth) {
        if (e == null) {
            return null;
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipMetaclasses()");
            direction = 0;
        }
        List<java.lang.Class<?>> javaClasses = new ArrayList<java.lang.Class<?>>();
        for (Class c : metaclasses) {
            java.lang.Class classOfMetaClass = StereotypesHelper.getClassOfMetaClass(c);
            if (classOfMetaClass == null) {
                continue;
            }
            javaClasses.add(classOfMetaClass);
        }
        return collectDirectedRelatedElementsByRelationshipJavaClasses(e, javaClasses, direction, depth);
    }

    /**
     * @param e
     * @param stereotypes
     * @param direction   0 means both, 1 means e is the client, 2 means e is the
     *                    supplier
     * @param derived     whether to consider derived stereotypes
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipStereotypes(Element e,
                                                                                        Collection<Stereotype> stereotypes, int direction, boolean derived, int depth) {
        List<Element> res = new ArrayList<Element>();
        if (e == null) {
            return res;
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipStereotypes()");
            direction = 0;
        }
        // client: 0 is both, 1 is client, 2 is supplier
        collectDirectedRelatedElementsByRelationshipStereotypesRecursive(e, stereotypes, direction, derived,
                depth, 1, res);
        return res;
    }

    private static void collectDirectedRelatedElementsByRelationshipStereotypesRecursive(Element e, Collection<Stereotype> stereotypes, int direction, boolean derived, int depth, int curdepth, List<Element> res) {
        if (e == null) {
            return;
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipStereotypesRecursive()");
            direction = 0;
        }
        if (depth != 0 && curdepth > depth) {
            return;
        }
        // client: 0 is both, 1 is client, 2 is supplier
        if (direction == 0 || direction == 1) {
            for (DirectedRelationship dr : e.get_directedRelationshipOfSource()) {
                if (derived && StereotypesHelper.hasStereotypeOrDerived(dr, stereotypes) || !derived
                        && StereotypesHelper.hasStereotype(dr, stereotypes)) {
                    if (!res.contains(ModelHelper.getSupplierElement(dr))) {
                        res.add(ModelHelper.getSupplierElement(dr));
                        collectDirectedRelatedElementsByRelationshipStereotypesRecursive(
                                ModelHelper.getSupplierElement(dr), stereotypes, direction, derived, depth,
                                curdepth + 1, res);
                    }
                }
            }
        }
        if (direction == 0 || direction == 2) {
            for (DirectedRelationship dr : e.get_directedRelationshipOfTarget()) {
                if (derived && StereotypesHelper.hasStereotypeOrDerived(dr, stereotypes) || !derived
                        && StereotypesHelper.hasStereotype(dr, stereotypes)) {
                    if (!res.contains(ModelHelper.getClientElement(dr))) {
                        res.add(ModelHelper.getClientElement(dr));
                        collectDirectedRelatedElementsByRelationshipStereotypesRecursive(
                                ModelHelper.getClientElement(dr), stereotypes, direction, derived, depth,
                                curdepth + 1, res);
                    }
                }
            }
        }
    }

    /**
     * @param e
     * @param stereotype
     * @param direction  direction 0 means both, 1 means e is the client, 2 means e is
     *                   the supplier
     * @param derived
     * @param depth      collect to what level of depth - 0 is infinite
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipStereotype(Element e, Stereotype stereotype, int direction, boolean derived, int depth) {
        if (e == null) {
            return Utils2.newList();
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipStereotype()");
            direction = 0;
        }
        List<Stereotype> s = new ArrayList<Stereotype>();
        s.add(stereotype);
        return collectDirectedRelatedElementsByRelationshipStereotypes(e, s, direction, derived, depth);
    }

    public static List<Object> collectByStereotypeProperty(Element e, Property p) {
        List<Object> results = new ArrayList<Object>();
        if (p.getOwner() instanceof Stereotype) {
            results.addAll(StereotypesHelper.getStereotypePropertyValue(e, (Stereotype) p.getOwner(), p));
        }
        if (results.isEmpty()) {
            try {
                Object value = e.refGetValue(p.getName()); // i think this
                // only works
                // for derived
                // properties
                if (value != null) {
                    if (value instanceof Collection) {
                        results.addAll((Collection<?>) value);
                    }
                    else {
                        results.add(value);
                    }
                }
            } catch (Throwable t) { // ignore
            }
        }
        return results;
    }

    protected static void badDirectionError(int direction, String methodSignature) {
        Debug.error("Error! Bad direction " + direction + " for " + methodSignature
                + "; using direction = 0 (both directions).");
    }

    /**
     * @param e
     * @param stereotype
     * @param direction  direction 0 means both, 1 means e is the client, 2 means e is
     *                   the supplier
     * @param derived
     * @param depth      collect to what level of depth - 0 is infinite
     * @return
     */
    public static List<Element> collectDirectedRelatedElementsByRelationshipStereotypeString(Element e, String stereotype, int direction, boolean derived, int depth) {
        Project project = Project.getProject(e);
        if (e == null) {
            return Utils2.newList();
        }
        if (direction < 0 || direction > 2) {
            badDirectionError(direction, "collectDirectedRelatedElementsByRelationshipStereotype()");
            direction = 0;
        }
        Stereotype s = StereotypesHelper.getStereotype(project, stereotype);
        if (s != null) {
            return collectDirectedRelatedElementsByRelationshipStereotype(e, s, direction, derived, depth);
        }
        return Utils2.newList();
    }

    /**
     * depending on includeInherited flag, gets all the attributes of e based on
     * redefinition (if e is not a classifier it'll be ignored) if
     * includeInherited is false, will get the owned attributes of the
     * classifiers
     *
     * @param e
     * @param includeInherited
     * @return
     */
    public static List<Property> getAttributes(Element e, boolean includeInherited) {
        List<Property> res = new ArrayList<Property>();

        if (!(e instanceof Classifier)) {
            return res;
        }
        List<Property> owned = new ArrayList<>(((Classifier) e).getAttribute());
        res.addAll(owned);
        if (includeInherited) {
            Collection<NamedElement> inherited = new ArrayList<>(
                    ((Classifier) e).getInheritedMember());
            List<NamedElement> inheritedCopy = new ArrayList<>(inherited);
            for (NamedElement ne : inherited) {
                if (ne instanceof Property) {
                    for (Property redef : ((Property) ne).getRedefinedProperty()) {
                        inheritedCopy.remove(redef);
                    }
                }
                else {
                    inheritedCopy.remove(ne);
                }
            }
            for (Property p : owned) {
                for (Property redef : p.getRedefinedProperty()) {
                    inheritedCopy.remove(redef);
                }
            }
            for (NamedElement ee : inheritedCopy) {
                res.add((Property) ee);
            }
        }

        return res;
    }

    public static List<Element> intersectionOfCollections(Collection<? extends Collection<Element>> a) {
        List<Element> i = new ArrayList<Element>();
        Set<Element> inter = new HashSet<Element>(a.iterator().next());
        for (Collection<Element> es : a) {
            inter.retainAll(es);
        }
        i.addAll(inter);
        return i;
    }

    public static List<Element> unionOfCollections(Collection<? extends Collection<Element>> a) {
        List<Element> i = new ArrayList<Element>();
        Set<Element> union = new HashSet<Element>(a.iterator().next());
        for (Collection<Element> c : a) {
            union.addAll(c);
        }
        i.addAll(union);
        return i;
    }

    /**
     * returns elements not shared
     * misnomer - this is really doing the symmetric difference
     *
     * @param cc
     * @return
     */
    public static List<Element> xorOfCollections(Collection<? extends Collection<Element>> cc) {
        if (cc.size() > 1) {
            Iterator<? extends Collection<Element>> i = cc.iterator();
            Set<Element> a = new HashSet<>(i.next());
            Set<Element> b = new HashSet<>(i.next());
            Set<Element> c = new HashSet<>();
            c.addAll(a);
            c.addAll(b);
            a.retainAll(b);
            c.removeAll(a);
            return new ArrayList<>(c);
        }
        return new ArrayList<>();
    }

    /**
     * Sorts e by name, returns a new list with name ordered elements
     *
     * @param e
     * @return
     */
    public static <T, TT extends T> List<T> sortByName(Collection<TT> e) {
        List<T> n = new ArrayList<T>(e);
        Collections.sort(n, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                if (o1 instanceof NamedElement && o2 instanceof NamedElement) {
                    return ((NamedElement) o1).getName().toLowerCase().compareTo(((NamedElement) o2).getName().toLowerCase());
                }
                return CompareUtils.compare(o1, o2);
            }
        });
        return n;
    }

    /**
     * Sorts elements by a specific attribute limited to the enumeration below,
     * which is suspiciously similar to the possible attributes in
     * tableAttributeColumn...
     *
     * @param elem
     * @param attr
     * @return
     */
    public static List<Element> sortByAttribute(Collection<? extends Element> elem, AvailableAttribute attr) {
        List<Element> list = new ArrayList<Element>(elem);
        switch (attr) {
            case Name:
                Collections.sort(list, getAttributeComparator(AvailableAttribute.Name, false));
                break;
            case Documentation:
                Collections.sort(list, getAttributeComparator(AvailableAttribute.Documentation, false));
                break;
            case Value:
                boolean isAllNumbers = true;
                for (Element e : list) {
                    if (!Utils2.isNumber(DocGenUtils.fixString(getElementAttribute(e, attr)))) {
                        isAllNumbers = false;
                        break;
                    }
                }
                Collections.sort(list, getAttributeComparator(AvailableAttribute.Value, isAllNumbers));
                break;

        }
        return list;
    }

    private static Comparator<Element> getAttributeComparator(AvailableAttribute attr, boolean isAllNumbers) {
        final AvailableAttribute attribute = attr;
        final boolean allNums = isAllNumbers;

        return new Comparator<Element>() {
            @Override
            public int compare(Element A, Element B) {
                if (A == B) {
                    return 0;
                }
                if (A == null) {
                    return -1;
                }
                if (B == null) {
                    return 1;
                }
                Object a = getElementAttribute(A, attribute);
                Object b = getElementAttribute(B, attribute);
                if (a == b) {
                    return 0;
                }
                if (a == null) {
                    return -1;
                }
                if (b == null) {
                    return 1;
                }
                switch (attribute) {
                    case Name:
                        return CompareUtils.compare(a.toString().toLowerCase(), b.toString().toLowerCase(), true);
                    case Documentation:
                        return CompareUtils.compare(a.toString().length(), b.toString().length(), true);
                    case Value:
                        if (allNums) {
                            Double da = Utils2.toDouble(DocGenUtils.fixString(a));
                            Double db = Utils2.toDouble(DocGenUtils.fixString(b));
                            return CompareUtils.compare(da, db, true);
                        }
                        return CompareUtils.compare(a, b, true);
                    default:
                        return CompareUtils.compare(a, b, true);
                }
            }
        };
    }

    /**
     * Sorts elements by a any property. Strings treated alphabetically, numbers
     * treated least on top.
     *
     * @param elem
     * @param prop
     * @return
     */
    public static List<Element> sortByProperty(Collection<? extends Element> elem, Property prop) {
        List<Element> list = new ArrayList<Element>(elem);
        // Check if all numbers first
        boolean isAllNumbers = true;
        for (Element e : list) {
            List<Object> temp = getElementPropertyValues(e, prop, true);
            if (temp.size() != 1) {
                isAllNumbers = false;
                break;
            }
            for (Object o : temp) {
                if (!Utils2.isNumber(DocGenUtils.fixString(o))) {
                    isAllNumbers = false;
                    break;
                }
            }
            if (!isAllNumbers) {
                break;
            }
        }
        Collections.sort(list, getPropertyComparator(prop, isAllNumbers));
        return list;
    }

    private static Comparator<Element> getPropertyComparator(Property prop, boolean isAllNumbers) {
        final Property property = prop;
        final boolean allNums = isAllNumbers;

        return new Comparator<Element>() {
            @Override
            public int compare(Element A, Element B) {
                if (A == B) {
                    return 0;
                }
                if (A == null) {
                    return -1;
                }
                if (B == null) {
                    return 1;
                }
                List<Object> a = getElementPropertyValues(A, property, true);
                List<Object> b = getElementPropertyValues(B, property, true);
                if (a == b) {
                    return 0;
                }
                if (a == null) {
                    return -1;
                }
                if (b == null) {
                    return 1;
                }
                if (a.size() == 1 && b.size() == 1) {
                    Object a0 = a.get(0);
                    Object b0 = b.get(0);
                    String as = DocGenUtils.fixString(a0);
                    String bs = DocGenUtils.fixString(b0);
                    if (allNums) {
                        Double da0 = Utils2.toDouble(as);
                        Double db0 = Utils2.toDouble(bs);
                        return CompareUtils.compare(da0, db0, true);
                    }
                    else {
                        return CompareUtils.compare(as, bs, true);
                    }
                }
                else {
                    return a.size() - b.size();
                }
            }
        };
    }

    /**
     * Sorts elements by attribute, provided it is one of those supported by
     *
     * @param elem the element whose attribute is sought
     * @param attr the type of attribute (name, value, ...)
     * @return
     */
    public static List<Element> sortByAttribute(Collection<? extends Element> elem, Object attr) {
        return sortByAttribute(elem, getAvailableAttribute(attr));
    }

    public static List<Element> sortByExpression(Collection<? extends Element> elem, Object o) {
        List<Element> list = new ArrayList<Element>(elem);
        // Check if all numbers
        boolean isAllNumbers = true;
        Map<Element, Object> resultMap = new HashMap<Element, Object>();
        Map<Element, Object> resultNumberMap = new HashMap<Element, Object>();
        for (Element e : list) {
            Object result = null;
            DocumentValidator dv = CollectFilterParser.getValidator();
            result = DocumentValidator.evaluate(o, e, dv, true);
            // try {
            // result = OclEvaluator.evaluateQuery(e, o);
            // } catch ( ParserException e1 ) {
            // // TODO Auto-generated catch block
            // e1.printStackTrace();
            // }
            resultMap.put(e, result);
            if (!isAllNumbers) {
                continue;
            }
            Collection<?> coll = null;
            if (result instanceof Collection) {
                coll = ((Collection<?>) result);
            }
            if (coll != null && coll.size() > 0) {
                isAllNumbers = false;
                continue;
            }
            if (coll != null) {
                List<Object> numbers = new ArrayList<Object>();
                for (Object c : coll) {
                    String s = DocGenUtils.fixString(c);
                    if (isAllNumbers) {
                        if (Utils2.isNumber(s)) {
                            numbers.add(Utils2.toDouble(s));
                        }
                        else {
                            isAllNumbers = false;
                            break;
                        }
                    }
                }
                if (isAllNumbers) {
                    resultNumberMap.put(e, numbers);
                }
            }
            else {
                String s = DocGenUtils.fixString(result);
                if (!Utils2.isNumber(s)) {
                    isAllNumbers = false;
                }
                else {
                    resultNumberMap.put(e, Utils2.toDouble(s));
                }

            }
        }
        if (isAllNumbers) {
            resultMap = resultNumberMap;
        }
        Collections.sort(list, new DocGenComparator(resultMap, isAllNumbers));
        return list;
    }

    public static class DocGenComparator implements Comparator<Object> {
        final boolean allNums;
        Map<Element, Object> resultMap = null;

        public DocGenComparator(boolean isAllNumbers) {
            allNums = isAllNumbers;
        }

        public DocGenComparator(Map<Element, Object> resultMap, boolean isAllNumbers) {
            this.resultMap = resultMap;
            allNums = isAllNumbers;
        }

        @Override
        public int compare(Object A, Object B) {
            Object resultA = resultMap == null ? A : resultMap.get(A);
            Object resultB = resultMap == null ? B : resultMap.get(B);
            return docgenCompare(resultA, resultB, allNums);
        }
    }

    private static int docgenCompare(Object a0, Object b0, boolean asNumbers) {
        if (a0 == b0) {
            return 0;
        }
        if (a0 == null) {
            return -1;
        }
        if (b0 == null) {
            return 1;
        }

        String as = DocGenUtils.fixString(a0);
        String bs = DocGenUtils.fixString(b0);
        if (as == bs) {
            return 0;
        }
        if (as == null) {
            return -1;
        }
        if (bs == null) {
            return 1;
        }
        if (asNumbers) {
            Double da0 = Utils2.toDouble(as);
            Double db0 = Utils2.toDouble(bs);
            return CompareUtils.compare(da0, db0, true);
        }
        else {
            return CompareUtils.compare(as, bs, true);
        }
    }

    /**
     * returns all directed relationships where client element is source and
     * supplier element is target
     *
     * @param source
     * @param target
     * @return
     */
    public static List<DirectedRelationship> findDirectedRelationshipsBetween(Element source, Element target) {
        List<DirectedRelationship> res = new ArrayList<DirectedRelationship>();
        for (DirectedRelationship dr : source.get_directedRelationshipOfSource()) {
            if (ModelHelper.getSupplierElement(dr) == target) {
                res.add(dr);
            }
        }
        return res;
    }

    /**
     * @param object   the object to test for a match
     * @param typeName regular expression String according to Pattern
     * @return whether the name of the object's Stereotype, EClass, or Java
     * class matches the typeName regular expression pattern
     */
    public static boolean isTypeOf(Object object, String typeName) {
        GetCallOperation op = new GetCallOperation(CallReturnType.TYPE, true, true);
        Object result = op.callOperation(object, new Object[]{typeName});
        if (result instanceof Collection && ((Collection<?>) result).size() == 1) {
            if (matches(((Collection<?>) result).iterator().next(), typeName)) {
                return true;
            }
        }
        return matches(result, typeName);
    }

    /**
     * See if the object matches the regular expression pattern by its name,
     * type, or string value.
     *
     * @param object  the object to test
     * @param pattern a Pattern regular expression string to match to the object
     * @return whether the name, type, or String value of the object matches the
     * regular expression pattern
     */
    private static boolean matches(Object object, String pattern) {
        // TODO -- types are limited to Stereotypes, EClasses, and Java Classes,
        // but metaclasses/base classes are left out here and in other methods
        // here that use regex patterns.
        String name = getName(object);
        if (EmfUtils.matches(name, pattern)) {
            return true;
        }
        boolean matched = EmfUtils.matches(object, pattern);
        return matched;
    }

    /**
     * @return the element at the top of the MagicDraw containment tree
     */
    public static Package getRootElement(Project project) {
        return project == null ? null : project.getPrimaryModel();
    }

    public static boolean isSiteChar(Project project, Package e) {
        Stereotype characterizes = Utils.getCharacterizesStereotype(project);
        if (characterizes != null) {
            List<Element> sites = Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, characterizes, 2, false, 1);
            boolean found = false;
            for (Element l : sites) {
                /*if (l instanceof NamedElement && ((NamedElement)l).getName().equals("Site Characterization")) {
                    found = true;
                    break;
                }*/
                if (l instanceof Classifier) {
                    for (Classifier g : ((Classifier) l).getGeneral()) {
                        if (g.getName().equals("Site Characterization")) {
                            found = true;
                            break;
                        }
                    }
                }
            }
            return found;
        }
        return false;
    }

    public static Map<Element, Map<String, Collection<Element>>> nameOrIdSearchOwnerCache =
            new HashMap<Element, Map<String, Collection<Element>>>();
    public static Map<String, Collection<Element>> nameOrIdSearchCache =
            new HashMap<String, Collection<Element>>();
    public static Map<String, Element> nameOrIdSingleElementSearchCache =
            new HashMap<String, Element>();
    public static Map<Element, Map<String, Element>> nameOrIdSingleElementSearchOwnerCache =
            new HashMap<Element, Map<String, Element>>();

    public static List<Element> findByName(Project project, String pattern, boolean getJustFirst) {
        // check cache
        if (getJustFirst) {
            Element e = nameOrIdSingleElementSearchCache.get(pattern);
            if (e != null) {
                return Utils2.newList(e);
            }
        }
        else {
            Collection<Element> res = nameOrIdSearchCache.get(pattern);
            return Utils2.asList(res);
        }

        // start at root and search recursively through children
        if (Utils2.isNullOrEmpty(pattern)) {
            return null;
        }
        Element root = getRootElement(project);
        List<Element> results = findByName(root, pattern, getJustFirst);
        nameOrIdSearchCache.put(pattern, results);
        return results;
    }

    public static List<Element> findByName(Element owner,
                                           String pattern,
                                           boolean getJustFirst) {
        // check cache
        if (getJustFirst) {
            Element e = Utils2.get(nameOrIdSingleElementSearchOwnerCache,
                    owner, pattern);
            if (e != null) {
                return Utils2.newList(e);
            }
        }
        else {
            Collection<Element> res =
                    Utils2.get(nameOrIdSearchOwnerCache, owner, pattern);
            return Utils2.asList(res);
        }

        // check immediate children
        List<Element> results = new ArrayList<Element>();
        for (Element e : owner.getOwnedElement()) {
            if (e instanceof NamedElement) {
                Pattern p = Pattern.compile(pattern);
                Matcher matcher = p.matcher(((NamedElement) e).getName());
                if (matcher.matches()) {
                    results.add(e);
//                    Utils2.add( nameOrIdSearchOwnerCache, owner, pattern, e );
                    if (getJustFirst) {
                        nameOrIdSingleElementSearchCache.put(pattern, e);
                        return results;
                    }
                }
            }
        }
        // check children of children
        for (Element e : owner.getOwnedElement()) {
            results.addAll(findByName(e, pattern, getJustFirst));
            if (getJustFirst && results.size() > 0) {
                Utils2.put(nameOrIdSingleElementSearchOwnerCache, owner, pattern, e);
                return results;
            }
        }
        Utils2.put(nameOrIdSearchOwnerCache, owner, pattern, results);
        return results;
    }

    @Deprecated
    public static Stereotype getStereotype(Project project, String stereotypeName) {
        return StereotypesHelper.getStereotype(project, stereotypeName);
    }

    public static Stereotype getConformStereotype(Project project) {
        return (Stereotype) project.getElementByID("_11_5EAPbeta_be00301_1147420728091_674481_152");
    }

    public static Stereotype getExposeStereotype(Project project) {
        return (Stereotype) project.getElementByID("_17_0_5beta_17530432_1382587480303_325976_12505");
    }

    public static Stereotype getElementGroupStereotype(Project project) {
        return (Stereotype) project.getElementByID("_17_0_5beta_17530432_1382588727729_600191_12925");
    }

    public static Stereotype getViewStereotype(Project project) {
        return (Stereotype) project.getElementByID("_11_5EAPbeta_be00301_1147420760998_43940_227");
    }

    public static Stereotype getViewpointStereotype(Project project) {
        return (Stereotype) project.getElementByID("_11_5EAPbeta_be00301_1147420812402_281263_364");
    }

    public static Stereotype getAspectStereotype(Project project) {
        return (Stereotype) project.getElementByID("_18_0_2_407019f_1449688347122_736579_14412");
    }

    public static Stereotype getCharacterizesStereotype(Project project) {
        return (Stereotype) project.getElementByID("_17_0_5_1_8660276_1407362513794_939259_26181");
    }

    public static Stereotype getDocumentStereotype(Project project) {
        return (Stereotype) project.getElementByID("_17_0_2_3_87b0275_1371477871400_792964_43374");
    }

    public static Stereotype getProductStereotype(Project project) {
        return (Stereotype) project.getElementByID("_17_0_1_407019f_1326996604350_494231_11646");
    }

    public static Stereotype getExpressionStereotype(Project project) {
        return (Stereotype) project.getElementByID("_17_0_2_3_e9f034d_1382549095816_841656_29288");
    }

    @Deprecated
    public static Stereotype getViewClassStereotype(Project project) {
        return (Stereotype) project.getElementByID("_17_0_1_232f03dc_1325612611695_581988_21583");
    }

    public static Property getGeneratedFromViewProperty(Project project) {
        return (Property) project.getElementByID("_17_0_5_1_407019f_1430628276506_565_12080");
    }

    public static Property getGeneratedFromElementProperty(Project project) {
        return (Property) project.getElementByID("_17_0_5_1_407019f_1430628376067_525763_12104");
    }

    public static Property getViewElementsProperty(Project project) {
        return (Property) project.getElementByID("_18_0_2_407019f_1433361787467_278914_14410");
    }

    public static Component getSiteCharacterizationComponent(Project project) {
        return (Component) project.getElementByID("_17_0_5_1_8660276_1415063844134_132446_18688");
    }

    /**********************************************
     * Constraint Utils
     **********************************************/
    public static Constraint getViewConstraint(Element view) {
        if (view != null) {
            Collection<Constraint> constraints = view.get_constraintOfConstrainedElement();
            for (Constraint constraint : constraints) {
                if (constraint != null && Converters.getElementToIdConverter().apply(constraint).endsWith((MDKConstants.VIEW_CONSTRAINT_SYSML_ID_SUFFIX))) {
                    return constraint;
                }
            }

        }
        return null;
    }

    public static Constraint getWarningConstraint(Project project) {
        return (Constraint) project.getElementByID("_17_0_2_2_f4a035d_1360957024690_702520_27755");
    }

    public static Constraint getErrorConstraint(Project project) {
        return (Constraint) project.getElementByID("_17_0_2_407019f_1354058024392_224770_12910");
    }

    public static Constraint getFatalConstraint(Project project) {
        return (Constraint) project.getElementByID("_17_0_2_2_f4a035d_1360957445325_901851_27756");
    }

    public static Constraint getInfoConstraint(Project project) {
        return (Constraint) project.getElementByID("_17_0_2_2_f4a035d_1360957474351_901777_27765");
    }

    /********************************************* User interaction ****************************************************/

    /**
     * Log to GUILog in UI's event dispatcher
     */
    public static void guilog(final String s) {
        Application.getInstance().getGUILog().log(s, !s.startsWith("[INFO]"));
    }

    /**
     * @param types
     * @param title title of the dialog box
     * @return
     */
    public static BaseElement getUserSelection(List<java.lang.Class<?>> types, String title) {
        SelectElementTypes a = new SelectElementTypes(null, types);
        SelectElementInfo b = new SelectElementInfo(false, false, Application.getInstance().getProject().getPrimaryModel(), true);
        Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
        ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
        ElementSelectionDlgFactory.initSingle(dlg, a, b, null);
        dlg.setTitle(title);
        dlg.show();
        if (dlg.isOkClicked()) {
            return dlg.getSelectedElement();
        }
        return null;
    }

    public static void showPopupMessage(String message) {
        if (popupsDisabled || MainFrame.isSilentMode()) {
            Utils.guilog("[POPUP] " + message);
        }
        else {
            JOptionPane.showMessageDialog(Application.getInstance().getMainFrame(), message);
        }
    }

    public static Boolean getUserYesNoAnswerWithButton(String question, String[] buttons, boolean includeCancel) {
        if (forceDialogFalse) {
            forceDialogFalse = false;
            return false;
        }
        if (forceDialogTrue) {
            forceDialogTrue = false;
            return true;
        }
        if (forceDialogCancel) {
            forceDialogCancel = false;
            return null;
        }
        int option = includeCancel ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION;
        int res = JOptionPane.showOptionDialog(Application.getInstance().getMainFrame(), question, "Choose", option, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[0]);
        if (res == JOptionPane.YES_OPTION) {
            return true;
        }
        else if (res == JOptionPane.NO_OPTION) {
            return false;
        }
        return null;
    }

    /**
     * @param question
     * @return null if user hits cancel
     */
    public static Boolean getUserYesNoAnswer(String question) {
        if (forceDialogFalse) {
            forceDialogFalse = false;
            return false;
        }
        if (forceDialogTrue) {
            forceDialogTrue = false;
            return true;
        }
        if (forceDialogCancel) {
            forceDialogCancel = false;
            return null;
        }
        int res = JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(), question);
        if (res == JOptionPane.YES_OPTION) {
            return true;
        }
        else if (res == JOptionPane.NO_OPTION) {
            return false;
        }
        return null;
    }

    public static Set<Annotation> getAnnotations(ValidationRule vr, Project project, Constraint cons) {
        Set<Annotation> annotations = new LinkedHashSet<Annotation>();
        List<RuleViolationResult> results = getRuleViolations(vr, project, cons);
        for (RuleViolationResult violation : results) {
            annotations.add(violation.getAnnotation());
        }
        return annotations;
    }

    public static List<RuleViolationResult> getRuleViolations(ValidationRule vr, Project project, Constraint cons) {
        List<RuleViolationResult> results = new ArrayList<RuleViolationResult>();

        EnumerationLiteral severity;
        /*
         * Switch added by Peter Di Pasquale 02/15/2013 Changelog: switch
         * statement added, selects element highlight and icon color according
         * to severity of error.
         */

        switch (vr.getSeverity()) {
            case WARNING:
                severity = Annotation.getSeverityLevel(project, Annotation.WARNING);
                cons = Utils.getWarningConstraint(project);
                break;
            case ERROR:
                severity = Annotation.getSeverityLevel(project, Annotation.ERROR);
                cons = Utils.getErrorConstraint(project);
                break;
            case FATAL:
                severity = Annotation.getSeverityLevel(project, Annotation.FATAL);
                cons = Utils.getFatalConstraint(project);
                break;
            case INFO:
                severity = Annotation.getSeverityLevel(project, Annotation.INFO);
                cons = Utils.getInfoConstraint(project);
                break;
            default:
                severity = Annotation.getSeverityLevel(project, Annotation.WARNING);
                break;
        }
        for (ValidationRuleViolation vrv : vr.getViolations()) {
            Annotation anno;
            if (vrv.getActions() != null && vrv.getActions().size() > 0) {
                anno = new Annotation(severity, vr.getName(), vrv.getComment(), vrv.getElement(), vrv.getActions());
                for (NMAction action : vrv.getActions()) {
                    if (action instanceof IRuleViolationAction) {
                        ((IRuleViolationAction) action).setAnnotation(anno);
                    }
                }
            }
            else {
                anno = new Annotation(severity, vr.getName(), vrv.getComment(), vrv.getElement());
            }
            RuleViolationResult rvr = new RuleViolationResult(anno, cons);
            results.add(rvr);
            if (vrv.getActions() != null && vrv.getActions().size() > 0) {
                for (NMAction action : vrv.getActions()) {
                    if (action instanceof IRuleViolationAction) {
                        ((IRuleViolationAction) action).setRuleViolationResult(rvr);
                    }
                }
            }

        }
        return results;
    }

    public static List<RuleViolationResult> getRuleViolations(Project project, Collection<ValidationSuite> vss) {
        List<RuleViolationResult> results = new ArrayList<>();
        Constraint cons = Utils.getWarningConstraint(project);
        for (ValidationSuite vs : vss) {
            for (ValidationRule vr : vs.getValidationRules()) {
                results.addAll(getRuleViolations(vr, project, cons));
            }
        }

        return results;
    }

    public static void displayValidationWindow(Project project, ValidationSuite vs, String title) {
        final List<ValidationSuite> vss = new ArrayList<>();
        vss.add(vs);
        displayValidationWindow(project, vss, title);
    }

    public static void displayValidationWindow(Project project, Collection<ValidationSuite> vss, String title) {
        List<RuleViolationResult> results = getRuleViolations(project, vss);
        Set<Element> elements = new LinkedHashSet<>();
        for (RuleViolationResult violation : results) {
            elements.add((Element) violation.getElement());
        }

        Package dummyvs = (Package) project.getElementByID("_17_0_2_407019f_1354124289134_280378_12909");
        // Auto-mounting proof of concept (untested)
        /*
        if (dummyvs == null) {
            try {
                ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
                File file = new File(Application.environment().getProfilesDirectory() + "SysML Extensions.mdxml");
                ProjectDescriptor projectDescriptor =
                        ProjectDescriptorsFactory.createProjectDescriptor(file.toURI());
                projectsManager.useModule(project, projectDescriptor);
                dummyvs = (Package) project.getElementByID("_17_0_2_407019f_1354124289134_280378_12909");
            } catch(Exception e){
                // didnt work but was worth a try.
            }
        }
        */
        if (dummyvs == null) {
            Application.getInstance().getGUILog().log("You don't have SysML Extensions mounted! You need it in order for the validations to show.");
            return;
        }
        EnumerationLiteral severitylevel = Annotation.getSeverityLevel(project, Annotation.INFO);
        ValidationRunData runData = new ValidationRunData(dummyvs, false, elements, severitylevel);
        // TODO @donbot change the id here from ms to the action name - this will cause windows to be reused
        String id = "" + System.currentTimeMillis();
        Map<Annotation, RuleViolationResult> mapping = new HashMap<>();
        ValidationWindowRun vwr = new ValidationWindowRun(id, title, runData, results, mapping);
        for (RuleViolationResult rvr : results) {
            for (NMAction action : rvr.getAnnotation().getActions()) {
                if (action instanceof IRuleViolationAction) {
                    ((IRuleViolationAction) action).setValidationWindowRun(vwr);
                }
            }
            mapping.put(rvr.getAnnotation(), rvr);
        }
        ValidationResultsWindowManager.updateValidationResultsWindow(id, title, runData, results);
    }

    /************************************************ Model Modification **************************************/

    /**
     * Copies all stereotypes of element a to element b if b doesn't already
     * have it (including derived)
     *
     * @param a
     * @param b
     */
    public static void copyStereotypes(Element a, Element b) {
        for (Stereotype s : StereotypesHelper.getStereotypes(a)) {
            if (!StereotypesHelper.hasStereotypeOrDerived(b, s)) {
                StereotypesHelper.addStereotype(b, s);
            }
        }
    }

    /**
     * Creates a generalization relationship between parent and child
     *
     * @param parent
     * @param child
     */
    public static void createGeneralization(Classifier parent, Classifier child) {
        Generalization g = Project.getProject(parent).getElementsFactory().createGeneralizationInstance();
        ModelHelper.setClientElement(g, child);
        ModelHelper.setSupplierElement(g, parent);
        g.setOwner(child);
    }

    private static void setOwnerPackage(Element child, Element parent) {
        while (!(parent instanceof Package)) {
            parent = parent.getOwner();
        }
        child.setOwner(parent);
    }

    public static void createDependencyWithStereotype(Element from, Element to, Stereotype s) {
        Dependency d = Project.getProject(from).getElementsFactory().createDependencyInstance();
        ModelHelper.setClientElement(d, from);
        ModelHelper.setSupplierElement(d, to);
        StereotypesHelper.addStereotype(d, s);
        setOwnerPackage(d, from);
    }

    /**
     * This will set the default value of p to value, based on what type the
     * default value currently is right now, it'll try to convert to:
     * LiteralBoolean, LiteralInteger, LiteralUnlimitedNatural, otherwise it'll
     * be a LiteralString more options possibly in future like durations, etc
     *
     * @param p
     * @param value
     */
    public static void setPropertyValue(Property p, String value) {
        Project project = Project.getProject(p);
        ValueSpecification valueSpec = p.getDefaultValue();
        ValueSpecification v = makeValueSpecification(project, value, valueSpec);
        p.setDefaultValue(v);
    }

    /**
     * This will set the default value of p to value, based on what type the
     * default value currently is right now, it'll try to convert to:
     * LiteralBoolean, LiteralInteger, LiteralUnlimitedNatural, otherwise it'll
     * be a LiteralString more options possibly in future like durations, etc
     *
     * @param slot
     * @param value
     */
    public static void setSlotValue(Slot slot, Object value) {
        Project project = Project.getProject(slot);
        List<ValueSpecification> valueSpecs = slot.getValue();
        ValueSpecification v = null;
        if (value instanceof ValueSpecification) {
            v = (ValueSpecification) value;
        }
        else {
            for (ValueSpecification valueSpec : valueSpecs) {
                v = makeValueSpecification(project, value.toString(), valueSpec);
                break;
            }
        }
        valueSpecs.clear();
        valueSpecs.add(v);
    }


    /**
     * Creates a new {@link ValueSpecification} of the same type as valueSpec
     * but with a new value to be parsed from a {@link String}. It'll try to
     * convert to: LiteralBoolean, LiteralInteger, LiteralUnlimitedNatural,
     * otherwise it'll be a LiteralString more options possibly in future like
     * durations, etc
     *
     * @param value
     * @param valueSpec
     * @return
     */
    public static ValueSpecification makeValueSpecification(Project project, String value, ValueSpecification valueSpec) {
        ElementsFactory ef = project.getElementsFactory();
        ValueSpecification v;
        try {
            if (valueSpec instanceof LiteralBoolean) {
                v = ef.createLiteralBooleanInstance();
                if (value.equals("false") || value.equals("False") || value.equals("F") || value.equals("f")
                        || value.equals("no") || value.equals("n") || value.isEmpty()
                        || value.equals("FALSE") || value.equals("NO") || value.equals("No")) {
                    ((LiteralBoolean) v).setValue(false);
                }
                else {
                    ((LiteralBoolean) v).setValue(true);
                }
            }
            else if (valueSpec instanceof LiteralInteger) {
                v = ef.createLiteralIntegerInstance();
                ((LiteralInteger) v).setValue(Integer.parseInt(value));
            }
            else if (valueSpec instanceof LiteralUnlimitedNatural) {
                v = ef.createLiteralUnlimitedNaturalInstance();
                ((LiteralUnlimitedNatural) v).setValue(Integer.parseInt(value));
            }
            else if (valueSpec instanceof LiteralReal) {
                v = ef.createLiteralRealInstance();
                ((LiteralReal) v).setValue(Double.parseDouble(value));
            }
            else {
                v = ef.createLiteralStringInstance();
                ((LiteralString) v).setValue(value);
            }
        } catch (NumberFormatException e) {
            // The field is no longer a number; treat it as a string
            // REVIEW -- This may conflict with the element's type. Should we
            // change the type of the element?
            // TODO -- Only some of the ValueSpecifications are supported here.
            // The
            // subclasses of ValueSpecification and supportive factory methods
            // in
            // ElementFactory can guide the addition of types.
            v = makeValueSpecification(project, value, ef.createLiteralStringInstance());
        }

        return v;
    }

    /*************************
     * Getting element attributes and properties/values
     ****************************/

    public enum AvailableAttribute {
        Name, Documentation, Value
    }

    /**
     * Convert to an From enum value
     *
     * @param attr the attribute of some type
     * @return the corresponding From value
     */
    public static From getFromAttribute(Object attr) {
        if (attr instanceof From) {
            return (From) attr;
        }
        if (attr instanceof AvailableAttribute) {
            AvailableAttribute aattr = (AvailableAttribute) attr;
            switch (aattr) {
                case Name:
                    return From.NAME;
                case Documentation:
                    return From.DOCUMENTATION;
                case Value:
                    return From.DVALUE;
                default:
            }
        }
        if (attr instanceof EnumerationLiteral) {
            return getFromAttribute(((EnumerationLiteral) attr).getName());
        }
        From f = null;
        if (attr instanceof String) {
            try {
                f = getFromAttribute(From.valueOf((String) attr));
            } catch (Exception e) {
            }
            try {
                if (f == null) {
                    f = getFromAttribute(AvailableAttribute.valueOf((String) attr));
                }
            } catch (Exception e) {
            }
        }
        if (f == null) {
            Debug.error(false, "Unexpected argument " + attr + " to getFromAttribute().");
        }
        return f;
    }

    /**
     * Convert the input Object to an availableAttribute enum value. Supported
     * attribute objects include AvailableAttribute, From, EnumerationLiteral,
     * and String.
     *
     * @param attr the attribute of some type
     * @return the corresponding AvailableAttribute
     */
    public static AvailableAttribute getAvailableAttribute(Object attr) {
        if (attr instanceof AvailableAttribute) {
            return (AvailableAttribute) attr;
        }
        if (attr instanceof From) {
            From fattr = (From) attr;
            switch (fattr) {
                case NAME:
                    return AvailableAttribute.Name;
                case DOCUMENTATION:
                    return AvailableAttribute.Documentation;
                case DVALUE:
                    return AvailableAttribute.Value;
                default:
            }
        }
        if (attr instanceof EnumerationLiteral) {
            return getAvailableAttribute(((EnumerationLiteral) attr).getName());
        }
        AvailableAttribute aattr = null;
        if (attr instanceof String) {
            try {
                aattr = getAvailableAttribute(AvailableAttribute.valueOf((String) attr));
            } catch (Exception e) {
            }
            try {
                if (aattr == null) {
                    aattr = getAvailableAttribute(From.valueOf((String) attr));
                }
            } catch (Exception e) {
            }
        }
        if (aattr == null) {
            Debug.error(false, "Unexpected argument " + attr + " to getFromAttribute().");
        }
        return aattr;
    }

    /**
     * Returns an attribute of the element based on the input availableAttribute
     * type.
     *
     * @param elem the element whose attribute is sought
     * @param attr the type of attribute (name, value, ...)
     * @return possible return values are String for name or documentation,
     * ValueSpec, or List of ValueSpec
     */
    public static Object getElementAttribute(Element elem, AvailableAttribute attr) {
        switch (attr) {
            case Name:
                if (elem instanceof NamedElement) {
                    return ((NamedElement) elem).getName();
                }
                else {
                    return elem.getHumanName();
                }
            case Documentation:
                return ModelHelper.getComment(elem);
            case Value:
                if (elem instanceof Property) {
                    return ((Property) elem).getDefaultValue();
                }
                else if (elem instanceof Slot) {
                    return ((Slot) elem).getValue();
                }
                else if (elem instanceof Constraint) {
                    return ((Constraint) elem).getSpecification();
                }
            default:
                return null;
        }
    }

    /**
     * Get Class Properties with a name matching that of the input Property.
     *
     * @param elem      the owner of the Property
     * @param prop      a property of the same name as that owned by the input Element
     * @param inherited ignored for now, should indicate whether to look for inherited
     *                  properties
     * @return the Properties of the input Element whose names match that of the
     * input Property
     */
    public static Property getClassProperty(Element elem, Property prop, boolean inherited) {
        if (prop == null) {
            return null;
        }
        Collection<Element> rOwned = elem.getOwnedElement();
        for (Element o : rOwned) {
            if (o instanceof Property && ((Property) o).getName().equals(prop.getName())) {
                return (Property) o;
            }
        }
        return null;
    }

    /**
     * Get the element's matching Slot.
     *
     * @param elem the source Element
     * @param prop the Stereotype tag that the Slot instantiates
     * @return a Slot with zero or more values or null if no such Slot exists
     */
    public static Slot getSlot(Element elem, Property prop) {
        if (prop == null || elem == null) {
            return null;
        }
        Element myOwner = prop.getOwner();
        if (myOwner instanceof Stereotype
                && StereotypesHelper.hasStereotypeOrDerived(elem, (Stereotype) myOwner)) { // REVIEW
            // may not be able to get slot from derived stereotype
            Slot slot = StereotypesHelper.getSlot(elem, prop, false);
            if (slot != null) {
                return slot;
            }
        }
        return null;
    }

    /**
     * @param elem
     * @return all slots for the element's applied stereotype instance
     */
    public static List<Slot> getSlots(Element elem) {
        List<Slot> slots = new ArrayList<Slot>();
        InstanceSpecification localInstanceSpecification = elem.getAppliedStereotypeInstance();
        if (localInstanceSpecification != null) {
            slots.addAll(localInstanceSpecification.getSlot());
        }
        return slots;
    }

    /**
     * Get the element's matching Slots or Properties.
     *
     * @param elem the Element with the sought Properties.
     * @param prop the Stereotype tag or Class Property
     * @return a slot that matches the input property or null
     */
    public static Slot getStereotypeProperty(Element elem, Property prop) {
        return getSlot(elem, prop);
    }

    public static Property getClassProperty(Element elem, String propName,
                                            boolean inherited) {
        for (Element e : elem.getOwnedElement()) {
            if (e instanceof Property) {
                if (((Property) e).getName().equals(propName)) {
                    return (Property) e;
                }
            }
        }
        return null;
    }

    public static Property getElementStereotypeProperty(Element elem,
                                                        String propName) {
        List<Stereotype> stereotypes = StereotypesHelper.getStereotypes(elem);
        for (Stereotype stereotype : stereotypes) {
            Property prop = getStereotypePropertyByName(stereotype, propName);
            if (prop != null) {
                return prop;
            }
        }
        return null;
    }

    /**
     * Get the property of the stereotype by name.
     *
     * @param stereotype
     * @param propName
     * @return
     */
    public static Property getStereotypePropertyByName(Stereotype stereotype,
                                                       String propName) {
        return StereotypesHelper.getPropertyByName(stereotype, propName);
    }

    /**
     * Get the element's matching Slot or Properties.
     *
     * @param elem the Element with the sought Properties.
     * @param prop the Stereotype tag or Class Property
     * @return a Property or Slot that corresponds to the input property
     */
    public static Element getElementProperty(Element elem, Property prop) {
        if (prop == null) {
            return null;
        }
        if (prop.getOwner() instanceof Stereotype) {
            Slot slot = getStereotypeProperty(elem, prop);
            if (slot != null) {
                return slot;
            }
        }
        else {
            Property result = getClassProperty(elem, prop, true);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * A list of property values will always be returned. Gets default value of
     * a stereotype property when there's no slot for the element. Class value
     * properties will be collected by name matching.
     *
     * @param elem the owner of the Property
     * @param prop a property of the same name as that owned by the input Element
     * @return values for Properties with a name matching that of the input
     * Property
     */
    public static List<Object> getElementPropertyValues(Element elem, Property prop,
                                                        boolean allowStereotypeDefaultOrInherited) {
        if (elem == null) {
            return Collections.emptyList();
        }
        if (prop == null) {
            return Collections.emptyList();
        }
        List<Object> results = getStereotypePropertyValues(elem, prop, allowStereotypeDefaultOrInherited);
        if (!Utils2.isNullOrEmpty(results)) {
            return results;
        }
        ValueSpecification vs = getClassPropertyValue(elem, prop, allowStereotypeDefaultOrInherited);
        if (vs != null) {
            results.add(vs);
        }
        if (results.isEmpty()) {
            try {
                Object value = elem.refGetValue(prop.getName()); // i think this
                // only works
                // for derived
                // properties
                if (value != null) {
                    if (value instanceof Collection) {
                        results.addAll((Collection<?>) value);
                    }
                    else {
                        results.add(value);
                    }
                }
            } catch (Throwable e) { // ignore
            }
        }
        return results;
    }

    public static List<Object> getElementPropertyValues(Element elem,
                                                        String propName,
                                                        boolean allowStereotypeDefaultOrInherited) {
        Property prop = getClassProperty(elem, propName, allowStereotypeDefaultOrInherited);
        //return Utils2.newList( (Object)getClassPropertyValue( elem, prop, allowStereotypeDefaultOrInherited ) );
        if (prop == null) {
            prop = getElementStereotypeProperty(elem, propName);
        }
        return getElementPropertyValues(elem, prop, allowStereotypeDefaultOrInherited);
    }

    /**
     * Get Class Property values for Properties with a name matching that of the
     * input Property.
     *
     * @param elem the owner of the Property
     * @param prop a property of the same name as that owned by the input Element
     * @return values for Properties with a name matching that of the input
     * Property
     */
    public static ValueSpecification getClassPropertyValue(Element elem, Property prop,
                                                           boolean includeInherited) {
        Property cprop = getClassProperty(elem, prop, includeInherited);
        if (cprop == null) {
            return null;
        }
        return cprop.getDefaultValue();
    }

    /**
     * Gets list of values for a stereotype property, this returns valuespecs
     *
     * @param elem
     * @param prop
     * @param useDefaultIfNoSlot
     * @return
     */
    public static List<Object> getStereotypePropertyValues(Element elem, Property prop,
                                                           boolean useDefaultIfNoSlot) {
        List<Object> results = new ArrayList<Object>();
        Slot elemProp = getStereotypeProperty(elem, prop);
        if (elemProp != null) {
            if (elemProp.getValue() != null) {
                results.addAll(elemProp.getValue());
            }
        }
        Element propOwner = prop.getOwner();
        if (useDefaultIfNoSlot && results.isEmpty() && prop != null) {
            if (propOwner instanceof Stereotype
                    && StereotypesHelper.hasStereotypeOrDerived(elem, (Stereotype) propOwner)) {
                ValueSpecification v = prop.getDefaultValue();
                if (v != null) {
                    results.add(v);
                }
            }
        }
        return results;
    }

    /*****************************************************************************************/

    public static void log(Object o) {
        if (o == null) {
            o = "null";
        }
        log(o, (Color) null);
    }

    /**
     * Get the Color by name or by RGB specification.
     *
     * @param colorString
     * @return the Color constant with the same name as the input (forgiving
     * capitalization, hyphens, underscores, etc.) or the RGB Color
     * specified as 3 comma separated integers
     */
    public static Color toColor(String colorString) {
        if (Utils2.isNullOrEmpty(colorString)) {
            return null;
        }

        Color color = null;

        // try to find a field in the Color class that has the same name
        Field f;
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add(colorString);
        String colorNameLetters = colorString.replaceAll("[^A-Za-z]*", "");
        set.add(colorNameLetters);
        set.add(colorString.toLowerCase());
        set.add(colorString.toUpperCase());
        set.add(colorNameLetters.toLowerCase());
        set.add(colorNameLetters.toUpperCase());
        for (String cName : set) {
            try {
                f = Color.class.getField(cName);
                if (f != null) {
                    color = (Color) f.get(null);
                    if (color != null) {
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (SecurityException e) {
            } catch (NoSuchFieldException e) {
            }
        }

        // try to parse RGB integers separated by commas
        if (color == null) {
            Pattern p = Pattern.compile("[^0-9]*([0-9]+), *([0-9]+), *([0-9]+).*");
            Matcher m = p.matcher(colorString);
            if (m.matches()) {
                if (m.groupCount() == 3) {
                    Integer r = Integer.valueOf(m.group(1));
                    Integer g = Integer.valueOf(m.group(2));
                    Integer b = Integer.valueOf(m.group(3));
                    color = new Color(r == null ? 0 : r,
                            g == null ? 0 : g,
                            b == null ? 0 : b);
                }
            }
        }

        return color;
    }

    public static boolean isColor(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Color) {
            return true;
        }
        return (toColor(o.toString()) != null);
    }

    public static void log(Object o, String colorName) {
        Color c = toColor(colorName);
        log(o, c);
    }

    public static void log(Object o, Color color) {
        if (o == null) {
            o = "null";
        }
        if (!(o instanceof String)) {
            // To handle arrays, etc.
            o = MoreToString.Helper.toString(o);
        }
        GUILog log = Application.getInstance().getGUILog();
        if (log != null) {
            log.log("" + o);
        }

        //MdDebug.logForce( o.toString(), true, false, color );
    }

    public static void log(Object o, Object color) {
        if (color == null || color instanceof Color) {
            log(o, (Color) color);
        }
        else {
            log(o, color.toString());
        }
    }


    /**
     * @param slot
     * @return the "represented text" for the slot values as a single String
     */
    public static String slotValueToString(Slot slot) {
        List<ValueSpecification> list = slot.getValue();
        List<String> sList = new ArrayList<String>();
        String s = ""; // if slot is empty or has nothing, show as empty string
        // If there is only one element in the list, just use that element
        // instead
        // of the list.
        for (ValueSpecification vs : list) {
            s = RepresentationTextCreator.getRepresentedText(vs);
            sList.add(s);
        }
        if (sList != null && !sList.isEmpty()) {
            if (sList.size() == 1) {
                s = sList.get(0);
            }
            else {
                s = sList.toString();
            }
        }
        return s;
    }

    /**
     * Given a string f and integer p, if f is a floating point number, returns
     * the string with precision p
     * <p>
     * ex. if f is 2.31111111 and p is 2, returns 2.31
     *
     * @param f
     * @param p
     * @return
     */
    public static String floatTruncate(String f, int p) {
        if (p < 0) {
            return f;
        }
        if (f.indexOf(".") != -1) {
            try {
                double d = Double.parseDouble(f);
                String pattern = "#.";
                if (p == 0) {
                    pattern = "#";
                }
                for (int i = 0; i < p; i++) {
                    pattern += "#";
                }
                return new DecimalFormat(pattern).format(d);
                // return String.format("%." + Integer.toString(p) + "f", d);
            } catch (Exception e) {
                return f;
            }
        }
        return f;
    }

    /**
     * For user scripts, if you have a pop up table that should look the same as
     * the docgen output, you can just use this method to get back a DBTable to
     * pass back to the output.
     *
     * @param et
     * @return
     */
    public static DBTable getDBTableFromEditableTable(EditableTable et, boolean addLineNum) {
        EditableTableModel tm = (EditableTableModel) et.getTable().getModel();
        DBTable res = new DBTable();
        List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
        List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
        int cols = tm.getColumnCount();
        if (addLineNum) {
            cols++;
        }
        List<DocumentElement> header = new ArrayList<DocumentElement>();
        headers.add(header);
        if (addLineNum) {
            header.add(new DBText(""));
        }
        for (int i = 0; i < tm.getColumnCount(); i++) {
            header.add(new DBText(tm.getColumnName(i)));
        }
        int rows = tm.getRowCount();
        for (int i = 0; i < rows; i++) {
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            body.add(row);
            if (addLineNum) {
                row.add(new DBText(Integer.toString(i + 1)));
            }
            for (int j = 0; j < tm.getColumnCount(); j++) {
                PropertyEnum what = tm.getWhatToChangeAt(i, j);
                boolean editable = tm.isCellEditable(i, j);
                Object cell = tm.getObjectAt(i, j);
                if (cell instanceof Element && what != null && editable) {
                    if (what == PropertyEnum.NAME) {
                        row.add(new DBText(tm.getValueAt(i, j).toString(), (Element) cell, From.NAME));
                    }
                    else if (what == PropertyEnum.VALUE) {
                        row.add(new DBText(tm.getValueAt(i, j).toString(), (Element) cell, From.DVALUE));
                    }
                    else {
                        row.add(new DBParagraph(tm.getValueAt(i, j).toString(), (Element) cell,
                                From.DOCUMENTATION));
                    }
                }
                else {
                    row.add(new DBText(tm.getValueAt(i, j).toString()));
                }
            }
        }
        res.setBody(body);
        res.setHeaders(headers);
        res.setCols(cols);
        res.setTitle(et.getTitle());
        return res;
    }

    /**
     * For user scripts, if you have a pop up table, but you want to merge the
     * first x columns into a column with indentations (like properties table).
     * this assumes that for each merging, you have empty cells until the cell
     * to be indented.
     *
     * @param et    the editable table.
     * @param merge this is a variable length parameter, indicates how many
     *              columns you want to merge consecutively. For example, if you
     *              want to merge the first 2 columns and then the next 2
     *              columnes, do getDBTableFromEditableTable(et, 2, 2)
     * @return
     */
    public static DBTable getDBTableFromEditableTable(EditableTable et, boolean addLineNum, Integer... merge) {
        EditableTableModel tm = (EditableTableModel) et.getTable().getModel();
        DBTable res = new DBTable();
        List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
        List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
        int total = 0;
        int adds = 0;
        for (Integer i : merge) {
            total += i;
            adds++;
        }
        int cols = tm.getColumnCount() - total + adds;
        if (addLineNum) {
            cols++;
        }
        List<DocumentElement> header = new ArrayList<DocumentElement>();
        headers.add(header);
        if (addLineNum) {
            header.add(new DBText(""));
        }
        int curCol = 0;
        for (Integer i : merge) {
            header.add(new DBText(tm.getColumnName(curCol)));
            for (int j = 0; j < i; j++) {
                curCol++;
            }
        }
        for (int i = curCol; i < tm.getColumnCount(); i++) {
            header.add(new DBText(tm.getColumnName(i)));
        }
        int rows = tm.getRowCount();
        for (int row = 0; row < rows; row++) {
            List<DocumentElement> rowl = new ArrayList<DocumentElement>();
            body.add(rowl);
            if (addLineNum) {
                rowl.add(new DBText(Integer.toString(row + 1)));
            }
            curCol = 0;
            for (Integer i : merge) {
                String s = "";
                for (int j = 0; j < i; j++) {
                    Object val = tm.getValueAt(row, curCol);
                    if (val.toString().isEmpty()) {
                        s += "&#xA0;&#xA0;&#xA0;&#xA0;";
                        curCol++;
                    }
                    else {
                        s += DocGenUtils.fixString(val.toString());
                        for (int k = j; k < i; k++) {
                            curCol++;
                        }
                        break;
                    }
                }
                rowl.add(new DBText(s));
            }
            for (int j = curCol; j < tm.getColumnCount(); j++) {
                PropertyEnum what = tm.getWhatToChangeAt(row, j);
                boolean editable = tm.isCellEditable(row, j);
                Object cell = tm.getObjectAt(row, j);
                if (cell instanceof Element && what != null && editable) {
                    if (what == PropertyEnum.NAME) {
                        rowl.add(new DBText(tm.getValueAt(row, j).toString(), (Element) cell, From.NAME));
                    }
                    else if (what == PropertyEnum.VALUE) {
                        rowl.add(new DBText(tm.getValueAt(row, j).toString(), (Element) cell, From.DVALUE));
                    }
                    else {
                        rowl.add(new DBParagraph(tm.getValueAt(row, j).toString(), (Element) cell,
                                From.DOCUMENTATION));
                    }
                }
                else {
                    rowl.add(new DBText(tm.getValueAt(row, j).toString()));
                }
            }
        }
        res.setBody(body);
        res.setHeaders(headers);
        res.setCols(cols);
        res.setTitle(et.getTitle());
        return res;
    }

    public static final Pattern HTML_WRAPPER_START = Pattern.compile("^<html>.*<body>\\s*", Pattern.DOTALL);
    public static final Pattern HTML_WRAPPER_END = Pattern.compile("\\s*</body>.*</html>\\s*$",
            Pattern.DOTALL);

    /**
     * Remove HTML wrapper. This might include a head element or some amount of
     * whitespace.
     *
     * @param before
     * @return
     */
    public static String stripHtmlWrapper(String before) {
        if (before == null) {
            return null;
        }
        String startRemoved = HTML_WRAPPER_START.matcher(before).replaceAll("");
        return HTML_WRAPPER_END.matcher(startRemoved).replaceAll("");
    }

    /**
     * Given a collection of stuff, joins their string representation into one
     * string using delimiter
     *
     * @param s
     * @param delimiter
     * @return
     */
    public static String join(Collection<?> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    /**
     * @param obj
     * @return a name associated with this object whether a NameElement, an
     * Element with a humanName, an EObject with a name property, or a
     * Java Object with a name member.
     */
    public static String getName(Object obj) {
        if (obj instanceof NamedElement) {
            return ((NamedElement) obj).getName();
        }
        if (obj instanceof BaseElement) {
            String humanName = ((BaseElement) obj).getHumanName();
            String[] arr = humanName.trim().split(" ");
            if (arr != null) {
                if (arr.length == 2) {
                    if (!Utils2.isNullOrEmpty(arr[0]) && !Utils2.isNullOrEmpty(arr[1])) {
                        return arr[1];
                    }
                }
            }
            // REVIEW -- this seems like a bad place to be -- error messages?
        }
        return EmfUtils.getName(obj);
    }

    public static String getTypeName(Object obj) {
        if (obj instanceof BaseElement) {
            String humanType = ("" + ((BaseElement) obj).getHumanType()).trim();
            if (!Utils2.isNullOrEmpty(humanType)) {
                return humanType;
            }
            String humanName = ((BaseElement) obj).getHumanName();
            String[] arr = humanName.trim().split(" ");
            if (arr != null) {
                if (arr.length == 2) {
                    if (!Utils2.isNullOrEmpty(arr[0]) && !Utils2.isNullOrEmpty(arr[1])) {
                        return arr[0];
                    }
                }
            }
            // REVIEW -- this seems like a bad place to be -- error messages?
        }
        // try stereotype
        if (obj instanceof Element) {
            Element elem = (Element) obj;
            List<Stereotype> sTypes = StereotypesHelper.getStereotypes(elem);
            if (!Utils2.isNullOrEmpty(sTypes)) {
                for (Stereotype st : sTypes) {
                    String t = st.getName();
                    if (!Utils2.isNullOrEmpty(t)) {
                        return t;
                    }
                }
            }
        }
        return EmfUtils.getTypeName(obj);
    }

    public static String toStringNameAndType(final Object o, final boolean includeId,
                                             final boolean useToStringIfNull) {
        if (o == null) {
            return "null";
        }

        // if list, call recursively
        if (o instanceof Collection || o instanceof Map || o.getClass().isArray() || o instanceof Entry) {
            StringBuffer sb = new StringBuffer();
            sb.append("( ");
            boolean first = true;
            Collection<?> c = null;
            String sep = ", ";
            if (o instanceof Collection) {
                c = (Collection<?>) o;
            }
            else if (o instanceof Map) {
                c = ((Map<?, ?>) o).entrySet();
            }
            else if (o.getClass().isArray()) {
                c = Arrays.asList((Object[]) o);
            }
            else if (o instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) o;
                c = Utils2.newList(entry.getKey(), entry.getValue());
                sep = " = ";
            }
            // TODO -- avoid infinite recursion with Utils2.seen()
            for (Object i : c) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append(sep);
                }
                sb.append(toStringNameAndType(i, includeId, useToStringIfNull));
            }
            sb.append(" )");
            return sb.toString();
        }

        // Not a list
        String name = ("" + getName(o)).trim();
        String type = ("" + getTypeName(o)).trim();
        String s = name;
        if (!Utils2.isNullOrEmpty(name)) {
            if (!Utils2.isNullOrEmpty(type)) {
                s = s + ":" + type;
            }
        }
        else {
            s = type;
        }

        // TODO @donbot BaseElement doesn't have getLocalID(), confirm that this doesn't need to be migrated to getElementToIDConverter
        if (includeId && o instanceof BaseElement) {
            if (!Utils2.isNullOrEmpty(s)) {
                s = s + ":" + ((BaseElement) o).getID();
            }
            else {
                s = ((BaseElement) o).getID();
            }
        }
        if (useToStringIfNull && Utils2.isNullOrEmpty(s)) {
            s = MoreToString.Helper.toString(o);
        }
        return s;
    }

    /**
     * return names of a collection of named elements
     *
     * @param elements
     * @return
     */
    public static List<String> getElementNames(Collection<NamedElement> elements) {
        List<String> names = new ArrayList<String>();
        for (NamedElement e : elements) {
            names.add(e.getName());
        }
        return names;
    }

    /**
     * if multiplicity is not a range, returns the number if upper limit is
     * infinity and lower limit > 0, returns lower else returns 1
     *
     * @param p
     * @return
     */
    public static int getMultiplicity(Property p) {
        int lower = p.getLower();
        int upper = p.getUpper();
        if (lower == upper) {
            return lower;
        }
        if (upper == -1 && lower > 0) {
            return lower;
        }
        return 1;
    }

    public static void printException(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        Application.getInstance().getGUILog().log(sw.toString()); // stack trace as a string
        ex.printStackTrace();
    }

    public static boolean recommendUpdateFromRemote(Project project) {
        return recommendUpdateFromRemote(project, "");
    }

    public static boolean recommendUpdateFromRemote(Project project, String add) {
        if (forceDialogFalse) {
            forceDialogFalse = false;
            return false;
        }
        if (forceDialogTrue) {
            forceDialogTrue = false;
            return true;
        }
        if (!project.isRemote()) {
            return true;
        }
        String user = EsiUtils.getLoggedUserName();
        try {
            long lastVersion = EsiUtils.getLastVersion(ProjectDescriptorsFactory.createAnyRemoteProjectDescriptor(project));
            if (user == null || lastVersion < 0) {
                Utils.guilog("[ERROR] You must be logged into Teamwork Cloud first.");
                return false;
            }
            if (lastVersion == MDUtils.getRemoteVersion(project)) {
                return true;
            }
        } catch (Exception ex) {
            Utils.guilog("[ERROR] Unexpected exception occurred when trying to verify Teamwork Cloud state.");
            ex.printStackTrace();
            return false;
        }
        String[] buttons = {"Update", "Ignore"};
        Boolean reply = Utils.getUserYesNoAnswerWithButton("There is a new project version available on Teamwork Cloud.\nIt is highly recommended that you update before proceeding.\n"
                + add, buttons, false);
        if (reply == null || !reply) {
            return false;
        }
        LocalSyncTransactionCommitListener listener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
        if (listener != null) {
            listener.setDisabled(true);
        }
        EsiUtils.updateProject(project);
        if (listener != null) {
            listener.setDisabled(false);
        }
        return true;
    }

    /**
     * Sets boolean that can disabled popups and redirect their messages to the GUI log.
     *
     * @param disable true to redirect popups to gui log, false to renable normal popup behavior
     */
    public static void setPopupsDisabled(boolean disable) {
        Utils.popupsDisabled = disable;
    }

    public static boolean isPopupsDisabled() {
        return Utils.popupsDisabled || MainFrame.isSilentMode();
    }

    /**
     * Causes the next call of a user dialog generating method to return the indicated value
     * The called method should then reset these values, so they don't accidentally get used again
     */

    public static void forceDialogReturnFalse() {
        Utils.forceDialogFalse = true;
    }

    /**
     * Resets all unconsumed forced return values
     */
    public static void resetForcedReturns() {
        Utils.forceDialogTrue = false;
        Utils.forceDialogFalse = false;
        Utils.forceDialogCancel = false;
    }
}
