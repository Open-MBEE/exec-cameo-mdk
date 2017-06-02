package gov.nasa.jpl.mbee.mdk.generator.graphs;

import java.util.Set;

public interface Graph<VertexType, EdgeType extends Edge<VertexType>> {
    Set<VertexType> getVertices();

    Set<EdgeType> getEdges();

    void clear();

    boolean addVertex(VertexType vertex);

    boolean addVertices(Set<VertexType> vertices);

    boolean removeVertex(VertexType vertex);

    boolean removeVertices(Set<VertexType> vertices);

    boolean addEdge(EdgeType edge);

    boolean addEdges(Set<EdgeType> edges);

    boolean removeEdge(EdgeType edge);

    boolean removeEdges(Set<EdgeType> edges);

    Set<EdgeType> findEdgesOf(VertexType vertex);

    Set<EdgeType> findEdgesOf(Set<VertexType> vertices);

    Set<VertexType> findNeighborsOf(VertexType vertex);

    Set<VertexType> findChildrenOf(VertexType vertex);
}
