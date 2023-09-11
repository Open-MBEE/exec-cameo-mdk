package org.openmbee.mdk.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * A hash set of seen (already visited) elements for avoiding infinite
 * recursion.
 * <p>
 * NOTE: Okay to use hash table since nothing changes based on the order. We
 * never need to iterate over the contents.
 */
public class SeenHashSet<E> extends HashSet<E> implements SeenSet<E> {

    /*
     * (non-Javadoc)
     *
     * @see gov.nasa.jpl.ae.util.SeenSet#seen(java.lang.Object, boolean)
     */
    @Override
    public boolean see(E element, boolean recursive) {
        if (contains(element)) {
            // ++seenCt;
            return true;
        }
        // ++notSeenCt;
        if (recursive == true) {
            add(element);
        }
        return false;
    }

    private static final long serialVersionUID = 3550974615402713022L;

    public SeenHashSet() {
        super();
    }

    /**
     * @param c
     */
    public SeenHashSet(Collection<? extends E> c) {
        super(c);
    }

    /**
     * @param initialCapacity
     */
    public SeenHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param initialCapacity
     * @param loadFactor
     */
    public SeenHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

}
