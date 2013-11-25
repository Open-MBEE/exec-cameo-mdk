package gov.nasa.jpl.graphs;

public interface DirectedEdge<VertexType> extends DirectedHyperEdge<VertexType>, UndirectedEdge<VertexType> {
    public VertexType getSourceVertex();

    public VertexType getTargetVertex();
}
