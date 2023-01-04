package gov.nasa.jpl.mbee.mdk.docgen;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.*;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.BehavioredClassifier;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.constraint.BasicConstraint;
import gov.nasa.jpl.mbee.mdk.constraint.Constraint;
import gov.nasa.jpl.mbee.mdk.docgen.actions.SetViewpointMethodAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.generator.GenerationContext;
import gov.nasa.jpl.mbee.mdk.ocl.OclEvaluator;
import gov.nasa.jpl.mbee.mdk.util.*;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import org.eclipse.ocl.ParserException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.*;

public class ViewViewpointValidator implements Runnable {
    private final Set<Element> elements;
    private final Project project;
    private final boolean recurse;

    private Set<Element> visited;

    private ValidationSuite validationSuite = new ValidationSuite("Views & Viewpoints Validation");

    private ValidationRule viewConformanceMultiplicity = new ValidationRule("View Conformance", "View shall conform to only one viewpoint.", ViolationSeverity.ERROR);
    private ValidationRule exposedElements = new ValidationRule("Exposed Elements", "View has no exposed elements.", ViolationSeverity.WARNING);
    private ValidationRule missingViewpointMethod = new ValidationRule("Missing Viewpoint Method", "Viewpoint shall have a method.", ViolationSeverity.ERROR);
    private ValidationRule initialNodeMultiplicity = new ValidationRule("Initial Node Multiplicity", "Activity shall have only one initial node.", ViolationSeverity.ERROR);
    private ValidationRule outgoingFlowMultiplicity = new ValidationRule("Outgoing Flow Multiplicity", "Activity node shall have only one outgoing flow.", ViolationSeverity.WARNING);
    private ValidationRule incomingFlowMultiplicity = new ValidationRule("Incoming Flow Multiplicity", "Activity node shall have only one incoming flow.", ViolationSeverity.WARNING);
    private ValidationRule missingInitialNode = new ValidationRule("Missing Initial Node", "Activity shall have an initial node.", ViolationSeverity.WARNING);
    private ValidationRule stereotypeMultiplicity = new ValidationRule("Stereotype Multiplicity", "Behaviors shall have only one stereotype.", ViolationSeverity.WARNING);
    private ValidationRule missingOutgoingFlow = new ValidationRule("Missing Outgoing Flow", "Non-final activity node shall have an outgoing flow.", ViolationSeverity.WARNING);

    {
        validationSuite.addValidationRule(viewConformanceMultiplicity);
        validationSuite.addValidationRule(outgoingFlowMultiplicity);
        validationSuite.addValidationRule(exposedElements);
        validationSuite.addValidationRule(initialNodeMultiplicity);
        validationSuite.addValidationRule(incomingFlowMultiplicity);
        validationSuite.addValidationRule(missingInitialNode);
        validationSuite.addValidationRule(missingViewpointMethod);
        validationSuite.addValidationRule(missingOutgoingFlow);
        validationSuite.addValidationRule(stereotypeMultiplicity);
    }

    private DirectedGraph<NamedElement, Element> directedGraph;
    private ActivityEdgeFactory activityEdgeFactory;

    private Stereotype viewStereotype;
    private Stereotype conformStereotype;
    private Stereotype exposeStereotype;

    public ViewViewpointValidator(Set<Element> elements, Project project, boolean recurse) {
        this.elements = elements;
        this.project = project;
        this.recurse = recurse;

        viewStereotype = Utils.getViewStereotype(project);
        conformStereotype = Utils.getConformStereotype(project);
        exposeStereotype = Utils.getExposeStereotype(project);

        visited = new HashSet<>();
        activityEdgeFactory = new ActivityEdgeFactory();
        directedGraph = new DefaultDirectedGraph<>(Element.class);
    }

    public boolean isFailed() {
        return validationSuite.getValidationRules().stream().filter(rule -> rule.getSeverity() == ViolationSeverity.ERROR || rule.getSeverity() == ViolationSeverity.FATAL).anyMatch(rule -> !rule.getViolations().isEmpty());
    }

    @Override
    public void run() {
        for (Element element : elements) {
            if (StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype) && element instanceof NamedElement) {
                validateView((NamedElement) element);
            }
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

    private void validateView(NamedElement view) {
        if (directedGraph.containsVertex(view)) {
            return;
        }
        directedGraph.addVertex(view);
        List<Element> viewpoints = Utils.collectDirectedRelatedElementsByRelationshipStereotype(view, conformStereotype, 1, true, 1);
        if (viewpoints.size() > 1) {
            viewConformanceMultiplicity.addViolation(view, viewConformanceMultiplicity.getDescription());
        }
        for (Element viewpoint : viewpoints) {
            if (viewpoint != null && viewpoint instanceof Class && !visited.contains(viewpoint)) {
                visited.add(viewpoint);
                Behavior behavior = GeneratorUtils.getViewpointMethod((Class) viewpoint, project);
                if (behavior == null) {
                    Behavior nestedBehavior = ((Class) viewpoint).getOwnedBehavior().stream().findFirst().orElse(null);
                    if (nestedBehavior == null) {
                        nestedBehavior = ((Class) viewpoint).getSuperClass().stream().map(BehavioredClassifier::getOwnedBehavior).filter(behaviors -> !behaviors.isEmpty()).findFirst().orElse(Collections.emptyList()).stream().findFirst().orElse(null);
                    }
                    ValidationRuleViolation vrv = new ValidationRuleViolation(viewpoint, missingViewpointMethod.getDescription() + (nestedBehavior != null ? " Nested viewpoint found." : ""));
                    if (nestedBehavior != null) {
                        vrv.addAction(new SetViewpointMethodAction((Class) viewpoint, nestedBehavior, "Set Nested Behavior as Viewpoint Method"));
                    }
                    missingViewpointMethod.addViolation(vrv);
                }
                else if (behavior instanceof Activity && !visited.contains(behavior)) {
                    visited.add(behavior);
                    validateActivity(behavior);
                }
            }
        }
        if (!viewpoints.isEmpty()) {
            List<Element> queries = Utils.collectDirectedRelatedElementsByRelationshipStereotype(view, exposeStereotype, 1, true, 1);
            if (queries.isEmpty()) {
                exposedElements.addViolation(view, exposedElements.getDescription());
            }
        }
        if (recurse && view instanceof Class) {
            for (Property p : ((Class) view).getOwnedAttribute()) {
                if (p.getType() != null && StereotypesHelper.hasStereotypeOrDerived(p.getType(), viewStereotype)) {
                    validateView(p.getType());
                    directedGraph.addEdge(view, p.getType(), p);
                }
            }
        }
    }

    private void validateActivity(NamedElement activity) {
        DirectedGraph<ActivityNode, ActivityEdge> graph = new DefaultDirectedGraph<>(activityEdgeFactory);
        List<InitialNode> initialNodes = findInitialNodes(activity);
        if (initialNodes.size() > 1) {
            initialNodeMultiplicity.addViolation(activity, initialNodeMultiplicity.getDescription());
        }
        if (initialNodes.isEmpty()) {
            missingInitialNode.addViolation(activity, missingInitialNode.getDescription());
        }
        for (InitialNode n : initialNodes) {
            graph.addVertex(n);
            validateNode(n, graph);
        }
    }

    private void validateNode(ActivityNode n, DirectedGraph<ActivityNode, ActivityEdge> graph) {
        Collection<ActivityEdge> outgoingEdges = n.getOutgoing();
        if (!(n instanceof ForkNode) && outgoingEdges.size() > 1) {
            outgoingFlowMultiplicity.addViolation(n, outgoingFlowMultiplicity.getDescription());
        }
        if (!(n instanceof FinalNode) && outgoingEdges.isEmpty()) {
            missingOutgoingFlow.addViolation(n, missingOutgoingFlow.getDescription());
        }
        if (!(n instanceof MergeNode) && !(n instanceof JoinNode) && !(n instanceof DecisionNode) && n.getIncoming().size() > 1) {
            incomingFlowMultiplicity.addViolation(n, incomingFlowMultiplicity.getDescription());
        }
        if (n instanceof CallBehaviorAction) {
            Behavior b = ((CallBehaviorAction) n).getBehavior();
            Collection<Stereotype> napplied = n.getAppliedStereotype();
            if (b == null) {
                if (napplied.size() > 1) {
                    stereotypeMultiplicity.addViolation(n, stereotypeMultiplicity.getDescription());
                }
            }
            else {
                Collection<Stereotype> bapplied = b.getAppliedStereotype();
                if (napplied.size() > 1 || bapplied.size() > 1) {
                    stereotypeMultiplicity.addViolation(n, stereotypeMultiplicity.getDescription());
                }
                if (!this.visited.contains(b)) {
                    this.visited.add(b);
                    validateActivity(b);
                }
            }
        }
        else if (n instanceof StructuredActivityNode) {
            validateActivity(n);
        }

        for (ActivityEdge out : outgoingEdges) {
            ActivityNode next = out.getTarget();
            if (graph.containsVertex(next)) {
                graph.addEdge(n, next);
            }
            else {
                graph.addVertex(next);
                graph.addEdge(n, next);
                validateNode(next, graph);
            }
        }
    }

    private List<InitialNode> findInitialNodes(NamedElement e) {
        List<InitialNode> res = new ArrayList<>();
        for (Element ee : e.getOwnedElement()) {
            if (ee instanceof InitialNode) {
                res.add((InitialNode) ee);
            }
        }
        return res;
    }

    // REVIEW -- should this function always be called instead of
    // ValidationRule.addViolation()? Consider making all rules a subclass of
    // ValidationRule that uses a subclass of ValidationRuleViolation that
    // implements Comparable so that set inclusion is efficient/elegant.

    /**
     * Evaluate the expression and, if the violationIfConsistent flag is true
     * and the validator is not null, add a validation rule violation if the
     * expression is inconsistent.
     *
     * @param expression
     * @param context
     * @param violationIfInconsistent
     * @return the result of the evaluation
     */
    public static Object evaluate(Object expression, Object context, boolean violationIfInconsistent) {
        if (expression == null) {
            return null;
        }
        Object result;
        try {
            result = OclEvaluator.evaluateQuery(context, expression);
        } catch (ParserException e) {
            if (violationIfInconsistent) {
                String id = context instanceof Element ? Converters.getElementToIdConverter().apply((Element) context) : context.toString();
                String errorMessage = e.getLocalizedMessage() + " for OCL query \"" + expression + "\" on " + Utils.getName(context) + (showElementIds ? "[" + id + "]" : "");
                Debug.error(violationIfInconsistent, false, errorMessage);
            }
            return null;
        }
        return result;
    }

    public static boolean showElementIds = true;
    protected static boolean loggingResults = true;

    /**
     * Evaluate the constraint and, if the constraint is inconsistent and the
     * violatedIfConsistent flag is true and the validation rule is not null,
     * add a rule violation. If the constraint evaluates to false, and the rule
     * is not null, add a violation.
     *
     * @param constraint
     * @return the result of the evaluation
     */
    public static Boolean evaluateConstraint(Constraint constraint) {
        if (constraint == null) {
            return null;
        }
        Boolean satisfied;
        if (constraint instanceof BasicConstraint) {
            satisfied = ((BasicConstraint) constraint).evaluate(false);
        }
        else {
            satisfied = constraint.evaluate();
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
    public static Boolean evaluateConstraints(Object constrainedObject, Object actionOutput, GenerationContext context, boolean addViolations, boolean addViolationForInconsistency) {
        Boolean result = true; // false if any false; else, null if any null,
        // else true
        if (context.getValidator() == null) {
            return result;
        }
        result = true;
        List<Constraint> constraints = getConstraints(constrainedObject, actionOutput, context);
        if (constrainedObject instanceof Element) {
            Element e = (Element) constrainedObject;
            Debug.outln("constraints for " + e.getHumanName() + ", " + Converters.getElementToIdConverter().apply(e) + ": " + MoreToString.Helper.toString(constraints));
        }
        else {
            Debug.outln("constraints for " + constrainedObject + ": " + MoreToString.Helper.toString(constraints));
        }
        ViewViewpointValidator dv = addViolations ? context.getValidator() : null;
        // If generating validation rule violations, evaluate all.
        // Result is false if any false; else, null if any null, else true.
        // MdDebug.logForce(
        // "*** Starting MDK Validate Viewpoint Constraints ***" );
        for (Constraint constraint : constraints) {
            Debug.outln("found constraint: " + MoreToString.Helper.toString(constraint));
            if (Utils2.isNullOrEmpty(constraint.getExpression())) {
                continue;
            }
            Boolean satisfied = evaluateConstraint(constraint);

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
    public static List<Constraint> getConstraints(Object constrainedObject, Object actionOutput, GenerationContext context) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        List<Object> targets = DocumentGenerator.getTargets(constrainedObject, context);
        // targets = (List< Element >)BasicConstraint.fixTargets( targets );

        List<Element> constraintElements = BasicConstraint.getConstraintElements(constrainedObject, BasicConstraint.Type.DYNAMIC);
        Object[] alternativeContexts = new Object[]{actionOutput, targets, constrainedObject};
        // Object[] vpcAlternativeContexts = new Object[] { targets };
        Object[] contexts = null;

        // constrained = fixTargets( constrained );
        for (Element constraintElement : constraintElements) {
            List<Object> separatelyConstrained = Utils2.newList();
            boolean isVpConstraint = BasicConstraint.elementIsViewpointConstraint(constraintElement);
            boolean isExpressionChoosable = StereotypesHelper.hasStereotypeOrDerived(constraintElement, DocGenProfile.expressionChoosable);
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
                Constraint c = BasicConstraint.makeConstraintFromAlternativeContexts(constraintElement, contexts);
                constraints.add(c);
            }
        }
        return constraints;
    }

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }
}
