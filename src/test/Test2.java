import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Test2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String blah = null;
		if (blah == null)
			System.out.println("null");
		if (null instanceof Integer)
			System.out.println("hmm");
		System.out.println(String.class.getSimpleName());
		
		String s = "<a href=\"mdel://_17_0_1_24c603f9_1326925305791_836200_16145\">blah asdf</a>";
		s = s.replaceAll("<a href=\"mdel://([^\"&^\\?]+)(\\?[^\"]*)?\">([^<]*)</a>", "<link linkend=\"$1\">$3</link>");
		System.out.println(s);
		
		s = "<a href=\"mdel://_17_0_1_24c603f9_1326925305791_836200_16145?projectID=ID_5_4_10_10_33_37_AM_73f784d2_12860399d62__5449_ako_ammos_jpl_nasa_gov_127_0_0_1&projectName=DocGen&elementName=View+DocGen3&metaType=Package&elementQName=DocGen+3+Documentation%3A%3AViewpointTest%3A%3ADocGen3\">blah asdf</a>";
		s = s.replaceAll("<a href=\"mdel://([^\"&^\\?]+)(\\?[^\"]*)?\">([^<]*)</a>", "<link linkend=\"$1\">$3</link>");
		System.out.println(s);
		
		s = "<a href=\"http://www.yahoo.com/asdf.jpl?asdfasd=2343\">blah asdf</a>";
		s = s.replaceAll("<a href=\"(http[^\"]+)\">([^<]*)</a>", "<link xlink:href=\"$1\">$2</link>");
		System.out.println(s);
		
		
		s = "<table class=\"informal\"";
		s = s.replaceAll("<table class=\"informal\"", "<informaltable");
		System.out.println(s);
		
		double a = 2123.1 / 0.0;
		System.out.println(a);
		
		System.out.println(new DecimalFormat("0").format(0.12));
		System.out.println(new DecimalFormat("#").format(112.12));
		
		Map<String, String> map = new HashMap<String, String>();
		
		if (map.get(null) == null)
			System.out.println("null can be passed as key!");
		
		s = "asdf &nbsp; asdf";
		s = s.replaceAll("&nbsp;", "&#160;");
		System.out.println(s);
		
		String html = "<html><body><table class=\"informal\"><tr><td></td></tr></table></body></html>";
		Document d = Jsoup.parse(html);
		Elements tables = d.select("table.informal");
		tables.tagName("informaltable");
		System.out.println(d.toString());
		System.out.println(new DecimalFormat("00").format(4));
		
		String html2 = "<html><body><p>              &#160;\n</p></body></html>";
		d = Jsoup.parse(html2);
		for (Element e: d.select("p")) {
			System.out.println(e.hasText());
			System.out.println(e.data());
			System.out.println(e.html());
			System.out.println(e.ownText());
			System.out.println(e.text());
		}
		
		System.out.println(new Date());
	}

}
