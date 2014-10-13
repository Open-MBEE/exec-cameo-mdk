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
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.uml2.uml.AggregationKind;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

public class ImportHierarchy extends RuleViolationAction implements
		AnnotationAction, IRuleViolationAction {
	private static final long serialVersionUID = 1L;
	private Element view;
	private ViewHierarchyVisitor vhv;

	public ImportHierarchy(Element e, ViewHierarchyVisitor vhv) {
		// JJS--MDEV-567 fix: changed 'Export' to 'Commit'
		//
		super("ImportHierarchy", "Import Hierarchy", null, null);
		this.view = e;
		this.vhv = vhv;
	}

	@Override
	public boolean canExecute(Collection<Annotation> arg0) {
		return true;
	}

	@Override
	public void execute(Collection<Annotation> annos) {
		Collection<Annotation> toremove = new ArrayList<Annotation>();
		for (Annotation anno : annos) {
			Element e = (Element) anno.getTarget();
			if (importHierarchy(e)) {
				toremove.add(anno);
			}
		}
		if (!toremove.isEmpty()) {
			this.removeViolationsAndUpdateWindow(toremove);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (importHierarchy(view)) {
			this.removeViolationAndUpdateWindow();
		}
	}

	@SuppressWarnings("unchecked")
	private boolean importHierarchy(Element document) {
		// get JSON of MagicDraw
		JSONObject hierarchy = vhv.getView2View(); 

		// get JSON of Alfresco
		String url = ExportUtility.getUrl();
		url += "/javawebscripts/products/" + document.getID();
		String docresponse = ExportUtility.get(url, false);
		if (docresponse == null)
			return false;
		JSONObject docResponse = (JSONObject) JSONValue.parse(docresponse);
		JSONArray docs = (JSONArray) docResponse.get("products");
		JSONObject keyed = null;
		for (Object docresult : docs) {
			if (((JSONObject) docresult).get("sysmlid").equals(document.getID())) {
				JSONArray view2view = (JSONArray) ((JSONObject) ((JSONObject) docresult)
						.get("specialization")).get("view2view");
				if (view2view == null)
					return false;
				keyed = ExportUtility.keyView2View(view2view);
				break;
			}
		}
		if (keyed == null) {
			return false;
		}
				
		// compare both JSON representations
		JSONArray webChildrenArray = (JSONArray) keyed.get(document.getID());
		JSONArray modelChildrenArray = (JSONArray) hierarchy.get(document.getID());	
		
		Object webChildrenObject = keyed.get(document.getID()); 		// can cast to JSONArray!!
		Object modelChildrenObject = hierarchy.get(document.getID());	
		
		String webChildrenString = webChildrenObject.toString();
		webChildrenString = webChildrenString.replace("[", "");
		webChildrenString = webChildrenString.replace("]", "");
		webChildrenString = webChildrenString.replace("\"", "");
		
		String modelChildrenString = modelChildrenObject.toString();
		modelChildrenString = modelChildrenString.replace("[", "");
		modelChildrenString = modelChildrenString.replace("]", "");
		modelChildrenString = modelChildrenString.replace("\"", "");
		
		String[] webChildrenObjectArray = webChildrenString.split(",");
		String[] modelChildrenObjectArray = modelChildrenString.split(",");
		
		if(!(webChildrenArray != null & modelChildrenArray != null)){
			return false;
		}
		
		// set up key MagicDraw objects
		Project project = Application.getInstance().getProject();  		
        ElementsFactory ef = project.getElementsFactory();
        project.getCounter().setCanResetIDForObject(true);
		
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
		
		

			

		
		
		return true;

	}
}
