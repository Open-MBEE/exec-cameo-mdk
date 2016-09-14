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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class MdDebug extends Debug {

    public static GUILog gl = getGuiLog();
    public static StringBuffer glBuf = new StringBuffer();
    public static StringBuffer glErrBuf = new StringBuffer();

    /**
     * Iterative deepening search for a Component of a specified type contained
     * by the Container c or a subcomponent of c.
     *
     * @param c
     * @param type
     * @return the first Component that is an instance of type in a level order
     * traversal of contained components.
     */
    public static <TT> TT getComponentOfType(Container c, Class<TT> type) {
        if (c == null) {
            return null;
        }
        if (type == null) {
            return null;
        }
        // TODO -- MOVE TO A DIFFERENT UTILS CLASS!
        int depth = 0;
        while (true) {
            Pair<Boolean, TT> p = getComponentOfType(c, type, 0, depth++, null);
            if (p == null) {
                return null;
            }
            if (!p.first) {
                return null;
            }
            TT tt = p.second;
            if (tt != null) {
                return tt;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <TT> Pair<Boolean, TT> getComponentOfType(Object o, Class<TT> type, int depth,
                                                            int maxDepth, Seen<Object> seen) {
        if (o == null) {
            return null;
        }
        if (type == null) {
            return null;
        }
        // don't check same component twice
        Pair<Boolean, Seen<Object>> p = Utils2.seen(o, true, seen);
        if (p.first) {
            return null;
        }
        seen = p.second;

        boolean gotLeafContainer = false;

        Container c = null;
        if (o instanceof Container) {
            c = (Container) o;
        }
        else {
        }

        if (depth >= maxDepth) {
            // System.out.println( "getComponentOfType(" +
            // o.getClass().getSimpleName() + ":" + name +
            // ", depth=" + depth + ")" ); // checking " +
            // cmp.getClass().getSimpleName() +
            // //":" + cmp.getName() );
            if (type.isInstance(o)) {
                try {
                    return new Pair<Boolean, TT>(true, (TT) o);
                } catch (ClassCastException e) {
                }
            }
            if (c instanceof Container) {
                gotLeafContainer = true;
            }
        }
        else {
            if (c != null) {
                Component[] cmps = c.getComponents();
                if (cmps.length > 0) {
                    // System.out.println( "getComponentOfType(" +
                    // o.getClass().getSimpleName() + ":" + name +
                    // ", depth=" + depth + "): components = " + Utils.toString(
                    // cmps ) );
                }
                for (Component cmp : cmps) {
                    Pair<Boolean, TT> pp = getComponentOfType(cmp, type, depth + 1, maxDepth, seen);
                    if (pp != null) {
                        if (pp.first == true) {
                            gotLeafContainer = true;
                            if (pp.second != null) {
                                return pp;
                            }
                        }
                    }
                    // }
                }
            }
            Field[] fields = ClassUtils.getAllFields(o.getClass());
            // if ( fields.length > 0 ) {
            // System.out.println( "getComponentOfType(" +
            // o.getClass().getSimpleName() + ":" + name +
            // ", depth=" + depth + "): fields = " + Utils.toString( fields ) );
            // }
            for (Field f : fields) {
                Object v = null;
                try {
                    v = f.get(o);
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
                if (v != null) {
                    if (ClassUtils.isPrimitive(v) && !ClassUtils.isPrimitive(type)) {
                        continue;
                    }
                    Pair<Boolean, TT> pp = getComponentOfType(v, type, depth + 1, maxDepth, seen);
                    if (pp != null) {
                        if (pp.first == true) {
                            gotLeafContainer = true;
                            if (pp.second != null) {
                                return pp;
                            }
                        }
                    }
                }
            }
        }
        return new Pair<Boolean, TT>(gotLeafContainer, null);
    }

    public static GUILog getGuiLog() {
        GUILog glt = null;
        try {
            Application app = Application.getInstance();
            if (app == null) {
                return null;
            }
            if (app.getMainFrame() == null) {
                return null;
            }
            glt = app.getGUILog();
            if (glt == null) {
                return null;
            }
            // glt.log("initializing log");
        } catch (NoClassDefFoundError e) {
            glt = null;
            System.out.println("Failed to get MagicDraw GUI log; continuing without.");
        } catch (NullPointerException e) {
            glt = null;
            System.out.println("Failed to get MagicDraw GUI log; continuing without.");
        }
        return glt;
    }

    protected static boolean isGuiThread() {
        return javax.swing.SwingUtilities.isEventDispatchThread();
    }

    public static void logUnsafe(final String s, final boolean addNewLine, final boolean isErr,
                                 final Color color) {
        if (!Debug.isOn()) {
            return;
        }
    }

    public static void logUnsafeForce(final String s, final boolean addNewLine, final boolean isErr,
                                      final Color color) {
        String ss = s;
        Color newColor = color;
        StringBuffer sb = (isErr ? glErrBuf : glBuf);

        if (addNewLine) {
            ss = sb.toString() + ss + "\n";
        }
        if (isErr && addNewLine) {
            if (newColor == null) {
                newColor = Color.RED;
            }
            ss = "ERR: " + ss;
        }
        else {
            if (newColor == null) {
                newColor = Color.BLACK;
            }
        }

        if (!addNewLine) {
            sb.append(ss);
        }
        else if (gl != null) {
            if (newColor != Color.BLACK) {
                logWithColor(ss, newColor);
            }
            else {
                gl.log(ss);
            }
            if (isErr) {
                glErrBuf = new StringBuffer();
            }
            else {
                glBuf = new StringBuffer();
            }
        }

        PrintStream stream = (isErr ? System.err : System.out);
        stream.print(ss);
        stream.flush();
    }

    public static void log(final String s) {
        log(s, true, false);
    }

    public static void logForce(final String s) {
        logForce(s, true, false, null);
    }

    public static void log(final String s, final boolean addNewLine, final boolean isErr) {
        log(s, addNewLine, isErr, null);
    }

    public static void log(final String s, final boolean addNewLine, final boolean isErr, final Color color) {
        if (!Debug.on) {
            return;
        }
        logForce(s, addNewLine, isErr, color);
    }

    public static void logForce(final String s, final boolean addNewLine, final boolean isErr,
                                final Color color) {
        if (isGuiThread()) {
            logUnsafeForce(s, addNewLine, isErr, color);
            return;
        }
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    logUnsafeForce(s, addNewLine, isErr, color);
                }
            });
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    public static void logWithColor(String msg, Color color) {
        if (color == null) {
            color = Color.black;
        }
        if (gl == null) {
            gl = getGuiLog();
        }
        gl.log(msg);
        /*JDialog log = gl.getLog();
        // JPanel jp = getComponentOfType( log, JPanel.class
        // );//(JPanel)((java.awt.Container)log).getComponent( 0 );
        // //.getComponents();
        // JEditorPane jep = getComponentOfType( jp, JEditorPane.class
        // );//(JEditorPane)jp.getComponent( 0 );
        StyledDocument doc = getComponentOfType(log, StyledDocument.class);
        if (doc == null) {
            JEditorPane jep = getComponentOfType(log, JEditorPane.class);// (JEditorPane)jp.getComponent(
                                                                         // 0 );
            if (jep != null) {
                doc = (StyledDocument)jep.getDocument();
            } else {
                System.out.println("ERROR! Failed to find Document!");
                System.err.println("ERROR! Failed to find Document!");
                return;
            }
        }
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, color); // Color.GREEN
        int i = doc.getLength();
        try {
            doc.insertString(i, msg, set);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }*/
    }

}
