package gov.nasa.jpl.graphs;

import java.util.Set;

public class DirectedGraphHashSet<VertexType, EdgeType extends DirectedEdge<VertexType>>
extends DirectedHyperGraphHashSet<VertexType, EdgeType>
implements DirectedGraph<VertexType, EdgeType> {

	@SuppressWarnings("unchecked")
	@Override
	public boolean addEdge(VertexType sourceVertex, VertexType targetVertex) {
		DirectedEdge<VertexType> edge = new DirectedEdgeVector<VertexType>(sourceVertex, targetVertex);
		return addEdge((EdgeType) edge);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean addEdge(Set<VertexType> sourceVertices, Set<VertexType> targetVertices) {
		assert(sourceVertices.size() == 1 && targetVertices.size() == 1);
		DirectedEdge<VertexType> edge = new DirectedEdgeVector<VertexType>(sourceVertices.iterator().next(), targetVertices.iterator().next());
		return addEdge((EdgeType) edge);
	}
}
