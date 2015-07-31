package gov.nasa.jpl.mbee.lib;

import java.util.ArrayList;
import java.util.Collection;
<<<<<<< HEAD
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
=======
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
>>>>>>> bc58372fe5ec61ce14e3ef70687b7551e4548862
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.iterators.ReverseListIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;

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
	
	@SuppressWarnings("unchecked")
	public static JSONObject nest(final JSONObject json) {
		if (json == null) {
			return null;
		}
		if (json.isEmpty()) {
			return new JSONObject();
		}
		
		final JSONObject result = new JSONObject();
		
		final Map<Object, JSONObject> cache = new LinkedHashMap<Object, JSONObject>();
		final Set<Map.Entry<Object, Object>> s = ((Set<Map.Entry<Object, Object>>) json.entrySet());
		final List<Map.Entry<Object, Object>> entries = new ArrayList<Map.Entry<Object, Object>>(s);
		while (!entries.isEmpty()) {
			final ListIterator<Map.Entry<Object, Object>> li = entries.listIterator();
			while (li.hasNext()) {
				final Map.Entry<Object, Object> entry = li.next();
				final JSONObject children = new JSONObject();
				if (entry.getValue() instanceof JSONArray) {
					final JSONArray array = (JSONArray) entry.getValue();
					boolean success = true;
					for (final Object o : array) {
						if (!cache.containsKey(o)) {
							success = false;
							break;
						}
						children.put(o, cache.get(o));
					}
					if (!success) {
						continue;
					}
					result.clear();
					result.put(entry.getKey(), children);
					cache.putAll(result);
					li.remove();
				}
			}
		}
		return result;
		
		// Cool algorithm to efficiently build the tree structure. Unfortunately cannot use because it is not safe to assume sorting.
		
		/*
		JSONObject result = new JSONObject();
		
		final Map<Object, JSONObject> cache = new LinkedHashMap<Object, JSONObject>();
		final Set<Map.Entry<Object, Object>> s = ((Set<Map.Entry<Object, Object>>) json.entrySet());
		final List<Map.Entry<Object, Object>> entries = new ArrayList<Map.Entry<Object, Object>>(s);
		final ListIterator<Map.Entry<Object, Object>> li = new ReverseListIterator(entries);
		while (li.hasNext()) {
			//Application.getInstance().getGUILog().log("GOT HERE DOE");
			final Map.Entry<Object, Object> entry = li.next();
			final JSONObject children = new JSONObject();
			if (entry.getValue() instanceof JSONArray) {
				final JSONArray array = (JSONArray) entry.getValue();
				for (final Object o : array) {
					children.put(o, cache.get(o));
				}
			}
			result.clear();
			result.put(entry.getKey(), children);
			cache.putAll(result);
		}
		return result;
		*/
	}

	public static JSONObject converge(JSONObject json) {
		JSONObject ret = new JSONObject();
//		System.out.println(json.toJSONString());
		ArrayList<Object> badKeys = new ArrayList<Object>();
		
		Map<Object, Map.Entry> entries = new HashMap<Object, Map.Entry>();
		for (Map.Entry entry: ((Set<Map.Entry>) json.entrySet())) {
			entries.put(entry.getKey(), entry);
		}
		for (Map.Entry entry: ((Set<Map.Entry>) json.entrySet())) {
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (val instanceof JSONArray) {
				// val is an array of sysml ids: ["18", "18_01", "18_02"]
				// "array" is the array we will replace
				JSONArray array = (JSONArray) val;
				// the retArray will replace the existing array, but we have to build it
				JSONArray retArray = new JSONArray();
				for (Object item: array) {
					if (entries.containsKey(item)) {
						// build JSONObject placeholder
						JSONObject jsonEntry = new JSONObject();
						jsonEntry.put(entries.get(item).getKey(), entries.get(item).getValue());
						// log the key that you found
						badKeys.add(item);
						// add the JSONObject to the array (this array used to be singular values,
						// but now it holds JSONObjects with values pointing to the subvalues)
						retArray.add(jsonEntry);
					}
				}
				// if there is anything added to the new array, add the array to the return object
				if (!retArray.isEmpty())
					ret.put(key, retArray);
			} else if (val instanceof JSONObject) {
				// val is a map of sysml ids: {"18":{}, "18_01":{}}
				JSONObject object = (JSONObject) val;
				JSONObject retObject = new JSONObject();
				for (Map.Entry objEntry: ((Set<Map.Entry>) object.entrySet())) {
					if (entries.containsKey(objEntry.getKey())) {
						retObject.put(objEntry.getKey(), entries.get(objEntry.getKey()).getValue());
						// log the key that you found
						badKeys.add(objEntry.getKey());
					}
				}
				// if you added anything to the new JSONObject, add it to the ret object
				if (!retObject.isEmpty())
					ret.put(key, retObject);
			} else {
				// this value is just the id itself, simple stuff
				if (entries.containsKey(val)) {
					JSONObject jsonEntry = new JSONObject();
					ret.put(key, entries.get(val));
					badKeys.add(val);
				}
			}
		}
		
		// we know for sure that the badKeys are on the top level only
		for (Object thing: badKeys) {
			ret.remove(thing);
		}
		
//		System.out.println(badKeys);
//		System.out.println(ret.toJSONString());
		return ret;
	}
	
}
