package org.openmbee.mdk.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of miscellaneous utility functions.
 */
@Deprecated
public class Utils2 {
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

}
