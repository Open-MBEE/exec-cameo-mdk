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
import gov.nasa.jpl.mbee.actions.ems.EMSLoginAction;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncProjectListener;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;
import javassist.bytecode.Descriptor.Iterator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.magicdraw.teamwork2.TeamworkService;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityParameterNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ControlFlow;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ObjectFlow;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.StringExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ParameterDirectionKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.OpaqueBehavior;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.CallEvent;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Event;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Trigger;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.Duration;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationInterval;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeInterval;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.FinalState;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Pseudostate;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.PseudostateKind;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdprotocolstatemachines.ProtocolConformance;

public class EMFExporter {
    public static Logger log = Logger.getLogger(EMFExporter.class);
    public static Map<String, Integer> mountedVersions;
    private static String developerUrl = "https://sheldon.jpl.nasa.gov";
    private static String developerSite = "europa";
    private static String developerWs = "master";
    public static boolean baselineNotSet = false;
    public static Map<String, Map<String, String>> wsIdMapping = new HashMap<String, Map<String, String>>();
    public static Map<String, Map<String, String>> sites = new HashMap<String, Map<String, String>>();
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillElement(Element e, JSONObject eInfo) {
        JSONObject elementInfo = eInfo;
        if (elementInfo == null){
                
        }
        return null;
    }

    public static String getElementID(Element e) {
        if (e == null) {
            return null;
        }
        if (e instanceof Slot) {
            if (e.getOwner() == null || ((Slot)e).getDefiningFeature() == null)
                return null;
            return e.getOwner().getID() + "-slot-"
                    + ((Slot) e).getDefiningFeature().getID();
        } else if (e instanceof Model && e == Application.getInstance().getProject().getModel()) {
            return Application.getInstance().getProject().getPrimaryProject().getProjectID();
        }
        return e.getID();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends MDObject> JSONArray makeJsonArrayOfIDs(
            Collection<T> collection) {
        JSONArray ids = new JSONArray();
        for (T t : collection) {
            if (t != null)
                ids.add(t.getID());
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    protected static <T> JSONArray makeJsonArray(Collection<T> collection) {
        JSONArray arr = new JSONArray();
        for (T t : collection) {
            if (t != null)
                arr.add(t);
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillElementOld(Element e, JSONObject eInfo) {
        JSONObject elementInfo = eInfo;
        if (elementInfo == null)
            elementInfo = new JSONObject();
        //JSONObject specialization = new JSONObject();
        //elementInfo.put("specialization", specialization);
        Stereotype commentS = Utils.getCommentStereotype();
        if (e instanceof Package) {
            fillPackage((Package)e, elementInfo);
        } else if (e instanceof Property || e instanceof Slot) {
        		fillPropertySpecialization(e, elementInfo, true, true);
        } else if (e instanceof DirectedRelationship) {
            fillDirectedRelationshipSpecialization((DirectedRelationship)e, elementInfo);
        } else if (e instanceof Connector) {
            fillConnectorSpecialization((Connector)e, elementInfo);
        } else if (e instanceof Operation) {
            fillOperationSpecialization((Operation)e, elementInfo);
        } else if (e instanceof Constraint) {
            fillConstraintSpecialization((Constraint)e, elementInfo);
        } else if (e instanceof InstanceSpecification) {
            elementInfo.put("type", "InstanceSpecification");
            fillInstanceSpecificationSpecialization((InstanceSpecification)e, elementInfo);
            /*ValueSpecification spec = ((InstanceSpecification) e)
                    .getSpecification();
            if (spec != null)
                specialization.put("instanceSpecificationSpecification",
                        spec.getID());*/
        } else if (e instanceof Parameter) {
            fillParameterSpecialization((Parameter)e, elementInfo);
        } else if (e instanceof Comment || StereotypesHelper.hasStereotypeOrDerived(e, commentS)) {
            elementInfo.put("type", "Comment");
        } else if (e instanceof Association) {
            fillAssociationSpecialization((Association)e, elementInfo);
       
        } else if (e.getClass().getSimpleName().equals("ClassImpl")) {
            Stereotype viewpoint = Utils.getViewpointStereotype();
            Stereotype view = Utils.getViewStereotype();
            Stereotype doc = Utils.getProductStereotype();
            //Stereotype view = Utils.getViewStereotype();
            if (viewpoint != null && StereotypesHelper.hasStereotypeOrDerived(e, viewpoint))
                elementInfo.put("type", "Viewpoint");
            else if (view != null && StereotypesHelper.hasStereotypeOrDerived(e, view)) {
                if (StereotypesHelper.hasStereotypeOrDerived(e, doc))
                    elementInfo.put("type", "Product");
                else
                    elementInfo.put("type", "View");
                fillViewContent(e, elementInfo);
            } else
                elementInfo.put("type", "Element");
        
        } else { 
            String typeName = "Untyped"; //default
            
            Class baseClass = StereotypesHelper.getBaseClass(e);
            if (baseClass != null)
                typeName = baseClass.getName();
            elementInfo.put("type", typeName);
           
            if (e instanceof ActivityParameterNode)
                fillActivityParameterNode((ActivityParameterNode) e, elementInfo); 
            else if (e instanceof Event)
                fillEvent((Event) e, elementInfo);
            else if (e instanceof Transition )
                fillTransition((Transition) e, elementInfo);
            else if (e instanceof ActivityEdge) //ControlFlow ObjectFlow
                fillActivityEdge((ActivityEdge)e, elementInfo);
            else if (e instanceof OpaqueBehavior) //OpaqueBehavior,  FunctionBehavior
                fillOpaqueBehavior((OpaqueBehavior) e, elementInfo);
            else if (e instanceof CallBehaviorAction)
                fillCallBehaviorAction((CallBehaviorAction)e, elementInfo);
            else if (e instanceof Trigger)
                fillTrigger((Trigger)e, elementInfo);
            else if (e instanceof State)
                fillState((State)e, elementInfo);
            else if (e instanceof Pseudostate)
                fillPseudostate((Pseudostate)e, elementInfo);
        }
        fillOwnedAttribute(e, elementInfo);
        fillName(e, elementInfo);
        fillDoc(e, elementInfo);
        fillOwner(e, elementInfo);
        fillMetatype(e, elementInfo);
        elementInfo.put("sysmlId", getElementID(e));
        return elementInfo;
    }
    
    
    
    public static JSONObject fillValueSpecification(ValueSpecification vs,
            JSONObject einfo) {
        return fillValueSpecification(vs, einfo, false);
    }
 
    //given value spec and value object, fill in stuff
    @SuppressWarnings("unchecked")
    public static JSONObject fillValueSpecification(ValueSpecification vs,
            JSONObject einfo, boolean useLongForDouble) {
        if (vs == null)
            return null;
        JSONObject elementInfo = einfo;
        if (elementInfo == null)
            elementInfo = new JSONObject();
        elementInfo.put("valueExpression", null);
        // ValueSpecification expr = vs.getExpression();
        // if ( expr != null ) {
        // elementInfo.put( "valueExpression", expr.getID() );
        // }
        if (vs instanceof Duration) {
            elementInfo.put("type", "Duration");
        } else if (vs instanceof DurationInterval) {
            elementInfo.put("type", "DurationInterval");
            elementInfo.put("min", null);
            elementInfo.put("max", null);
            /*Duration maxD = ((DurationInterval) vs).getMax();
            if (maxD != null) {
                elementInfo.put("max", maxD.getID());
            Duration minD = ((DurationInterval) vs).getMin();
            if (minD != null)
                elementInfo.put("durationMin", minD.getID());*/
        } else if (vs instanceof ElementValue) {
            elementInfo.put("type", "ElementValue");
            Element elem = ((ElementValue) vs).getElement();
            elementInfo.put("elementId", ((elem != null) ? getElementID(elem) : null));
        } else if (vs instanceof Expression) {
            elementInfo.put("type", "Expression");
            //if (((Expression) vs).getSymbol() != null) {
            //    elementInfo.put("symbol", ((Expression) vs).getSymbol());
            //}
            List<ValueSpecification> vsl = ((Expression) vs).getOperand();
            if (vsl != null && vsl.size() > 0) {
                JSONArray operand = new JSONArray();
                for (ValueSpecification vs2 : vsl) {
                    JSONObject res = new JSONObject();
                    fillValueSpecification(vs2, res, useLongForDouble);
                    operand.add(res);
                }
                elementInfo.put("operand", operand);
            }
        } else if (vs instanceof InstanceValue) {
            elementInfo.put("type", "InstanceValue");
            InstanceValue iv = (InstanceValue) vs;
            InstanceSpecification i = iv.getInstance();
            elementInfo.put("instance", ((i != null) ? getElementID(i) : null));
         } else if (vs instanceof LiteralSpecification) {
            if (vs instanceof LiteralBoolean) {
                elementInfo.put("type", "LiteralBoolean");
                elementInfo.put("boolean", ((LiteralBoolean) vs).isValue());
            } else if (vs instanceof LiteralInteger) {
                elementInfo.put("type", "LiteralInteger");
                elementInfo.put("integer", new Long(((LiteralInteger) vs).getValue()));
            } else if (vs instanceof LiteralNull) {
                elementInfo.put("type", "LiteralNull");
            } else if (vs instanceof LiteralReal) {
                elementInfo.put("type", "LiteralReal");
                double real = ((LiteralReal) vs).getValue();
                elementInfo.put("double", real);
                if (real % 1 == 0 && useLongForDouble) {
                    try {
                        elementInfo.put("double", (long)real);
                    } catch (Exception ex) {
                        
                    }
                }
            } else if (vs instanceof LiteralString) {
                elementInfo.put("type", "LiteralString");
                elementInfo.put("string", Utils.stripHtmlWrapper(((LiteralString) vs).getValue()));
            } else if (vs instanceof LiteralUnlimitedNatural) {
                elementInfo.put("type", "LiteralUnlimitedNatural");
                elementInfo.put("naturalValue", new Long(
                        ((LiteralUnlimitedNatural) vs).getValue()));
            }
        } else if (vs instanceof OpaqueExpression) {
            elementInfo.put("type", "OpaqueExpression");
            List<String> body = ((OpaqueExpression) vs).getBody();
            elementInfo.put("expressionBody", ((body != null) ?  makeJsonArray(body) : new JSONArray()));
        } else if (vs instanceof StringExpression) {
            elementInfo.put("type", "StringExpression");
        } else if (vs instanceof TimeExpression) {
            elementInfo.put("type", "TimeExpression");
        } else if (vs instanceof TimeInterval) {
            elementInfo.put("type", "TimeInterval");
            elementInfo.put("min", null);
            elementInfo.put("max", null);
            /*TimeExpression maxD = ((TimeInterval) vs).getMax();
            if (maxD != null)
                elementInfo.put("timeIntervalMax", maxD.getID());
            TimeExpression minD = ((TimeInterval) vs).getMin();
            if (minD != null)
                elementInfo.put("timeIntervalMin", minD.getID());*/
        }
        return elementInfo;
    }
    
    
    
    
    @SuppressWarnings("unchecked")
    public static void fillPseudostate(Pseudostate e, JSONObject elementInfo) {
        PseudostateKind s;
        elementInfo.put("kind", ((s = e.getKind()) == null) ? null : s.toString());
    }
    @SuppressWarnings("unchecked")
    public static void fillState(State e, JSONObject elementInfo) {
        Element s;
        if (!(e instanceof FinalState)){
            elementInfo.put("doActivityId", ((s = e.getDoActivity()) == null) ? null : s.getID());
            elementInfo.put("entryId", ((s = e.getEntry()) == null) ? null : s.getID());
            elementInfo.put("exitId", ((s = e.getExit()) == null) ? null : s.getID());
        }
    }
    @SuppressWarnings("unchecked")
    public static void fillEvent(Event e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("behaviorId", ((s = e.getBehavior()) == null) ? null : s.getID());
        if ( e instanceof CallEvent)
            elementInfo.put("operationId", ((s = ((CallEvent)e).getOperation()) == null) ? null : s.getID());
    }
    @SuppressWarnings("unchecked")
    public static void fillTrigger(Trigger e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("eventId", ((s = e.getEvent()) == null) ? null : s.getID());
    }
    @SuppressWarnings("unchecked")
    public static void fillTransition(Transition e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("clientId", ((s = ModelHelper.getClientElement(e)) == null) ? null : s.getID());
        elementInfo.put("supplierId", ((s = ModelHelper.getSupplierElement(e)) == null) ? null : s.getID());
        elementInfo.put("effectId", ((s = e.getEffect()) == null) ? null : s.getID());
        if ( e.hasTrigger()){
            for (Trigger t: e.getTrigger()) {//only one is allow to define in MD
                elementInfo.put("triggerId", t.getID());
            }
        }
        else
            elementInfo.put("triggerId", null);  
    }
    @SuppressWarnings("unchecked")
    public static void fillActivityParameterNode(ActivityParameterNode e, JSONObject elementInfo) {
        Parameter s;
        elementInfo.put("parameterId", ((s = e.getParameter()) == null) ? null : s.getID());
    }
    @SuppressWarnings("unchecked")
    public static void fillOpaqueBehavior(OpaqueBehavior e, JSONObject elementInfo) {
        elementInfo.put("body", makeJsonArray(e.getBody()));
        elementInfo.put("language", makeJsonArray(e.getLanguage()));
    }
    @SuppressWarnings("unchecked")
    public static void fillActivityEdge(ActivityEdge e, JSONObject elementInfo) {
      
        Element s;
        elementInfo.put("sourceId", ((s = e.getSource()) == null) ? null : s.getID());
        elementInfo.put("targetId", ((s = e.getTarget()) == null) ? null : s.getID());
        
        ValueSpecification gurad = e.getGuard();
        if ( gurad == null)
            elementInfo.put("guard", null);
        else {
            JSONObject vs = new JSONObject();
            fillValueSpecification(gurad, vs);
            elementInfo.put("guard", vs);
        }
    }
    @SuppressWarnings("unchecked")
    public static void fillCallBehaviorAction(CallBehaviorAction e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("behaviorId", ((s = e.getBehavior()) == null) ? null : s.getID());
    }
	public static JSONObject fillViewContent(Element e, JSONObject elementInfo) {
        Stereotype doc = Utils.getProductStereotype();
        if (elementInfo == null)
            elementInfo = new JSONObject();
        if (StereotypesHelper.hasStereotypeOrDerived(e, doc))
            elementInfo.put("type", "Product");
        else
            elementInfo.put("type", "View");
        Constraint c = Utils.getViewConstraint(e);
        if (c != null) {
            JSONObject cob = fillConstraintSpecialization(c, null);
            if (cob.containsKey("specification")) {
                elementInfo.put("contents", (JSONObject)cob.get("specification"));
                elementInfo.put("contains", new JSONArray());
            }
        }
        Object o = StereotypesHelper.getStereotypePropertyFirst(e, Utils.getViewClassStereotype(), "elements");
        if (o != null && o instanceof String) {
            try {
                JSONArray a = (JSONArray)JSONValue.parse((String)o);
                elementInfo.put("allowedElements", new JSONArray());
                elementInfo.put("displayedElements", a);
            } catch (Exception ex) {}
        } else {
            elementInfo.put("displayedElements", new JSONArray());
        }
        return elementInfo;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillPropertySpecialization(Element e, JSONObject elementInfo, boolean value, boolean ptype) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
		if (e instanceof Property) {
		    elementInfo.put("aggregation", ((Property)e).getAggregation().toString().toUpperCase());
		    elementInfo.put("type", "Property");
		    elementInfo.put("isDerived", ((Property) e).isDerived());
		    elementInfo.put("isSlot", false);
		    if (value) {
		        ValueSpecification vs = ((Property) e).getDefaultValue();
		        JSONArray singleElementSpecVsArray = new JSONArray();
		        if (vs != null) {
		            // Create a new JSONObject and a new JSONArray. Fill in
		            // the values to the new JSONObject and then insert
		            // that JSONObject into the array (NOTE: there will
		            // be single element in this array). Finally, insert
		            // the array into the specialization element as the
		            // value of the "value" property.
		            //
		            
		            JSONObject newElement = new JSONObject();
		            fillValueSpecification(vs, newElement);
		            singleElementSpecVsArray.add(newElement);
		        }
		        elementInfo.put("value", singleElementSpecVsArray);
		    }
		    //specialization.put("upper", fillValueSpecification(((Property)e).getUpperValue(), null));
		    //specialization.put("lower", fillValueSpecification(((Property)e).getLowerValue(), null));
		    if (ptype) {
		        Type type = ((Property) e).getType();
		        elementInfo.put("propertyTypeId", (type == null) ? null : type.getID());
		        
		    }
		    elementInfo.put("multiplicityMin", (long)((Property)e).getLower());
		    elementInfo.put("multiplicityMax", (long)((Property)e).getUpper());
		     
		    Collection<Property> cps = ((Property)e).getRedefinedProperty();
		    JSONArray redefinedProperties = new JSONArray();
		    for (Property cp : cps) 
		     	redefinedProperties.add(getElementID(cp));
		    elementInfo.put("redefinesId", redefinedProperties);
		   
		} else { //if (e instanceof Slot) {
		    elementInfo.put("type", "Property");
		    elementInfo.put("isDerived", false);
		    elementInfo.put("isSlot", true);
		    
		
		    // Retrieve a list of ValueSpecification objects.
		    // Loop through these objects, creating a new JSONObject
		    // for each value spec. Fill in the new JSONObject and
		    // insert them into a new JSONArray.
		    // Finally, once you've looped through all the value
		    // specifications, insert the JSONArray into the
		    // new specialization element.
		    //
		    if (value) {
		        List<ValueSpecification> vsl = ((Slot) e).getValue();
		        JSONArray specVsArray = new JSONArray();
		        if (vsl != null && vsl.size() > 0) {
		            for (ValueSpecification vs : vsl) {
		                JSONObject newElement = new JSONObject();
		                fillValueSpecification(vs, newElement);
		                specVsArray.add(newElement);
		            }
		        }
		        elementInfo.put("value", specVsArray);
		    }
		    if (ptype) {
		        Element type = ((Slot) e).getDefiningFeature();
		        elementInfo.put("propertyTypeId", (type == null) ? null : type.getID());
		    }
		}
        return elementInfo;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillInstanceSpecificationSpecialization(InstanceSpecification e, JSONObject elementInfo) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
        if (e.getSpecification() != null)
            elementInfo.put("instanceSpecificationSpecification", fillValueSpecification(e.getSpecification(), null));
        JSONArray classifiers = new JSONArray();
        for (Classifier c: e.getClassifier()) {
            classifiers.add(c.getID());
        }
        elementInfo.put("classifierId", classifiers);
        elementInfo.put("type", "InstanceSpecification");
        return elementInfo;
    }
    
	public static JSONObject sanitizeJSON(JSONObject spec) {
		List<Object> remKeys = new ArrayList<Object>();
		for (Object key: spec.keySet()) {
			// delete empty JSONArray
			if (spec.get(key) instanceof JSONArray && ((JSONArray)spec.get(key)).isEmpty()) {
				remKeys.add(key);
			}
		}
		for (Object key: remKeys) {
			spec.remove(key);
		}
		return spec;
	}
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillAssociationSpecialization(Association e, JSONObject elementInfo) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
        int i = 0;
        for (Property p: e.getMemberEnd()) {
            if (i == 0) {
                elementInfo.put("sourceId", p.getID());
                // specialization.put("sourceAggregation", p.getAggregation().toString().toUpperCase());
            } else {
                elementInfo.put("targetId", p.getID());
                // specialization.put("targetAggregation", p.getAggregation().toString().toUpperCase());
            }
            i++;
        }
        JSONArray owned = new JSONArray();
        for (Property p: e.getOwnedEnd()) {
            owned.add(p.getID());
        }
        elementInfo.put("ownedEnd", owned);
        elementInfo.put("type", "Association");
        return elementInfo;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillPackage(Package e, JSONObject elementInfo) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
        elementInfo.put("type", "Package");
        elementInfo.put("isSite", Utils.isSiteChar(e));
        return elementInfo;
    }
 
    @SuppressWarnings("unchecked")
    public static JSONObject fillConstraintSpecialization(Constraint e, JSONObject elementInfo) {
        elementInfo.put("type", "Constraint");
        ValueSpecification vspec = ((Constraint) e).getSpecification();
        if (vspec != null) {
            JSONObject cspec = new JSONObject();
            fillValueSpecification(vspec, cspec);
            elementInfo.put("specification", cspec);
        }
        return elementInfo;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillConnectorSpecialization(Connector e, JSONObject elementInfo) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
        elementInfo.put("type", "Connector");
        int i = 0;
        if (e.getEnd() == null)
            return elementInfo;
        for ( ConnectorEnd end : e.getEnd()) {
            JSONArray propertyPath = new JSONArray();
            if ( end.getRole() != null ) {
                if (StereotypesHelper.hasStereotype(end, "NestedConnectorEnd")) {
                    List<Element> ps = StereotypesHelper.getStereotypePropertyValue(end, "NestedConnectorEnd", "propertyPath");
                    for (Element path: ps) {
                        if (path instanceof ElementValue) {
                            propertyPath.add(((ElementValue)path).getElement().getID());
                        } else if (path instanceof Property)
                            propertyPath.add(path.getID());
                    }
                }
                propertyPath.add(end.getRole().getID());
            }
            if (i == 0) {
                //specialization.put("sourceUpper", fillValueSpecification(end.getUpperValue(), null));
                //specialization.put("sourceLower", fillValueSpecification(end.getLowerValue(), null));
                elementInfo.put("sourcePathId", propertyPath);
            } else {
                //specialization.put("targetUpper", fillValueSpecification(end.getUpperValue(), null));
                //specialization.put("targetLower", fillValueSpecification(end.getLowerValue(), null));
                elementInfo.put("targetPathId", propertyPath);
            }
            i++;
        }
        Association type = e.getType();
        elementInfo.put("connectorTypeId", (type == null) ? null : type.getID());
        elementInfo.put("connectorKind", (e.getKind() == null) ? null : e.getKind().toString());
        return elementInfo;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillOperationSpecialization(Operation e, JSONObject elementInfo) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
        elementInfo.put("type", "Operation");
        List<Parameter> vsl = ((Operation) e).getOwnedParameter();
        if (vsl != null && vsl.size() > 0) 
            elementInfo.put("parametersId", makeJsonArrayOfIDs(vsl));
        else
            elementInfo.put("parametersId", new JSONArray());
        return elementInfo;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillParameterSpecialization(Parameter e, JSONObject elementInfo) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
        elementInfo.put("type", "Parameter");
               
        ParameterDirectionKind dir = e.getDirection() ;
        elementInfo.put("direction", (dir == null) ? null :dir.toString());
        
        Type type = e.getType();
        elementInfo.put("parameterTypeId", (type == null) ? null : type.getID());
            
        
        //ValueSpecification defaultValue = p.getDefaultValue();
        //if (defaultValue != null) {
        //    specialization.put("parameterDefaultValue",
         //           defaultValue.getID());
        // }
        return elementInfo;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillDirectedRelationshipSpecialization(DirectedRelationship e, JSONObject elementInfo) {
        if (elementInfo == null)
            elementInfo = new JSONObject();
        if (e instanceof Dependency) {
            if (StereotypesHelper.hasStereotype(e, "characterizes"))
                elementInfo.put("type", "Characterizes");
            else if (StereotypesHelper.hasStereotypeOrDerived(e,
                    DocGen3Profile.queriesStereotype))
                elementInfo.put("type", "Expose");
            else
                elementInfo.put("type", "Dependency");
        } else if (e instanceof Generalization) {
            Stereotype conforms = Utils.getSysML14ConformsStereotype();
            if (conforms != null && StereotypesHelper.hasStereotypeOrDerived(e, conforms))
                elementInfo.put("type", "Conform");
            else
                elementInfo.put("type", "Generalization");
        } else if ( e instanceof ProtocolConformance){ //StateMachine
            elementInfo.put("type", "ProtocolConformance");  
        } else {
            elementInfo.put("type", "DirectedRelationship");
        }
        Element client = ModelHelper.getClientElement(e);
        Element supplier = ModelHelper.getSupplierElement(e);
        // (client != null) //this shouldn't happen
        elementInfo.put("sourceId", (client == null) ? null : getElementID(client));
        //(supplier != null) //this shouldn't happen
        elementInfo.put("targetId", (supplier == null) ? null : getElementID(supplier));
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillName(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put("sysmlId", getElementID(e));
        }
        
        info.put("name", (e instanceof NamedElement) ? ((NamedElement)e).getName() : "");
        return info;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillDoc(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put("sysmlId", getElementID(e));
        }
        info.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(e)));
        return info;
    }
    
	@SuppressWarnings("unchecked")
	public static JSONObject fillOwnedAttribute(Element e, JSONObject einfo) {
		JSONObject info = einfo;
		if (info == null) {
			info = new JSONObject();
			info.put("sysmlId", getElementID(e));
		}
		
		JSONArray propIDs = new JSONArray();
		if (e instanceof Class) {
			for (Property prop: ((Class)e).getOwnedAttribute()) {
				propIDs.add(getElementID(prop));
			}
			info.put("ownedAttributeId", propIDs);
		}
		return info;
	}
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillOwner(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put("sysmlId", getElementID(e));
        }
        
        info.put("ownerId", (e.getOwner() == null) ? null :  getElementID(e.getOwner()));
        return info;
    }
    
    public static JSONObject fillId(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
        }
        info.put("sysmlId", getElementID(e));
        return info;
    }
    
    @SuppressWarnings("unchecked")
    public static JSONObject fillMetatype(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put("sysmlId", getElementID(e));
        }
        info.put("isMetatype", false);
        if (e instanceof Stereotype) {
            info.put("isMetatype", true);
            JSONArray metatypes = new JSONArray();
            for (Class c: ((Stereotype)e).getSuperClass()) {
                if (c instanceof Stereotype) {
                    metatypes.add(c.getID());
                }
            }
            for (Class c: StereotypesHelper.getBaseClasses((Stereotype)e)) {
                metatypes.add(c.getID());
            }
            info.put("metatypesId", metatypes);
        }
        if (e instanceof Class) {
            try {
                java.lang.Class c = StereotypesHelper.getClassOfMetaClass((Class)e);
                if (c != null) {
                    info.put("isMetatype", true);
                    info.put("metatypes", new JSONArray());
                }
            } catch (Exception ex) {}
        }
        List<Stereotype> stereotypes = StereotypesHelper.getStereotypes(e);
        JSONArray applied = new JSONArray();
        for (Stereotype s: stereotypes) {
            applied.add(s.getID());
        }
        Class baseClass = StereotypesHelper.getBaseClass(e);
        if (baseClass != null)
            applied.add(baseClass.getID());
           
        info.put("appliedMetatypesId", applied);
        return info;
    } 

}
