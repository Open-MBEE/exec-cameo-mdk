package gov.nasa.jpl.graphs;

import java.util.Collection;
import java.util.List;

public interface Edge<VertexType> {
    public List<VertexType> getVertices();

    public boolean containsVertex(VertexType vertex);

    public boolean containsAllVertices(Collection<VertexType> vertices);
}
