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

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractGraph<VertexType, EdgeType extends Edge<VertexType>> implements
        Graph<VertexType, EdgeType> {
    protected Set<VertexType> V;
    protected Set<EdgeType>   E;

    public AbstractGraph() {
        V = new HashSet<VertexType>();
        E = new HashSet<EdgeType>();
    }

    @Override
    public Set<VertexType> getVertices() {
        return V;
    }

    @Override
    public Set<EdgeType> getEdges() {
        return E;
    }

    @Override
    public void clear() {
        V.clear();
        E.clear();
    }

    @Override
    public boolean addVertex(VertexType vertex) {
        return V.add(vertex);
    }

    @Override
    public boolean addVertices(Set<VertexType> vertices) {
        boolean added = false;
        for (VertexType v: vertices) {
            added = addVertex(v) || added;
        }
        return added;
    }

    @Override
    public boolean removeVertex(VertexType vertex) {
        boolean removed = V.remove(vertex);
        if (removed) {
            E.removeAll(findEdgesOf(vertex));
        }
        return removed;
    }

    @Override
    public boolean removeVertices(Set<VertexType> vertices) {
        boolean removed = false;
        for (VertexType v: vertices) {
            removed = removeVertex(v) || removed;
        }
        return removed;
    }

    @Override
    public boolean addEdge(EdgeType edge) {
        boolean added = E.add(edge);
        if (added) {
            V.addAll(edge.getVertices());
        }
        return added;
    }

    @Override
    public boolean addEdges(Set<EdgeType> edges) {
        boolean added = false;
        for (EdgeType e: edges) {
            added = addEdge(e) || added;
        }
        return added;
    }

    @Override
    public boolean removeEdge(EdgeType edge) {
        return E.remove(edge);
    }

    @Override
    public boolean removeEdges(Set<EdgeType> edges) {
        boolean removed = false;
        for (EdgeType e: edges) {
            removed = removeEdge(e) || removed;
        }
        return removed;
    }

    @Override
    public Set<EdgeType> findEdgesOf(VertexType vertex) {
        Set<EdgeType> edges = new HashSet<EdgeType>();
        for (EdgeType e: E) {
            if (e.containsVertex(vertex)) {
                edges.add(e);
            }
        }
        return edges;
    }

    @Override
    public Set<EdgeType> findEdgesOf(Set<VertexType> vertices) {
        Set<EdgeType> edges = new HashSet<EdgeType>();
        if (vertices.isEmpty()) {
            return edges;
        } else {
            for (EdgeType e: E) {
                if (e.containsAllVertices(vertices)) {
                    edges.add(e);
                }
            }
            return edges;
        }
    }

    @Override
    public Set<VertexType> findNeighborsOf(VertexType vertex) {
        Set<VertexType> vertices = new HashSet<VertexType>();
        for (EdgeType e: findEdgesOf(vertex)) {
            vertices.addAll(e.getVertices());
        }
        vertices.remove(vertex);
        return vertices;
    }
}
