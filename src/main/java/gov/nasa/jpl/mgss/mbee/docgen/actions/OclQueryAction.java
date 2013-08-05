/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.EmfUtils;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.RepeatInputComboBoxDialog;
import gov.nasa.jpl.ocl.CallOperation;
import gov.nasa.jpl.ocl.DgEnvironmentFactory;
import gov.nasa.jpl.ocl.DgOperationInstance;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.Assert;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.base.ModelObject;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class OclQueryAction extends MDAction {

  private static final long serialVersionUID = -4695340422718243709L;

  private List< Element > context = new ArrayList< Element >();

  public static final String actionid = "OclQuery";
  
  public static String actionText = "Run OCL Query";
  
  protected String lastInput = "";
  protected LinkedList< String > inputHistory = new LinkedList< String >();
  protected TreeSet< String > pastInputs = new TreeSet< String >();
  protected LinkedList< String > choices = new LinkedList< String >();
  protected int maxChoices = 10;
  
  public OclQueryAction( Element context ) {
    super(actionid, actionText, null, null);
    getContext().add( context ); 
  }
  public OclQueryAction() {
    this(null);
  }
  
  public static String getStackTrace( Throwable t ) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    t.printStackTrace( pw );
    sw.flush();
    return sw.toString();
  }
  
  private class ProcessOclQuery implements RepeatInputComboBoxDialog.Processor {

    /**
     * Parse and evaluate OCL and additional helper syntax:
     * <ol>
     * <li>Parse as OCL.
     * <li>If the parse succeeds, return the result.
     * <li>Get the evaluation result up to the point where parse failed.
     * <li>Try to parse using additional syntax where the parse failed in the
     * context of the result from the previous step.
     * <li>If the parse fails, return with failure.
     * <li>If there is more to parse, continue at step 1 in the context of the
     * new result with the remaining unparsed input string.
     * <li>TODO -- How does this work when expressions are nested and not just
     * chained?
     * </ol>
     * 
     * @param input
     * @return the result of the evaluation of the input expression
     */
    public Object parseAndProcess( Object input ) {
      String oclString = input == null ? null : input.toString();
      ArrayList<Object> outputList = new ArrayList< Object >(); 
      ArrayList<Object> localContext =  new ArrayList< Object >();
      localContext.addAll( getContext() );
      
      // TODO -- How does this work when expressions are nested and not just chained?
      // Parse as OCL.
      // TODO
      
      // If the parse succeeds, return the result.
      // TODO
      
      // Get the evaluation result up to the point where parse failed.
      // TODO
      
      // Try to parse using additional syntax where the parse failed in the
      // context of the result from the previous step.
      // TODO
      
      // If the parse fails, return with failure.
      // TODO
      
      // If there is more to parse, continue at step 1 in the context of the new
      // result with the remaining unparsed input string.
      // TODO

      // try to parse as OCL
      // TODO
      // try to parse 
      
      
      // ALTERNATIVE APPROACH
      // Try to parse in pieces using "." and "->" as delimiters. 
      int pos = 0;
      boolean found = true;
      while (found) {
        int nextPos1 = oclString.indexOf( '.', pos );
        int nextPos2 = oclString.indexOf( "->", pos );
        // TODO -- HERE!!
      }
      
      
//      String[] split = oclString.split( "[.]|([-][>])" );
//      ArrayList<String> tokens = new ArrayList< String >();
//      int insideExpression = 0; 
//      for ( String s : split ) {
//        if (Utils2.count("\"", s) % 2 != insideExpression ) {
//          // expression is still open
//        }
//      }

//      while (true) {
//        for ( Object o : localContext ) {
//          
//        }
//        if ( true && false ) break;
//      }
      return outputList;
    }    
    
    @Override
    public Object process( Object input ) {
      String oclString = input == null ? null : input.toString();
      ArrayList<String> outputList = new ArrayList< String >(); 
      for ( Element elem : getContext() ) {
        Object result = null;
        String output = null;
        try {
          result = OclEvaluator.evaluateQuery( elem, oclString, true );
          output = toString(result);
          output =
              "evaluated \"" + oclString + "\" for element "
                  + toString(elem) + "\n" + "    result = "
                  + output + "\n";

          outputList.add( output );
        } catch ( Exception ex ) {
          String errorStr = getStackTrace( ex );
          Debug.outln( errorStr );
          outputList.add( errorStr );
        }
//        try {
//          result = eval2( oclString, elem );
//          output = "eval2 \"" + oclString + "\" for element "
//                       + toString(elem) 
//                       + "    result = " + toString(result) + "\n";
//          Debug.outln( output );
//          outputList.add( output );
//        } catch ( Exception ex ) {
//          String errorStr = getStackTrace( ex );
//          Debug.outln( errorStr );
//          outputList.add( errorStr );
//        }
        Debug.outln( spew(result) );
//        Debug.outln( OclEvaluator.commandCompletionChoiceStrings( null, elem,
//                                                                  oclString )//, 3 )
//                                 .toString() );
        //outputList.add( "\nSPEW\n" + spew( result ) );
      }
      //Debug.outln( outputList.toString() );
      return outputList;
    }

    private String toString( Object result ) {
      String s = null;
      if ( result instanceof Collection ) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        boolean first = true;
        for ( Object r : (Collection<?>)result ) {
          if ( first ) first = false;
          else sb.append(",");
          sb.append(toString(r));
        }
        sb.append(")");
        s = sb.toString();
      }
      if ( Utils2.isNullOrEmpty( s ) && result instanceof BaseElement ) {
        BaseElement be = ( (BaseElement)result );
        s = be.getHumanName();
      }
      if ( Utils2.isNullOrEmpty( s ) && result instanceof ModelObject ) {
        s = ( (ModelObject)result ).get_representationText();
      }
      if ( Utils2.isNullOrEmpty( s ) && result != null ) {
        s = result.toString();
      }

      if ( s == null ) s = "null";
      return s;
    }
    
    private String propertiesToString( EObject eObject ) {
      StringBuffer sb = new StringBuffer();
      // TODO
      Assert.assertFalse( true );
      return sb.toString();
    }
    
    private String spew( Object result ) {
      String s = null;
      if ( result instanceof Collection ) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        boolean first = true;
        for ( Object r : (Collection<?>)result ) {
          if ( first ) first = false;
          else sb.append(",");
          sb.append(spew(r));
        }
        sb.append(")");
        s = sb.toString();
      }
      if ( Utils2.isNullOrEmpty( s ) && result instanceof BaseElement ) {
        BaseElement be = ( (BaseElement)result );
        s = be.getHumanName();
      }
      if ( Utils2.isNullOrEmpty( s ) && result instanceof ModelObject ) {
        s = ( (ModelObject)result ).get_representationText();
      }
      if ( result instanceof EObject ) { 
        if ( Utils2.isNullOrEmpty( s ) ) {
          s = "";
        } else {
          s = s + ": ";
        }
        s = s + EmfUtils.spew( result );
      }
      if ( Utils2.isNullOrEmpty( s ) && result != null ) {
        s = result.toString();
      }
      if ( s == null ) s = "null";
      return s;
    }

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

    boolean wasOn = Debug.isOn();
    Debug.turnOn();
    RepeatInputComboBoxDialog.showRepeatInputComboBoxDialog( "Enter an OCL expression:",
                                                             "OCL Evaluation",
                                                             new ProcessOclQuery());
    
//    String oclString = "";
//    while (oclString != null) {
//      JComboBox jcb = new JComboBox( choices.toArray() );
//      jcb.setEditable(true);
//      JOptionPane.showMessageDialog(null, jcb);
//      oclString =
//          (String)JOptionPane.showInputDialog( (Component)null,
//                                               "Enter an OCL expression:",
//                                               "OCL Evaluation",
//                                               JOptionPane.PLAIN_MESSAGE,
//                                               (Icon)null,
//                                               inputHistory.toArray(),
//                                               lastInput );
//      if ( !Utils2.isNullOrEmpty( oclString ) ) {
//        lastInput = oclString;
//        inputHistory.push( oclString );
//        if ( pastInputs.contains( oclString ) ) {
//          choices.remove( oclString );
//        } else {
//          pastInputs.add( oclString );
//        }
//        choices.push( oclString );
//        while ( choices.size() > maxChoices ) {
//          choices.pollLast();
//        }
//      }
//      for ( Element elem : getContext() ) {
//        try {
//          Object result = OclEvaluator.evaluateQuery( elem, oclString, true );
//          Debug.outln( "evaluated \"" + oclString + "\" for element "
//                       + elem.get_representationText() );
//          Debug.outln( "    result = " + result + "\n" );
//        } catch ( Exception ex ) {
//          ex.printStackTrace();
//        }
//        try {
//          Object result = eval2( oclString, elem );
//          Debug.outln( "eval2 \"" + oclString + "\" for element !! "
//                       + elem.get_representationText() );
//          Debug.outln( "    result = " + result + "\n" );
//        } catch ( Exception ex ) {
//          ex.printStackTrace();
//        }
//      }
//    }
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
//      if ( result instanceof Set ) {
//        for ( Stereotype key : (Set< Stereotype >)result ) {
//          System.out.println( "\t" + key.getHumanName() );
//        }
//      } else if ( result instanceof List ) {
//        for ( Property prop : (List< Property >)result ) {
//          System.out.println( "\t" + prop.getHumanName() );
//        }
//      }
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
