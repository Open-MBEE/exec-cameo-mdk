/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.ocl.CallOperation;
import gov.nasa.jpl.ocl.DgEnvironmentFactory;
import gov.nasa.jpl.ocl.DgOperationInstance;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EcoreFactory;
import org.jdesktop.swingx.action.BoundAction;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.BrowserTabTree;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class OclQueryAction extends MDAction {

  private static final long serialVersionUID = -4695340422718243709L;

  private List< Element > context = new ArrayList< Element >();

  public static final String actionid = "OclQuery";
  
  public static String actionText = "Run OCL Query"; 

  public OclQueryAction( Element context ) {
    super(actionid, actionText, null, null);
    getContext().add( context ); 
  }
  public OclQueryAction() {
    this(null);
  }
  
  public void actionPerformed(ActionEvent e) {
    GUILog gl = Application.getInstance().getGUILog();
    
    Collection< Element > selectedElements = MDUtils.getSelection( e );
    setContext( selectedElements );
    
//    if ( getContext().isEmpty() ) {
//      List< PresentationElement > selection = getActiveDiagramSelection();
//      if ( selection != null ) {
//        for ( PresentationElement pel : selection ) {
//          context.add( pel.getActualElement() );
//        }
//      }
//      if ( getContext().isEmpty() ) {
//        context.addAll( getElements( e.getSource() ) );
//      }
//    }
    
    String oclString = JOptionPane.showInputDialog( "enter an OCL expression" );
    boolean wasOn = Debug.isOn();
    Debug.turnOn();
    for ( Element elem : getContext() ) {
      try {
        Object result = OclEvaluator.evaluateQuery( elem, oclString, true );
        Debug.outln( "evaluated \"" + oclString + "\" for element " + elem.get_representationText() );
        Debug.outln( "    result = " + result + "\n" );
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      try {
        Object result = eval2( oclString, elem );
        Debug.outln( "eval2 \"" + oclString + "\" for element !! " + elem.get_representationText() );
        Debug.outln( "    result = " + result + "\n" );
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    if ( !wasOn ) Debug.turnOff();

//    JFrame window = new JFrame( actionText );
//    
//    LayoutManager layout = null;
//    JPanel pane = new JPanel( layout, true );
//    JTextField c = new JTextField();
//    pane.add(c);
//    window.add( pane );
    
  }
  
  public static Object eval2( String oclQuery, EObject contextEObject ) {
    // lets play with OCL
    // TODO: need to move this out into its own test class
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
    doi.addParameter( parm );

    // essentially set the actual operation as function pointer
    doi.setOperation( new CallOperation() {
      @Override
      public Object callOperation( Object source, Object[] args ) {
        Pattern pattern = Pattern.compile( (String)args[ 0 ] );
        Matcher matcher = pattern.matcher( (String)source );

        return matcher.matches() ? matcher.group() : null;
      }
    } );

    // add custom operation to environment and evaluation environment
    envFactory.getDgEnvironment().addDgOperation( doi );
    envFactory.getDgEvaluationEnvironment().addDgOperation( doi );

    // create the ocl evaluator
    OclEvaluator.createOclInstance( envFactory );

    // create query and evaluate
    if ( Utils2.isNullOrEmpty( oclQuery ) ) {
      oclQuery = "name.regexMatch('DocGen Templating') <> null";
    }
    // oclquery = "name <> 'DocGen Templating'"; //"ownedType->asSet()";
    // oclquery = "self.appliedStereotypeInstance.slot";
    // oclquery = "self.appliedStereotypeInstance.classifier.attribute";
    // oclquery = "name.regexMatch('DocGen Templating') <> null";

    Object result =
        OclEvaluator.evaluateQuery( contextEObject, oclQuery, verbose );
    if ( result != null ) {
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

    return result;

  }
  
  /**
   * @return the context
   */
  public List< Element > getContext() {
    if ( context == null ) context = new ArrayList< Element >();
    return context;
  }
  /**
   * @param context the context to set
   */
  public void setContext( List< Element > context ) {
    this.context = context;
  }
  public void setContext( Collection< Element > context ) {
    getContext().clear();
    getContext().addAll( context );
  }

}
