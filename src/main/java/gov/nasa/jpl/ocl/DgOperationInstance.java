package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.lib.CompareUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.ocl.ecore.internal.OCLStandardLibraryImpl;

import sferyx.administration.editors.HTMLEditor.newParagraphAction;

public class DgOperationInstance implements DgOperation {
	private String name;
	private String annotationName;
	private CallOperation operation;
	private List<EParameter> parameters = new ArrayList<EParameter>();
	private EClassifier callerType, returnType;

  public DgOperationInstance() {}

  public DgOperationInstance( String name, String annotationName,
                              DgEnvironmentFactory envFactory,
                              CallOperation operation,
                              EParameter... parameters ) {
    this.name = name;
    this.annotationName = annotationName;
    this.operation = operation;
    for ( EParameter ep : parameters ) {
      addParameter( ep );
    }
    //this.parameters.addAll(Arrays.asList( parameters ) );
    addToEnvironment(envFactory);
  }

  public DgOperationInstance( String name, String annotationName,
                              DgEnvironmentFactory envFactory,
                              EClassifier callerType, EClassifier returnType,
                              CallOperation operation, EParameter[] parameters ) {
    this.name = name;
    this.annotationName = annotationName;
    this.operation = operation;
    this.callerType = callerType;
    this.returnType = returnType;
    for ( EParameter ep : parameters ) {
      addParameter( ep );
    }
    addToEnvironment(envFactory);
  }

  public static DgOperationInstance
      addOperation( String name, String annotationName,
                    DgEnvironmentFactory envFactory, CallOperation operation,
                    EParameter... parameters ) {
    return new DgOperationInstance( name, annotationName,
                                    envFactory, operation, parameters );

  }

  public static DgOperationInstance
      addOperation( String name, String annotationName,
                    DgEnvironmentFactory envFactory, EClassifier callerType,
                    EClassifier returnType, CallOperation operation,
                    EParameter... parameters ) {
    return new DgOperationInstance( name, annotationName, envFactory,
                                    callerType, returnType, operation,
                                    parameters );
  }

  /**
   * Add this operation to the environment through the EnvironemntFactory
   * 
   * @param envFactory
   * @param callOp
   */
  public void addToEnvironment( DgEnvironmentFactory envFactory ) {
    // add custom operation to environment and evaluation environment
    envFactory.getDgEnvironment().addDgOperation( this );
    envFactory.getDgEvaluationEnvironment().addDgOperation( this );
  }
  
	public void addStringParameter(EParameter parameter) {
		addParameter(parameter, OCLStandardLibraryImpl.INSTANCE.getString());
		this.parameters.add(parameter);
	}

  @Override
  public void addParameter(EParameter parameter, EClassifier type) {
    parameter.setEType(type);
    this.parameters.add(parameter);
  }

  @Override
  public void addParameter(EParameter parameter) {
    this.parameters.add(parameter);
  }

	@Override
	public Object callOperation(Object source, Object[] args) {
		return operation.callOperation(source, args);
	}

	@Override
	public boolean checkOperationName(String operationName) {
		if (name.equals(operationName)) {
			return true;
		}
		return false;
	}

	@Override
	public String getAnnotationName() {
		return annotationName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<EParameter> getParameters() {
		return parameters;
	}

	@Override
	public void setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setOperation(CallOperation operation) {
		this.operation = operation;
	}

  @Override
  public EClassifier getReturnType() {
    return returnType;
  }

  @Override
  public EClassifier getCallerType() {
    return callerType;
  }

  public void setCallerType( EClassifier callerType ) {
    this.callerType = callerType;
  }

  public void setReturnType( EClassifier returnType ) {
    this.returnType = returnType;
  }

  @Override
  public int compareTo( DgOperation o ) {
    int compare = CompareUtils.compare( this, o );
    return compare;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append( getName() + "(" );
    boolean first = true;
    for ( EParameter p : parameters ) {
      if ( first ) first = false;
      else sb.append( ", " );
      sb.append( p.getName() + " : " + p.getEType() );
    }
    sb.append( ") : " + this.returnType );
    sb.append( " (" + this.callerType + ")" );
    //sb.append( this.annotationName );
    return sb.toString();
  }


}
