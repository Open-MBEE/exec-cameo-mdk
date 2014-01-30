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

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
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
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ExportUtility {
    
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
        return "https://sheldon/alfresco/service";
        /*String url = null;
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
        return url;*/ 
    }
    
    public static String getUrlWithSite() {
        Element model = Application.getInstance().getProject().getModel();
        String  url = getUrl();
        if (url == null)
            return null;
        /*String site = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem", "site");
        if (site == null || site.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Your project root element doesn't have ModelManagementSystem site stereotype property set!");
                return null;
        }*/
        return url + "/javawebscripts/sites/europa";
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
    
    public static boolean showErrors(int code, String response) {
        if (code != 200) {
            if (code == 500) {
                Utils.showPopupMessage("Server Error");
                Application.getInstance().getGUILog().log(response);
            } else if (code == 401) {
                Utils.showPopupMessage("You are not authorized (you may have entered password wrong, you have been logged out, try again");
                ViewEditUtils.clearCredentials();
            } else if (code == 403) {
                Utils.showPopupMessage("You do not have permission to do this");
            } else if (code == 404) {
                Utils.showPopupMessage("Not found");
            } else if (code == 400) {
                Application.getInstance().getGUILog().log(response);
                return false;
            }
            return true;
        }
        if (response.length() > 3000) {
            System.out.println(response);
            Application.getInstance().getGUILog().log("see md.log for what got received - too big to show");
        } else
            Application.getInstance().getGUILog().log(response);
        return false;
    }
    
    public static boolean send(String url, String json, String method) {
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
                System.out.println(json);
                gl.log("(see md.log for what got send - too big to show)");
            } else
                gl.log(json);
            pm.setRequestHeader("Content-Type", "application/json;charset=utf-8");
            pm.setRequestEntity(JsonRequestEntity.create(json));
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url);
            int code = client.executeMethod(pm);
            String response = pm.getResponseBodyAsString();
            if (showErrors(code, response)) {
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
        if (url == null)
            return null;
        GetMethod gm = new GetMethod(url);
        try {
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url);
            Application.getInstance().getGUILog().log("[INFO] Getting...");
            int code = client.executeMethod(gm);
            String json = gm.getResponseBodyAsString();
            
            if (showErrors(code, json)) {
                return null;
            }
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
        JSONArray comments = new JSONArray();
        if ( e.get_commentOfAnnotatedElement() != null ) {
            for (Comment c: e.get_commentOfAnnotatedElement()) {
                if (isElementDocumentation(c))
                    continue;
                comments.add(c.getID());
            }
        }
        elementInfo.put("comments", comments);
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
}
