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

import javafx.util.Pair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import org.apache.commons.lang.StringUtils;

/**
 * A set of miscellaneous utility functions.
 */
public class Utils2 {

    // static members

    public static ClassLoader loader = null;

    // empty collection constants
    public static final List<?> emptyList = Collections.EMPTY_LIST; // new
    // ArrayList(
    // 0 );

    @SuppressWarnings("unchecked")
    public static <T> List<T> getEmptyList() {
        return (List<T>) emptyList;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getEmptyList(Class<T> cls) {
        return (List<T>) emptyList;
    }

    public static final Set<?> emptySet = Collections.EMPTY_SET; // new
    // TreeSet();

    @SuppressWarnings("unchecked")
    public static <T> Set<T> getEmptySet() {
        return (Set<T>) emptySet;
    }

    public static final Map<?, ?> emptyMap = Collections.EMPTY_MAP; // new
    // TreeMap();

    @SuppressWarnings("unchecked")
    public static <T1, T2> Map<T1, T2> getEmptyMap() {
        return (Map<T1, T2>) emptyMap;
    }

    public static String toString(Object[] arr) {
        return toString(arr, true);
    }

    public static String toString(Object[] arr, boolean square) {
        if (arr == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        if (square) {
            sb.append("[");
        }
        else {
            sb.append("(");
        }
        for (int i = 0; i < arr.length; ++i) {// Object o : arr ) {
            if (i > 0) {
                sb.append(",");
            }
            if (arr[i] == null) {
                sb.append("null");
            }
            else {
                sb.append(arr[i].toString());
            }
        }
        if (square) {
            sb.append("]");
        }
        else {
            sb.append(")");
        }

        return sb.toString();
    }

    /**
     * Translate a string s to an Integer.
     *
     * @param s is the string to parse as an Integer
     * @return the integer translation of string s, or return null if s is not
     * an integer.
     */
    public static Integer toInteger(String s) {
        Integer i = null;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            // leave i = null
        }
        return i;
    }

    /**
     * Translate a string s to an Long.
     *
     * @param s is the string to parse as an Long
     * @return the long translation of string s, or return null if s is not a
     * long.
     */
    public static Long toLong(String s) {
        Long l = null;
        try {
            l = Long.parseLong(s);
        } catch (NumberFormatException e) {
            // leave i = null
        }
        return l;
    }

    /**
     * Translate a string s to a Double.
     *
     * @param s is the string to parse as a Double
     * @return the double translation of string s, or return null if s is not a
     * double/integer.
     */
    public static Double toDouble(String s) {
        Double i = null;
        try {
            i = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            // leave i = null
        }
        return i;
    }

    /**
     * Generate a string that repeats/replicates a string a specified number of
     * times.
     *
     * @param s     is the string to repeat.
     * @param times is the number of times to repeat the string.
     * @return a concatenation of times instances of s.
     */
    public static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String spaces(int n) {
        return repeat(" ", n);
    }

    public static String numberWithLeadingZeroes(int n, int totalChars) {
        Formatter formatter = new Formatter(Locale.US);
        String suffix = "" + formatter.format("%0" + totalChars + "d", n);
        formatter.close();
        return suffix;
    }

    public static boolean isNullOrEmpty(Object s) {
        if (s == null) {
            return true;
        }
        if (s.getClass().isArray()) {
            return isNullOrEmpty((Object[]) s);
        }
        if (s instanceof String) {
            return isNullOrEmpty((String) s);
        }
        if (s instanceof Collection) {
            return isNullOrEmpty((Collection<?>) s);
        }
        if (s instanceof Map) {
            return isNullOrEmpty((Map<?, ?>) s);
        }
        return false;
    }

    // Check if string has really got something.
    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.isEmpty() || s.trim().toLowerCase().equals("null"));
    }

    // Check if array has really got something.
    public static boolean isNullOrEmpty(Object[] s) {
        return (s == null || s.length == 0);
    }

    // Check if Collection has really got something.
    public static boolean isNullOrEmpty(Collection<?> s) {
        return (s == null || s.isEmpty());
    }

    // Check if Map has really got something.
    public static boolean isNullOrEmpty(Map<?, ?> s) {
        return (s == null || s.isEmpty());
    }

    // generic map<X, Set<Y>>.add(x, y)
    public static <T1, T2> boolean addToSet(Map<T1, Set<T2>> map, T1 t1, T2 t2) {
        if (Debug.errorOnNull("Error! Called Utils.add() with null argument!", map, t1, t2)) {
            return false;
        }
        boolean affected = false;
        Set<T2> innerCollection = map.get(t1);
        if (innerCollection == null) {
            innerCollection = new TreeSet<T2>(CompareUtils.GenericComparator.instance());
            map.put(t1, innerCollection);
        }
        affected = innerCollection.add(t2);
        return affected;
    }

    // generic map<X, Collection<Y>>.add(x, y)
    public static <T1, T2> boolean add(Map<T1, Collection<T2>> map, T1 t1, T2 t2) {
        if (Debug.errorOnNull("Error! Called Utils.add() with null argument!", map, t1, t2)) {
            return false;
        }
        boolean affected = false;
        Collection<T2> innerCollection = map.get(t1);
        if (innerCollection == null) {
            innerCollection = new ArrayList<T2>();
            map.put(t1, innerCollection);
        }
        affected = innerCollection.add(t2);
        return affected;
    }

    // generic map<X, Set<Y>>.addAll(x, y)
    public static <T1, T2> boolean addAllToSet(Map<T1, Set<T2>> map, T1 t1, Collection<T2> t2Collection) {
        if (Debug.errorOnNull("Error! Called Utils.add() with null argument!", map, t1, t2Collection)) {
            return false;
        }
        boolean affected = false;
        Set<T2> innerCollection = map.get(t1);
        if (innerCollection == null) {
            innerCollection = new HashSet<T2>();
            map.put(t1, innerCollection);
        }
        affected = innerCollection.addAll(t2Collection);
        return affected;
    }

    // generic map<X, Collection<Y>>.addAll(x, y)
    public static <T1, T2> boolean addAll(Map<T1, Collection<T2>> map, T1 t1, Collection<T2> t2Collection) {
        if (Debug.errorOnNull("Error! Called Utils.add() with null argument!", map, t1, t2Collection)) {
            return false;
        }
        boolean affected = false;
        Collection<T2> innerCollection = map.get(t1);
        if (innerCollection == null) {
            innerCollection = new ArrayList<T2>();
            map.put(t1, innerCollection);
        }
        affected = innerCollection.addAll(t2Collection);
        return affected;
    }

    // generic map<X, map<Y, Z>>.put(x, y, z)
    public static <T1, T2, T3> T3 put(Map<T1, Map<T2, T3>> map, T1 t1, T2 t2, T3 t3) {
        if (Debug.errorOnNull("Error! Called Utils.put() with null argument!", map, t1, t2, t3)) {
            return null;
        }
        Map<T2, T3> innerMap = map.get(t1);
        if (innerMap == null) {
            innerMap = new TreeMap<T2, T3>(CompareUtils.GenericComparator.instance());
            map.put(t1, innerMap);
        }
        return innerMap.put(t2, t3);
    }

    // generic map<X, map<Y, Collection<Z>>>.add(x, y, z)
    public static <T1, T2, T3> boolean add(Map<T1, Map<T2, Collection<T3>>> map, T1 t1, T2 t2, T3 t3) {
        if (Debug.errorOnNull("Error! Called Utils.add() with null argument!", map, t1, t2, t3)) {
            return false;
        }
        boolean affected = false;
        Collection<T3> innerCollection = get(map, t1, t2);
        if (innerCollection == null) {
            innerCollection = new ArrayList<T3>();
            put(map, t1, t2, innerCollection);
        }
        if (!innerCollection.contains(t3)) {
            affected = innerCollection.add(t3);
        }
        return affected;
    }

    // generic map<X, map<Y, Collection<Z>>>.add(x, y, z)
    public static <T1, T2, T3> boolean addAll(Map<T1, Map<T2, Collection<T3>>> map, T1 t1, T2 t2,
                                              Collection<T3> t3Collection) {
        if (Debug.errorOnNull("Error! Called Utils.add() with null argument!", map, t1, t2, t3Collection)) {
            return false;
        }
        boolean affected = false;
        Collection<T3> innerCollection = get(map, t1, t2);
        if (innerCollection == null) {
            innerCollection = new HashSet<T3>();
            put(map, t1, t2, innerCollection);
        }
        affected = innerCollection.addAll(t3Collection);
        return affected;
    }

    // generic map<X, map<Y, Z> >.get(x, y) --> z
    public static <T1, T2, T3> T3 get(Map<T1, Map<T2, T3>> map, T1 t1, T2 t2) {
        if (Debug.errorOnNull("Error! Called Utils.get() with null argument!", map, t1, t2)) {
            return null;
        }
        Map<T2, T3> innerMap = map.get(t1);
        if (innerMap != null) {
            return innerMap.get(t2);
        }
        return null;
    }

    // generic map< W, map<X, map<Y, Z> >.put(w, x, y, z)
    public static <T1, T2, T3, T4> T4 put(Map<T1, Map<T2, Map<T3, T4>>> map, T1 t1, T2 t2, T3 t3, T4 t4) {
        if (Debug.errorOnNull("Error! Called Utils.put() with null argument!", map, t1, t2, t3, t4)) {
            return null;
        }
        Map<T2, Map<T3, T4>> innerMap = map.get(t1);
        if (innerMap == null) {
            innerMap = new TreeMap<T2, Map<T3, T4>>(CompareUtils.GenericComparator.instance());
            map.put(t1, innerMap);
        }
        return put(innerMap, t2, t3, t4);
    }

    // generic map< W, map<X, map<Y, Z> >.get(w, x, y) --> z
    public static <T1, T2, T3, T4> T4 get(Map<T1, Map<T2, Map<T3, T4>>> map, T1 t1, T2 t2, T3 t3) {
        if (Debug.errorOnNull("Error! Called Utils.get() with null argument!", map, t1, t2, t3)) {
            return null;
        }
        Map<T2, Map<T3, T4>> innerMap = map.get(t1);
        if (innerMap != null) {
            return get(innerMap, t2, t3);
        }
        return null;
    }

    /**
     * Manages a "seen" set for avoiding infinite recursion.
     *
     * @param o         is the object visited
     * @param recursive is whether infinite recursion is possible
     * @param seen      is the set of objects already visited
     * @return whether the object has already been visited
     */
    public static <T> Pair<Boolean, SeenSet<T>> seen(T o, boolean recursive, SeenSet<T> seen) {
        // boolean hadSeen = false;
        // if ( seen == null && recursive ) {
        // seen = new SeenHashSet< T >();
        // seen.add( o );
        // }
        // seen.see( o, recursive );
        if (seen != null && seen.contains(o)) {
            // ++seenCt;
            return new Pair<Boolean, SeenSet<T>>(seen.see(o, recursive), seen);
        }
        // ++notSeenCt;
        if (seen == null && recursive == true) {
            seen = new SeenHashSet<T>(); // ok to use hash here since we never
            // iterate
            // over the contents
        }
        if (seen != null) {
            seen.add(o);
        }
        return new Pair<Boolean, SeenSet<T>>(false, seen);
    }

    // private static long notSeenCt = 0;
    // private static long seenCt = 0;

    /**
     * Manages a "seen" set for avoiding infinite recursion.
     *
     * @param o         is the object visited
     * @param recursive is whether infinite recursion is possible
     * @param seen      is the set of objects already visited
     * @return whether the object has already been visited
     */
    public static <T> Pair<Boolean, Set<T>> seen(T o, boolean recursive, Set<T> seen) {
        if (seen instanceof SeenSet) {
            Pair<Boolean, Set<T>> p = seen(o, recursive, seen);
            return p;
        }
        if (seen != null && seen.contains(o)) {
            // ++seenCt;
            return new Pair<Boolean, Set<T>>(true, seen);
        }
        // ++notSeenCt;
        if (seen == null && recursive == true) {
            seen = new HashSet<T>(); // ok to use hash here since we never
            // iterate
            // over the contents
        }
        if (seen != null) {
            seen.add(o);
        }
        return new Pair<Boolean, Set<T>>(false, seen);
    }

    /**
     * Replace the last occurrence of the substring in s with the replacement.
     *
     * @param s
     * @param replacement
     * @return the result of the replacement
     */
    public static String replaceLast(String s, String substring, String replacement) {
        int pos = s.lastIndexOf(substring);
        if (pos == -1) {
            return s;
        }
        return s.substring(0, pos) + replacement + s.substring(pos + substring.length());
    }

    public static String spewObjectPrefix = "* * * * *";
    public static String spewObjectSuffix = spewObjectPrefix;

    public static String spewObject(Object o, String indent) {
        return spewObject(o, indent, spewObjectPrefix, spewObjectSuffix);
    }

    // TODO -- bring over spewContents from EMFUtils, but apply it to Objects
    // instead of EObjects.
    // TODO -- write out fields and their values. don't forget about setting
    // things to be accessible, if desired.
    public static String spewObject(Object o, String indent, String prefix, String suffix) {
        StringBuffer sb = new StringBuffer();
        sb.append(indent + prefix + "\n");
        Class<?> c = o.getClass();
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            if (m.getReturnType() == void.class || m.getReturnType() == null
                    || m.getName().startsWith("wait") || m.getName().startsWith("notify")
                    || m.getName().startsWith("remove") || m.getName().startsWith("delete")) {
                continue;
            }
            if (m.getParameterTypes().length == 0) {
                sb.append(indent + m.getDeclaringClass() + ", " + m.toGenericString() + " --> "
                        + ClassUtils.runMethod(true, o, m).getValue() + "\n");
            }
        }
        sb.append(indent + suffix + "\n");
        return sb.toString();
    }

    /**
     * @param c
     * @return a c if c is a {@link List} or, otherwise, an ArrayList containing
     * the elements of c
     */
    public static <T> List<T> toList(Collection<T> c) {
        return asList(c);
    }

    /**
     * @param c
     * @return a c if c is a {@link List} or, otherwise, a new ArrayList
     * containing the elements of c
     */
    public static <T> List<T> asList(Collection<T> c) {
        if (c instanceof List) {
            return (List<T>) c;
        }
        List<T> list = new ArrayList<T>(c);
        return list;
    }

    /**
     * @param c
     * @param cls
     * @return a new {@link List} containing
     * the elements of c cast to type V
     */
    public static <V, T> List<V> asList(Collection<T> c, Class<V> cls) {
        List<V> list = new ArrayList<V>();
        for (T t : c) {
            if (t == null || cls == null || cls.isAssignableFrom(t.getClass())) {
                try {
                    V v = (cls == null ? (V) t : cls.cast(t));
                    list.add(v);
                } catch (ClassCastException e) {
                }
            }
        }
        return list;
    }

    /**
     * @param c
     * @return a c if c is a {@link Set} or, otherwise, a {@link LinkedHashSet}
     * containing the elements of c
     */
    public static <T> Set<T> toSet(Collection<T> c) {
        return asSet(c);
    }

    /**
     * @param c
     * @return a c if c is a {@link Set} or, otherwise, a {@link LinkedHashSet}
     * containing the elements of c
     */
    public static <T> Set<T> asSet(Collection<T> c) {
        if (c instanceof Set) {
            return (Set<T>) c;
        }
        LinkedHashSet<T> set = new LinkedHashSet<T>(c);
        return set;
    }

    /**
     * @param arrays
     * @return the sum of the lengths of the arrays, ignoring arrays that are
     * null (but counting null entries)
     */
    public static int totalSize(Object[]... arrays) {
        if (arrays == null) {
            return 0;
        }
        int size = 0;
        for (Object[] array : arrays) {
            size += (array == null ? 0 : array.length);
        }
        return size;
    }

    /**
     * @param arrays
     * @return the concatenation of the elements of the arrays into a new array,
     * ignoring arrays that are null (but including null entries in each
     * of the arrays)
     */
    public static Object[] join(Object[]... arrays) {
        int size = totalSize(arrays);
        Object[] result = new Object[size];
        int i = 0;
        for (Object[] array : arrays) {
            if (array != null) {
                for (int j = 0; j < array.length; ++j, ++i) {
                    result[i] = array[j];
                }
            }
        }
        return result;
    }

    public static <T1, T2> boolean toArrayOfType(T1[] source, T2[] target, Class<T2> newType) {
        boolean succ = true;
        for (int i = 0; i < source.length; ++i) {
            try {
                target[i] = newType.cast(source[i]);
            } catch (ClassCastException e) {
                succ = false;
                target[i] = null;
            }
        }
        return succ;
    }

    public static <T1, T2> boolean toArrayOfType(Collection<T1> source, T2[] target, Class<T2> newType) {
        return toArrayOfType(source.toArray(), target, newType);
    }

    public static <T> String join(Collection<T> things, String delim) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (T t : things) {
            if (first) {
                first = false;
            }
            else {
                sb.append(delim);
            }
            sb.append(t);
        }
        return sb.toString();
    }

    // /**
    // * @param o
    // * @param T
    // * @return the number of occurrences of o in the Collection c
    // */
    // public static < T > int occurrences( T value, Collection< T > c ) {
    // if ( c == null ) return 0;
    // int ct = 0;
    // // TODO -- shouldn't be calling event.Expression here--make
    // // Expression.evaluate() work for Wraps and include Wraps in src/util
    // Object v = Expression.evaluate( value, null, false );
    // for ( T o : c ) {
    // Object ov = Expression.evaluate( o, null, false );
    // if ( valuesEqual( ov, v ) ) ct++;
    // }
    // return ct;
    // }

    /**
     * A potentially more efficient addAll() for unordered Collections.
     *
     * @param coll1
     * @param coll2
     * @return the longer of the two collections after adding the shorter to the
     * longer.
     */
    @SuppressWarnings("unchecked")
    public static <T, C extends Collection<T>> C addAll(Collection<T> coll1, Collection<T> coll2) {
        if (coll1 == null) {
            return (C) coll2;
        }
        if (coll2 == null) {
            return (C) coll1;
        }

        Collection<T> cSmaller, cBigger;
        if (coll1.size() < coll2.size()) {
            cSmaller = coll1;
            cBigger = coll2;
        }
        else {
            cSmaller = coll2;
            cBigger = coll1;
        }
        try {
            cBigger.addAll(cSmaller);
            return (C) cBigger;
        } catch (UnsupportedOperationException e) {
        }
        try {
            cSmaller.addAll(cBigger);
            return (C) cSmaller;
        } catch (UnsupportedOperationException e) {
        }
        ArrayList<T> newList = new ArrayList<T>(cBigger);
        newList.addAll(cSmaller);
        return (C) newList;
    }

    public static <T1, T2> boolean valuesEqual(T1 v1, T2 v2) {
        return v1 == v2 || (v1 != null && v1.equals(v2));
    }

    public static String toStringNoHash(Object o) {
        if (o == null) {
            return "null";
        }
        // try {
        // if ( o.getClass().getMethod( "toString", (Class<?>[])null
        // ).getDeclaringClass() == o.getClass() ) {
        // return o.toString();
        // }
        // } catch ( SecurityException e ) {
        // e.printStackTrace();
        // } catch ( NoSuchMethodException e ) {
        // e.printStackTrace();
        // }
        return o.toString().replace(Integer.toHexString(o.hashCode()), "");
    }

    /**
     * @param s1
     * @param s2
     * @return the length of the longest common substring which is also a prefix
     * of one of the strings.
     */
    public static int longestPrefixSubstring(String subcontext, String subc) {
        int numMatch = 0;
        if (subcontext.contains(subc)) {
            if (numMatch < subc.length()) {
                // subcontextKey = subc;
                numMatch = subc.length();
                // numDontMatch = subcontext.length() - subc.length();
                // } else if ( numMatch == subc.length() ) {
                // if ( subcontext.length() - subc.length() < numDontMatch ) {
                // subcontextKey = subc;
                // numDontMatch = subcontext.length() - subc.length();
                // }
            }
        }
        else if (subc.contains(subcontext)) {
            if (numMatch < subcontext.length()) {
                // subcontextKey = subcontext;
                numMatch = subcontext.length();
                // numDontMatch = subc.length() - subcontext.length();
                // } else if ( numMatch == subcontext.length() ) {
                // if ( subc.length() - subcontext.length() < numDontMatch ) {
                // subcontextKey = subcontext;
                // numDontMatch = subc.length() - subcontext.length();
                // }
            }
        }
        return numMatch;
    }

    /**
     * This implementation appears {@code O(n^2)}. This is slower than a suffix
     * trie implementation, which is {@code O(n+m)}. The code below is copied
     * from wikipedia.
     *
     * @param s1
     * @param s2
     * @return the length of the longest common substring
     */
    public static int longestCommonSubstr(String s1, String s2) {
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0;
        }

        int m = s1.length();
        int n = s2.length();
        int cost = 0;
        int maxLen = 0;
        int[] p = new int[n];
        int[] d = new int[n];

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (s1.charAt(i) != s2.charAt(j)) {
                    cost = 0;
                }
                else {
                    if ((i == 0) || (j == 0)) {
                        cost = 1;
                    }
                    else {
                        cost = p[j - 1] + 1;
                    }
                }
                d[j] = cost;

                if (cost > maxLen) {
                    maxLen = cost;
                }
            } // for {}

            int[] swap = p;
            p = d;
            d = swap;
        }

        return maxLen;
    }

    /**
     * @param word
     * @return the word with the first character capitalized, if applicable
     */
    public static String capitalize(String word) {
        String capitalizedWord = word;
        if (Character.isLowerCase(word.charAt(0))) {
            capitalizedWord = "" + Character.toUpperCase(word.charAt(0)) + word.substring(1);
        }
        return capitalizedWord;
    }

    /**
     * Creates a new {@link ArrayList} and inserts the arguments, {@code ts}.
     *
     * @param ts
     * @return the new {@link ArrayList}
     */
    public static <T> ArrayList<T> newList(T... ts) {
        ArrayList<T> newList = new ArrayList<T>();
        newList.addAll(Arrays.asList(ts));
        return newList;
    }

    public static Integer parseInt(String intStr) {
        try {
            int i = Integer.parseInt(intStr);
            return i;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static boolean isInt(String intStr) {
        try {
            Integer.parseInt(intStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNumber(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    /**
     * Count the number of occurrences of the regular expression in the string.
     *
     * @param regex
     * @param string
     * @return the number of occurrences of regex in string
     */
    public static int count(String regex, String string) {
        int count = 0;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        // boolean found = false;
        while (matcher.find()) {
            // System.out.format( "I found the text" + " \"%s\" starting at "
            // + "index %d and ending at index %d.%n",
            // matcher.group(), matcher.start(), matcher.end() );
            // found = true;
            count++;
        }
        return count;
    }

    public static String getStackTrace(Throwable t) {
        if (t == null) {
            try {
                throw new Exception();
            } catch (Exception e) {
                t = e;
            }
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        sw.flush();
        return sw.toString();
    }

}
