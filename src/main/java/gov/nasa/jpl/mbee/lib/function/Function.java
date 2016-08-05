package gov.nasa.jpl.mbee.lib.function;

/**
 * Created by igomes on 7/14/16.
 */
public interface Function<T, R> {
    R apply(T t);
}
