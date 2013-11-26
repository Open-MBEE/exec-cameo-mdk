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
package gov.nasa.jpl.mbee.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;

import com.nomagic.magicdraw.core.Application;
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
    private Element                                           a;
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
        new HashSet<ControlFlow>();
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
        StrongConnectivityInspector<ActivityNode, ControlFlow> sci = new StrongConnectivityInspector<ActivityNode, ControlFlow>(graph);
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
