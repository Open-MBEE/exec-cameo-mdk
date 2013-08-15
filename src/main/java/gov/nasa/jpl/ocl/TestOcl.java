/**
 * 
 */
package gov.nasa.jpl.ocl;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class TestOcl {

  protected Project project = null;
  protected Model rootEObject = null;

  public static void testOcl( Project project, EObject rootEObject ) {
    boolean verbose = true;

    // set up the customized environment
    // create custom environment factory
    DgEnvironmentFactory envFactory = new DgEnvironmentFactory();

    // create custom operation
    DgOperationInstance doi = new DgOperationInstance();
    doi.setName( "regexMatch" );
    doi.setAnnotationName( "DocGenEnvironment" );
    EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
    parm.setName( "pattern" );
    doi.addStringParameter( parm );

    // essentially set the actual operation as function pointer
    doi.setOperation( new CallOperation() {
      @Override
      public Object callOperation( Object source, Object[] args ) {
        Pattern pattern = Pattern.compile( (String)args[ 0 ] );
        Matcher matcher = pattern.matcher( (String)source );

        return matcher.matches() ? matcher.group() : null;
      }
    } );

    Assert.assertNotNull( project );
    Assert.assertNotNull( rootEObject );

    if ( project != null && rootEObject != null ) {

      // add custom operation to environment and evaluation environment
      envFactory.getDgEnvironment().addDgOperation( doi );
      envFactory.getDgEvaluationEnvironment().addDgOperation( doi );

      // create the ocl evaluator
      OclEvaluator.createOclInstance( envFactory );

      // create query and evaluate
      String oclquery = "name.regexMatch('DocGen Templating') <> null";
      // oclquery = "name <> 'DocGen Templating'"; //"ownedType->asSet()";
      // oclquery = "self.appliedStereotypeInstance.slot";
      // oclquery = "self.appliedStereotypeInstance.classifier.attribute";
      // oclquery = "name.regexMatch('DocGen Templating') <> null";

      Object result =
          OclEvaluator.evaluateQuery( rootEObject, oclquery, verbose );
      Assert.assertNotNull( result );

      if ( result != null ) {
        if ( oclquery.equals("name.regexMatch('DocGen Templating') <> null") ) {
          Assert.assertTrue( (Boolean)result );
        }
        System.out.println( result.getClass() + ": " + result.toString() );
        if ( result instanceof Set ) {
          for ( Stereotype key : (Set< Stereotype >)result ) {
            System.out.println( "\t" + key.getHumanName() );
          }
        } else if ( result instanceof List ) {
          for ( Property prop : (List< Property >)result ) {
            System.out.println( "\t" + prop.getHumanName() );
          }
        }
      }
    }
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    project = Application.getInstance().getProject();
    rootEObject  = project.getModel();
  }

//  /**
//   * Test method for {@link gov.nasa.jpl.ocl.OclEvaluator#createOclInstance(gov.nasa.jpl.ocl.DgEnvironmentFactory)}.
//   */
//  @Test
//  public void testCreateOclInstance() {
//    fail( "Not yet implemented" );
//  }

  /**
   * Test method for {@link gov.nasa.jpl.ocl.OclEvaluator#evaluateQuery(org.eclipse.emf.ecore.EObject, java.lang.String, boolean)}.
   */
  @Test
  public void testEvaluateQuery() {
    testOcl(project, rootEObject);
  }

//  /**
//   * Test method for {@link gov.nasa.jpl.ocl.OclEvaluator#checkConstraint(org.eclipse.emf.ecore.EObject, java.lang.String, boolean)}.
//   */
//  @Test
//  public void testCheckConstraint() {
//    fail( "Not yet implemented" );
//  }
//
//  /**
//   * Test method for {@link gov.nasa.jpl.ocl.OclEvaluator#getQueryStatus()}.
//   */
//  @Test
//  public void testGetQueryStatus() {
//    fail( "Not yet implemented" );
//  }
//
//  /**
//   * Test method for {@link gov.nasa.jpl.ocl.OclEvaluator#isValid()}.
//   */
//  @Test
//  public void testIsValid() {
//    fail( "Not yet implemented" );
//  }

}
