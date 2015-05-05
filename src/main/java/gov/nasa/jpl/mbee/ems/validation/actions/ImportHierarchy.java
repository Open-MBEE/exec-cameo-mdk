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
package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncCommitListener;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.ProjectListenerMapping;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.uml2.uml.AggregationKind;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

public class ImportHierarchy extends RuleViolationAction implements
AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element view;
    private JSONObject keyed;
    private JSONObject md;
    private Map<String, Object> tosend;
    
    public ImportHierarchy(Element e, JSONObject md, JSONObject keyed) {
        super("ImportHierarchy", "Import View Hierarchy", null, null);
        this.view = e;
        this.keyed = keyed;
        this.md = md;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        
    }
    
    @Override
    protected void doAfterSuccess() {
        if (tosend != null)
            sendChanges(tosend);
    }
    
    @Override
    protected boolean doAction(Annotation anno) throws ReadOnlyElementException {
        if (anno != null) {
            
        } else {
            Map<String, Object> result = importHierarchy(view, md, keyed);
            
            if ((Boolean)result.get("success")) {
                tosend = result;
                //List<Request> requests = sendChanges(result);
                //for (Request r: requests) {
                //    OutputQueue.getInstance().offer(r);
                //}
                return true;
            } else {
                Application.getInstance().getGUILog().log("[ERROR] Import hierarchy aborted because view hierarchy isn't editable. Lock it first.");
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        execute("Change Hierarchy");
    }

    @SuppressWarnings("unchecked")
    public static List<Request> sendChanges(Map<String, Object> results) {
        Set<Element> added = (Set<Element>)results.get("added");
        Set<Property> moved = (Set<Property>)results.get("moved");
        Set<Element> deleted = (Set<Element>)results.get("deleted");
        Set<Property> ptyped = (Set<Property>) results.get("ptyped");
        Set<String> deletedIds = (Set<String>)results.get("deletedIds");
        List<Request> returns = new ArrayList<Request>();
        JSONArray changes = new JSONArray();
        for (Element e: added) {
            changes.add(ExportUtility.fillElement(e, null));
        }
        for (Property p: moved) {
            changes.add(ExportUtility.fillOwner(p, null));
        }
        for (Property p: ptyped) {
            changes.add(ExportUtility.fillElement(p, null));
        }
        JSONObject tosend = new JSONObject();
        tosend.put("elements", changes);
        tosend.put("source", "magicdraw");
        String url = ExportUtility.getPostElementsUrl();
        if (!changes.isEmpty()) {
            Request r = new Request(url, tosend.toJSONString(), "POST", false, changes.size());
            OutputQueue.getInstance().offer(r);
        }
        if (!deletedIds.isEmpty()) {
            url = ExportUtility.getUrlWithWorkspace();
            JSONObject send = new JSONObject();
            JSONArray elements = new JSONArray();
            send.put("elements", elements);
            send.put("source", "magicdraw");
            for (String e: deletedIds) {
                JSONObject eo = new JSONObject();
                eo.put("sysmlid", e);
                elements.add(eo);
            }
            OutputQueue.getInstance().offer(new Request(url + "/elements", send.toJSONString(), "DELETEALL", false, elements.size()));
        }
        return returns;
    }
    
    private static boolean lock(Element e, boolean isTeamwork, Project project) {
        if (e == null)
            return true;
        if (!e.isEditable()) {
            return false;
            /*if (!ProjectUtilities.isElementInAttachedProject(e)) {
                if (isTeamwork) {
                    if (TeamworkUtils.getLoggedUserName() == null)
                        return false;
                    boolean recursive = false;
                    if (e instanceof Association)
                        recursive = true;
                    boolean hmm = TeamworkUtils.lockElement(project, e, recursive);
                    if (!hmm) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }*/
        }
        return true;
    }
    
    public static Map<String, Object> importHierarchy(Element document, JSONObject md, JSONObject keyed) throws ReadOnlyElementException {
        Project project = Application.getInstance().getProject();
        boolean isTeamwork = false;
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject()))
            isTeamwork = true;
        Map<String, Object> retval = new HashMap<String, Object>();
        retval.put("success", true);
        ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
        Stereotype viewS = Utils.getViewClassStereotype();
        //keep track of current models views with properties that are typed by them
        Map<String, List<Property>> viewId2props = new HashMap<String, List<Property>>();
        //curate all properties in current md doc model with type of view 
        Set<String> processedViews = new HashSet<String>();
        List<JSONObject> newviews = new ArrayList<JSONObject>();
        for (Object vid: md.keySet()) {
            String viewid = (String)vid;
            Element view = ExportUtility.getElementFromID(viewid);
            if (view != null && view instanceof Class) {
                if (!lock(view, isTeamwork, project)) {
                    retval.put("success", false);
                    return retval;
                }
                for (Property p: ((Class)view).getOwnedAttribute()) {
                    Type t = p.getType();
                    if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, viewS)) {
                        List<Property> viewprops = viewId2props.get(t.getID());
                        if (viewprops == null) {
                            viewprops = new ArrayList<Property>();
                            viewId2props.put(t.getID(), viewprops);
                        }
                        if (!lock(p, isTeamwork, project) || !lock(p.getAssociation(), isTeamwork, project) || !lock(t, isTeamwork, project)) {
                            retval.put("success", false);
                            return retval;
                        }
                        viewprops.add(p);
                    }
                }
                processedViews.add(view.getID());
            } 
        }
        //curate properties with type view from views that may not be connected in the model but is referenced on alfresco
        for (Object vid: keyed.keySet()) {
            String viewid = (String)vid;
            if (processedViews.contains(viewid))
                continue;
            Element view = ExportUtility.getElementFromID(viewid);
            if (view != null && view instanceof Class) {
                if (!lock(view, isTeamwork, project)) {
                    retval.put("success", false);
                    return retval;
                }
                for (Property p: ((Class)view).getOwnedAttribute()) {
                    Type t = p.getType();
                    if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, viewS)) {
                        List<Property> viewprops = viewId2props.get(t.getID());
                        if (viewprops == null) {
                            viewprops = new ArrayList<Property>();
                            viewId2props.put(t.getID(), viewprops);
                        }
                        if (!lock(p, isTeamwork, project) || !lock(p.getAssociation(), isTeamwork, project) || !lock(t, isTeamwork, project)) {
                            retval.put("success", false);
                            return retval;
                        }
                        viewprops.add(p);
                    }
                }
            } else {
                //try to create the view
                //Element newview = null;
                String url = ExportUtility.getUrlWithWorkspace();
                url += "/elements/" + viewid;
                String result = ExportUtility.get(url, false);
                if (result != null) {
                    JSONObject ob = (JSONObject)JSONValue.parse(result);
                    if (ob != null) {
                        JSONArray elements = (JSONArray)ob.get("elements");
                        if (elements != null && !elements.isEmpty()) {
                            JSONObject viewob = (JSONObject)elements.get(0);
                            newviews.add(viewob);
                        }
                    }
                }
            }
        }
        List<JSONObject> sortedNewviews = ImportUtility.getCreationOrder(newviews);
        if (sortedNewviews == null) {
            Application.getInstance().getGUILog().log("[ERROR] Creating new view(s) failed.");
            retval.put("success", false);
            return retval;
        }
        for (JSONObject ob: sortedNewviews) {
            Element newview = ImportUtility.createElement(ob, true);
            if (newview != null) {
                List<Property> viewprops = new ArrayList<Property>();
                viewId2props.put(newview.getID(), viewprops);
            } else {
                Application.getInstance().getGUILog().log("[ERROR] Creating new view(s) failed.");
                retval.put("success", false);
                return retval;
            }
        }
        Set<Property> moved = new HashSet<Property>();
        Set<Element> added = new HashSet<Element>();
        Set<Element> deleted = new HashSet<Element>();
        Set<Property> ptyped = new HashSet<Property>();
        for (Object vid: keyed.keySet()) { //go through all views on mms
            String viewid = (String)vid;
            JSONArray children = (JSONArray)keyed.get(vid);
            List<Property> cprops = new ArrayList<Property>(); //new owned attribute array for the parent view
            Element view = ExportUtility.getElementFromID(viewid);
            if (view != null && view instanceof Class) {
                for (Object cid: children) { //for all children views
                    String childId = (String)cid;
                    List<Property> availableProps = viewId2props.get(childId); 
                    //get a list of properties we can repurpose with type of the child view
                    if (availableProps == null || availableProps.isEmpty()) {
                        //no free property available, make one
                        Element cview = ExportUtility.getElementFromID(childId);
                        if (cview instanceof Type) {
                            Association association = ef.createAssociationInstance();
                            //ModelHelper.setSupplierElement(association, viewType);
                            Property propType1 = ModelHelper.getFirstMemberEnd(association);
                            propType1.setName(((NamedElement)cview).getName().toLowerCase());
                            propType1.setAggregation(AggregationKindEnum.COMPOSITE);
                            propType1.setType((Type)cview);
                            ModelHelper.setNavigable(propType1, true);
                            Stereotype partPropertyST = Utils.getStereotype("PartProperty");
                            StereotypesHelper.addStereotype(propType1, partPropertyST);
                            Property propType2 = ModelHelper.getSecondMemberEnd(association);
                            propType2.setType((Type)view);
                            propType2.setOwner(association);
                            //ModelHelper.setClientElement(association, document);
                            association.setOwner(document.getOwner());
                            cprops.add(propType1);
                            added.add(propType1);
                            added.add(propType2);
                            added.add(association);
                        }
                    } else {
                        Property p = availableProps.remove(0);
                        if (p.getOwner() != view) {
                            moved.add(p);
                            
                            Property opposite = getOpposite(p);//p.getOpposite();
                            if (opposite != null) {
                                opposite.setType((Type)view);
                                JSONObject ptype = new JSONObject();
                                ptype.put("sysmlid", opposite.getID());
                                JSONObject spec = new JSONObject();
                                spec.put("type", "Property");
                                spec.put("propertyType", view.getID());
                                ptype.put("specialization", spec);
                                ptyped.add(opposite);
                            }
                        }
                        //add the property to owned attribute array
                        cprops.add(p);
                    }
                }
                for (Property p: ((Class)view).getOwnedAttribute()) {
                    //keep any non view owned attribute ("regular" attribute)
                    if (p.getType() == null || !StereotypesHelper.hasStereotypeOrDerived(p.getType(), viewS)) {
                        cprops.add(p);
                    }
                }
                //
                ((Class)view).getOwnedAttribute().clear();
                ((Class)view).getOwnedAttribute().addAll(cprops);
            }
        }
        for (List<Property> props: viewId2props.values()) {
            for (Property p: props) {
                deleted.add(p);
                Association asso = p.getAssociation();
                if (asso != null) {
                    deleted.addAll(asso.getOwnedEnd());
                    deleted.add(asso);
                }
                ModelElementsManager.getInstance().removeElement(p);
            }
        }
        Set<String> deletedIds = new HashSet<String>();
        for (Element deletedE: deleted) {
            deletedIds.add(deletedE.getID());
        }
        retval.put("deleted", deleted);
        retval.put("deletedIds", deletedIds);
        retval.put("added", added);
        retval.put("moved", moved);
        retval.put("ptyped", ptyped);
        return retval;
    }
    
    public static Property getOpposite(Property p) {
        Association a = p.getAssociation();
        if (a != null) {
            for (NamedElement e: a.getMember()) {
                if (e instanceof Property && e != p)
                    return (Property)e;
            }
        }
        return null;
    }
}
