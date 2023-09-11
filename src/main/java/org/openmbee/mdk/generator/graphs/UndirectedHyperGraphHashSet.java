package org.openmbee.mdk.generator.graphs;

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
            for (VertexType v : edge.getVertices()) {
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
            for (VertexType v : edge.getVertices()) {
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
        for (VertexType v : vertices) {
            if (isFirstPass) {
                edges.addAll(V2E.get(v));
                isFirstPass = false;
            }
            else {
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
        for (VertexType v : getVertices()) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                string = string.concat(", ");
            }
            string = string.concat(v.toString());
        }
        for (EdgeType e : getEdges()) {
            string = string.concat("\n" + e.toString());
        }
        return string;
    }
}
