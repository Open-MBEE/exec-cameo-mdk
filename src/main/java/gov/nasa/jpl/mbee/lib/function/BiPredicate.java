package gov.nasa.jpl.mbee.lib.function;

/**
 * Created by igomes on 6/30/16.
 */
public interface BiPredicate<T, U> {
    boolean test(T t, U u);
}