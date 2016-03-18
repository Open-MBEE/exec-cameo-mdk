package gov.nasa.jpl.mbee.ems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import gov.nasa.jpl.graphs.DirectedEdgeVector;
import gov.nasa.jpl.graphs.DirectedGraphHashSet;
import gov.nasa.jpl.graphs.algorithms.TopologicalSort;
import gov.nasa.jpl.mbee.ems.validation.PropertyValueType;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses.AssociationClass;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationInterval;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeInterval;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

public class ImportUtility {
    public static Logger log = Logger.getLogger(ImportUtility.class);
    public static boolean outputError = true;
    public static final Set<String> VALUESPECS = new HashSet<String>(Arrays.asList(
            new String[] {"LiteralInteger", "LiteralString", "LiteralBoolean", 
                    "LiteralUnlimitedNatural", "Expression", "InstanceValue", 
                    "ElementValue", "OpaqueExpression", "Interval", "LiteralReal", 
                    "Duration", "DurationInterval", "TimeInterval", "TimeExpression", "StringExpression"}
                    
       ));
    
    public static Map<String, List<JSONObject>> getCreationOrder(List<JSONObject> newElements) {
        Map<String, List<JSONObject>> returns = new HashMap<String, List<JSONObject>>();
        
        DirectedGraphHashSet<JSONObject, DirectedEdgeVector<JSONObject>> graph = new DirectedGraphHashSet<JSONObject, DirectedEdgeVector<JSONObject>>();
        Map<String, JSONObject> id2ob = new HashMap<String, JSONObject>();
        for (JSONObject ob: newElements) {
            String sysmlid = (String)ob.get("sysmlid");
            if (sysmlid == null)
                continue;
            id2ob.put(sysmlid, ob);
            graph.addVertex(ob);
        }
        Map<String, JSONObject> fail = new HashMap<String, JSONObject>();
        for (JSONObject ob: newElements) {
            String sysmlid = (String)ob.get("sysmlid");
            String ownerid = (String)ob.get("owner");
            Element newE = ExportUtility.getElementFromID(sysmlid);
            Element ownerE = ExportUtility.getElementFromID(ownerid);
            if (ownerE == null && !id2ob.containsKey(ownerid)) {
                fail.put(sysmlid, ob);
                continue;
            }
            if (newE != null || ownerE != null)
                continue;
            JSONObject newj = id2ob.get(sysmlid);
            JSONObject ownerj = id2ob.get(ownerid);
            if (newj != null && ownerj != null)
                graph.addEdge(newj, ownerj);
        }
        
        SortedSet<JSONObject> reverse = (new TopologicalSort()).topological_sort(graph);
        List<JSONObject> toposort = new ArrayList<JSONObject>(reverse);
        
        Set<JSONObject> fails = new HashSet<JSONObject>();
        fails.addAll(fail.values());
        int size = fails.size()-1;
        while (fails.size() > size) {
            size = fails.size();
            Set<DirectedEdgeVector<JSONObject>> edges = graph.findEdgesWithTargetVertices(fails);
            for (DirectedEdgeVector<JSONObject> edge: edges) {
                fails.add(edge.getSourceVertex());
            }
        }
        toposort.removeAll(fails);
        returns.put("create", toposort);
        returns.put("fail", new ArrayList<JSONObject>(fails));
        return returns;
    }
    
    public static Element createElement(JSONObject ob, boolean updateRelations) throws ImportException {
        Project project = Application.getInstance().getProject();
        ElementsFactory ef = project.getElementsFactory();
        project.getCounter().setCanResetIDForObject(true);
        String ownerId = (String) ob.get("owner");
        if (ownerId == null || ownerId.isEmpty())
            return null;
        if (ExportUtility.getElementFromID(ownerId) == null)
            return null;
        // For all new elements the should be the following fields
        // should be present: name, owner, and documentation
        //
        String sysmlID = (String) ob.get("sysmlid");
        Element existing = ExportUtility.getElementFromID(sysmlID);
        if (existing != null && !updateRelations)
            return existing; //maybe jms feedback
        JSONObject specialization = (JSONObject) ob.get("specialization");
        String elementType = "Element";
        if (specialization != null) {
            elementType = (String) specialization.get("type");
        }
        Element newE = existing;
        try {
            if (elementType.equalsIgnoreCase("view")) {
                if (newE == null) {
                    Class view = ef.createClassInstance();
                    newE = view;
                }
                Stereotype sysmlView = Utils.getViewClassStereotype();
                if (updateRelations) {
                    //StereotypesHelper.addStereotype(newE, sysmlView);
                    setOrCreateAsi(sysmlView, newE);
                    setViewConstraint(newE, specialization);
                }
            } else if (elementType.equalsIgnoreCase("viewpoint")) {
                if (newE == null) {
                    Class view = ef.createClassInstance();
                    newE = view;
                }
                if (updateRelations) {
                    Stereotype sysmlView = Utils.getViewpointStereotype();
                    //StereotypesHelper.addStereotype(newE, sysmlView);
                    setOrCreateAsi(sysmlView, newE);
                }
            } else if (elementType.equalsIgnoreCase("Property")) {
                JSONArray vals = (JSONArray) specialization.get("value");
                Boolean isSlot = (Boolean) specialization.get("isSlot");
                if ((isSlot != null) && (isSlot == true)) {
                    if (newE == null) {
                        Slot newSlot = ef.createSlotInstance();
                        newE = newSlot;
                    }
                    if (specialization.containsKey("value"))
                        setSlotValues((Slot)newE, vals);
                } else {
                    if (newE == null) {
                        Property newProperty = ef.createPropertyInstance();
                        newE = newProperty;
                    }
                    if (specialization.containsKey("value"))
                    		setPropertyDefaultValue((Property)newE, vals);
                    if (specialization.containsKey("propertyType"))
                    		setProperty((Property)newE, specialization);
                }
            } else if (elementType.equalsIgnoreCase("Dependency")
                    || elementType.equalsIgnoreCase("Expose")
                    || elementType.equalsIgnoreCase("DirectedRelationship")
                    || elementType.equalsIgnoreCase("Characterizes")) {
                if (newE == null) {
                    Dependency newDependency = ef.createDependencyInstance();
                    newE = newDependency;
                }
                if (updateRelations)
                    setRelationshipEnds((Dependency)newE, specialization);
                if (elementType.equalsIgnoreCase("Characterizes")) {
                    Stereotype character = Utils.getCharacterizesStereotype();
                    StereotypesHelper.addStereotype((Dependency)newE, character);
                } else if (elementType.equalsIgnoreCase("Expose")) {
                    Stereotype expose = Utils.getExposeStereotype();
                    StereotypesHelper.addStereotype((Dependency)newE, expose);
                }
            } else if (elementType.equalsIgnoreCase("Generalization") || elementType.equalsIgnoreCase("Conform")) {
                if (newE == null) {
                    Generalization newGeneralization = ef.createGeneralizationInstance();
                    newE = newGeneralization;
                }
                if (updateRelations)
                    setRelationshipEnds((Generalization)newE, specialization);
                if (elementType.equalsIgnoreCase("Conform")) {
                    Stereotype conform = Utils.getSysML14ConformsStereotype();
                    StereotypesHelper.addStereotype((Generalization)newE, conform);
                }
            } else if (elementType.equalsIgnoreCase("Package")) {
                if (newE == null) {
                    Package newPackage = ef.createPackageInstance();
                    newE = newPackage;
                }
            } else if (elementType.equalsIgnoreCase("Constraint")) {
                if (newE == null) {
                    Constraint c = ef.createConstraintInstance();
                    newE = c;
                }
                setConstraintSpecification((Constraint)newE, specialization);
            } else if (elementType.equalsIgnoreCase("Product")) {
                if (newE == null) {
                    Class prod = ef.createClassInstance();
                    newE = prod;
                }
                if (updateRelations) {
                    Stereotype product = Utils.getDocumentStereotype();
                    //StereotypesHelper.addStereotype(newE, product);
                    setOrCreateAsi(product, newE);
                    setViewConstraint(newE, specialization);
                }
            } else if (elementType.equalsIgnoreCase("Association")) {
                if (newE == null) {
                    Association ac = ef.createAssociationInstance();
                    newE = ac;
                }
                if (updateRelations)
                    setAssociation((Association)newE, specialization);
            } else if (elementType.equalsIgnoreCase("Connector")) { 
                if (newE == null) {
                    Connector conn = ef.createConnectorInstance();
                    newE = conn;
                }
                if (updateRelations)
                    setConnectorEnds((Connector)newE, specialization);
            } else if (elementType.equalsIgnoreCase("InstanceSpecification")) {
                if (newE == null) {
                    InstanceSpecification is = ef.createInstanceSpecificationInstance();
                    newE = is;
                }
                setInstanceSpecification((InstanceSpecification)newE, specialization);
            } else if (newE == null) {
                Class newElement = ef.createClassInstance();
                newE = newElement;
            }
            setName(newE, ob);
            if (!(newE.getOwner() != null && ob.get("owner") instanceof String && 
                    ((String)ob.get("owner")).contains("holding_bin")))
                //don't update owner if trying to update existing element's owner to under a holding bin
                setOwner(newE, ob);
            setDocumentation(newE, ob);
            setOwnedAttribute(newE, ob);
            newE.setID(sysmlID);
        } catch (ImportException ex) {
            if (ex instanceof ReferenceException && updateRelations) {
                newE.dispose();
                throw ex;
            }
            setName(newE, ob);
            if (!(newE.getOwner() != null && ob.get("owner") instanceof String && 
                    ((String)ob.get("owner")).contains("holding_bin")))
                //don't update owner if trying to update existing element's owner to under a holding bin
                setOwner(newE, ob);
            setDocumentation(newE, ob);
            newE.setID(sysmlID);
            throw ex;
        }
        return newE;
    }
    
    public static void updateElement(Element e, JSONObject o) throws ImportException {
        setName(e, o);
        setDocumentation(e, o);
        setOwnedAttribute(e, o);
        JSONObject spec = (JSONObject)o.get("specialization");
        if (spec != null) {
            try {
                String type = (String)spec.get("type");
                if (type != null && type.equals("Property") && e instanceof Property) {
                		setProperty((Property)e, spec);
                    if (spec.containsKey("value"))
                        setPropertyDefaultValue((Property)e, (JSONArray)spec.get("value"));
    //                if (spec.containsKey("propertyType"))
    //                    setProperty((Property)e, spec);
                }
                if (type != null && type.equals("Property") && e instanceof Slot && spec.containsKey("value"))
                    setSlotValues((Slot)e, (JSONArray)spec.get("value"));
                if (type != null && e instanceof DirectedRelationship)
                    setRelationshipEnds((DirectedRelationship)e, spec);
                if (type != null && e instanceof Constraint && type.equals("Constraint"))
                    setConstraintSpecification((Constraint)e, spec);
                if (type != null && e instanceof Connector && type.equals("Connector"))
                    setConnectorEnds((Connector)e, spec);
                if (type != null && e instanceof Association && type.equals("Association")) {
                    try {
                        setAssociation((Association)e, spec);
                    } catch (Exception ex) {
                        
                    }
                }
                if (type != null && e instanceof InstanceSpecification && type.equals("InstanceSpecification"))
                    setInstanceSpecification((InstanceSpecification)e, spec);
                if (type != null && e instanceof Class && (type.equals("View") || type.equals("Product")) && spec.containsKey("contents"))
                    setViewConstraint(e, spec);
            } catch (ReferenceException ex) {
                throw new ImportException(e, o, ex.getMessage());
            } catch (ImportException ex) {
                throw new ImportException(e, o, ex.getMessage());
            }
        }
    }

	public static void setViewConstraint(Element e, JSONObject specialization) throws ImportException {
        Constraint c = Utils.getViewConstraint(e);
        if (c == null) {
            c = Application.getInstance().getProject().getElementsFactory().createConstraintInstance();
            Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
            c.setID(e.getID() + "_vc");
            c.setOwner(e);
            c.getConstrainedElement().add(e);
        }
        if (specialization.containsKey("contents")) {
            if (specialization.get("contents") == null) {
                c.setSpecification(null);
            } else {
                try {
                    c.setSpecification(createValueSpec((JSONObject)specialization.get("contents"), c.getSpecification()));
                } catch (ReferenceException ex) {
                    throw new ImportException(e, specialization, "View constraint: " + ex.getMessage());
                }
            }
        }
        if (specialization.containsKey("displayedElements")) {
            JSONArray des = (JSONArray)specialization.get("displayedElements");
            if (des != null)
                StereotypesHelper.setStereotypePropertyValue(e, Utils.getViewClassStereotype(), "elements", des.toJSONString());
        }
    }
    
    public static void setName(Element e, JSONObject o) {
        if (!o.containsKey("name"))
            return;
        String name = (String)o.get("name");
        setName(e, name);
    }
    
    public static void setName(Element e, String name) {
        if (e instanceof NamedElement && name != null)
            ((NamedElement)e).setName(ExportUtility.unescapeHtml(name));
    }
    
    public static void setOwner(Element e, JSONObject o) {
        if (!o.containsKey("owner"))
            return;
        String ownerId = (String) o.get("owner");
        if ((ownerId == null) || (ownerId.isEmpty())) {
            Utils.guilog("[ERROR] Owner not specified for mms sync add");
            return;
        }
        Element owner = ExportUtility.getElementFromID(ownerId);
        if (owner == null) {
            if (outputError)
                Utils.guilog("[ERROR] Owner not found for mms sync add");
            return;
        }
        e.setOwner(owner);
    }
    
    public static void setDocumentation(Element e, JSONObject o) {
        if (!o.containsKey("documentation"))
            return;
        String doc = (String)o.get("documentation");
        setDocumentation(e, doc);
    }
    
    public static void setDocumentation(Element e, String doc) {
        if (doc != null)
            ModelHelper.setComment(e, Utils.addHtmlWrapper(doc));
    }
    
    public static void setOwnedAttribute(Element e, JSONObject o) {
    	if (e instanceof Class && o.containsKey("ownedAttribute")) {
    		Class c = (Class)e;
    		JSONArray attr = (JSONArray)o.get("ownedAttribute");
    		List<Property> ordered = new ArrayList<Property>();
    		for (Object a: attr) {
    			if (a instanceof String) {
    				Element prop = ExportUtility.getElementFromID((String)a);
    				if (prop instanceof Property)
    					ordered.add((Property)prop);
    			}
    		}
    		//if (ordered.size() < c.getOwnedAttribute().size())
    		  //  return; //some prevention of accidental model corruption, if property can be left without an owner
    		c.getOwnedAttribute().clear();
    		c.getOwnedAttribute().addAll(ordered);
    	}
    }
    
    public static void setInstanceSpecification(InstanceSpecification is, JSONObject specialization) throws ImportException {
        JSONObject spec = (JSONObject)specialization.get("instanceSpecificationSpecification");
        if (spec != null) {
            try {
                is.setSpecification(createValueSpec(spec, is.getSpecification()));
            } catch (ReferenceException ex) {
                throw new ImportException(is, specialization, "Specification: " + ex.getMessage());
            }
        } else
            is.setSpecification(null);
        if (specialization.containsKey("classifier")) {
            JSONArray classifier = (JSONArray)specialization.get("classifier");
            if (classifier == null || classifier.isEmpty()) {
                log.info("[IMPORT/AUTOSYNC CORRUPTION PREVENTED] instance spec classifier is empty: " + is.getID());
                throw (new ReferenceException(is, specialization, "Instance Specification has no classifier"));
            }
            List<Classifier> newClassifiers = new ArrayList<Classifier>();
            for (Object id: classifier) {
                Element e = ExportUtility.getElementFromID((String)id);
                if (e instanceof Classifier) {
                    newClassifiers.add((Classifier)e);
                } else {
                    //throw new ImportException(is, specialization, (String)id + " is not a classifier");
                }
            }
            if (newClassifiers.isEmpty())
                throw (new ReferenceException(is, specialization, "Instance Specification has no classifier"));
            is.getClassifier().clear();
            is.getClassifier().addAll(newClassifiers);
        }
    }
    
    public static void setRelationshipEnds(DirectedRelationship dr, JSONObject specialization) throws ReferenceException {
        String sourceId = (String) specialization.get("source");
        String targetId = (String) specialization.get("target");
        Element source = ExportUtility.getElementFromID(sourceId);
        Element target = ExportUtility.getElementFromID(targetId);
        if (source != null && target != null) {
            ModelHelper.setSupplierElement(dr, target);
            ModelHelper.setClientElement(dr, source);
        } else {
            log.info("[IMPORT/AUTOSYNC CORRUPTION PREVENTED] directed relationship missing source or target: " + dr.getID());
            throw (new ReferenceException(dr, specialization, "Directed relationship has no source or target"));
        }
    }
    
    public static void setPropertyDefaultValue(Property p, JSONArray values) throws ReferenceException {
        if (values != null && values.size() > 0)
            p.setDefaultValue(createValueSpec((JSONObject)values.get(0), p.getDefaultValue()));
        if (values != null && values.isEmpty())
            p.setDefaultValue(null);
    }
    public static void setProperty(Property p, JSONObject spec) {
        // fix the property type here
        String ptype = (String)spec.get("propertyType");
        if (ptype != null) {
            Type t = (Type)ExportUtility.getElementFromID(ptype);
            if (t != null)
                p.setType(t);
            else
                log.info("[IMPORT/AUTOSYNC PROPERTY TYPE] prevent mistaken null type");
                //something bad happened
        }
        
        // set aggregation here
        AggregationKind aggr = null;
        if (spec.get("aggregation") != null)
            aggr = AggregationKindEnum.getByName(((String)spec.get("aggregation")).toLowerCase());
        if (aggr != null) {
            p.setAggregation(aggr);
        }
        ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
        
        Long spmin = (Long) spec.get("multiplicityMin");
        if ( spmin != null){
        	try{
        	    ValueSpecification pmin = p.getLowerValue();
        	    if (pmin == null)
        	        pmin = ef.createLiteralIntegerInstance();
        	    if (pmin instanceof LiteralInteger)
        	        ((LiteralInteger)pmin).setValue(spmin.intValue());
        	    if (pmin instanceof LiteralUnlimitedNatural)
        	        ((LiteralUnlimitedNatural)pmin).setValue(spmin.intValue());
	        	p.setLowerValue(pmin);
        	}
        	catch (NumberFormatException en){}
        }
        Long spmax = (Long) spec.get("multiplicityMax");
        if ( spmax != null){
        	try{
        	    ValueSpecification pmax = p.getUpperValue();
                if (pmax == null)
                    pmax = ef.createLiteralUnlimitedNaturalInstance();
                if (pmax instanceof LiteralInteger)
                    ((LiteralInteger)pmax).setValue(spmax.intValue());
                if (pmax instanceof LiteralUnlimitedNatural)
                    ((LiteralUnlimitedNatural)pmax).setValue(spmax.intValue());
                p.setUpperValue(pmax);
        	}
        	catch (NumberFormatException en){}
        }
        JSONArray redefineds = (JSONArray) spec.get("redefines");
        Collection<Property> redefinedps = p.getRedefinedProperty();
        if (redefineds != null && redefineds.size() != 0) { //for now prevent accidental removal of things in case server doesn't have the right reference
            redefinedps.clear();
            for (Object redefined: redefineds){
                Property redefinedp = (Property) ExportUtility.getElementFromID((String)redefined);
                if (redefinedp != null)
                    redefinedps.add(redefinedp);
            }
        }
    }
    
    public static void setPropertyType(Property p, Type type) {
        p.setType(type);
    }
    
    public static void setSlotValues(Slot s, JSONArray values) throws ReferenceException {
        if (values == null)
            return;
        List<ValueSpecification> originals = new ArrayList<ValueSpecification>(s.getValue());
        List<ValueSpecification> origs = new ArrayList<ValueSpecification>(originals);
        try {
            s.getValue().clear();
            for (Object o: values) {
                ValueSpecification vs = null;
                if (originals.size() > 0)
                    vs = createValueSpec((JSONObject)o, originals.remove(0));
                else
                    vs = createValueSpec((JSONObject)o, null);
                if (vs != null)
                    s.getValue().add(vs);
            }
        } catch (ReferenceException ex) {
            s.getValue().clear();
            s.getValue().addAll(origs);
            throw ex;
        }
    }
    
    public static void setConstraintSpecification(Constraint c, JSONObject spec) throws ImportException {
        if (!spec.containsKey("specification"))
            return;
        JSONObject sp = (JSONObject)spec.get("specification");
        if (sp != null) {
            try {
                c.setSpecification(createValueSpec(sp, c.getSpecification()));
            } catch (ReferenceException ex) {
                throw new ImportException(c, spec, "Constraint Specification: " + ex.getMessage());
            }
        }
    }
    
    public static void setConnectorEnds(Connector c, JSONObject spec) throws ReferenceException{
        JSONArray webSourcePath = (JSONArray)spec.get("sourcePath");
        JSONArray webTargetPath = (JSONArray)spec.get("targetPath");
        String webSource = null;
        String webTarget = null;
        if (webSourcePath != null && !webSourcePath.isEmpty())
            webSource = (String)webSourcePath.remove(webSourcePath.size()-1);
        if (webTargetPath != null && !webTargetPath.isEmpty())
            webTarget = (String)webTargetPath.remove(webTargetPath.size()-1);
        Element webSourceE = ExportUtility.getElementFromID(webSource);
        Element webTargetE = ExportUtility.getElementFromID(webTarget);
        if (webSourceE instanceof ConnectableElement && webTargetE instanceof ConnectableElement) {
            c.getEnd().get(0).setRole((ConnectableElement)webSourceE);
            c.getEnd().get(1).setRole((ConnectableElement)webTargetE);
        } else {
            log.info("[IMPORT/AUTOSYNC CORRUPTION PREVENTED] connector missing source or target: " + c.getID());
            throw (new ReferenceException(c, spec, "Connector doesn't have both connectable roles."));
        }
        Stereotype nestedend = StereotypesHelper.getStereotype(Application.getInstance().getProject(), "NestedConnectorEnd");
        if (webSourcePath != null && !webSourcePath.isEmpty()) {
            List<Property> evs = createPropertyPath((List<String>)webSourcePath);
            StereotypesHelper.setStereotypePropertyValue(c.getEnd().get(0), nestedend, "propertyPath", evs);
        }
        if (webTargetPath != null && !webTargetPath.isEmpty()) {
            List<Property> evs = createPropertyPath((List<String>)webTargetPath);
            StereotypesHelper.setStereotypePropertyValue(c.getEnd().get(1), nestedend, "propertyPath", evs);
        }
        String type = (String)spec.get("connectorType");
        Element asso = ExportUtility.getElementFromID(type);
        if (asso instanceof Association)
            c.setType((Association)asso);
    }
    
    public static void setAssociation(Association a, JSONObject spec) throws ImportException {
        String webSourceId = (String)spec.get("source");
        String webTargetId = (String)spec.get("target");
        Element webSource = ExportUtility.getElementFromID(webSourceId);
        Element webTarget = ExportUtility.getElementFromID(webTargetId);
        Property modelSource = null;
        Property modelTarget = null;
//        String webSourceA = (String)spec.get("sourceAggregation");
//        String webTargetA = (String)spec.get("targetAggregation");
        List<Property> todelete = new ArrayList<Property>();
        int i = 0;
        if (webSource == null || webTarget == null) {
            log.info("[IMPORT/AUTOSYNC CORRUPTION PREVENTED] association missing source or target: " + a.getID());
            throw new ReferenceException(a, spec, "Association missing ends");
        }
        for (Property end: a.getMemberEnd()) {
            if (end != webSource && end != webTarget)
                todelete.add(end);
            else if (i == 0) {
                modelSource = end;
            } else {
                modelTarget = end;
            }
            i++;
        }
        /*for (Property p: todelete) { //this used to be needed to prevent model corruption in 2.1? not needed in 18.0 (2.2)? corruption changes if asso is new or existing
            try {
                ModelElementsManager.getInstance().removeElement(p); //TODO propagate to alfresco?
            } catch (ReadOnlyElementException e) {
                e.printStackTrace();
            }
        }*/
        if (modelSource == webSource && modelTarget == webTarget)
            return; //don't need to mess with it
        a.getMemberEnd().clear();
        //if (modelSource == null && webSource instanceof Property) {
            a.getMemberEnd().add(0, (Property)webSource);
            modelSource = (Property)webSource;
        //}
        //if (modelTarget == null && webTarget instanceof Property) {
            a.getMemberEnd().add((Property)webTarget);
            modelTarget = (Property)webTarget;
        //}
//        if (modelSource != null && webSourceA != null) {
//            AggregationKindEnum agg = AggregationKindEnum.getByName(webSourceA.toLowerCase());
//            modelSource.setAggregation(agg);
//        }
//        if (modelTarget != null && webTargetA != null) {
//            AggregationKindEnum agg = AggregationKindEnum.getByName(webTargetA.toLowerCase());
//            modelTarget.setAggregation(agg);
//        }
    }
    
    public static List<ValueSpecification> createElementValues(List<String> ids) {
        List<ValueSpecification> result = new ArrayList<ValueSpecification>();
        ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
        for (String id: ids) {
            Element e = ExportUtility.getElementFromID(id);
            if (e == null)
                continue;
            ElementValue ev = ef.createElementValueInstance();
            ev.setElement(e);
            result.add(ev);
        }
        return result;
    }
    
    public static List<Property> createPropertyPath(List<String> ids) {
        List<Property> result = new ArrayList<Property>();
        for (String id: ids) {
            Element e = ExportUtility.getElementFromID(id);
            if (e == null || !(e instanceof Property))
                continue;
            result.add((Property)e);
        }
        return result;
    }
    
    public static ValueSpecification createValueSpec(JSONObject o, ValueSpecification v) throws ReferenceException {
        ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
        String valueType = (String)o.get("type");
        ValueSpecification newval = null;
        PropertyValueType propValueType = PropertyValueType.valueOf(valueType);
        
        switch ( propValueType ) {
        case LiteralString:
            if (v != null && v instanceof LiteralString)
                newval = v;
            else
                newval = ef.createLiteralStringInstance();
            String s = (String)o.get("string");
            if (s != null)
                ((LiteralString)newval).setValue(Utils.addHtmlWrapper(s));
            break;
        case LiteralInteger:
            if (v != null && v instanceof LiteralInteger)
                newval = v;
            else
                newval = ef.createLiteralIntegerInstance();
            Long l = (Long)o.get("integer");
            if (l != null)
                ((LiteralInteger)newval).setValue(l.intValue());
            break;
        case LiteralBoolean:
            if (v != null && v instanceof LiteralBoolean)
                newval = v;
            else
                newval = ef.createLiteralBooleanInstance();
            Boolean b = (Boolean)o.get("boolean");
            if (b != null)
                ((LiteralBoolean)newval).setValue(b);
            break;
        case LiteralUnlimitedNatural:
            if (v != null && v instanceof LiteralUnlimitedNatural)
                newval = v;
            else
                newval = ef.createLiteralUnlimitedNaturalInstance();
            Long ll = (Long)o.get("naturalValue");
            if (ll != null)
                ((LiteralUnlimitedNatural)newval).setValue(ll.intValue());
            break;
        case LiteralReal:
            Double value;
            if (o.get("double") instanceof Long)
                value = Double.parseDouble(((Long)o.get("double")).toString());
            else
                value = (Double)o.get("double");
            if (v != null && v instanceof LiteralReal)
                newval = v;
            else
                newval = ef.createLiteralRealInstance();
            if (value != null)
                ((LiteralReal)newval).setValue(value);
            break;
        case ElementValue:
            Element find = ExportUtility.getElementFromID((String)o.get("element"));
            if (find == null) {
                if (outputError) {
                    //Utils.guilog("Element with id " + o.get("element") + " not found!");
                    throw new ReferenceException(v, o, "Element with id " + o.get("element") + " for ElementValue not found!");
                }
                break;
            }
            if (v != null && v instanceof ElementValue)
                newval = v;
            else
                newval = ef.createElementValueInstance();
            ((ElementValue)newval).setElement(find);
            break;
        case InstanceValue:
            Element findInst = ExportUtility.getElementFromID((String)o.get("instance"));
            if (findInst == null){
                if (outputError) {
                    //Utils.guilog("Element with id " + o.get("instance") + " not found!");
                    throw new ReferenceException(v, o, "Instance with id " + o.get("instance") + " for InstanceValue not found!");
                }
                break;
            }
            if (!(findInst instanceof InstanceSpecification)) {
                if (outputError) {
                    //Utils.guilog("Element with id " + o.get("instance") + " is not an instance spec, cannot be put into an InstanceValue.");
                    throw new ReferenceException(v, o, "Element with id " + o.get("instance") + " is not an instance spec, cannot be put into an InstanceValue.");
                }
                break;
            }
            if (v != null && v instanceof InstanceValue)
                newval = v;
            else
                newval = ef.createInstanceValueInstance();
            ((InstanceValue)newval).setInstance((InstanceSpecification)findInst);
            break;
        case Expression:
            if (v != null && v instanceof Expression)
                newval = v;
            else
                newval = ef.createExpressionInstance();
            if (!o.containsKey("operand") || !(o.get("operand") instanceof JSONArray))
                break;
            ((Expression)newval).getOperand().clear();
            for (Object op: (JSONArray)o.get("operand")) {
                ValueSpecification operand = createValueSpec((JSONObject)op, null);
                if (operand != null)
                    ((Expression)newval).getOperand().add(operand);
            }
            break;
        case OpaqueExpression:
            if (v != null && v instanceof OpaqueExpression)
                newval = v;
            else
                newval = ef.createOpaqueExpressionInstance();
            if (!o.containsKey("expressionBody") || !(o.get("expressionBody") instanceof JSONArray))
                break;
            ((OpaqueExpression)newval).getBody().clear();
            for (Object op: (JSONArray)o.get("expressionBody")) {
                if (op instanceof String)
                    ((OpaqueExpression)newval).getBody().add((String)op);
            }
            break;
        case TimeExpression:
            if (v != null && v instanceof TimeExpression)
                newval = v;
            else
                newval = ef.createTimeExpressionInstance();
            break;
        case DurationInterval:
            if (v != null && v instanceof DurationInterval)
                newval = v;
            else
                newval = ef.createDurationIntervalInstance();
            break;
        case TimeInterval:
            if (v != null && v instanceof TimeInterval)
                newval = v;
            else
                newval = ef.createTimeIntervalInstance();
            break;
        default:
            log.error("Bad PropertyValueType: " + valueType);
        };
        return newval;
    }
    
    public static InstanceSpecification setOrCreateAsi(Stereotype s, Element e) {
        List<Stereotype> ss = new ArrayList<Stereotype>();
        ss.add(s);
        return setOrCreateAsi(ss, e);
    }
    //create applied steretype instance manually so we can set id explicitly instead of letting md generate id
    public static InstanceSpecification setOrCreateAsi(List<Stereotype> stereotypes, Element e) {
        InstanceSpecification is = null;
        for (Element child: e.getOwnedElement()) {
            if (child instanceof InstanceSpecification) {
                is = (InstanceSpecification)child;
                for (Classifier c: is.getClassifier()) {
                    if (!(c instanceof Stereotype)) { //not asi
                        is = null;
                        break;
                    }
                }
            }
        }
        if (is == null) {
            is = Application.getInstance().getProject().getElementsFactory().createInstanceSpecificationInstance();
            Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
            is.setID(e.getID() + "_asi");
            is.setOwner(e);
        }
        is.getClassifier().clear();
        is.getClassifier().addAll(stereotypes);
        return is;
    }
}
