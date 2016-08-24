package gov.nasa.jpl.mbee.ems;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;

public class EMFImporter {

	private JSONObject head;
	private HashMap<String, EObject> createdElements;
	private ArrayList<EObject> notContained;
	private Package owningPackage;
	private EReference ownerRef;
	private EAttribute nameAtt;
	private Project project;

	public EMFImporter(Element element) {
		Element owner = element;
		while (!(owner instanceof Package)) {
			owner = owner.getOwner();
		}
		project = Application.getInstance().getProject();
		owningPackage = (Package) owner;
		createdElements = new HashMap<String, EObject>();
		notContained = new ArrayList<EObject>();
		JSONParser parsier = new JSONParser();
		ownerRef = UMLFactory.eINSTANCE.getUMLPackage().getElement_Owner();
		nameAtt = UMLFactory.eINSTANCE.getUMLPackage().getNamedElement_Name();
		try {
			head = (JSONObject) parsier.parse(new FileReader("/Users/johannes/Documents/projects/SECAE/compareImports/test04.json"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public void createElementsFromJSON() {
		// This can be speed up by preselecting a couple of the most present eClasses and switch through them.

		SessionManager.getInstance().createSession("importing elements");
		UMLFactory.eINSTANCE.setRepository(project.getRepository());
		project.getCounter().setCanResetIDForObject(true);
		try {

			JSONArray jsonArrayElements = (JSONArray) head.get("elements");
			for (Object jsonElement : jsonArrayElements) {
				if (jsonElement instanceof JSONObject) {
					// System.out.println(((JSONObject) je).get("type"));
					Object typename = ((JSONObject) jsonElement).get("type");
					if (!typename.toString().equals("PackageImport")) { // TODO whats wrong with PackageImports?
						// System.out.println("PI");
						// }
						EClassifier etype = UMLPackage.eINSTANCE.getEClassifier(typename.toString());
						if (etype instanceof EClass) {
							EClass eclass = (EClass) etype;
							EObject editedObject = UMLFactory.eINSTANCE.create((EClass) etype);

							clearPrePopulatedLists(editedObject);
							System.out.print("Created new " + etype.getName());
							if (editedObject instanceof MDObject) {
								if (project.getElementByID((String) ((JSONObject) jsonElement).get("sysmlId")) == null) { // If it exists, create new ID.
									((MDObject) editedObject).setID((String) ((JSONObject) jsonElement).get("sysmlId"));
									System.out.print(" with its old ID.   " + ((JSONObject) jsonElement).get("sysmlId"));
								}
							}
							createdElements.put((String) ((JSONObject) jsonElement).get("sysmlId"), editedObject);
							System.out.println("");
							for (Object property : ((JSONObject) jsonElement).keySet()) {
								EStructuralFeature sf = eclass.getEStructuralFeature(property.toString());
								if (sf != null) {
									if (sf instanceof EAttribute) {
										if (sf.isChangeable()) {
											EDataType type = ((EAttribute) sf).getEAttributeType();
											if (!sf.isMany()) {
												Object jso = ((JSONObject) jsonElement).get(property);
												editedObject.eSet(sf, EcoreUtil.createFromString(type, jso.toString()));
												if (editedObject instanceof NamedElement) {
													System.out.println("	Setting " + editedObject.eGet(nameAtt) + " " + sf.getName() + " to " + jso.toString());
												}
											} else {
												List list = (List) editedObject.eGet(sf);
												for (Object arrayElement : ((JSONArray) ((JSONObject) jsonElement).get(property))) {

													list.add(arrayElement);
													if (editedObject instanceof NamedElement) {
														System.out.println("	Adding " + editedObject.eGet(nameAtt) + " " + sf.getName() + " to " + arrayElement.toString());
													}
												}

											}
										}
									}

								} else {
									// System.out.println("null attribute " + property.toString() + " in a " + typename.toString());
								}
							}
						}
					}
				}
			}

			for (Object editedJSONObject : jsonArrayElements) {
				if (editedJSONObject instanceof JSONObject) {
					Object typename = ((JSONObject) editedJSONObject).get("type");
					System.out.println("Editing a " + typename + "  ID: " + ((JSONObject) editedJSONObject).get("sysmlId"));
					if (!typename.toString().equals("PackageImport")) { // TODO what's wrong with PackageImports?
						EClassifier etype = UMLPackage.eINSTANCE.getEClassifier(typename.toString());
						if (etype instanceof EClass) {
							EClass eclass = (EClass) etype;
							EObject editedObject = createdElements.get((String) ((JSONObject) editedJSONObject).get("sysmlId"));
							AddLinks: for (Object property : ((JSONObject) editedJSONObject).keySet()) {
								String propName = property.toString();
								if (propName.contains("Ids")) {
									propName = propName.substring(0, property.toString().length() - 3);
								} else if (propName.contains("Id")) {
									propName = propName.substring(0, property.toString().length() - 2);
								}
								EStructuralFeature sf = eclass.getEStructuralFeature(propName);
								if (sf != null) {
									if (sf instanceof EReference) {
										if (sf.isChangeable()) {
											if (!sf.isDerived()) {
												if (!sf.isMany()) {
													EObject referencedObject = createdElements.get(((JSONObject) editedJSONObject).get(property));
													if (referencedObject == null) {
														referencedObject = (EObject) project.getElementByID((String) ((JSONObject) editedJSONObject).get(property));
													}
													if (referencedObject != null) {
														if (editedObject instanceof NamedElement && referencedObject instanceof NamedElement) {
															System.out.println("	Setting " + editedObject.eGet(nameAtt) + " " + sf.getName() + " to " + referencedObject.eGet(nameAtt) + " ID: "
																	+ ((JSONObject) editedJSONObject).get(property));
														}
														try {
															editedObject.eSet(sf, referencedObject);
														} catch (NullPointerException e) {
															// createdElements.remove((String) ((JSONObject) je).get("sysmlId"));
															// ((Element) editedObject).dispose();
															System.out.println("	SING Not set " + sf.getName() + " due to NPE.");
														}
													} else {
														System.out.println("	SING Not found " + sf.getName() + " with id: " + ((JSONObject) editedJSONObject).get(property));
														if (sf.isRequired()) {
															System.out.println("	Grande Misere, because we need it! Deleting Element.");
															createdElements.remove((String) ((JSONObject) editedJSONObject).get("sysmlId"));
															((Element) editedObject).dispose();
															break AddLinks;
														}
													}

												} else {
													for (Object propid : ((JSONArray) ((JSONObject) editedJSONObject).get(property))) {
														EObject referencedObject = createdElements.get(propid);
														if (referencedObject == null) {
															referencedObject = (EObject) project.getElementByID((String) propid);
														}
														if (referencedObject != null) {

															List list = (List) editedObject.eGet(sf);
															if (!list.contains(referencedObject)) {
																list.add(referencedObject);
																if (editedObject instanceof NamedElement && referencedObject instanceof NamedElement) {
																	System.out.println("	Adding to " + editedObject.eGet(nameAtt) + " " + sf.getName() + " element: "
																			+ referencedObject.eGet(nameAtt) + " ID: " + propid);
																	EAnnotation ea = sf.getEAnnotation("subsets");
																	if (ea != null) {
																		for (EObject first : ea.getReferences()) {
																			if (first instanceof ENamedElement) {
																				System.out.println("			" + sf.getName() + " subsets " + ((ENamedElement) first).getName());
																			}
																		}
																	}
																}
															}
														} else {
															System.out.println("	MANY Not found " + sf.getName() + " with id " + propid);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					} else {
						System.out.println("PackageImports are not dealt with yet.");
					}
				}
			}
			findOwners();
		} finally

		{
			UMLFactory.eINSTANCE.setRepository(null);
		}
		SessionManager.getInstance().closeSession();

	}

	private void clearPrePopulatedLists(EObject editedObject) {
		for (EReference er : editedObject.eClass().getEAllReferences()) {
			if (er.isMany()) {
				if (!((List) editedObject.eGet(er)).isEmpty()) {
					System.out.println("Cleared " + editedObject.eClass().getName() + "  " + er.getName() + " is not empty");
					((List) editedObject.eGet(er)).clear();
				}
			} else {
				Object elf = editedObject.eGet(er);
				if (elf != null) {
					if (!elf.toString().isEmpty()) {
						System.out.println("Elf " + er.getName() + " " + elf);
					}
				}
			}
		}

	}

	public void addReferencesToElements() {

	}

	public void findOwners() {
		for (EObject el : createdElements.values()) {
			// System.out.println(el.eGet(nameAtt));
			if (!createdElements.values().contains(el.eGet(ownerRef))) {
				notContained.add(el);
			}
		}
		for (EObject el : notContained) {
			if (el instanceof PackageableElement) {
				System.out.println("Adding " + el.eGet(nameAtt) + " to the owning package.");
				owningPackage.getPackagedElement().add((PackageableElement) el);
			}
		}
	}
}
