package org.openmbee.mdk.generator.graphs;

public interface DirectedEdge<VertexType> extends DirectedHyperEdge<VertexType>, UndirectedEdge<VertexType> {
    VertexType getSourceVertex();

    VertexType getTargetVertex();
}
