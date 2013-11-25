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

public class UndirectedHyperGraphHashSet<VertexType, EdgeType extends UndirectedHyperEdge<VertexType>>
        extends AbstractGraph<VertexType, EdgeType> implements UndirectedHyperGraph<VertexType, EdgeType> {
    protected Map<VertexType, Set<EdgeType>> V2E;

    public UndirectedHyperGraphHashSet() {
        super();
        V2E = new HashMap<VertexType, Set<EdgeType>>();
    }

    @Override
    public boolean addVertex(VertexType vertex) {
        boolean added = super.addVertex(vertex);
        if (added) {
            V2E.put(vertex, new HashSet<EdgeType>());
        }
        return added;
    }

    @Override
    public boolean removeVertex(VertexType vertex) {
        boolean removed = super.removeVertex(vertex);
        if (removed) {
            V2E.remove(vertex);
        }
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        V2E.clear();
    }

    @Override
    public boolean addEdge(EdgeType edge) {
        boolean added = super.addEdge(edge);
        if (added) {
            for (VertexType v: edge.getVertices()) {
                if (V2E.get(v) == null) {
                    V2E.put(v, new HashSet<EdgeType>());
                }
                V2E.get(v).add(edge);
            }
        }
        return added;
    }

    @Override
    public boolean removeEdge(EdgeType edge) {
        boolean removed = super.removeEdge(edge);
        if (removed) {
            for (VertexType v: edge.getVertices()) {
                assert (V2E.get(v) != null);
                V2E.get(v).remove(edge);
            }
        }
        return removed;
    }

    @Override
    public Set<EdgeType> findEdgesOf(VertexType vertex) {
        return V2E.get(vertex);
    }

    @Override
    public Set<EdgeType> findEdgesOf(Set<VertexType> vertices) {
        Set<EdgeType> edges = new HashSet<EdgeType>();
        if (vertices.isEmpty()) {
            return edges;
        }
        boolean isFirstPass = true;
        for (VertexType v: vertices) {
            if (isFirstPass) {
                edges.addAll(V2E.get(v));
                isFirstPass = false;
            } else {
                edges.retainAll(V2E.get(v));
            }
            if (edges.isEmpty()) {
                break;
            }
        }
        return edges;
    }

    @Override
    public Set<VertexType> findChildrenOf(VertexType vertex) {
        return findNeighborsOf(vertex);
    }

    @Override
    public String toString() {
        String string = new String();
        boolean isFirst = true;
        for (VertexType v: getVertices()) {
            if (isFirst) {
                isFirst = false;
            } else {
                string = string.concat(", ");
            }
            string = string.concat(v.toString());
        }
        for (EdgeType e: getEdges()) {
            string = string.concat("\n" + e.toString());
        }
        return string;
    }
}
