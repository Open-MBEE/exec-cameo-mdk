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
import gov.nasa.jpl.mbee.ems.sync.ProjectListenerMapping;
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
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.AggregationKind;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
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

    public ImportHierarchy(Element e, JSONObject md, JSONObject keyed) {
        super("ImportHierarchy", "Import Hierarchy", null, null);
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
        /*Collection<Annotation> toremove = new ArrayList<Annotation>();
        for (Annotation anno : annos) {
            Element e = (Element) anno.getTarget();
            if (importHierarchy(e)) {
                toremove.add(anno);
            }
        }
        if (!toremove.isEmpty()) {
            this.removeViolationsAndUpdateWindow(toremove);
        }*/
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = Application.getInstance().getProject();
        Map<String, ?> projectInstances = ProjectListenerMapping.getInstance().get(project);
        AutoSyncCommitListener listener = (AutoSyncCommitListener)projectInstances.get("AutoSyncCommitListener");
        if (listener != null)
            listener.disable();
        SessionManager.getInstance().createSession("Change Hierarchy");
        try {
            if (importHierarchy(view))
                this.removeViolationAndUpdateWindow();
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean importHierarchy(Element document) throws ReadOnlyElementException {	
        ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
        Stereotype viewS = Utils.getViewClassStereotype();
        Map<String, List<Property>> viewId2props = new HashMap<String, List<Property>>();
        //curate all properties in current model with type of view that's referenced on mms
        for (Object vid: keyed.keySet()) {
            String viewid = (String)vid;
            Element view = ExportUtility.getElementFromID(viewid);
            if (view != null && view instanceof Class) {
                for (Property p: ((Class)view).getOwnedAttribute()) {
                    Type t = p.getType();
                    if (keyed.keySet().contains(t.getID())) {
                        List<Property> viewprops = viewId2props.get(t.getID());
                        if (viewprops == null) {
                            viewprops = new ArrayList<Property>();
                            viewId2props.put(t.getID(), viewprops);
                        }
                        viewprops.add(p);
                    }
                }
            } else {
                //create the view
                
            }
        }
        for (Object vid: keyed.keySet()) {
            String viewid = (String)vid;
            JSONArray children = (JSONArray)keyed.get(vid);
            List<Property> cprops = new ArrayList<Property>();
            Element view = ExportUtility.getElementFromID(viewid);
            if (view != null && view instanceof Class) {
                for (Object cid: children) {
                    String childId = (String)cid;
                    List<Property> availableProps = viewId2props.get(childId);
                    if (availableProps == null || availableProps.isEmpty()) {
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
                        }
                    } else {
                        cprops.add(availableProps.remove(0));
                    }
                }
                for (Property p: ((Class)view).getOwnedAttribute()) {
                    if (p.getType() == null || !StereotypesHelper.hasStereotypeOrDerived(p.getType(), viewS)) {
                        cprops.add(p);
                    }
                }
                ((Class)view).getOwnedAttribute().clear();
                ((Class)view).getOwnedAttribute().addAll(cprops);
            }
        }
        for (List<Property> props: viewId2props.values()) {
            for (Property p: props) {
                ModelElementsManager.getInstance().removeElement(p);
            }
        }
        return true;





        /*
		// go through all web child elements
		for (String webChild : webChildrenObjectArray) {

			// is element in MagicDraw? 
			// Checking if element with MagicDraw ID exists is possible - but no guarantee of element being in the correct hierarchy
			// Comparing document child elements of Alfresco and MagicDraw
			boolean isElementInMagicDraw = false;
			for (String modelChild : modelChildrenObjectArray) {
				if(webChild.equals(modelChild)){
					isElementInMagicDraw = true;
					break;
				}
			}

			// add element to MagicDraw if necessary 							
			Element viewType = null;
			if(isElementInMagicDraw){
				continue;
			}					
			else{

				// check if Magicdraw View Class exists
				boolean magicDrawViewClassExists = false;
				viewType = (Element) project.getElementByID((String) webChild);
				if(viewType != null){
					magicDrawViewClassExists = true;
				}

				// create Magicdraw View Class if it doesn't exist
				if(!magicDrawViewClassExists){

					// get JSON of Alfresco element				
					String elementUrl = "https://ems-stg.jpl.nasa.gov/alfresco/service/workspaces/master/elements/" + webChild;		
					String elementResponse = ExportUtility.get(elementUrl, false);
					if (docresponse == null)
						return false;
					JSONObject elementJSONResponse = (JSONObject) JSONValue.parse(elementResponse);
					JSONArray elementJSONArray = (JSONArray) elementJSONResponse.get("elements");					
					JSONObject elementJSONObject = (JSONObject) elementJSONArray.get(0);

					// create new MagicDraw view class
					com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class newClass = ef.createClassInstance();

					// set class name
					String elementName = (String)elementJSONObject.get("name");
					newClass.setName(elementName);

					// place class under the same owner as document view					
					Element owner = ExportUtility.getElementFromID(view.getOwner().getID());
					newClass.setOwner(owner);

					// add view stereotype
					Stereotype sysmlView = Utils.getViewClassStereotype();
					StereotypesHelper.addStereotype(newClass, sysmlView);

					viewType = newClass;

				}

				// define association and part property				
		        Association association = ef.createAssociationInstance();

		        ModelHelper.setSupplierElement(association, viewType);
		        Property propType1 = 
				        ModelHelper.getFirstMemberEnd(association);
				        propType1.setName(((NamedElement)viewType).getName().toLowerCase());
				        propType1.setAggregation(AggregationKindEnum.COMPOSITE);		       
				        ModelHelper.setNavigable(propType1, true);
				Stereotype partPropertyST = Utils.getStereotype("PartProperty");
				StereotypesHelper.addStereotype(propType1, partPropertyST);
				propType1.setOwner(document);

		        ModelHelper.setClientElement(association, document);


		        // set id of part property identical to Alfresco element
				// class can have any id. 
//		        propType1.setID((String) webChild);


		        // association owner
		        association.setOwner(document.getOwner());

				// go recursively through all children elements ?


			}
		}


		// go through all model child elements
		// if MagicDraw element not in Alfresco, delete it in MagicDraw







		return true;*/

    }
}
