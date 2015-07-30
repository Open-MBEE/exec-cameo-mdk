package gov.nasa.jpl.mbee.lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtils {

	/**
	 * This method will return true or false if the model and web data is equal
	 * First it will sanitize the model and web objects, then it will compare
	 * This method is only really meant for JSONObjects and JSONArrays
	 *
	 * @param dirtyMod a "dirty" object that we assume is some sort of JSON from the model
	 * @param dirtyWeb a "dirty" object that we assume is some sort of JSON from the web
	 * @param recurse whether to recursively compare or not
	 * @return a boolean true if model and web are effectively equal else false
	 */
	public static boolean compare(Object dirtyMod, Object dirtyWeb) {		
		if (dirtyMod == dirtyWeb) return true;
		if (dirtyMod == null || dirtyWeb == null) return false;

		if (dirtyMod instanceof JSONObject && dirtyWeb instanceof JSONObject) {
			return compareJSONObject((JSONObject) dirtyMod, (JSONObject) dirtyWeb);
		} else if (dirtyMod instanceof JSONArray && dirtyWeb instanceof JSONArray) {
			return compareJSONArray((JSONArray) dirtyMod, (JSONArray) dirtyWeb);
		} else {
			return false;
		}
	}

	/**
	 * This method compares two JSONObjects; sanitizes at this level; recurses
	 *
	 * @param dirtyMod a particular element json (not sanitized)
	 * @param dirtyWeb a particular element json (not sanitized)
	 * @return boolean if web and model are equivalent
	 */
	private static boolean compareJSONObject(JSONObject dirtyMod, JSONObject dirtyWeb) {		
		JSONObject mod = sanitizeJSONObject(dirtyMod);
		JSONObject web = sanitizeJSONObject(dirtyWeb);

		// checking the keys is easy
		if (!(mod.keySet().equals(web.keySet()))) return false;

		Set<Object> keys = mod.keySet();

		// checking the values is more involved
		for (Object key : keys) {
			Object modVal = mod.get(key);
			Object webVal = web.get(key);
			if (!(modVal.getClass().equals(webVal.getClass()))) return false;
			if (modVal instanceof JSONObject && webVal instanceof JSONObject) {
				if (!compareJSONObject((JSONObject) modVal, (JSONObject) webVal)) return false;
			} else if (modVal instanceof JSONArray && webVal instanceof JSONArray) {
				if (!compareJSONArray((JSONArray) modVal, (JSONArray) webVal)) return false;
			} else {
				if (!modVal.equals(webVal)) return false;
			}
		}

		return true;
	}

	/**
	 *
	 * This method compares two JSONArrays; sanitizes at this level; recurses
	 * This relies on the JSONArray equals method
	 *
	 * @param dirtyMod a particular element json (not sanitized)
	 * @param dirtyWeb a particular element json (not sanitized)
	 * @return boolean if web and model are equivalent
	 */
	private static boolean compareJSONArray(JSONArray dirtyMod, JSONArray dirtyWeb) {		
		JSONArray mod = sanitizeJSONArray(dirtyMod);
		JSONArray web = sanitizeJSONArray(dirtyWeb);
		
		if (mod.size() != web.size()) return false;
		if (mod.equals(web)) return true;

		for (Object modItem: mod) {
			int ind = mod.indexOf(modItem);
			Object webItem = web.get(ind);
			if (modItem instanceof JSONArray && webItem instanceof JSONArray) {
				if (!(compareJSONArray((JSONArray) modItem, (JSONArray) webItem))) return false;
			} else if (modItem instanceof JSONObject && webItem instanceof JSONObject) {
				if (!(compareJSONObject((JSONObject) modItem, (JSONObject) webItem))) return false;
			} else {
				if (!modItem.equals(webItem)) return false;
			}
		}
		
		return true;
	}

	/**
	 *
	 * This method deletes empty arrays from json objects
	 * This will prevent false positives in the Validation Window, where implemented
	 * Not recursive
	 *
	 * @param json is a JSONObject that will be cleaned up
	 * @return ret a JSONObject without offending key value pairs
	 */
	@SuppressWarnings("unchecked")
	private static JSONObject sanitizeJSONObject(JSONObject json) {
		JSONObject ret = new JSONObject();

		for (Map.Entry entry : (Set<Map.Entry>) json.entrySet()) {
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (val instanceof JSONArray && !((JSONArray)val).isEmpty()) {
				ret.put(key, (JSONArray)val);
			} else if (val instanceof JSONObject && !((JSONObject)val).isEmpty()) {
				ret.put(key, (JSONObject)val);
			} else if (val instanceof String && !((String)val).isEmpty()) {
				ret.put(key, (String)val);
			} else if (val != null && !(val instanceof JSONArray) && !(val instanceof JSONObject) && !(val instanceof String)) {
				ret.put(key, val);
			}
		}

		return ret;
	}

	/**
	 *
	 * This function deletes empty arrays or values from JSONArrays
	 * This will prevent false positives in the Validation Window, where implemented
	 * Not recursive
	 *
	 * @param json is a JSONArray that will be cleaned up
	 * @return ret a JSONArray without the offending values
	 */
	@SuppressWarnings("unchecked")
	private static JSONArray sanitizeJSONArray(JSONArray json) {
		JSONArray ret = new JSONArray();

		for (Object item : json.toArray()) {
			if (item instanceof JSONArray && !((JSONArray)item).isEmpty()) {
				ret.add((JSONArray)item);
			} else if (item instanceof JSONObject && !((JSONObject)item).isEmpty()) {
				ret.add((JSONObject)item);
			} else if (item instanceof String && !((String)item).isEmpty()) {
				ret.add((String)item);
			} else if (item != null && !(item instanceof JSONArray) && !(item instanceof JSONObject) && !(item instanceof String)) {
				ret.add(item);
			}
		}

		return ret;
	}

	public static JSONObject converge(JSONObject json) {
		JSONObject ret = new JSONObject();
		System.out.println(json.toJSONString());
		Map<Object, Map.Entry> entries = new HashMap<Object, Map.Entry>();
		for (Map.Entry entry: ((Set<Map.Entry>) json.entrySet())) {
			entries.put(entry.getKey(), entry);
		}
		for (Map.Entry entry: ((Set<Map.Entry>) json.entrySet())) {
			if (!entries.containsValue(entry)) {
				continue;
			}
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (val instanceof JSONArray) {
				JSONArray array = (JSONArray) val;
				JSONArray retArray = new JSONArray();
				for (Object item: array) {
					if (entries.containsKey(item)) {
						JSONObject jsonEntry = new JSONObject();
						jsonEntry.put(entries.get(item).getKey(), entries.get(item).getValue());
						retArray.add(jsonEntry);
						entries.remove(item);
					}
				}
				ret.put(key, retArray);
			} else if (val instanceof JSONObject) {
				JSONObject object = (JSONObject) val;
				JSONObject retObject = new JSONObject();
				for (Map.Entry objEntry: ((Set<Map.Entry>) object.entrySet())) {
					if (entries.containsKey(objEntry.getKey())) {
						JSONObject jsonEntry = new JSONObject();
						retObject.put(objEntry.getKey(), entries.get(objEntry.getKey()).getValue());
						entries.remove(objEntry.getKey());
					}
				}
			}
		}
		System.out.println(ret.toJSONString());
		return ret;
	}
	
}
