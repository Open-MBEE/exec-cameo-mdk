package org.openmbee.mdk.generator.graphs;

import java.util.Collection;
import java.util.List;

public interface Edge<VertexType> {
    List<VertexType> getVertices();

    boolean containsVertex(VertexType vertex);

    boolean containsAllVertices(Collection<VertexType> vertices);
}
