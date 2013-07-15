package gov.nasa.jpl.mbee.docweb;

import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONObject;

/**
 * Convenience class to specify JSON content-type and UTF-8 encoding,
 * without requiring the calling code to explicitly check for
 * UnsupportingEncodingException. If the JVM doesn't have UTF-8
 * encoding available, this is treated as an IllegalStateException.
 * <p>Example:</p>
 * <pre>
 * PostMethod m = new PostMethod(...);
 * m.setRequestEntity(JsonRequestEntity.create(json));
 * </pre>
 */
public class JsonRequestEntity extends StringRequestEntity {
	private JsonRequestEntity(String body) throws UnsupportedEncodingException {
		super(body, "application/json", "UTF-8");
	}

	public static JsonRequestEntity create(JSONObject json) {
		return create(json.toJSONString());
	}

	public static JsonRequestEntity create(String body) {
		try {
			return new JsonRequestEntity(body);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 encoding not available");
		}
	}
}
