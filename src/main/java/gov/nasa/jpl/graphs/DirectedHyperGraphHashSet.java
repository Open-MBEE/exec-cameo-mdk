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
package gov.nasa.jpl.graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DirectedHyperGraphHashSet<VertexType, EdgeType extends DirectedHyperEdge<VertexType>> extends
        UndirectedHyperGraphHashSet<VertexType, EdgeType> implements DirectedHyperGraph<VertexType, EdgeType> {
    protected Map<VertexType, Set<EdgeType>> Vs2E;
    protected Map<VertexType, Set<EdgeType>> Vt2E;

    public DirectedHyperGraphHashSet() {
        super();
        Vs2E = new HashMap<VertexType, Set<EdgeType>>();
        Vt2E = new HashMap<VertexType, Set<EdgeType>>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addEdge(Set<VertexType> sourceVertices, Set<VertexType> targetVertices) {
        DirectedHyperEdge<VertexType> edge = new DirectedHyperEdgeVector<VertexType>(sourceVertices,
                targetVertices);
        return addEdge((EdgeType)edge);
    }

    @Override
    public boolean addVertex(VertexType vertex) {
        boolean added = super.addVertex(vertex);
        if (added) {
            Vs2E.put(vertex, new HashSet<EdgeType>());
            Vt2E.put(vertex, new HashSet<EdgeType>());
        }
        return added;
    }

    @Override
    public boolean removeVertex(VertexType vertex) {
        boolean removed = super.removeVertex(vertex);
        if (removed) {
            Vs2E.remove(vertex);
            Vt2E.remove(vertex);
        }
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        Vs2E.clear();
        Vt2E.clear();
    }

    @Override
    public boolean addEdge(EdgeType edge) {
        boolean added = super.addEdge(edge);
        if (added) {
            for (VertexType v: edge.getSourceVertices()) {
                if (Vs2E.get(v) == null) {
                    Vs2E.put(v, new HashSet<EdgeType>());
                }
                if (Vt2E.get(v) == null) {
                    Vt2E.put(v, new HashSet<EdgeType>());
                }
                Vs2E.get(v).add(edge);
            }
            for (VertexType v: edge.getTargetVertices()) {
                if (Vs2E.get(v) == null) {
                    Vs2E.put(v, new HashSet<EdgeType>());
                }
                if (Vt2E.get(v) == null) {
                    Vt2E.put(v, new HashSet<EdgeType>());
                }
                Vt2E.get(v).add(edge);
            }
        }
        return added;
    }

    @Override
    public boolean removeEdge(EdgeType edge) {
        boolean removed = super.removeEdge(edge);
        if (removed) {
            for (VertexType v: edge.getSourceVertices()) {
                assert (Vs2E.get(v) != null);
                Vs2E.get(v).remove(edge);
            }
            for (VertexType v: edge.getTargetVertices()) {
                assert (Vt2E.get(v) != null);
                Vt2E.get(v).remove(edge);
            }
        }
        return removed;
    }

    @Override
    public Set<VertexType> findNeighborsOf(VertexType vertex) {
        Set<VertexType> vertices = new HashSet<VertexType>();
        for (EdgeType e: findEdgesWithSourceVertex(vertex)) {
            vertices.addAll(e.getTargetVertices());
        }
        return vertices;
    }

    @Override
    public Set<VertexType> findChildrenOf(VertexType vertex) {
        Set<VertexType> vertices = new HashSet<VertexType>();
        for (EdgeType edge: Vs2E.get(vertex)) {
            vertices.addAll(edge.getTargetVertices());
        }
        return vertices;
    }

    @Override
    public Set<EdgeType> findEdgesWithSourceVertex(VertexType vertex) {
        return Vs2E.get(vertex);
    }

    @Override
    public Set<EdgeType> findEdgesWithSourceVertices(Set<VertexType> vertices) {
        Set<EdgeType> edges = new HashSet<EdgeType>();
        if (vertices.size() == 0) {
            return edges;
        }
        boolean isFirstPass = true;
        for (VertexType v: vertices) {
            if (isFirstPass) {
                edges.addAll(findEdgesWithSourceVertex(v));
                isFirstPass = false;
            } else {
                edges.retainAll(findEdgesWithSourceVertex(v));
            }
            if (edges.size() == 0) {
                break;
            }
        }
        return edges;
    }

    @Override
    public Set<EdgeType> findEdgesWithTargetVertex(VertexType vertex) {
        return Vt2E.get(vertex);
    }

    @Override
    public Set<EdgeType> findEdgesWithTargetVertices(Set<VertexType> vertices) {
        Set<EdgeType> edges = new HashSet<EdgeType>();
        if (vertices.size() == 0) {
            return edges;
        }
        boolean isFirstPass = true;
        for (VertexType v: vertices) {
            if (isFirstPass) {
                edges.addAll(findEdgesWithTargetVertex(v));
                isFirstPass = false;
            } else {
                edges.retainAll(findEdgesWithTargetVertex(v));
            }
            if (edges.size() == 0) {
                break;
            }
        }
        return edges;
    }

}
