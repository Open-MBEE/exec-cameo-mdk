package gov.nasa.jpl.mbee.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Node in a "tree" for simplifying presentation as tree structures, although
 * the underlying model representation may actually be a directed graph - two
 * nodes can represent the same element, a key is used to identify these nodes
 * (key can be null right now), hence the same model element can actually have
 * >1 parents
 * 
 * @author dlam
 * 
 * @param <K>
 * @param <T>
 */
public class Node<K, T> {

    private Set<Node<K, T>>        children;
    private T                      data;
    private K                      key;
    private Node<K, T>             parent;
    private List<Node<K, T>>       list;
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
            if (comparator != null)
                Collections.sort(list, comparator);
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
        if (this.key == key || key.equals(this.key))
            result.add(this);
        for (Node<K, T> child: this.children) {
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
        if (prep == null)
            return this;
        return prep;
    }

    public List<Node<K, T>> getPathToRoot() {
        List<Node<K, T>> path = new ArrayList<Node<K, T>>();
        Node<K, T> curp = parent;
        Node<K, T> prep = parent;
        while (curp != null) {
            path.add(curp);
            prep = curp;
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
        for (Node<K, T> child: this.children) {
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
        for (Node<K, T> child: this.children) {
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
            for (Node<K, T> child: this.children) {
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
