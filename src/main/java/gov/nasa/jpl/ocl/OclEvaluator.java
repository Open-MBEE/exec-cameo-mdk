package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.EmfUtils;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import org.eclipse.ocl.util.CollectionUtil;
import org.eclipse.ocl.util.OCLUtil;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

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
 * TODO: Need to expand so it can handle multiple contexts and add blackbox operations to the
 *       context.
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

  /**
   * Evaluates the specified query given a particular context
   * 
   * @param context   EObject of the context that the query should be run against (e.g., self)
   * @param queryString Valid OCL string that to be evaluated in the context
   * @return        Object of the result whose type should be known by the caller
   */
  public static Object evaluateQuery(EObject context, String queryString) {
    return evaluateQuery( context, queryString, isVerboseDefault );
  }
  
	/**
	 * Evaluates the specified query given a particular context
	 * 
	 * @param context		EObject of the context that the query should be run against (e.g., self)
	 * @param queryString	Valid OCL string that to be evaluated in the context
	 * @param verbose		Turns on OCL debugging if true, off if false 
	 * @return				Object of the result whose type should be known by the caller
	 */
	public static Object evaluateQuery(EObject context, String queryString,
	                                   boolean verbose) {
    setupEnvironment();

	  // create the ocl evaluator
    OclEvaluator.createOclInstance( envFactory );    

	  setOclTracingEnabled(verbose);
		queryStatus = QueryStatus.VALID_OCL;

		getHelper().setContext(context == null ? null : context.eClass());
		
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
    }
		
		if (query != null) {
			result = getOcl().evaluate(context, query);
			if (getOcl().isInvalid(result)) {
				queryStatus = QueryStatus.INVALID_OCL;
			}
		}

		return result;
	}

	protected static void addRegexMatchOperation(DgEnvironmentFactory envFactory) {
	  
    // create custom operation
    // essentially set the actual operation as function pointer
    CallOperation callOp =
        new CallOperation() {
          @Override
          public Object callOperation( Object source, Object[] args ) {
            Pattern pattern = Pattern.compile( (String)args[ 0 ] );
            Matcher matcher = pattern.matcher( (String)source );
    
            return matcher.matches() ? matcher.group() : null;
          }
        };

    EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
    parm.setName( "pattern" );
    parm.setEType(OCLStandardLibraryImpl.INSTANCE.getString());

    EClassifier callerAndReturnType = OCLStandardLibraryImpl.INSTANCE.getString();
    DgOperationInstance.addOperation( "regexMatch", "DocGenEnvironment",
                                      envFactory, callerAndReturnType,
                                      callerAndReturnType, callOp, parm );
	}
	
  protected static void addROperation(DgEnvironmentFactory envFactory) {
    
    // create custom operation
    // essentially set the actual operation as function pointer
    CallOperation callOp1 =
       new CallOperation() {
            @Override
            public Object callOperation( Object source, Object[] args ) {
              List< Object > resultList = new ArrayList< Object >();
              if ( source instanceof Element ) {
                Element elem = (Element)source;
                resultList.addAll(getRelationships( elem ));
                if ( args != null ) {
                  for ( Object filter : args ) {
                    resultList = collectOrFilter( resultList, false, filter );
                  }
                }
              } else if ( source instanceof Collection ) {
                for ( Object child : (Collection< ? >)source ) {
                  Object childRes = callOperation( child, args );
                  if ( childRes != null ) {
                    resultList.add( childRes );
                  }
                }
              }
              return CollectionUtil.asSequence( resultList );
            }
          };

    // Create the one parameter for the operation
    EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
    parm.setName( "relationship" );
    parm.setEType(OCLStandardLibraryImpl.INSTANCE.getString());
    
    // Create the one-parameter operation
    EClassifier callerType = OCLStandardLibraryImpl.INSTANCE.getOclAny();
    EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
    DgOperationInstance.addOperation( "r", "DocGenEnvironment",
                                      envFactory, callerType, returnType,
                                      callOp1, parm );
    
    // Create the zero-parameter operation
    DgOperationInstance.addOperation( "r", "DocGenEnvironment", envFactory,
                                      callerType, returnType, callOp1 );
  }
  
  protected static void addMOperation(DgEnvironmentFactory envFactory) {
    
    // create custom operation
    // essentially set the actual operation as function pointer
    CallOperation callOp1 =
       new CallOperation() {
            @Override
            public Object callOperation( Object source, Object[] args ) {
              List< Object > resultList = new ArrayList< Object >();
              if ( source == null ) return resultList;
              if ( source instanceof Element ) {
                Element elem = (Element)source;
                resultList.addAll(elem.getOwnedElement());
                if ( args != null ) {
                  resultList = collectOrFilter( resultList, false, args );
                }
              } else if ( source instanceof Collection ) {
                for ( Object child : (Collection< ? >)source ) {
                  Object childRes = callOperation( child, args );
                  if ( childRes != null ) {
                    resultList.add( childRes );
                  }
                }
              }
              return CollectionUtil.asSequence( resultList );
            }
          };

    // Create the one parameter for the operation
    EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
    parm.setName( "member" );
    parm.setEType(OCLStandardLibraryImpl.INSTANCE.getOclAny());

    // Create the one-parameter operation
    EClassifier callerType = OCLStandardLibraryImpl.INSTANCE.getOclAny();
    EClassifier returnType = OCLStandardLibraryImpl.INSTANCE.getSequence();
    DgOperationInstance.addOperation( "m", "DocGenEnvironment",
                                      envFactory, callerType, returnType,
                                      callOp1, parm );
    
    // Create the zero-parameter operation
    DgOperationInstance.addOperation( "m", "DocGenEnvironment", envFactory,
                                      callerType, returnType, callOp1 );

  }

  protected static List<Element> getRelationships( Element elem ) {
    HashSet< Element > elements = new HashSet< Element >();
    elements.addAll( elem.get_relationshipOfRelatedElement() );
    elements.addAll( elem.get_directedRelationshipOfSource() );
    elements.addAll( elem.get_directedRelationshipOfTarget() );
    return Utils2.toList( elements );
  }
  
  public static boolean matches( String s, String pattern ) {
    if ( s == pattern ) return true;
    if ( pattern == null ) return false;
    if ( s == null ) return false;
    if ( s.equalsIgnoreCase( pattern ) ) return true;
    if ( s.matches( pattern ) ) return true;
    List< String > list = EmfUtils.getPossibleFieldNames( s );
    list.remove(0);
    for ( String os : list ) {
      if ( os.equalsIgnoreCase( pattern ) ) return true;
      if ( os.matches( pattern ) ) return true;
    }
    return false;
  }
  
  /**
   * Determine 
   * @param obj
   * @param pattern
   * @return
   */
  public static boolean matches( Object obj, Object pattern ) {
    if ( obj == pattern ) return true;
    if ( pattern == null ) return false;
    if ( obj == null ) return false;

    String oName = EmfUtils.getName( obj );
    String pStr = pattern.toString();
    if ( Utils2.isNullOrEmpty( oName ) ) {
      if ( oName != null && oName.equals( pStr ) ) return true;
    } else {
      if ( matches( oName, pStr ) ) return true;
    }
    
    String pName = EmfUtils.getName( pattern );
    if ( Utils2.isNullOrEmpty( oName ) ) {
      if ( oName != null && oName.equals( pName ) ) return true;
    } else {
      if ( matches( oName, pName ) ) return true;
    }
    
    String oStr = obj.toString();
    if ( Utils2.isNullOrEmpty( oStr ) ) {
      if ( oStr != null && oStr.equals( pStr ) ) return true;
    } else {
      if ( matches( oStr, pStr ) ) return true;
      if ( matches( oStr, pName ) ) return true;
    }

    String oType = EmfUtils.getTypeName( obj );
    if ( Utils2.isNullOrEmpty( oType ) ) {
      if ( oType != null && oType.equals( pStr ) ) return true;
    } else {
      if ( matches( oType, pStr ) ) return true;
      if ( matches( oType, pName ) ) return true;
    }

    if ( obj instanceof Element ) {
      Set< Stereotype > set =
          StereotypesHelper.getAllAssignedStereotypes( Utils2.newList( (Element)obj ) );
      for ( Stereotype sType : set ) {
        String sName = sType.getName();
        if ( matches( sName, pStr ) ) return true;
        if ( matches( sName, pName ) ) return true;
      }
    }
    return false;
  }
  
  protected static List< Object > collectOrFilter( Collection< Object > elements,
                                                   boolean collect, Object... filters ) {
    ArrayList< Object > resultList = new ArrayList< Object >();
    if ( filters == null || filters.length == 0 || ( filters.length == 1 && filters[0] == null ) ) {
      return Utils2.newList( elements.toArray() );
    }
    for ( Object elem : elements ) {
      for ( Object filter : filters ) {
        if ( matches( elem, filter ) ) {
          resultList.add( elem ); // REVIEW -- weird case?!
          break;
        }
      }
      if ( collect && elem instanceof Collection ) {
        List< Object > childRes = collectOrFilter( (Collection< Object >)elem, collect, filters );
        if ( childRes != null ) {
          resultList.add( childRes );
        }
      }
    }
    return resultList;
  }
  
  protected static DgEnvironmentFactory setupEnvironment() {
    // set up the customized environment
    // create custom environment factory
    DgEnvironmentFactory.reset();
	  envFactory = new DgEnvironmentFactory();
	  addRegexMatchOperation( envFactory );
    addROperation( envFactory );
    addMOperation( envFactory );
    
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
    Object result = evaluateQuery( context, oclInput, Debug.isOn() );
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
