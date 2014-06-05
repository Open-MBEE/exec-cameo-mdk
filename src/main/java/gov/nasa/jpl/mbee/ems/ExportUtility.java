/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.ems.validation.PropertyValueType;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.ci.persistence.versioning.IVersionDescriptor;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.magicdraw.teamwork2.ProjectVersion;
import com.nomagic.magicdraw.teamwork2.TeamworkService;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.StringExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralNull;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Operation;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Parameter;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.Duration;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationInterval;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.Interval;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeInterval;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ExportUtility {
	public static Logger log = Logger.getLogger(ExportUtility.class);
	public static Map<String, Integer> mountedVersions;
	private static String developerUrl = "https://sheldon.jpl.nasa.gov";
	private static String developerSite = "europa";
	public static boolean baselineNotSet = false;

	public static Set<String> ignoreSlots = new HashSet<String>(Arrays.asList(
			"_17_0_2_3_e9f034d_1375396269655_665865_29411", //stylesaver
			"_17_0_2_2_ff3038a_1358222938684_513628_2513",  //integrity
			"_17_0_2_2_ff3038a_1358666613056_344763_2540",  //integrity
			"_17_0_2_3_407019f_1383165366792_59388_29094", //mms
			"_17_0_2_3_407019f_1389652520710_658839_29078", //mms
			"_17_0_2_3_407019f_1391466672868_698092_29164", //mms
			"_be00301_1073306188629_537791_2", //diagraminfo
			"_be00301_1077726770128_871366_1", //diagraminfo
			"_be00301_1073394345322_922552_1", //diagraminfo
			"_16_8beta_8ca0285_1257244649124_794756_344"));  //diagraminfo

	public static String getBaselineTag() {
		Element model = Application.getInstance().getProject().getModel();
		String tag = null;
		if (StereotypesHelper.hasStereotype(model, "ModelManagementSystem")) {
			tag = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem",
					"baselineTag");
			if (tag == null || tag.equals("")) {
				baselineNotSet = true;
				//JOptionPane
				//       .showMessageDialog(null,
				//              "Your project root element doesn't have ModelManagementSystem baselineTag stereotype property set! Mount structure check will not be done!");
				return null;
			}
		} else {
			//JOptionPane
			//       .showMessageDialog(null,
			//              "Your project root element doesn't have ModelManagementSystem baselineTag stereotype property set! Mount structure check will not be done!");
			baselineNotSet = true;
			return null;
		}
		baselineNotSet = false;
		return tag;
	}

	public static String getElementID(Element e) {
		if (e instanceof Slot) {
			return e.getOwner().getID() + "-slot-" + ((Slot)e).getDefiningFeature().getID();
		}
		return e.getID();
	}

	public static Element getElementFromID(String id) {
		Project prj = Application.getInstance().getProject();
		String[] ids = id.split("-slot-");
		if (ids.length < 2) {
			return (Element)prj.getElementByID(ids[0]);
		} else {
			Element instancespec = (Element)prj.getElementByID(ids[0]);
			Element definingFeature = (Element)prj.getElementByID(ids[1]);
			if (instancespec != null && definingFeature != null && instancespec instanceof InstanceSpecification) {
				for (Element e: ((InstanceSpecification)instancespec).getOwnedElement()) {
					if (e instanceof Slot && ((Slot)e).getDefiningFeature() == definingFeature)
						return e;
				}
			} else
				return null;
		}
		return null;
	}

	public static String getUrl() {
		if ( false ) {
			return "http://localhost:8080/alfresco/service";
			//          //return "https://sheldon/alfresco/service";
		}
		String url = null;
		Element model = Application.getInstance().getProject().getModel();
		if (StereotypesHelper.hasStereotype(model, "ModelManagementSystem")) {
			url = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem",
					"MMS URL");
			if (url == null || url.equals("")) {
				JOptionPane
				.showMessageDialog(null,
						"Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
				url = null;
			}
		} else {
			JOptionPane
			.showMessageDialog(null,
					"Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
			url = null;
		}
		if (url == null && MDUtils.isDeveloperMode()) {
			url = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the editor URL:", developerUrl);  
		}
		if (url == null || url.equals(""))
			return null;
		developerUrl = url;
		url += "/alfresco/service";
		return url;
	}

	public static String getUrlWithSite() {
		Element model = Application.getInstance().getProject().getModel();
		String  url = getUrl();
		if (url == null)
			return null;
		String site = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem", "MMS Site");
		if (site == null || site.equals("")) {
			JOptionPane.showMessageDialog(null,
					"Your project root element doesn't have ModelManagementSystem MMS Site stereotype property set!");
			site = null;
		}
		if (site == null && MDUtils.isDeveloperMode()) {
			site = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the site:", developerSite);
		}
		if (site == null || site.equals(""))
			return null;
		developerSite = site;
		//do switch here
		return url + "/javawebscripts/sites/" + site;

		//return url + "/javawebscripts/sites/europa";
	}

	public static String getUrlWithSiteAndProject() {
		String url = getUrlWithSite();
		if (url != null)
			return url + "/projects/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
		return null;
	}

	public static String getPostElementsUrl() {
		String url = getUrlWithSiteAndProject();
		if (url == null)
			return null;
		return url + "/elements";
	}

	public static boolean showErrors(int code, String response, boolean showPopupErrors) {
		if (code != 200) {
			if (code == 500) {
				if (showPopupErrors)
					Utils.showPopupMessage("Server Error, see message window for details");
				Application.getInstance().getGUILog().log(response);
			} else if (code == 401) {
				if (showPopupErrors)
					Utils.showPopupMessage("You are not authorized or don't have permission, (you can login and try again).");
				else
					Application.getInstance().getGUILog().log("You are not authorized or don't have permission, (you can login and try again).");
				ViewEditUtils.clearCredentials();
			} else if (code == 403) {
				if (showPopupErrors)
					Utils.showPopupMessage("You do not have permission to do this");
				else
					Application.getInstance().getGUILog().log("You do not have permission to do this");
			} else if (code == 404) {
				if (showPopupErrors)
					Utils.showPopupMessage("The thing you're trying to validate or get wasn't found on the server, see validation window");
				else
					Application.getInstance().getGUILog().log("The thing you're trying to validate or get wasn't found on the server, see validation window");
			} else if (code == 400) {
				Application.getInstance().getGUILog().log(response);
				log.info(response);
				return false;
			} else {
				Application.getInstance().getGUILog().log(response);
			}
			log.info(response);
			return true;
		}
		if (response.length() > 3000) {
			//System.out.println(response);
			log.info(response);
			Application.getInstance().getGUILog().log("see md.log for what got received - too big to show");
		} else {
			log.info(response);
			//Application.getInstance().getGUILog().log(response);
		}
		return false;
	}

	public static boolean send(String url, String json, String method) {
		return send(url, json, method, true);
	}

	public static boolean send(String url, String json, String method, boolean showPopupErrors) {
		if (url == null)
			return false;

		EntityEnclosingMethod pm = null;
		if (method == null)
			pm = new PostMethod(url);
		else
			pm = new PutMethod(url);
		GUILog gl = Application.getInstance().getGUILog();
		try {
			gl.log("[INFO] Sending...");
			if (json.length() > 3000) {
				//System.out.println(json);
				log.info(json);
				gl.log("(see md.log for what got sent - too big to show)");
			} else
				log.info(json);//gl.log(json);
			pm.setRequestHeader("Content-Type", "application/json;charset=utf-8");
			pm.setRequestEntity(JsonRequestEntity.create(json));
			HttpClient client = new HttpClient();
			ViewEditUtils.setCredentials(client, url);
			int code = client.executeMethod(pm);
			String response = pm.getResponseBodyAsString();
			if (showErrors(code, response, showPopupErrors)) {
				return false;
			}
			gl.log("[INFO] Successful.");
			return true;
		} catch (Exception ex) {
			Utils.printException(ex);
			return false;
		} finally {
			pm.releaseConnection();
		}
	}

	public static boolean send(String url, String json) {
		return send(url, json, null);
	}

	@SuppressWarnings("unchecked")
	public static JSONArray formatView2View(JSONObject vv) {
		JSONArray response = new JSONArray();
		for (Object viewid: vv.keySet()) {
			JSONArray children = (JSONArray)vv.get(viewid);
			JSONObject viewinfo = new JSONObject();
			viewinfo.put("id", viewid);
			viewinfo.put("childrenViews", children);
			response.add(viewinfo);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject keyView2View(JSONArray vv) {
		JSONObject response = new JSONObject();
		for (Object viewinfo: vv) {
			String id = (String)((JSONObject)viewinfo).get("id");
			JSONArray children = (JSONArray)((JSONObject)viewinfo).get("childrenViews");
			response.put(id, children);
		}
		return response;
	}

	public static String get(String url) {
		return get(url, true);
	}

	public static String get(String url, boolean showPopupErrors) {
		if (url == null)
			return null;
		GetMethod gm = new GetMethod(url);
		try {
			HttpClient client = new HttpClient();
			ViewEditUtils.setCredentials(client, url);
			Application.getInstance().getGUILog().log("[INFO] Getting...");
			Application.getInstance().getGUILog().log("url=" + url);

			int code = client.executeMethod(gm);
			String json = gm.getResponseBodyAsString();

			if (showErrors(code, json, showPopupErrors)) {
				return null;
			}
			Application.getInstance().getGUILog().log("[INFO] Successful...");
			return json;
		} catch (Exception ex) {
			Utils.printException(ex);
		} finally {
			gm.releaseConnection();
		}
		return null;
	}

	public static boolean isElementDocumentation(Comment c) {
		if (c.getAnnotatedElement().size() > 1 || c.getAnnotatedElement().isEmpty())
			return false;
		if (c.getAnnotatedElement().iterator().next() == c.getOwner())
			return true;
		return false;
	}

	@SuppressWarnings( "unchecked" )
	public static void fillValueSpecification(ValueSpecification vs, JSONObject elementInfo, Stereotype view, Stereotype viewpoint) {
		ValueSpecification expr = vs.getExpression();
		if ( expr != null ) {
			elementInfo.put( "valueExpression", expr.getID() );
		}
		if ( vs instanceof Duration ) {
			elementInfo.put("type", "Duration");
		} 
		else if (vs instanceof DurationInterval) {
			elementInfo.put("type", "DurationInterval");
			Duration maxD = ((DurationInterval)vs ).getMax();
			if ( maxD != null ) elementInfo.put( "durationMax", maxD.getID() );
			Duration minD = ((DurationInterval)vs ).getMin();
			if ( minD != null ) elementInfo.put( "durationMin", minD.getID() );
		} 
		else if (vs instanceof ElementValue) {
			elementInfo.put("type", "ElementValue");
			Element elem = ((ElementValue)vs).getElement();
			if ( elem != null ) {
				elementInfo.put( "elementValueOfElement", elem.getID() );
			}
		} 
		else if (vs instanceof Expression) {
			elementInfo.put("type", "Expression");
			if ( ((Expression)vs).getSymbol() != null ) {
				elementInfo.put("symbol", ((Expression)vs).getSymbol());
			}
			List<ValueSpecification> vsl = ((Expression)vs).getOperand();
			if (vsl != null && vsl.size() > 0) {
				elementInfo.put( "operand", makeJsonArrayOfIDs( vsl ) );
			}
		} 
		else if (vs instanceof InstanceValue) {
			elementInfo.put("type", "InstanceValue");
			InstanceValue iv = (InstanceValue)vs;
			InstanceSpecification i = iv.getInstance();
			if ( i != null ) {
				elementInfo.put( "instance", i.getID() );
			}
		} 
		else if (vs instanceof Interval) {
			elementInfo.put("type", "Interval");
		}
		else if (vs instanceof LiteralSpecification) {
			if (vs instanceof LiteralBoolean) {
				elementInfo.put("type", "LiteralBoolean");
				elementInfo.put( "boolean", ((LiteralBoolean)vs ).isValue() );
			}
			else if (vs instanceof LiteralInteger) {
				elementInfo.put("type", "LiteralInteger");
				elementInfo.put( "integer", ((LiteralInteger)vs ).getValue() );
			}
			else if (vs instanceof LiteralNull) {
				elementInfo.put("type", "LiteralNull");
			}
			else if (vs instanceof LiteralReal) {
				elementInfo.put("type", "LiteralReal");
				elementInfo.put( "double", ((LiteralReal)vs ).getValue() );
			}
			else if (vs instanceof LiteralString) {
				elementInfo.put("type", "LiteralString");
				elementInfo.put( "string", ((LiteralString)vs ).getValue() );
			} 
			else if (vs instanceof LiteralUnlimitedNatural) {
				elementInfo.put("type", "LiteralUnlimitedNatural");
				elementInfo.put( "naturalValue", ((LiteralUnlimitedNatural)vs ).getValue() );
			}
		}
		else if (vs instanceof OpaqueExpression) {
			elementInfo.put("type", "OpaqueExpression");
			List<String> body = ((OpaqueExpression)vs).getBody();
			if ( body != null && body.size() > 0 ) {
				elementInfo.put("expressionBody", makeJsonArray( body ) );
			}
		} 
		else if (vs instanceof StringExpression) {
			elementInfo.put("type", "StringExpression");
		} 
		else if (vs instanceof TimeExpression) {
			elementInfo.put("type", "TimeExpression");
		} 
		else if (vs instanceof TimeInterval) {
			elementInfo.put("type", "TimeInterval");
			TimeExpression maxD = ((TimeInterval)vs).getMax();
			if ( maxD != null ) elementInfo.put( "timeIntervalMax", maxD.getID() );
			TimeExpression minD = ((TimeInterval)vs).getMin();
			if ( minD != null ) elementInfo.put( "timeIntervalMin", minD.getID() );
		}

	}

	@SuppressWarnings( "unchecked" )
	protected static void add( JSONArray arr, Object o ) {
		arr.add( o );
	}

	protected static < T extends MDObject > JSONArray makeJsonArrayOfIDs( Collection< T > collection ) {
		JSONArray ids = new JSONArray();
		for ( T t : collection ) {
			if ( t != null ) add( ids, t.getID() );
		}
		return ids;
	}
	protected static < T > JSONArray makeJsonArray( Collection< T > collection ) {
		JSONArray arr = new JSONArray();
		for ( T t : collection ) {
			if ( t != null ) add( arr, t );
		}
		return arr;
	}

	@SuppressWarnings("unchecked")
	public static void fillElement(Element e, JSONObject elementInfo, Stereotype view, Stereotype viewpoint) {
		JSONObject specialization = new JSONObject();
		elementInfo.put("specialization", specialization);

		if (e instanceof Package) {
			specialization.put("type", "Package");
		} 
		else if (e instanceof Property) {
			specialization.put("type", "Property");
			specialization.put("isDerived", ((Property)e).isDerived());
			specialization.put("isSlot", false);
			ValueSpecification vs = ((Property)e).getDefaultValue();
			if (vs != null) {
				//Create a new JSONObject and a new JSONArray. Fill in
				//the values to the new JSONObject and then insert
				//that JSONObject into the array (NOTE: there will
				//be single element in this array). Finally, insert
				//the array into the specialization element as the
				//value of the "value" property.
				//
				JSONArray singleElementSpecVsArray = new JSONArray();
				JSONObject newElement = new JSONObject();
				fillValueSpecification(vs, newElement, view, viewpoint);
				singleElementSpecVsArray.add(newElement);
				specialization.put( "value", singleElementSpecVsArray );
			}
			Type type = ((Property)e).getType();
			if (type != null) {
				specialization.put("propertyType", "" + type.getID());
			} 
			else
				specialization.put("propertyType", "null");
		} 
		else if (e instanceof Slot) {
			specialization.put("type", "Property");
			specialization.put("isDerived", false);
			specialization.put("isSlot", true);
			if (((Slot)e).getDefiningFeature().getID().equals("_17_0_2_3_e9f034d_1375396269655_665865_29411"))
				specialization.put("stylesaver", true);
			
			//Retrieve a list of ValueSpecification objects.
			//Loop through these objects, creating a new JSONObject 
			//for each value spec. Fill in the new JSONObject and
			//insert them into a new JSONArray.
			//Finally, once you've looped through all the value
			//specifications, insert the JSONArray into the 
			//new specialization element.
			//
			List<ValueSpecification> vsl = ((Slot)e).getValue();
			if (vsl != null && vsl.size() > 0) {
				JSONArray specVsArray = new JSONArray();
				for (ValueSpecification vs: vsl) {
					JSONObject newElement = new JSONObject();
					fillValueSpecification(vs, newElement, view, viewpoint);
					specVsArray.add(newElement);
				}
				specialization.put( "value", specVsArray);
			}
			Element type = ((Slot)e).getDefiningFeature();
			if (type != null) {
				specialization.put("propertyType", "" + type.getID());
			}
		} 
		else if (e instanceof Dependency) {
			if (StereotypesHelper.hasStereotypeOrDerived( e, DocGen3Profile.conformStereotype))//(e, Utils.getConformsStereotype()))
				specialization.put("type", "Conform");
			else if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.queriesStereotype))
				specialization.put("type", "Expose");
			else
				specialization.put("type", "Dependency");
		} 
		else if (e instanceof Generalization) {
			boolean isConform = StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.conformStereotype);//(e, Utils.getConformsStereotype()))
			if (isConform)
				specialization.put("type", "Conform");
			else if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.queriesStereotype))
				specialization.put("type", "Expose");
			else
				specialization.put("type", "Generalization");
		} 
		else if (e instanceof DirectedRelationship) {   
			specialization.put("type", "DirectedRelationship");
		} 
		else if (e instanceof Comment) {
			specialization.put("type", "Comment");
			specialization.put("body", Utils.stripHtmlWrapper(((Comment)e).getBody()));
			specialization.put("annotatedElements",
					makeJsonArrayOfIDs( ((Comment)e).getAnnotatedElement() ));
		} 
		else if (e instanceof Connector) {
			specialization.put("type", "Connector");
			Connector c = (Connector)e;
			List< ConnectorEnd > ends = c.getEnd();
			ArrayList<Element> roles = new ArrayList< Element >();
			for ( ConnectorEnd end : ends ) {
				if ( end.getRole() != null ) {
					roles.add( end.getRole() );
				}
			}
			JSONArray ids = makeJsonArrayOfIDs( roles );
			specialization.put("connectorRole", ids);
		} 
		else if (e instanceof Operation) {
			specialization.put("type", "Operation");
			List<Parameter> vsl = ((Operation)e).getOwnedParameter();
			if (vsl != null && vsl.size() > 0) {

				specialization.put("operationParameter", makeJsonArrayOfIDs( vsl ));
			}
		} 
		else if (e instanceof Constraint) {
			specialization.put( "type", "Constraint" );
			ValueSpecification spec = ((Constraint)e).getSpecification();
			if ( spec != null ) 
				specialization.put("constraintSpecification", spec.getID() );
		} 
		else if (e instanceof InstanceSpecification) {
			specialization.put( "type", "InstanceSpecification" );

			ValueSpecification spec = ((InstanceSpecification)e).getSpecification();
			if ( spec != null ) 
				specialization.put("instanceSpecificationSpecification", spec.getID() );

		} 
		else if (e instanceof Parameter) {
			Parameter p = (Parameter)e;
			specialization.put( "type", "Element" );
			if ( p.getDirection() != null ) specialization.put( "parameterDirection", p.getDirection() );
			if ( p.getType() != null ) specialization.put( "parameterType", p.getType().getID() );
			ValueSpecification defaultValue = p.getDefaultValue();
			if ( defaultValue != null ) {
				specialization.put( "parameterDefaultValue", defaultValue.getID() );
			}
		} 
		else {
			elementInfo.put("type", "Element");
		}
		if (e instanceof DirectedRelationship) {
			Element client = ModelHelper.getClientElement(e);
			Element supplier = ModelHelper.getSupplierElement(e);
			specialization.put("source", client.getID());
			specialization.put("target", supplier.getID());
		}
		if (viewpoint != null && StereotypesHelper.hasStereotypeOrDerived(e, viewpoint))
			specialization.put("type", "Viewpoint");
		
		if (e instanceof NamedElement) {
			elementInfo.put("name", ((NamedElement)e).getName());
		} 
		else
			elementInfo.put("name", "");
		
		elementInfo.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(e)));
		if (e.getOwner() == null || e.getOwner() == Application.getInstance().getProject().getModel())
			elementInfo.put("owner", "null");
		else
			elementInfo.put("owner", "" + e.getOwner().getID());
		elementInfo.put("id", getElementID(e));
	}

	// TODO -- Can DocGenUtils.getLiteralValue() be used to simplify this; maybe
	// create a Class<?> getValueClass( ValueSpecification) that gets the
	// valueType.  Then this would be three lines (forgetting error checking):
	//   elementInfo.put( "valueType", getValueClass( vs ).getSimpleName() );
	//   value.add( DocGenUtils.getLiteralValue( vs ) );
	//   elementInfo.put( "value", value );
	@SuppressWarnings("unchecked")
	public static void addValues(Element e, JSONArray value, JSONObject elementInfo, ValueSpecification vs) {
		if (vs instanceof LiteralBoolean) {
			elementInfo.put("valueType", PropertyValueType.LiteralBoolean.toString());
			value.add(((LiteralBoolean)vs).isValue());
		} else if (vs instanceof LiteralString) {
			elementInfo.put("valueType", PropertyValueType.LiteralString.toString());
			value.add(Utils.stripHtmlWrapper(((LiteralString)vs).getValue()));
		} else if (vs instanceof LiteralInteger || vs instanceof LiteralUnlimitedNatural) {
			elementInfo.put("valueType", PropertyValueType.LiteralInteger.toString());
			if (vs instanceof LiteralInteger) {
				value.add(((LiteralInteger)vs).getValue());
			} else 
				value.add(((LiteralUnlimitedNatural)vs).getValue());
		} else if (vs instanceof LiteralReal) {
			elementInfo.put("valueType", PropertyValueType.LiteralReal.toString());
			value.add(((LiteralReal)vs).getValue());
		} else if (vs instanceof Expression) {
			elementInfo.put("valueType", PropertyValueType.Expression.toString());
			value.add(RepresentationTextCreator.getRepresentedText(vs));
		} else if (vs instanceof ElementValue) {
			elementInfo.put("valueType", PropertyValueType.ElementValue.toString());
			Element ev = ((ElementValue)vs).getElement();
			if (ev != null) {
				value.add(ev.getID());
			}
		} else if (vs instanceof InstanceValue) {
			elementInfo.put("valueType", PropertyValueType.ElementValue.toString());
			Element ev = ((InstanceValue)vs).getInstance();
			if (ev != null) {
				value.add(ExportUtility.getElementID(ev));
			}
		}
		elementInfo.put("value", value);
	}

	public static boolean checkBaselineMount() {
		Project prj = Application.getInstance().getProject();
		if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
			String baselineTag = getBaselineTag();
			if (baselineTag == null)
				return true;
			List<String> tags = ProjectUtilities.getVersionTags(prj.getPrimaryProject());
			if (!tags.contains(baselineTag)) {
				Application.getInstance().getGUILog().log("The current project is not an approved baseline version!");
				return false;
			}

			for (IAttachedProject proj: ProjectUtilities.getAllAttachedProjects(prj)) {
				if (ProjectUtilities.isFromTeamworkServer(proj)) {
					List<String> tags2 = ProjectUtilities.getVersionTags(proj);
					if (!tags2.contains(baselineTag)) {
						Application.getInstance().getGUILog().log(proj.getName() + " is not an approved baseline module version!");
						return false;
					}
				}
			}
		} else
			baselineNotSet = false;
		return true;
	}

	public static boolean checkBaseline() {
		if (!ExportUtility.checkBaselineMount()) {
			Boolean con = Utils.getUserYesNoAnswer("Mount structure check did not pass (your project or mounts are not baseline versions)! Do you want to continue?");
			//Utils.showPopupMessage("Your project isn't the baseline/isn't mounting the baseline versions, or the check cannot be completed");
			if (con == null || !con)
				return false;
		}
		return true;
	}

	public static Integer getAlfrescoProjectVersion(String projectId) {
		String baseUrl = getUrl();
		String checkProjUrl = baseUrl + "/javawebscripts/projects/" + projectId;
		String json = get(checkProjUrl, false);
		if (json == null)
			return null; //??
		JSONObject result = (JSONObject)JSONValue.parse(json);
		if (result.containsKey("projectVersion"))
			return Integer.valueOf(result.get("projectVersion").toString());
		return null;
	}

	public static void sendProjectVersion(Element e) {
		Project prj = Application.getInstance().getProject();
		if (ProjectUtilities.isElementInAttachedProject(e)) {
			IProject module = ProjectUtilities.getAttachedProject(e);
			if (ProjectUtilities.isFromTeamworkServer(module)) {
				IVersionDescriptor vd = ProjectUtilities.getVersion(module);
				ProjectVersion pv = new ProjectVersion(vd);
				Integer teamwork = pv.getNumber();
				sendProjectVersion(module.getProjectID(), teamwork);
			}
		} else {
			if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
				sendProjectVersion(prj.getPrimaryProject().getProjectID(),TeamworkService.getInstance(prj).getVersion(prj).getNumber());
			}
		}

	}

	public static boolean okToExport(Element e) {
		if (mountedVersions == null)
			mountedVersions = new HashMap<String, Integer>();
		Project prj = Application.getInstance().getProject();
		if (ProjectUtilities.isElementInAttachedProject(e)) {
			IAttachedProject module = ProjectUtilities.getAttachedProject(e);
			if (ProjectUtilities.isFromTeamworkServer(module)) {
				IVersionDescriptor vd = ProjectUtilities.getVersion(module);
				ProjectVersion pv = new ProjectVersion(vd);
				Integer teamwork = pv.getNumber();
				//Integer teamwork = TeamworkService.getInstance(prj).getVersion(modulePrj).getNumber();
				Integer mms = getAlfrescoProjectVersion(module.getProjectID());
				if (teamwork == mms || mms == null || teamwork >= mms)
					return true;
				Boolean con = Utils.getUserYesNoAnswer("The element is in project " + module.getName() + " (" + teamwork + 
						") that is an older version of what's on the server (" + mms + "), do you want to continue export?");
				if (con == null || !con)
					return false;
			}
			return true;
		} else {
			if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
				Integer teamwork = TeamworkService.getInstance(prj).getVersion(prj).getNumber();
				Integer mms = getAlfrescoProjectVersion(prj.getPrimaryProject().getProjectID());
				if (teamwork == mms || mms == null || teamwork >= mms)
					return true;
				Boolean con = Utils.getUserYesNoAnswer("The element is in project " + prj.getName() + " (" + teamwork + 
						") that is an older version of what's on the server (" + mms + "), do you want to continue export?");
				if (con == null || !con)
					return false;
			}
			return true;
		}        
	}

	public static boolean okToExport(Set<Element> set) {
		Project prj = Application.getInstance().getProject();
		mountedVersions = new HashMap<String, Integer>();
		Map<String, String> projectNames = new HashMap<String, String>();
		if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
			mountedVersions.put(prj.getPrimaryProject().getProjectID(), TeamworkService.getInstance(prj).getVersion(prj).getNumber());
			projectNames.put(prj.getPrimaryProject().getProjectID(), prj.getName());
		}
		for (Element e: set) {
			if (ProjectUtilities.isElementInAttachedProject(e)) {
				IProject module = ProjectUtilities.getAttachedProject(e);
				if (ProjectUtilities.isFromTeamworkServer(module) && !mountedVersions.containsKey(module.getProjectID())) {
					IVersionDescriptor vd = ProjectUtilities.getVersion(module);
					ProjectVersion pv = new ProjectVersion(vd);
					Integer teamwork = pv.getNumber();
					mountedVersions.put(module.getProjectID(), teamwork);
					projectNames.put(module.getProjectID(), module.getName());
				}
			}
		}
		for (String prjId: mountedVersions.keySet()) {
			Integer serverVersion = getAlfrescoProjectVersion(prjId);
			if (serverVersion != null && serverVersion > mountedVersions.get(prjId)) {
				Boolean con = Utils.getUserYesNoAnswer("Your project " + projectNames.get(prjId) + 
						" is an older project version (" + mountedVersions.get(prjId) + 
								") than what's on the server (" + serverVersion + ") , do you want to continue?");
				if (con == null || !con)
					return false;
			}
		}
		return true;
	}

	public static boolean okToExport() {
		mountedVersions = new HashMap<String, Integer>();
		Map<String, String> projectNames = new HashMap<String, String>();
		Project prj = Application.getInstance().getProject();
		if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
			mountedVersions.put(prj.getPrimaryProject().getProjectID(), TeamworkService.getInstance(prj).getVersion(prj).getNumber());
			projectNames.put(prj.getPrimaryProject().getProjectID(), prj.getName());
		}
		for (IAttachedProject p: ProjectUtilities.getAllAttachedProjects(prj)) {
			if (ProjectUtilities.isFromTeamworkServer(p)) {
				IVersionDescriptor vd = ProjectUtilities.getVersion(p);
				ProjectVersion pv = new ProjectVersion(vd);
				Integer teamwork = pv.getNumber();
				mountedVersions.put(p.getProjectID(), teamwork);
				projectNames.put(p.getProjectID(), p.getName());
			}
		}
		for (String prjId: mountedVersions.keySet()) {
			Integer serverVersion = getAlfrescoProjectVersion(prjId);
			if (serverVersion != null && serverVersion > mountedVersions.get(prjId)) {
				Boolean con = Utils.getUserYesNoAnswer("Your project " + projectNames.get(prjId) + 
						" is an older project version (" + mountedVersions.get(prjId) + 
								") than what's on the server (" + serverVersion + ") , do you want to continue?");
				if (con == null || !con)
					return false;
			}
		}
		return true;
	}

	public static Map<String, Integer> getMountedVersions() {
		return mountedVersions;
	}

	public static void sendProjectVersion(String projId, Integer version) {
		String baseurl = getUrl();
		if (baseurl == null)
			return;
		String url = baseurl + "/javawebscripts/projects/" + projId + "?fix=true";
		JSONObject tosend = new JSONObject();
		tosend.put("projectVersion", version.toString());
		send(url, tosend.toJSONString(), null, false);
	}

	public static void sendProjectVersions() {
		for (String projid: mountedVersions.keySet()) {
			sendProjectVersion(projid, mountedVersions.get(projid));
		}
	}

	public static String unescapeHtml(String s) {
		return StringEscapeUtils.unescapeHtml(s);
	}

	public static boolean shouldAdd(Element e) {
		if (e instanceof ValueSpecification || e instanceof Extension || e instanceof ProfileApplication)
			return false;
		if (e instanceof Comment && ExportUtility.isElementDocumentation((Comment)e)) 
			return false;
		if (e instanceof InstanceSpecification && e.getOwnedElement().isEmpty() && !(e instanceof EnumerationLiteral))
			return false;
		if (e instanceof Slot && ExportUtility.ignoreSlots.contains(((Slot)e).getDefiningFeature().getID()))
			return false;
		return true;
	}

	/**
	 * Add nested Expression elements to the set recursively.
	 * @param set
	 */
	protected static void getNestedValueElements( Set< Element > set ) {
		LinkedList<Element> queue = new LinkedList< Element >( set );
		while ( !queue.isEmpty() ) {
			Element e = queue.pollFirst();
			Element value = null;
			HashSet<Element> moreElements = new HashSet< Element >();
			if ( e instanceof Expression ) {
				moreElements.addAll( ((Expression)e).getOperand() );
			} else if ( e instanceof Slot ) {
				moreElements.addAll( ((Slot)e).getValue() );
			} else {
				if (e instanceof Property) {
					ValueSpecification propVal = ((Property)e).getDefaultValue();
					value = propVal;
				} else if (e instanceof ElementValue) {
					value = ((ElementValue)e).getElement();
				} else if (e instanceof InstanceValue) {
					value = ((InstanceValue)e).getInstance();
				}
				if ( value != null ) {
					moreElements.add( value );
				}
			}
			for ( Element ee : moreElements ) {
				if ( !set.contains( ee ) ) {
					set.add( ee );
					queue.add( ee );
				}
			}
		}
	}

	//    public static Map<String, JSONObject> getReferencedElements(Element e) {
	//        return getReferencedElements( e, true );
	//    }
	public static Map<String, JSONObject> getReferencedElements(Element e ) {
		//                                                                boolean justValues) {
		Stereotype view = Utils.getViewStereotype();
		Stereotype viewpoint = Utils.getViewpointStereotype();
		Map<String, JSONObject> result = new HashMap<String, JSONObject>();
		HashSet< Element > values = new HashSet< Element >();
		values.add( e );
		getNestedValueElements( values );
		for ( Element value : values ) {
			if ( value != null ) {//&& !result.containsKey( value.getID() ) ) {
				JSONObject j = new JSONObject();
				fillElement( value, j, view, viewpoint );
				result.put( value.getID(), j );
			}
		}
		values.remove( e );
		return result;
	}

	public static final Pattern HTML_WHITESPACE_END = Pattern.compile("\\s*</p>", Pattern.DOTALL);
	public static final Pattern HTML_WHITESPACE_START = Pattern.compile("<p>\\s*", Pattern.DOTALL);
	public static String cleanHtml(String s) {
		return Utils.stripHtmlWrapper(s).replace(" class=\"pwrapper\"", "").replace("<br>", "").replace("</br>", "");//.replace("\n", "");
		//inter = HTML_WHITESPACE_END.matcher(inter).replaceAll("</p>");
		//return HTML_WHITESPACE_START.matcher(inter).replaceAll("<p>");
	}

}
