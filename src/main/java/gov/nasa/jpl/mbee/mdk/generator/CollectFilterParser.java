package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallOperationAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.DecisionNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ForkNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.JoinNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.MergeNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.generator.graphs.DirectedEdgeVector;
import gov.nasa.jpl.mbee.mdk.generator.graphs.DirectedGraphHashSet;
import gov.nasa.jpl.mbee.mdk.generator.graphs.algorithms.TopologicalSort;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.ScriptRunner;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Utils2;
import org.apache.log4j.Logger;

import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class CollectFilterParser {

    private static GenerationContext context;
    private static Logger log = Logger.getLogger(CollectFilterParser.class);

    public static void setContext(GenerationContext gc) {
        context = gc;
    }

    public static GenerationContext getContext() {
        return context;
    }

    public static DocumentValidator getValidator() {
        return context == null ? null : context.getValidator();
    }

    /**
     * gets a graph of the collect/filter actions that starts from a, evaluates
     * and executes actions topologically and return result
     *
     * @param a
     * @param in
     * @return
     */
    public static List<Element> startCollectAndFilterSequence(ActivityNode a, List<Element> in) {
        DirectedGraphHashSet<CollectFilterNode, DirectedEdgeVector<CollectFilterNode>> graph = new DirectedGraphHashSet<CollectFilterNode, DirectedEdgeVector<CollectFilterNode>>();
        getCollectFilterGraph(a, new HashSet<ActivityNode>(), graph,
                new HashMap<ActivityNode, CollectFilterNode>());
        SortedSet<CollectFilterNode> reverse = (new TopologicalSort()).topological_sort(graph);
        List<CollectFilterNode> toposort = new ArrayList<CollectFilterNode>(reverse);
        Collections.reverse(toposort);

        List<Element> res = in;
        for (CollectFilterNode node : toposort) {
            Set<CollectFilterNode> incomings = new HashSet<CollectFilterNode>();
            for (DirectedEdgeVector<CollectFilterNode> edge : graph.findEdgesWithTargetVertex(node)) {
                incomings.add(edge.getSourceVertex());
            }
            Set<List<Element>> ins = new HashSet<List<Element>>();
            if (incomings.isEmpty()) {
                if (in == null) {
                    if (!context.targetsEmpty()) {
                        ins.add(Utils2.asList(context.peekTargets(), Element.class));
                    }
                }
                else {
                    ins.add(in);
                }
            }
            else {
                for (CollectFilterNode i : incomings) {
                    ins.add(i.getResult());
                }
            }
            if (node.getNode() instanceof CallBehaviorAction) {
                if (ins.size() == 1) {
                    res = collectAndFilter((CallBehaviorAction) node.getNode(), ins.iterator().next());
                    /*System.out.println( "collectAndFilter(): returned (after removing duplicates) res["
                            + res.size() + "]="
                            + MoreToString.Helper.toLongString( res ) );*/
                    node.setResult(res);
                }
                else {
                    // ???
                    res = new ArrayList<Element>();
                    node.setResult(res);
                }
            }
            else if (node.getNode() instanceof ForkNode) {
                if (ins.size() == 1) {
                    res = ins.iterator().next();
                    node.setResult(res);
                }
                else {
                    res = new ArrayList<Element>();
                    node.setResult(res);
                    // ???
                }
            }
            else if (node.getNode() instanceof MergeNode) {
                res = Utils.unionOfCollections(ins);
                node.setResult(res);
            }
            else if (node.getNode() instanceof JoinNode) {
                res = Utils.intersectionOfCollections(ins);
                node.setResult(res);
            }
            else if (node.getNode() instanceof DecisionNode) {
                res = Utils.xorOfCollections(ins);
                node.setResult(res);
            }
            context.setCurrentNode(node.getNode());
            DocumentValidator.evaluateConstraints(node.getNode(), res, context, true, true);
        }
        return res;
    }

    private static void getCollectFilterGraph(ActivityNode cur, Set<ActivityNode> done,
                                              DirectedGraphHashSet<CollectFilterNode, DirectedEdgeVector<CollectFilterNode>> graph,
                                              Map<ActivityNode, CollectFilterNode> mapping) {
        if (done.contains(cur)) {
            return;
        }
        done.add(cur);
        CollectFilterNode source = mapping.get(cur);
        if (source == null) {
            source = new CollectFilterNode(cur);
            mapping.put(cur, source);
        }
        graph.addVertex(source);
        for (ActivityEdge e : cur.getOutgoing()) {
            ActivityNode n = e.getTarget();
            if (GeneratorUtils.hasStereotypeByString(n, DocGenProfile.collectFilterStereotype, true)) {
                CollectFilterNode target = mapping.get(n);
                if (target == null) {
                    target = new CollectFilterNode(n);
                    mapping.put(n, target);
                }
                graph.addEdge(source, target);
                getCollectFilterGraph(n, done, graph, mapping);
            }
        }
    }

    /**
     * given in as input, execute collect/filter action and return result
     *
     * @param cba
     * @param in
     * @return
     */
    @SuppressWarnings("unchecked")
    private static List<Element> collectAndFilter(CallBehaviorAction cba, List<Element> in) {
        //System.out.println("collectAndFilter(): cba=" + MoreToString.Helper.toLongString( cba ) );
        //System.out.println("collectAndFilter(): in[" + in.size() + "]=" + MoreToString.Helper.toLongString( in ) );
        Integer depth = (Integer) GeneratorUtils.getStereotypePropertyFirst(cba, DocGenProfile.depthChoosable,
                "depth", DocGenProfile.PROFILE_NAME, 0);
        int direction = ((Boolean) GeneratorUtils.getStereotypePropertyFirst(cba, DocGenProfile.directionChoosable,
                "directionOut", DocGenProfile.PROFILE_NAME, true)) ? 1 : 2;
        List<Stereotype> stereotypes = (List<Stereotype>) GeneratorUtils.getStereotypePropertyValue(cba,
                DocGenProfile.stereotypeChoosable, "stereotypes", DocGenProfile.PROFILE_NAME, new ArrayList<Stereotype>());
        List<Class> metaclasses = (List<Class>) GeneratorUtils.getStereotypePropertyValue(cba,
                DocGenProfile.metaclassChoosable, "metaclasses", DocGenProfile.PROFILE_NAME, new ArrayList<Class>());
        Boolean derived = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba, DocGenProfile.derivedChoosable,
                "considerDerived", DocGenProfile.PROFILE_NAME, true);
        List<String> names = (List<String>) GeneratorUtils.getStereotypePropertyValue(cba, DocGenProfile.nameChoosable,
                "names", DocGenProfile.PROFILE_NAME, new ArrayList<String>());
        List<String> diagramTypes = Utils.getElementNames((List<NamedElement>) GeneratorUtils.getStereotypePropertyValue(
                cba, DocGenProfile.diagramTypeChoosable, "diagramTypes", DocGenProfile.PROFILE_NAME, new ArrayList<NamedElement>()));
        Boolean include = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba, DocGenProfile.includeChoosable,
                "include", DocGenProfile.PROFILE_NAME, true);
        List<Property> stereotypeProperties = (List<Property>) GeneratorUtils.getStereotypePropertyValue(cba, DocGenProfile.stereotypePropertyChoosable, "stereotypeProperties",
                DocGenProfile.PROFILE_NAME, new ArrayList<Property>());
        Boolean inherited = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba, DocGenProfile.inheritedChoosable,
                "includeInherited", DocGenProfile.PROFILE_NAME, false);
        EnumerationLiteral asso = (EnumerationLiteral) GeneratorUtils.getStereotypePropertyFirst(cba,
                DocGenProfile.associationChoosable, "associationType", DocGenProfile.PROFILE_NAME, null);
        String expression = (String) GeneratorUtils.getStereotypePropertyFirst(cba, DocGenProfile.expressionChoosable,
                "expression", DocGenProfile.PROFILE_NAME, null);
        Boolean iterate = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba, DocGenProfile.expressionChoosable,
                "iterate", DocGenProfile.PROFILE_NAME, true);
        AggregationKind associationType = null;
        if (asso != null) {
            if (asso.getName().equals("composite")) {
                associationType = AggregationKindEnum.COMPOSITE;
            }
            else if (asso.getName().equals("none")) {
                associationType = AggregationKindEnum.NONE;
            }
            else {
                associationType = AggregationKindEnum.SHARED;
            }
        }
        if (associationType == null) {
            associationType = AggregationKindEnum.COMPOSITE;
        }
        List<Element> res = new ArrayList<Element>();

        if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectDiagram)) {
            for (Element e : in) {
                if (e instanceof Diagram) {
                    res.addAll(Utils.getElementsOnDiagram((Diagram) e));
                }
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectAssociationStereotype)) {
            for (Element e : in) {
                res.addAll(Utils.collectAssociatedElements(e, depth, associationType));
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectOwnedElementStereotype)) {
            for (Element e : in) {
                res.addAll(Utils.collectOwnedElements(e, depth));
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectOwnerStereotype)) {
            for (Element e : in) {
                res.addAll(Utils.collectOwners(e, depth));
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectRelMetaclassStereotype)) {
            for (Element e : in) {
                res.addAll(Utils.collectDirectedRelatedElementsByRelationshipMetaclasses(e, metaclasses,
                        direction, depth));
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectRelStereotypeStereotype)) {
            for (Element e : in) {
                res.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypes(e, stereotypes,
                        direction, derived, depth));
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectStereotypePropStereotype)) {
            List<Object> blah = new ArrayList<Object>();
            for (Element e : in) {
                for (Property p : stereotypeProperties)
                //blah.addAll(StereotypesHelper.getStereotypePropertyValue(e, (Stereotype)p.getOwner(), p));
                {
                    blah.addAll(Utils.collectByStereotypeProperty(e, p));
                }
            }
            for (Object b : blah) {
                if (b instanceof Element) {
                    res.add((Element) b);
                }
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectTypeStereotype)) {
            for (Element e : in) {
                if (e instanceof TypedElement) {
                    if (((TypedElement) e).getType() != null) {
                        res.add(((TypedElement) e).getType());
                    }
                }
                else if (e instanceof CallBehaviorAction && ((CallBehaviorAction) e).getBehavior() != null) {
                    res.add(((CallBehaviorAction) e).getBehavior());
                }
                else if (e instanceof CallOperationAction
                        && ((CallOperationAction) e).getOperation() != null) {
                    res.add(((CallOperationAction) e).getOperation());
                }
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectClassifierAttributes)) {
            for (Element e : in) {
                res.addAll(Utils.getAttributes(e, inherited));
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectExpressionStereotype)) {
            res.addAll(Utils.collectByExpression(in, expression, iterate));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.filterDiagramTypeStereotype)) {
            res.addAll(Utils.filterDiagramsByDiagramTypes(in, diagramTypes, include));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.filterMetaclassStereotype)) {
            res.addAll(Utils.filterElementsByMetaclasses(in, metaclasses, include));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.filterNameStereotype)) {
            res.addAll(Utils.filterElementsByNameRegex(in, names, include));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.filterStereotypeStereotype)) {
            res.addAll(Utils.filterElementsByStereotypes(in, stereotypes, include, derived));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.filterExpressionStereotype)) {
            res.addAll(Utils.filterElementsByExpression(in, expression, include, iterate));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.collectionStereotype)
                && cba.getBehavior() != null) {
            res.addAll(collectAndFilterGroup((Activity) cba.getBehavior(), in));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.removeDuplicates)) {
            res.addAll(Utils.removeDuplicates(in));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.userScriptCFStereotype, true)) {
            res.addAll(getUserScriptCF(in, cba));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.sortByName)) {
            res.addAll(sortElements(in, DocGenProfile.sortByName, cba));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.sortByAttribute)) {
            res.addAll(sortElements(in, DocGenProfile.sortByAttribute, cba));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.sortByProperty)) {
            res.addAll(sortElements(in, DocGenProfile.sortByProperty, cba));
        }
        else if (GeneratorUtils.hasStereotypeByString(cba, DocGenProfile.sortByExpression)) {
            res.addAll(sortElements(in, DocGenProfile.sortByExpression, cba));
        }
        // TODO -- duplicates should probably not be removed if just sorting!
        /*System.out.println( "collectAndFilter(): returning (before removing duplicates) res["
                            + res.size() + "]="
                            + MoreToString.Helper.toLongString( res ) );*/

        return Utils.removeDuplicates(res);
    }

    /**
     * Sorts elements by property, attribute, or name after applying call
     * behavior.
     *
     * @param in             elements to be sorted
     * @param sortStereotype the kind of sort based on sort stereotype name. This may be
     *                       sortByProperty, sortByAttribute, or sortByName as found in
     *                       DocGenProfile.
     * @param cba            call behavior to be applied before getting the specified
     * @return
     */
    public static List<Element> sortElements(Collection<? extends Element> in, String sortStereotype,
                                             Element cba) {
        List<Element> ordered = new ArrayList<Element>(in);

        boolean isProp = sortStereotype.equals(DocGenProfile.sortByProperty);
        boolean isAttr = sortStereotype.equals(DocGenProfile.sortByAttribute);
        boolean isExpr = sortStereotype.equals(DocGenProfile.sortByExpression);
        boolean isName = sortStereotype.equals(DocGenProfile.sortByName);
        if (!isProp && !isAttr && !isName && !isExpr) {
            log.error("Error! Trying to sort by unknown sort type: " + sortStereotype);
            return ordered;
        }

        String stereotypeProperty = null;
        if (isProp) {
            stereotypeProperty = "desiredProperty";
        }
        else if (isAttr) {
            stereotypeProperty = "desiredAttribute";
        }
        else if (isExpr) {
            stereotypeProperty = "expression";
        }

        Object o = GeneratorUtils.getStereotypePropertyFirst(cba, sortStereotype, stereotypeProperty, DocGenProfile.PROFILE_NAME, null);

        if (o instanceof Property && isProp) {
            ordered = Utils.sortByProperty(in, (Property) o);
        }
        else if (o instanceof EnumerationLiteral && isAttr) {
            ordered = Utils.sortByAttribute(in, o);
        }
        else if (isExpr) {
            ordered = Utils.sortByExpression(in, o);
        }
        else if (isName) {
            ordered = Utils2.asList(Utils.sortByName(in), Element.class);
        }
        else {
            log.error("Error! Trying to sort as " + sortStereotype
                    + ", but the property/attribute is the wrong type: " + o);
            return ordered;
        }
        o = GeneratorUtils.getStereotypePropertyFirst(cba, sortStereotype, "reverse", DocGenProfile.PROFILE_NAME, false);
        if (o == null) {
            o = GeneratorUtils.getStereotypePropertyFirst(cba, sortStereotype, "invertOrder", DocGenProfile.PROFILE_NAME, false);
        }

        Boolean b = null;
        try {
            b = (Boolean) o;
        } catch (ClassCastException e) {
            // ignore
        }
        if (b != null && b) {
            Collections.reverse(ordered);
        }
        return ordered;
    }

    /**
     * an activity that should only has collect/filter actions in it
     *
     * @param a
     * @param in
     * @return
     */
    private static List<Element> collectAndFilterGroup(Activity a, List<Element> in) {
        InitialNode initial = GeneratorUtils.findInitialNode(a);
        Collection<ActivityEdge> outs = initial.getOutgoing();
        List<Element> res = in;
        if (outs != null && outs.size() == 1) {
            ActivityNode n = outs.iterator().next().getTarget();
            if (StereotypesHelper.hasStereotypeOrDerived(n, DocGenProfile.collectFilterStereotype)
                    || n instanceof CallBehaviorAction
                    && ((CallBehaviorAction) n).getBehavior() != null
                    && StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction) n).getBehavior(),
                    DocGenProfile.collectFilterStereotype)) {
                res = startCollectAndFilterSequence(n, in);
            }
        }
        return res;
    }

    /**
     * collect/filter action can be a userscript - input and output are both
     * collection of elements
     *
     * @param in
     * @param cba
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static List<Element> getUserScriptCF(List<Element> in, CallBehaviorAction cba) {
        List<Element> res = new ArrayList<Element>();
        try {
            Map<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("DocGenTargets", in);
            Element e = cba;
            if (!StereotypesHelper.hasStereotypeOrDerived(cba, DocGenProfile.userScriptCFStereotype)) {
                if (cba.getBehavior() != null
                        && StereotypesHelper.hasStereotypeOrDerived(cba.getBehavior(),
                        DocGenProfile.userScriptCFStereotype)) {
                    e = ((CallBehaviorAction) e).getBehavior();
                }
            }
            Object o = ScriptRunner.runScriptFromStereotype(e,
                    StereotypesHelper.checkForDerivedStereotype(e, DocGenProfile.userScriptCFStereotype),
                    inputs);
            if (o != null && o instanceof Map && ((Map) o).containsKey("DocGenOutput")) {
                Object l = ((Map) o).get("DocGenOutput");
                if (l instanceof List) {
                    for (Object oo : (List) l) {
                        if (oo instanceof Element) {
                            res.add((Element) oo);
                        }
                    }
                }
            }
        } catch (ScriptException ex) {
            ex.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            context.log(sw.toString()); // stack trace as a string
        }
        return res;
    }

}
