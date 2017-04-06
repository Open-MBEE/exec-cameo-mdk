package gov.nasa.jpl.mbee.mdk.generator.graphs;

public interface UndirectedGraph<VertexType, EdgeType extends UndirectedEdge<VertexType>> extends
        UndirectedHyperGraph<VertexType, EdgeType> {
    boolean addEdge(VertexType vertex1, VertexType vertex2);
}
