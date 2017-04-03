package gov.nasa.jpl.mbee.mdk.generator.graphs;

import java.util.Set;

public interface DirectedHyperGraph<VertexType, EdgeType extends DirectedHyperEdge<VertexType>> extends
        UndirectedHyperGraph<VertexType, EdgeType> {
    boolean addEdge(Set<VertexType> sourceVertices, Set<VertexType> targetVertices);

    Set<EdgeType> findEdgesWithSourceVertex(VertexType vertex);

    Set<EdgeType> findEdgesWithSourceVertices(Set<VertexType> vertices);

    Set<EdgeType> findEdgesWithTargetVertices(Set<VertexType> vertices);

    Set<EdgeType> findEdgesWithTargetVertex(VertexType vertex);
}
