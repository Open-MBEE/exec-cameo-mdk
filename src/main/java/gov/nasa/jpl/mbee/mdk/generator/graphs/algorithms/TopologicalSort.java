package gov.nasa.jpl.mbee.mdk.generator.graphs.algorithms;

import gov.nasa.jpl.mbee.mdk.generator.graphs.Edge;
import gov.nasa.jpl.mbee.mdk.generator.graphs.Graph;

import java.util.*;

/**
 * A class for performing topological sort. This code is base on: [1] T. H.
 * Cormen, C. E. Leiserson, R. L. Rivest, and C. Stein. Introduction to
 * Algorithms. The MIT Press, Cambridge, Massachusetts, 2nd edition, 2001.
 *
 * @author shchung
 */
public class TopologicalSort {
    private class MapBasedComparator implements Comparator<Object> {
        private Map<Object, Integer> value;

        public MapBasedComparator(Map<Object, Integer> map) {
            value = map;
        }

        @Override
        public int compare(Object o1, Object o2) {
            if (value.get(o1) < value.get(o2)) {
                return -1;
            }
            else if (Objects.equals(value.get(o1), value.get(o2))) {
                return 0;
            }
            else {
                return 1;
            }
        }
    }

    /**
     * TOPOLOGICAL-SORT(G) 1. call DFS(G) to compute finishing times f [v] for
     * each vertex v 2. as each vertex is finished, insert it onto the front of
     * a linked list 3. return the linked list of vertices
     */
    public <VertexType, EdgeType extends Edge<VertexType>> SortedSet<VertexType> topological_sort(
            Graph<VertexType, EdgeType> G, Set<VertexType> roots) {
        // 1. call DFS(G) to compute finishing times f [v] for each vertex v
        DepthFirstSearch dfs = new DepthFirstSearch();
        dfs.dfs(G, roots);
        // 2. as each vertex is finished, insert it onto the front of a linked
        // list
        SortedSet<VertexType> sortedVertices = new TreeSet<VertexType>(new MapBasedComparator(dfs.f));
        sortedVertices.addAll(G.getVertices());
        // 3. return the linked list of vertices
        return sortedVertices;
    }

    public <VertexType, EdgeType extends Edge<VertexType>> SortedSet<VertexType> topological_sort(
            Graph<VertexType, EdgeType> G) {
        return topological_sort(G, G.getVertices());
    }
}
