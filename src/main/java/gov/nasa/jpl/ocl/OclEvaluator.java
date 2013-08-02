package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.Query;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.helper.Choice;
import org.eclipse.ocl.helper.ConstraintKind;
import org.eclipse.ocl.helper.OCLHelper;

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
	
	
	public static void createOclInstance(DgEnvironmentFactory envFactory) {
		ocl = OCL.newInstance(envFactory);
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
	  if ( ocl == null ) ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
		setOclTracingEnabled(verbose);
		queryStatus = QueryStatus.VALID_OCL;

		OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
		helper.setContext(context.eClass());
		
		Object result = null;
		OCLExpression<EClassifier> query = null;

		try {
			query = helper.createQuery(queryString);
			//query.
		} catch (ParserException e) {
			queryStatus = QueryStatus.PARSE_EXCEPTION;
			e.printStackTrace();
			helper.getProblems();
		}
		
		if (query != null) {
			result = ocl.evaluate(context, query);
			if (ocl.isInvalid(result)) {
				queryStatus = QueryStatus.INVALID_OCL;
			}
		}

		return result;
	}

  public static List< Choice >
      commandCompletionChoices( OCLHelper< EClassifier, ?, ?, Constraint > helper,
                                EObject context, String oclInput ) {
    if ( helper == null ) {
      helper = ocl.createOCLHelper();
      helper.setContext( context.eClass() );
    }
    List< Choice > choices =
        helper.getSyntaxHelp( ConstraintKind.INVARIANT, oclInput );
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
          Debug.outln( next.getDescription() );
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
		ocl.setEvaluationTracingEnabled(verbose);
		ocl.setParseTracingEnabled(verbose);
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
}
