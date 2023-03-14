package org.openmbee.mdk.generator.graphs;

import java.util.Set;

public interface DirectedHyperEdge<VertexType> extends UndirectedHyperEdge<VertexType> {
    Set<VertexType> getSourceVertices();

    Set<VertexType> getTargetVertices();
}
