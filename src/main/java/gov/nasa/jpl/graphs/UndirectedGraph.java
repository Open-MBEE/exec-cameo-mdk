package gov.nasa.jpl.graphs;

public interface UndirectedGraph<VertexType, EdgeType extends UndirectedEdge<VertexType>> extends
        UndirectedHyperGraph<VertexType, EdgeType> {
    public boolean addEdge(VertexType vertex1, VertexType vertex2);
}
