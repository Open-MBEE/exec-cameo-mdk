package gov.nasa.jpl.mbee.mdk.generator.graphs;

public interface DirectedEdge<VertexType> extends DirectedHyperEdge<VertexType>, UndirectedEdge<VertexType> {
    VertexType getSourceVertex();

    VertexType getTargetVertex();
}
