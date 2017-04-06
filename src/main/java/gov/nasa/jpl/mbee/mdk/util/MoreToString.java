package gov.nasa.jpl.mbee.mdk.util;

import gov.nasa.jpl.mbee.mdk.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MoreToString adds options to toString() for recursively nested objects.
 */
public interface MoreToString {

    // Constants to be used as options for formatting collections and arrays.
    int NO_FORMAT = -1;
    int SQUARE_BRACES = 0;
    int CURLY_BRACES = 1;
    int PARENTHESES = 2;
    int COMMA = 3;
    int PERIOD = 4;
    int EQUALS = 5;

    String[] prefixes = new String[]{"[", "{", "(", "(", "(", "("};
    String[] suffixes = new String[]{"]", "}", ")", ")", ")", ")"};
    String[] delimiters = new String[]{",", ",", ",", ",", ".", "="};
    Map<String, String[]> formatOptions = Helper.initFormatOptions();

    /**
     * Write object recursively based on passed options.
     *
     * @param withHash whether to include "@" + hasCode() in the returned String.
     * @param deep     whether to include member/child detail and call their
     *                 MoreToString.toString() (typically) with the same options.
     * @param seen     whether the object has already been written with deep=true, in
     *                 which case it will set deep=false to end the recursion.
     * @return the string representation of the object.
     */
    String toString(boolean withHash, boolean deep, Set<Object> seen);

    /**
     * Write object recursively based on passed options.
     *
     * @param withHash     whether to include "@" + hasCode() in the returned String.
     * @param deep         whether to include member/child detail and call their
     *                     MoreToString.toString() (typically) with the same options.
     * @param seen         whether the object has already been written with deep=true, in
     *                     which case it will set deep=false to end the recursion.
     * @param otherOptions other class or context-specific options with names and values.
     * @return the string representation of the object.
     */
    String toString(boolean withHash, boolean deep, Set<Object> seen, Map<String, Object> otherOptions);

    /**
     * @return a single name or value
     */
    String toShortString();

    /**
     * Helper class for MoreToString toString() calls. There are also functions
     * for formatting collections.
     */
    class Helper {
        /**
         * Helper function for MoreToString.toString() when it is not known
         * whether the input object implements MoreToString.
         *
         * @return ((MoreToString)object).toString(...) with the same options
         * passed if the object does implement MoreToString; otherwise
         * return object.toString().
         */
        public static String toString(Object object, boolean withHash, boolean deep, Set<Object> seen,
                                      boolean checkIfMoreToString, Map<String, Object> otherOptions) {
            if (object == null) {
                return "null";
            }

            // We have to make sure we get to the right method since collections
            // require an extra boolean argument.
            if (object.getClass().isArray()) {
                return toString(Arrays.asList(object), withHash, deep, seen, otherOptions);
            }
            if (object instanceof Collection) {
                return toString((Collection<?>) object, withHash, deep, seen, otherOptions, true);
            }
            if (object instanceof Map) {
                return toString((Map<?, ?>) object, withHash, deep, seen, otherOptions, true);
            }
            if (object instanceof Pair) {
                return toString((Pair<?, ?>) object, withHash, deep, seen, otherOptions, true);
            }
            if (object instanceof Map.Entry) {
                return toString((Map.Entry<?, ?>) object, withHash, deep, seen, otherOptions, true);
            }

            if (checkIfMoreToString && object instanceof MoreToString) {
                return ((MoreToString) object).toString(withHash, deep, seen, otherOptions);
            }
            return object.toString();
        }

        /**
         * Helper function for MoreToString.toString() when it is not known
         * whether the input object implements MoreToString.
         *
         * @return ((MoreToString)object).toString(...) with the same options
         * passed if the object does implement MoreToString; otherwise
         * return object.toString().
         */
        public static String toString(Object object, boolean withHash, boolean deep, Set<Object> seen,
                                      Map<String, Object> otherOptions) {
            return toString(object, withHash, deep, seen, true, otherOptions);
        }

        /**
         * Helper function for MoreToString.toString() when it is not known
         * whether the input object implements MoreToString.
         *
         * @return ((MoreToString)object).toString(...) with the same options
         * passed if the object does implement MoreToString; otherwise
         * return object.toString().
         */
        public static String toString(Object object, boolean withHash, boolean deep, Set<Object> seen) {
            return toString(object, withHash, deep, seen, null);
        }

        /**
         * Helper function for MoreToString.toString() when it is not known
         * whether the input object implements MoreToString and default
         * parameter values are fine.
         *
         * @param object
         * @return
         */
        public static String toString(Object object) {
            // Below works for array or other non-collection object.
            return toString(object, false, false, null, null);
        }

        /**
         * Helper function for MoreToString.toString() when it is not known
         * whether the input object implements MoreToString and default
         * parameter values are fine.
         *
         * @param object
         * @return
         */
        public static String toLongString(Object object) {
            // Below works for array or other non-collection object.
            return toString(object, true, true, null);
        }

        public static String toShortString(Object object) {
            if (object == null) {
                return "null";
            }

            // We have to make sure we get to the right method since collections
            // require an extra boolean argument.
            if (object instanceof Collection) {
                return toShortString((Collection<?>) object, null, true);
            }
            if (object instanceof Map) {
                return toShortString((Map<?, ?>) object, null, true);
            }
            if (object instanceof Pair) {
                return toShortString((Pair<?, ?>) object, null, true);
            }
            if (object instanceof Map.Entry) {
                return toShortString((Map.Entry<?, ?>) object, null, true);
            }

            if (object instanceof MoreToString) {
                return ((MoreToString) object).toShortString();
            }
            return object.toString();
        }

        public static <T> String toShortString(Collection<T> collection, Map<String, Object> otherOptions,
                                               boolean checkIfMoreToString) {
            int formatKey = PARENTHESES;
            if (hasFormatOptions(otherOptions)) {
                formatKey = NO_FORMAT;
            }
            return toShortString(collection, formatKey, otherOptions, checkIfMoreToString);
        }

        public static <T> String toShortString(final Collection<T> collection, int formatKey,
                                               Map<String, Object> otherOptions, boolean checkIfMoreToString) {
            if (checkIfMoreToString && collection instanceof MoreToString) {
                return ((MoreToString) collection).toShortString();
            }
            return toString(collection.toArray(), false, false, null, otherOptions, formatKey);
        }

        public static <K, V> String toShortString(Map<K, V> map, Map<String, Object> otherOptions,
                                                  int formatKey, boolean checkIfMoreToString) {
            if (checkIfMoreToString && map instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, formatKey);
                return ((MoreToString) map).toShortString();
            }
            return toString(map.entrySet().toArray(), false, false, null, otherOptions, formatKey);
        }

        public static <K, V> String toShortString(Map<K, V> map, Map<String, Object> otherOptions,
                                                  boolean checkIfMoreToString) {
            int formatKey = CURLY_BRACES;
            if (hasFormatOptions(otherOptions)) {
                formatKey = NO_FORMAT;
            }
            return toString(map, false, false, null, otherOptions, formatKey, checkIfMoreToString);
        }

        public static <K, V> String toShortString(Map.Entry<K, V> entry, Map<String, Object> otherOptions,
                                                  int formatKey, boolean checkIfMoreToString) {
            if (checkIfMoreToString && entry instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, formatKey);
                return ((MoreToString) entry).toShortString();
            }
            Pair<K, V> p = new Pair<K, V>(entry.getKey(), entry.getValue());
            return toShortString(p, otherOptions, formatKey, true);
        }

        public static <K, V> String toShortString(Map.Entry<K, V> entry, Map<String, Object> otherOptions,
                                                  boolean checkIfMoreToString) {
            int formatKey = EQUALS;
            if (hasFormatOptions(otherOptions)) {
                formatKey = NO_FORMAT;
            }// else {
            return toString(entry, false, false, null, otherOptions, formatKey, checkIfMoreToString);
            // }
        }

        public static <K, V> String toShortString(Pair<K, V> pair, Map<String, Object> otherOptions,
                                                  int formatKey, boolean checkIfMoreToString) {
            if (checkIfMoreToString && pair instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, formatKey);
                return ((MoreToString) pair).toShortString();
            }
            return toString(new Object[]{pair.getKey(), pair.getValue()}, false, false, null, otherOptions,
                    formatKey);
        }

        public static <K, V> String toShortString(Pair<K, V> pair, Map<String, Object> otherOptions,
                                                  boolean checkIfMoreToString) {
            int formatKey = hasFormatOptions(otherOptions) ? NO_FORMAT : PARENTHESES;
            return toShortString(pair, otherOptions, formatKey, checkIfMoreToString);
        }

        public static <T> String toShortString(final T[] array, Map<String, Object> otherOptions) {
            return toString(array, false, false, null, otherOptions);
        }

        /**
         * Writes an array to a string with MoreToString options, including an
         * explicit format key. For example, if the format key
         * MoreToString.PARENTHESES is given, the collection will be enclosed in
         * parentheses, "(" and ")" and the default comma will be used as a
         * delimiter between elements.
         *
         * @param withHash     whether to include "@" + hasCode() in the returned String.
         * @param deep         whether to include member/child detail and call their
         *                     MoreToString.toString() (typically) with the same options.
         * @param seen         whether the object has already been written with
         *                     deep=true, in which case it will set deep=false to end the
         *                     recursion.
         * @param otherOptions other class or context-specific options with names and
         *                     values.
         * @param formatKey    a MoreToString constant indicating the grouping format of
         *                     the elements of the collection, for example,
         *                     MoreToString.CURLY_BRACES.
         * @return the string representation of the object.
         */
        public static <T> String toString(final Collection<T> collection, boolean withHash, boolean deep,
                                          Set<Object> seen, Map<String, Object> otherOptions, int formatKey, boolean checkIfMoreToString) {
            if (checkIfMoreToString && collection instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, formatKey);
                return ((MoreToString) collection).toString(withHash, deep, seen, otherOptions);
            }
            return toString(collection.toArray(), withHash, deep, seen, otherOptions, formatKey);
        }

        public static <T> String toString(Collection<T> collection, boolean withHash, boolean deep,
                                          Set<Object> seen, Map<String, Object> otherOptions, boolean square,
                                          boolean checkIfMoreToString) {
            int formatKey = (square ? SQUARE_BRACES : PARENTHESES);
            return toString(collection, withHash, deep, seen, otherOptions, formatKey, checkIfMoreToString);
        }

        public static <T> String toString(Collection<T> collection, boolean withHash, boolean deep,
                                          Set<Object> seen, Map<String, Object> otherOptions, boolean checkIfMoreToString) {
            int formatKey = PARENTHESES;
            if (hasFormatOptions(otherOptions)) {
                formatKey = NO_FORMAT;
            }
            return toString(collection, withHash, deep, seen, otherOptions, formatKey, checkIfMoreToString);
        }

        public static <T> String toString(Collection<T> collection, boolean withHash, boolean deep,
                                          Set<Object> seen, Map<String, Object> otherOptions, String prefix, String delimiter,
                                          String suffix, boolean checkIfMoreToString) {
            if (checkIfMoreToString && collection instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, prefix, delimiter, suffix);
                return ((MoreToString) collection).toString(withHash, deep, seen, otherOptions);
            }
            return toString(collection.toArray(), withHash, deep, seen, otherOptions, prefix, delimiter,
                    suffix);
        }

        public static <K, V> String toString(Map<K, V> map, boolean withHash, boolean deep, Set<Object> seen,
                                             Map<String, Object> otherOptions, int formatKey, boolean checkIfMoreToString) {
            if (checkIfMoreToString && map instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, formatKey);
                return ((MoreToString) map).toString(withHash, deep, seen, otherOptions);
            }
            return toString(map.entrySet().toArray(), withHash, deep, seen, otherOptions, formatKey);
        }

        public static <K, V> String toString(Map<K, V> map, boolean withHash, boolean deep, Set<Object> seen,
                                             Map<String, Object> otherOptions, boolean checkIfMoreToString) {
            int formatKey = CURLY_BRACES;
            if (hasFormatOptions(otherOptions)) {
                formatKey = NO_FORMAT;
                // if ( checkIfMoreToString && map instanceof MoreToString ) {
                // return ((MoreToString)map).toString(withHash, deep, seen,
                // otherOptions);
                // }
                // return toString( map.entrySet().toArray(), withHash, deep,
                // seen,
                // otherOptions );
            }
            return toString(map, withHash, deep, seen, otherOptions, formatKey, checkIfMoreToString);
        }

        public static <K, V> String toString(Map<K, V> map, boolean withHash, boolean deep, Set<Object> seen,
                                             Map<String, Object> otherOptions, String prefix, String delimiter, String suffix,
                                             boolean checkIfMoreToString) {
            if (checkIfMoreToString && map instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, prefix, delimiter, suffix);
                return ((MoreToString) map).toString(withHash, deep, seen, otherOptions);
            }
            return toString(map.entrySet().toArray(), withHash, deep, seen, otherOptions, prefix, delimiter,
                    suffix);
        }

        public static <K, V> String toString(Map.Entry<K, V> entry, boolean withHash, boolean deep,
                                             Set<Object> seen, Map<String, Object> otherOptions, int formatKey, boolean checkIfMoreToString) {
            if (checkIfMoreToString && entry instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, formatKey);
                return ((MoreToString) entry).toString(withHash, deep, seen, otherOptions);
            }
            Pair<K, V> p = new Pair<K, V>(entry.getKey(), entry.getValue());
            return toString(p, withHash, deep, seen, otherOptions, formatKey, false);
        }

        public static <K, V> String toString(Map.Entry<K, V> entry, boolean withHash, boolean deep,
                                             Set<Object> seen, Map<String, Object> otherOptions, boolean checkIfMoreToString) {
            int formatKey = EQUALS;
            if (hasFormatOptions(otherOptions)) {
                formatKey = NO_FORMAT;
                // if ( checkIfMoreToString && entry instanceof MoreToString ) {
                // return ((MoreToString)entry).toString(withHash, deep, seen,
                // otherOptions);
                // }
                // Pair< K, V > p = new Pair< K, V >( entry.getKey(),
                // entry.getValue() );
                // return toString( p, withHash, deep, seen, otherOptions, false
                // );
            }// else {
            return toString(entry, withHash, deep, seen, otherOptions, formatKey, checkIfMoreToString);
            // }
        }

        public static <K, V> String toString(Map.Entry<K, V> entry, boolean withHash, boolean deep,
                                             Set<Object> seen, Map<String, Object> otherOptions, String prefix, String delimiter,
                                             String suffix, boolean checkIfMoreToString) {
            if (checkIfMoreToString && entry instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, prefix, delimiter, suffix);
                return ((MoreToString) entry).toString(withHash, deep, seen, otherOptions);
            }
            Pair<K, V> p = new Pair<K, V>(entry.getKey(), entry.getValue());
            return toString(p, withHash, deep, seen, otherOptions, prefix, delimiter, suffix, false);
        }

        public static <K, V> String toString(Pair<K, V> pair, boolean withHash, boolean deep,
                                             Set<Object> seen, Map<String, Object> otherOptions, int formatKey, boolean checkIfMoreToString) {
            if (checkIfMoreToString && pair instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, formatKey);
                return ((MoreToString) pair).toString(withHash, deep, seen, otherOptions);
            }
            return toString(new Object[]{pair.getKey(), pair.getValue()}, withHash, deep, seen, otherOptions,
                    formatKey);
        }

        public static <K, V> String toString(Pair<K, V> pair, boolean withHash, boolean deep,
                                             Set<Object> seen, Map<String, Object> otherOptions, boolean checkIfMoreToString) {
            int formatKey = hasFormatOptions(otherOptions) ? NO_FORMAT : PARENTHESES;
            // if ( checkIfMoreToString && pair instanceof MoreToString ) {
            // return ((MoreToString)pair).toString(withHash, deep, seen,
            // otherOptions);
            // }
            return toString(pair, withHash, deep, seen, otherOptions, formatKey, checkIfMoreToString);
        }

        public static <K, V> String toString(Pair<K, V> pair, boolean withHash, boolean deep,
                                             Set<Object> seen, Map<String, Object> otherOptions, String prefix, String delimiter,
                                             String suffix, boolean checkIfMoreToString) {
            if (checkIfMoreToString && pair instanceof MoreToString) {
                stuffOptionsFromKey(otherOptions, prefix, delimiter, suffix);
                return ((MoreToString) pair).toString(withHash, deep, seen, otherOptions);
            }
            return toString(new Object[]{pair.getKey(), pair.getValue()}, withHash, deep, seen, otherOptions,
                    prefix, delimiter, suffix);
        }

        // public static < T > String toString( final T[] collection,
        // boolean withHash, boolean deep,
        // Set< Object > seen,
        // Map<String,Object> otherOptions ) {
        // return toString( collection, withHash, deep, seen, otherOptions, true
        // );
        // }

        /**
         * @param map
         * @param key
         * @param cls
         * @return a value in map of type T with the given key or null if not
         * found.
         */
        @SuppressWarnings("unchecked")
        public static <K, V, T> T get(Map<K, V> map, K key, Class<T> cls) {
            if (key == null) {
                return null;
            }
            if (map == null) {
                return null;
            }
            V v = map.get(key);
            if (v == null) {
                return null;
            }
            T t = null;
            // try {
            if (cls.isAssignableFrom(v.getClass())) {
                t = (T) v;
            }
            // } catch ( ClassCastException cce ) {
            // ;// ignore
            // }
            return t;
        }

        /**
         * @param map
         * @param key
         * @return a value in map of type T with the given key or null if not
         * found.
         */
        public static <K, V, T> T get(Map<K, V> map, K key) {
            return get(map, key, null);
        }

        private static Map<String, String[]> initFormatOptions() {
            Map<String, String[]> optionMap = new TreeMap<String, String[]>();
            optionMap.put("prefix", prefixes);
            optionMap.put("suffix", suffixes);
            optionMap.put("delimiter", delimiters);
            return optionMap;
        }

        private static String[] getOptions(Map<String, Object> options, String[] optionNames) {
            String[] optionValues = new String[optionNames.length];
            if (!Utils2.isNullOrEmpty(options) && !Utils2.isNullOrEmpty(optionNames)) {
                // initialize option values to null
                for (int i = 0; i < optionNames.length; ++i) {
                    optionValues[i] = null;
                }
                // get matching values for the name
                for (int i = 0; i < optionNames.length; ++i) {
                    // look for a matching string value first
                    String value = get(options, optionNames[i], String.class);
                    if (value != null) {
                        optionValues[i] = value;
                    }
                    else {
                        // look for a format key integer
                        Integer optionKey = get(options, optionNames[i], Integer.class);
                        if (optionKey != null) {
                            // look up the string value for the format key
                            String[] subOptions = formatOptions.get(optionNames[i]);
                            if (subOptions != null) {
                                if (optionKey > 0 && optionKey < subOptions.length) {
                                    value = subOptions[optionKey];
                                    optionValues[i] = value;
                                }
                            }
                            // The format key often applies to all options, so
                            // set any that
                            // are null to the default string value for the name
                            // and key.
                            for (int j = 0; j < optionNames.length; ++j) {
                                // Don't overwrite an assigned value with a
                                // default.
                                if (optionValues[j] != null) {
                                    continue;
                                }
                                // look up the string value for the format key
                                subOptions = formatOptions.get(optionNames[j]);
                                if (subOptions != null) {
                                    if (optionKey > 0 && optionKey < subOptions.length) {
                                        value = subOptions[optionKey];
                                        optionValues[j] = value;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return optionValues;
        }

        private static String[] getFormatOptions(Map<String, Object> options) {
            // final Object[] objects = formatOptions.keySet().toArray();
            String[] optionNames = new String[formatOptions.size()];
            Utils2.toArrayOfType(formatOptions.keySet(), optionNames, String.class);
            // new String[] { "prefix", "delimiter", "suffix" };
            return getOptions(options, optionNames);
        }

        public static boolean hasFormatOptions(Map<String, Object> options) {
            return (options != null && options.get("prefix") != null);
        }

        /**
         * Writes an array to a string with MoreToString options, including an
         * explicit format key. For example, if the format key
         * MoreToString.PARENTHESES is given, the collection will be enclosed in
         * parentheses, "(" and ")" and the default comma will be used as a
         * delimiter between elements.
         *
         * @param withHash     whether to include "@" + hasCode() in the returned String.
         * @param deep         whether to include member/child detail and call their
         *                     MoreToString.toString() (typically) with the same options.
         * @param seen         whether the object has already been written with
         *                     deep=true, in which case it will set deep=false to end the
         *                     recursion.
         * @param otherOptions other class or context-specific options with names and
         *                     values.
         * @param formatKey    a MoreToString constant indicating the grouping format of
         *                     the elements of the collection, for example,
         *                     MoreToString.CURLY_BRACES.
         * @return the string representation of the object.
         */
        public static <T> String toString(final T[] array, boolean withHash, boolean deep, Set<Object> seen,
                                          Map<String, Object> otherOptions, int formatKey) {
            if (otherOptions == null) {
                otherOptions = new TreeMap<String, Object>();
            }
            stuffOptionsFromKey(otherOptions, formatKey);
            return toString(array, withHash, deep, seen, otherOptions);
        }

        public static void stuffOptionsFromKey(Map<String, Object> options, int formatKey) {
            if (formatKey == NO_FORMAT) {
                return;
            }
            if (options == null) {
                return;
            }
            for (String opt : formatOptions.keySet()) {
                options.put("prefix", formatKey);
            }
        }

        private static void stuffOptionsFromKey(Map<String, Object> options, String prefix, String delimiter,
                                                String suffix) {
            if (options == null) {
                options = new TreeMap<String, Object>();
            }
            options.put("prefix", prefix);
            options.put("delimiter", delimiter);
            options.put("suffix", suffix);
        }

        public static void removeFormatOptions(Map<String, Object> options) {
            if (Utils2.isNullOrEmpty(options)) {
                return;
            }
            for (String opt : formatOptions.keySet()) {
                options.remove(opt);
            }
        }

        // public static < T > String toShortString( final T[] array, int
        // formatKey,
        // boolean checkIfMoreToString ) {
        // String[] options = getFormatOptions(otherOptions);
        // String delimiter = options[0]; // ordered by option name
        // String prefix = options[1];
        // String suffix = options[2];
        // return null;
        // }

        public static String toString(final int[] array) {
            if (array == null) {
                return "null";
            }
            Integer[] arrayI = new Integer[array.length];
            for (int i = 0; i < array.length; ++i) {
                arrayI[i] = array[i];
            }
            return toString(arrayI);
        }

        public static String toString(final double[] array) {
            if (array == null) {
                return "null";
            }
            Double[] arrayI = new Double[array.length];
            for (int i = 0; i < array.length; ++i) {
                arrayI[i] = array[i];
            }
            return toString(arrayI);
        }

        public static String toString(final boolean[] array) {
            if (array == null) {
                return "null";
            }
            Boolean[] arrayI = new Boolean[array.length];
            for (int i = 0; i < array.length; ++i) {
                arrayI[i] = array[i];
            }
            return toString(arrayI);
        }

        // TODO -- need methods like above for other primitives

        public static <T> String toString(final T[] array) {
            return toString(array, false, false, null, null);
        }

        public static <T> String toString(final T[] array, boolean withHash, boolean deep, Set<Object> seen,
                                          Map<String, Object> otherOptions) {
            String[] options = getFormatOptions(otherOptions);
            String delimiter = options[0]; // ordered by option name
            String prefix = options[1];
            String suffix = options[2];
            if (prefix != null && !otherOptions.containsKey("formatDeep")) {
                removeFormatOptions(otherOptions);
            }
            return toString(array, withHash, deep, seen, otherOptions, prefix, delimiter, suffix);
        }

        public static <T> String toString(final T[] array, boolean withHash, boolean deep, Set<Object> seen,
                                          Map<String, Object> otherOptions, String prefix, String delimiter, String suffix) {
            StringBuffer sb = new StringBuffer();
            if (prefix == null) {
                prefix = "[";
                suffix = "]";
            }
            if (prefix.equals("{") && !suffix.equals("}")) {
                suffix = "}";
            }
            if (delimiter == null) {
                delimiter = ", ";
            }
            sb.append(prefix);
            boolean first = true;
            for (Object object : array) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append(delimiter);
                }
                if (deep && (seen == null || !seen.contains(object))) {
                    sb.append(MoreToString.Helper.toString(object, withHash, deep, seen, otherOptions));
                }
                else {
                    sb.append(MoreToString.Helper.toShortString(object));
                }
            }
            sb.append(suffix);
            return sb.toString();
        }

        public static List<String> fromString(String s, String prefix, String delimiter, String suffix) {
            List<String> list = Utils2.getEmptyList();
            Pattern p = Pattern.compile(prefix);
            Matcher matcher = p.matcher(s);
            if (!matcher.find()) {
                return list;
            }
            p = Pattern.compile(delimiter);
            String[] arr = p.split(s.substring(matcher.end()));
            String last = arr[arr.length - 1];
            p = Pattern.compile(suffix);
            matcher = p.matcher(last);
            int startPos = -1;
            while (matcher.find()) {
                startPos = matcher.start();
            }
            int length = arr.length;
            if (startPos >= 0) {
                if (startPos == 0) {
                    --length;
                }
                else {
                    arr[length - 1] = last.substring(0, startPos);
                }
            }
            list = Arrays.asList(arr).subList(0, length);
            return list;
        }

        public static void fromString(Map<String, String> map, String s) {
            fromString(map, s, "[\\[{(]\\s*", ",\\s*", "\\s*[\\]})]", "[\\[{(]\\s*", "\\s*=\\s*",
                    "\\s*[\\]})]");
        }

        public static void fromString(Map<String, String> map, String s, String prefix, String delimiter,
                                      String suffix, String keyValuePrefix, String keyValueDelimiter, String keyValueSuffix) {

            if (map == null) {
                map = new HashMap<String, String>();
            }
            map.clear();

            Pattern p = Pattern.compile(prefix);
            Matcher matcher = p.matcher(s);
            if (!matcher.find()) {
                return;
            }
            int start = matcher.end();

            Pattern d = Pattern.compile(delimiter);
            Pattern kvp = Pattern.compile(keyValuePrefix);
            Pattern kvd = Pattern.compile(keyValueDelimiter);
            Pattern kvs = Pattern.compile(keyValueSuffix);
            boolean gotDelimiter = true;
            while (gotDelimiter) {
                Debug.outln("\nstart = " + start);
                Debug.outln("substring = " + s.substring(start));
                // find key-value prefix
                matcher = kvp.matcher(s.substring(start));
                if (!matcher.find()) {
                    break;
                }
                start = start + matcher.end();

                Debug.outln("\nkvp match = " + matcher.group());
                Debug.outln("matcher.start() = " + matcher.start());
                Debug.outln("matcher.end() = " + matcher.end());
                Debug.outln("\nstart = " + start);
                Debug.outln("substring = " + s.substring(start));

                // find delimiter between key and value
                matcher = kvd.matcher(s.substring(start));
                if (!matcher.find()) {
                    break;
                }
                // get the key as the characters before the key-value delimiter
                String key = s.substring(start, start + matcher.start());
                start = start + matcher.end();

                Debug.outln("\nkvd match = " + matcher.group());
                Debug.outln("matcher.start() = " + matcher.start());
                Debug.outln("matcher.end() = " + matcher.end());
                Debug.outln("\nkey = " + key);
                Debug.outln("\nstart = " + start);
                Debug.outln("substring = " + s.substring(start));

                // get the value between the key-value delimiter and the
                // key-value suffix
                matcher = kvs.matcher(s.substring(start));
                if (!matcher.find()) {
                    break;
                }
                String value = s.substring(start, start + matcher.start());
                start = start + matcher.end();
                boolean foundValue = matcher.start() != 0;

                Debug.outln("\nkvs match = " + matcher.group());
                Debug.outln("matcher.start() = " + matcher.start());
                Debug.outln("matcher.end() = " + matcher.end());
                if (foundValue) {
                    Debug.outln("\nvalue = " + value);
                }
                Debug.outln("\nstart = " + start);
                Debug.outln("substring = " + s.substring(start));

                if (foundValue) {
                    // add the key-value pair to the map
                    map.put(key, value);
                }

                // skip over the delimiter
                matcher = d.matcher(s.substring(start));
                // set the start position at the end of the delimiter to get the
                // next pair
                gotDelimiter = matcher.find();

                if (!foundValue) {
                    if (gotDelimiter) {
                        foundValue = matcher.start() != 0;
                        if (!foundValue) {
                            System.err.println("Error! fromString(): no value parsed from "
                                    + s.substring(start));
                        }
                        value = s.substring(start, start + matcher.start());
                    }
                    else {
                        value = s.substring(start);
                    }
                    Debug.outln("\nvalue = " + value);
                    map.put(key, value);
                }

                if (gotDelimiter) {
                    start = start + matcher.end();

                    Debug.outln("\nd match = " + matcher.group());
                    Debug.outln("matcher.start() = " + matcher.start());
                    Debug.outln("matcher.end() = " + matcher.end());
                    Debug.outln("\nstart = " + start);
                    Debug.outln("substring = " + s.substring(start));
                }

            }
            Debug.outln("parsed map = " + map);
        }

        private static String readLine(String format, Object... args) {
            if (System.console() != null) {
                return System.console().readLine(format, args);
            }
            System.out.print(String.format(format, args));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                return reader.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        private static void print(String format, Object... args) {
            if (System.console() != null) {
                System.console().format(format, args);
            }
            else {
                System.out.format(format, args);
            }
        }

        private static void println(String format, Object... args) {
            print(format + "%n", args);
        }

        public static class TestMoreToString {

            /**
             * testing fromString() for a Map< String, String > with numerical
             * data
             */
            @Test
            public void testFromString() {
                // create a map of numbers
                Map<Integer, Double> mapTruth = new TreeMap<Integer, Double>();
                mapTruth.put(0, 2.31359797761508);
                mapTruth.put(84600, 2.080913063156552);
                mapTruth.put(85500, 2.3810581138436953);

                // Capture the mapTruth toString() for parsing with fromString()
                // later.
                // TODO -- this doesn't match parser. fromString expects
                // parentheses
                // around the key=value pairs, but toString() leaves them out.
                // TODO -- fromString() also can't tolerate spaces, which
                // toString() inserts.
                // String mapString = mapTruth.toString();
                // String mapString =
                // "{(0=2.31359797761508),(84600=2.080913063156552),(85500=2.3810581138436953)}";
                String mapString = mapTruth.toString().replaceAll("([.\\w-]*)=([.\\w-]*)", "($1=$2)")
                        .replaceAll(" ", "");

                // convert the map into a Map<String, String> by calling
                // toString() on
                // each key and value
                Map<String, String> mapStringTruth = new TreeMap<String, String>();
                for (Entry<Integer, Double> e : mapTruth.entrySet()) {
                    mapStringTruth.put(e.getKey().toString(), e.getValue().toString());
                }

                // parse mapString as a String map
                Map<String, String> mapTest = new TreeMap<String, String>();
                fromString(mapTest, mapString);
                System.out.println("string to parse into map: " + mapString);
                System.out.println("map after parsing:\n");
                for (Map.Entry<String, String> e : mapTest.entrySet()) {
                    System.out.println("key = " + e.getKey());
                    System.out.println("value = " + e.getValue());
                }

                Assert.assertArrayEquals(mapTest.keySet().toArray(), mapStringTruth.keySet().toArray());
                Assert.assertArrayEquals(mapTest.values().toArray(), mapStringTruth.values().toArray());
                Assert.assertEquals(mapTest.toString(), mapStringTruth.toString());
                Assert.assertEquals(mapTest.toString(), mapTruth.toString());
                Assert.assertEquals(mapTest, mapStringTruth);

                System.out.println("\nbye!");
            }
        }

        public static void main(String[] args) {
            // specific regex test
            String input = "0,1\\n1,2\\n";
            Pattern p = Pattern.compile("\\s*,\\s*");
            Matcher m = p.matcher(input);
            while (m.find()) {
                print("Start index: " + m.start());
                print(" End index: " + m.end() + " ");
                println(m.group());
            }

            // repeated user tests of regex from console input
            while (true) {
                String patternString = readLine("enter pattern:");
                if (patternString == null) {
                    break;
                }
                Pattern pattern = Pattern.compile(patternString);
                String string = readLine("enter string to match against pattern:");
                if (string == null) {
                    break;
                }
                Matcher matcher = pattern.matcher(string);
                while (matcher.find()) {
                    print("Start index: " + matcher.start());
                    print(" End index: " + matcher.end() + " ");
                    println(matcher.group());
                }
            }

            // testing fromString() for a map

        }

    }
}
