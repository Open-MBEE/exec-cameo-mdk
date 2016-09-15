package gov.nasa.jpl.mbee.ems;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class EMFImporter implements Function<JSONObject, Element> {

    // TODO Remove me @donbot
    private JSONObject head;
    private HashMap<String, EObject> createdElements;
    private ArrayList<EObject> notContained;
    private Package owningPackage;
    private Project project;

    private static final EAttribute NAMED_ELEMENT_NAME = UMLFactory.eINSTANCE.getUMLPackage().getNamedElement_Name();
    private static final EReference DEPENDENCY_SUPPLIER = UMLFactory.eINSTANCE.getUMLPackage().getDependency_Supplier();
    private static final EReference DEPENDENCY_CLIENT = UMLFactory.eINSTANCE.getUMLPackage().getDependency_Client();
    private static final EReference ELEMENT_OWNER = UMLFactory.eINSTANCE.getUMLPackage().getElement_Owner();
    private static final EClass VALUE_SPECIFICATION = UMLFactory.eINSTANCE.getUMLPackage().getValueSpecification();
    private static final EClass LITERAL_SPECIFICATION = UMLFactory.eINSTANCE.getUMLPackage().getLiteralSpecification();

    @Override
    public Element apply(JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    private static void contextExample() {
        Constraint constraint = UMLFactory.eINSTANCE.createConstraint();
        Class classCont = UMLFactory.eINSTANCE.createClass();
        classCont.setName("Hallo");
        EReference sf = UMLFactory.eINSTANCE.getUMLPackage().getConstraint_Context();
        constraint.eSet(sf, classCont);

        // constraint.setContext(classCont);

        System.out.println(constraint.getContext().getName());
    }

    public void createElementsFromJSON() {
        // This can be speed up by preselecting a couple of the most present eClasses and switch through them.

        SessionManager.getInstance().createSession("importing elements");
        UMLFactory.eINSTANCE.setRepository(project.getRepository());
        project.getCounter().setCanResetIDForObject(true);
        try {
            contextExample();
            JSONArray jsonArrayElements = (JSONArray) head.get("elements");
            createElementsAndSetAttributes(jsonArrayElements);
            setEReferencesForElements(jsonArrayElements);
            findOwners();
            // checkAllElements();
            removeBrokenElements();

        } finally

        {
            UMLFactory.eINSTANCE.setRepository(null);
        }
        SessionManager.getInstance().closeSession();

    }

    private void createElementsAndSetAttributes(JSONArray jsonArrayElements) {
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
                                                System.out.println("	Setting " + editedObject.eGet(NAMED_ELEMENT_NAME) + " " + sf.getName() + " to " + jso.toString());
                                            }
                                        }
                                        else {
                                            List list = (List) editedObject.eGet(sf);
                                            for (Object arrayElement : ((JSONArray) ((JSONObject) jsonElement).get(property))) {

                                                list.add(arrayElement);
                                                if (editedObject instanceof NamedElement) {
                                                    System.out.println("	Adding " + editedObject.eGet(NAMED_ELEMENT_NAME) + " " + sf.getName() + " to " + arrayElement.toString());
                                                }
                                            }

                                        }
                                    }
                                }

                            }
                            else {
                                // System.out.println("null attribute " + property.toString() + " in a " + typename.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    private void setEReferencesForElements(JSONArray jsonArrayElements) {

        for (Object editedJSONObject : jsonArrayElements) {
            if (editedJSONObject instanceof JSONObject) {
                EObject editedObject = createdElements.get(((JSONObject) editedJSONObject).get("sysmlId"));

                setReferencesForObject((JSONObject) editedJSONObject, editedObject);
            }
        }
    }

    private void setReferencesForObject(JSONObject editedJSONObject, EObject editedObject) {
        Object typename = editedJSONObject.get("type");
        System.out.println("Editing a " + typename + "  ID: " + editedJSONObject.get("sysmlId"));
        if (!typename.toString().equals("PackageImport")) { // TODO what's wrong with PackageImports?
            EClassifier etype = UMLPackage.eINSTANCE.getEClassifier(typename.toString());
            if (etype instanceof EClass) {
                EClass eclass = (EClass) etype;
                AddLinks:
                for (Object property : editedJSONObject.keySet()) {
                    String propName = property.toString();
                    if (propName.contains("Ids")) {
                        propName = propName.substring(0, property.toString().length() - 3);
                    }
                    else if (propName.contains("Id")) {
                        propName = propName.substring(0, property.toString().length() - 2);
                    }
                    EStructuralFeature sf = eclass.getEStructuralFeature(propName);
                    if (sf != null) {
                        if (sf instanceof EReference) {
                            if (sf.isChangeable()) {
                                if (!sf.isDerived()) {
                                    if (!sf.isMany()) {
                                        if (((EReference) sf).getEReferenceType() == VALUE_SPECIFICATION) {
                                            Object vs = editedJSONObject.get(property.toString());
                                            if (vs instanceof JSONObject) {
                                                Object specificationTypeName = ((JSONObject) vs).get("type");
                                                EClassifier specificationType = UMLPackage.eINSTANCE.getEClassifier(specificationTypeName.toString());
                                                EObject editedObject1 = UMLFactory.eINSTANCE.create((EClass) specificationType);
                                                editedObject.eSet(sf, editedObject1);
                                                setReferencesForObject(editedJSONObject, editedObject1);
                                                System.out.println("Its a " + specificationType.getName());

                                                // for (Object property2 : ((JSONObject) vs).keySet()) {
                                                // EStructuralFeature sf1 = eclass.getEStructuralFeature(property2.toString());}
                                                // switch (specificationType.getName()) {
                                                // case "LiteralString":
                                                // break;
                                                // case "Expression":
                                                // System.out.println("");
                                                // break;
                                                // default:
                                                // break;
                                                // }
                                            }
                                            // TODO Handle special impl. and check might fail, is the type no == valuespec
                                            // if (((EReference) sf).getEReferenceType() == LITERAL_SPECIFICATION) {
                                            // EClassifier literalType = UMLPackage.eINSTANCE.getEClassifier(((EReference) sf).getEReferenceType().getName());
                                            // }
                                        }
                                        else {
                                            EObject referencedObject = createdElements.get(editedJSONObject.get(property));
                                            if (referencedObject == null) {
                                                referencedObject = (EObject) project.getElementByID((String) editedJSONObject.get(property));
                                            }
                                            if (referencedObject != null) {
                                                if (editedObject instanceof NamedElement && referencedObject instanceof NamedElement) {
                                                    System.out.println("	Setting " + editedObject.eGet(NAMED_ELEMENT_NAME) + " " + sf.getName() + " to " + referencedObject.eGet(NAMED_ELEMENT_NAME) + " ID: "
                                                            + editedJSONObject.get(property));
                                                }
                                                try {
                                                    editedObject.eSet(sf, referencedObject);
                                                } catch (NullPointerException e) {
                                                    // createdElements.remove((String) ((JSONObject) je).get("sysmlId"));
                                                    // ((Element) editedObject).dispose();
                                                    System.out.println("	SING Not set " + sf.getName() + " due to NPE.");
                                                }
                                            }
                                            else {
                                                System.out.println("	SING Not found " + sf.getName() + " with id: " + editedJSONObject.get(property));
                                                if (sf.isRequired()) {
                                                    System.out.println("	Grande Misere, because we need it! Deleting Element.");
                                                    createdElements.remove(editedJSONObject.get("sysmlId"));
                                                    ((Element) editedObject).dispose();
                                                    break AddLinks;
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        for (Object propid : ((JSONArray) editedJSONObject.get(property))) {
                                            EObject referencedObject = createdElements.get(propid);
                                            if (referencedObject == null) {
                                                referencedObject = (EObject) project.getElementByID((String) propid);
                                            }
                                            if (referencedObject != null) {

                                                List list = (List) editedObject.eGet(sf);
                                                if (!list.contains(referencedObject)) {
                                                    list.add(referencedObject);
                                                    if (editedObject instanceof NamedElement && referencedObject instanceof NamedElement) {
                                                        System.out.println(
                                                                "	Adding to " + editedObject.eGet(NAMED_ELEMENT_NAME) + " " + sf.getName() + " element: " + referencedObject.eGet(NAMED_ELEMENT_NAME) + " ID: " + propid);
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
                                            }
                                            else {
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
        }
        else {
            System.out.println("PackageImports are not dealt with yet.");
        }
    }

    private void removeBrokenElements() {
        for (EObject createdElement : createdElements.values()) {
            if (createdElement instanceof Dependency) {
                if (((Dependency) createdElement).isInvalid()) {
                    System.out.println("This one also checksout invalid." + ((MDObject) createdElement).getID());
                }
                if (!((Dependency) createdElement).hasClient() | !((Dependency) createdElement).hasSupplier()) {
                    System.out.println("Deleting dep: " + ((MDObject) createdElement).getID());
                    ((Dependency) createdElement).dispose();
                }
            }
            else if (createdElement instanceof Association) {
                if (!((Association) createdElement).hasMemberEnd()) {
                    System.out.println("Deleting association: " + ((MDObject) createdElement).getID());
                    // ((Association) createdElement).dispose();
                }
            }
        }
    }

    private void checkAllElements() {
        for (EObject createdElement : createdElements.values()) {
            for (EStructuralFeature sf : createdElement.eClass().getEAllReferences()) {
                if (sf.isRequired()) {
                    if (!createdElement.eIsSet(sf)) {
                        if (createdElement instanceof NamedElement && createdElement instanceof MDObject) {
                            System.out.println("Caution: " + createdElement.eGet(NAMED_ELEMENT_NAME) + " ID: " + ((MDObject) createdElement).getID() + "  reference " + sf.getName() + " is not set.");
                        }
                        else {
                            System.out.println("This should not happen, could you take a look please?");
                        }
                    }
                }
            }
        }

    }

    private void clearPrePopulatedLists(EObject editedObject) {
        for (EReference er : editedObject.eClass().getEAllReferences()) {
            if (er.isMany()) {
                if (!((List) editedObject.eGet(er)).isEmpty()) {
                    System.out.println("Cleared " + editedObject.eClass().getName() + "  " + er.getName() + " is not empty");
                    ((List) editedObject.eGet(er)).clear();
                }
            }
            else {
                Object elf = editedObject.eGet(er);
                if (elf != null) {
                    if (!elf.toString().isEmpty()) {
                        System.out.println("Elf " + er.getName() + " " + elf);
                    }
                }
            }
        }

    }

    public void findOwners() {
        for (EObject el : createdElements.values()) {
            // System.out.println(el.eGet(NAMED_ELEMENT_NAME));
            if (!createdElements.values().contains(el.eGet(ELEMENT_OWNER))) {
                notContained.add(el);
            }
        }
        for (EObject el : notContained) {
            if (el instanceof PackageableElement) {
                System.out.println("Adding " + el.eGet(NAMED_ELEMENT_NAME) + " to the owning package.");
                owningPackage.getPackagedElement().add((PackageableElement) el);
            }
        }
    }
}
