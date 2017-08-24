package gov.nasa.jpl.mbee.mdk.ocl;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.generator.ViewParser;
import gov.nasa.jpl.mbee.mdk.ocl.GetCallOperation.CallReturnType;
import gov.nasa.jpl.mbee.mdk.util.Debug;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Utils2;
import lpg.runtime.ParseTable;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.*;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.Query;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.internal.OCLStandardLibraryImpl;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.helper.Choice;
import org.eclipse.ocl.helper.ConstraintKind;
import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.ocl.lpg.AbstractLexer;
import org.eclipse.ocl.lpg.AbstractParser;
import org.eclipse.ocl.lpg.ProblemHandler;
import org.eclipse.ocl.util.OCLUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for encapsulating the OCL query and constraint evaluations.
 * Note that OCL query subsumes constraints, so evaluateQuery can always be
 * called in place of checkConstraint.
 * <p>
 * Here's an example of how to use OclEvaluator, setting up the environment as
 * well // create custom environment facto DocGenEnvironmentFactory envFactory = new
 * DocGenEnvironmentFactory();
 * <p>
 * // create custom operation DocGenOperationInstance doi = new
 * DocGenOperationInstance(); doi.setName("regexMatch");
 * doi.setAnnotationName("DocGenEnvironment"); EParameter parm =
 * EcoreFactory.eINSTANCE.createEParameter(); parm.setName("pattern");
 * doi.addParameter(parm);
 * <p>
 * // essentially set the actual operation as function pointer
 * doi.setOperation(new CallOperation() {
 *
 * @author cinyoung
 * @Override public Object callOperation(Object source, Object[] args) { Pattern
 * pattern = Pattern.compile((String) args[0]); Matcher matcher =
 * pattern.matcher((String) source);
 * <p>
 * return matcher.matches() ? matcher.group() : null; } });
 * <p>
 * // add custom operation to environment and evaluation environment
 * envFactory.getDgEnvironment().addDgOperation(doi);
 * envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
 * <p>
 * // create the ocl evaluator
 * OclEvaluator.createOclInstance(envFactory);
 * <p>
 * // create query and evaluate String oclquery =
 * "name.regexMatch('DocGen Templating') <> null"; Object result =
 * OclEvaluator.evaluateQuery(rootEObject, oclquery, verbose);
 * <p>
 * <p>
 * TODO: Need to expand so it can handle multiple contexts
 */
public class OclEvaluator {
    public static OclEvaluator instance = null;

    public enum QueryStatus {
        PARSE_EXCEPTION, VALID_OCL, INVALID_OCL, NO_QUERY
    }

    private OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl;
    private QueryStatus queryStatus = QueryStatus.NO_QUERY;
    public static boolean isVerboseDefault = Debug.isOn();
    protected BasicDiagnostic basicDiagnostic = null;
    protected OCLHelper<EClassifier, ?, ?, Constraint> helper = null;
    private ProblemHandler problemHandler = null;
    protected DocGenEnvironmentFactory environmentFactory = new DocGenEnvironmentFactory();
    public String errorMessage = "";

    // public static Set< DocGenOperationInstance > opsCache = null;
    // public static boolean useCachedOps = true;

    public OclEvaluator() {
        // make sure order is correct
        getEnvironmentFactory();
        getEnvironment();
        getOcl();
    }

    public void createOclInstance(DocGenEnvironmentFactory envFactory) {
        ocl = OCL.newInstance(envFactory);
    }

    protected static boolean notNullOrEndInQuestion(String expr) {
        return (!Utils2.isNullOrEmpty(expr) && expr.trim().lastIndexOf("?") < expr.trim().length() - 1);
    }

    public static String queryElementToStringExpression(Element query) {
        String expr = null;
        Object o = GeneratorUtils.getStereotypePropertyFirst(query, DocGenProfile.expressionChoosable, "expression",
                DocGenProfile.PROFILE_NAME, null);
        expr = queryObjectToStringExpression(o);
        if (notNullOrEndInQuestion(expr)) {
            return expr;
        }
        if (query instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint) {
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint c = (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint) query;
            ValueSpecification v = c.getSpecification();
            if (v != null) {
                expr = DocGenUtils.fixString(v);
                if (notNullOrEndInQuestion(expr)) {
                    return expr;
                }
                expr = v.get_representationText();
                if (notNullOrEndInQuestion(expr)) {
                    return expr;
                }
                expr = v.toString();
                if (notNullOrEndInQuestion(expr)) {
                    return expr;
                }
                if (v instanceof OpaqueExpression) {
                    OpaqueExpression oe = (OpaqueExpression) v;
                    List<String> list = oe.getBody();
                    if (!Utils2.isNullOrEmpty(list)) {
                        expr = queryCollectionToStringExpression(list);
                        if (notNullOrEndInQuestion(expr)) {
                            return expr;
                        }
                    }
                    Expression x = oe.getExpression();
                    if (x != null) {
                        expr = x.get_representationText();
                        if (notNullOrEndInQuestion(expr)) {
                            return expr;
                        }
                        expr = x.toString();
                        if (notNullOrEndInQuestion(expr)) {
                            return expr;
                        }
                    }
                }
            }
        }
        else {
            // Assume it's a LiteralString or some other value expression.
            expr = DocGenUtils.fixString(query, false);
        }
        return expr;
    }

    public static String queryCollectionToStringExpression(Collection<?> queryColl) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Object q : queryColl) {
            if (first) {
                first = false;
            }
            else {
                sb.append(" and ");
            }
            String expr = queryObjectToStringExpression(q);
            sb.append("(" + expr + ")"); // REVIEW -- Do parentheses work??!
        }
        String exprString = sb.toString();
        return exprString;
    }

    /**
     * @param query
     * @return
     */
    public static String queryObjectToStringExpression(Object query) {
        if (query == null) {
            return null;
        }
        String exprString = null;
        if (query instanceof Element) {
            Element element = (Element) query;
            exprString = queryElementToStringExpression(element);
        }
        else if (query instanceof String) {
            exprString = (String) query;
        }
        else if (query instanceof Collection) {
            Collection<?> queryColl = (Collection<?>) query;
            exprString = queryCollectionToStringExpression(queryColl);
        }
        else if (query != null) {
            exprString = query.toString();
        }
        return exprString;
    }

    /**
     * Evaluates the specified query given a particular context
     *
     * @param context EObject of the context that the query should be run against
     *                (e.g., self)
     * @param query   object to convert to a valid OCL string to be evaluated in the
     *                context
     * @return Object of the result whose type should be known by the caller
     * @throws ParserException
     */
    public static Object evaluateQuery(Object context, Object query) throws ParserException {
        // public static Object evaluateQuery(EObject context, Object query) {
        return evaluateQuery(context, queryObjectToStringExpression(query));
    }

    /**
     * Evaluates the specified query given a particular context
     *
     * @param context     EObject of the context that the query should be run against
     *                    (e.g., self)
     * @param queryString Valid OCL string that to be evaluated in the context
     * @return Object of the result whose type should be known by the caller
     * @throws ParserException
     */
    public static Object evaluateQuery(Object context, String queryString) throws ParserException {
        // public static Object evaluateQuery(EObject context, String
        // queryString) {
        return evaluateQuery(context, queryString, isVerboseDefault);
    }

    public Object evaluateQueryNoSetup(Object context, String queryString, boolean verbose)
            throws ParserException {
        Object result = null;
        OCLExpression<EClassifier> query = null;
        try {
            query = getHelper().createQuery(queryString);
        } catch (ParserException e) {
            queryStatus = QueryStatus.PARSE_EXCEPTION;
            if (verbose) {
                e.printStackTrace();
                Debug.outln("my diag = " + getBasicDiagnostic());
                //Object analyzer = getBasicDiagnostic().getData().get(0);
                //Debug.outln("analyzer = " + analyzer);
                Debug.outln("ProblemHandler = " + getProblemHandler());
                if (getProblemHandler() != null) {
                    int offset = getProblemHandler().getErrorReportLineOffset();
                    Debug.outln("getErrorReportLineOffset() = " + offset);
                    this.errorMessage = Utils2.toString(ProblemHandler.ERROR_MESSAGES);
                    Debug.outln("Error messages = " + errorMessage);
                    AbstractParser parser = getProblemHandler().getParser();
                    Debug.outln("parser = " + parser);
                    if (parser != null) {
                        ParseTable pt = parser.getParseTable();
                        Debug.outln("ParseTable = " + pt);
                        AbstractLexer lexer = parser.getLexer();
                        Debug.outln("lexer = " + lexer);
                        if (lexer != null) {
                            pt = lexer.getParseTable();
                            Debug.outln("lexer ParseTable = " + pt);
                        }
                    }
                }
            }
            // if ( !this.errorMessage.matches( ".*[A-Za-z]" ) ) {
            this.errorMessage = e.getLocalizedMessage();
            // }
            throw e;// new ParserException( getBasicDiagnostic() );
        } catch (NullPointerException ignored) {
        }
        result = getOcl().evaluate(context, query);
        System.out.println(query.toString());
        if (getOcl().isInvalid(result)) {
            queryStatus = QueryStatus.INVALID_OCL;
        }
        return result;
    }

    /**
     * Evaluates the specified query given a particular context
     *
     * @param context     EObject of the context that the query should be run against
     *                    (e.g., self)
     * @param queryString Valid OCL string that to be evaluated in the context
     * @param verbose     Turns on OCL debugging if true, off if false
     * @return Object of the result whose type should be known by the caller
     * @throws ParserException
     */
    public static Object evaluateQuery(Object context, String queryString, boolean verbose)
            throws ParserException {
        OclEvaluator ev = new OclEvaluator();
        instance = ev;
        ev.setupEnvironment();
        if (queryString == null) {
            return null;
        }

        // create the ocl evaluator
        // boolean wasOn = Debug.isOn(); Debug.turnOn(); verbose = true;
        ev.setOclTracingEnabled(verbose);
        ev.queryStatus = QueryStatus.VALID_OCL;

        if (context == null) {
            ev.getHelper().setContext(OCLStandardLibraryImpl.INSTANCE.getOclVoid());
        }
        else if (context instanceof EObject) {
            ev.getHelper().setContext(((EObject) context).eClass());
        }
        else if (context instanceof Collection) {
            ev.getHelper().setContext(OCLStandardLibraryImpl.INSTANCE.getSequence());
        }

        Object result = null;

        ev.basicDiagnostic = null;
        ev.problemHandler = null;

        result = ev.evaluateQueryNoSetup(context, queryString, verbose);

        Debug.outln("evaluateQuery(context=" + DocGenUtils.fixString(context) + ", queryString="
                + queryString + ", verbose=" + verbose + ") = " + DocGenUtils.fixString(result));
        // if ( !wasOn ) Debug.turnOff();
        return result;
    }

    public static List<GetCallOperation> addOperation(String[] names, EClassifier callerType,
                                                      EClassifier returnType, EClassifier parmType, String parmName, boolean zeroArgToo,
                                                      boolean singularNameReturnsOnlyOne, CallReturnType opType, DocGenEnvironmentFactory envFactory) {
        // GetCallOperation op = new GetCallOperation();
        // op.resultType = opType;
        ArrayList<GetCallOperation> ops = new ArrayList<GetCallOperation>();

        GetCallOperation op = null;

        boolean someEndWithS = false;
        boolean notAllEndWithS = false;
        if (singularNameReturnsOnlyOne) {
            for (String name : names) {
                if (!Utils2.isNullOrEmpty(name)) {
                    if (name.trim().substring(name.length() - 1).toLowerCase().equals("s")) {
                        someEndWithS = true;
                    }
                    else {
                        notAllEndWithS = true;
                    }
                }
            }
        }
        for (String name : names) {
            op = new GetCallOperation();
            op.resultType = opType;
            boolean endsWithS = false;
            boolean oneChar = false;

            // Create the one parameter for the operation
            EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
            parm.setName(parmName);
            parm.setEType(parmType);

            if (someEndWithS && notAllEndWithS) {
                oneChar = name.trim().length() == 1;
                endsWithS = name.trim().substring(name.length() - 1).toLowerCase().equals("s");
                if (endsWithS || oneChar) {
                    op.onlyOneForAll = false;
                    op.onlyOnePer = false;
                }
                else {
                    op.onlyOneForAll = false;
                    op.onlyOnePer = true;
                }
            }
            // Create the one-parameter operation
            DocGenOperationInstance.addOperation(name, "DocGenEnvironment",
                    envFactory, callerType,
                    returnType, op, parm);
            ops.add(op);

            if (zeroArgToo) {
                // Create the zero-parameter operation
                op = new GetCallOperation();
                op.resultType = opType;
                if (singularNameReturnsOnlyOne && someEndWithS && notAllEndWithS) {
                    if (endsWithS || oneChar) {
                        op.onlyOneForAll = false;
                        op.onlyOnePer = false;
                    }
                    else {
                        op.onlyOneForAll = false;
                        op.onlyOnePer = true;
                    }
                }
                DocGenOperationInstance.addOperation(name, "DocGenEnvironment",
                        envFactory, callerType,
                        returnType, op);
                ops.add(op);
            }
        }
        return ops;
    }

    protected static void addRegexMatchOperation(DocGenEnvironmentFactory envFactory) {

        // create custom operation
        DocGenOperationInstance doi = new DocGenOperationInstance();
        doi.setName("regexMatch");
        doi.setAnnotationName("DocGenEnvironment");
        EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
        parm.setName("pattern");
        doi.addStringParameter(parm);
        doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getString());
        doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getString());

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            @Override
            public Object callOperation(Object source, Object[] args) {
                Pattern pattern = Pattern.compile((String) args[0], Pattern.MULTILINE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher((String) source);

                return matcher.matches() ? matcher.group() : null;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addLogOperation(DocGenEnvironmentFactory envFactory, boolean addArg, boolean addColorArg) {
        addLogOperation(envFactory, addArg, addColorArg, false, false);
        addLogOperation(envFactory, addArg, addColorArg, true, false);
        addLogOperation(envFactory, addArg, addColorArg, false, true);
        addLogOperation(envFactory, addArg, addColorArg, true, true);
    }

    protected static void addLogOperation(DocGenEnvironmentFactory envFactory,
                                          boolean addArg, boolean addColorArg,
                                          boolean asSequence,
                                          boolean fromSequence) {  // Are these last two args helpful???

        // create custom operation
        DocGenOperationInstance doi = new DocGenOperationInstance();
        doi.setName("log");// + (asSequence ? "S" : "" ) + (fromSequence ? "F" : "") );
        doi.setAnnotationName("DocGenEnvironment");
        if (addArg) {
            EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
            parm.setName("toLog");
            if (asSequence) {
                doi.addParameter(parm, OCLStandardLibraryImpl.INSTANCE.getSequence());
            }
            else {
                doi.addParameter(parm, OCLStandardLibraryImpl.INSTANCE.getOclAny());
            }
            if (addColorArg) {
                parm = EcoreFactory.eINSTANCE.createEParameter();
                parm.setName("color");
                doi.addParameter(parm, OCLStandardLibraryImpl.INSTANCE.getOclAny());
            }
        }
        if (fromSequence) {
            doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
        }
        else {
            doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getSequence());
        }
        doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getOclAny());

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            @Override
            public Object callOperation(Object source, Object[] args) {
                if (args != null && args.length > 0) {
                    Object o = args[0];
                    Object colorObj = null;
                    if (args.length >= 2) {
                        colorObj = args[1];
                    }
                    else if (source != null && Utils.isColor(o)) {
                        colorObj = args[0];
                        o = source;
                    }
                    Utils.log(o, colorObj);
                }
                else if (source != null) {
                    Utils.log(source);
                }
                return source;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addGetOperation(DocGenEnvironmentFactory envFactory) {

        // create custom operation
        DocGenOperationInstance doi = new DocGenOperationInstance();
        doi.setName("get");
        doi.setAnnotationName("DocGenEnvironment");
        EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
        parm.setName("nameOrId");
        doi.addStringParameter(parm);
        doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
        doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getOclAny());

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            @Override
            public Object callOperation(Object source, Object[] args) {
                if (args.length < 1) {
                    return null;
                }
                if (args.length > 1) {
                    return null;
                }
                String nameOrId = (String) args[0];

                // try id
                BaseElement e = Converters.getIdToElementConverter()
                        .apply(nameOrId, Application.getInstance().getProject());
                if (e != null) {
                    return e;
                }

                // try child
                if (source != null && source instanceof Element) {
                    e = ElementFinder.findOwnedElementByName((Element) source, nameOrId);
                    if (e != null) {
                        return e;
                    }
                }

                // try qualified name
                //TODO @donbot verify that this usage can be removed, or that it's harmless to restrict search to only primary model
                e = ElementFinder.getElementByQualifiedName(nameOrId, Application.getInstance().getProject());
                if (e != null) {
                    return e;
                }

                // try searching everything
                List<Element> results = Utils.findByName(Application.getInstance().getProject(), nameOrId, true);
                if (!Utils2.isNullOrEmpty(results)) {
                    return results.get(0);
                }
                return null;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addRunOperation(DocGenEnvironmentFactory envFactory,
                                          EClassifier argType) {

        // create custom operation
        DocGenOperationInstance doi = new DocGenOperationInstance();
        doi.setName("run");
        doi.setAnnotationName("DocGenEnvironment");
        EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
        parm.setName("input");

        if (argType != null) {
            doi.addParameter(parm, argType);
            doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
            doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
        }

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            boolean runItemsInCollectionIndividually = true;

            @Override
            public Object callOperation(Object source, Object[] args) {

                if (!(source instanceof Element)) {
                    return null;
                }
                Element sourceElement = (Element) source;

                // If the source is a view, parse it.
                if (sourceElement instanceof Class
                        && StereotypesHelper.hasStereotypeOrDerived(sourceElement,
                        DocGenProfile.viewStereotype)) {
                    DocumentValidator dv = new DocumentValidator(sourceElement);
                    DocumentGenerator dg =
                            new DocumentGenerator(sourceElement, dv, null);
                    ViewParser vp = new ViewParser(dg, true, true, dg.getDocument(), sourceElement);
                    return vp.parse();
                }

                // Need to parse the behavior of the Viewpoint, not the
                // Viewpoint itself.
                if (sourceElement instanceof Class
                        && StereotypesHelper.hasStereotypeOrDerived(sourceElement,
                        DocGenProfile.viewpointStereotype)) {
                    sourceElement = ((Class) sourceElement).getClassifierBehavior();
                }

                Object input = args[0];

                // Allow for activity and target input to be reversed.
                // For example, run may be called as
                //   viewpoint1.run(Sequence{element1, element2}) or as
                //   Sequence{element1, element2}.run(viewpoint1)
                if (GeneratorUtils.findInitialNode(sourceElement) == null
                        && input instanceof Element
                        && GeneratorUtils.findInitialNode((Element) input) != null) {
                    // Change to run Collection of items together as a single
                    // input since user can use "." or "->" to specify to the Ocl
                    // parser which way to handle it.
                    runItemsInCollectionIndividually = false;
                    // Call with swapped source/input, using the original source
                    // since sourceElement may have been reassigned.
                    return callOperation(input, new Object[]{source});
                }

                List<Object> inputs = new ArrayList<Object>();
                if (runItemsInCollectionIndividually
                        && input instanceof Collection) {
                    inputs.addAll((Collection<?>) input);
                }
                else {
                    inputs.add(input);
                }
                DocumentValidator dv = new DocumentValidator(sourceElement);
                DocumentGenerator dg =
                        new DocumentGenerator(sourceElement, dv, null);
                dg.getContext().pushTargets(inputs);
                Object result =
                        dg.parseActivityOrStructuredNode(sourceElement,
                                dg.getDocument());
                return result;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addEvalOperation(DocGenEnvironmentFactory envFactory, String opName) {

        // create custom operation
        DocGenOperationInstance doi = new DocGenOperationInstance();
        doi.setName(opName);
        doi.setAnnotationName("DocGenEnvironment");
        EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
        parm.setName("expression");
        doi.addStringParameter(parm);
        doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
        doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getOclAny());

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            @Override
            public Object callOperation(Object source, Object[] args) {
                String expression = (String) args[0];
                Object result = null;
                try {
                    result = evaluateQuery(source, expression, isVerboseDefault());
                } catch (Throwable e) {
                    Debug.error(true, false, e.getLocalizedMessage());
                }
                return result;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addExpressionOperation(final String opName, final String expression,
                                                 DocGenEnvironmentFactory envFactory) {
        // create custom operation
        DocGenOperationInstance doi = new DocGenOperationInstance();
        doi.setName(opName);
        doi.setAnnotationName("DocGenEnvironment");

        // REVIEW -- Can we do better than OclAny? Would it help avoid needing
        // to add oclAsType() before and after?
        doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
        doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getOclAny());

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            @Override
            public Object callOperation(Object source, Object[] args) {
                Object result = null;
                try {
                    result = evaluateQuery(source, expression, isVerboseDefault());
                } catch (Throwable e) {
                    Debug.error(true, false, e.getLocalizedMessage());
                }
                return result;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

//    /**
//     * @param result
//     * @return an OclCollection
//     */
//    public static Object makeCollectionOcl( Object result ) {
//        if ( result instanceof Bag ) return result;
//        if ( result instanceof LinkedHashSet ) return result;
//        if ( result instanceof ArrayList ) {
//            result = CollectionUtil.asSequence( (Collection< ? >)result );
//        } else if ( result instanceof HashSet ) {
//            result = CollectionUtil.asSet( (Collection< ? >)result );
//        } else if ( result instanceof Collection ) {
//            result = CollectionUtil.asBag( (Collection< ? >)result );
//        }
//        return result;
//    }

    static EClassifier getGenericCallerType() {
        return OCLStandardLibraryImpl.INSTANCE.getOclAny();
    }

    protected static void addROperation(DocGenEnvironmentFactory envFactory) {
        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[]{"relationship", "relationships", "r"}, callerType, returnType, stringType,
                "relationship", true, true, CallReturnType.RELATIONSHIP, envFactory);
    }

    protected static void addMOperation(DocGenEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[]{"member", "members", "m"}, callerType, returnType, stringType, "member",
                true, false, CallReturnType.MEMBER, envFactory);
    }

    protected static void addTOperation(DocGenEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[]{"type", "types", "t"}, callerType, returnType, stringType, "type", true,
                true, CallReturnType.TYPE, envFactory);
    }

    protected static void addVOperation(DocGenEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getOclAny();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[]{"value", "values", "v"}, callerType, returnType, stringType, "value", true,
                true, CallReturnType.VALUE, envFactory);
    }

    protected static void addDefaultOperation(DocGenEnvironmentFactory envFactory) {
        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getString();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[]{"default"}, callerType, returnType, stringType,
                "default", true, true, CallReturnType.DEFAULT, envFactory);
    }

    protected static void addSOperation(DocGenEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        List<GetCallOperation> ops = addOperation(new String[]{"stereotype", "stereotypes", "s"},
                callerType, returnType, stringType, "stereotype", true, true, CallReturnType.TYPE, envFactory);
        for (GetCallOperation op : ops) {
            op.alwaysFilter = new Object[]{"Stereotype"};
        }
    }

    /**
     * Add n(), name(), and names() OCL shortcuts with and without arguments.
     *
     * @param envFactory
     */
    protected static void addNOperation(DocGenEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getString();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[]{"name", "n"}, callerType, returnType, stringType, "name", true, true,
                CallReturnType.NAME, envFactory);
    }

    /**
     * Add n(), name(), and names() OCL shortcuts with and without arguments.
     *
     * @param envFactory
     */
    protected static void addOOperation(DocGenEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[]{"owner", "owners", "o"}, callerType, returnType, stringType, "owner", true, true,
                CallReturnType.OWNER, envFactory);
    }

    /**
     * @param exprString an OCL expression
     * @return an error message if the parse failed; otherwise return null
     */
    public String checkParsable(String exprString) {
        try {
            OCLExpression<EClassifier> query = getHelper().createQuery(exprString);
            if (query == null) {
                throw new Exception();
            }
        } catch (ParserException e) {
            return e.getLocalizedMessage();
        } catch (Throwable e) {
            return "query is null: \"" + exprString + "\"";
        }
        return null;
    }

    /**
     * Find Expressions in model and add them as blackbox shortcuts.
     * ExpressionLibraries are retained
     *
     * @param envFactory
     */
    protected static void addExpressionOperations(DocGenEnvironmentFactory envFactory) {
        // add each of the elements with the Expression stereotype as shortcut/blackbox functions
        List<Element> expressions = StereotypesHelper.getExtendedElements(Utils.getExpressionStereotype(Application.getInstance().getProject()));
        for (Element expression : expressions) {
            // function name can't have spaces and strange characters; e.g. the name "four+five" would be parsed as a sum operation.
            String name = Utils.getName(expression);
            name = name.replaceAll("[^A-Za-z0-9_]+", "");
            String exprString = queryElementToStringExpression(expression);
            if (!Utils2.isNullOrEmpty(name)) {
                try {
                    addExpressionOperation(name, exprString, envFactory);
                } catch (Throwable e) {
                    Debug.error(true, false, "Could not add " + name + " OCL shortcut with expression \"" + exprString + "\". " + e.getLocalizedMessage());
                }
            }
        }
    }

    public DocGenEnvironmentFactory getEnvironmentFactory() {
        if (environmentFactory == null) {
            environmentFactory = new DocGenEnvironmentFactory();
        }
        return environmentFactory;
    }

    public DocGenEvaluationEnvironment getEvaluationEnvironment() {
        return getEnvironmentFactory().getDgEvaluationEnvironment();
    }

    public DocGenEnvironment getEnvironment() {
        return getEnvironmentFactory().getDgEnvironment();
    }

    protected DocGenEnvironmentFactory setupEnvironment() {
        // set up the customized environment
        // add custom OCL functions
        addRegexMatchOperation(getEnvironmentFactory());
        addEvalOperation(getEnvironmentFactory(), "eval");
        addEvalOperation(getEnvironmentFactory(), "evaluate");
        addEvalOperation(getEnvironmentFactory(), "e");
        addRunOperation(getEnvironmentFactory(),
                OCLStandardLibraryImpl.INSTANCE.getOclAny());
        addRunOperation(getEnvironmentFactory(),
                OCLStandardLibraryImpl.INSTANCE.getSequence());
        addRunOperation(getEnvironmentFactory(), null);
        addGetOperation(getEnvironmentFactory());
        addLogOperation(getEnvironmentFactory(), true, false);
        addLogOperation(getEnvironmentFactory(), true, true);
        addLogOperation(getEnvironmentFactory(), false, false);
        // add one-letter custom OCL operations
        addROperation(getEnvironmentFactory());
        addMOperation(getEnvironmentFactory());
        addTOperation(getEnvironmentFactory());
        addSOperation(getEnvironmentFactory());
        addNOperation(getEnvironmentFactory());
        addOOperation(getEnvironmentFactory());
        addVOperation(getEnvironmentFactory());
        addDefaultOperation(getEnvironmentFactory());

        addExpressionOperations(getEnvironmentFactory());

        return getEnvironmentFactory();
    }

    public List<Choice> commandCompletionChoices(OCLHelper<EClassifier, ?, ?, Constraint> helper,
                                                 Object context, String oclInput) {
        EClassifier helperContext = null;
        if (context instanceof EObject) {
            helperContext = ((EObject) context).eClass();
        }
        else if (context instanceof Collection) {
            helperContext = OCLStandardLibraryImpl.INSTANCE.getSequence();
        }

        getHelper().setContext(helperContext != null ? helperContext : OCLStandardLibraryImpl.INSTANCE.getOclVoid());
        List<Choice> choices = getHelper().getSyntaxHelp(ConstraintKind.INVARIANT, oclInput);
        Debug.outln("Completion choices for OCL expression \"" + oclInput + "\" = " + choices);
        return choices;
    }

    public List<String> commandCompletionChoiceStrings(OCLHelper<EClassifier, ?, ?, Constraint> helper,
                                                       Object context, String oclInput, int depth) {
        Object result = null;
        try {
            result = evaluateQuery(context, oclInput, Debug.isOn());
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (result == null) {
            return Collections.emptyList();
        }
        List<Choice> choiceList = commandCompletionChoices(helper, context, oclInput);
        List<String> newChoiceStringList = new ArrayList<String>();
        boolean canExtend = depth > 0;
        for (Choice c : choiceList) {
            String newChoiceString = oclInput;
            if (canExtend) {
                newChoiceString += "." + c.getName();
            }
            newChoiceStringList.add(newChoiceString);
            List<String> extensions = null;
            if (depth > 1) {
                extensions = commandCompletionChoiceStrings(helper, context, newChoiceString, depth - 1);
            }
            canExtend = !Utils2.isNullOrEmpty(extensions);
            if (!canExtend) {
                newChoiceStringList.add(newChoiceString);
            }
            else {
                newChoiceStringList.addAll(extensions);
            }
        }
        return newChoiceStringList;
    }

    public List<String> commandCompletionChoiceStrings(OCLHelper<EClassifier, ?, ?, Constraint> helper,
                                                       Object context, String oclInput) {
//        boolean wasOn = Debug.isOn();
//        Debug.turnOn();
        List<String> choiceList = new ArrayList<String>();
        List<Choice> choices = commandCompletionChoices(helper, context, oclInput);
        for (Choice next : choices) {
            choiceList.add(next.getName() + " : " + next.getDescription());
            switch (next.getKind()) {
                case OPERATION:
                case SIGNAL:
                    // the description is already complete
                    // Debug.outln( next.getDescription() );
                    // break;
                case PROPERTY:
                case ENUMERATION_LITERAL:
                case VARIABLE:
                    Debug.outln(next.getName() + " : " + next.getDescription());
                    // choiceList.add( next.getName() );
                    break;
                default:
                    // choiceList.add( next.getName() );
                    Debug.outln(next.getName());
                    break;
            }
        }
//        if (!wasOn)
//            Debug.turnOff();
        Debug.outln("choices = " + choiceList.toString());
        return choiceList;
    }

    /**
     * Evaluates the specified invariant (constraint given a particular context)
     * <p>
     * Note that the evaluateQuery is more generic and can handle invariants as
     * well
     *
     * @param context          EObject of the context that the constraint should be checked
     *                         against
     * @param constraintString Valid OCL constraint string to be checked
     * @param verbose          Turns on OCL debugging if true, off if false
     * @return true if constraint is satisfied, false otherwise
     */
    public boolean checkConstraint(EObject context, String constraintString, boolean verbose) {
        setOclTracingEnabled(verbose);

        queryStatus = QueryStatus.VALID_OCL;

        OCLHelper<EClassifier, ?, ?, Constraint> helper = getOcl().createOCLHelper();
        helper.setContext(context.eClass());

        boolean ok = false;
        Constraint constraint = null;
        try {
            constraint = helper.createInvariant(constraintString);
        } catch (ParserException e) {
            queryStatus = QueryStatus.PARSE_EXCEPTION;
            e.printStackTrace();
            return ok;
        }

        if (constraint != null) {
            Query<EClassifier, EClass, EObject> eval = getOcl().createQuery(constraint);
            ok = eval.check(context);
        }

        return ok;
    }

    /**
     * Utility method for toggling OCL tracing/debugging information
     *
     * @param verbose true if tracing should be enabled, false otherwise
     */
    private void setOclTracingEnabled(boolean verbose) {
        getOcl().setEvaluationTracingEnabled(verbose);
        getOcl().setParseTracingEnabled(verbose);
    }

    /**
     * Returns the query status.
     *
     * @return
     */
    public QueryStatus getQueryStatus() {
        return queryStatus;
    }

    /**
     * Simple rollup function that determines whether a query was executed
     * properly or not
     *
     * @return
     */
    public boolean isValid() {
        switch (getQueryStatus()) {
            case VALID_OCL:
                return true;
            default:
                return false;
        }
    }

    public OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> getOcl() {
        if (ocl == null) {
            createOclInstance(getEnvironmentFactory());
        }
        return ocl;
    }

    public void setOcl(OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl) {
        this.ocl = ocl;
    }

    public static boolean isVerboseDefault() {
        return isVerboseDefault;
    }

    public static void setVerboseDefault(boolean isVerboseDefault) {
        OclEvaluator.isVerboseDefault = isVerboseDefault;
    }

    public BasicDiagnostic getBasicDiagnostic() {
        if (basicDiagnostic == null) {
            if (getHelper() != null) {
                Diagnostic diag = getHelper().getProblems();
                if (diag instanceof BasicDiagnostic) {
                    setBasicDiagnostic((BasicDiagnostic) diag);
                }
            }
        }
        return basicDiagnostic;
    }

    public void setBasicDiagnostic(BasicDiagnostic basicDiagnostic) {
        this.basicDiagnostic = basicDiagnostic;
    }

    /**
     * @return
     */
    public ProblemHandler getProblemHandler() {
        if (problemHandler == null) {
            if (getOcl() != null) {
                setProblemHandler(OCLUtil.getAdapter(getOcl().getEnvironment(), ProblemHandler.class));
            }
        }
        return problemHandler;
    }

    /**
     * @param problemHandler
     */
    public void setProblemHandler(ProblemHandler problemHandler) {
        this.problemHandler = problemHandler;
    }

    public void setQueryStatus(QueryStatus queryStatus) {
        this.queryStatus = queryStatus;
    }

    public OCLHelper<EClassifier, ?, ?, Constraint> getHelper() {
        if (helper == null) {
            if (getOcl() != null) {
                setHelper(getOcl().createOCLHelper());
            }
        }
        return helper;
    }

    public void setHelper(OCLHelper<EClassifier, ?, ?, Constraint> helper) {
        this.helper = helper;
    }
}
