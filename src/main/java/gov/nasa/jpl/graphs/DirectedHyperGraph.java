package gov.nasa.jpl.graphs;

import java.util.Set;

public interface DirectedHyperGraph<VertexType, EdgeType extends DirectedHyperEdge<VertexType>>
extends UndirectedHyperGraph<VertexType, EdgeType> {
	public boolean			addEdge(Set<VertexType> sourceVertices, Set<VertexType> targetVertices);
	
	public Set<EdgeType>	findEdgesWithSourceVertex(VertexType vertex);
	public Set<EdgeType>	findEdgesWithSourceVertices(Set<VertexType> vertices);
	public Set<EdgeType>	findEdgesWithTargetVertices(Set<VertexType> vertices);
	public Set<EdgeType>	findEdgesWithTargetVertex(VertexType vertex);
}
