package gov.nasa.jpl.mbee.ems;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.VisibilityKind;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;

public class EMFImporter {

	private JSONObject head;
	private HashMap<String, EObject> createdElements;
	private ArrayList<EObject> notContained;
	private Package owningPackage;

	public EMFImporter(Element element) {
		Element owner = element;
		while (!(owner instanceof Package)) {
			owner = owner.getOwner();
		}
		owningPackage = (Package) owner;
		createdElements = new HashMap<String, EObject>();
		notContained = new ArrayList<EObject>();
		JSONParser parsier = new JSONParser();
		try {
			head = (JSONObject) parsier.parse(new FileReader("/Users/johannes/Documents/projects/SECAE/HybridSUVJSONemf2.json"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public void createElementsFromJSON() { // This can be speed up by preselecting a couple of the most present eClasses and switch through them.
		// UMLFactory.eINSTANCE.create(null);
		// InstanceSpecification is = UMLFactory.eINSTANCE.createInstanceSpecification();
		// EList<EAttribute> list = is.eClass().getEAllAttributes();
		// System.out.println(list.size());
		// for (EAttribute ea : list) {
		// System.out.println(ea.getName());
		// EDataType type = (ea).getEAttributeType();
		// if (type instanceof EEnum) {
		// System.out.println(type + " is an EEnum");
		// } else {
		// System.out.println(type + " is not an EEnum ");
		// }
		// }
		JSONArray ja = (JSONArray) head.get("elements");
		for (Object je : ja) {
			if (je instanceof JSONObject) {
				// System.out.println(((JSONObject) je).get("type"));
				Object typename = ((JSONObject) je).get("type");
				if (!typename.toString().equals("PackageImport")) { // TODO whats wrong with PackageImports?
					EClassifier etype = UMLPackage.eINSTANCE.getEClassifier(typename.toString());
					if (etype instanceof EClass) {
						EClass eclass = (EClass) etype;
						EObject newobject = UMLFactory.eINSTANCE.create((EClass) etype);

						createdElements.put((String) ((JSONObject) je).get("sysmlId"), newobject);
						for (Object property : ((JSONObject) je).keySet()) {
							EStructuralFeature sf = eclass.getEStructuralFeature(property.toString());
							if (sf != null) {
								if (sf instanceof EAttribute) {
									if (sf.isChangeable()) {

										EDataType type = ((EAttribute) sf).getEAttributeType();
										if (type instanceof EEnum) {
											System.out.println(type.getName() + type.getInstanceClassName() + type.getInstanceTypeName());
											// System.out.println("Setting " + sf.getName() + " in " + typename.toString() + " to " + ((JSONObject) je).get(property));
											Enumerator literal = ((EEnum) type).getEEnumLiteral((String) ((JSONObject) je).get(property)).getInstance();
											System.out.println(literal.getName());
											newobject.eSet(sf, literal);
											if (literal.getName().contains("public")) {
												UMLPackage umlPackage = UMLFactory.eINSTANCE.getUMLPackage();
												VisibilityKind visibilityKind = com.nomagic.uml2.ext.magicdraw.classes.mdkernel.VisibilityKindEnum.PUBLIC;
												System.out.println(literal.toString() + " equals " + visibilityKind.toString() + " = " + literal.equals(visibilityKind));
											}
										} else if ("visibility".equals(sf.getName())) {
											// System.out.println(type.getName() + type.getInstanceClassName() + type.getInstanceTypeName());
											UMLPackage umlPackage = UMLFactory.eINSTANCE.getUMLPackage();
											VisibilityKind visibilityKind = com.nomagic.uml2.ext.magicdraw.classes.mdkernel.VisibilityKindEnum.PACKAGE;

											// newobject.eSet(umlPackage.getPackageableElement_Visibility(), visibilityKind);
											newobject.eSet(umlPackage.getNamedElement_Visibility(), visibilityKind);
											// ((RefFeatured) newobject).refSetValue(PropertyNames.VISIBILITY, visibilityKind);
										} else {
											newobject.eSet(sf, ((JSONObject) je).get(property));
											System.out.print(".");
										}
									}
								}

							} else {
								System.out.println("null attribute " + property.toString() + " in a " + typename.toString());
							}
						}
					}
				}
			}
		}

		JSONArray ja1 = (JSONArray) head.get("elements");
		for (Object je : ja1) {
			if (je instanceof JSONObject) {
				// System.out.println(((JSONObject) je).get("type"));
				Object typename = ((JSONObject) je).get("type");
				if (!typename.toString().equals("PackageImport")) { // TODO whats wrong with PackageImports?
					EClassifier etype = UMLPackage.eINSTANCE.getEClassifier(typename.toString());
					if (etype instanceof EClass) {
						EClass eclass = (EClass) etype;
						EObject editedObject = createdElements.get((String) ((JSONObject) je).get("sysmlId"));
						for (Object property : ((JSONObject) je).keySet()) {
							String propName = "";
							if (property.toString().contains("Ids")) {
								propName = property.toString().substring(0, property.toString().length() - 3);
							} else {
								propName = property.toString().substring(0, property.toString().length() - 2);
							}
							EStructuralFeature sf = eclass.getEStructuralFeature(propName);
							if (sf != null) {
								if (sf instanceof EReference) {
									if (sf.isChangeable()) {
										if (!sf.isMany()) {
											if (!((EReference) sf).isContainment()) {
												// EClass type = ((EReference) sf).getEReferenceType();
												EObject referencedObject = createdElements.get(((JSONObject) je).get(property));
												if (referencedObject != null) {
													editedObject.eSet(sf, referencedObject);
												}
											}
										} else {

										}
									}
								}
							}
						}
					}
				}
			}
		}

		findOwners();

	}

	public void addReferencesToElements() {

	}

	public void findOwners() {
		EReference ownerRef = UMLFactory.eINSTANCE.getUMLPackage().getElement_Owner();
		for (EObject el : createdElements.values()) {
			if (!createdElements.keySet().contains(el.eGet(ownerRef))) {
				notContained.add(el);
			}
		}
		SessionManager.getInstance().createSession("importing element");
		for (EObject el : notContained) {
			if (el instanceof PackageableElement) {
				owningPackage.getPackagedElement().add((PackageableElement) el);
			}
		}
		SessionManager.getInstance().closeSession();
	}
}
