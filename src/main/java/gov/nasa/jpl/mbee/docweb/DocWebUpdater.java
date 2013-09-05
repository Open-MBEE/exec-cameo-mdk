package gov.nasa.jpl.mbee.docweb;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

@SuppressWarnings("unchecked") // JSONObject should really support generics
/**
 * this is for updating category, tw module, tw document information on docweb from a model
 * it currently doesn't use magicdraw ids and does matching by name, which is prone to errors
 * i don't think this actually gets used that often since people just go through the web admin interface anyways
 * future docweb updates should fix this...
 * 
 * deprecated? too unrobust to use
 * @author dlam
 *
 */
public class DocWebUpdater {
	
	private JSONObject catIdName;
	private JSONObject projIdName;
	private JSONObject docIdName;
	private JSONObject cat2cat;
	private JSONObject doc2proj;
	private JSONObject doc2cat;
	private JSONObject cat2order;
	
	public DocWebUpdater() {
		catIdName = new JSONObject();
		projIdName = new JSONObject();
		docIdName = new JSONObject();
		cat2cat = new JSONObject();
		cat2order = new JSONObject();
		doc2cat = new JSONObject();
		doc2proj = new JSONObject();
	}
	
	private void fillStuff(Element cat, Element parent, List<Integer> order) {
		if (StereotypesHelper.hasStereotype(cat, TeamworkProfile.category)) { // REVIEW -- hasStereotypeOrDerived?
			cat2order.put(cat.getID(), join(order, "."));
			if (parent != null && StereotypesHelper.hasStereotype(parent, TeamworkProfile.category)) // REVIEW -- hasStereotypeOrDerived?
				cat2cat.put(cat.getID(), parent.getID());
			catIdName.put(cat.getID(), ((NamedElement)cat).getName());
			List<Element> twprojs = findCategoryProjects(cat);
			for (Element e: twprojs) {
				projIdName.put(e.getID(), ((NamedElement)e).getName());
				List<Element> docs = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e, TeamworkProfile.hasDocument, 1, true, 1);
				for (Element doc: docs) {
					docIdName.put(doc.getID(), ((NamedElement)doc).getName());
					doc2cat.put(doc.getID(), cat.getID());
					doc2proj.put(doc.getID(), e.getID());
				}
			}
		}
		List<Element> firsts = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cat, DocGen3Profile.firstStereotype, 1, false, 1);
		if (firsts.isEmpty())
			return;
		Element first = firsts.get(0);
		order.add(1);
		fillStuff(first, cat, order);
		List<Element> nexts = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(first, DocGen3Profile.nextStereotype, 1, false, 1);
		while(!nexts.isEmpty()) {
			Element next = nexts.get(0);
			order.set(order.size()-1, order.get(order.size()-1)+1);
			fillStuff(next, cat, order);
			nexts = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(next, DocGen3Profile.nextStereotype, 1, false, 1);
		}
		order.remove(order.size()-1);	
	}
	
	public static String join(List<Integer> s, String delimiter) {
		DecimalFormat format = new DecimalFormat("00");
    	StringBuilder builder = new StringBuilder();
    	Iterator<Integer> iter = s.iterator();
    	while (iter.hasNext()) {
    		builder.append(format.format(iter.next()));
    		if (!iter.hasNext())
    			break;
    		builder.append(delimiter);
    	}
    	return builder.toString();
    }
	
	//uses mdids
	public String publish2(Package root, String server, String project, String url) {
		JSONObject res = new JSONObject();
		fillStuff(root, null, new ArrayList<Integer>());
		res.put("catIdName", catIdName);
		res.put("projIdName", projIdName);
		res.put("docIdName", docIdName);
		res.put("cat2cat", cat2cat);
		res.put("doc2proj", doc2proj);
		res.put("doc2cat", doc2cat);
		res.put("cat2order", cat2order);
		res.put("Server", server);
		res.put("Project", project);
		String json = res.toJSONString();
		return json;
		//uncomment below when ready
		/*PostMethod pm = new PostMethod(url);
		try {
			pm.setRequestEntity(new StringRequestEntity(json));
			pm.setRequestHeader("Content-type", "text/plain");
			Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
			Protocol.registerProtocol("https", easyhttps);
			HttpClient client = new HttpClient();
			int code = client.executeMethod(pm);
			return pm.getResponseBodyAsString();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return "wrong url";
		} catch (IOException e1) {
			e1.printStackTrace();
			return "cannot connect";
		} finally{
			pm.releaseConnection();
		}*/
	}
	
	public static String publish(Package root, String server, String project, String url) {
	
		JSONObject res = new JSONObject();
		JSONObject catNameChange = new JSONObject();
		JSONObject projNameChange = new JSONObject();
		JSONObject docNameChange = new JSONObject();
		JSONObject cat2doc = new JSONObject();
		JSONObject proj2doc = new JSONObject();
		
		List<Element> all = Utils.collectOwnedElements(root, 0);
		Stereotype oldName = StereotypesHelper.getStereotype(Project.getProject(root), TeamworkProfile.oldName);
		List<Element> nameChanges = Utils.filterElementsByStereotype(all, oldName, true, true);
		for (Element e: nameChanges) {
			Element newname = ModelHelper.getClientElement(e);
			Element oldname = ModelHelper.getSupplierElement(e);
			if (StereotypesHelper.hasStereotypeOrDerived(newname, TeamworkProfile.twproject))
				projNameChange.put(((NamedElement)oldname).getName(), ((NamedElement)newname).getName());
			else if (StereotypesHelper.hasStereotypeOrDerived(newname, TeamworkProfile.category))
				catNameChange.put(((NamedElement)oldname).getName(), ((NamedElement)newname).getName());
			else if (StereotypesHelper.hasStereotypeOrDerived(newname, TeamworkProfile.document)) {
				JSONObject docdetail = new JSONObject();
				docdetail.put("name", ((NamedElement)newname).getName());
				List<Element> projs = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(newname, TeamworkProfile.hasDocument, 2, true, 1);
				if (!projs.isEmpty())
					docdetail.put("project", ((NamedElement)projs.get(0)).getName());
				docNameChange.put(((NamedElement)oldname).getName(), docdetail);
			}
		}
		
		Stereotype cats = StereotypesHelper.getStereotype(Project.getProject(root), TeamworkProfile.category);
		List<Element> categories = Utils.filterElementsByStereotype(all, cats, true, true);
		for (Element cat: categories) {
			JSONArray catdocs = new JSONArray();
			List<Element> catprojs = findCategoryProjects(cat);
			for (Element e: catprojs) {
				JSONArray projdocs = new JSONArray();
				List<Element> docs = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e, TeamworkProfile.hasDocument, 1, true, 1);
				for (Element doc: docs) {
					projdocs.add(((NamedElement)doc).getName());
					JSONObject docproj = new JSONObject();
					docproj.put("name", ((NamedElement)doc).getName());
					docproj.put("project", ((NamedElement)e).getName());
					catdocs.add(docproj);
				}
				proj2doc.put(((NamedElement)e).getName(), projdocs);
			}
			cat2doc.put(((NamedElement)cat).getName(), catdocs);
		}
		
		res.put("Category Name Changes", catNameChange);
		res.put("Project Name Changes", projNameChange);
		res.put("Document Name Changes", docNameChange);
		res.put("Category2Document", cat2doc);
		res.put("Project2Document", proj2doc);
		res.put("Server", server);
		res.put("Project", project);
		String json = res.toJSONString();
		
		HttpsUtils.allowSelfSignedCertificates();
		PostMethod pm = new PostMethod(url);
		try {
			pm.setRequestEntity(JsonRequestEntity.create(json));
			pm.setRequestHeader("Content-type", "text/plain");
			HttpClient client = new HttpClient();

			@SuppressWarnings("unused") // result is still useful for debugging
			int code = client.executeMethod(pm);

			return pm.getResponseBodyAsString();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return "wrong url";
		} catch (IOException e1) {
			e1.printStackTrace();
			return "cannot connect";
		} finally{
			pm.releaseConnection();
		}
		
	}
	
	private static List<Element> findCategoryProjects(Element cat) {
		List<Element> projs = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cat, TeamworkProfile.includesModule, 1, true, 1);
		List<Element> twcats = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cat, TeamworkProfile.includesMdCat, 1, true, 1);
		for (Element e: twcats) {
			List<Element> mdcatall = Utils.collectOwnedElements(e, 0);
			List<Element> mdcatproj = Utils.filterElementsByStereotypeString(mdcatall, TeamworkProfile.twproject, true, true);
			projs.addAll(mdcatproj);
		}
		return projs;
	}
}
