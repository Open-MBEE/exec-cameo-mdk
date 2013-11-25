/**
 * 
 */
package gov.nasa.jpl.mbee.lib;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 */
public class Debug {
    protected static boolean on = false;

    public static synchronized void turnOn() {
        on = true;
        MdDebug.gl = MdDebug.getGuiLog();
    }

    public static synchronized void turnOff() {
        on = false;
    }

    /**
     * Place a breakpoint here and call breakpoint() wherever you need to add a
     * command to break on. For example:<br>
     * {@code if ( input.equals("x") ) Debug.breakpoint();}<br>
     * This makes it easy to clean up after debugging since you can show the
     * Call Hierarchy (Ctrl-Alt-h while breakpoint() is selected) to see where
     * it's being called.
     */
    public static void breakpoint() {
        if (Debug.isOn())
            out("");
    }

    public static void out(String s) {
        MdDebug.log(s, false, false);
        // if (on) {
        // if ( gl != null ) {
        // glBuf.append( s );
        // }
        // System.out.print( s );
        // }
    }

    public static void outln(String s) {
        MdDebug.log(s, true, false);
        // if (on) {
        // if ( gl != null ) {
        // gl.log( glBuf.toString() + s );
        // }
        // System.out.println( s );
        // glBuf = new StringBuffer();
        // }
    }

    public static void err(String s) {
        MdDebug.log(s, false, true);
        // if (on) {
        // if ( gl != null ) {
        // glErrBuf.append( s );
        // }
        // System.err.print( s );
        // }
    }

    public static void errln(String s) {
        MdDebug.log(s, true, true, Color.RED);
        // if (on) {
        // if ( gl != null ) {
        // //gl.showError( "ERR: " + glErrBuf.toString() + s );
        // logWithColor( "ERR: " + glErrBuf.toString() + s + "\n", Color.RED );
        // }
        // System.err.println( s );
        // glErrBuf = new StringBuffer();
        // }
    }

    public static boolean isOn() {
        return on;
    }

    /**
     * Throws and catches an exception and prints a supplied message and stack
     * trace to stderr if any of the input objects are null.
     * 
     * @param msg
     * @param maybeNullObjects
     *            variable number of Objects to check if null
     * @return
     */
    public static boolean errorOnNull(String msg, Object... maybeNullObjects) {
        return errorOnNull(true, msg, maybeNullObjects);
    }

    public static String stackTrace() {
        Exception e = new Exception();
        return stackTrace(e);
    }

    public static String stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Throws and catches an exception if any of the input objects are null. It
     * prints a supplied message and, optionally, a stack trace to stderr.
     * 
     * @param msg
     * @param maybeNullObjects
     *            variable number of Objects to check if null
     * @return
     */
    public static boolean errorOnNull(boolean stackTrace, String msg, Object... maybeNullObjects) {
        return errorOnNull(stackTrace, stackTrace, msg, maybeNullObjects);
    }

    public static boolean errorOnNull(boolean forceOutput, boolean stackTrace, String msg,
            Object... maybeNullObjects) {
        try {
            if (maybeNullObjects == null)
                throw new Exception();
            for (Object o: maybeNullObjects) {
                if (o == null) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            boolean wasOn = isOn();
            if (forceOutput)
                turnOn();
            Debug.errln(msg);
            if (stackTrace) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Debug.errln(sw.toString());
                if (Debug.isOn()) {
                    Debug.err(""); // good place for a breakpoint
                    // breakpoint();
                }
            }
            if (!wasOn)
                turnOff();
            return true;
        }
        return false;
    }

    /**
     * Writes to stderr and throws and catches an exception printing a stack
     * trace.
     * 
     * @param msg
     */
    public static void error(String msg) {
        error(true, msg);
    }

    /**
     * Writes to stderr and throws and catches an exception, optionally printing
     * a stack trace.
     * 
     * @param msg
     */
    public static void error(boolean stackTrace, String msg) {
        errorOnNull(stackTrace, msg, (Object[])null);
    }

    public static void error(boolean forceOutput, boolean stackTrace, String msg) {
        errorOnNull(forceOutput, stackTrace, msg, (Object[])null);
    }
}
