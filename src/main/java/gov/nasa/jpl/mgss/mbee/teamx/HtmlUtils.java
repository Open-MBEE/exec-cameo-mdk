package gov.nasa.jpl.mgss.mbee.teamx;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * I think these were used by some teamx stuff
 * @author dlam
 *
 */
public class HtmlUtils {

	public static Element getHtmlBody(String s) {
		return Jsoup.parse(s).body(); 
	}
	
	public static List<Element> getBodyChildren(Element e) {
		List<Element> res = new ArrayList<Element>();
		for (Element c: e.children())
			if (c.tagName().equals("p") || c.tagName().equals("ul") || c.tagName().equals("ol")) 
				res.add(c);
		return res;
	}
	
	public static List<Element> getListItems(Element list) {
		List<Element> res = new ArrayList<Element>();
		for (Element c: list.children())
			if (c.tagName().equals("li"))
				res.add(c);
		return res;
	}
	
	public static String getElementText(Element e) {
		if (e.hasText())
			return e.ownText();
		return "";
	}
}
