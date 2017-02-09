package gov.nasa.jpl.mbee.lib;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
		if (dirtyMod ==  null && (dirtyWeb instanceof Collection) && ((Collection)dirtyWeb).isEmpty())
		    return true;
		if (dirtyMod instanceof Collection && ((Collection)dirtyMod).isEmpty() && dirtyWeb == null)
		    return true;
		if (dirtyMod == null || dirtyWeb == null) return false;

		if (dirtyMod instanceof JSONObject && dirtyWeb instanceof JSONObject) {
			return compareJSONObject((JSONObject) dirtyMod, (JSONObject) dirtyWeb);
		} else if (dirtyMod instanceof JSONArray && dirtyWeb instanceof JSONArray) {
			return compareJSONArray((JSONArray) dirtyMod, (JSONArray) dirtyWeb);
		} else if (dirtyMod instanceof Number && dirtyWeb instanceof Number) {
			BigDecimal decimalMod = new BigDecimal(dirtyMod.toString());
			BigDecimal decimalWeb = new BigDecimal(dirtyWeb.toString());
			return decimalMod.compareTo(decimalWeb) == 0;
		} else {
			return dirtyMod.equals(dirtyWeb);
		}
	}

	/**
	 * This method compares two JSONObjects; sanitizes at this level; recurses
	 *
	 * @param dirtyMod a particular element json (not sanitized)
	 * @param dirtyWeb a particular element json (not sanitized)
	 * @return boolean if web and model are equivalent
	 */
	private static boolean compareJSONObject(JSONObject mod, JSONObject web) {		
		Set<String> keys = new HashSet<String>();
		keys.addAll((Set<String>)mod.keySet());
		keys.addAll((Set<String>)web.keySet());
		// checking the values is more involved
		for (Object key : keys) {
			Object modVal = mod.get(key);
			Object webVal = web.get(key);
			if (!compare(modVal, webVal)) {
				return false;
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
	private static boolean compareJSONArray(JSONArray mod, JSONArray web) {		
		//JSONArray mod = sanitizeJSONArray(dirtyMod);
		//JSONArray web = sanitizeJSONArray(dirtyWeb);
		
		if (mod.size() != web.size()) 
			return false;
		if (mod.equals(web)) return true;

		for (int i = 0; i < mod.size(); i++) {
			Object modItem = mod.get(i);
			Object webItem = web.get(i);
			if (!compare(modItem, webItem))
				return false;
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
		System.out.println(json.toJSONString());
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
		System.out.println(result.toJSONString());
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

}
