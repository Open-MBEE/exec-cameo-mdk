

import gov.nasa.jpl.graphs.DirectedEdgeVector;
import gov.nasa.jpl.graphs.DirectedGraphHashSet;

public class Figure22_2
extends DirectedGraphHashSet<Integer, DirectedEdgeVector<Integer>> {
	public Figure22_2() {
		super();
		addEdge(new DirectedEdgeVector<Integer>(new Integer(1), new Integer(2)));
		addEdge(new DirectedEdgeVector<Integer>(new Integer(1), new Integer(4)));
		addEdge(new DirectedEdgeVector<Integer>(new Integer(2), new Integer(5)));
		addEdge(new DirectedEdgeVector<Integer>(new Integer(3), new Integer(5)));
		addEdge(new DirectedEdgeVector<Integer>(new Integer(3), new Integer(6)));
		addEdge(new DirectedEdgeVector<Integer>(new Integer(4), new Integer(2)));
		addEdge(new DirectedEdgeVector<Integer>(new Integer(5), new Integer(4)));
		addEdge(new DirectedEdgeVector<Integer>(new Integer(6), new Integer(6)));
	}	
}
