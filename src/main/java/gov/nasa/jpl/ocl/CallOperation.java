package gov.nasa.jpl.ocl;

/**
 * Function pointer interface to be used by custom OCL operations (basically make anonymous class
 * overriding callOperation method when defining methods) 
 * 
 * Example regex implementation is:
 * 
   new CallOperation() {
			@Override
			public Object callOperation(Object source, Object[] args) {
				Pattern pattern = Pattern.compile((String) args[0]);
				Matcher matcher = pattern.matcher((String) source);
	
				return matcher.matches() ? matcher.group() : null;
			}
	}
 * 
 * @author cinyoung
 *
 */
public interface CallOperation {
	/**
	 * The implmentation of this methodshould specify the logic for the custom logic 
	 * @param source	Source object that is calling the operation
	 * @param args		Arguments to the calling operation
	 * @return			Result of the callOperation
	 */
	public Object callOperation(Object source, Object[] args);
}
