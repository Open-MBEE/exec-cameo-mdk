/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.generator;

import gov.nasa.jpl.graphs.DirectedEdgeVector;
import gov.nasa.jpl.graphs.DirectedGraphHashSet;
import gov.nasa.jpl.graphs.algorithms.TopologicalSort;
import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.ScriptRunner;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.script.ScriptException;

import org.apache.log4j.Logger;

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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

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
     * @param context
     *            TODO
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
        for (CollectFilterNode node: toposort) {
            Set<CollectFilterNode> incomings = new HashSet<CollectFilterNode>();
            for (DirectedEdgeVector<CollectFilterNode> edge: graph.findEdgesWithTargetVertex(node)) {
                incomings.add(edge.getSourceVertex());
            }
            Set<List<Element>> ins = new HashSet<List<Element>>();
            if (incomings.isEmpty()) {
                if (in == null) {
                    if (!context.targetsEmpty())
                        ins.add(Utils2.asList(context.peekTargets(), Element.class));
                } else {
                    ins.add(in);
                }
            } else {
                for (CollectFilterNode i: incomings) {
                    ins.add(i.getResult());
                }
            }
            if (node.getNode() instanceof CallBehaviorAction) {
                if (ins.size() == 1) {
                    res = collectAndFilter((CallBehaviorAction)node.getNode(), ins.iterator().next());
                    /*System.out.println( "collectAndFilter(): returned (after removing duplicates) res["
                            + res.size() + "]="
                            + MoreToString.Helper.toLongString( res ) );*/
                    node.setResult(res);
                } else {
                    // ???
                    res = new ArrayList<Element>();
                    node.setResult(res);
                }
            } else if (node.getNode() instanceof ForkNode) {
                if (ins.size() == 1) {
                    res = ins.iterator().next();
                    node.setResult(res);
                } else {
                    res = new ArrayList<Element>();
                    node.setResult(res);
                    // ???
                }
            } else if (node.getNode() instanceof MergeNode) {
                res = Utils.unionOfCollections(ins);
                node.setResult(res);
            } else if (node.getNode() instanceof JoinNode) {
                res = Utils.intersectionOfCollections(ins);
                node.setResult(res);
            } else if (node.getNode() instanceof DecisionNode) {
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
        if (done.contains(cur))
            return;
        done.add(cur);
        CollectFilterNode source = mapping.get(cur);
        if (source == null) {
            source = new CollectFilterNode(cur);
            mapping.put(cur, source);
        }
        graph.addVertex(source);
        for (ActivityEdge e: cur.getOutgoing()) {
            ActivityNode n = e.getTarget();
            if (GeneratorUtils.hasStereotypeByString(n, DocGen3Profile.collectFilterStereotype, true)) {
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
        Integer depth = (Integer)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.depthChoosable,
                "depth", 0);
        int direction = ((Boolean)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.directionChoosable,
                "directionOut", true)) ? 1 : 2;
        List<Stereotype> stereotypes = (List<Stereotype>)GeneratorUtils.getListProperty(cba,
                DocGen3Profile.stereotypeChoosable, "stereotypes", new ArrayList<Stereotype>());
        List<Class> metaclasses = (List<Class>)GeneratorUtils.getListProperty(cba,
                DocGen3Profile.metaclassChoosable, "metaclasses", new ArrayList<Class>());
        Boolean derived = (Boolean)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.derivedChoosable,
                "considerDerived", true);
        List<String> names = (List<String>)GeneratorUtils.getListProperty(cba, DocGen3Profile.nameChoosable,
                "names", new ArrayList<String>());
        List<String> diagramTypes = Utils.getElementNames((List<NamedElement>)GeneratorUtils.getListProperty(
                cba, DocGen3Profile.diagramTypeChoosable, "diagramTypes", new ArrayList<NamedElement>()));
        Boolean include = (Boolean)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.includeChoosable,
                "include", true);
        List<Property> stereotypeProperties = (List<Property>)GeneratorUtils
                .getListProperty(cba, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties",
                        new ArrayList<Property>());
        Boolean inherited = (Boolean)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.inheritedChoosable,
                "includeInherited", false);
        EnumerationLiteral asso = (EnumerationLiteral)GeneratorUtils.getObjectProperty(cba,
                DocGen3Profile.associationChoosable, "associationType", null);
        String expression = (String)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.expressionChoosable,
                "expression", null);
        Boolean iterate = (Boolean)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.expressionChoosable,
                "iterate", true);
        AggregationKind associationType = null;
        if (asso != null) {
            if (asso.getName().equals("composite"))
                associationType = AggregationKindEnum.COMPOSITE;
            else if (asso.getName().equals("none"))
                associationType = AggregationKindEnum.NONE;
            else
                associationType = AggregationKindEnum.SHARED;
        }
        if (associationType == null)
            associationType = AggregationKindEnum.COMPOSITE;
        List<Element> res = new ArrayList<Element>();

        if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectDiagram)) {
            for (Element e: in)
                if (e instanceof Diagram)
                    res.addAll(Utils.getElementsOnDiagram((Diagram)e));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectAssociationStereotype)) {
            for (Element e: in)
                res.addAll(Utils.collectAssociatedElements(e, depth, associationType));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectOwnedElementStereotype)) {
            for (Element e: in)
                res.addAll(Utils.collectOwnedElements(e, depth));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectOwnerStereotype)) {
            for (Element e: in)
                res.addAll(Utils.collectOwners(e, depth));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectRelMetaclassStereotype)) {
            for (Element e: in)
                res.addAll(Utils.collectDirectedRelatedElementsByRelationshipMetaclasses(e, metaclasses,
                        direction, depth));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectRelStereotypeStereotype)) {
            for (Element e: in)
                res.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypes(e, stereotypes,
                        direction, derived, depth));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectStereotypePropStereotype)) {
            List<Object> blah = new ArrayList<Object>();
            for (Element e: in)
                for (Property p: stereotypeProperties)
                    //blah.addAll(StereotypesHelper.getStereotypePropertyValue(e, (Stereotype)p.getOwner(), p));
                    blah.addAll(Utils.collectByStereotypeProperty(e, p));
            for (Object b: blah)
                if (b instanceof Element)
                    res.add((Element)b);
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectTypeStereotype)) {
            for (Element e: in) {
                if (e instanceof TypedElement) {
                    if (((TypedElement)e).getType() != null) {
                        res.add(((TypedElement)e).getType());
                    }
                } else if (e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
                    res.add(((CallBehaviorAction)e).getBehavior());
                } else if (e instanceof CallOperationAction
                        && ((CallOperationAction)e).getOperation() != null) {
                    res.add(((CallOperationAction)e).getOperation());
                }
            }
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectClassifierAttributes)) {
            for (Element e: in)
                res.addAll(Utils.getAttributes(e, inherited));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectExpressionStereotype)) {
            res.addAll(Utils.collectByExpression(in, expression, iterate));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.filterDiagramTypeStereotype)) {
            res.addAll(Utils.filterDiagramsByDiagramTypes(in, diagramTypes, include));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.filterMetaclassStereotype)) {
            res.addAll(Utils.filterElementsByMetaclasses(in, metaclasses, include));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.filterNameStereotype)) {
            res.addAll(Utils.filterElementsByNameRegex(in, names, include));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.filterStereotypeStereotype)) {
            res.addAll(Utils.filterElementsByStereotypes(in, stereotypes, include, derived));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.filterExpressionStereotype)) {
            res.addAll(Utils.filterElementsByExpression(in, expression, include, iterate));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.collectionStereotype)
                && cba.getBehavior() != null) {
            res.addAll(collectAndFilterGroup((Activity)cba.getBehavior(), in));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.removeDuplicates)) {
            res.addAll(Utils.removeDuplicates(in));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.userScriptCFStereotype, true)) {
            res.addAll(getUserScriptCF(in, cba));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.sortByName)) {
            res.addAll(sortElements(in, DocGen3Profile.sortByName, cba));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.sortByAttribute)) {
            res.addAll(sortElements(in, DocGen3Profile.sortByAttribute, cba));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.sortByProperty)) {
            res.addAll(sortElements(in, DocGen3Profile.sortByProperty, cba));
        } else if (GeneratorUtils.hasStereotypeByString(cba, DocGen3Profile.sortByExpression)) {
            res.addAll(sortElements(in, DocGen3Profile.sortByExpression, cba));
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
     * @param in
     *            elements to be sorted
     * @param sortStereotype
     *            the kind of sort based on sort stereotype name. This may be
     *            sortByProperty, sortByAttribute, or sortByName as found in
     *            DocGen3Profile.
     * @param cba
     *            call behavior to be applied before getting the specified
     * @return
     */
    public static List<Element> sortElements(Collection<? extends Element> in, String sortStereotype,
            Element cba) {
        List<Element> ordered = new ArrayList<Element>(in);

        boolean isProp = sortStereotype.equals(DocGen3Profile.sortByProperty);
        boolean isAttr = sortStereotype.equals(DocGen3Profile.sortByAttribute);
        boolean isExpr = sortStereotype.equals(DocGen3Profile.sortByExpression);
        boolean isName = sortStereotype.equals(DocGen3Profile.sortByName);
        if (!isProp && !isAttr && !isName && !isExpr) {
            log.error("Error! Trying to sort by unknown sort type: " + sortStereotype);
            return ordered;
        }

        String stereotypeProperty = null;
        if (isProp)
            stereotypeProperty = "desiredProperty";
        else if (isAttr)
            stereotypeProperty = "desiredAttribute";
        else if (isExpr)
            stereotypeProperty = "expression";

        Object o = GeneratorUtils.getObjectProperty(cba, sortStereotype, stereotypeProperty, null);

        if (o instanceof Property && isProp) {
            ordered = Utils.sortByProperty(in, (Property)o);
        } else if (o instanceof EnumerationLiteral && isAttr) {
            ordered = Utils.sortByAttribute(in, o);
        } else if (isExpr) {
            ordered = Utils.sortByExpression(in, o);
        } else if (isName) {
            ordered = Utils2.asList( Utils.sortByName(in), Element.class );
        } else {
            log.error("Error! Trying to sort as " + sortStereotype
                    + ", but the property/attribute is the wrong type: " + o);
            return ordered;
        }
        o = GeneratorUtils.getObjectProperty(cba, sortStereotype, "reverse", false);
        if (o == null)
            o = GeneratorUtils.getObjectProperty(cba, sortStereotype, "invertOrder", false);

        Boolean b = null;
        try {
            b = (Boolean)o;
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
            if (StereotypesHelper.hasStereotypeOrDerived(n, DocGen3Profile.collectFilterStereotype)
                    || n instanceof CallBehaviorAction
                    && ((CallBehaviorAction)n).getBehavior() != null
                    && StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)n).getBehavior(),
                            DocGen3Profile.collectFilterStereotype)) {
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
            if (!StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.userScriptCFStereotype))
                if (cba.getBehavior() != null
                        && StereotypesHelper.hasStereotypeOrDerived(cba.getBehavior(),
                                DocGen3Profile.userScriptCFStereotype))
                    e = ((CallBehaviorAction)e).getBehavior();
            Object o = ScriptRunner.runScriptFromStereotype(e,
                    StereotypesHelper.checkForDerivedStereotype(e, DocGen3Profile.userScriptCFStereotype),
                    inputs);
            if (o != null && o instanceof Map && ((Map)o).containsKey("DocGenOutput")) {
                Object l = ((Map)o).get("DocGenOutput");
                if (l instanceof List) {
                    for (Object oo: (List)l) {
                        if (oo instanceof Element)
                            res.add((Element)oo);
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
