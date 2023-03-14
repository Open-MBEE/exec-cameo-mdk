package org.openmbee.mdk.emf;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.openmbee.mdk.api.incubating.convert.Converters;
import junit.framework.Assert;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.openmbee.mdk.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public final class EmfUtils {
    public static String toString(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof Collection) {
            Collection<?> c = (Collection<?>) o;
            int count = 0;
            while (c.size() == 1 && c != c.iterator().next() && count++ < 5) {
                o = c.iterator().next();
                if (o instanceof Collection) {
                    c = (Collection<?>) o;
                }
            }
            if (c == o && c.size() != 1) {
                StringBuffer sb = new StringBuffer();
                sb.append("(");
                boolean first = true;
                for (Object oo : c) {
                    if (first) {
                        first = false;
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append(toString(oo));
                }
                sb.append(")");
                return sb.toString();
            }
        }
        else if (o.getClass().isArray()) {
            Object[] arr = (Object[]) o;
            if (arr.length == 1) {
                return toString(arr[0]);
            }
            // TODO -- potential infinite loop -- use Utils2.seen()
            return toString(Arrays.asList(arr));
        }
        String result = null;
        String name = getName(o);
        if (Utils2.isNullOrEmpty(name)) {
            name = "";
        }
        else {
            name = name + ":";
        }
        if (o instanceof Element) {
            Element e = (Element) o;
            String repText = e.get_representationText();
            if (Utils2.isNullOrEmpty(repText)) {
                repText = "";
            }
            else {
                repText = ":" + repText;
            }
            result = name + // e.getHumanType() + ":" +
                    (Debug.isOn() ? Converters.getElementToIdConverter().apply(e) : "") + repText;
            result = result.replaceFirst("::", ":");
            result = result.trim().replaceAll("^:", "");
            result = result.trim().replaceAll(":$", "");
            return result;
        }
        if (Utils2.isNullOrEmpty(name)) {
            result = o.toString();
        }
        else {
            result = name + getTypeNames(o);
        }
        result = result.trim().replaceAll("^:", "");
        result = result.trim().replaceAll(":$", "");
        return result;
    }

    /**
     * @param specifier
     * @return
     */
    public static List<String> getPossibleFieldNames(String specifier) {
        List<String> possibleFieldNames = new ArrayList<String>();

        // get field(s) with matching name
        possibleFieldNames.add(specifier);
        possibleFieldNames.add(specifier.toUpperCase(Locale.US));
        possibleFieldNames.add(specifier.toLowerCase(Locale.US));
        String capitalizedSpec = Utils2.capitalize(specifier);
        if (Character.isLowerCase(specifier.charAt(0))) {
            possibleFieldNames.add(capitalizedSpec);
        }
        return possibleFieldNames;
    }

    /**
     * @param specifier
     * @return
     */
    public static List<String> getPossibleMethodNames(String specifier) {
        return getPossibleMethodNames(specifier, "e", "eGet", "get", "get_");
    }

    /**
     * @param specifier
     * @param methodPrefixes
     * @return methods with names that match the specifier or the specifier with
     * one of the method prefixes
     */
    public static List<String> getPossibleMethodNames(String specifier, String... methodPrefixes) {
        // get methods with matching names
        List<String> possibleMethodNames = getPossibleFieldNames(specifier);
        String capitalizedSpec = Utils2.capitalize(specifier);
        for (String prefix : methodPrefixes) {
            possibleMethodNames.add(prefix + capitalizedSpec);
        }
        return possibleMethodNames;
    }

    /**
     * Determines the validity of a return value packaged with a {@link Boolean}
     * in a {@link Pair}.
     * <p>
     * <p>
     * A method may return a success/fail {@link Boolean} with another return
     * value in a pair. For example, a getValue() method may want to return null
     * sometimes as a valid value and at other times as an invalid value.
     * <p>
     * <p>
     * The {@link Boolean} may be null, which this method assumes means that
     * success is "unknown." When unknown, this method gives the benefit of the
     * doubt for non-null return values, so true is returned for Pair(null,
     * nonNullT), and false for Pair(null,null), assuming that an unsuccessful
     * call always has a null return value.
     *
     * @param p a return value from another function paired with a success
     *          flag, Pair(Boolean success, T returnValue).
     * @return true if the success flag is true or null with a non-null return
     * value.
     */
    public static <T> boolean trueOrNotNull(Pair<Boolean, T> p) {
        return (p.getKey() == null && p.getValue() != null) || (p.getKey() != null && p.getKey());
    }

    /**
     * Get the values of the object's fields that have one the specified names
     * (or, if not strict, some close variation). Only return values that are
     * instances of the specified {@link Class}. If cls is null, return all
     * values for matching fields.
     *
     * @param o
     * @param cls
     * @param propagate
     * @param justFirst
     * @param specifiers
     * @return field members of o that have one of the specified names or are
     * instances of cls
     */
    public static <T> List<T> getMethodResults(Object o, Class<T> cls, boolean propagate,
                                               boolean strictMatch, boolean justFirst, String... specifiers) {
        LinkedHashSet<T> results = new LinkedHashSet<T>();
        if (o == null || specifiers == null) {
            return Collections.emptyList();
        }
        for (String specifier : specifiers) {
            List<String> possibleMethodNames = (strictMatch ? Utils2.newList(specifier)
                    : getPossibleMethodNames(specifier));
            for (String name : possibleMethodNames) {
                Method[] methods = ClassUtils.getMethodsForName(o.getClass(), name);
                if (methods != null) {
                    for (Method method : methods) {
                        // TODO -- pass in potential arguments? can they be
                        // deduced?
                        Pair<Boolean, Object> pr = ClassUtils.runMethod(true, o, method);
                        if (trueOrNotNull(pr)) {
                            Pair<Boolean, T> pc = ClassUtils.coerce(pr.getValue(), cls);
                            if (trueOrNotNull(pc)) {
                                results.add(pc.getValue());
                                if (justFirst) {
                                    return Utils2.asList(results);
                                }
                            }
                        }
                    }
                }
            }
        }
        return Utils2.asList(results);
    }

    /**
     * Get methods whose names are variations of those specified and return the
     * results of their invocations. Only return values that are instances of
     * the specified {@link Class}. If cls is null, return all values for
     * matching members.
     *
     * @param o
     * @param cls
     * @param propagate
     * @param justFirst
     * @param specifiers
     * @return member values of o that have one of the specified names or are
     * instances of cls
     */
    public static <T> List<T> getFieldValues(Object o, Class<T> cls, boolean propagate, boolean strictMatch,
                                             boolean justFirst, String... specifiers) {
        LinkedHashSet<T> results = new LinkedHashSet<T>();
        if (o == null || specifiers == null) {
            return Collections.emptyList();
        }
        for (String specifier : specifiers) {
            List<String> possibleFieldNames = (strictMatch ? Utils2.newList(specifier)
                    : getPossibleFieldNames(specifier));
            for (String name : possibleFieldNames) {
                if (ClassUtils.getField(o, name) != null) {
                    Object r = ClassUtils.getField(o, name);
                    Pair<Boolean, T> p = // Expression.
                            ClassUtils.coerce(r, cls);
                    if (trueOrNotNull(p)) {
                        results.add(p.getValue());
                        if (justFirst) {
                            return Utils2.asList(results);
                        }
                    }
                }
            }
        }
        return Utils2.asList(results);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> List<T> getFieldValues(Object o, boolean includeStatic) {
        List<T> results = new ArrayList<T>();
        for (Field f : o.getClass().getFields()) {
            if (!includeStatic && ClassUtils.isStatic(f)) {
                continue;
            }
            f.setAccessible(true);
            T t = null;
            try {
                t = (T) f.get(o);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
            results.add(t);
        }
        return results;
    }

    /**
     * Get the value of the object's field with the specified name (or some
     * close variation). Or, if the field does not exist, find a method whose
     * name is a variation of the one specified and return the result of its
     * invocation.
     *
     * @param o
     * @param specifier
     * @param propagate
     * @return
     */
    public static Object getMemberValue(Object o, String specifier, boolean propagate, boolean strictMatch) {
        List<Object> members = getMemberValues(o, Object.class, propagate, strictMatch, true, specifier);
        if (Utils2.isNullOrEmpty(members)) {
            return null;
        }
        return members.get(0);
    }

    /**
     * Get the values of the object's fields that have one the specified names
     * (or some close variation). Then get methods whose names are variations of
     * those specified and return the results of their invocations. Only return
     * values that are instances of the specified {@link Class}. If cls is null,
     * return all values for matching members.
     *
     * @param o
     * @param cls
     * @param propagate
     * @param justFirst
     * @param specifiers
     * @param strictMatch
     * @return member values of o that have one of the specified names or are
     * instances of cls
     */
    public static <T> List<T> getMemberValues(Object o, Class<T> cls, boolean propagate, boolean strictMatch,
                                              boolean justFirst, String... specifiers) {
        if (o == null || specifiers == null) {
            return Collections.emptyList();
        }
        LinkedHashSet<T> results = new LinkedHashSet<T>();
        results.addAll(getFieldValues(o, cls, propagate, true, justFirst, specifiers));
        if (justFirst && !results.isEmpty()) {
            return Utils2.asList(results);
        }
        results.addAll(getFieldValues(o, cls, propagate, true, justFirst, specifiers));
        if (justFirst && !results.isEmpty()) {
            return Utils2.asList(results);
        }
        if (!strictMatch) {
            results.addAll(getMethodResults(o, cls, propagate, false, justFirst, specifiers));
            if (justFirst && !results.isEmpty()) {
                return Utils2.asList(results);
            }
            results.addAll(getMethodResults(o, cls, propagate, false, justFirst, specifiers));
        }
        return Utils2.asList(results);
    }

    public static Collection<Class<?>> getTypes(Object o) {
        if (o == null) {
            return null;
        }
        EObject eo = (EObject) (o instanceof EObject ? o : null);

        Collection<Class<?>> results = new LinkedHashSet<Class<?>>();
        if (eo != null) {
            results.addAll(getTypes(eo, true, true, true, true, null));
        }
        // results.add( o.getClass() );
        results.addAll(ClassUtils.getAllClasses(o));
        return results;
    }

    public static Class<?> getType(Object o) {
        if (o == null) {
            return null;
        }
        EObject eo = (EObject) (o instanceof EObject ? o : null);
        Class<?> c = (eo != null ? getType(eo) : o.getClass());
        return c;
    }

    public static String getTypeName(Object o) {
        if (o == null) {
            return null;
        }
        Class<?> c = getType(o);
        if (c == null) {
            return null;
        }
        return c.getSimpleName();
    }

    public static Collection<String> getTypeNames(Object o) {
        Collection<Class<?>> types = getTypes(o);
        if (types == null) {
            return null;
        }
        Collection<String> results = new TreeSet<String>();
        for (Class<?> t : types) {
            results.add(t.getSimpleName());
        }
        return results;
    }

    public static String getName(Object o) {
        String name = null;
        // for the fancy EObject
        EObject eo = (EObject) (o instanceof EObject ? o : null);
        if (eo != null) {
            if (o instanceof EClassifier) {
                name = ((EClassifier) o).getInstanceClassName();
            }
            if (Utils2.isNullOrEmpty(name)) {
                EStructuralFeature nameFeature = eo.eClass().getEStructuralFeature("name");
                if (nameFeature != null) {
                    name = (String) eo.eGet(nameFeature);
                }
            }
            if (Utils2.isNullOrEmpty(name)) {
                if (eo instanceof Element) {
                    if (eo instanceof ENamedElement) {
                        name = ((ENamedElement) eo).getName();
                    }
                    else {
                        name = ((Element) eo).getHumanName();
                    }
                }
            }
        }
        if (Utils2.isNullOrEmpty(name)) {
            // for the vanilla object
            Object n = getMemberValue(o, "name", true, false);
            if (n != null) {
                name = n.toString();
            }
        }
        return name;
    }

    public static void getEObjectsOfType(EObject o, Class<?> type, Set<EObject> set) {
        assert set != null;
        if (type.isAssignableFrom(o.getClass())) {
            if (set.contains(o)) {
                return;
            }
            else {
                set.add(o);
            }
        }
        Iterator<EObject> iter = o.eContents().iterator();
        while (iter.hasNext()) {
            EObject subO = iter.next();
            getEObjectsOfType(subO, type, set);
        }
    }

    public static Set<EObject> getEObjectsOfType(EObject o, Class<?> type) {
        Set<EObject> set = new LinkedHashSet<EObject>();
        getEObjectsOfType(o, type, set);
        return set;
    }

    public static boolean contains(EObject outer, EObject inner) {
        for (EObject o : getEObjectsOfType(outer, inner.getClass())) {
            if (o == inner) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param eObj
     * @param cls
     * @param propagate   whether to propagate value dependencies through Expressions.<br>
     *                    TODO -- Expression-specific stuff like this should go back to
     *                    Expression. A version of coerce() can be defined in ClassUtils
     *                    without referencing Parameters and Expressions.
     * @param strictMatch
     * @param justFirst
     * @return
     */
    public static <TT> List<TT> getEValues(EObject eObj, Class<TT> cls, boolean propagate,
                                           boolean strictMatch, boolean justFirst, boolean complainIfNotFound, SeenSet<Object> seen) {
        // Check for bad input
        if (eObj == null) {
            if (complainIfNotFound) {
                Debug.error(true, "Error! Passed null object to getValues().");
            }
            return Collections.emptyList();
        }
        // return if we've already tried this eObj to avoid infinite recursion
        Pair<Boolean, Set<Object>> sp = Utils2.seen(eObj, true, (Set<Object>) seen);
        if (sp.getKey()) {
            return Collections.emptyList();
        }
        seen = (SeenSet<Object>) sp.getValue();

        List<TT> results = new ArrayList<TT>();

        // add "Value" to a list with its other words that may reference the
        // value
        List<String> list = Utils2.newList();
        list.addAll(Arrays.asList(eWordsForValue));
        boolean strictThisTime = true;
        List<EStructuralFeature> seenFeatures = Utils2.newList();
        Pair<Boolean, TT> p = null;
        // At most two loop iterations. First loop is for strict matches. Second
        // is
        // for non-strict.
        while (true) {
            String[] sArr = new String[list.size()];
            Utils2.toArrayOfType(list, sArr, String.class);
            List<EStructuralFeature> features = findStructuralFeaturesMatching(eObj, strictThisTime, false,
                    sArr);
            // remove strict matches that are duplicated in non-strict matches
            features.removeAll(seenFeatures);
            // collect seen features for next loop iteration
            seenFeatures.addAll(features);
            // get non-null results
            Debug.outln("features=" + features);
            for (EStructuralFeature f : features) {
                if (f != null) {
                    Object res = eObj.eGet(f);
                    if (res != null) {
                        p = // Expression.
                                ClassUtils.coerce(res, cls);
                        if (trueOrNotNull(p)) {
                            results.add(p.getValue());
                            if (justFirst) {
                                return results;
                            }
                        }
                    }
                }
            }
            // get results for contents
            for (EObject eo : eObj.eContents()) {
                // check if contained object's name matches
                // boolean found = false;
                String myName = getName(eo);
                boolean noName = Utils2.isNullOrEmpty(myName);
                if (!noName && list.contains(myName)) {
                    p = // Expression.
                            ClassUtils.coerce(eo, cls);
                    if (trueOrNotNull(p)) {
                        results.add(p.getValue());
                        if (justFirst) {
                            return results;
                        }
                        // found = true;
                    }
                }
                if (!noName && !strictThisTime) {
                    // non-strict name check
                    for (String name : list) {
                        if (myName.contains(name)) {
                            p = // Expression.
                                    ClassUtils.coerce(eo, cls);
                            if (trueOrNotNull(p)) {
                                results.add(p.getValue());
                                if (justFirst) {
                                    return results;
                                }
                                // found = true;
                                break;
                            }
                        }
                    }
                }
                // if ( !found ) {
                // check if contained object has "values"
                List<TT> resList = getValues(eo, cls, propagate, strictThisTime, justFirst, false, seen);
                if (!Utils2.isNullOrEmpty(resList)) {
                    if (justFirst) {
                        return resList;
                    }
                    // Don't combine using Utils2.addAll(), which is unordered!
                    results.addAll(resList);
                }
            }
            // }
            if (strictMatch) {
                break;
            }
            if (!strictThisTime) {
                break;
            }
            strictThisTime = false;
            // sizeOfLast = features.size(); // skip the ones we have already
            // seen on the next loop
        }

        // If failed print the last Exception's stack trace.
        if (complainIfNotFound && Utils2.isNullOrEmpty(results)) {
            Debug.error(false, "Error! EmfUtils.getValues(" + getName(eObj) + ") found no value to return!");
        }

        return results;
    }

    /**
     * Get a "value" corresponding to the {@link Object} that are instances of
     * the input {@link Class} type.
     *
     * @param obj
     * @param cls
     * @param propagate
     * @param strictMatch
     * @param complainIfNotFound
     * @return
     */
    public static <TT> TT getValue(Object obj, Class<TT> cls, boolean propagate, boolean strictMatch,
                                   boolean complainIfNotFound) {
        List<TT> values = getValues(obj, cls, propagate, strictMatch, true, complainIfNotFound, null);
        if (Utils2.isNullOrEmpty(values)) {
            return null;
        }
        return values.get(0);
    }

    /**
     * Get "values" corresponding to the {@link Object} that are instances of
     * the input {@link Class} type.
     *
     * @param obj
     * @param cls
     * @param propagate
     * @param strictMatch
     * @param justFirst
     * @param complainIfNotFound
     * @return
     */
    public static <TT> List<TT> getValues(Object obj, Class<TT> cls, boolean propagate, boolean strictMatch,
                                          boolean justFirst, boolean complainIfNotFound, SeenSet<Object> seen) {
        // Check for bad input
        if (obj == null) {
            if (complainIfNotFound) {
                Debug.error(true, "Error! Passed null object to getValues().");
            }
            return Collections.emptyList();
        }

        // return if we've already tried this eObj to avoid infinite recursion
        Pair<Boolean, SeenSet<Object>> sp = Utils2.seen(obj, true, seen);
        if (sp.getKey()) {
            return Collections.emptyList();
        }
        seen = sp.getValue();

        List<TT> results = null;// new called by getMembers() call below. // new
        // ArrayList< TT >();
        Pair<Boolean, TT> p = null;

        // Try for an exact match with a member field or function.
        results = getMemberValues(obj, cls, propagate, true, justFirst, "value");
        if (!Utils2.isNullOrEmpty(results) && justFirst) {
            return results;
        }

        // Try getting value as an EObject.
        EObject eObj = null;
        if (obj instanceof EObject) {
            eObj = (EObject) obj;
            seen.remove(obj);
            // Try getting strict matching values as an EObject. Will be less
            // strict later.
            List<TT> vList = EmfUtils.getEValues(eObj, cls, propagate, false, justFirst, false, seen);
            if (justFirst && !vList.isEmpty()) {
                return vList;
            }
            // Don't combine using Utils2.addAll(), which is unordered!
            results.addAll(vList);
        }

        // Return the obj if it is already the *exact* right type.
        if (cls != null && cls.equals(obj.getClass())) {
            p = // Expression.
                    ClassUtils.coerce(obj, cls);
            if (trueOrNotNull(p)) {
                results.add(p.getValue());
                if (justFirst) {
                    return results;
                }
            }
            else {
                Debug.error(true, "Error! Coercion of " + obj + " of type " + obj.getClass().getSimpleName()
                        + " to " + cls.getSimpleName() + " unexpectedly failed!");
            }
        }

        // Try finding members of other names that could mean "value."
        String clsName = (cls == null ? "object" : cls.getSimpleName());
        ArrayList<String> wordsForValueList = new ArrayList<String>();
        String[] wordsForValue = new String[]{clsName + "Value", "literalValue", clsName};
        wordsForValueList.addAll(Arrays.asList(wordsForValue));
        wordsForValueList.addAll(Arrays.asList(EmfUtils.oWordsForValue));
        wordsForValue = new String[wordsForValueList.size()];
        Utils2.toArrayOfType(wordsForValueList, wordsForValue, String.class);

        // Try for an exact match with a member field or function.
        List<TT> resList = getMemberValues(obj, cls, propagate, strictMatch, justFirst,
                wordsForValue);
        if (justFirst && !resList.isEmpty()) {
            return resList;
        }
        // Don't combine using Utils2.addAll(), which is unordered!
        results.addAll(resList);

        // Try to coerce the input obj to the correct type.
        if (cls != null) {// && Utils2.isNullOrEmpty( results ) ) {
            p = // Expression.
                    ClassUtils.coerce(obj, cls);
            if (trueOrNotNull(p)) {
                results.add(p.getValue());
                if (justFirst) {
                    return results;
                }
            }
        }

        // if yet unsuccessful and cls == String.class return toString()
        if (Utils2.isNullOrEmpty(results) && cls != null && cls.equals(String.class)) {
            p = // Expression.
                    ClassUtils.coerce(obj.toString(), cls);
            if (trueOrNotNull(p)) {
                results.add(p.getValue());
                if (justFirst) {
                    return results;
                }
            }
            else {
                // We should never get here.
                String msg = "Error! Coercion of " + obj + " of type " + obj.getClass().getSimpleName()
                        + " to " + cls.getSimpleName() + " unexpectedly failed!";
                Assert.assertFalse(msg, true);
                Debug.error(true, msg);
            }
        }

        // If we aren't restricted by type and are yet unsuccessful, return the
        // object itself.
        if ((cls == null || cls.equals(Object.class)) && Utils2.isNullOrEmpty(results)) {
            p = // Expression.
                    ClassUtils.coerce(obj, cls);
            if (trueOrNotNull(p)) {
                results.add(p.getValue());
                if (justFirst) {
                    return results;
                }
            }
        }

        // If failed print the last Exception's stack trace.
        if (complainIfNotFound && Utils2.isNullOrEmpty(results)) {
            Debug.error(false, "Error! EmfUtils.getValues(" + getName(obj) + ") found no value to return!");
        }

        return results;
    }

    public static List<EStructuralFeature> findStructuralFeaturesMatching(EObject eObj, boolean strictMatch,
                                                                          boolean justfirst, String... possibleNames) {
        List<EStructuralFeature> features = new ArrayList<EStructuralFeature>();
        ArrayList<String> list = new ArrayList<String>();
        for (String s : possibleNames) {
            if (!strictMatch) {
                s = s.toLowerCase();
            }
            list.add(s);
        }
        TreeSet<String> names = new TreeSet<String>(list);
        for (EStructuralFeature f : eObj.eClass().getEStructuralFeatures()) {
            String fName = f.getName();
            if (!strictMatch) {
                fName = fName.toLowerCase();
            }
            if (names.contains(fName)) {
                features.add(f);
                if (justfirst) {
                    return features;
                }
            }
            if (!strictMatch) {
                for (String valueWord : names) {
                    if (fName.contains(valueWord)) {
                        features.add(f);
                        if (justfirst) {
                            return features;
                        }
                    }
                }
            }
        }
        return features;
    }

    public static Class<?> getType(EObject eObj) {
        return getType(eObj, true);
    }

    public static Class<?> getType(EObject eObj, boolean strictMatch) {
        List<Class<?>> list = getTypes(eObj, true, strictMatch, true, true, null);
        if (!Utils2.isNullOrEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    public static Class<?> asClass(Object obj, SeenSet<Object> seen) {
        Class<?> cls = null;
        if (obj instanceof EClassifier) {
            cls = ((EClassifier) obj).getInstanceClass();
        }
        if (cls == null) {
            cls = ClassUtils.evaluate(obj, Class.class);
        }
        if (cls == null) {
            @SuppressWarnings("rawtypes")
            List<Class> values = getValues(obj, Class.class, true, true, true, false, seen);
            if (!Utils2.isNullOrEmpty(values)) {
                cls = values.get(0);
            }
        }
        return cls;
    }

    public static List<Class<?>> getTypes(EObject eObj, boolean propagate, boolean strictMatch,
                                          boolean justFirst, boolean complainIfNotFound, SeenSet<Object> seen) {
        List<Class<?>> results = new ArrayList<Class<?>>();
        List<Object> typeObjects = getTypeObjects(eObj, propagate, strictMatch, justFirst,
                complainIfNotFound, seen);
        for (Object typeObj : typeObjects) {
            if (typeObj != null) {
                Class<?> cls = asClass(typeObj, seen);
                if (cls != null) {
                    results.add(cls);
                    if (justFirst) {
                        return results;
                    }
                }
            }
        }
        return results;
    }

    public static List<Object> getTypeObjects(EObject eObj, boolean propagate, boolean strictMatch,
                                              boolean justFirst, boolean complainIfNotFound, SeenSet<Object> seen) {
        if (eObj == null) {
            return null;
        }

        // return if we've already tried this eObj to avoid infinite recursion
        Pair<Boolean, SeenSet<Object>> sp = Utils2.seen(eObj, true, seen);
        if (sp.getKey()) {
            return Collections.emptyList();
        }
        seen = sp.getValue();

        ArrayList<Object> results = new ArrayList<Object>();
        results.add(eObj.eClass());
        if (justFirst) {
            return results;
        }

        TreeSet<String> wordsForTypeSet = new TreeSet<String>(Arrays.asList(wordsForType));

        boolean strictThisTime = true;
        int sizeOfLast = 0;
        // At most two loop iterations. First loop is for strict matches. Second
        // is
        // for non-strict.
        while (true) {
            // Get structural features whose names are words for "type"
            List<EStructuralFeature> features = findStructuralFeaturesMatching(eObj, strictThisTime, false,
                    wordsForType);
            // Ignore (remove) the features we already saw from the last loop.
            if (sizeOfLast > 0) {
                features = features.subList(sizeOfLast, features.size());
            }
            Object res = null;
            // See if the eObj's instantiations of these structural features are
            // Classes.
            for (EStructuralFeature f : features) {
                if (f != null) {
                    res = eObj.eGet(f);
                    // Class<?> cls = asClass( res, seen );
                    if (res != null) {
                        results.add(res);
                        if (justFirst) {
                            return results;
                        }
                    }
                }
            }
            if (res == null) {
                for (EObject eo : eObj.eContents()) {
                    // check if contained object's name indicates that it's a
                    // type
                    boolean found = false;
                    String myName = getName(eo);
                    if (myName != null && wordsForTypeSet.contains(getName(eo))) {
                        // Is the contained object itself represent a type?
                        // Class<?> cls = asClass( eo, seen );
                        if (eo != null) {
                            results.add(eo);
                            if (justFirst) {
                                return results;
                            }
                            found = true;
                        }
                        else {
                            // S
                            List<Object> oList = getValues(eo, Object.class, propagate, strictMatch,
                                    justFirst, false, seen);
                            for (Object o : oList) {
                                // TODO -- REVIEW -- have already seen o from
                                // getValues()?!!
                                // cls = asClass( o, seen );
                                if (o != null) {
                                    results.add(o);
                                    if (justFirst) {
                                        return results;
                                    }
                                    found = true;
                                }
                            }
                        }
                        found = true;
                    }
                    if (myName != null && !strictThisTime) {
                        // non-strict name check
                        for (String name : wordsForType) {
                            if (myName.toLowerCase().contains(name.toLowerCase())) {
                                // Class<?> cls = asClass( eo, seen ); // REVIEW
                                // seen correct here?
                                if (eo != null) {
                                    results.add(eo);
                                    if (justFirst) {
                                        return results;
                                    }
                                    found = true;
                                    // break;
                                }
                            }
                        }
                    }
                    if (!found)
                    // get type of value
                    {
                        if (!found && !strictThisTime) {
                            // get types of contents???
                            List<Object> resList = getTypeObjects(eo, propagate, strictMatch, justFirst,
                                    false, seen);
                            if (!Utils2.isNullOrEmpty(resList)) {
                                if (justFirst) {
                                    return resList;
                                }
                                results.addAll(resList);
                            }
                        }
                    }
                }
            }
            if (strictMatch) {
                break;
            }
            if (!strictThisTime) {
                break;
            }
            // skip the ones we have already seen on the next loop
            sizeOfLast = features.size(); // TODO -- REVIEW Didn't we get rid of
            // lastSize??!
            strictThisTime = false;
        }
        return results;
    }

    public static String[] wordsForType = new String[]{"Type", "Class", "Typename", "ClassName",
            "DefaultType", "eClass", "Stereotype", "Metaclass"};

    public static String[] eWordsForValue = new String[]{"value", "StringExpression", "OpaqueExpression",
            "LiteralBoolean", "LiteralInteger", "LiteralNull", "LiteralSpecification", "LiteralString",
            "LiteralUnlimitedNatural", "ElementValue", "Expression", "InstanceValue", "TimeExpression",
            "TimeInterval", "Duration", "DurationInterval", "Interval",

            "ValueSpecification", "propertyValue", "attributeValue", "referenceValue", "body", "result",

            "defaultValue", "specification",

            "literal", "instance"};

    public static String[] oWordsForValue = new String[]{"value", "literal", "instance",

            "Expression", "InstanceValue", "body", "result",

            "StringExpression", "OpaqueExpression", "LiteralBoolean",
            "LiteralInteger", "LiteralNull", "LiteralSpecification", "LiteralString",
            "LiteralUnlimitedNatural", "ElementValue",

            "defaultValue", "specification",

            "ValueSpecification", "propertyValue", "attributeValue", "referenceValue",

            "TimeExpression", "TimeInterval", "Duration", "DurationInterval", "Interval"};

    public static List<Element> getRelationships(Element elem) {
        LinkedHashSet<Element> elements = new LinkedHashSet<Element>();
        elements.addAll(elem.get_relationshipOfRelatedElement());
        elements.addAll(elem.get_directedRelationshipOfSource());
        elements.addAll(elem.get_directedRelationshipOfTarget());
        return Utils2.toList(elements);
    }

    public static boolean matches(String s, String pattern) {
        if (s == pattern) {
            return true;
        }
        if (pattern == null) {
            return false;
        }
        if (s == null) {
            return false;
        }
        if (s.equalsIgnoreCase(pattern)) {
            return true;
        }
        if (s.matches(pattern)) {
            return true;
        }
        List<String> list = getPossibleFieldNames(s);
        list.remove(0);
        for (String os : list) {
            if (os.equalsIgnoreCase(pattern)) {
                return true;
            }
            if (os.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine
     *
     * @param obj
     * @param pattern
     * @return
     */
    public static boolean matches(Object obj, Object pattern) {
        return matches(obj, pattern, true, true);
    }

    public static boolean matches(Object obj, boolean useName, boolean useType, Object[] patterns) {

        for (Object pattern : patterns) {
            if (matches(obj, pattern, useName, useType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(Object obj, Object pattern, boolean useName, boolean useType) {
        if (obj == pattern) {
            return true;
        }
        if (pattern == null) {
            return false;
        }
        if (obj == null) {
            return false;
        }

        String oName = null; // obj's name
        String oStr = null; // obj as String
        Collection<Class<?>> oTypes = null; // obj's type's
        String pStr = null; // pattern as String
        String pName = null; // pattern's name
        // don't use pType

        if (obj.getClass().equals(String.class) && pattern.getClass().equals(String.class)) {
            boolean m = matches((String) obj, (String) pattern);
            if (m || !useType) {
                return m;
            }
        }
        else {
            pStr = pattern.toString();
            if (useName) {
                oName = getName(obj);
                if (Utils2.isNullOrEmpty(oName)) {
                    if (oName != null && oName.equals(pStr)) {
                        return true;
                    }
                }
                else {
                    if (matches(oName, pStr)) {
                        return true;
                    }
                }
            }

            if (useName) {
                pName = getName(pattern);
                if (Utils2.isNullOrEmpty(oName)) {
                    if (oName != null && oName.equals(pName)) {
                        return true;
                    }
                }
                else {
                    if (matches(oName, pName)) {
                        return true;
                    }
                }
            }

            oStr = obj.toString();
            if (Utils2.isNullOrEmpty(oStr)) {
                if (oStr != null && oStr.equals(pStr)) {
                    return true;
                }
            }
            else {
                if (matches(oStr, pStr)) {
                    return true;
                }
                if (useName && matches(oStr, pName)) {
                    return true;
                }
            }
        }

        if (useType) {
            oTypes = getTypes(obj);
            for (Class<?> t : oTypes) {
                for (String oType : new String[]{t.getSimpleName(), t.getName()}) {
                    if (Utils2.isNullOrEmpty(oType)) {
                        if (oType != null && oType.equals(pStr)) {
                            return true;
                        }
                    }
                    else {
                        if (matches(oType, pStr)) {
                            return true;
                        }
                        if (useName && matches(oType, pName)) {
                            return true;
                        }
                    }
                }
            }
            if (obj instanceof Element) {
                Collection<Stereotype> set = StereotypesHelper.getAllAssignedStereotypes(Utils2
                        .newList((Element) obj));
                for (Stereotype sType : set) {
                    String sName = sType.getName();
                    if (matches(sName, pStr)) {
                        return true;
                    }
                    if (useName && matches(sName, pName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static Object collectOrFilter(CollectionAdder adder, Object obj, boolean collect, boolean onlyOne,
                                         boolean useName, boolean useType, boolean useValue, boolean searchJava, Object... filters) {
        if (obj instanceof Collection) {
            return collectOrFilter(adder, (Collection<Object>) obj, collect, onlyOne, useName, useType,
                    useValue, searchJava, filters);
        }
        if (matches(obj, useName, useType, filters)) {
            return obj;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> collectOrFilter(CollectionAdder adder, Collection<Object> elements,
                                               boolean collect, boolean onlyOne, boolean useName, boolean useType, boolean useValue,
                                               boolean searchJava, Object... filters) {
        ArrayList<Object> resultList = new ArrayList<Object>();
        if (filters == null || filters.length == 0 || (filters.length == 1 && filters[0] == null)) {
            return Utils2.newList(elements.toArray());
        }
        for (Object elem : elements) {
            boolean added = false;
            for (Object filter : filters) {
                if (matches(elem, filter, useName, useType)) {
                    added = adder.add(elem, resultList);
                    if (added) {
                        break;
                    }
                }
            }
            if (onlyOne && added) {
                break;
            }
            if (collect && elem instanceof Collection) {
                --adder.defaultFlattenDepth;
                List<Object> childRes = collectOrFilter(adder, (Collection<Object>) elem, collect, onlyOne,
                        useName, useType, useValue, searchJava, filters);
                // collectOrFilter( (Collection< Object >)elem, collect,
                // useName, useType, filters );
                ++adder.defaultFlattenDepth;
                if (childRes != null) {
                    added = adder.add(childRes, resultList);
                    if (onlyOne && added) {
                        break;
                    }
                }
            }
        }
        return resultList;
    }

}
