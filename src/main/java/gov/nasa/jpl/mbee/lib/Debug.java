/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
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
