package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.ocl.GetCallOperation.CallReturnType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * Utility class for encapsulating the OCL query and constraint evaluations.
 * Note that OCL query subsumes constraints, so evaluateQuery can always be called in place of
 * checkConstraint.
 * 
 * Here's an example of how to use OclEvaluator, setting up the environment as well
  		// create custom environment factory
  		DgEnvironmentFactory	envFactory 	= new DgEnvironmentFactory();
  		
  		// create custom operation
		DgOperationInstance		doi 		= new DgOperationInstance();
		doi.setName("regexMatch");
		doi.setAnnotationName("DocGenEnvironment");
		EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
		parm.setName("pattern");
		doi.addParameter(parm);
		
		// essentially set the actual operation as function pointer
		doi.setOperation(new CallOperation() {
			@Override
			public Object callOperation(Object source, Object[] args) {
				Pattern pattern = Pattern.compile((String) args[0]);
				Matcher matcher = pattern.matcher((String) source);
	
				return matcher.matches() ? matcher.group() : null;
			}
		});
		
		// add custom operation to environment and evaluation environment
		envFactory.getDgEnvironment().addDgOperation(doi);
		envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
				
		// create the ocl evaluator
		OclEvaluator.createOclInstance(envFactory);

		// create query and evaluate
		String oclquery = "name.regexMatch('DocGen Templating') <> null";
		Object result = OclEvaluator.evaluateQuery(rootEObject, oclquery, verbose);

 * 
 * TODO: Need to expand so it can handle multiple contexts
 * @author cinyoung
 *
 */
public class OclEvaluator {
	public enum QueryStatus {
		PARSE_EXCEPTION, VALID_OCL, INVALID_OCL, NO_QUERY
	}
	private static OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject>	ocl;
	private static QueryStatus																queryStatus = QueryStatus.NO_QUERY;
	public static boolean isVerboseDefault = Debug.isOn();
	protected static BasicDiagnostic basicDiagnostic = null;
	protected static OCLHelper<EClassifier, ?, ?, Constraint> helper = null;
	private static ProblemHandler problemHandler = null;

	protected static DgEnvironmentFactory envFactory = new DgEnvironmentFactory();  
  
	public static void createOclInstance(DgEnvironmentFactory envFactory) {
		ocl = OCL.newInstance(envFactory);
	}

	
	public static String queryElementToStringExpression( Element query ) {
        Object o = GeneratorUtils.getObjectProperty(query, DocGen3Profile.expressionChoosable,
                                                    "expression", null);
        return queryObjectToStringExpression( o );
	}
	
    public static String queryCollectionToStringExpression(Collection<?> queryColl ) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for ( Object q : queryColl ) {
            if ( first ) first = false;
            else sb.append( " and " ); 
            String expr = queryObjectToStringExpression( q );
            sb.append( "(" + expr + ")" ); // REVIEW -- Do parentheses work??!
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
	  if ( query == null ) return null;
      String exprString = null;
      if (query instanceof Element) {
          Element element = (Element)query;
          exprString = queryElementToStringExpression( element );
      } else if (query instanceof String) {
        exprString = (String) query;
      } else if (query instanceof Collection) {
        Collection<?> queryColl = (Collection<?>) query;
        if (queryColl.size() == 1) {
          exprString = queryObjectToStringExpression(queryColl.iterator().next());
        } else {
          Debug.error(false, false, "Error! Query cannot be a list of multiple things!");
        }
      } else if (query != null) {
        exprString = (String) query.toString();
      }
      return exprString;
	}
	
  /**
   * Evaluates the specified query given a particular context
   * 
   * @param context   EObject of the context that the query should be run against (e.g., self)
   * @param query object to convert to a valid OCL string to be evaluated in the context
   * @return        Object of the result whose type should be known by the caller
 * @throws ParserException 
   */
  public static Object evaluateQuery(Object context, Object query) throws ParserException {
  //  public static Object evaluateQuery(EObject context, Object query) {
    return evaluateQuery(context, queryObjectToStringExpression(query));
  }
	
  /**
   * Evaluates the specified query given a particular context
   * 
   * @param context   EObject of the context that the query should be run against (e.g., self)
   * @param queryString Valid OCL string that to be evaluated in the context
   * @return        Object of the result whose type should be known by the caller
 * @throws ParserException 
   */
  public static Object evaluateQuery(Object context, String queryString) throws ParserException {
  //public static Object evaluateQuery(EObject context, String queryString) {
    return evaluateQuery( context, queryString, isVerboseDefault );
  }
  
	/**
	 * Evaluates the specified query given a particular context
	 * 
	 * @param context		EObject of the context that the query should be run against (e.g., self)
	 * @param queryString	Valid OCL string that to be evaluated in the context
	 * @param verbose		Turns on OCL debugging if true, off if false 
	 * @return				Object of the result whose type should be known by the caller
	 * @throws ParserException 
	 */
	public static Object evaluateQuery(Object context, String queryString,
	                                   boolean verbose) throws ParserException {
    setupEnvironment();

	  // create the ocl evaluator
    OclEvaluator.createOclInstance( envFactory );    
      // boolean wasOn = Debug.isOn(); Debug.turnOn(); verbose = true;
	  setOclTracingEnabled(verbose);
		queryStatus = QueryStatus.VALID_OCL;

		if ( context instanceof EObject ) {
		    getHelper().setContext(context == null ? null : ((EObject)context).eClass());
		} else if ( context instanceof Collection ) {
	        getHelper().setContext(context == null ? null : OCLStandardLibraryImpl.INSTANCE.getCollection());
		}
		
		Object result = null;
		OCLExpression<EClassifier> query = null;

		basicDiagnostic = null;
		problemHandler = null;
		
    try {
      query = getHelper().createQuery( queryString );
    } catch ( ParserException e ) {
      queryStatus = QueryStatus.PARSE_EXCEPTION;
      if ( verbose ) {
        e.printStackTrace();
        Debug.outln( "my diag = " + getBasicDiagnostic() );
        Object analyzer = getBasicDiagnostic().getData().get( 0 );
        Debug.outln( "analyzer = " + analyzer );
        Debug.outln( "ProblemHandler = " + getProblemHandler() );
        if ( getProblemHandler() != null ) {
          int offset = getProblemHandler().getErrorReportLineOffset();
          Debug.outln( "getErrorReportLineOffset() = " + offset );
          Debug.outln( "Error messages = "
                       + Utils2.toString( ProblemHandler.ERROR_MESSAGES ) );
          AbstractParser parser = getProblemHandler().getParser();
          Debug.outln( "parser = " + parser );
          if ( parser != null ) {
            ParseTable pt = parser.getParseTable();
            Debug.outln( "ParseTable = " + pt );
            AbstractLexer lexer = parser.getLexer();
            Debug.outln( "lexer = " + lexer );
            if ( lexer != null ) {
              pt = lexer.getParseTable();
              Debug.outln( "lexer ParseTable = " + pt );
            }
          }
        }
      }
      throw new ParserException( getBasicDiagnostic() );
    }
		
		if (query != null) {
			result = getOcl().evaluate(context, query);
			if (getOcl().isInvalid(result)) {
				queryStatus = QueryStatus.INVALID_OCL;
			}
		}

		Debug.outln("evaluateQuery(context=" + DocGenUtils.fixString(context) + ", queryString=" + queryString + ", verbose=" + verbose + ") = " + DocGenUtils.fixString(result));
        // if ( !wasOn ) Debug.turnOff();
		return result;
	}

    public static List< GetCallOperation > addOperation( String[] names,
                                                         EClassifier callerType,
                                                         EClassifier returnType,
                                                         EClassifier parmType,
                                                         String parmName,
                                                         boolean zeroArgToo,
                                                         boolean singularNameReturnsOnlyOne,
                                                         CallReturnType opType,
                                                         DgEnvironmentFactory envFactory ) {
        //        GetCallOperation op = new GetCallOperation();
//        op.resultType = opType;
        ArrayList<GetCallOperation> ops = new ArrayList< GetCallOperation >();
	    
        // Create the one parameter for the operation
        EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
        parm.setName( parmName );
        parm.setEType( parmType );
    
        GetCallOperation op = null;
        
        boolean someEndWithS = false;
        boolean notAllEndWithS = false;
        if ( singularNameReturnsOnlyOne ) {
            for ( String name : names ) {
                if ( !Utils2.isNullOrEmpty( name ) ) {
                    if ( name.trim().substring( name.length() - 1 )
                             .toLowerCase().equals( "s" ) ) {
                        someEndWithS = true;
                    } else {
                        notAllEndWithS = true;
                    }
                }
            }
        }
        for ( String name : names ) {
          op = new GetCallOperation();
          op.resultType = opType;
          boolean endsWithS = false;
          boolean oneChar = false;
          if ( someEndWithS && notAllEndWithS ) {
              oneChar = name.trim().length() == 1;
              endsWithS = name.trim().substring( name.length()-1 ).toLowerCase().equals( "s" );
              if ( endsWithS || oneChar ) {
                  op.onlyOneForAll = false;
                  op.onlyOnePer = false;
              } else {
                  op.onlyOneForAll = false;
                  op.onlyOnePer = true;
              }
          }
          // Create the one-parameter operation
          DgOperationInstance.addOperation( name, "DocGenEnvironment",
                                            envFactory, callerType, returnType, op,
                                            parm );
          ops.add( op );

          if ( zeroArgToo ) {
            // Create the zero-parameter operation
            op = new GetCallOperation();
            op.resultType = opType;
            if ( singularNameReturnsOnlyOne && someEndWithS && notAllEndWithS ) {
                if ( endsWithS || oneChar ) {
                    op.onlyOneForAll = false;
                    op.onlyOnePer = false;
                } else {
                    op.onlyOneForAll = false;
                    op.onlyOnePer = true;
                }
            }
            DgOperationInstance.addOperation( name, "DocGenEnvironment", envFactory,
                                              callerType, returnType, op );
            ops.add( op );
          }
        }
        return ops;
	}
	
	private static boolean newWay = true;
	
	protected static void addRegexMatchOperation(DgEnvironmentFactory envFactory) {
	  
    EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();

    if ( newWay ) {
      addOperation( new String[] { "regexMatch", "match" }, stringType,
                    stringType, stringType, "pattern", false, false,
                    CallReturnType.SELF, envFactory );
	    return;
	  }
	}
	
	static EClassifier getGenericCallerType() { return OCLStandardLibraryImpl.INSTANCE.getOclAny(); }
	
	protected static void addROperation(DgEnvironmentFactory envFactory) {
	  
    EClassifier callerType = getGenericCallerType();
    EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
    EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
    if ( newWay ) {
      addOperation( new String[] { "relationship", "relationships", "r" },
                    callerType, returnType, stringType, "relationship", true,
                    true, CallReturnType.RELATIONSHIP, envFactory );
      return;
    }    
  }
  
  protected static void addMOperation(DgEnvironmentFactory envFactory) {
    
    EClassifier callerType = getGenericCallerType();
    EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
    EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
    if ( newWay ) {
      addOperation( new String[] { "member", "members", "m" },
                    callerType, returnType, stringType, "member", true,
                    false, CallReturnType.MEMBER, envFactory );
      return;
    }
  }

  protected static void addTOperation(DgEnvironmentFactory envFactory) {
    
    EClassifier callerType = getGenericCallerType();
    EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
    EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
    if ( newWay ) {
      addOperation( new String[] { "type", "types", "t" },
                    callerType, returnType, stringType, "type", true,
                    true, CallReturnType.TYPE, envFactory );
      return;
    }
  }

  protected static void addSOperation(DgEnvironmentFactory envFactory) {
      
      EClassifier callerType = getGenericCallerType();
      EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
      EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
      if ( newWay ) {
        List< GetCallOperation > ops =
                addOperation( new String[] { "stereotype", "stereotypes","s" },
                              callerType, returnType, stringType, "stereotype",
                              true, true, CallReturnType.TYPE, envFactory );
        for ( GetCallOperation op : ops ) {
            op.alwaysFilter = new Object[] { "Stereotype" };
        }
        return;
      }
    }

  protected static void addNOperation(DgEnvironmentFactory envFactory) {
    
    EClassifier callerType = getGenericCallerType();
    EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
    EClassifier stringType = OCLStandardLibraryImpl.INSTANCE.getString();
    if ( newWay ) {
      addOperation( new String[] { "name", "n" },
                    callerType, returnType, stringType, "name", true, true,
                    CallReturnType.NAME, envFactory );
      return;
    }
  }

  protected static DgEnvironmentFactory setupEnvironment() {
    // set up the customized environment
    // create custom environment factory
    DgEnvironmentFactory.reset();
    envFactory = new DgEnvironmentFactory();
    addRegexMatchOperation( envFactory );
    addROperation( envFactory );
    addMOperation( envFactory );
    addTOperation( envFactory );
    addSOperation( envFactory );
    addNOperation( envFactory );
    
    return envFactory;
  }

  public static List< Choice >
      commandCompletionChoices( OCLHelper< EClassifier, ?, ?, Constraint > helper,
                                EObject context, String oclInput ) {
    getHelper().setContext( context == null ? null : context.eClass() );
    List< Choice > choices =
        getHelper().getSyntaxHelp( ConstraintKind.INVARIANT, oclInput );
    Debug.outln( "Completion choices for OCL expression \"" + oclInput
                 + "\" = " + choices );
    return choices;
  }


  public static List< String >
      commandCompletionChoiceStrings( OCLHelper< EClassifier, ?, ?, Constraint > helper,
                                      EObject context, String oclInput, int depth ) {
    Object result = null;
    try {
        result = evaluateQuery( context, oclInput, Debug.isOn() );
    } catch ( ParserException e ) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    if ( result == null ) return Collections.emptyList();
    List< Choice > choiceList = commandCompletionChoices( helper, context, oclInput );
    List< String > newChoiceStringList = new ArrayList< String >();
    boolean canExtend = depth > 0;
    for ( Choice c : choiceList ) {
      String newChoiceString = oclInput;
      if ( canExtend ) {
        newChoiceString += "." + c.getName();
      }
      newChoiceStringList.add(newChoiceString);
      List< String > extensions = null;  
      if ( depth > 1 ) {
        extensions = commandCompletionChoiceStrings( helper, context, newChoiceString, depth-1 );
      }
      canExtend = !Utils2.isNullOrEmpty( extensions );
      if ( !canExtend ) {
        newChoiceStringList.add( newChoiceString );
      } else {
        newChoiceStringList.addAll( extensions );
      }
    }
    return newChoiceStringList;
  }
  
  public static List< String >
      commandCompletionChoiceStrings( OCLHelper< EClassifier, ?, ?, Constraint > helper,
                                      EObject context, String oclInput ) {
    boolean wasOn = Debug.isOn();
    Debug.turnOn();
    List< String > choiceList = new ArrayList< String >();
    List< Choice > choices = commandCompletionChoices( helper, context, oclInput );
    for ( Choice next : choices ) {
      choiceList.add( next.getName() );
      switch ( next.getKind() ) {
        case OPERATION:
        case SIGNAL:
          // the description is already complete
//          Debug.outln( next.getDescription() );
//          break;
        case PROPERTY:
        case ENUMERATION_LITERAL:
        case VARIABLE:
          Debug.outln( next.getName() + " : " + next.getDescription() );
          //choiceList.add( next.getName() );
          break;
        default:
          //choiceList.add( next.getName() );
          Debug.outln( next.getName() );
          break;
      }
    }
    if ( !wasOn ) Debug.turnOff();
    Debug.outln( "choices = " + choiceList.toString() );
    return choiceList;
  }
	
	/**
	 * Evaluates the specified invariant (constraint given a particular context)
	 * 
	 * Note that the evaluateQuery is more generic and can handle invariants as well
	 * 
	 * @param context			EObject of the context that the constraint should be checked against
	 * @param constraintString	Valid OCL constraint string to be checked
	 * @param verbose			Turns on OCL debugging if true, off if false 
	 * @return					true if constraint is satisfied, false otherwise
	 */
	public static boolean checkConstraint(EObject context, String constraintString, boolean verbose) {
		setOclTracingEnabled(verbose);

		queryStatus = QueryStatus.VALID_OCL;

		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
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
			Query<EClassifier, EClass, EObject> eval = ocl.createQuery(constraint);
			ok = eval.check(context);
		}

		return ok;
	}
	
	/**
	 * Utility method for toggling OCL tracing/debugging information
	 * @param verbose	true if tracing should be enabled, false otherwise
	 */
	private static void setOclTracingEnabled(boolean verbose) {
		getOcl().setEvaluationTracingEnabled(verbose);
		getOcl().setParseTracingEnabled(verbose);
	}
	
	/**
	 * Returns the query status.
	 * @return
	 */
	public static QueryStatus getQueryStatus() {
		return queryStatus;
	}

	/**
	 * Simple rollup function that determines whether a query was executed properly or not
	 * @return
	 */
	public static boolean isValid() {
		switch (getQueryStatus()) {
		case VALID_OCL:
			return true;
		default:
			return false;
		}
	}

  public static
      OCL< ?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject >
      getOcl() {
    if ( ocl == null ) {
      setOcl( OCL.newInstance( envFactory ) );
    }
    return ocl;
  }

  public static void
      setOcl( OCL< ?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject > ocl ) {
    OclEvaluator.ocl = ocl;
  }

  public static boolean isVerboseDefault() {
    return isVerboseDefault;
  }

  public static void setVerboseDefault( boolean isVerboseDefault ) {
    OclEvaluator.isVerboseDefault = isVerboseDefault;
  }

  public static BasicDiagnostic getBasicDiagnostic() {
    if ( basicDiagnostic == null ) {
      if ( getHelper() != null ) {
        Diagnostic diag = getHelper().getProblems();
        if ( diag instanceof BasicDiagnostic ) {
          setBasicDiagnostic( (BasicDiagnostic)diag );
        }
      }
    }
    return basicDiagnostic;
  }

  public static void setBasicDiagnostic( BasicDiagnostic basicDiagnostic ) {
    OclEvaluator.basicDiagnostic = basicDiagnostic;
  }

  /**
   * @return
   */
  public static ProblemHandler getProblemHandler() {
    if ( problemHandler == null ) {
      if ( getOcl() != null ) {
        setProblemHandler( OCLUtil.getAdapter( getOcl().getEnvironment(),
                                               ProblemHandler.class ) );
      }
    }
    return problemHandler;
  }

  /**
   * @param problemHandler
   */
  public static void setProblemHandler( ProblemHandler problemHandler ) {
    OclEvaluator.problemHandler = problemHandler;
  }

  public static void setQueryStatus( QueryStatus queryStatus ) {
    OclEvaluator.queryStatus = queryStatus;
  }

  public static OCLHelper< EClassifier, ?, ?, Constraint > getHelper() {
    if ( helper == null ) {
      if ( getOcl() != null ) {
        setHelper( getOcl().createOCLHelper() );
      }
    }
    return helper;
  }
  
  public static void
      setHelper( OCLHelper< EClassifier, ?, ?, Constraint > helper ) {
    OclEvaluator.helper = helper;
  }
}
