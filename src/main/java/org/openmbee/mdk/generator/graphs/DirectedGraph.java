package org.openmbee.mdk.generator.graphs;

public interface DirectedGraph<VertexType, EdgeType extends DirectedEdge<VertexType>> extends
        DirectedHyperGraph<VertexType, EdgeType> {
    boolean addEdge(VertexType sourceVertex, VertexType targetVertex);
}
