package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.*;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.constraint.BasicConstraint;
import gov.nasa.jpl.mbee.mdk.constraint.Constraint;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mbee.mdk.ocl.OclEvaluator;
import gov.nasa.jpl.mbee.mdk.util.Debug;
import gov.nasa.jpl.mbee.mdk.util.MoreToString;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Utils2;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import org.eclipse.ocl.ParserException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.PrintWriter;
import java.util.*;

/**
 * validates docgen 3 document uses jgrapht to detect cycles in the document and
 * various other potential errors this only checks for static model structure
 * and does not actually try to execute the document
 *
 * @author dlam Changelog: Document Validator updated to use Validationsuite.
 */
public class DocumentValidator {

    public ValidationRule getViewpointConstraintRule() {
        return viewpointConstraintRule;
    }

    private Element start;
    private Set<Behavior> done;

    private ValidationSuite validationui = new ValidationSuite(
            "Validationui");
    private ValidationSuite dynamicExpressionValidation = new ValidationSuite(
            "ExpressionValidation");

    private static final Map<String, String> requiredTags = new HashMap<String, String>() {

        private static final long serialVersionUID = -5391825454091546000L;

        {
            put(DocGenProfile.metaclassChoosable, "metaclasses");
            put(DocGenProfile.stereotypeChoosable, "stereotypes");
            put(DocGenProfile.nameChoosable, "names");
            put(DocGenProfile.diagramTypeChoosable, "diagramTypes");
            put(DocGenProfile.expressionChoosable, "expression");
            put(DocGenProfile.propertyChoosable, "desiredProperty");
            put(DocGenProfile.attributeChoosable, "desiredAttribute");
        }
    };
    /*
     * Statuses possible, currently error, warning, and fatal error.
     */

    private ViolationSeverity error = ViolationSeverity.ERROR;
    private ViolationSeverity warn = ViolationSeverity.WARNING;
    private ViolationSeverity fatalerror = ViolationSeverity.FATAL;

    /*
     * Current List of validation flags.
     */
    private ValidationRule multipleFirstErrors = new ValidationRule(
            "Multiple First Errors",
            "Has multiple firsts!",
            error);
    private ValidationRule multipleNextErrors = new ValidationRule(
            "Multiple Next Errors",
            "Has multiple nexts!",
            error);
    private ValidationRule multipleContentErrors = new ValidationRule(
            "Multiple Content Errors",
            "has multiple (unstereotyped) dependencies!",
            error);
    private ValidationRule multipleViewpoints = new ValidationRule(
            "MultipleViewpoints",
            "Conforms to multiple vewpoints!",
            error);
    private ValidationRule missingViewpointErrors = new ValidationRule(
            "Missing Viewpoint Errors",
            "Doesn't conform to any viewpoint!",
            warn);
    private ValidationRule missingImportErrors = new ValidationRule(
            "Missing Import Errors",
            "Is missing imports!",
            warn);
    private ValidationRule missingViewpointBehavior = new ValidationRule(
            "Missing Viewpoint Behavior",
            "Is missng the viewpoint behavior",
            error);
    private ValidationRule nonView2View = new ValidationRule(
            "Nonsection With Dependencies",
            "Is a nonsection but has first or next dependencies!",
            warn);
    private ValidationRule shouldBeSection = new ValidationRule(
            "Should be a Section",
            "Is a nonsection but should be a section (because it's the target of a First or Next",
            warn);
    private ValidationRule shouldNotBeSection = new ValidationRule(
            "Should not be a section",
            "Is a section but should not be (because it's the target of a vanilla dependency",
            warn);
    private ValidationRule multipleInitialNode = new ValidationRule(
            "Muliple Initial Nodes",
            "Has multiple initial nodes!",
            error);
    private ValidationRule multipleOutgoingFlows = new ValidationRule(
            "Multiple Outgoing Flows",
            "Has multiple outgoing flows!",
            error);
    private ValidationRule multipleIncomingFlows = new ValidationRule(
            "Multple Incoming Flows",
            "Has multiple incoming flows!",
            warn);
    private ValidationRule missingInitialNode = new ValidationRule(
            "Missing Initial Node",
            "Is missing an initial node!",
            warn);
    private ValidationRule multipleStereotypes = new ValidationRule(
            "Multiple Stereotypes",
            "Element and/or its behavior has multiple stereotypes!",
            error);
    private ValidationRule mismatchStereotypeErrors = new ValidationRule(
            "Mismatched Stereotypes",
            "Element and its behavior have mismatched sterotypes!",
            error);
    private ValidationRule missingStereotype = new ValidationRule(
            "Missing stereotype",
            "Element and its behavior (if present) is missing a document stereotype!",
            error);
    private ValidationRule missingOutgoingFlow = new ValidationRule(
            "Missing outgoing flow",
            "Non-final node is missing outgoing flow!",
            warn);
    private ValidationRule cycleError = new ValidationRule(
            "Cycles in model",
            "There are loops in this document! Do not generate document!",
            fatalerror);
    private ValidationRule activityNodeCycleError = new ValidationRule(
            "Activity Node Cycles in Model",
            "There are loops in this document! Do not generate document!",
            fatalerror);
    private ValidationRule missingTagValue = new ValidationRule(
            "Missing tag",
            "An action is missing required tag value",
            error);

    private ValidationRule viewpointConstraintRule = new ConstraintValidationRule();

    /*
     * Needed to use the utils.displayvalidationwindow
     */

    private Collection<ValidationSuite> validationOutput = new ArrayList<ValidationSuite>();

    private GUILog log;
    private DirectedGraph<NamedElement, Element> dg;           // graph for viewpoints
    private List<Set<ActivityNode>> cycles;       // cycles for activities and structured nodes
    private ActivityEdgeFactory aef;
    private boolean fatal;
    private Stereotype sysmlview;
    private Stereotype conform;
    private Stereotype expose;
    private Project project;

    public DocumentValidator(Element e) {
        start = e;
        project = Project.getProject(e);

        sysmlview = Utils.getViewStereotype(project);
        conform = Utils.getConformStereotype(project);
        expose = Utils.getExposeStereotype(project);


        log = Application.getInstance().getGUILog();

        cycles = new ArrayList<>();
        fatal = false;
        done = new HashSet<>();
        aef = new ActivityEdgeFactory();
        dg = new DefaultDirectedGraph<>(Element.class);

        // List of Validation Rules
        validationui.addValidationRule(multipleFirstErrors);
        validationui.addValidationRule(multipleNextErrors);
        validationui.addValidationRule(multipleContentErrors);
        validationui.addValidationRule(multipleViewpoints);
        validationui.addValidationRule(multipleOutgoingFlows);
        validationui.addValidationRule(mismatchStereotypeErrors);
        validationui.addValidationRule(missingViewpointErrors);
        validationui.addValidationRule(missingImportErrors);
        validationui.addValidationRule(multipleInitialNode);
        validationui.addValidationRule(multipleIncomingFlows);
        validationui.addValidationRule(missingInitialNode);
        validationui.addValidationRule(missingViewpointBehavior);
        validationui.addValidationRule(missingStereotype);
        validationui.addValidationRule(missingOutgoingFlow);
        validationui.addValidationRule(multipleStereotypes);
        validationui.addValidationRule(nonView2View);
        validationui.addValidationRule(shouldBeSection);
        validationui.addValidationRule(shouldNotBeSection);
        validationui.addValidationRule(cycleError);
        validationui.addValidationRule(activityNodeCycleError);
        validationui.addValidationRule(missingTagValue);

        dynamicExpressionValidation.addValidationRule(viewpointConstraintRule);

        // Need Collection to use the utils.DisplayValidationWindow method
        validationOutput.add(validationui);
        validationOutput.add(dynamicExpressionValidation);

    }

    public boolean isFatal() {
        return fatal;
    }

    public void validateDocument() {
        if (StereotypesHelper.hasStereotypeOrDerived(start, sysmlview)) {
            validateView((NamedElement) start, true);
        }
        else if (StereotypesHelper.hasStereotypeOrDerived(start, DocGenProfile.documentStereotype)
                && start instanceof Activity) {
            this.done.add((Activity) start);
            validateActivity((NamedElement) start);
        }
        else {
            log.log("This is not a starting docgen 3 document!");
        }
    }

    class ViewDependencyEdgeFactory implements EdgeFactory<NamedElement, DirectedRelationship> {
        @Override
        public DirectedRelationship createEdge(NamedElement sourceVertex, NamedElement targetVertex) {
            List<DirectedRelationship> dep = Utils.findDirectedRelationshipsBetween(sourceVertex,
                    targetVertex);
            if (!dep.isEmpty()) {
                return dep.get(0);
            }
            return null;
        }
    }

    class ActivityEdgeFactory implements EdgeFactory<ActivityNode, ActivityEdge> {
        @Override
        public ActivityEdge createEdge(ActivityNode sourceVertex, ActivityNode targetVertex) {
            for (ActivityEdge ae : sourceVertex.getOutgoing()) {
                if (ae.getTarget() == targetVertex) {
                    return ae;
                }
            }
            return null;
        }
    }

    private void validateView(NamedElement view, boolean section) {

        if (dg.containsVertex(view)) {
            return;
        }
        dg.addVertex(view);
        List<Element> viewpoints = Utils.collectDirectedRelatedElementsByRelationshipStereotype(view,
                conform, 1, true, 1);
        if (viewpoints.size() > 1) {
            multipleViewpoints.addViolation(view, multipleViewpoints.getDescription());
        }
        for (Element viewpoint : viewpoints) {
            if (viewpoint != null && viewpoint instanceof Class) {
                Collection<Behavior> viewpointBehavior = ((Class) viewpoint).getOwnedBehavior();
                Behavior b = null;
                if (viewpointBehavior.size() > 0) {
                    b = viewpointBehavior.iterator().next();
                }
                else {
                    Class now = (Class) viewpoint;
                    while (now != null) {
                        if (!now.getSuperClass().isEmpty()) {
                            now = now.getSuperClass().iterator().next();
                            if (now.getOwnedBehavior().size() > 0) {
                                b = now.getOwnedBehavior().iterator().next();
                                break;
                            }
                        }
                        else {
                            now = null;
                        }
                    }
                }
                if (b == null) {
                    missingViewpointBehavior.addViolation(viewpoint,
                            missingViewpointBehavior.getDescription());
                }
                else {
                    if (b instanceof Activity) {
                        if (!this.done.contains(b)) {
                            this.done.add(b);
                            validateActivity(b);
                        }
                    }
                }
            }
        }
        if (!viewpoints.isEmpty()) {
            List<Element> elementImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(view,
                    ElementImport.class, 1, 1);
            List<Element> packageImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(view,
                    PackageImport.class, 1, 1);
            List<Element> queries = Utils.collectDirectedRelatedElementsByRelationshipStereotype(view,
                    expose, 1, true, 1);
            elementImports.addAll(packageImports);
            elementImports.addAll(queries);
            if (elementImports.isEmpty()) {
                missingImportErrors.addViolation(view, missingImportErrors.getDescription());
            }
        }
        else if (!(view instanceof Diagram)) {
            missingViewpointErrors.addViolation(view, missingViewpointErrors.getDescription());
        }
        if (view instanceof Class) {
            for (Property p : ((Class) view).getOwnedAttribute()) {
                if (p.getType() != null && StereotypesHelper.hasStereotypeOrDerived(p.getType(), sysmlview)) {
                    validateView(p.getType(), true);
                    dg.addEdge(view, p.getType(), p);
                }
            }
        }
    }

    private void validateActivity(NamedElement activity) {
        DirectedGraph<ActivityNode, ActivityEdge> graph = new DefaultDirectedGraph<ActivityNode, ActivityEdge>(
                aef);
        List<InitialNode> inodes = findInitialNodes(activity);
        if (inodes.size() > 1) {
            multipleInitialNode.addViolation(activity, multipleInitialNode.getDescription());
        }
        if (inodes.isEmpty()) {
            missingInitialNode.addViolation(activity, missingInitialNode.getDescription());
        }
        for (InitialNode n : inodes) {
            graph.addVertex(n);
            validateNode(n, graph);
        }

        StrongConnectivityInspector<ActivityNode, ActivityEdge> sci = new StrongConnectivityInspector<ActivityNode, ActivityEdge>(
                graph);
        List<Set<ActivityNode>> cycles = sci.stronglyConnectedSets();
        if (!cycles.isEmpty()) {
            for (Set<ActivityNode> cycle : cycles) {
                if (cycle.size() > 1) {
                    this.cycles.add(cycle);
                }
            }
        }

    }

    private void validateNode(ActivityNode n, DirectedGraph<ActivityNode, ActivityEdge> graph) {
        Collection<ActivityEdge> outs = n.getOutgoing();
        if (!(n instanceof ForkNode) && outs.size() > 1) {
            multipleOutgoingFlows.addViolation(n, multipleOutgoingFlows.getDescription());
        }
        if (!(n instanceof FinalNode) && outs.isEmpty()) {
            missingOutgoingFlow.addViolation(n, missingOutgoingFlow.getDescription());
        }
        if (!(n instanceof MergeNode) && !(n instanceof JoinNode) && !(n instanceof DecisionNode)
                && n.getIncoming().size() > 1) {
            multipleIncomingFlows.addViolation(n, multipleIncomingFlows.getDescription());
        }
        if (n instanceof CallBehaviorAction) {
            Behavior b = n instanceof CallBehaviorAction ? ((CallBehaviorAction) n).getBehavior() : null;
            Collection<Stereotype> napplied = new HashSet<Stereotype>(
                    StereotypesHelper
                            .checkForAllDerivedStereotypes(n, DocGenProfile.collectFilterStereotype));
            napplied.addAll(StereotypesHelper.checkForAllDerivedStereotypes(n,
                    DocGenProfile.ignorableStereotype));
            napplied.addAll(StereotypesHelper.checkForAllDerivedStereotypes(n,
                    DocGenProfile.tableColumnStereotype));
            if (b == null) {
                if (napplied.isEmpty()) {
                    missingStereotype.addViolation(n, missingStereotype.getDescription());
                }
                else if (napplied.size() > 1) {
                    multipleStereotypes.addViolation(n, multipleStereotypes.getDescription());
                }
            }
            else {
                Collection<Stereotype> bapplied = new HashSet<Stereotype>(
                        StereotypesHelper.checkForAllDerivedStereotypes(b,
                                DocGenProfile.collectFilterStereotype));
                bapplied.addAll(StereotypesHelper.checkForAllDerivedStereotypes(b,
                        DocGenProfile.ignorableStereotype));
                bapplied.addAll(StereotypesHelper.checkForAllDerivedStereotypes(b,
                        DocGenProfile.tableColumnStereotype));
                if (napplied.isEmpty() && bapplied.isEmpty()) {
                    missingStereotype.addViolation(n, missingStereotype.getDescription());
                }
                // else if (bapplied.isEmpty())
                // mismatchStereotypeErrors.addViolation(n,
                // mismatchStereotypeErrors.getDescription());
                else if (napplied.size() > 1 || bapplied.size() > 1) {
                    multipleStereotypes.addViolation(n, multipleStereotypes.getDescription());
                }
                else if (!napplied.isEmpty() && !bapplied.isEmpty()
                        && napplied.iterator().next() != bapplied.iterator().next()) {
                    Stereotype ns = napplied.iterator().next();
                    if (!ns.getName().equals(DocGenProfile.tableAttributeColumnStereotype) &&
                            !ns.getName().equals(DocGenProfile.tableColumnStereotype) &&
                            !ns.getName().equals(DocGenProfile.tableExpressionColumnStereotype) &&
                            !ns.getName().equals(DocGenProfile.tablePropertyColumnStereotype)) {
                        mismatchStereotypeErrors.addViolation(n, mismatchStereotypeErrors.getDescription());
                    }
                }
                /*
                 * if (StereotypesHelper.hasStereotype(b,
                 * DocGenProfile.sectionStereotype) ||
                 * StereotypesHelper.hasStereotype(b,
                 * DocGenProfile.structuredQueryStereotype) ||
                 * StereotypesHelper.hasStereotypeOrDerived(b,
                 * DocGenProfile.collectionStereotype) ||
                 * StereotypesHelper.hasStereotype(b,
                 * DocGenProfile.tableStructureStereotype)) {
                 */
                if (!this.done.contains(b)) {
                    this.done.add(b);
                    validateActivity(b);
                }
                // }
            }
            validateTags(n, b);
        }
        else if (n instanceof StructuredActivityNode) {
            if (!StereotypesHelper.hasStereotype(n, DocGenProfile.structuredQueryStereotype)
                    && !StereotypesHelper.hasStereotype(n, DocGenProfile.tableStructureStereotype)
                    && StereotypesHelper.checkForAllDerivedStereotypes(n,
                    DocGenProfile.tableColumnStereotype).isEmpty()) {
                missingStereotype.addViolation(n, missingStereotype.getDescription());
            }
            validateActivity(n);
            validateTags(n, null);
        }

        for (ActivityEdge out : outs) {
            ActivityNode next = out.getTarget();
            if (graph.containsVertex(next)) {
                graph.addEdge(n, next);
                continue;
            }
            else {
                graph.addVertex(next);
                graph.addEdge(n, next);
                validateNode(next, graph);
            }
        }
    }

    private void validateTags(ActivityNode node, Behavior b) {
        for (String stereotype : requiredTags.keySet()) {
            String tag = requiredTags.get(stereotype);
            if (StereotypesHelper.hasStereotypeOrDerived(node, stereotype)
                    && StereotypesHelper.getStereotypePropertyFirst(node, stereotype, tag) == null) {
                if (b == null) {
                    missingTagValue.addViolation(node, missingTagValue.getDescription());
                    return;
                }
                else {
                    if (StereotypesHelper.hasStereotypeOrDerived(b, stereotype)
                            && StereotypesHelper.getStereotypePropertyFirst(b, stereotype, tag) == null) {
                        missingTagValue.addViolation(b, missingTagValue.getDescription());
                        return;
                    }
                }
            }
        }
    }

    private List<InitialNode> findInitialNodes(NamedElement e) {
        List<InitialNode> res = new ArrayList<InitialNode>();
        for (Element ee : e.getOwnedElement()) {
            if (ee instanceof InitialNode) {
                res.add((InitialNode) ee);
            }
        }
        return res;
    }

    public void printErrors() {
        printErrors(true);
    }

    // the 2 print errors should be consolidated...
    public void printErrors(boolean showWindow) {

        String fatal = "[FATAL] DocGen: ";
        StrongConnectivityInspector<NamedElement, Element> sci = new StrongConnectivityInspector<NamedElement, Element>(
                dg);
        List<Set<NamedElement>> cycles = sci.stronglyConnectedSets();
        if (!cycles.isEmpty()) {
            for (Set<NamedElement> cycle : cycles) {
                if (cycle.size() > 1) {
                    this.fatal = true;
                    log.log("\tView Cycle Set:");
                    for (NamedElement ne : cycle) {
                        cycleError.addViolation(ne, cycleError.getDescription());
                        log.log("\t\t" + ne.getQualifiedName());
                    }
                }
            }
        }
        if (!this.cycles.isEmpty()) {
            for (Set<ActivityNode> cycle : this.cycles) {
                if (cycle.size() > 1) {
                    this.fatal = true;
                    log.log("\tActivityNode Cycle Set:");
                    for (NamedElement ne : cycle) {
                        log.log("\t\t" + ne.getQualifiedName());
                        activityNodeCycleError.addViolation(ne,
                                activityNodeCycleError.getDescription());
                    }
                }
            }
        }
        if (this.fatal) {
            log.log(fatal + "There are loops in this document! Do not generate document!");
        }

        else if (showWindow) {
            log.log("Validation done.");
        }
        if (showWindow) {
            Utils.displayValidationWindow(project, validationOutput, "Document Validation Results");
        }
    }

    public void printErrors(PrintWriter pw) {
        String warning = "[WARNING] DocGen: ";
        String fatal = "[FATAL] DocGen: ";
        String error = "[ERROR] DocGen: ";

        /*
         * In the following code, I cast the element that violated the rule to a
         * NamedElement so that I could get the qualified name that was used in
         * the printout pre-Validation Suite (to preserve the printout). This
         * could also be done by modifying the ValidationRuleViolation class,
         * but it's not necessary. -Peter
         */
        for (ValidationRuleViolation e : multipleFirstErrors.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName() + " has multiple firsts!");
        }
        for (ValidationRuleViolation e : multipleNextErrors.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName() + " has multiple nexts!");
        }
        for (ValidationRuleViolation e : multipleContentErrors.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " has multiple (unstereotyped) dependencies!");
        }
        for (ValidationRuleViolation e : multipleViewpoints.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " conform to multiple viewpoints!");
        }
        for (ValidationRuleViolation e : missingViewpointErrors.getViolations()) {
            pw.println(warning + ((NamedElement) e.getElement()).getQualifiedName()
                    + " doesn't conform to any viewpoint!");
        }
        for (ValidationRuleViolation e : missingImportErrors.getViolations()) {
            pw.println(warning + ((NamedElement) e.getElement()).getQualifiedName() + " is missing imports!");
        }
        for (ValidationRuleViolation e : missingViewpointBehavior.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " is missing the viewpoint behavior!");
        }
        for (ValidationRuleViolation e : nonView2View.getViolations()) {
            pw.println(warning + ((NamedElement) e.getElement()).getQualifiedName()
                    + " is a nonsection but has first or next dependencies!");
        }
        for (ValidationRuleViolation e : shouldBeSection.getViolations()) {
            pw.println(warning + ((NamedElement) e.getElement()).getQualifiedName()
                    + " is a nonsection but should be a section (because it's the target of First or Next");
        }
        for (ValidationRuleViolation e : shouldNotBeSection.getViolations()) {
            pw.println(warning + ((NamedElement) e.getElement()).getQualifiedName()
                    + " is a section but should not be (because it's the target of a vanilla dependency");
        }
        for (ValidationRuleViolation e : multipleInitialNode.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " has multiple initial nodes!");
        }
        for (ValidationRuleViolation e : multipleOutgoingFlows.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " has multiple outgoing flows!");
        }
        for (ValidationRuleViolation e : multipleIncomingFlows.getViolations()) {
            pw.println(warning + ((NamedElement) e.getElement()).getQualifiedName()
                    + " has multiple incoming flows!");
        }
        for (ValidationRuleViolation e : missingInitialNode.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " is missing an initial node!");
        }
        for (ValidationRuleViolation e : multipleStereotypes.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " and/or its behavior has multiple stereotypes!");
        }
        for (ValidationRuleViolation e : mismatchStereotypeErrors.getViolations()) {
            pw.println(error + ((NamedElement) e.getElement()).getQualifiedName()
                    + " and its behavior have mismatched stereotypes!");
        }
        for (ValidationRuleViolation e : missingStereotype.getViolations()) {
            pw.println(warning + ((NamedElement) e.getElement()).getQualifiedName()
                    + " and its behavior (if present) is missing a document stereotype!");
        }

        StrongConnectivityInspector<NamedElement, Element> sci = new StrongConnectivityInspector<NamedElement, Element>(
                dg);
        List<Set<NamedElement>> cycles = sci.stronglyConnectedSets();
        if (!cycles.isEmpty()) {
            for (Set<NamedElement> cycle : cycles) {
                if (cycle.size() > 1) {
                    this.fatal = true;
                    pw.println("\tView Cycle Set:");
                    for (NamedElement ne : cycle) {
                        pw.println("\t\t" + ne.getQualifiedName());
                    }
                }
            }
        }
        if (!this.cycles.isEmpty()) {
            for (Set<ActivityNode> cycle : this.cycles) {
                if (cycle.size() > 1) {
                    this.fatal = true;
                    pw.println("\tActivityNode Cycle Set:");
                    for (NamedElement ne : cycle) {
                        pw.println("\t\t" + ne.getQualifiedName());
                    }
                }
            }
        }
        if (this.fatal) {
            pw.println(fatal + "There are loops in this document! Do not generate document!");
        }
        else {
            pw.println("Validation done.");
        }
    }

    // REVIEW -- should this function always be called instead of
    // ValidationRule.addViolation()? Consider making all rules a subclass of
    // ValidationRule that uses a subclass of ValidationRuleViolation that
    // implements Comparable so that set inclusion is efficient/elegant.

    /**
     * Add a violation for the rule only if none of the rules existing
     * violations have the same element and comment.
     *
     * @param rule
     * @param element
     * @param comment
     * @return whether a violation was added
     */
    public static boolean addViolationIfUnique(ValidationRule rule, Element element, String comment,
                                               boolean reported) {
        if (rule == null) {
            return false;
        }
        List<ValidationRuleViolation> violations = rule.getViolations();
        boolean alreadyAdded = false;
        if (violations != null) {
            for (ValidationRuleViolation v : violations) {
                if (Utils2.valuesEqual(v.getElement(), element)
                        && Utils2.valuesEqual(v.getComment(), comment)) {
                    alreadyAdded = true;
                    break;
                }
            }
        }
        if (alreadyAdded) {
            return false;
        }
        rule.addViolation(element, comment, reported);
        return true;
    }

    /**
     * Evaluate the expression and, if the violationIfConsistent flag is true
     * and the validator is not null, add a validation rule violation if the
     * expression is inconsistent.
     *
     * @param expression
     * @param context
     * @param validator
     * @param violationIfInconsistent
     * @return the result of the evaluation
     */
    public static Object evaluate(Object expression, Object context, DocumentValidator validator,
                                  boolean violationIfInconsistent) {
        ValidationRule rule = validator == null ? null : validator.getViewpointConstraintRule();
        return evaluate(expression, context, rule, violationIfInconsistent);
    }

    /**
     * Evaluate the expression and, if the violationIfConsistent flag is true
     * and the validator is not null, add a validation rule violation if the
     * expression is inconsistent.
     *
     * @param expression
     * @param context
     * @param rule
     * @param violationIfInconsistent
     * @return the result of the evaluation
     */
    public static Object evaluate(Object expression, Object context, ValidationRule rule,
                                  boolean violationIfInconsistent) {
        if (expression == null) {
            return null;
        }
        Object result = null;
        try {
            result = OclEvaluator.evaluateQuery(context, expression);
        } catch (ParserException e) {
            if (violationIfInconsistent) {
                String id = context instanceof Element ? Converters.getElementToIdConverter().apply((Element) context) : context.toString();
                String errorMessage = e.getLocalizedMessage() + " for OCL query \"" + expression + "\" on "
                        + Utils.getName(context) + (showElementIds ? "[" + id + "]" : "");
                if (rule != null && context instanceof Element) {
                    // need fixes to allow context be a collection
                    addViolationIfUnique(rule, (Element) context, errorMessage, false);
                }
                Debug.error(violationIfInconsistent, false, errorMessage);
            }
        }
        return result;
    }

    /**
     * Evaluate the constraint and, if the constraint is inconsistent and the
     * violatedIfConsistent flag is true and the validator is not null, add a
     * validation rule violation. If the constraint evaluates to false, and the
     * validator is not null, add a violation.
     *
     * @param constraint
     * @param validator
     * @param violatedIfInconsistent
     * @return the result of the evaluation
     */
    public static Boolean evaluateConstraint(Constraint constraint, DocumentValidator validator,
                                             boolean violatedIfInconsistent) {
        ValidationRule rule = validator.getViewpointConstraintRule();
        return evaluateConstraint(constraint, rule, violatedIfInconsistent);
    }

    public static int maxNumberOfViolatingElementsToShow = Integer.MAX_VALUE;
    public static boolean showElementIds = true;
    protected static boolean loggingResults = true;

    /**
     * Evaluate the constraint and, if the constraint is inconsistent and the
     * violatedIfConsistent flag is true and the validation rule is not null,
     * add a rule violation. If the constraint evaluates to false, and the rule
     * is not null, add a violation.
     *
     * @param constraint
     * @param rule
     * @param violatedIfInconsistent
     * @return the result of the evaluation
     */
    public static Boolean evaluateConstraint(Constraint constraint, ValidationRule rule,
                                             boolean violatedIfInconsistent) {
        if (constraint == null) {
            return null;
        }
        Boolean satisfied = null;
        if (constraint instanceof BasicConstraint) {
            satisfied = ((BasicConstraint) constraint).evaluate(false);
        }
        else {
            satisfied = constraint.evaluate();
        }
        if (rule == null) {
            return satisfied;
        }
        // check if constraint is violated
        if (satisfied != null && satisfied.equals(Boolean.FALSE)) {
            Element violatedElement = constraint.getViolatedConstraintElement();
            String comment;
            if (constraint instanceof BasicConstraint) {
                comment = ((BasicConstraint) constraint).toStringViolated(maxNumberOfViolatingElementsToShow,
                        showElementIds);
            }
            else {
                comment = constraint.toString();
            }
            addViolationIfUnique(rule, violatedElement, comment, constraint.isReported());
        }
        else if (violatedIfInconsistent) {
            // check if inconsistent
            // TODO -- not yet checking if the constraint is self-contradictory
            // (always false independent of context)
            Element constrainingElement = (Utils2.isNullOrEmpty(constraint.getConstrainingElements()) ? null
                    : constraint.getConstrainingElements().iterator().next());
            if (!constraint.isConsistent()) {
                String msg = ((BasicConstraint) constraint).getErrorMessage();
                if (Utils2.isNullOrEmpty(msg)) {
                    msg = "inconsistent ";
                }
                else {
                    msg = msg + " for ";
                }
                if (constraint instanceof BasicConstraint) {
                    msg = msg
                            + ((BasicConstraint) constraint).toString(maxNumberOfViolatingElementsToShow,
                            showElementIds);
                }
                else {
                    msg = msg + "constraint " + constraint;
                }
                addViolationIfUnique(rule, constrainingElement, msg, constraint.isReported());
            }
        }
        return satisfied;
    }

    /**
     * Evaluate all constraints on the execution of the constrainedObject. For
     * each constraint, add validation rule violations if evaluated to false and
     * addViolations is true or if inconsistent (because malformed or
     * self-contradictory) and addViolationForInconsistency is true.
     *
     * @param constrainedObject
     * @param actionOutput                 the result of executing the constrainedObject as an action, to
     *                                     which the constraints may applied
     * @param context                      the execution context, providing target elements that passed
     *                                     through, to which the constraints may be applied
     * @param addViolations
     * @param addViolationForInconsistency
     * @return the conjunction of the constraint evaluations (false if any are
     * false; otherwise null if any are null, else true)
     */
    public static Boolean evaluateConstraints(Object constrainedObject, Object actionOutput,
                                              GenerationContext context, boolean addViolations, boolean addViolationForInconsistency) {
        Boolean result = true; // false if any false; else, null if any null,
        // else true
        if (context.getValidator() == null) {
            return result;
        }
        result = true;
        List<Constraint> constraints = getConstraints(constrainedObject, actionOutput, context);
        if (constrainedObject instanceof Element) {
            Element e = (Element) constrainedObject;
            Debug.outln("constraints for " + e.getHumanName() + ", " + Converters.getElementToIdConverter().apply(e) + ": "
                    + MoreToString.Helper.toString(constraints));
        }
        else {
            Debug.outln("constraints for " + constrainedObject + ": "
                    + MoreToString.Helper.toString(constraints));
        }
        DocumentValidator dv = addViolations ? context.getValidator() : null;
        // If generating validation rule violations, evaluate all.
        // Result is false if any false; else, null if any null, else true.
        // MdDebug.logForce(
        // "*** Starting MDK Validate Viewpoint Constraints ***" );
        for (Constraint constraint : constraints) {
            Debug.outln("found constraint: " + MoreToString.Helper.toString(constraint));
            if (Utils2.isNullOrEmpty(constraint.getExpression())) {
                continue;
            }
            Boolean satisfied = evaluateConstraint(constraint, dv, addViolationForInconsistency);

            if (loggingResults) {
                ConstraintValidationRule.logResults(satisfied, constraint);
            }

            if (satisfied != null && satisfied.equals(Boolean.FALSE)) {
                result = false;
                if (dv == null) {
                    break;
                }
            }
            else if (satisfied == null && Boolean.TRUE.equals(result)) {
                result = null;
            }
        }
        // MdDebug.logForce(
        // "*** Finished MDK Validate Viewpoint Constraints ***" );
        return result;
    }

    /**
     * Gather all constraints applicable to the output or input of the
     * constrainedObject or the constrainedObject itself.
     *
     * @param constrainedObject
     * @param actionOutput      the result of executing the constrainedObject as an action, to
     *                          which the constraints may applied
     * @param context           the execution context, providing target elements that passed
     *                          through, to which the constraints may be applied
     * @return a list of constraints
     */
    public static List<Constraint> getConstraints(Object constrainedObject, Object actionOutput,
                                                  GenerationContext context) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        List<Object> targets = DocumentGenerator.getTargets(constrainedObject, context);
        // targets = (List< Element >)BasicConstraint.fixTargets( targets );

        List<Element> constraintElements = BasicConstraint.getConstraintElements(constrainedObject,
                BasicConstraint.Type.DYNAMIC);
        Object[] alternativeContexts = new Object[]{actionOutput, targets, constrainedObject};
        // Object[] vpcAlternativeContexts = new Object[] { targets };
        Object[] contexts = null;

        // constrained = fixTargets( constrained );
        for (Element constraintElement : constraintElements) {
            List<Object> separatelyConstrained = Utils2.newList();
            boolean isVpConstraint = BasicConstraint.elementIsViewpointConstraint(constraintElement);
            boolean isExpressionChoosable = StereotypesHelper.hasStereotypeOrDerived(constraintElement,
                    DocGenProfile.expressionChoosable);
            // if ( isVpConstraint) {

            Element vpConstraint = constraintElement;
            // contexts = vpcAlternativeContexts;
            if (!isExpressionChoosable || BasicConstraint.iterateViewpointConstrraint(vpConstraint)) {
                separatelyConstrained.addAll(targets);
            }
            else {
                separatelyConstrained.add(targets);
            }
            // } else {
            // separatelyConstrained.add( constrainedObject );
            // contexts = alternativeContexts;
            // }
            for (Object constrained : separatelyConstrained) {
                if (isVpConstraint) {
                    contexts = new Object[]{constrained};
                }
                else {
                    contexts = alternativeContexts;
                }
                Constraint c = BasicConstraint.makeConstraintFromAlternativeContexts(constraintElement,
                        contexts);
                constraints.add(c);
            }
        }
        return constraints;
    }
}
