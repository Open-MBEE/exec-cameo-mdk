package gov.nasa.jpl.mbee.mdk.generator.graphs;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractGraph<VertexType, EdgeType extends Edge<VertexType>> implements
        Graph<VertexType, EdgeType> {
    protected Set<VertexType> V;
    protected Set<EdgeType> E;

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
        for (VertexType v : vertices) {
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
        for (VertexType v : vertices) {
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
        for (EdgeType e : edges) {
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
        for (EdgeType e : edges) {
            removed = removeEdge(e) || removed;
        }
        return removed;
    }

    @Override
    public Set<EdgeType> findEdgesOf(VertexType vertex) {
        Set<EdgeType> edges = new HashSet<EdgeType>();
        for (EdgeType e : E) {
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
        }
        else {
            for (EdgeType e : E) {
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
        for (EdgeType e : findEdgesOf(vertex)) {
            vertices.addAll(e.getVertices());
        }
        vertices.remove(vertex);
        return vertices;
    }
}
