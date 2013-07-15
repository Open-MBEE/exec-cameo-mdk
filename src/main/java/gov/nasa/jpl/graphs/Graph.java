package gov.nasa.jpl.graphs;
import java.util.Set;

public interface Graph<VertexType, EdgeType extends Edge<VertexType>> {
	public Set<VertexType>	getVertices();
	public Set<EdgeType>	getEdges();
	
	public void				clear();
	
	public boolean			addVertex(VertexType vertex);
	public boolean			addVertices(Set<VertexType> vertices);
	public boolean			removeVertex (VertexType vertex);
	public boolean			removeVertices (Set<VertexType> vertices);
	
	public boolean			addEdge(EdgeType edge);
	public boolean			addEdges(Set<EdgeType> edges);
	public boolean			removeEdge(EdgeType edge);
	public boolean			removeEdges(Set<EdgeType> edges);
	
	public Set<EdgeType>	findEdgesOf(VertexType vertex);
	public Set<EdgeType>	findEdgesOf(Set<VertexType> vertices);
	public Set<VertexType>	findNeighborsOf(VertexType vertex);
	public Set<VertexType>	findChildrenOf(VertexType vertex);
}
