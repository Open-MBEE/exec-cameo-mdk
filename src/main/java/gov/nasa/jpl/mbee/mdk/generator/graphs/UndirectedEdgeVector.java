package gov.nasa.jpl.mbee.mdk.generator.graphs;

import java.util.Collection;

public class UndirectedEdgeVector<VertexType> extends UndirectedHyperEdgeVector<VertexType> implements
        UndirectedEdge<VertexType> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UndirectedEdgeVector(Collection<VertexType> vertices) {
        super(2, 0);
        assert (vertices.size() == 2);
        this.addAll(vertices);
    }

    public UndirectedEdgeVector(VertexType vertex0, VertexType vertex1) {
        super(2, 0);
        add(0, vertex0);
        add(1, vertex1);
    }

    @Override
    public synchronized String toString() {
        return get(0).toString() + " -- " + get(1).toString();
    }

}
