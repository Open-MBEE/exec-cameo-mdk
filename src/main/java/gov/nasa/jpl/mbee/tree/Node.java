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
package gov.nasa.jpl.mbee.tree;

import java.util.*;

/**
 * Node in a "tree" for simplifying presentation as tree structures, although
 * the underlying model representation may actually be a directed graph - two
 * nodes can represent the same element, a key is used to identify these nodes
 * (key can be null right now), hence the same model element can actually have
 * >1 parents
 *
 * @param <K>
 * @param <T>
 * @author dlam
 */
public class Node<K, T> {

    private Set<Node<K, T>> children;
    private T data;
    private K key;
    private Node<K, T> parent;
    private List<Node<K, T>> list;
    private Comparator<Node<K, T>> comparator;

    public Node(K key, T data) {
        this.key = key;
        this.data = data;
        children = new HashSet<Node<K, T>>();
        list = new ArrayList<Node<K, T>>();
    }

    public Node(K key, T data, Comparator<Node<K, T>> compare) {
        this.key = key;
        this.data = data;
        this.comparator = compare;
        children = new HashSet<Node<K, T>>();
        list = new ArrayList<Node<K, T>>();
    }

    public Set<Node<K, T>> getChildren() {
        return children;
    }

    public List<Node<K, T>> getChildrenAsList() {
        return list;
    }

    public void addChild(Node<K, T> child) {
        if (!children.contains(child)) {
            children.add(child);
            list.add(child);
            if (comparator != null) {
                Collections.sort(list, comparator);
            }
            child.setParent(this);
            child.setComparator(this.comparator);
        }
    }

    public Node<K, T> removeChild(Node<K, T> child) {
        children.remove(child);
        list.remove(child);
        child.setParent(null);
        child.setComparator(null);
        return child;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public K getKey() {
        return key;
    }

    public Node<K, T> getParent() {
        return parent;
    }

    public void setParent(Node<K, T> parent) {
        this.parent = parent;
    }

    /**
     * get all children nodes with current node as root, that represents key
     *
     * @param key
     * @return
     */
    public Set<Node<K, T>> getNodes(K key) {
        Set<Node<K, T>> result = new HashSet<Node<K, T>>();
        return getNodes(key, result);
    }

    public Set<Node<K, T>> getNodes(K key, Set<Node<K, T>> result) {
        if (this.key == key || key.equals(this.key)) {
            result.add(this);
        }
        for (Node<K, T> child : this.children) {
            child.getNodes(key, result);
        }
        return result;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * distance from root of tree (root is 0)
     *
     * @return
     */
    public int getLevel() {
        int level = 0;
        Node<K, T> p = parent;
        while (p != null) {
            ++level;
            p = p.getParent();
        }
        return level;
    }

    /**
     * get root of tree
     *
     * @return
     */
    public Node<K, T> getRoot() {
        Node<K, T> curp = parent;
        Node<K, T> prep = parent;
        while (curp != null) {
            prep = curp;
            curp = curp.getParent();
        }
        if (prep == null) {
            return this;
        }
        return prep;
    }

    public List<Node<K, T>> getPathToRoot() {
        List<Node<K, T>> path = new ArrayList<Node<K, T>>();
        Node<K, T> curp = parent;
        while (curp != null) {
            path.add(curp);
            curp = curp.getParent();
        }
        return path;
    }

    /**
     * get all children data nodes with current node as root
     *
     * @param key
     * @return
     */
    public Set<T> getAllData() {
        Set<T> result = new HashSet<T>();
        return getAllData(result);
    }

    public Set<T> getAllData(Set<T> result) {
        result.add(this.data);
        for (Node<K, T> child : this.children) {
            child.getAllData(result);
        }
        return result;
    }

    /**
     * get all children data nodes with current node as root
     *
     * @param key
     * @return
     */
    public Set<Node<K, T>> getAllNodes() {
        Set<Node<K, T>> result = new HashSet<Node<K, T>>();
        return getAllNodes(result);
    }

    public Set<Node<K, T>> getAllNodes(Set<Node<K, T>> result) {
        result.add(this);
        for (Node<K, T> child : this.children) {
            child.getAllNodes(result);
        }
        return result;
    }

    public void setComparator(Comparator<Node<K, T>> compare) {
        this.comparator = compare;
    }

    public Comparator<Node<K, T>> getComparator() {
        return comparator;
    }

    public void sortAllChildren() {
        if (comparator != null) {
            for (Node<K, T> child : this.children) {
                child.setComparator(comparator);
                child.sortAllChildren();
            }
            Collections.sort(list, comparator);
        }
    }

    public void sortAllChildren(Comparator<Node<K, T>> compare) {
        this.comparator = compare;
        sortAllChildren();
    }
}
