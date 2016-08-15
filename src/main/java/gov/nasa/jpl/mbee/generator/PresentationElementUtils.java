package gov.nasa.jpl.mbee.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import gov.nasa.jpl.mbee.api.docgen.PresentationElementType;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.PresentationElement;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.util.*;

public class PresentationElementUtils {
    public static final String ID_SUFFIX = "_pei";

    private Classifier paraC = PresentationElementType.OPAQUE_PARAGRAPH.getClassifier(Application.getInstance().getProject()),
            tparaC = PresentationElementType.PARAGRAPH.getClassifier(Application.getInstance().getProject()),
            tableC = PresentationElementType.OPAQUE_TABLE.getClassifier(Application.getInstance().getProject()),
            listC = PresentationElementType.OPAQUE_LIST.getClassifier(Application.getInstance().getProject()),
            imageC = PresentationElementType.OPAQUE_IMAGE.getClassifier(Application.getInstance().getProject()),
            sectionC = PresentationElementType.OPAQUE_SECTION.getClassifier(Application.getInstance().getProject()),
            tsectionC = PresentationElementType.SECTION.getClassifier(Application.getInstance().getProject());
    private Stereotype presentsStereotype = Utils.getPresentsStereotype(),
            productStereotype = Utils.getProductStereotype(),
            viewClassStereotype = Utils.getViewClassStereotype();
    private Property generatedFromView = Utils.getGeneratedFromViewProperty(),
            generatedFromElement = Utils.getGeneratedFromElementProperty();
    
    private ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();

    public Stereotype getPresentsStereotype() {
        return presentsStereotype;
    }

    public Stereotype getProductStereotype() {
        return productStereotype;
    }

    public Stereotype getViewClassStereotype() {
        return viewClassStereotype;
    }

    public static Expression getViewOrSectionExpression(Element viewOrSection) {
        if (viewOrSection instanceof InstanceSpecification) {
            if (((InstanceSpecification)viewOrSection).getSpecification() instanceof Expression)
                return (Expression)((InstanceSpecification)viewOrSection).getSpecification();
        } else if (viewOrSection instanceof Class) {
            Constraint c = Utils.getViewConstraint(viewOrSection);
            if (c != null && c.getSpecification() instanceof Expression)
                return (Expression)c.getSpecification();
        }
        return null;
    }
    
    public PresentationElementInfo getCurrentInstances(Element viewOrSection, Element view) {
        List<InstanceSpecification> tables = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> lists = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> sections = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> paras = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> images = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> manuals = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> all = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> extraRef = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> unused = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> opaque = new ArrayList<InstanceSpecification>();
        List<InstanceSpecification> extraManualRef = new ArrayList<InstanceSpecification>();

        PresentationElementInfo res = new PresentationElementInfo(all, images, tables, lists, paras, sections, manuals, extraRef, extraManualRef, unused, opaque);
        Expression e = getViewOrSectionExpression(viewOrSection);
        boolean isView = !(viewOrSection instanceof InstanceSpecification);
        if (e == null) {
            return res;
        }
        for (ValueSpecification vs: e.getOperand()) {
            if (vs instanceof InstanceValue) {
                InstanceSpecification is = ((InstanceValue)vs).getInstance();
                if (is ==  null)
                    continue;
                if (!is.getClassifier().isEmpty()) {
                    List<Classifier> iscs = is.getClassifier();
                    boolean viewinstance = false;
                    if (iscs.contains(paraC) || iscs.contains(tableC) || iscs.contains(listC) ||
                            iscs.contains(imageC) || iscs.contains(sectionC)) {
                        for (Element el: is.getOwnedElement()) {
                            if (el instanceof Slot && ((Slot)el).getDefiningFeature().getName().equals("generatedFromView") &&
                                    !((Slot)el).getValue().isEmpty() && ((Slot)el).getValue().get(0) instanceof ElementValue &&
                                    ((ElementValue)((Slot)el).getValue().get(0)).getElement() == view) {
                                viewinstance = true;
                            }
                        }
                        if (!viewinstance) {
                            for (InstanceValue iv: is.get_instanceValueOfInstance()) {
                                if (iv != vs && iv.getOwner() != null) { //an opaque instance that's referenced from somewhere else
                                    extraRef.add(is);
                                    break;
                                }
                            }
                        }
                    }
                    if ((iscs.contains(paraC) || iscs.contains(tparaC)) && isView && is.getSpecification() instanceof LiteralString) {
                        try {
                            JSONObject ob = (JSONObject)(new JSONParser()).parse(((LiteralString)is.getSpecification()).getValue());
                            if (view.getID().equals(ob.get("source")) && "documentation".equals(ob.get("sourceProperty"))) {
                                viewinstance = false; //a view doc instance
                                res.setViewDocHack(is);
                            }
                        } catch (Exception x) {}
                    }
                    if (viewinstance) {//instance generated by current view
                        if (iscs.contains(paraC))
                            paras.add(is);
                        else if (iscs.contains(tableC))
                            tables.add(is);
                        else if (iscs.contains(listC))
                            lists.add(is);
                        else if (iscs.contains(imageC))
                            images.add(is);
                        else if (iscs.contains(sectionC))
                            sections.add(is);
                        opaque.add(is);
                    } else {
                        manuals.add(is);
                        for (InstanceValue iv: is.get_instanceValueOfInstance()) {
                            if (iv != vs && iv.getOwner() != null) { //a non opaque instance being referenced from somewhere else
                                extraManualRef.add(is);
                                break;
                            }
                        }
                    }
                    all.add(is);
                }
            }
        }
        if (isView) {
            Package viewp = findViewInstancePackage(view);
            if (viewp != null) {
                for (Element el: viewp.getOwnedElement()) {
                    if (el instanceof InstanceSpecification && ((InstanceSpecification)el).get_instanceValueOfInstance().isEmpty()) {
                        unused.add((InstanceSpecification)el); //but this might be a manual instance that's referenced by higher project?
                    }
                }
            }
        }
        return res;
    }
    
    public boolean isSection(InstanceSpecification is) {
        if (is.getClassifier().contains(sectionC) || is.getClassifier().contains(tsectionC))
            return true;
        return false;
    }
    
    public boolean isInSomeViewPackage(InstanceSpecification is) {
        Element owner = is.getOwner();
        if (owner instanceof Package) {
            for (Element e: Utils.collectDirectedRelatedElementsByRelationshipStereotype(owner, presentsStereotype, 2, false, 1)) {
                if (StereotypesHelper.hasStereotypeOrDerived(e, viewClassStereotype))
                    return true;
            }
        }
        return false;
    }
    
    public Package findViewInstancePackage(Element view) {
        List<Element> results = Utils.collectDirectedRelatedElementsByRelationshipStereotype(view, presentsStereotype, 1, false, 1);
        if (!results.isEmpty() && results.get(0) instanceof Package) {
            return (Package)results.get(0);
        }
        return null;
    }
    
    public List<Package> findCorrectViewInstancePackageOwners(Element view) {
        Type viewt = (Type)view;
        List<Package> parentPack = new ArrayList<Package>();
        if (StereotypesHelper.hasStereotypeOrDerived(view, productStereotype)) {
            Element owner = view.getOwner();
            while (!(owner instanceof Package)) {
                owner = owner.getOwner();
            }
            parentPack.add((Package)owner);
        } else {
            for (TypedElement t: viewt.get_typedElementOfType()) {
                if (t instanceof Property && ((Property)t).getAggregation().equals(AggregationKindEnum.COMPOSITE) &&
                        StereotypesHelper.hasStereotypeOrDerived(t.getOwner(), viewClassStereotype)) {
                    Package parent = findViewInstancePackage(t.getOwner());
                    if (parent != null)
                        parentPack.add(parent);
                }
            }
            if (parentPack.isEmpty()) {
                Element owner = view.getOwner();
                while (!(owner instanceof Package)) {
                    owner = owner.getOwner();
                }
                parentPack.add((Package)owner);
            }
        }
        return parentPack;
    }
    
    public Package createViewInstancePackage(Element view, Package owner) {
        Package viewPackage = ef.createPackageInstance();
        viewPackage.setName(((NamedElement)view).getName() + " Instances");
        viewPackage.setOwner(owner);
        Dependency d = ef.createDependencyInstance();
        d.setOwner(viewPackage);
        ModelHelper.setSupplierElement(d, viewPackage);
        ModelHelper.setClientElement(d, view);
        StereotypesHelper.addStereotype(d, presentsStereotype);
        return viewPackage;
    }
    
    public boolean needLockForEdit(PresentationElement pe) {
        InstanceSpecification is = pe.getInstance();
        if (is == null || (pe.isManual() && !pe.isViewDocHack()))
            return false;
        if (pe.isViewDocHack())
            return true;
        ValueSpecification oldvs = is.getSpecification();
        //check classifier
        if (pe.getNewspec() != null && !pe.getNewspec().get("type").equals("Section")) {
            if (oldvs instanceof LiteralString && ((LiteralString)oldvs).getValue() != null) {
                try {
                    JSONObject oldob = (JSONObject)JSONValue.parse(((LiteralString)oldvs).getValue());
                    if (oldob == null || !oldob.equals(pe.getNewspec()))
                        return true;
                } catch (Exception ex) {
                    return true;
                }
            } else
                return true;
        } else if (pe.getType().equals(PresentationElementType.SECTION)) {
            if (!(is.getSpecification() instanceof Expression))
                return true;
            List<InstanceSpecification> list = new ArrayList<InstanceSpecification>();
            for (PresentationElement cpe: pe.getChildren()) {
                if (cpe.getInstance() == null)
                    return true;
                list.add(cpe.getInstance());
            }
            List<ValueSpecification> model = ((Expression)is.getSpecification()).getOperand();
            if (model.size() != list.size())
                 return true;
            for (int i = 0; i < model.size(); i++) {
                ValueSpecification modelvs = model.get(i);
                if (!(modelvs instanceof InstanceValue) || ((InstanceValue)modelvs).getInstance() != list.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public InstanceSpecification updateOrCreateInstance(PresentationElement pe, Package owner) {
        InstanceSpecification is = pe.getInstance();
        if (is != null && pe.isManual() && !pe.isViewDocHack())
            return is;
        if (is == null) {
            is = ef.createInstanceSpecificationInstance();
            Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
            is.setID(ModelValidator.HIDDEN_ID_PREFIX + is.getID() + ID_SUFFIX);
            if (!pe.isViewDocHack()) {
                Slot s = ef.createSlotInstance();
                s.setOwner(is);
                s.setOwningInstance(is);
                s.setDefiningFeature(generatedFromView);
                ElementValue ev = ef.createElementValueInstance();
                ev.setElement(pe.getView());
                s.getValue().add(ev);
                if (pe.getType() == PresentationElementType.SECTION && pe.getLoopElement() != null) {
                    Slot ss = ef.createSlotInstance();
                    ss.setOwner(is);
                    ss.setOwningInstance(is);
                    ss.setDefiningFeature(generatedFromElement);
                    ElementValue ev2 = ef.createElementValueInstance();
                    ev2.setElement(pe.getLoopElement());
                    ss.getValue().add(ev2);
                }
            }
        }
        JSONObject newspec  = pe.getNewspec();
        Classifier classifier = null;
        String name;
        if (pe.isViewDocHack()) {
            newspec = new JSONObject();
            newspec.put("source", is.getID());
            newspec.put("type", "Paragraph");
            newspec.put("sourceProperty", "documentation");
            String transclude = "<p>&nbsp;</p><p><mms-transclude-doc data-mms-eid=\"" + pe.getView().getID() + "\">[cf." + ((NamedElement)pe.getView()).getName() +".doc]</mms-transclude-doc></p><p>&nbsp;</p>";
            ModelHelper.setComment(is, transclude);
            name = "View Documentation";
            classifier = tparaC;
        } else {
            if (pe.getType() == PresentationElementType.PARAGRAPH)
                classifier = paraC;
            else if (pe.getType() == PresentationElementType.TABLE)
                classifier = tableC;
            else if (pe.getType() == PresentationElementType.LIST)
                classifier = listC;
            else if (pe.getType() == PresentationElementType.IMAGE)
                classifier = imageC;
            else if (pe.getType() == PresentationElementType.SECTION)
                classifier = sectionC;
            name = pe.getName();
            if (name == null || name.isEmpty())
                name = "<>";
        }
        if (newspec != null) {
            ValueSpecification string = is.getSpecification();
            if (!(string instanceof LiteralString))
                string = ef.createLiteralStringInstance();
            string.setOwner(is);
            ((LiteralString)string).setValue(newspec.toJSONString());
            is.setSpecification(string);
        }
        is.setName(name);
        is.getClassifier().clear();
        is.getClassifier().add(classifier);
        if (pe.getType() == PresentationElementType.SECTION) { //assume all children pe have instance, caller should walk bottom up
            ValueSpecification expression = is.getSpecification();
            if (!(expression instanceof Expression))
                expression = ef.createExpressionInstance();
            expression.setOwner(is);
            List<InstanceValue> ivs = new ArrayList<InstanceValue>();
            for (PresentationElement spe: pe.getChildren()) {
                InstanceValue iv = ef.createInstanceValueInstance();
                iv.setInstance(spe.getInstance());
                ivs.add(iv);
            }
            ((Expression)expression).getOperand().clear();
            ((Expression)expression).getOperand().addAll(ivs);
        }
        is.setOwner(owner);
        pe.setInstance(is);
        return is;
    }
    
    //return bfs view order
    public List<Element> getViewProcessOrder(Element start, Map<Element, List<Element>> view2view) {
        List<Element> res = new ArrayList<Element>();
        Queue<Element> toProcess = new LinkedList<Element>();
        toProcess.add(start);
        while (!toProcess.isEmpty()) {
            Element next = toProcess.remove();
            res.add(next);
            if (view2view.containsKey(next))
                toProcess.addAll(view2view.get(next));
        }
        return res;
    }

    public Constraint getOrCreateViewConstraint(Element view) {
        Constraint c = Utils.getViewConstraint(view);
        if (c != null)
            return c;
        c = ef.createConstraintInstance();
        Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
        c.setID(view.getID() + "_vc");
        c.setOwner(view);
        c.getConstrainedElement().add(view);
        return c;
    }

    public void updateOrCreateConstraintFromInstanceSpecifications(Element view, List<InstanceSpecification> instanceSpecifications) {
        Constraint c = getOrCreateViewConstraint(view);
        Expression expression = c.getSpecification() instanceof Expression ? (Expression) c.getSpecification() : ef.createExpressionInstance();
        expression.setOwner(c);
        List<InstanceValue> ivs = new ArrayList<>(instanceSpecifications.size());
        for (InstanceSpecification instanceSpecification : instanceSpecifications) {
            InstanceValue iv = ef.createInstanceValueInstance();
            iv.setInstance(instanceSpecification);
            ivs.add(iv);
        }
        expression.getOperand().clear();
        expression.getOperand().addAll(ivs);
    }
    
    public void updateOrCreateConstraintFromPresentationElements(Element view, List<PresentationElement> presentationElements) {
        List<InstanceSpecification> instanceSpecifications = new ArrayList<>(presentationElements.size());
        for (PresentationElement presentationElement : presentationElements) {
            instanceSpecifications.add(presentationElement.getInstance());
        }
        updateOrCreateConstraintFromInstanceSpecifications(view, instanceSpecifications);
    }
    
    /*public boolean needLockForEditConstraint(Element view, List<PresentationElement> pes) {
        Constraint c = Utils.getViewConstraint(view);
        if (c == null)
            return false;
        ValueSpecification vs = c.getSpecification();
        if (vs == null || !(vs instanceof Expression))
            return true;
        Expression ex = (Expression)vs;
        List<InstanceSpecification> list = new ArrayList<InstanceSpecification>();
        for (PresentationElement cpe: pes) {
            if (cpe.getInstance() == null)
                return true;
            list.add(cpe.getInstance());
        }
        List<ValueSpecification> model = ex.getOperand();
        if (model.size() != list.size())
             return true;
        for (int i = 0; i < model.size(); i++) {
            ValueSpecification modelvs = model.get(i);
            if (!(modelvs instanceof InstanceValue) || ((InstanceValue)modelvs).getInstance() != list.get(i)) {
                return true;
            }
        }
        return false;
    }*/
    
    public Package getOrCreateUnusedInstancePackage() {
        Package rootPackage = Utils.getRootElement();
        String viewInstID = Utils.getProject().getPrimaryProject() .getProjectID().replace("PROJECT", "View_Instances");
        String unusedId = Utils.getProject().getPrimaryProject() .getProjectID().replace("PROJECT", "Unused_View_Instances");
        Package viewInst = (Package)Application.getInstance().getProject().getElementByID(viewInstID);
        Package unusedViewInst = (Package)Application.getInstance().getProject().getElementByID(unusedId);
        if (unusedViewInst != null)
            return unusedViewInst;
        Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
        if (viewInst == null) {
            viewInst = ef.createPackageInstance();
            viewInst.setID(viewInstID);
            viewInst.setName("View Instances");
            viewInst.setOwner(rootPackage);
        }
        unusedViewInst = ef.createPackageInstance();
        unusedViewInst.setID(unusedId);
        unusedViewInst.setName("Unused View Instances");
        unusedViewInst.setOwner(viewInst);
        return unusedViewInst;
        
    }
}
