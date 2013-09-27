/**
 * 
 */
package gov.nasa.jpl.mbee.lib;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
/**
 *
 */
public class Debug {
  protected static boolean on = false;
  public static GUILog gl = getGuiLog();

  /**
   * Iterative deepening search for a Component of a specified type contained by
   * the Container c or a subcomponent of c.
   * 
   * @param c
   * @param type
   * @return the first Component that is an instance of type in a level order
   *         traversal of contained components.
   */
  public static < TT > TT getComponentOfType( Container c, Class< TT > type ) {
    if ( c == null ) return null;
    if ( type == null ) return null;
    // TODO -- MOVE TO A DIFFERENT UTILS CLASS!
    int depth = 0;
    while ( true ) {
      Pair< Boolean, TT > p = getComponentOfType( c, type, 0, depth++, null );
      if ( p == null ) return null;
      if ( !p.first ) return null;
      TT tt = p.second;
      if ( tt != null ) return tt;
    }
  }
  public static < TT > Pair<Boolean, TT>
      getComponentOfType( Object o, Class< TT > type, int depth, int maxDepth,
                          Seen<Object> seen ) {
    if ( o == null ) return null;
    if ( type == null ) return null;
    // don't check same component twice 
    Pair< Boolean, Seen< Object > > p = Utils2.seen( o, true, seen );
    if ( p.first ) return null;
    seen = p.second;

    boolean gotLeafContainer = false;
    
    Container c = null;
    String name = null;
    if ( o instanceof Container ) {
      c = (Container)o;
      name = c.getName();
    } else {
      name = o.toString();
    }
    
    if ( depth >= maxDepth ) {
//      System.out.println( "getComponentOfType(" +
//                          o.getClass().getSimpleName() + ":" + name +
//                          ", depth=" + depth + ")" ); // checking " + cmp.getClass().getSimpleName() +
//                          //":" + cmp.getName() );
      if ( type.isInstance( o ) ) {
        try {
          return new Pair< Boolean, TT >( true, (TT)o );
        } catch ( ClassCastException e ) {}
      }
      if ( c instanceof Container ) {
        gotLeafContainer = true;
      }
    } else {
      if ( c != null ) {
        Component[] cmps = c.getComponents();
        if ( cmps.length > 0 ) {
//          System.out.println( "getComponentOfType(" +
//              o.getClass().getSimpleName() + ":" + name +
//              ", depth=" + depth + "): components = " + Utils.toString( cmps ) );
        }
        for ( Component cmp : cmps ) {
//          if ( cmp instanceof Container ) {
            TT tt = null;
            Pair< Boolean, TT > pp =
                getComponentOfType( (Container)cmp, type, depth + 1, maxDepth,
                                    seen );
            if ( pp != null ) {
              if ( pp.first == true ) {
                gotLeafContainer = true;
                if ( pp.second != null ) {
                  return pp;
                }
              }
            }
//          }
        }
      }
      Field[] fields = ClassUtils.getAllFields(o.getClass());
//      if ( fields.length > 0 ) {
//        System.out.println( "getComponentOfType(" +
//            o.getClass().getSimpleName() + ":" + name +
//            ", depth=" + depth + "): fields = " + Utils.toString( fields ) );
//      }
      for ( Field f : fields ) {
        Object v = null;
        try {
          v = f.get( o );
        } catch ( IllegalArgumentException e ) {
        } catch ( IllegalAccessException e ) {
        }
        if ( v != null ) {
          if ( ClassUtils.isPrimitive( v ) && !ClassUtils.isPrimitive( type ) ) { 
            continue;
          }
          //System.out.print( "field " + f.getName() + ": " );
          TT tt = null;
          Pair< Boolean, TT > pp =
              getComponentOfType( v, type, depth + 1, maxDepth, seen );
          if ( pp != null ) {
            if ( pp.first == true ) {
              gotLeafContainer = true;
              if ( pp.second != null ) {
                return pp;
              }
            }
          }
        }
      }
    }
    return new Pair< Boolean, TT >( gotLeafContainer, null );
  }

  public static StringBuffer glBuf = new StringBuffer(); 
  public static StringBuffer glErrBuf = new StringBuffer();

  public static GUILog getGuiLog() {
    GUILog glt = null;
    try {
      Application app = Application.getInstance();
      if ( app == null ) return null;
      if ( app.getMainFrame() == null ) return null;
      glt = app.getGUILog();
      if ( glt == null ) return null;
      //glt.log("initializing log");
    } catch (NoClassDefFoundError e) {
      glt = null;
      System.out.println("Failed to get MagicDraw GUI log; continuing without.");
    } catch (NullPointerException e) {
      glt = null;
      System.out.println("Failed to get MagicDraw GUI log; continuing without.");
    }
    return glt;
  }
  
  public static synchronized void turnOn() {
    on = true;
    gl = getGuiLog();
  }
  public static synchronized void turnOff() {
    on = false;
  }
  
  /**
   * Place a breakpoint here and call breakpoint() wherever you need to add a
   * command to break on. For example:<br>
   * {@code if ( input.equals("x") ) Debug.breakpoint();}<br>
   * This makes it easy to clean up after debugging since you can show the Call
   * Hierarchy (Ctrl-Alt-h while breakpoint() is selected) to see where it's
   * being called.
   */
  public static void breakpoint() {
    if ( Debug.isOn() ) out( "" );
  }
  
  protected static boolean isGuiThread() {
      return javax.swing.SwingUtilities.isEventDispatchThread();
  }
  
  public static void out( String s ) {
      log( s, false, false );
//    if (on) {
//      if ( gl != null ) {
//        glBuf.append( s );
//      }
//      System.out.print( s );
//    }
  }
  public static void outln( String s ) {
      log( s, true, false );
//    if (on) {
//      if ( gl != null ) {
//        gl.log( glBuf.toString() + s );
//      }
//      System.out.println( s );
//      glBuf = new StringBuffer();
//    }
  }
  public static void err( String s ) {
      log( s, false, true );
//    if (on) {
//      if ( gl != null ) {
//        glErrBuf.append( s );
//      }
//      System.err.print( s );
//    }
  }
  public static void errln( String s ) {
      log( s, true, true, Color.RED );
//    if (on) {
//      if ( gl != null ) {
//        //gl.showError( "ERR: " + glErrBuf.toString() + s );
//        logWithColor( "ERR: " + glErrBuf.toString() + s + "\n", Color.RED );
//      }
//      System.err.println( s );
//      glErrBuf = new StringBuffer();
//    }
  }
  public static boolean isOn() {
    return on;
  }

    public static void logUnsafe( final String s, final boolean addNewLine,
                                  final boolean isErr, final Color color ) {
        if ( !isOn() ) return;
        String ss = s;
        Color newColor = color;
        StringBuffer sb = ( isErr ? glErrBuf : glBuf );

        if ( addNewLine ) {
            ss = sb.toString() + ss + "\n";
        }
        if ( isErr && addNewLine ) {
            if ( newColor == null ) {
                newColor = Color.RED;
            }
            ss = "ERR: " + ss;
        } else {
            if ( newColor == null ) {
                newColor = Color.BLACK;
            }
        }

        if ( !addNewLine ) {
            sb.append( ss );
        } else if ( gl != null ) {
            if ( newColor != Color.BLACK ) {
                logWithColor( ss, newColor );
            } else {
                gl.log( ss );
            }
            if ( isErr ) glErrBuf = new StringBuffer();
            else glBuf = new StringBuffer();
        }

        PrintStream stream = ( isErr ? System.err : System.out );
        stream.print( ss );
        stream.flush();
    }
  
    public static void log( final String s ) {
        log( s, true, false );
    }
    public static void log( final String s, final boolean addNewLine,
                            final boolean isErr ) {
        log( s, addNewLine, isErr, null );
    }
    public static void log( final String s, final boolean addNewLine,
                            final boolean isErr, final Color color ) {
        if ( !on ) return;
        if ( isGuiThread() ) {
            logUnsafe( s, addNewLine, isErr, color );
            return;
        }
        try {
            SwingUtilities.invokeAndWait( new Runnable() {
                @Override
                public void run() {
                    logUnsafe(s, addNewLine, isErr, color);
                }
            } );
        } catch ( Exception e ) {
            System.err.println(e.getLocalizedMessage());
        }
    }

  public static void logWithColor( String msg, Color color ) {
    JDialog log = gl.getLog();
    //JPanel jp = getComponentOfType( log, JPanel.class );//(JPanel)((java.awt.Container)log).getComponent( 0 ); //.getComponents();
    //JEditorPane jep = getComponentOfType( jp, JEditorPane.class );//(JEditorPane)jp.getComponent( 0 );
    StyledDocument doc = getComponentOfType( log, StyledDocument.class );
    if ( doc == null ) {
      JEditorPane jep = getComponentOfType( log, JEditorPane.class );//(JEditorPane)jp.getComponent( 0 );
      if ( jep != null ) {
        doc = (StyledDocument)jep.getDocument();
      } else {
        System.out.println("ERROR! Failed to find Document!");
        System.err.println("ERROR! Failed to find Document!");
        return;
      }
    }
    SimpleAttributeSet set = new SimpleAttributeSet();
    StyleConstants.setForeground( set, color ); // Color.GREEN
    int i = doc.getLength();
    try {
      doc.insertString( i, msg, set );
    } catch ( BadLocationException e ) {
      e.printStackTrace();
    }
  }
  
  /**
   * Throws and catches an exception and prints a supplied message and stack
   * trace to stderr if any of the input objects are null.
   * 
   * @param msg
   * @param maybeNullObjects
   *          variable number of Objects to check if null
   * @return
   */
  public static boolean errorOnNull( String msg, Object... maybeNullObjects ) {
    return errorOnNull( true, msg, maybeNullObjects );
  }

  /**
   * Throws and catches an exception if any of the input objects are null. It
   * prints a supplied message and, optionally, a stack trace to stderr.
   * 
   * @param msg
   * @param maybeNullObjects
   *          variable number of Objects to check if null
   * @return
   */
  public static boolean errorOnNull( boolean stackTrace, String msg,
                                     Object... maybeNullObjects ) {
      return errorOnNull( stackTrace, stackTrace, msg, maybeNullObjects );
  }
  public static boolean errorOnNull( boolean forceOutput, boolean stackTrace, String msg,
                                     Object... maybeNullObjects ) {
    try {
      if ( maybeNullObjects == null ) throw new Exception();
      for ( Object o : maybeNullObjects ) {
        if ( o == null ) {
          throw new Exception();
        } 
      }
    } catch ( Exception e ) {
      boolean wasOn = isOn();
      if ( forceOutput ) turnOn();
      Debug.errln( msg );
      if ( stackTrace ) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Debug.errln( sw.toString() );
        if ( Debug.isOn() ) {
          Debug.err( "" ); // good place for a breakpoint
          //breakpoint();
        }
      }
      if ( forceOutput && !wasOn ) turnOff(); 
      return true;
    }
    return false;
  }

  /**
   * Writes to stderr and throws and catches an exception printing a stack trace.
   * 
   * @param msg
   */
  public static void error( String msg ) {
    error( true, msg );
  }

  /**
   * Writes to stderr and throws and catches an exception, optionally printing a
   * stack trace.
   * 
   * @param msg
   */
  public static void error( boolean stackTrace, String msg ) {
    errorOnNull( stackTrace, msg, (Object[])null );
  }
  public static void error( boolean forceOutput, boolean stackTrace, String msg ) {
    errorOnNull( forceOutput, stackTrace, msg, (Object[])null );
  }
}
