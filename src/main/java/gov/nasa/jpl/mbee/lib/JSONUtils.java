package gov.nasa.jpl.mbee.lib;

import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtils {

	private JSONObject results;

	/**
	 * This function will return true or false if the model and web data is equal
	 * First it will sanitize the model and web objects, then it will compare
	 *
	 * @param dirtyModel a "dirty" object that we assume is some sort of JSON from the model
	 * @param dirtyWeb a "dirty" object that we assume is some sort of JSON from the web
	 * @param recurse whether to recursively compare or not
	 * @return a boolean true if model and web are effectively equal else false
	 */
	public static boolean compare(Object dirtyModel, Object dirtyWeb, boolean recurse) {
		return true;
	}

	/**
	 * This function will return the differences between two JSON items as a JSONObject
	 * It will sanitize the items, and log the sanitizations in the diff if necessary
	 * If there are absolutely no differences (including no sanitization changes) this returns null
	 *
	 * @param dirtyModel a "dirty" object that we assume is some sort of JSON from the model
	 * @param dirtyWeb a "dirty" object that we assume is some sort of JSON from the web
	 * @param recurse whether to recursively diff or not
	 * @return a JSONObject representing the differences between model and web
	 */
	public static JSONObject diff(Object dirtyModel, Object dirtyWeb, boolean recurse) {
		return null;
	}

	public static boolean compareJSON(Object dmod, Object dweb, boolean recurse) {
		return true;
	}

	/**
	 * This compares two JSONObjects; sanitizes; recurses
	 * Only use this for comparing specialization
	 *
	 * @param dweb a particular element json (not sanitized)
	 * @param dmod a particular element json (not sanitized)
	 * @return boolean if web and model are equivalent
	 */
	public static boolean compareJSONObject(JSONObject dmod, JSONObject dweb) {

		if (dmod == dweb) return true; // this handles when both are null i think
		if (dmod == null || dweb == null) return false;

//		// sanitize json
		JSONObject mod = sanitizeJSONObject(dmod);
		JSONObject web = sanitizeJSONObject(dweb);

		// checking the keys is easy
		if (!(mod.keySet().equals(web.keySet()))) return false;

		Set<Object> keys = mod.keySet();

		// checking the values is more involved
		for (Object key : keys) {
			Object modVal = mod.get(key);
			Object webVal = web.get(key);
			if (!(modVal.getClass().equals(webVal.getClass()))) return false;
			if (modVal instanceof JSONObject) {
				JSONObject mObject = (JSONObject) modVal;
				JSONObject wObject = (JSONObject) webVal;
				// go ahead and recurse, break out if the recursion is false
				if (!compareJSONObject(mObject, wObject)) return false;
			} else if (modVal instanceof JSONArray) {
				JSONArray mArray = (JSONArray) modVal;
				JSONArray wArray = (JSONArray) webVal;
				if (!compareJSONArray(mArray, wArray)) return false;
			}
		}

		return true;
	}

	public static boolean compareJSONArray(JSONArray dmod, JSONArray dweb) {
		if (dmod == dweb) return true;
		if (dmod == null || dweb == null) return false;

		// sanitize JSON
//		JSONArray mod = sanitizeJSONArray(dmod);

		// size check


		return true;

//    	if (a != null && b != null) {
//    		Set as = new HashSet();
//    		Set bs = new HashSet();
//    		as.addAll(a);
//    		bs.addAll(b);
//    		if (as.equals(bs))
//    			return true;
//    		return false;
//    	}
//    	if (a == b)
//    		return true;
//    	return false;
	}

	/**
	 *
	 * This function deletes empty arrays from json objects
	 * This will prevent false positives in the Validation Window, where implemented
	 *
	 * @param json is a JSONObject that will be cleaned up
	 * @return ret a JSONObject without offending key value pairs
	 */
	public static JSONObject sanitizeJSONObject(JSONObject json) {
		JSONObject ret = new JSONObject();

		for (Map.Entry entry : (Set<Map.Entry>) json.entrySet()) {
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (val instanceof JSONArray && !((JSONArray)val).isEmpty()) {
				ret.put(key, sanitizeJSONArray((JSONArray)val));
			} else if (val instanceof JSONObject && !((JSONObject)val).isEmpty()) {
				ret.put(key, sanitizeJSONObject((JSONObject)val));
			} else if (val instanceof String && !((String)val).isEmpty()) {
				ret.put(key, (String)val);
			} else if (val != null) {
				ret.put(key, val);
			}
		}

		return ret;
	}

	/**
	 *
	 * This function deletes empty arrays or values from JSONArrays
	 * It's also recursive because that's how computers do
	 *
	 * @param json is a JSONArray that will be cleaned up
	 * @return ret a JSONArray without the offending values
	 */
	public static JSONArray sanitizeJSONArray(JSONArray json) {
		JSONArray ret = new JSONArray();

		for (Object item : json.toArray()) {
			if (item instanceof JSONArray && !((JSONArray)item).isEmpty()) {
				ret.add(sanitizeJSONArray((JSONArray)item));
			} else if (item instanceof JSONObject && !((JSONObject)item).isEmpty()) {
				ret.add(sanitizeJSONObject((JSONObject)item));
			} else if (item instanceof String && !((String)item).isEmpty()) {
				ret.add((String)item);
			} else if (item != null) {
				ret.add(item);
			}
		}

		return ret;
	}

}
