/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.graphs.algorithms;

import gov.nasa.jpl.graphs.DirectedEdge;
import gov.nasa.jpl.graphs.DirectedEdgeVector;
import gov.nasa.jpl.graphs.DirectedGraph;
import gov.nasa.jpl.graphs.DirectedGraphHashSet;
import gov.nasa.jpl.graphs.Edge;
import gov.nasa.jpl.graphs.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Performs depth first search on a graph. For references relating to this code
 * see: [1] T. H. Cormen, C. E. Leiserson, R. L. Rivest, and C. Stein.
 * Introduction to Algorithms. The MIT Press, Cambridge, Massachusetts, 2nd
 * edition, 2001.
 * 
 * @author shchung
 */

public class DepthFirstSearch {
    // WHITE = not discovered, GRAY = discovered, and BLACK = finished
    private static enum Color {
        WHITE, GRAY, BLACK
    }

    private Map<Object, Color>  color; // colors of the vertices
    private Integer             time;

    public Map<Object, Integer> d;    // discovery times of the vertices
    public Map<Object, Integer> f;    // finish times of the vertices

    public DepthFirstSearch() {
        color = new HashMap<Object, Color>();
        d = new HashMap<Object, Integer>();
        f = new HashMap<Object, Integer>();
    }

    private void clear() {
        color.clear();
        d.clear();
        f.clear();
    }

    /**
     * Performs a depth first search for a given graph G. For a reference see
     * [1] pg. 541. Following is the pseudo code from the aforementioned
     * reference.
     * 
     * <pre>
     * DFS(G)
     * </pre>
     * <ol>
     * <li>
     * 
     * <pre>
     * for each vertex u &isin; V[G]
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * do color[u] &larr; WHITE
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * &pi;[u] &larr; NIL
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * time &larr; 0
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * for each vertex u &isin; V[G]
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * do if color[u] = WHITE
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * then DFS-VISIT(u)
     * </pre>
     * 
     * </li>
     * </ol>
     * 
     * <pre>
     * DFS - VISIT(u)
     * </pre>
     * <ol>
     * <li>
     * 
     * <pre>
     * color[u] &larr; GRAY *WHITE vertex u has just been discovered.
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * time &larr; time + 1
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * d[u] &larr; time
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * for each v &isin; Adj[u] *Explore edge(u,v).
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * do if color[v] = WHITE
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * then &pi;[v] &larr; u
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * DFS - VISIT(v)
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * color[u] &larr; BLACK *BLACKen u;
     * </pre>
     * 
     * </li>
     * </ol>
     * 
     * Modifications:
     * <ul>
     * <li>"vertices" key was added to enable user defined search ordering at
     * the top level.</li>
     * <li>Added a hash table that keeps track of the children of the search
     * tree.</li>
     * </ul>
     * 
     * @param G
     *            a graph.
     * @param roots
     *            a set of vertices from which depth first search should be
     *            performed.
     */
    // DFS(G)
    public <VertexType, EdgeType extends Edge<VertexType>> DirectedGraph<VertexType, DirectedEdge<VertexType>> dfs(
            Graph<VertexType, EdgeType> G, Set<VertexType> roots) {
        clear();
        DirectedGraph<VertexType, DirectedEdge<VertexType>> pi = new DirectedGraphHashSet<VertexType, DirectedEdge<VertexType>>();
        // parents of the vertices

        // 1 for each vertex u \in V[G]
        for (VertexType u: roots) {
            // 2 color[u] <- WHITE
            color.put(u, Color.WHITE);
            // 3 \pi[u] <- NIL
            pi.addVertex(u);
        }
        // 4 time <- 0
        time = 0;
        // 5 for each vertex u \in V[G]
        for (VertexType u: roots) {
            // 6 do if color[u] = WHITE
            if (color.get(u) == Color.WHITE) {
                // 7 then DFS-VISIT(u)
                dfs_visit(G, u, pi);
            }
        }
        return pi;
    }

    /**
     * Performs a depth first search for a given graph G. For a reference see
     * [1] pg. 541. The depth first search is performed from all vertices of the
     * graph graph.
     * 
     * @param G
     *            a graph.
     */
    public <VertexType, EdgeType extends Edge<VertexType>> DirectedGraph<VertexType, DirectedEdge<VertexType>> dfs(
            Graph<VertexType, EdgeType> G) {
        return dfs(G, G.getVertices());
    }

    /**
     * Performs a depth first visit for a given graph G as described in [1] pg.
     * 541.
     * 
     * <pre>
     * DFS - VISIT(u)
     * </pre>
     * <ol>
     * <li>
     * 
     * <pre>
     * color[u] &larr; GRAY *WHITE vertex u has just been discovered.
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * time &larr; time + 1
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * d[u] &larr; time
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * for each v &isin; Adj[u] *Explore edge(u,v).
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * do if color[v] = WHITE
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * then &pi;[v] &larr; u
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * DFS - VISIT(v)
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * color[u] &larr; BLACK *BLACKen u;
     * </pre>
     * 
     * </li>
     * </ol>
     * 
     * @param u
     *            a vertex to be visited
     */
    private <VertexType, EdgeType extends Edge<VertexType>> void dfs_visit(Graph<VertexType, EdgeType> G,
            VertexType u, DirectedGraph<VertexType, DirectedEdge<VertexType>> pi) {
        // 1 color[u] <- GRAY -> WHITE vertex u has just been discovered.
        color.put(u, Color.GRAY);
        // 2 time <- time + 1
        time++;
        // 3 d[u] <- time
        d.put(u, time);
        // guiLog.log("Discovered: " + u.getName() + " , " + str(d[u]))
        // guiLog.log("Children(" + u.getName() + "): " + str(map(lambda x:
        // x.getName(), G.getChildren(u))))
        // 4 for each v \in Adj[u] -> Explore edge(u,v).
        for (VertexType v: G.findChildrenOf(u)) {
            // 5 do if color[v] = WHITE
            if (color.get(v) == Color.WHITE) {
                // 6 then \pi[v] <- u
                pi.addEdge(new DirectedEdgeVector<VertexType>(u, v));
                // 7 DFS-VISIT(v)
                dfs_visit(G, v, pi);
            }
        }
        // 8 color[u] <- BLACK -> BLACKen u; it is finished.
        color.put(u, Color.BLACK);
        // 9 f[u] <- time <- time + 1
        f.put(u, ++time);
    }
}
