package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import javax.swing.JOptionPane;

public class ViewEditUtils {
	private static final String DEFAULT_EDITOR_URL = "http://mbee.jpl.nasa.gov/editor";
	private static String editorurl = null;
	public static String getUrl() {
		String chosen = editorurl == null ? DEFAULT_EDITOR_URL : editorurl;
		String url = JOptionPane.showInputDialog("Enter the editor URL:", chosen);
		if (url == null) {
			return url;
		}
		url = url.replaceAll("/+$", "");
		editorurl = url;
		return url;
	}
}
