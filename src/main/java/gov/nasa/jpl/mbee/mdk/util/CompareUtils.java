package gov.nasa.jpl.mbee.mdk.util;

// import gov.nasa.jpl.ae.solver.HasId;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class CompareUtils {

    public static class GenericComparator<T> implements Comparator<T> {

        @SuppressWarnings("rawtypes")
        protected static GenericComparator<?> instance = new CompareUtils.GenericComparator();

        @Override
        public int compare(T o1, T o2) {
            return CompareUtils.compare(o1, o2, true);// , true );
        }

        @SuppressWarnings("unchecked")
        public static <TT> GenericComparator<TT> instance() {
            // TODO Auto-generated method stub
            return (GenericComparator<TT>) instance;
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T1, T2> int compare(T1 o1, T2 o2, boolean checkComparable) {
        // return compare( o1, o2, checkComparable, false );
        // }
        // @SuppressWarnings( "unchecked" )
        // public static <T1, T2> int compare( T1 o1, T2 o2, boolean
        // checkComparable,
        // boolean useId ) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        // if ( useId && o1 instanceof HasId && o2 instanceof HasId ) {
        // return CompareUtils.compare( ( (HasId)o1 ).getElementID(), ( (HasId)o2
        // ).getElementID() );
        // }
        int compare = o1.getClass().getName().compareTo(o2.getClass().getName());
        if (compare != 0) {
            return compare;
        }
        if (checkComparable) {
            if (o1 instanceof Comparable) {
                return ((Comparable<T2>) o1).compareTo(o2);
            }
        }
        if (o1 instanceof Collection && o2 instanceof Collection) {
            return CompareUtils.compareCollections((Collection) o1, (Collection) o2, checkComparable);// ,
            // useId
            // );
        }
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return CompareUtils.compareCollections((Object[]) o1, (Object[]) o2, checkComparable);// ,
            // useId
            // );
        }
        if (o1 instanceof Map && o2 instanceof Map) {
            return CompareUtils.compareCollections((Map) o1, (Map) o2, checkComparable);// ,
            // useId
            // );
        }
        compare = CompareUtils.compareToStringNoHash(o1, o2);
        if (compare != 0) {
            return compare;
        }
        return compare;
    }

    public static int compare(Object o1, Object o2) {
        return compare(o1, o2, false);// , false ); // default false to avoid
        // infinite recursion
    }

    public static <T> int compareCollections(Collection<T> coll1, Collection<T> coll2, boolean checkComparable) {// ,
        // boolean useId ) {
        if (coll1 == coll2) {
            return 0;
        }
        if (coll1 == null) {
            return -1;
        }
        if (coll2 == null) {
            return 1;
        }
        Iterator<T> i1 = coll1.iterator();
        Iterator<T> i2 = coll2.iterator();
        int compare = 0;
        while (i1.hasNext() && i2.hasNext()) {
            T t1 = i1.next();
            T t2 = i2.next();
            compare = compare(t1, t2, checkComparable);// , useId );
            if (compare != 0) {
                return compare;
            }
        }
        if (i1.hasNext()) {
            return 1;
        }
        if (i2.hasNext()) {
            return -1;
        }
        return 0;
    }

    public static <T> int compareCollections(T[] arr1, T[] arr2, boolean checkComparable) {// ,
        // boolean useId) {
        if (arr1 == arr2) {
            return 0;
        }
        if (arr1 == null) {
            return -1;
        }
        if (arr2 == null) {
            return 1;
        }
        int i = 0;
        int compare = 0;
        for (i = 0; i < Math.min(arr1.length, arr2.length); ++i) {
            T t1 = arr1[i];
            T t2 = arr2[i];
            compare = compare(t1, t2, checkComparable);// , useId );
            if (compare != 0) {
                return compare;
            }
        }
        if (i < arr1.length) {
            return 1;
        }
        if (i < arr2.length) {
            return -1;
        }
        return 0;
    }

    public static <K, V> int compareCollections(Map<K, V> m1, Map<K, V> m2, boolean checkComparable) {// ,
        // boolean useId ) {
        if (m1 == m2) {
            return 0;
        }
        if (m1 == null) {
            return -1;
        }
        if (m2 == null) {
            return 1;
        }
        return compareCollections(m1.entrySet(), m2.entrySet(), checkComparable);// ,
        // useId
        // );
    }

    public static int compareToStringNoHash(Object o1, Object o2) {
        assert o1 != null;
        assert o2 != null;
        int pos = 0;
        String s1 = o1.toString();
        String s2 = o2.toString();
        boolean gotAmp = false;
        for (pos = 0; pos < Math.min(s1.length(), s2.length()); ++pos) {
            char c1 = s1.charAt(pos);
            char c2 = s2.charAt(pos);
            if (gotAmp) {
                if (Character.isDigit(c1) || Character.isDigit(c2)) {
                    System.err.println("Warning! Assumed comparing hash codes!: o1=" + o1 + ", o2=" + o2 + "\n" + MoreToString.Helper.toString(Thread.currentThread().getStackTrace()).replaceAll(", ", "\n  ") + "\n");
                    return 0;
                }
                else {
                    gotAmp = false;
                }
            }
            if (c1 < c2) {
                return -1;
            }
            if (c1 > c2) {
                return 1;
            }
            if (c1 == '@') {
                gotAmp = true;
            }
        }
        if (pos < s1.length()) {
            return 1;
        }
        if (pos < s2.length()) {
            return -1;
        }
        return 0;
        // int compare = Utils.toStringNoHash(o1).compareTo(
        // Utils.toStringNoHash(o2) );
        // return compare;
    }

    public static int compare(int i1, int i2) {
        if (i1 < i2) {
            return -1;
        }
        if (i1 > i2) {
            return 1;
        }
        return 0;
    }

    public static int compare(String s1, String s2) {
        if (s1 == s2) {
            return 0;
        }
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        return s1.compareTo(s2);
    }

}
