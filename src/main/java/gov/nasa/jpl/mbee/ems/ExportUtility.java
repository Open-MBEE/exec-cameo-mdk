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
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.ci.persistence.versioning.IVersionDescriptor;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.StringExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
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
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ExportUtility {
    public static Logger log = Logger.getLogger(ExportUtility.class);
    
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
        String  url = getUrl();
        if (url == null)
            return null;
        /* Element model = Application.getInstance().getProject().getModel();
        String site = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem", "site");
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
                Utils.showPopupMessage("Server Error, see message window for details");
                Application.getInstance().getGUILog().log(response);
            } else if (code == 401) {
                Utils.showPopupMessage("You are not authorized or don't have permission, you have been logged out (you can login and try again)");
                ViewEditUtils.clearCredentials();
            } else if (code == 403) {
                Utils.showPopupMessage("You do not have permission to do this");
            } else if (code == 404) {
                Utils.showPopupMessage("The thing you're trying to validate or get wasn't found on the server, see validation window");
            } else if (code == 400) {
                Application.getInstance().getGUILog().log(response);
                return false;
            }
            return true;
        }
        if (response.length() > 3000) {
            //System.out.println(response);
            log.info(response);
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

    @SuppressWarnings( "unchecked" )
    public static void fillValueSpecification(ValueSpecification vs, JSONObject elementInfo, Stereotype view, Stereotype viewpoint) {
        ValueSpecification expr = ((Duration)vs ).getExpression();
        if ( expr != null ) {
            elementInfo.put( "valueExpression", expr.getID() );
        }
        if ( vs instanceof Duration ) {
            elementInfo.put("type", "Duration");
            //((Duration)vs).getExpr(); // REVIEW -- Is this the same as getExpression() above?!
//   java.util.Collection<DurationInterval>    get_durationIntervalOfMax()
//            Returns the value of the 'duration Interval Of Max' reference list.
//   java.util.Collection<DurationInterval>     get_durationIntervalOfMin()
//            Returns the value of the 'duration Interval Of Min' reference list.
//   ValueSpecification     getExpr()
//            Returns the value of the 'Expr' containment reference.
//   java.util.Collection<Observation>  getObservation()
//            Returns the value of the 'Observation' reference list.
        } else if (vs instanceof DurationInterval) {
            elementInfo.put("type", "DurationInterval");
            //            DurationConstraint     get_durationConstraintOfSpecification()
//            Returns the value of the 'duration Constraint Of Specification' container reference.
            Duration maxD = ((DurationInterval)vs ).getMax();
            if ( maxD != null ) elementInfo.put( "durationMax", maxD.getID() );
            Duration minD = ((DurationInterval)vs ).getMin();
            if ( minD != null ) elementInfo.put( "durationMin", minD.getID() );
        } else if (vs instanceof ElementValue) {
            elementInfo.put("type", "ElementValue");
            Element elem = ((ElementValue)vs).getElement();
            if ( elem != null ) {
                elementInfo.put( "element", elem.getID() );
            }
        } else if (vs instanceof Expression) {
            elementInfo.put("type", "Expression");
            if ( ((Expression)vs).getSymbol() != null ) {
                elementInfo.put("symbol", ((Expression)vs).getSymbol());
            }
            List<ValueSpecification> vsl = ((Expression)vs).getOperand();
            if (vsl != null && vsl.size() > 0) {
                elementInfo.put( "operand", makeJsonArrayOfIDs( vsl ) );
            }
        } else if (vs instanceof InstanceValue) {
            elementInfo.put("type", "InstanceValue");
            InstanceValue iv = (InstanceValue)vs;
            InstanceSpecification i = iv.getInstance();
            if ( i != null ) {
                elementInfo.put( "instance", i.getID() );
            }
        } else if (vs instanceof Interval) {
            elementInfo.put("type", "Interval");
        } else if (vs instanceof LiteralSpecification) {
            if (vs instanceof LiteralBoolean) {
                elementInfo.put("type", "LiteralBoolean");
                elementInfo.put( "boolean", ((LiteralBoolean)vs ).isValue() );
            } else if (vs instanceof LiteralInteger) {
                elementInfo.put("type", "LiteralInteger");
                elementInfo.put( "integer", ((LiteralInteger)vs ).getValue() );
            } else if (vs instanceof LiteralNull) {
                elementInfo.put("type", "LiteralNull");
            } else if (vs instanceof LiteralReal) {
                elementInfo.put("type", "LiteralReal");
                elementInfo.put( "double", ((LiteralReal)vs ).getValue() );
            } else if (vs instanceof LiteralString) {
                elementInfo.put("type", "LiteralString");
                elementInfo.put( "string", ((LiteralString)vs ).getValue() );
            } else if (vs instanceof LiteralUnlimitedNatural) {
                elementInfo.put("type", "LiteralUnlimitedNatural");
                elementInfo.put( "naturalValue", ((LiteralUnlimitedNatural)vs ).getValue() );
            }
        } else if (vs instanceof OpaqueExpression) {
            elementInfo.put("type", "OpaqueExpression");
            List<String> body = ((OpaqueExpression)vs).getBody();
            if ( body != null && body.size() > 0 ) {
                elementInfo.put("body", makeJsonArray( body ) );
            }
//   Abstraction    get_abstractionOfMapping() 
//            Returns the value of the 'abstraction Of Mapping' container reference.
//   Behavior   getBehavior() 
//            Returns the value of the 'Behavior' reference.
//   java.util.List<java.lang.String>   getBody() 
//            Returns the value of the 'Body' attribute list.
//   java.util.List<java.lang.String>   getLanguage() 
//            Returns the value of the 'Language' attribute list.
//   Parameter  getResult() 
//            Returns the value of the 'Result' reference.                
        } else if (vs instanceof StringExpression) {
            elementInfo.put("type", "StringExpression");
//   NamedElement   get_namedElementOfNameExpression() 
//            Returns the value of the 'named Element Of Name Expression' container reference.
//   StringExpression   getOwningExpression() 
//            Returns the value of the 'Owning Expression' container reference.
//   java.util.Collection<StringExpression> getSubExpression() 
//            Returns the value of the 'Sub Expression' containment reference list.                
        } else if (vs instanceof TimeExpression) {
            elementInfo.put("type", "TimeExpression");
//   TimeEvent  get_timeEventOfWhen() 
//            Returns the value of the 'time Event Of When' container reference.
//   java.util.Collection<TimeInterval> get_timeIntervalOfMax() 
//            Returns the value of the 'time Interval Of Max' reference list.
//   java.util.Collection<TimeInterval> get_timeIntervalOfMin() 
//            Returns the value of the 'time Interval Of Min' reference list.
//   ValueSpecification getExpr() 
//            Returns the value of the 'Expr' containment reference.
//   java.util.Collection<Observation>  getObservation() 
//            Returns the value of the 'Observation' reference list.
        } else if (vs instanceof TimeInterval) {
            elementInfo.put("type", "TimeInterval");
//   TimeConstraint get_timeConstraintOfSpecification() 
//            Returns the value of the 'time Constraint Of Specification' container reference.
            TimeExpression maxD = ((TimeInterval)vs).getMax();
            if ( maxD != null ) elementInfo.put( "timeMax", maxD.getID() );
            TimeExpression minD = ((TimeInterval)vs).getMin();
            if ( minD != null ) elementInfo.put( "timeMin", minD.getID() );
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
        if (e instanceof Package) {
            elementInfo.put("type", "Package");
        } else if (e instanceof Property) {
            elementInfo.put("type", "Property");
            elementInfo.put("isDerived", ((Property)e).isDerived());
            elementInfo.put("isSlot", false);
            ValueSpecification vs = ((Property)e).getDefaultValue();
            if (vs != null) {
                elementInfo.put( "value", vs.getID() );
//                JSONArray value = new JSONArray();
//                addValues(e, value, elementInfo, vs);
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
//                JSONArray value = new JSONArray();
//                for (ValueSpecification vs: vsl) {
//                    if ( vs != null ) value.add( vs.getID() );
////                    addValues(e, value, elementInfo, vs);
//                }
//                elementInfo.put( "value", value );
                elementInfo.put( "value", makeJsonArrayOfIDs( vsl ) );
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
//            JSONArray elements = new JSONArray();
//            for (Element el: ((Comment)e).getAnnotatedElement()) {
//                if ( el != null ) elements.add(el.getID());
//            }
//            elementInfo.put("annotatedElements", elements);
            elementInfo.put("annotatedElements",
                            makeJsonArrayOfIDs( ((Comment)e).getAnnotatedElement() ));
        } else if (e instanceof Operation) {
            elementInfo.put("type", "Operation");
            List<Parameter> vsl = ((Operation)e).getOwnedParameter();
            if (vsl != null && vsl.size() > 0) {
//                JSONArray value = new JSONArray();
//                for (Parameter p: vsl) {
//                    if ( p != null ) value.add( p.getID() );
//                }
//                elementInfo.put( "value", value );
                elementInfo.put("parameter", makeJsonArrayOfIDs( vsl ));
            }
        } else if (e instanceof ValueSpecification) {
            fillValueSpecification( (ValueSpecification)e, elementInfo, view, viewpoint );
        } else if (e instanceof InstanceSpecification) {
            elementInfo.put( "type", "InstanceSpecification" );
//   java.util.Collection<InstanceValue>   get_instanceValueOfInstance()
//            Returns the value of the 'instance Value Of Instance' reference list.
//   java.util.List<Classifier>     getClassifier()
//            Returns the value of the 'Classifier' reference list.
//   java.util.Collection<Slot>     getSlot()
//            Returns the value of the 'Slot' containment reference list.
//   ValueSpecification     getSpecification()
//          Returns the value of the 'Specification' containment reference.
            ValueSpecification spec = ((InstanceSpecification)e).getSpecification();
            if ( spec != null ) elementInfo.put("specification", spec.getID() );
//   Element    getStereotypedElement()
//            Returns the value of the 'Stereotyped Element' container reference.            
        } else if (e instanceof Parameter) {
            Parameter p = (Parameter)e;
            elementInfo.put( "type", "Element" );
            if ( p.getDirection() != null ) elementInfo.put( "direction", p.getDirection() );
            if ( p.getType() != null ) elementInfo.put( "parameterType", p.getType().getID() );
            ValueSpecification defaultValue = p.getDefaultValue();
            if ( defaultValue != null ) {
                elementInfo.put( "defaultValue", defaultValue.getID() );
            }
        // REVIEW -- do we want to specify Type?
//        } else if (e instanceof Type) {
////   java.util.Collection<Association>     get_associationOfEndType()
////            Returns the value of the 'association Of End Type' reference list.
////   java.util.Collection<BehavioralFeature>    get_behavioralFeatureOfRaisedException()
////            Returns the value of the 'behavioral Feature Of Raised Exception' reference list.
////   java.util.Collection<Operation>    get_operationOfRaisedException()
////            Returns the value of the 'operation Of Raised Exception' reference list.
////   java.util.Collection<TypedElement>     get_typedElementOfType()
////            Returns the value of the 'typed Element Of Type' reference list.
////   Package    getPackage()
////            Returns the value of the 'Package' reference.
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
    
    public static boolean checkBaselineMount() {
        Project prj = Application.getInstance().getProject();
        if (prj.isRemote()) {
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
}
