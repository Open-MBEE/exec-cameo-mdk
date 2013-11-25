package gov.nasa.jpl.graphs;

public interface DirectedGraph<VertexType, EdgeType extends DirectedEdge<VertexType>> extends
        DirectedHyperGraph<VertexType, EdgeType> {
    public boolean addEdge(VertexType sourceVertex, VertexType targetVertex);
}
