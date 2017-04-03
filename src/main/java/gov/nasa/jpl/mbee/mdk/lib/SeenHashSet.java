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
package gov.nasa.jpl.mbee.mdk.lib;

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
