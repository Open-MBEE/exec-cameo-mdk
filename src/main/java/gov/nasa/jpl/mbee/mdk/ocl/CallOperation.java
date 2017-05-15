package gov.nasa.jpl.mbee.mdk.ocl;

/**
 * Function pointer interface to be used by custom OCL operations (basically
 * make anonymous class overriding callOperation method when defining methods)
 * <p>
 * Example regex implementation is:
 * <p>
 * new CallOperation() {
 *
 * @author cinyoung
 * @Override public Object callOperation(Object source, Object[] args) { Pattern
 * pattern = Pattern.compile((String) args[0]); Matcher matcher =
 * pattern.matcher((String) source);
 * <p>
 * return matcher.matches() ? matcher.group() : null; } }
 */
public interface CallOperation {
    /**
     * The implementation of this method should specify the logic for the custom
     * logic
     *
     * @param source Source object that is calling the operation
     * @param args   Arguments to the calling operation
     * @return Result of the callOperation
     */
    Object callOperation(Object source, Object[] args);
}
