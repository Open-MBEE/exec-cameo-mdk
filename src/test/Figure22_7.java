

import java.util.SortedSet;

import gov.nasa.jpl.graphs.DirectedEdgeVector;
import gov.nasa.jpl.graphs.DirectedGraphHashSet;
import gov.nasa.jpl.graphs.algorithms.TopologicalSort;

public class Figure22_7
extends DirectedGraphHashSet<String, DirectedEdgeVector<String>> {
	public static void main(String[] args) {
		Figure22_7 figure22_7 = new Figure22_7();
		System.out.println(figure22_7.toString());
		TopologicalSort ts = new TopologicalSort();
		SortedSet<String> tsVertices = ts.topological_sort(figure22_7);
		System.out.println(tsVertices.toString());
	}
	
	public Figure22_7() {
		super();
		addEdge(new DirectedEdgeVector<String>(new String("undershorts"), new String("pants")));
		addEdge(new DirectedEdgeVector<String>(new String("undershorts"), new String("shoes")));
		addEdge(new DirectedEdgeVector<String>(new String("pants"), new String("belt")));
		addEdge(new DirectedEdgeVector<String>(new String("pants"), new String("shoes")));
		addEdge(new DirectedEdgeVector<String>(new String("socks"), new String("shoes")));
		addEdge(new DirectedEdgeVector<String>(new String("shirt"), new String("belt")));
		addEdge(new DirectedEdgeVector<String>(new String("shirt"), new String("tie")));
		addEdge(new DirectedEdgeVector<String>(new String("belt"), new String("jacket")));
		addEdge(new DirectedEdgeVector<String>(new String("tie"), new String("jacket")));
		addVertex(new String("watch"));
		
	}
}
