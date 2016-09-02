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
 * 
 * @author Johannes Gross
 ******************************************************************************/
package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;

public class EMFExporter {
	private JSONArray siblings;
	private EAttribute nameAtt;

	public EMFExporter(Element element) {
		super();
		siblings = new JSONArray();
		createdIDs = new ArrayList<String>();
		for (EStructuralFeature ef : element.eClass().getEAllStructuralFeatures()) {
			if (ef.getName().equals("ID")) {
				IDStructuralFeature = ef;
				break;
			}
		}
		nameAtt = UMLFactory.eINSTANCE.getUMLPackage().getNamedElement_Name();

	}

	public static Logger log = Logger.getLogger(EMFExporter.class);
	public static Map<String, Integer> mountedVersions;
	public static boolean baselineNotSet = false;
	public static Map<String, Map<String, String>> wsIdMapping = new HashMap<String, Map<String, String>>();
	public static Map<String, Map<String, String>> sites = new HashMap<String, Map<String, String>>();
	private static EStructuralFeature IDStructuralFeature;
	private static ArrayList<String> createdIDs;

	@SuppressWarnings("unchecked")
	public JSONObject fillElement(Element element) {
		// showElementMetaModel((NamedElement) element);
		JSONObject elementInfo = new JSONObject();
		if (element.eClass().getEAllStructuralFeatures().contains(IDStructuralFeature)) {
			String id = (String) element.eGet(IDStructuralFeature);
			CREATENODE: {

				if (createdIDs.contains(id)) {
					break CREATENODE;
				} else {
					createdIDs.add(id);
				}
				if (element.eClass().getName().equals("Slot")) {
					elementInfo.put("sysmlId", ((Slot) element).getOwner().getID() + "-slot-" + ((Slot) element).getDefiningFeature().getID());
				} else {
					elementInfo.put("sysmlId", id);
				}
				elementInfo.put("type", element.eClass().getName());
				if ("Connector".equals(element.eClass().getName())) {
					fillConnectorSpecialization((Connector) element, elementInfo);
				}
				for (EAttribute sf : element.eClass().getEAllAttributes()) {
					if (!sf.isDerived()) {
						if (sf != IDStructuralFeature) {
							// if (!sf.getName().contains("_")) {
							Object val = element.eGet(sf);
							if (val != null) {
								if (val instanceof EList) {
									if (!((EList<?>) val).isEmpty()) {
										elementInfo.put(sf.getName(), val);
									}
								} else if (val instanceof EObject) {
									elementInfo.put(sf.getName(), val);
								} else if (val instanceof Boolean) {
									elementInfo.put(sf.getName(), val);
								} else {
									if (!val.toString().contains("html")) {
										String escapedVal = StringEscapeUtils.escapeHtml(val.toString());
										elementInfo.put(sf.getName(), escapedVal);
									}
								}
							}
							// }
						}
					}
				}
			}
			ArrayList<EReference> references = new ArrayList<EReference>();
			EList<EReference> refs = element.eClass().getEAllReferences();
			references.addAll(refs);
			EList<EReference> conts = element.eClass().getEAllContainments();
			references.removeAll(conts);

			for (EReference ref : references) {
				if (!ref.isDerived() || ref.getName().equals("source") || ref.getName().equals("target")) {
					// if (!ref.getName().contains("_")) {
					Object val = element.eGet(ref);
					if (val != null) {
						if (val instanceof EList) {
							JSONArray array = new JSONArray();
							for (Object obs : ((EList) val)) {
								if (obs instanceof EObject)
									if (!((EList<?>) val).isEmpty()) {
										if (((EObject) obs).eClass().getEAllStructuralFeatures().contains(IDStructuralFeature)) {
											array.add(((EObject) obs).eGet(IDStructuralFeature));
										} else {
											System.out.println("Skipped: " + obs);
										}
									}
							}
							if (!array.isEmpty()) {
								if (ref.getName().equals("source"))
									elementInfo.put("sourceId", array.get(0));
								else if (ref.getName().equals("target"))
									elementInfo.put("targetId", array.get(0));
								else
									elementInfo.put(ref.getName() + "Ids", array);
							}
						} else if (val instanceof EObject) {
							if (((EObject) val).eClass().getEAllStructuralFeatures().contains(IDStructuralFeature)) {
								elementInfo.put(ref.getName() + "Id", ((EObject) val).eGet(IDStructuralFeature));
							} else {
								System.out.println("Skipped: " + val);
							}
							// }
						}
					}
				} else if ("owner".equals(ref.getName())) {
					Object val = element.eGet(ref);
					elementInfo.put(ref.getName() + "Id", ((EObject) val).eGet(IDStructuralFeature));
				}

			}
			for (EReference ref : conts) {
				// if (!ref.getName().contains("_") & !"ownedDiagram".equals(ref.getName())) {
				if (!"ownedDiagram".equals(ref.getName())) {
					Object val = element.eGet(ref);
					if (val != null) {
						if (val instanceof EList) {
							if (!((EList<?>) val).isEmpty()) {
								// if ("ownedAttribute".equals(ref.getName())) {
								Iterator it = ((EList) val).iterator();
								JSONArray childArray = new JSONArray();
								JSONArray idArray = new JSONArray();

								while (it.hasNext()) {
									EObject eo = (EObject) it.next();
									if (eo instanceof Element) {
										if (eo instanceof ValueSpecification) {
											if (!createdIDs.contains(((Element) eo).getID())) {
												childArray.add(fillElement((Element) eo));
												// fillElement((Element) eo);
												// array.add(((Element) eo).getID());
											}
										} else {
											if (!createdIDs.contains(((Element) eo).getID())) {
												siblings.add(fillElement((Element) eo));
												// fillElement((Element) eo);
												idArray.add(((Element) eo).getID());
											}
										}
									}
								}
								if (!idArray.isEmpty()) {
									elementInfo.put(ref.getName() + "Ids", idArray);
								}
								if (!childArray.isEmpty()) {
									elementInfo.put(ref.getName(), childArray);
								}
							}
						} else if (val instanceof EObject) {
							if (val instanceof Element) {
								if (val instanceof ValueSpecification) {
									if (!createdIDs.contains(((Element) val).getID())) {
										elementInfo.put(ref.getName(), fillElement((Element) val));
										// // fillElement((Element) eo);
										// // array.add(((Element) eo).getID());
									}
								} else {
									if (!createdIDs.contains(((Element) val).getID())) {
										siblings.add(fillElement((Element) val));
										// fillElement((Element) val);
										elementInfo.put(ref.getName() + "Id", ((Element) val).getID());
									}
								}
							}

						}
					}
				}
			}
		}
		elementInfo.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(element)));
		fillMetatype(element, elementInfo);
		return elementInfo;
	}

	public static String getElementID(Element e) {
		if (e == null) {
			return null;
		}
		if (e instanceof Slot) {
			if (e.getOwner() == null || ((Slot) e).getDefiningFeature() == null)
				return null;
			return e.getOwner().getID() + "-slot-" + ((Slot) e).getDefiningFeature().getID();
		} else if (e instanceof Model && e == Application.getInstance().getProject().getModel()) {
			return Application.getInstance().getProject().getPrimaryProject().getProjectID();
		}
		return e.getID();
	}

	@SuppressWarnings("unchecked")
	public static JSONObject fillConnectorSpecialization(Connector e, JSONObject elementInfo) {
		if (elementInfo == null)
			elementInfo = new JSONObject();
		elementInfo.put("type", "Connector");
		int i = 0;
		if (e.getEnd() == null)
			return elementInfo;
		for (ConnectorEnd end : e.getEnd()) {
			JSONArray propertyPath = new JSONArray();
			if (end.getRole() != null) {
				if (StereotypesHelper.hasStereotype(end, "NestedConnectorEnd")) {
					List<Element> ps = StereotypesHelper.getStereotypePropertyValue(end, "NestedConnectorEnd", "propertyPath");
					for (Element path : ps) {
						if (path instanceof ElementValue) {
							propertyPath.add(((ElementValue) path).getElement().getID());
						} else if (path instanceof Property)
							propertyPath.add(path.getID());
					}
				}
				propertyPath.add(end.getRole().getID());
			}
			if (i == 0) {
				// specialization.put("sourceUpper", fillValueSpecification(end.getUpperValue(), null));
				// specialization.put("sourceLower", fillValueSpecification(end.getLowerValue(), null));
				elementInfo.put("endAPathIds", propertyPath);
			} else {
				// specialization.put("targetUpper", fillValueSpecification(end.getUpperValue(), null));
				// specialization.put("targetLower", fillValueSpecification(end.getLowerValue(), null));
				elementInfo.put("endBPathIds", propertyPath);
			}
			i++;
		}
		Association type = e.getType();
		elementInfo.put("typeId", (type == null) ? null : type.getID());
		elementInfo.put("kind", (e.getKind() == null) ? null : e.getKind().toString());
		return elementInfo;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject fillMetatype(Element e, JSONObject einfo) {
		JSONObject info = einfo;
		if (info == null) {
			info = new JSONObject();
			info.put("sysmlId", getElementID(e));
		}
		// info.put("isMetatype", false);
		if (e instanceof Stereotype) {
			info.put("isMetatype", true);
			JSONArray metatypes = new JSONArray();
			for (Class c : ((Stereotype) e).getSuperClass()) {
				if (c instanceof Stereotype) {
					metatypes.add(c.getID());
				}
			}
			for (Class c : StereotypesHelper.getBaseClasses((Stereotype) e)) {
				metatypes.add(c.getID());
			}
			info.put("metatypesId", metatypes);
		}
		if (e instanceof Class) {
			try {
				java.lang.Class c = StereotypesHelper.getClassOfMetaClass((Class) e);
				if (c != null) {
					info.put("isMetatype", true);
					info.put("metatypes", new JSONArray());
				}
			} catch (Exception ex) {
			}
		}
		List<Stereotype> stereotypes = StereotypesHelper.getStereotypes(e);
		JSONArray applied = new JSONArray();
		for (Stereotype s : stereotypes) {
			applied.add(s.getID());
		}
		// Class baseClass = StereotypesHelper.getBaseClass(e);
		// if (baseClass != null)
		// applied.add(baseClass.getID());

		info.put("appliedStereotypeIds", applied);
		return info;
	}

	public JSONArray getSiblings() {
		return siblings;
	}

	public void showElementMetaModel(NamedElement element) {
		for (EAttribute sf : element.eClass().getEAllAttributes()) {
			System.out.println(element.eGet(nameAtt) + " " + sf.getName() + " " + sf.isDerived() + " " + sf.isChangeable() + " " + sf.isID() + " " + sf.isMany());

		}
		for (EReference sf : element.eClass().getEAllReferences()) {
			System.out.println(element.eGet(nameAtt) + " " + sf.getName() + " " + sf.isDerived() + " " + sf.isChangeable() + " " + sf.isContainment() + " " + sf.isContainer() + " " + sf.isMany());

		}
	}

}
