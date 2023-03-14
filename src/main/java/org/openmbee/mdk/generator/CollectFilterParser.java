package org.openmbee.mdk.generator;

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
import org.openmbee.mdk.SysMLExtensions;
import org.openmbee.mdk.docgen.ViewViewpointValidator;
import org.openmbee.mdk.generator.graphs.DirectedEdgeVector;
import org.openmbee.mdk.generator.graphs.DirectedGraphHashSet;
import org.openmbee.mdk.generator.graphs.algorithms.TopologicalSort;
import org.openmbee.mdk.util.GeneratorUtils;
import org.openmbee.mdk.util.ScriptRunner;
import org.openmbee.mdk.util.Utils;
import org.openmbee.mdk.util.Utils2;
import org.apache.log4j.Logger;

import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class CollectFilterParser {

    private static GenerationContext context;
    private static Logger log = Logger.getLogger(CollectFilterParser.class);

    public static void setContext(GenerationContext gc) {
        context = gc;
    }

    public static GenerationContext getContext() {
        return context;
    }

    public static ViewViewpointValidator getValidator() {
        return context == null ? null : context.getValidator();
    }

    public static SysMLExtensions profile;

    public static void setProfile(SysMLExtensions p) {
        profile = p;
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
            ViewViewpointValidator.evaluateConstraints(node.getNode(), res, context, true, true);
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
            if (profile.collectOrFilter().is(n)) {
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
        Integer depth = (Integer) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.depthChoosable().getDepthProperty(), 0);
        int direction = ((Boolean) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.directionChoosable().getDirectionOutProperty(), true)) ? 1 : 2;
        List<Stereotype> stereotypes = (List<Stereotype>) GeneratorUtils.getStereotypePropertyValue(cba,
                profile.stereotypeChoosable().getStereotypesProperty(), new ArrayList<Stereotype>());
        List<Class> metaclasses = (List<Class>) GeneratorUtils.getStereotypePropertyValue(cba,
                profile.metaclassChoosable().getMetaclassesProperty(), new ArrayList<Class>());
        Boolean derived = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.considerDerivedChoosable().getConsiderDerivedProperty(), true);
        List<String> names = (List<String>) GeneratorUtils.getStereotypePropertyValue(cba,
                profile.nameChoosable().getNamesProperty(), new ArrayList<String>());
        List<String> diagramTypes = ((List<NamedElement>) GeneratorUtils.getStereotypePropertyValue(
                cba, profile.diagramTypeChoosable().getDiagramTypesProperty(), new ArrayList<NamedElement>()))
                .stream().map(NamedElement::getName).collect(Collectors.toList());
        Boolean include = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.includeChoosable().getIncludeProperty(), true);
        List<Property> stereotypeProperties = (List<Property>) GeneratorUtils.getStereotypePropertyValue(cba,
                profile.stereotypePropertiesChoosable().getStereotypePropertiesProperty(), new ArrayList<Property>());
        Boolean inherited = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.includeInheritedChoosable().getIncludeInheritedProperty(), false);
        EnumerationLiteral asso = (EnumerationLiteral) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.associationTypeChoosable().getAssociationTypeProperty(), null);
        String expression = (String) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.expressionChoosable().getExpressionProperty(), null);
        Boolean iterate = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba,
                profile.expressionChoosable().getIterateProperty(), true);
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

        if (GeneratorUtils.hasStereotype(cba, profile.collectThingsOnDiagram().getStereotype())) {
            for (Element e : in) {
                if (e instanceof Diagram) {
                    res.addAll(Utils.getElementsOnDiagram((Diagram) e));
                }
            }
        } else if (GeneratorUtils.hasStereotype(cba, profile.collectByAssociation().getStereotype())) {
            for (Element e : in) {
                res.addAll(Utils.collectAssociatedElements(e, depth, associationType));
            }
        } else if (GeneratorUtils.hasStereotype(cba, profile.collectOwnedElements().getStereotype())) {
            for (Element e : in) {
                res.addAll(Utils.collectOwnedElements(e, depth));
            }
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectOwners().getStereotype())) {
            for (Element e : in) {
                res.addAll(Utils.collectOwners(e, depth));
            }
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectByDirectedRelationshipMetaclasses().getStereotype())) {
            for (Element e : in) {
                res.addAll(Utils.collectDirectedRelatedElementsByRelationshipMetaclasses(e, metaclasses,
                        direction, depth));
            }
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectByDirectedRelationshipStereotypes().getStereotype())) {
            for (Element e : in) {
                res.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypes(e, stereotypes,
                        direction, derived, depth));
            }
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectByStereotypeProperties().getStereotype())) {
            List<Object> blah = new ArrayList<Object>();
            for (Element e : in) {
                for (Property p : stereotypeProperties) {
                    blah.addAll(Utils.collectByStereotypeProperty(e, p));
                }
            }
            for (Object b : blah) {
                if (b instanceof Element) {
                    res.add((Element) b);
                }
            }
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectTypes().getStereotype())) {
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
        else if (GeneratorUtils.hasStereotype(cba, profile.collectClassifierAttributes().getStereotype())) {
            for (Element e : in) {
                res.addAll(Utils.getAttributes(e, inherited));
            }
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectByExpression().getStereotype())) {
            res.addAll(Utils.collectByExpression(in, expression, iterate));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.filterByDiagramType().getStereotype())) {
            res.addAll(Utils.filterDiagramsByDiagramTypes(in, diagramTypes, include));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.filterByMetaclasses().getStereotype())) {
            res.addAll(Utils.filterElementsByMetaclasses(in, metaclasses, include));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.filterByNames().getStereotype())) {
            res.addAll(Utils.filterElementsByNameRegex(in, names, include));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.filterByStereotypes().getStereotype())) {
            res.addAll(Utils.filterElementsByStereotypes(in, stereotypes, include, derived));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.filterByExpression().getStereotype())) {
            res.addAll(Utils.filterElementsByExpression(in, expression, include, iterate));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectionAndFilterGroup().getStereotype())
                && cba.getBehavior() != null) {
            res.addAll(collectAndFilterGroup((Activity) cba.getBehavior(), in));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.removeDuplicates().getStereotype())) {
            res.addAll(Utils.removeDuplicates(in));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.collectFilterUserScript().getStereotype(), true)) {
            res.addAll(getUserScriptCF(in, cba));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.sortByName().getStereotype())) {
            res.addAll(sortElements(in, null, cba));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.sortByAttribute().getStereotype())) {
            res.addAll(sortElements(in, profile.sortByAttribute().getDesiredAttributeProperty(), cba));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.sortByProperty().getStereotype())) {
            res.addAll(sortElements(in, profile.sortByProperty().getDesiredPropertyProperty(), cba));
        }
        else if (GeneratorUtils.hasStereotype(cba, profile.sortByExpression().getStereotype())) {
            res.addAll(sortElements(in, profile.sortByExpression().getExpressionProperty(), cba));
        }
        return Utils.removeDuplicates(res);
    }

    /**
     * Sorts elements by property, attribute, or name after applying call
     * behavior.
     *
     * @param in             elements to be sorted
     * @param sortProperty the kind of sort based on sort stereotype name. This may be
     *                       sortByProperty, sortByAttribute, or sortByName as found in
     *                       DocGenProfile.
     * @param cba            call behavior to be applied before getting the specified
     * @return
     */
    public static List<Element> sortElements(Collection<? extends Element> in, Property sortProperty,
                                             Element cba) {
        List<Element> ordered = new ArrayList<Element>(in);

        boolean isName = false;
        boolean isProp = false;
        boolean isAttr = false;
        boolean isExpr = false;
        if (sortProperty == null) {
            isName = true;
        } else {
            isProp = sortProperty.getName().equals("desiredProperty");
            isAttr = sortProperty.getName().equals("desiredAttribute");
            isExpr = sortProperty.getName().equals("expression");
        }
        if (!isProp && !isAttr && !isName && !isExpr) {
            log.error("Error! Trying to sort by unknown sort type: " + sortProperty.getName());
            return ordered;
        }
        Object o = GeneratorUtils.getStereotypePropertyFirst(cba, sortProperty, null);

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
            log.error("Error! Trying to sort as " + sortProperty.getName()
                    + ", but the property/attribute is the wrong type: " + o);
            return ordered;
        }
        o = GeneratorUtils.getStereotypePropertyFirst(cba, profile.sortable().getReverseProperty(), false);

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
            if (profile.collectOrFilter().is(n)
                    || n instanceof CallBehaviorAction
                    && ((CallBehaviorAction) n).getBehavior() != null
                    && profile.collectOrFilter().is(((CallBehaviorAction) n).getBehavior())) {
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
            if (!profile.collectFilterUserScript().is(e)) {
                if (cba.getBehavior() != null
                        && profile.collectFilterUserScript().is(cba.getBehavior())) {
                    e = ((CallBehaviorAction) e).getBehavior();
                }
            }
            Object o = ScriptRunner.runScriptFromStereotype(e,
                    StereotypesHelper.checkForDerivedStereotype(e, profile.collectFilterUserScript().getStereotype()),
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
