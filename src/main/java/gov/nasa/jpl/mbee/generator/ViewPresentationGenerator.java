package gov.nasa.jpl.mbee.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.simple.JSONArray;

import gov.nasa.jpl.mbee.ems.validation.ImageValidator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.PresentationElement;
import gov.nasa.jpl.mbee.viewedit.PresentationElement.PEType;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;


public class ViewPresentationGenerator {
    private Classifier paraC = Utils.getOpaqueParaClassifier();
    private Classifier tableC = Utils.getOpaqueTableClassifier();
    private Classifier listC = Utils.getOpaqueListClassifier();
    private Classifier imageC = Utils.getOpaqueImageClassifier();
    private Classifier sectionC = Utils.getSectionClassifier();
    private Property generatedFromView = Utils.getGeneratedFromViewProperty();
    private Property generatedFromElement = Utils.getGeneratedFromElementProperty();
    private Stereotype presentsS = Utils.getPresentsStereotype();
    private ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
    
    private boolean recurse;
    private Element view;
    
    public ViewPresentationGenerator(Element view, boolean recursive) {
        this.view = view;
        this.recurse = recursive;
    }
    
    public void generate() {
        DocumentValidator dv = new DocumentValidator(view);
        dv.validateDocument();
        if (dv.isFatal()) {
            dv.printErrors(false);
            return;
        }
        //first run a local generation of the view model to get the current model view structure
        DocumentGenerator dg = new DocumentGenerator(view, dv, null);
        Document dge = dg.parseDocument(true, recurse, false);
        (new PostProcessor()).process(dge);
        
        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        DBAlfrescoVisitor visitor2 = new DBAlfrescoVisitor(recurse, true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null)
            return;
        book.accept(visitor2);
        Map<Element, List<PresentationElement>> view2pe = visitor2.getView2Pe();
        Map<Element, JSONArray> view2elements = visitor2.getView2Elements();
        if (!visitor2.getNotEditable().isEmpty()) {
            Application.getInstance().getGUILog().log("There are instances or view constraints/views that are not editable. Instance generation aborted.");
            return;
        }
            
        SessionManager.getInstance().createSession("view instance gen");
        try {
        	viewInstanceBuilder(view2pe);
            for (Element v: view2elements.keySet()) {
                JSONArray es = view2elements.get(v);
                StereotypesHelper.setStereotypePropertyValue(v, Utils.getViewClassStereotype(), "elements", es.toJSONString());
            }
            SessionManager.getInstance().closeSession();
        } catch(Exception e) {
            Utils.printException(e);
            SessionManager.getInstance().cancelSession();
        }
        
        ImageValidator iv = new ImageValidator(visitor2.getImages());
        //this checks images generated from the local generation against what's on the web based on checksum
        iv.validate();
        ValidationSuite imageSuite = iv.getSuite();
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(imageSuite);
        Utils.displayValidationWindow(vss, "Images Validation");
    }
    
    private Stack<String> findRoot(Element elem, Stack<String> path) {
    	// add the proper name of this element to the path, so we can use it later
    	// when we are building our package structure
    	path.push(((NamedElement)elem).getName() + " Presentation Elements");
    	for (Relationship r: elem.get_relationshipOfRelatedElement()) {
    		if (r instanceof Association) {
    			// Associations have owned ends, which tell you which element owns which other element
    			Association asso = (Association) r;
    			for (Property prop: asso.getOwnedEnd()) {
    				// for every incoming association (arrow pointing at this element)
    				// there are properties with the name of the source of the association
    				// for every outgoing association (arrow pointing away from this element)
    				// there are properties with the name of this element
    				if (!prop.getType().getName().equals(((NamedElement)elem).getName())) {
    					// this is usually always true
    					if (prop.getType() instanceof Element) {
    						// go find the root of the root you just found
    						path = findRoot(prop.getType(), path);
    					}
    				}
    			}
    		}
    	}
		return path;
    }
    
    private void viewInstanceBuilder(Map<Element, List<PresentationElement>> view2pe) {
    	// find the doc path
    	for (Element v: view2pe.keySet()) {
    		Stack<String> docPath = findRoot(v, new Stack<String>());
    		System.out.println("path:\t" + docPath);
    	}
        for (Element v: view2pe.keySet()) {
            handleViewOrSection(v, null, view2pe.get(v));
        }
    }
    
    private void handleViewOrSection(Element view, InstanceSpecification section, List<PresentationElement> pes) {
    	Package owner = getFolder(view);
    	handleViewOrSection(view, section, pes, owner);
    }
    
    private void handleViewOrSection(Element view, InstanceSpecification section, List<PresentationElement> pes, Package owner) {
        List<InstanceValue> list = new ArrayList<InstanceValue>();
        for (PresentationElement pe: pes) {
            if (pe.isManual()) {
                InstanceValue iv = ef.createInstanceValueInstance();
                iv.setInstance(pe.getInstance());
                list.add(iv);
                continue;
            }
            InstanceSpecification is = pe.getInstance();
            if (is == null) {
                is = ef.createInstanceSpecificationInstance();
                is.setOwner(owner);
                is.setName(pe.getName());
                if (pe.getType() == PEType.PARA)
                    is.getClassifier().add(paraC);
                else if (pe.getType() == PEType.TABLE)
                    is.getClassifier().add(tableC);
                else if (pe.getType() == PEType.LIST)
                    is.getClassifier().add(listC);
                else if (pe.getType() == PEType.IMAGE)
                    is.getClassifier().add(imageC);
                else if (pe.getType() == PEType.SECTION)
                    is.getClassifier().add(sectionC);
                Slot s = ef.createSlotInstance();
                s.setOwner(is);
                s.setDefiningFeature(generatedFromView);
                ElementValue ev = ef.createElementValueInstance();
                ev.setElement(view);
                s.getValue().add(ev);
                if (pe.getType() == PEType.SECTION && pe.getLoopElement() != null) {
                    Slot ss = ef.createSlotInstance();
                    ss.setOwner(is);
                    ss.setDefiningFeature(generatedFromElement);
                    ElementValue ev2 = ef.createElementValueInstance();
                    ev2.setElement(pe.getLoopElement());
                    ss.getValue().add(ev2);
                }
            }
            if (pe.getNewspec() != null) {
                LiteralString ls = ef.createLiteralStringInstance();
                ls.setOwner(is);
                ls.setValue(pe.getNewspec().toJSONString());
                is.setSpecification(ls);
            }
            is.setName(pe.getName());
            InstanceValue iv = ef.createInstanceValueInstance();
            iv.setInstance(is);
            list.add(iv);
            if (pe.getType() == PEType.SECTION) {
                handleViewOrSection(view, is, pe.getChildren());
            }
        }
        if (section != null) {
            Expression ex = ef.createExpressionInstance();
            ex.setOwner(section);
            ex.getOperand().addAll(list);
            section.setSpecification(ex);
        } else {
            Constraint c = getViewConstraint(view);
            Expression ex = ef.createExpressionInstance();
            ex.setOwner(c);
            ex.getOperand().addAll(list);
            c.setSpecification(ex);
        }
    }
    
    private Constraint getViewConstraint(Element view) {
        Constraint c = Utils.getViewConstraint(view);
        if (c != null)
            return c;
        c = ef.createConstraintInstance();
        c.setOwner(view);
        c.getConstrainedElement().add(view);
        return c;
    }
    
    private Package getFolder(Element view) {
        List<Element> results = Utils.collectDirectedRelatedElementsByRelationshipStereotype(view, presentsS, 1, false, 1);
        if (!results.isEmpty() && results.get(0) instanceof Package) {
        	return (Package)results.get(0);
        }
        Element result = view.getOwner();
        while(!(result instanceof Package)) {
            result = result.getOwner();
        }
        Package p = ef.createPackageInstance();
        p.setOwner(result);
        p.setName(((NamedElement)view).getName() + " Presentation Elements");
        Dependency d = ef.createDependencyInstance();
        d.setOwner(result);
        ModelHelper.setSupplierElement(d, p);
        ModelHelper.setClientElement(d, view);
        StereotypesHelper.addStereotype(d, presentsS);
        return p;

    }
}
