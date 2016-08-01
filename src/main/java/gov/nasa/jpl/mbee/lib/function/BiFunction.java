package gov.nasa.jpl.mbee.lib.function;

/**
 * Created by igomes on 7/26/16.
 */
public interface BiFunction<T, U, R> {
    R apply(T t, U u);
}
