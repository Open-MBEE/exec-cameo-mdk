package gov.nasa.jpl.mbee.mdk.util;

import java.lang.reflect.*;
import java.util.*;

public class ClassUtils {

    public static Class<?> classForPrimitive(Class<?> primClass) {
        return getPrimToNonPrim().get(primClass);
    }

    private static Map<Class<?>, Class<?>> primToNonPrim;

    public static Map<Class<?>, Class<?>> getPrimToNonPrim() {
        if (primToNonPrim == null) {
            initializePrimToNonPrim();
        }
        return primToNonPrim;
    }

    private static Map<Class<?>, Class<?>> initializePrimToNonPrim() {
        primToNonPrim = new HashMap<>(9);
        primToNonPrim.put(boolean.class, Boolean.class);
        primToNonPrim.put(byte.class, Byte.class);
        primToNonPrim.put(char.class, Character.class);
        primToNonPrim.put(short.class, Short.class);
        primToNonPrim.put(int.class, Integer.class);
        primToNonPrim.put(long.class, Long.class);
        primToNonPrim.put(float.class, Float.class);
        primToNonPrim.put(double.class, Double.class);
        primToNonPrim.put(void.class, Void.class);
        return primToNonPrim;
    }

    public static Pair<Boolean, Object> runMethod(boolean suppressErrors, Object o, Method method, Object... args) {
        Pair<Boolean, Object> p = new Pair<>(false, null);
        List<Throwable> errors = new ArrayList<>();
        try {
            p = runMethod(o, method, args);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            if (!suppressErrors) {
                errors.add(e);
            }
        }
        if (!p.getKey() && isStatic(method) && o != null) {
            List<Object> l = Utils2.newList(o);
            l.addAll(Arrays.asList(args));
            p = runMethod(true, (Object) null, method, l.toArray());
            if (!p.getKey() && l.size() > 1) {
                p = runMethod(true, (Object) null, method, o);
            }
        }
        if (!suppressErrors && !p.getKey()) {
            Debug.error(false, "runMethod( " + o + ", " + method + ", " + Utils2.toString(args, true) + " ) failed!");
        }
        for (Throwable e : errors) {
            e.printStackTrace();
        }
        return p;
    }

    public static boolean isStatic(Member method) {
        if (method == null) {
            return false;
        }
        return (Modifier.isStatic(method.getModifiers()));
    }

    /**
     * Invoke the method from the given object with the given arguments.
     *
     * @param o
     * @param m
     * @param args
     * @return in a Pair whether the invocation was successful and the return
     * value (or null)
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Pair<Boolean, Object> runMethod(Object o, Method m, Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return new Pair<>(m != null, m != null ? m.invoke(o, args) : null);
    }

    /**
     * Get the Field of the Class of the object with the given fieldName using
     * reflection.
     *
     * @param o         the object whose field is sought
     * @param fieldName
     * @return the Field of the Class
     */
    public static Field getField(Object o, String fieldName) throws IllegalArgumentException {
        if (o == null || Utils2.isNullOrEmpty(fieldName)) {
            return null;
        }
        try {
            return o.getClass().getField(fieldName);
        } catch (NoSuchFieldException | SecurityException ignored) {
        }
        return null;
    }

    /**
     * @param type
     * @return whether the input type is a number class or a number primitive.
     */
    public static boolean isNumber(Class<?> type) {
        if (type == null) {
            return false;
        }
        Class<?> forPrim = classForPrimitive(type);
        if (forPrim != null) {
            type = forPrim;
        }
        return (Number.class.isAssignableFrom(type));
    }

    /**
     * @param cls
     * @param methodName
     * @return all public methods of {@code cls} (or inherited by {@code cls})
     * that have the simple name, {@code methodName}.
     */
    public static Method[] getMethodsForName(Class<?> cls, String methodName) {
        List<Method> methods = new ArrayList<>();
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(methodName)) {
                methods.add(m);
            }
        }
        Method[] mArr = new Method[methods.size()];
        boolean succ = Utils2.toArrayOfType(methods, mArr, Method.class);
        if (!succ) {
            Debug.error("Error! Cast to Method[] failed for getMethodsForName(" + cls + ", " + methodName
                    + ")");
        }
        return mArr;
    }

    /**
     * Try to convert an object into one of the specified class.
     *
     * @param o   the object to convert into type cls
     * @param cls the {@link Class} of the object to return
     * @return an object of the type specified or null if the conversion was
     * unsuccessful.
     */
    @SuppressWarnings("unchecked")
    public static <T> Pair<Boolean, T> coerce(Object o, Class<T> cls) {
        if (o == null) {
            return new Pair<>(false, null);
        }
        Object v = evaluate(o, cls);
        Boolean succ = null;
        T t = null;
        if (v != null) {
            succ = true;
            try {
                if (cls == null) {
                    t = (T) v;
                }
                else {
                    t = cls.cast(v);
                }
            } catch (ClassCastException e) {
                succ = false;
            }
            if (t == null) {
                succ = false;
            }
        }
        return new Pair<>(succ, t);
    }

    /**
     * Evaluate/dig or wrap the object of the given type cls from the object o,
     * which may be a Parameter or an Expression.
     *
     * @param object the object to evaluate
     * @param cls    the type of the object to find
     * @return o if o is of type cls, an object of type cls that is an
     * evaluation of o, or null otherwise.
     */
    @SuppressWarnings("unchecked")
    public static <TT> TT evaluate(Object object, Class<TT> cls) throws ClassCastException {
        if (object == null) {
            return null;
        }
        // Check if object is already what we want.
        if (cls != null && cls.isInstance(object) || cls == object.getClass()) {
            return (TT) object;
        }

        if (cls != null && ClassUtils.isNumber(cls) && ClassUtils.isNumber(object.getClass())) {
            try {
                Number n = (Number) object;
                Class<?> c = ClassUtils.classForPrimitive(cls);
                if (c == null) {
                    c = cls;
                }
                // TODO -- instead of returning here, assign to object and reuse
                // try/catch below
                if (c == Long.class) {
                    return (TT) (Long) n.longValue();
                }
                if (c == Short.class) {
                    return (TT) (Short) n.shortValue();
                }
                if (c == Double.class) {
                    return (TT) (Double) n.doubleValue();
                }
                if (c == Integer.class) {
                    return (TT) (Integer) n.intValue();
                }
                if (c == Float.class) {
                    return (TT) (Float) n.floatValue();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        TT r;
        try {
            r = (TT) object;
        } catch (ClassCastException cce) {
            Debug.errln("Warning! No evaluation of " + object + " with type " + cls.getName() + "!");
            throw cce;
        }
        if (cls != null && cls.isInstance(r) || cls == r.getClass()) {
            return r;
        }
        return null;
    }

    /**
     * @param o
     * @return a collection of o's Class, superclasses, and interfaces
     */
    public static List<Class<?>> getAllClasses(Object o) {
        Class<?> cls = (Class<?>) (o instanceof Class ? o : o.getClass());
        HashSet<Class<?>> set = new HashSet<>();
        List<Class<?>> classes = new ArrayList<>();
        List<Class<?>> queue = new ArrayList<>();
        queue.add(cls);
        while (!queue.isEmpty()) {
            Class<?> c = queue.get(0);
            queue.remove(0);
            if (set.contains(c)) {
                continue;
            }
            Class<?> parent = cls.getSuperclass();
            if (parent != null && !set.contains(parent)) {
                queue.add(parent);
            }
            queue.addAll(Arrays.asList(cls.getInterfaces()));
            classes.add(0, c);
            set.add(c);
        }
        return classes;
    }
}
