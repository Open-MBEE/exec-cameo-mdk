/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.EmfUtils;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.RepeatInputComboBoxDialog;
import gov.nasa.jpl.ocl.OCLSyntaxHelper;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.helper.ConstraintKind;
import org.junit.Assert;

import com.nomagic.magicdraw.actions.MDAction;
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
    public ArrayList< Object > parseAndProcess( Object input ) {
      String oclString = input == null ? null : input.toString();
      ArrayList< Object > outputList = new ArrayList< Object >();
      ArrayList< Object > localContext = new ArrayList< Object >();
      localContext.addAll( getContext() );

      OCLSyntaxHelper syntaxHelper = null;
      
//      // TODO -- How does this work when expressions are nested and not just chained?
//      // Parse as OCL.
      EObject contextEObject = null;
//      for ( Object o : localContext ) {
//        if ( o instanceof Element ) {
//          contextEObject = (EObject)o;
//          break;
//        }
//      }
//      if ( contextEObject == null ) {
      for ( Object o : localContext ) {
        if ( o instanceof EObject ) {
          contextEObject = (EObject)o;
//            break;
//          }
//        }
//      }
          Object result =
              OclEvaluator.evaluateQuery( contextEObject, oclString, true );
      
          // If the parse succeeds, return the result.
          if ( OclEvaluator.isValid() ) {
            // return result;
            outputList.add( result );
          } else {
            outputList.add( null );
          }
      
          // Get the evaluation result up to the point where parse failed.
          syntaxHelper =
              new OCLSyntaxHelper( OclEvaluator.getOcl().getEnvironment() );
          List completions =
              syntaxHelper.getSyntaxHelp( ConstraintKind.INVARIANT, oclString );
          Debug.outln( "completions = " + completions );
      
          // Try to parse using additional syntax where the parse failed in the
          // context of the result from the previous step.
          // TODO

          // If the parse fails, return with failure.
          // TODO

          // If there is more to parse, continue at step 1 in the context of the
          // new result with the remaining unparsed input string.
          // TODO

          // try to parse as OCL
          // TODO
      
        }
      }

      
      // ALTERNATIVE APPROACH
      // Try to parse in pieces using "." and "->" as delimiters.
      if ( syntaxHelper == null ) {
        int pos = 0;
        boolean found = true;
        while ( !found ) {
          int nextPos1 = oclString.indexOf( '.', pos );
          int nextPos2 = oclString.indexOf( "->", pos );
          // TODO -- HERE!!
        }
      }      
      
//      String[] split = oclString.split( "[.]|([-][>])" );
//      ArrayList<String> tokens = new ArrayList< String >();
//      int insideExpression = 0; 
//      for ( String s : split ) {
//        if (Utils2.count("\"", s) % 2 != insideExpression ) {
//          // expression is still open
//        }
//      }

      return outputList;
    }    
    
    public ArrayList<Object> process( Element elem, String oclString ) {
      ArrayList<Object> outputList = new ArrayList< Object >(); 
      Object result = null;
      String output = null;
      try {
        if ( elem == null ) return null;
        result = OclEvaluator.evaluateQuery( elem, oclString, true );
        output = toString(result);
        String type = null;
        
        //EmfUtils.getTypeName( result ); // TODO -- THIS LINE REPLACES BELOW
        Object o = result;
        if ( o == null ) return null;
        EObject eo = (EObject)( o instanceof EObject ? o : null );
        Class< ? > c = ( eo != null ? EmfUtils.getType( eo ) : o.getClass() );
        if ( c == null ) return null;
        type = c.getSimpleName();
        
        output = output + " : " + type;
        Debug.outln(
            "evaluated \"" + oclString + "\" for element "
                + toString(elem) + "\n" + "    result = "
                + output + "\n" );

        outputList.add( output );
      } catch ( Exception ex ) {
        String errorStr = getStackTrace( ex );
        Debug.outln( errorStr );
        outputList.add( errorStr );
      }
      Debug.outln( OclEvaluator.commandCompletionChoiceStrings( null, elem,
                                                                oclString )//, 3 )
                               .toString() );
      return outputList;
    }
    
    @Override
    public Object process( Object input ) {
      String oclString = input == null ? null : input.toString();
      ArrayList<Object> outputList = new ArrayList< Object >(); 
//      outputList = parseAndProcess( input );
//      if ( Utils2.isNullOrEmpty( outputList ) ) {
//        outputList = new ArrayList< Object >(); 
//      } else {
//        return outputList;
//      }
      if ( Utils2.isNullOrEmpty( getContext() ) ) {
        outputList = process( null, oclString );
      } else for ( Element elem : getContext() ) {
        ArrayList< Object > results = process( elem, oclString );
        if ( results != null ) outputList.addAll( results );
      }
      if ( outputList != null && outputList.size() == 1 ) return outputList.get( 0 );
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
    Collection< Element > selectedElements = MDUtils.getSelection( e );
    setContext( selectedElements );

    boolean wasOn = Debug.isOn();
    Debug.turnOn();
    RepeatInputComboBoxDialog.showRepeatInputComboBoxDialog( "Enter an OCL expression:",
                                                             "OCL Evaluation",
                                                             new ProcessOclQuery());
    if ( !wasOn ) Debug.turnOff();
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

  /**
   * @param context the context to set
   */
  public void setContext( Collection< Element > context ) {
    getContext().clear();
    getContext().addAll( context );
  }

}
