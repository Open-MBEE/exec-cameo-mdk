/**
 * 
 */
package gov.nasa.jpl.mbee.lib;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.helper.OCLHelper;

/**
 * Tools for leveraging the eclipse OCL library.  
 * 
 * Following example from http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.ocl.doc%2Fhelp%2FCustomizingtheEnvironment.html
 * 
 * This allows customization of the EcoreEnvironment, need to be able to register operations with this
 *
 */
public class OclUtils {
  private static boolean verbose = true;
  protected static OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl;
  protected static OCLHelper<EClassifier, ?, ?, Constraint> helper;
  //private static Environment< ?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject > env = null;

  static {
    ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
    ocl.setEvaluationTracingEnabled(verbose);
    ocl.setParseTracingEnabled(verbose);
    helper = ocl.createOCLHelper();
  }
  
  public static synchronized void setContext( EObject context ) {
    helper.setContext( context.eClass() );
  }

  //  public static OCL< ?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject > createOclInstance() {
//    EnvironmentFactory< ?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject > envF = null;
//    //Environment< ?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject > env = null;
////    env = EcoreEnvironmentFactory.INSTANCE.createEnvironment();
////    ocl = OCL.newInstance( env );
//    ocl = 
////    ocl = OCL.newInstance( envF );
//    return ocl;
//  }

  

}
