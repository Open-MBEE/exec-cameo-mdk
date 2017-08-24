package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.commands.Command;
import com.nomagic.magicdraw.commands.CommandHistory;
import com.nomagic.magicdraw.commands.MacroCommand;
import com.nomagic.magicdraw.commands.RemoveCommandCreator;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import gov.nasa.jpl.mbee.mdk.api.docgen.presentation_elements.PresentationElementEnum;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.util.*;

//@donbot update json simple to jackson
public class PresentationElementUtils {
    public static final String ID_SUFFIX = "_pei";

    private Project project;

    private Classifier paraC,
            tparaC,
            tableC,
            listC,
            imageC,
            sectionC;

    private Property generatedFromView,
            generatedFromElement;

    private ElementsFactory ef;

    {
        this.project = Application.getInstance().getProject();
        this.paraC = PresentationElementEnum.OPAQUE_PARAGRAPH.get().apply(project);
        this.tparaC = PresentationElementEnum.PARAGRAPH.get().apply(project);
        this.tableC = PresentationElementEnum.OPAQUE_TABLE.get().apply(project);
        this.listC = PresentationElementEnum.OPAQUE_LIST.get().apply(project);
        this.imageC = PresentationElementEnum.OPAQUE_IMAGE.get().apply(project);
        this.sectionC = PresentationElementEnum.OPAQUE_SECTION.get().apply(project);
        this.generatedFromView = Utils.getGeneratedFromViewProperty(project);
        this.generatedFromElement = Utils.getGeneratedFromElementProperty(project);
        this.ef = project.getElementsFactory();
    }

    public static Expression getViewOrSectionExpression(Element viewOrSection) {
        if (viewOrSection instanceof InstanceSpecification) {
            if (((InstanceSpecification) viewOrSection).getSpecification() instanceof Expression) {
                return (Expression) ((InstanceSpecification) viewOrSection).getSpecification();
            }
        }
        else if (viewOrSection instanceof Class) {
            Constraint c = Utils.getViewConstraint(viewOrSection);
            if (c != null && c.getSpecification() instanceof Expression) {
                return (Expression) c.getSpecification();
            }
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
        for (ValueSpecification vs : e.getOperand()) {
            if (vs instanceof InstanceValue) {
                InstanceSpecification is = ((InstanceValue) vs).getInstance();
                if (is == null) {
                    continue;
                }
                if (!is.getClassifier().isEmpty()) {
                    List<Classifier> iscs = is.getClassifier();
                    boolean viewinstance = false;
                    if (iscs.contains(paraC) || iscs.contains(tableC) || iscs.contains(listC) ||
                            iscs.contains(imageC) || iscs.contains(sectionC)) {
                        for (Element el : is.getOwnedElement()) {
                            if (el instanceof Slot && ((Slot) el).getDefiningFeature() != null && ((Slot) el).getDefiningFeature().getName() != null && ((Slot) el).getDefiningFeature().getName().equals("generatedFromView") &&
                                    !((Slot) el).getValue().isEmpty() && ((Slot) el).getValue().get(0) instanceof ElementValue &&
                                    ((ElementValue) ((Slot) el).getValue().get(0)).getElement() == view) {
                                viewinstance = true;
                            }
                        }
                        if (!viewinstance) {
                            for (InstanceValue iv : is.get_instanceValueOfInstance()) {
                                if (iv != vs && iv.getOwner() != null) { //an opaque instance that's referenced from somewhere else
                                    extraRef.add(is);
                                    break;
                                }
                            }
                        }
                    }
                    if ((iscs.contains(paraC) || iscs.contains(tparaC)) && isView && is.getSpecification() instanceof LiteralString) {
                        try {
                            JSONObject ob = (JSONObject) (new JSONParser()).parse(((LiteralString) is.getSpecification()).getValue());
                            //TODO sourceProperty json key migration? @donbot
                            if (Converters.getElementToIdConverter().apply(view).equals(ob.get("sourceId")) && "documentation".equals(ob.get("sourceProperty"))) {
                                viewinstance = false; //a view doc instance
                                res.setViewDocHack(is);
                            }
                        } catch (Exception x) {
                        }
                    }
                    if (viewinstance) {//instance generated by current view
                        if (iscs.contains(paraC)) {
                            paras.add(is);
                        }
                        else if (iscs.contains(tableC)) {
                            tables.add(is);
                        }
                        else if (iscs.contains(listC)) {
                            lists.add(is);
                        }
                        else if (iscs.contains(imageC)) {
                            images.add(is);
                        }
                        else if (iscs.contains(sectionC)) {
                            sections.add(is);
                        }
                        opaque.add(is);
                    }
                    else {
                        manuals.add(is);
                        for (InstanceValue iv : is.get_instanceValueOfInstance()) {
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
        return res;
    }

    public InstanceSpecification updateOrCreateInstance(PresentationElementInstance pe, Package owner) {
        InstanceSpecification is = pe.getInstance();
        if (is != null && pe.isManual() && !pe.isViewDocHack()) {
            return is;
        }
        if (is == null) {
            is = ef.createInstanceSpecificationInstance();
            Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
            is.setID(MDKConstants.HIDDEN_ID_PREFIX + Converters.getElementToIdConverter().apply(is) + ID_SUFFIX);
            if (!pe.isViewDocHack()) {
                Slot s = ef.createSlotInstance();
                s.setOwner(is);
                s.setOwningInstance(is);
                s.setDefiningFeature(generatedFromView);
                ElementValue ev = ef.createElementValueInstance();
                ev.setElement(pe.getView());
                s.getValue().add(ev);
                if (pe.getType() == PresentationElementEnum.SECTION && pe.getLoopElement() != null) {
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
        JSONObject newspec = pe.getNewspec();
        Classifier classifier = null;
        String name;
        if (pe.isViewDocHack()) {
            newspec = new JSONObject();
            newspec.put("type", "Paragraph");
            newspec.put("sourceType", "reference");
            newspec.put("source", Converters.getElementToIdConverter().apply(pe.getView()));
            newspec.put("sourceProperty", "documentation");
            name = "View Documentation";
            classifier = tparaC;
        }
        else {
            if (pe.getType() == PresentationElementEnum.PARAGRAPH) {
                classifier = paraC;
            }
            else if (pe.getType() == PresentationElementEnum.TABLE) {
                classifier = tableC;
            }
            else if (pe.getType() == PresentationElementEnum.LIST) {
                classifier = listC;
            }
            else if (pe.getType() == PresentationElementEnum.IMAGE) {
                classifier = imageC;
            }
            else if (pe.getType() == PresentationElementEnum.SECTION) {
                classifier = sectionC;
            }
            name = pe.getName();
            if (name == null || name.isEmpty()) {
                name = "<>";
            }
        }
        is.setName(name);
        is.getClassifier().clear();
        is.getClassifier().add(classifier);
        if (pe.getType() == PresentationElementEnum.SECTION) { //assume all children pe have instance, caller should walk bottom up
            Expression expression = is.getSpecification() instanceof Expression ? (Expression) is.getSpecification() : ef.createExpressionInstance();
            expression.setOwner(is);
            List<InstanceValue> ivs = new ArrayList<>(pe.getChildren().size());
            for (int i = 0; i < pe.getChildren().size(); i++) {
                InstanceValue iv = i < expression.getOperand().size() && expression.getOperand().get(i) instanceof InstanceValue ? (InstanceValue) expression.getOperand().get(i) : ef.createInstanceValueInstance();
                iv.setInstance(pe.getChildren().get(i).getInstance());
                ivs.add(iv);
            }
            expression.getOperand().clear();
            expression.getOperand().addAll(ivs);
        }
        else if (newspec != null) {
            ValueSpecification string = is.getSpecification();
            if (!(string instanceof LiteralString)) {
                string = ef.createLiteralStringInstance();
            }
            string.setOwner(is);
            ((LiteralString) string).setValue(newspec.toJSONString());
            is.setSpecification(string);
        }
        is.setOwner(owner);
        pe.setInstance(is);
        return is;
    }

    //return bfs view order
    public List<Element> getViewProcessOrder(Element start, Map<Element, List<Element>> view2view) {
        List<Element> res = new ArrayList<>();
        Queue<Element> toProcess = new LinkedList<>();
        HashSet<Element> processed = new HashSet<>();
        toProcess.add(start);
        while (!toProcess.isEmpty()) {
            Element next = toProcess.remove();
            res.add(next);
            processed.add(next);
            if (view2view.containsKey(next)) {
                for (Element element : view2view.get(next)) {
                    if (!processed.contains(element)) {
                        toProcess.add(element);
                    }
                    else {
                        Application.getInstance().getGUILog().log("[WARNING] Circular view order detected for " + element.getHumanName() + ". View will not be added to the ordering an additional time.");
                    }
                }
            }
        }
        return res;
    }

    public Constraint getOrCreateViewConstraint(Element view) {
        Constraint c = Utils.getViewConstraint(view);
        if (c != null) {
            return c;
        }
        c = ef.createConstraintInstance();
        Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
        c.setID(Converters.getElementToIdConverter().apply(view) + "_vc");
        c.setOwner(view);
        c.getConstrainedElement().add(view);
        return c;
    }

    public void updateOrCreateConstraintFromInstanceSpecifications(Element view, List<InstanceSpecification> instanceSpecifications) {
        Constraint c = getOrCreateViewConstraint(view);
        // It was necessary to delete the old expression and make a new one, because expression.getOperand().addAll(instanceValues) would return false in seemingly random, albeit consistent, views.
        Expression expression = c.getSpecification() instanceof Expression ? (Expression) c.getSpecification() : null;
        if (expression != null) {
            try {
                Command command = RemoveCommandCreator.getCommand(expression);
                command.execute();
                MacroCommand macroCommand = CommandHistory.getCommandForAppend(expression);
                macroCommand.add(command);
            } catch (RuntimeException ignored) {
                System.out.println("Could not clean up " + expression.getLocalID());
                return;
            }
        }
        Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
        expression = ef.createExpressionInstance();
        expression.setID(Converters.getElementToIdConverter().apply(view) + "_vc_expression");
        expression.setOwner(c);
        for (int i = 0; i < instanceSpecifications.size(); i++) {
            InstanceValue instanceValue = ef.createInstanceValueInstance();
            instanceValue.setID(expression.getLocalID() + "_" + i);
            instanceValue.setInstance(instanceSpecifications.get(i));
            expression.getOperand().add(instanceValue);
        }
    }

    public void updateOrCreateConstraintFromPresentationElements(Element view, List<PresentationElementInstance> presentationElementInstances) {
        List<InstanceSpecification> instanceSpecifications = new ArrayList<>(presentationElementInstances.size());
        for (PresentationElementInstance presentationElementInstance : presentationElementInstances) {
            instanceSpecifications.add(presentationElementInstance.getInstance());
        }
        updateOrCreateConstraintFromInstanceSpecifications(view, instanceSpecifications);
    }
}
