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

import gov.nasa.jpl.graphs.Edge;
import gov.nasa.jpl.graphs.Graph;

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
            else if (value.get(o1) == value.get(o2)) {
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
