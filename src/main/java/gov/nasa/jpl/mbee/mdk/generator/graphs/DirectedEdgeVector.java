package gov.nasa.jpl.mbee.mdk.generator.graphs;

import java.util.HashSet;
import java.util.Set;

public class DirectedEdgeVector<VertexType> extends UndirectedEdgeVector<VertexType> implements
        DirectedEdge<VertexType> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DirectedEdgeVector(VertexType sourceVertex, VertexType targetVertex) {
        super(sourceVertex, targetVertex);
    }

    @Override
    public Set<VertexType> getSourceVertices() {
        Set<VertexType> vertices = new HashSet<VertexType>();
        vertices.add(getSourceVertex());
        return vertices;
    }

    @Override
    public Set<VertexType> getTargetVertices() {
        Set<VertexType> vertices = new HashSet<VertexType>();
        vertices.add(getTargetVertex());
        return vertices;
    }

    @Override
    public VertexType getSourceVertex() {
        return this.get(0);
    }

    @Override
    public VertexType getTargetVertex() {
        return this.get(1);
    }

    @Override
    public synchronized String toString() {
        return getSourceVertex().toString() + " -> " + getTargetVertex().toString();
    }

}
