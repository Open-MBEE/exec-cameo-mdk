package gov.nasa.jpl.graphs;

import java.util.Set;

public interface DirectedHyperEdge<VertexType> extends UndirectedHyperEdge<VertexType> {
    public Set<VertexType> getSourceVertices();

    public Set<VertexType> getTargetVertices();
}
