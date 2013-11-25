package gov.nasa.jpl.mgss.mbee.docgen.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ControlFlow;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.FinalNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ActivityDiagramGraph {

    private Diagram                                           diagram;
    private Element                                           a;               // activity
                                                                                // or
                                                                                // structured
                                                                                // behavior
                                                                                // node
    private DirectedGraph<ActivityNode, ControlFlow>          graph;
    private DirectedGraph<ActivityNode, ControlFlow>          acyclicGraph;
    private Collection<Element>                               ondiagram;
    private InitialNode                                       start;
    private Set<FinalNode>                                    ends;
    private List<DirectedSubgraph<ActivityNode, ControlFlow>> loops;
    private Set<ActivityNode>                                 loopNodes;
    private Set<ControlFlow>                                  loopFlows;
    private boolean                                           hasloops;
    private List<ActivityNode>                                sorted;
    private Set<ControlFlow>                                  removedLoopFlows;
    private GUILog                                            gl;

    public ActivityDiagramGraph(Diagram diagram, Element a) {
        this.diagram = diagram;
        graph = new DefaultDirectedGraph<ActivityNode, ControlFlow>(ControlFlow.class);
        acyclicGraph = new DefaultDirectedGraph<ActivityNode, ControlFlow>(ControlFlow.class);
        if (diagram != null)
            ondiagram = Application.getInstance().getProject().getDiagram(diagram)
                    .getUsedModelElements(false);
        else
            ondiagram = a.getOwnedElement();
        this.a = a;
        loops = new ArrayList<DirectedSubgraph<ActivityNode, ControlFlow>>();
        loopNodes = new HashSet<ActivityNode>();
        loopFlows = new HashSet<ControlFlow>();
        hasloops = false;
        sorted = new ArrayList<ActivityNode>();
        ends = new HashSet<FinalNode>();
        removedLoopFlows = new HashSet<ControlFlow>();
        gl = Application.getInstance().getGUILog();
    }

    public void fillGraphs() {
        Collection<Element> filter = a.getOwnedElement();
        for (Element e: ondiagram) {
            if (!filter.contains(e))
                continue;
            if (e instanceof ActivityNode)
                handleNode((ActivityNode)e);
            if (e instanceof InitialNode)
                start = (InitialNode)e;
            if (e instanceof FinalNode)
                ends.add((FinalNode)e);
        }
        StrongConnectivityInspector<ActivityNode, ControlFlow> sci = new StrongConnectivityInspector(graph);
        for (DirectedSubgraph<ActivityNode, ControlFlow> dsg: sci.stronglyConnectedSubgraphs()) {
            if (dsg.edgeSet().size() > 0) {
                hasloops = true;
                loops.add(dsg);
                loopNodes.addAll(dsg.vertexSet());
                loopFlows.addAll(dsg.edgeSet());
            }
        }
        // if (hasloops)
        // removeLoopEdges();
        // TopologicalOrderIterator<ActivityNode, ControlFlow> toi = new
        // TopologicalOrderIterator<ActivityNode, ControlFlow>(acyclicGraph);
        // while(toi.hasNext()) {
        // sorted.add(toi.next());
        // }
    }

    private void handleNode(ActivityNode an) {
        graph.addVertex(an);
        acyclicGraph.addVertex(an);
        for (ActivityEdge ae: an.getOutgoing()) {
            if (!(ae instanceof ControlFlow))
                continue;
            ActivityNode target = ae.getTarget();
            if (ondiagram.contains(ae) && ondiagram.contains(target)) {
                graph.addVertex(target);
                graph.addEdge(an, target, (ControlFlow)ae);
                // gl.log("added edge from " + an.getName() + " to " +
                // target.getName());
                acyclicGraph.addVertex(target);
                acyclicGraph.addEdge(an, target, (ControlFlow)ae);
            }
        }
        if (StereotypesHelper.hasStereotypeOrDerived(an, BPMNProfile.BPMNActivity)) {
            for (Object e: StereotypesHelper.getStereotypePropertyValue(an, BPMNProfile.BPMNActivity,
                    BPMNProfile.boundaryEventRefs)) {
                if (ondiagram.contains(e) && e instanceof ActivityNode) {
                    ControlFlow fakecf = Application.getInstance().getProject().getElementsFactory()
                            .createControlFlowInstance();
                    graph.addVertex((ActivityNode)e);
                    graph.addEdge(an, (ActivityNode)e, fakecf);
                    acyclicGraph.addVertex((ActivityNode)e);
                    acyclicGraph.addEdge(an, (ActivityNode)e, fakecf);
                }
            }
        }
    }

    private void removeLoopEdges() {
        if (start == null)
            return;
        Set<ControlFlow> seenFlows = new HashSet<ControlFlow>();
        Set<ActivityNode> seenNodes = new HashSet<ActivityNode>();
        Queue<ControlFlow> nextFlows = new LinkedList<ControlFlow>();
        for (ControlFlow cf: acyclicGraph.outgoingEdgesOf(start)) {
            nextFlows.offer(cf);
        }
        while (!nextFlows.isEmpty()) {
            ControlFlow curFlow = nextFlows.remove();
            seenFlows.add(curFlow);
            ActivityNode curNode = acyclicGraph.getEdgeTarget(curFlow);
            seenNodes.add(curNode);
            for (ControlFlow nextFlow: acyclicGraph.outgoingEdgesOf(curNode)) {
                if (seenFlows.contains(nextFlow) && seenNodes.contains(curNode)
                        && loopFlows.contains(curFlow)) {
                    // going to the curNode will result in a loop
                    acyclicGraph.removeEdge(curFlow);
                    removedLoopFlows.add(curFlow);
                    break;
                }
                nextFlows.offer(nextFlow);
            }
        }
    }

    public Diagram getDiagram() {
        return diagram;
    }

    public DirectedGraph<ActivityNode, ControlFlow> getGraph() {
        return graph;
    }

    public DirectedGraph<ActivityNode, ControlFlow> getAcyclicGraph() {
        return acyclicGraph;
    }

    public Collection<Element> getOndiagram() {
        return ondiagram;
    }

    public InitialNode getStart() {
        return start;
    }

    public Set<FinalNode> getEnds() {
        return ends;
    }

    public List<DirectedSubgraph<ActivityNode, ControlFlow>> getLoops() {
        return loops;
    }

    public Set<ActivityNode> getLoopNodes() {
        return loopNodes;
    }

    public Set<ControlFlow> getLoopFlows() {
        return loopFlows;
    }

    public boolean isHasloops() {
        return hasloops;
    }

    public List<ActivityNode> getSorted() {
        return sorted;
    }
}
