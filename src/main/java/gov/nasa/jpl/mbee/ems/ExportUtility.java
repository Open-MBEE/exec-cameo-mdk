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
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.nomagic.magicdraw.teamwork2.ProjectVersion;
import com.nomagic.magicdraw.teamwork2.TeamworkService;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ExportUtility {
    public static Logger log = Logger.getLogger(ExportUtility.class);
    public static Map<String, Integer> mountedVersions;
    
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
                JOptionPane
                        .showMessageDialog(null,
                                "Your project root element doesn't have ModelManagementSystem baselineTag stereotype property set! Mount structure check will not be done!");
                return null;
            }
        } else {
            JOptionPane
                    .showMessageDialog(null,
                            "Your project root element doesn't have ModelManagementSystem baselineTag stereotype property set! Mount structure check will not be done!");
            return null;
        }
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
        //return "https://sheldon/alfresco/service";
        String url = null;
        Element model = Application.getInstance().getProject().getModel();
        if (StereotypesHelper.hasStereotype(model, "ModelManagementSystem")) {
            url = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem",
                    "url");
            if (url == null || url.equals("")) {
                JOptionPane
                        .showMessageDialog(null,
                                "Your project root element doesn't have ModelManagementSystem url stereotype property set!");
                return null;
            }
        } else {
            JOptionPane
                    .showMessageDialog(null,
                            "Your project root element doesn't have ModelManagementSystem url stereotype property set!");
            return null;
        }
        return url;
    }
    
    public static String getUrlWithSite() {
        Element model = Application.getInstance().getProject().getModel();
        String  url = getUrl();
        if (url == null)
            return null;
        String site = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem", "site");
        if (site == null || site.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Your project root element doesn't have ModelManagementSystem site stereotype property set!");
                return null;
        }
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
                log.info(response);
            } else if (code == 401) {
                if (showPopupErrors)
                    Utils.showPopupMessage("You are not authorized or don't have permission, you have been logged out (you can login and try again)");
                ViewEditUtils.clearCredentials();
            } else if (code == 403) {
                if (showPopupErrors)
                    Utils.showPopupMessage("You do not have permission to do this");
            } else if (code == 404) {
                if (showPopupErrors)
                    Utils.showPopupMessage("The thing you're trying to validate or get wasn't found on the server, see validation window");
            } else if (code == 400) {
                Application.getInstance().getGUILog().log(response);
                log.info(response);
                return false;
            }
            return true;
        }
        if (response.length() > 3000) {
            //System.out.println(response);
            log.info(response);
            Application.getInstance().getGUILog().log("see md.log for what got received - too big to show");
        } else
            log.info(response);//Application.getInstance().getGUILog().log(response);
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
                gl.log(json);
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
    
    @SuppressWarnings("unchecked")
    public static void fillElement(Element e, JSONObject elementInfo, Stereotype view, Stereotype viewpoint) {
        if (e instanceof Package) {
            elementInfo.put("type", "Package");
        } else if (e instanceof Property) {
            elementInfo.put("type", "Property");
            elementInfo.put("isDerived", ((Property)e).isDerived());
            elementInfo.put("isSlot", false);
            ValueSpecification vs = ((Property)e).getDefaultValue();
            if (vs != null) {
                JSONArray value = new JSONArray();
                addValues(e, value, elementInfo, vs);
            }
            Type type = ((Property)e).getType();
            if (type != null) {
                elementInfo.put("propertyType", type.getID());
            } else
                elementInfo.put("propertyType", null);
        } else if (e instanceof Slot) {
            elementInfo.put("type", "Property");
            elementInfo.put("isDerived", false);
            elementInfo.put("isSlot", true);
            if (((Slot)e).getDefiningFeature().getID().equals("_17_0_2_3_e9f034d_1375396269655_665865_29411"))
                elementInfo.put("stylesaver", true);
            List<ValueSpecification> vsl = ((Slot)e).getValue();
            if (vsl != null && vsl.size() > 0) {
                JSONArray value = new JSONArray();
                for (ValueSpecification vs: vsl) {
                    addValues(e, value, elementInfo, vs);
                }
            }
            Element type = ((Slot)e).getDefiningFeature();
            if (type != null) {
                elementInfo.put("propertyType", type.getID());
            }
        } else if (e instanceof Dependency) {
            if (StereotypesHelper.hasStereotypeOrDerived(e, Utils.getConformsStereotype()))
                elementInfo.put("type", "Conform");
            else if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.queriesStereotype))
                elementInfo.put("type", "Expose");
            else
                elementInfo.put("type", "Dependency");
        } else if (e instanceof Generalization) {
            elementInfo.put("type", "Generalization");
        } else if (e instanceof DirectedRelationship) {   
            elementInfo.put("type", "DirectedRelationship");
        } else if (e instanceof Comment) {
            elementInfo.put("type", "Comment");
            elementInfo.put("body", Utils.stripHtmlWrapper(((Comment)e).getBody()));
            JSONArray elements = new JSONArray();
            for (Element el: ((Comment)e).getAnnotatedElement()) {
                elements.add(el.getID());
            }
            elementInfo.put("annotatedElements", elements);
        } else {
            elementInfo.put("type", "Element");
        }
        if (e instanceof DirectedRelationship) {
            Element client = ModelHelper.getClientElement(e);
            Element supplier = ModelHelper.getSupplierElement(e);
            elementInfo.put("source", client.getID());
            elementInfo.put("target", supplier.getID());
        }
        /*if (StereotypesHelper.hasStereotypeOrDerived(e, view))
            elementInfo.put("isView", true);
        else
            elementInfo.put("isView", false);*/
        if (viewpoint != null && StereotypesHelper.hasStereotypeOrDerived(e, viewpoint))
            elementInfo.put("type", "Viewpoint");
        if (e instanceof NamedElement) {
            elementInfo.put("name", ((NamedElement)e).getName());
        } else
            elementInfo.put("name", "");
        elementInfo.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(e)));
        if (e.getOwner() == null || e.getOwner() == Application.getInstance().getProject().getModel())
            elementInfo.put("owner", null);
        else
            elementInfo.put("owner", e.getOwner().getID());
        elementInfo.put("id", getElementID(e));
        /*JSONArray comments = new JSONArray();
        if ( e.get_commentOfAnnotatedElement() != null ) {
            for (Comment c: e.get_commentOfAnnotatedElement()) {
                if (isElementDocumentation(c))
                    continue;
                comments.add(c.getID());
            }
        }
        elementInfo.put("comments", comments);*/
    }
    
    @SuppressWarnings("unchecked")
    private static void addValues(Element e, JSONArray value, JSONObject elementInfo, ValueSpecification vs) {
        if (vs instanceof LiteralBoolean) {
            elementInfo.put("valueType", PropertyValueType.LiteralBoolean.toString());
            value.add(((LiteralBoolean)vs).isValue());
        } else if (vs instanceof LiteralString) {
            elementInfo.put("valueType", PropertyValueType.LiteralString.toString());
            value.add(((LiteralString)vs).getValue());
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
        }
        elementInfo.put("value", value);
    }
    
    public static boolean checkBaselineMount() {
        Project prj = Application.getInstance().getProject();
        if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
            String baselineTag = getBaselineTag();
            if (baselineTag == null)
                return false;
            List<String> tags = ProjectUtilities.getVersionTags(prj.getPrimaryProject());
            if (!tags.contains(baselineTag)) {
                Application.getInstance().getGUILog().log("The current project is not an approved version!");
                return false;
            }
        
            for (IAttachedProject proj: ProjectUtilities.getAllAttachedProjects(prj)) {
                if (ProjectUtilities.isFromTeamworkServer(proj)) {
                    List<String> tags2 = ProjectUtilities.getVersionTags(proj);
                    if (!tags2.contains(baselineTag)) {
                        Application.getInstance().getGUILog().log(proj.getName() + " is not an approved module version!");
                        return false;
                    }
                }
            }
        }
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
    
    public static Date getModuleTimestamp(Element e) {
        if (ProjectUtilities.isElementInAttachedProject(e)) {
            IProject module = ProjectUtilities.getAttachedProject(e);
            IVersionDescriptor version = ProjectUtilities.getVersion(module);
            return version.getDate();
        }
        if (!e.isEditable()) {
            IVersionDescriptor version = ProjectUtilities.getVersion(Application.getInstance().getProject().getPrimaryProject());
            return version.getDate();
        }
        return new Date();
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
    
    public static Integer getAlfrescoProjectVersion(Element e) {
        if (ProjectUtilities.isElementInAttachedProject(e)) {
            IAttachedProject aprj = ProjectUtilities.getAttachedProject(e);
            if (ProjectUtilities.isFromTeamworkServer(aprj))
                return getAlfrescoProjectVersion(aprj.getProjectID());
            return null;
        } else {
            Project prj = Application.getInstance().getProject();
            if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
                return getAlfrescoProjectVersion(prj.getPrimaryProject().getProjectID());
            }
            return null;
        }
    }
    
    public static Integer getModuleVersion(Element e) {
        Project prj = Application.getInstance().getProject();
        if (ProjectUtilities.isElementInAttachedProject(e)) {
            IProject module = ProjectUtilities.getAttachedProject(e);
            if (ProjectUtilities.isFromTeamworkServer(module)) {
                IVersionDescriptor vd = ProjectUtilities.getVersion(module);
                ProjectVersion pv = new ProjectVersion(vd);
                Integer teamwork = pv.getNumber();
                return teamwork;//TeamworkService.getInstance(prj).getVersion(modulePrj).getNumber();
            }
            return null;
        } else {
            if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject()))
                return TeamworkService.getInstance(prj).getVersion(prj).getNumber();
            return null;
        }
    }
    
    public static boolean versionOk(Element e) {
        Project prj = Application.getInstance().getProject();
        if (ProjectUtilities.isElementInAttachedProject(e)) {
            IProject module = ProjectUtilities.getAttachedProject(e);
            if (ProjectUtilities.isFromTeamworkServer(module)) {
                IVersionDescriptor vd = ProjectUtilities.getVersion(module);
                ProjectVersion pv = new ProjectVersion(vd);
                Integer teamwork = pv.getNumber();
                //Integer teamwork = TeamworkService.getInstance(prj).getVersion(modulePrj).getNumber();
                Integer mms = getAlfrescoProjectVersion(module.getProjectID());
                if (teamwork == mms || mms == null || teamwork > mms)
                    return true;
                return false;
            }
            return true;
        } else {
            if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) {
                Integer teamwork = TeamworkService.getInstance(prj).getVersion(prj).getNumber();
                Integer mms = getAlfrescoProjectVersion(prj.getPrimaryProject().getProjectID());
                if (teamwork == mms || mms == null || teamwork > mms)
                    return true;
                return false;
            }
            return true;
        }
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
        if (!versionOk(e)) {
            Boolean con = Utils.getUserYesNoAnswer("The element " + e.getHumanName() + " is in a project that is an older version of what's on the server, do you want to continue export?");
            if (con == null || !con)
                return false;
        }
        return true;
    }
    
    public static boolean okToExport(Set<Element> set) {
        Project prj = Application.getInstance().getProject();
        mountedVersions = new HashMap<String, Integer>();
        if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject()))
            mountedVersions.put(prj.getPrimaryProject().getProjectID(), TeamworkService.getInstance(prj).getVersion(prj).getNumber());
        for (Element e: set) {
            if (ProjectUtilities.isElementInAttachedProject(e)) {
                IProject module = ProjectUtilities.getAttachedProject(e);
                if (ProjectUtilities.isFromTeamworkServer(module) && !mountedVersions.containsKey(module.getProjectID())) {
                    IVersionDescriptor vd = ProjectUtilities.getVersion(module);
                    ProjectVersion pv = new ProjectVersion(vd);
                    Integer teamwork = pv.getNumber();
                    mountedVersions.put(module.getProjectID(), teamwork);
                }
            }
        }
        for (String prjId: mountedVersions.keySet()) {
            Integer serverVersion = getAlfrescoProjectVersion(prjId);
            if (serverVersion != null && serverVersion > mountedVersions.get(prjId)) {
                Boolean con = Utils.getUserYesNoAnswer("Some elements being exported come from an older project version than what's on the server, do you want to continue?");
                if (con == null || !con)
                    return false;
            }
        }
        return true;
    }
    
    public static boolean okToExport() {
        mountedVersions = new HashMap<String, Integer>();
        Project prj = Application.getInstance().getProject();
        if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject()))
            mountedVersions.put(prj.getPrimaryProject().getProjectID(), TeamworkService.getInstance(prj).getVersion(prj).getNumber());
        for (IAttachedProject p: ProjectUtilities.getAllAttachedProjects(prj)) {
            if (ProjectUtilities.isFromTeamworkServer(p)) {
                IVersionDescriptor vd = ProjectUtilities.getVersion(p);
                ProjectVersion pv = new ProjectVersion(vd);
                Integer teamwork = pv.getNumber();
                mountedVersions.put(p.getProjectID(), teamwork);
            }
        }
        for (String prjId: mountedVersions.keySet()) {
            Integer serverVersion = getAlfrescoProjectVersion(prjId);
            if (serverVersion != null && serverVersion > mountedVersions.get(prjId)) {
                Boolean con = Utils.getUserYesNoAnswer("Your project or mounts is an older project version than what's on the server, do you want to continue?");
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
    
    public static Map<String, JSONObject> getReferencedElements(Element e) {
        Stereotype view = Utils.getViewStereotype();
        Stereotype viewpoint = Utils.getViewpointStereotype();
        Map<String, JSONObject> result = new HashMap<String, JSONObject>();
        if (e instanceof Property) {
            Element value = null;
            if (((Property)e).getDefaultValue() instanceof ElementValue) {
                value = ((ElementValue)((Property)e).getDefaultValue()).getElement();
            } else if (((Property)e).getDefaultValue() instanceof InstanceValue) {
                value = ((InstanceValue)((Property)e).getDefaultValue()).getInstance();
            }
            if (value != null) {
                JSONObject j = new JSONObject();
                fillElement(value, j, view, viewpoint);
                result.put(value.getID(), j);
            }
        } else if (e instanceof Slot) {
            for (ValueSpecification vs: ((Slot)e).getValue()) {
                Element value = null;
                if (vs instanceof ElementValue) {
                    value = ((ElementValue)vs).getElement();
                } else if (vs instanceof InstanceValue) {
                    value = ((InstanceValue)vs).getInstance();
                }
                if (value != null) {
                    JSONObject j = new JSONObject();
                    fillElement(value, j, view, viewpoint);
                    result.put(value.getID(), j);
                }
            }
        }
        return result;
    }
    
}
