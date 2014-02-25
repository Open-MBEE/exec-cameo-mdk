/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.generator.ViewParser;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.ocl.GetCallOperation.CallReturnType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lpg.runtime.ParseTable;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EcoreFactory;
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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * Utility class for encapsulating the OCL query and constraint evaluations.
 * Note that OCL query subsumes constraints, so evaluateQuery can always be
 * called in place of checkConstraint.
 * 
 * Here's an example of how to use OclEvaluator, setting up the environment as
 * well // create custom environment facto DgEnvironmentFactory envFactory = new
 * DgEnvironmentFactory();
 * 
 * // create custom operation DgOperationInstance doi = new
 * DgOperationInstance(); doi.setName("regexMatch");
 * doi.setAnnotationName("DocGenEnvironment"); EParameter parm =
 * EcoreFactory.eINSTANCE.createEParameter(); parm.setName("pattern");
 * doi.addParameter(parm);
 * 
 * // essentially set the actual operation as function pointer
 * doi.setOperation(new CallOperation() {
 * 
 * @Override public Object callOperation(Object source, Object[] args) { Pattern
 *           pattern = Pattern.compile((String) args[0]); Matcher matcher =
 *           pattern.matcher((String) source);
 * 
 *           return matcher.matches() ? matcher.group() : null; } });
 * 
 *           // add custom operation to environment and evaluation environment
 *           envFactory.getDgEnvironment().addDgOperation(doi);
 *           envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
 * 
 *           // create the ocl evaluator
 *           OclEvaluator.createOclInstance(envFactory);
 * 
 *           // create query and evaluate String oclquery =
 *           "name.regexMatch('DocGen Templating') <> null"; Object result =
 *           OclEvaluator.evaluateQuery(rootEObject, oclquery, verbose);
 * 
 * 
 *           TODO: Need to expand so it can handle multiple contexts
 * @author cinyoung
 * 
 */
public class OclEvaluator {
    public static OclEvaluator instance = null;

    public enum QueryStatus {
        PARSE_EXCEPTION, VALID_OCL, INVALID_OCL, NO_QUERY
    }

    private OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl;
    private QueryStatus                                                           queryStatus        = QueryStatus.NO_QUERY;
    public static boolean                                                         isVerboseDefault   = Debug.isOn();
    protected BasicDiagnostic                                                     basicDiagnostic    = null;
    protected OCLHelper<EClassifier, ?, ?, Constraint>                            helper             = null;
    private ProblemHandler                                                        problemHandler     = null;
    protected DgEnvironmentFactory                                                environmentFactory = new DgEnvironmentFactory();
    public String                                                                 errorMessage       = "";

    // public static Set< DgOperationInstance > opsCache = null;
    // public static boolean useCachedOps = true;

    public OclEvaluator() {
        // make sure order is correct
        getEnvironmentFactory();
        getEnvironment();
        getOcl();
    }

    public void createOclInstance(DgEnvironmentFactory envFactory) {
        ocl = OCL.newInstance(envFactory);
    }

    protected static boolean notNullOrEndInQuestion(String expr) {
        return (!Utils2.isNullOrEmpty(expr) && expr.trim().lastIndexOf("?") < expr.trim().length() - 1);
    }

    public static String queryElementToStringExpression(Element query) {
        String expr = null;
        Object o = GeneratorUtils.getObjectProperty(query, DocGen3Profile.expressionChoosable, "expression",
                null);
        expr = queryObjectToStringExpression(o);
        if (notNullOrEndInQuestion(expr))
            return expr;
        if (query instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint) {
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint c = (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)query;
            ValueSpecification v = c.getSpecification();
            if (v != null) {
                expr = DocGenUtils.fixString(v);
                if (notNullOrEndInQuestion(expr))
                    return expr;
                expr = v.get_representationText();
                if (notNullOrEndInQuestion(expr))
                    return expr;
                expr = v.toString();
                if (notNullOrEndInQuestion(expr))
                    return expr;
                if (v instanceof OpaqueExpression) {
                    OpaqueExpression oe = (OpaqueExpression)v;
                    List<String> list = oe.getBody();
                    if (!Utils2.isNullOrEmpty(list)) {
                        expr = queryCollectionToStringExpression(list);
                        if (notNullOrEndInQuestion(expr))
                            return expr;
                    }
                    Expression x = oe.getExpression();
                    if (x != null) {
                        expr = x.get_representationText();
                        if (notNullOrEndInQuestion(expr))
                            return expr;
                        expr = x.toString();
                        if (notNullOrEndInQuestion(expr))
                            return expr;
                    }
                }
            }
        }
        return expr;
    }

    public static String queryCollectionToStringExpression(Collection<?> queryColl) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Object q: queryColl) {
            if (first)
                first = false;
            else
                sb.append(" and ");
            String expr = queryObjectToStringExpression(q);
            sb.append("(" + expr + ")"); // REVIEW -- Do parentheses work??!
        }
        String exprString = sb.toString();
        return exprString;
    }

    /**
     * 
     * @param query
     * @return
     */
    public static String queryObjectToStringExpression(Object query) {
        if (query == null)
            return null;
        String exprString = null;
        if (query instanceof Element) {
            Element element = (Element)query;
            exprString = queryElementToStringExpression(element);
        } else if (query instanceof String) {
            exprString = (String)query;
        } else if (query instanceof Collection) {
            Collection<?> queryColl = (Collection<?>)query;
            exprString = queryCollectionToStringExpression(queryColl);
        } else if (query != null) {
            exprString = query.toString();
        }
        return exprString;
    }

    /**
     * Evaluates the specified query given a particular context
     * 
     * @param context
     *            EObject of the context that the query should be run against
     *            (e.g., self)
     * @param query
     *            object to convert to a valid OCL string to be evaluated in the
     *            context
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
     * @param context
     *            EObject of the context that the query should be run against
     *            (e.g., self)
     * @param queryString
     *            Valid OCL string that to be evaluated in the context
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
                Object analyzer = getBasicDiagnostic().getData().get(0);
                Debug.outln("analyzer = " + analyzer);
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
        }

        if (query != null) {
            result = getOcl().evaluate(context, query);
            if (getOcl().isInvalid(result)) {
                queryStatus = QueryStatus.INVALID_OCL;
            }
        }
        return result;
    }

    /**
     * Evaluates the specified query given a particular context
     * 
     * @param context
     *            EObject of the context that the query should be run against
     *            (e.g., self)
     * @param queryString
     *            Valid OCL string that to be evaluated in the context
     * @param verbose
     *            Turns on OCL debugging if true, off if false
     * @return Object of the result whose type should be known by the caller
     * @throws ParserException
     */
    public static Object evaluateQuery(Object context, String queryString, boolean verbose)
            throws ParserException {
        OclEvaluator ev = new OclEvaluator();
        instance = ev;
        // if ( needEnvironmentSetup() ) {
        resetEnvironment(false);
        ev.setupEnvironment();
        // }
        if (queryString == null)
            return null;

        // create the ocl evaluator
        // boolean wasOn = Debug.isOn(); Debug.turnOn(); verbose = true;
        ev.setOclTracingEnabled(verbose);
        ev.queryStatus = QueryStatus.VALID_OCL;

        if (context instanceof EObject) {
            ev.getHelper().setContext(context == null ? null : ((EObject)context).eClass());
        } else if (context instanceof Collection) {
            ev.getHelper().setContext(
                    context == null ? null : OCLStandardLibraryImpl.INSTANCE.getCollection());
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

    public boolean needEnvironmentSetup() {
        if (environmentFactory == null || environmentFactory.getDgEnvironment() == null || ocl == null
                || helper == null) {
            return true;
        }
        return false;
    }

    public static List<GetCallOperation> addOperation(String[] names, EClassifier callerType,
            EClassifier returnType, EClassifier parmType, String parmName, boolean zeroArgToo,
            boolean singularNameReturnsOnlyOne, CallReturnType opType, DgEnvironmentFactory envFactory) {
        // GetCallOperation op = new GetCallOperation();
        // op.resultType = opType;
        ArrayList<GetCallOperation> ops = new ArrayList<GetCallOperation>();

        // Create the one parameter for the operation
        EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
        parm.setName(parmName);
        parm.setEType(parmType);

        GetCallOperation op = null;

        boolean someEndWithS = false;
        boolean notAllEndWithS = false;
        if (singularNameReturnsOnlyOne) {
            for (String name: names) {
                if (!Utils2.isNullOrEmpty(name)) {
                    if (name.trim().substring(name.length() - 1).toLowerCase().equals("s")) {
                        someEndWithS = true;
                    } else {
                        notAllEndWithS = true;
                    }
                }
            }
        }
        for (String name: names) {
            op = new GetCallOperation();
            op.resultType = opType;
            boolean endsWithS = false;
            boolean oneChar = false;
            if (someEndWithS && notAllEndWithS) {
                oneChar = name.trim().length() == 1;
                endsWithS = name.trim().substring(name.length() - 1).toLowerCase().equals("s");
                if (endsWithS || oneChar) {
                    op.onlyOneForAll = false;
                    op.onlyOnePer = false;
                } else {
                    op.onlyOneForAll = false;
                    op.onlyOnePer = true;
                }
            }
            // Create the one-parameter operation
            DgOperationInstance.addOperation(name, "DocGenEnvironment", envFactory, callerType, returnType,
                    op, parm);
            ops.add(op);

            if (zeroArgToo) {
                // Create the zero-parameter operation
                op = new GetCallOperation();
                op.resultType = opType;
                if (singularNameReturnsOnlyOne && someEndWithS && notAllEndWithS) {
                    if (endsWithS || oneChar) {
                        op.onlyOneForAll = false;
                        op.onlyOnePer = false;
                    } else {
                        op.onlyOneForAll = false;
                        op.onlyOnePer = true;
                    }
                }
                DgOperationInstance.addOperation(name, "DocGenEnvironment", envFactory, callerType,
                        returnType, op);
                ops.add(op);
            }
        }
        return ops;
    }

    protected static void addRegexMatchOperation(DgEnvironmentFactory envFactory) {

        // create custom operation
        DgOperationInstance doi = new DgOperationInstance();
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
                Pattern pattern = Pattern.compile((String)args[0]);
                Matcher matcher = pattern.matcher((String)source);

                return matcher.matches() ? matcher.group() : null;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addLogOperation(DgEnvironmentFactory envFactory, boolean addArg) {

        // create custom operation
        DgOperationInstance doi = new DgOperationInstance();
        doi.setName("log");
        doi.setAnnotationName("DocGenEnvironment");
        if ( addArg ) {
            EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
            parm.setName("toLog");
            doi.addParameter(parm, OCLStandardLibraryImpl.INSTANCE.getOclAny());
        }
        doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
        doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getOclAny());

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            @Override
            public Object callOperation(Object source, Object[] args) {
                if ( args != null && args.length > 0 ) {
                    if ( args.length == 1 ) {
                        Utils.log( MoreToString.Helper.toString( args[ 0 ] ) );
                    } else {
                        Utils.log( MoreToString.Helper.toString( args ) );
                    }
                } else if ( source != null ) {
                    Utils.log( source );
                }
                return source;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addGetOperation(DgEnvironmentFactory envFactory) {

        // create custom operation
        DgOperationInstance doi = new DgOperationInstance();
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
                if ( args.length < 1 ) return null;
                if ( args.length > 1 ) return null;
                String nameOrId = (String)args[0];
                
                // try id
                BaseElement e = Application.getInstance().getProject().getElementByID( nameOrId );
                if ( e != null ) return e;
                
                // try child
                if ( source != null && source instanceof Element ) {
                    e = Utils.findChildByName( (Element)source, nameOrId );
                    if ( e != null ) return e;
                }
                
                // try qualified name
                e = Utils.getElementByQualifiedName( nameOrId );
                if ( e != null ) return e;

                // try searching everything
                List< Element > results = Utils.findByName( nameOrId, true );
                if ( !Utils2.isNullOrEmpty( results ) ) {
                    return results.get( 0 );
                }
                return null;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addRunOperation(DgEnvironmentFactory envFactory, EClassifier argType) {

        // create custom operation
        DgOperationInstance doi = new DgOperationInstance();
        doi.setName("run");
        doi.setAnnotationName("DocGenEnvironment");
        EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
        parm.setName("input");
        doi.addParameter(parm, argType);
        doi.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
        doi.setReturnType(OCLStandardLibraryImpl.INSTANCE.getOclAny());

        // essentially set the actual operation as function pointer
        doi.setOperation(new CallOperation() {
            boolean runItemsInCollectionIndividually = true;
            @Override
            public Object callOperation(Object source, Object[] args) {
                
                if ( !( source instanceof Element ) ) return null;
                Element sourceElement = (Element)source;

                // If the source is a view, parse it.
                if ( sourceElement instanceof Class
                        && StereotypesHelper.hasStereotypeOrDerived( sourceElement,
                                                                     DocGen3Profile.viewStereotype ) ) {
                    DocumentValidator dv = new DocumentValidator(sourceElement);
                    DocumentGenerator dg =
                            new DocumentGenerator( sourceElement, dv, null );
                    ViewParser vp = new ViewParser( dg, true, true, dg.getDocument(), sourceElement );
                    return vp.parse();
                }
                
                // Need to parse the behavior of the Viewpoint, not the
                // Viewpoint itself.
                if ( sourceElement instanceof Class
                     && StereotypesHelper.hasStereotypeOrDerived( sourceElement,
                                                                  DocGen3Profile.viewpointStereotype ) ) {
                    sourceElement = ((Class)sourceElement).getClassifierBehavior();
                }

                Object input = args[0];

                // Allow for activity and target input to be reversed.
                // For example, run may be called as 
                //   viewpoint1.run(Sequence{element1, element2}) or as
                //   Sequence{element1, element2}.run(viewpoint1)
                if ( GeneratorUtils.findInitialNode(sourceElement) == null
                     && input instanceof Element
                     && GeneratorUtils.findInitialNode( (Element)input ) != null ) {
                    // Change to run Collection of items together as a single
                    // input since user can use "." or "->" to specify to the Ocl
                    // parser which way to handle it.
                    runItemsInCollectionIndividually = false;
                    // Call with swapped source/input, using the original source
                    // since sourceElement may have been reassigned.
                    return callOperation( input, new Object[]{ source } );
                }
                
                List<Object> inputs = new ArrayList< Object >();
                if ( runItemsInCollectionIndividually 
                     && input instanceof Collection ) {
                    inputs.addAll( (Collection< ? >)input );
                } else {
                    inputs.add( input );
                }
                DocumentValidator dv = new DocumentValidator(sourceElement);
                DocumentGenerator dg =
                        new DocumentGenerator( sourceElement, dv, null );
                dg.getContext().pushTargets( inputs );
                Object result = 
                        dg.parseActivityOrStructuredNode( sourceElement,
                                                          dg.getDocument() );
                return result;
            }
        });

        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(doi);
        envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
    }

    protected static void addEvalOperation(DgEnvironmentFactory envFactory, String opName ) {

        // create custom operation
        DgOperationInstance doi = new DgOperationInstance();
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
                String expression = (String)args[0];
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
            DgEnvironmentFactory envFactory) {
        // create custom operation
        DgOperationInstance doi = new DgOperationInstance();
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

    static EClassifier getGenericCallerType() {
        return OCLStandardLibraryImpl.INSTANCE.getOclAny();
    }

    protected static void addROperation(DgEnvironmentFactory envFactory) {
        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[] {"relationship", "relationships", "r"}, callerType, returnType, stringType,
                "relationship", true, true, CallReturnType.RELATIONSHIP, envFactory);
    }

    protected static void addMOperation(DgEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[] {"member", "members", "m"}, callerType, returnType, stringType, "member",
                true, false, CallReturnType.MEMBER, envFactory);
    }

    protected static void addTOperation(DgEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[] {"type", "types", "t"}, callerType, returnType, stringType, "type", true,
                true, CallReturnType.TYPE, envFactory);
    }

    protected static void addVOperation(DgEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getOclAny();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[] {"value", "values", "v"}, callerType, returnType, stringType, "value", true,
                true, CallReturnType.VALUE, envFactory);
    }

    protected static void addSOperation(DgEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        List<GetCallOperation> ops = addOperation(new String[] {"stereotype", "stereotypes", "s"},
                callerType, returnType, stringType, "stereotype", true, true, CallReturnType.TYPE, envFactory);
        for (GetCallOperation op: ops) {
            op.alwaysFilter = new Object[] {"Stereotype"};
        }
    }

    /**
     * Add n(), name(), and names() OCL shortcuts with and without arguments.
     * 
     * @param envFactory
     */
    protected static void addNOperation(DgEnvironmentFactory envFactory) {

        EClassifier callerType = getGenericCallerType();
        EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
        EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
        addOperation(new String[] {"name", "n"}, callerType, returnType, stringType, "name", true, true,
                CallReturnType.NAME, envFactory);
    }

    /**
     * @param exprString
     *            an OCL expression
     * @return an error message if the parse failed; otherwise return null
     */
    public String checkParsable(String exprString) {
        try {
            OCLExpression<EClassifier> query = getHelper().createQuery(exprString);
            if (query == null)
                throw new Exception();
        } catch (ParserException e) {
            return e.getLocalizedMessage();
        } catch (Throwable e) {
            return "query is null: \"" + exprString + "\"";
        }
        return null;
    }

    // static List< Package > packages = null;
    // static List< Package > getPkgs() {
    // if ( packages == null ) packages = Utils.getPackagesOfType(
    // DocGen3Profile.expressionLibrary );
    // return packages;
    // }
    static ArrayList<Element> expressions = null;

    static ArrayList<Element> getExpressions() {
        if (expressions == null) {
            expressions = new ArrayList<Element>();
            // get reference to entire model, and
            // find packages with the ExpressionLibrary stereotype
            List<Package> pkgs = Utils.getPackagesOfType(DocGen3Profile.expressionLibrary);// getPkgs();
            Stereotype exprStereotype = Utils.getStereotype(DocGen3Profile.expressionChoosable);
            for (Package pkg: pkgs) {
                List<Element> owned = Utils.collectOwnedElements(pkg, 0);
                List<Element> moreExprs = Utils.filterElementsByStereotype(owned, exprStereotype, true, true);
                expressions.addAll(moreExprs);
            }
        }
        return expressions;
    }

    /**
     * Find Expressions in ExpressionLibraries and add them as blackbox
     * shortcuts.
     * 
     * @param envFactory
     */
    protected static void addExpressionOperations(DgEnvironmentFactory envFactory) {
        ArrayList<Element> exprs = getExpressions();
        // add each of the elements with the Expression stereotype as
        // shortcut/blackbox functions
        for (Element expr: exprs) {
            String name = Utils.getName(expr);
            // function name can't have spaces and strange characters; e.g. the
            // name "four+five" would be parsed as a sum operation.
            name = name.replaceAll("[^A-Za-z0-9_]+", "");
            String exprString = queryElementToStringExpression(expr);
            // String errorMsg = checkParsable( exprString );
            String errorMsg = null;
            if (!Utils2.isNullOrEmpty(name) && errorMsg == null)
                try {
                    addExpressionOperation(name, exprString, envFactory);
                } catch (Throwable e) {
                    errorMsg = e.getLocalizedMessage();
                }
            if (errorMsg != null) {
                Debug.error(true, false, "Could not add " + name + " OCL shortcut with expression \""
                        + exprString + "\". " + errorMsg);
            }
        }
    }

    public DgEnvironmentFactory getEnvironmentFactory() {
        if (environmentFactory == null)
            environmentFactory = new DgEnvironmentFactory();
        return environmentFactory;
    }

    public DgEvaluationEnvironment getEvaluationEnvironment() {
        return getEnvironmentFactory().getDgEvaluationEnvironment();
    }

    public DgEnvironment getEnvironment() {
        return getEnvironmentFactory().getDgEnvironment();
    }

    public static void resetEnvironment(boolean resetOpsCache) {
        // DgEnvironmentFactory.reset();
        // environmentFactory = null;//new DgEnvironmentFactory();
        if (resetOpsCache) {
            // opsCache = null;
            expressions = null;
        }
        // ocl = null;
        // helper = null;
    }

    public static void resetEnvironment() {
        resetEnvironment(true);
    }

    protected static int cacheHits   = 0;
    protected static int cacheMisses = 0;

    protected DgEnvironmentFactory setupEnvironment() {
        // set up the customized environment
        // create custom environment factory
        resetEnvironment(false);

        // add custom OCL functions
        addRegexMatchOperation( getEnvironmentFactory() );
        addEvalOperation( getEnvironmentFactory(), "eval" );
        addEvalOperation( getEnvironmentFactory(), "evaluate" );
        addEvalOperation( getEnvironmentFactory(), "e" );
        addRunOperation( getEnvironmentFactory(),
                         OCLStandardLibraryImpl.INSTANCE.getOclAny() );
        addRunOperation( getEnvironmentFactory(),
                         OCLStandardLibraryImpl.INSTANCE.getSequence() );
        addGetOperation( getEnvironmentFactory() );
        addLogOperation( getEnvironmentFactory(), true );
        addLogOperation( getEnvironmentFactory(), false );
        // add one-letter custom OCL operations
        addROperation( getEnvironmentFactory() );
        addMOperation( getEnvironmentFactory() );
        addTOperation( getEnvironmentFactory() );
        addSOperation( getEnvironmentFactory() );
        addNOperation( getEnvironmentFactory() );
        addVOperation( getEnvironmentFactory() );

        addExpressionOperations( getEnvironmentFactory() );

        return getEnvironmentFactory();
    }

    public List<Choice> commandCompletionChoices(OCLHelper<EClassifier, ?, ?, Constraint> helper,
            EObject context, String oclInput) {
        getHelper().setContext(context == null ? null : context.eClass());
        List<Choice> choices = getHelper().getSyntaxHelp(ConstraintKind.INVARIANT, oclInput);
        //Debug.outln("Completion choices for OCL expression \"" + oclInput + "\" = " + choices);
        return choices;
    }

    public List<String> commandCompletionChoiceStrings(OCLHelper<EClassifier, ?, ?, Constraint> helper,
            EObject context, String oclInput, int depth) {
        Object result = null;
        try {
            result = evaluateQuery(context, oclInput, Debug.isOn());
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (result == null)
            return Collections.emptyList();
        List<Choice> choiceList = commandCompletionChoices(helper, context, oclInput);
        List<String> newChoiceStringList = new ArrayList<String>();
        boolean canExtend = depth > 0;
        for (Choice c: choiceList) {
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
            } else {
                newChoiceStringList.addAll(extensions);
            }
        }
        return newChoiceStringList;
    }

    public List<String> commandCompletionChoiceStrings(OCLHelper<EClassifier, ?, ?, Constraint> helper,
            EObject context, String oclInput) {
//        boolean wasOn = Debug.isOn();
//        Debug.turnOn();
        List<String> choiceList = new ArrayList<String>();
        List<Choice> choices = commandCompletionChoices(helper, context, oclInput);
        for (Choice next: choices) {
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
     * 
     * Note that the evaluateQuery is more generic and can handle invariants as
     * well
     * 
     * @param context
     *            EObject of the context that the constraint should be checked
     *            against
     * @param constraintString
     *            Valid OCL constraint string to be checked
     * @param verbose
     *            Turns on OCL debugging if true, off if false
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
     * @param verbose
     *            true if tracing should be enabled, false otherwise
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
                    setBasicDiagnostic((BasicDiagnostic)diag);
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
