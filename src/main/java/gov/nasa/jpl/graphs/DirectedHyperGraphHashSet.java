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
