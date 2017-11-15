package gov.nasa.jpl.mbee.mdk.generator.graphs;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class UndirectedHyperEdgeVector<VertexType> extends Vector<VertexType> implements
        UndirectedHyperEdge<VertexType> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UndirectedHyperEdgeVector(Collection<VertexType> vertices) {
        super(vertices);
    }

    protected UndirectedHyperEdgeVector(int initialCapacity, int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }

    protected UndirectedHyperEdgeVector() {
        super();
    }

    @Override
    public List<VertexType> getVertices() {
        return this;
    }

    @Override
    public boolean containsVertex(VertexType vertex) {
        return super.contains(vertex);
    }

    @Override
    public boolean containsAllVertices(Collection<VertexType> vertices) {
        return super.containsAll(vertices);
    }

    @Override
    public synchronized String toString() {
        String string = "[";
        boolean isFirst = true;
        for (VertexType v : getVertices()) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                string = string.concat(", ");
            }
            string = string.concat(v.toString());
        }
        string = string.concat("]");
        return string;
    }
}
