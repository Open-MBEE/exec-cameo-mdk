package gov.nasa.jpl.mbee.mdk.generator.graphs;

import java.util.Set;

public class DirectedHyperEdgeVector<VertexType> extends UndirectedHyperEdgeVector<VertexType> implements
        DirectedHyperEdge<VertexType> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected Set<VertexType> sourceVertices;
    protected Set<VertexType> targetVertices;

    public DirectedHyperEdgeVector(Set<VertexType> sourceVertices, Set<VertexType> targetVertices) {
        super();
        addAll(sourceVertices);
        addAll(targetVertices);
        sourceVertices.addAll(sourceVertices);
        targetVertices.addAll(targetVertices);
    }

    @Override
    public Set<VertexType> getSourceVertices() {
        return sourceVertices;
    }

    @Override
    public Set<VertexType> getTargetVertices() {
        return targetVertices;
    }

    @Override
    public synchronized String toString() {
        String string = "[";
        boolean isFirst = true;
        for (VertexType v : getSourceVertices()) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                string = string.concat(", ");
            }
            string = string.concat(v.toString());
        }
        string = string.concat("] -> [");
        isFirst = true;
        for (VertexType v : getTargetVertices()) {
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
