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
package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RepeatInputComboBoxDialog implements Runnable {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 6516421214781803070L;

    /**
     * A callback interface for processing input and returning results for
     * display in the dialog.
     * 
     */
    public static interface Processor {
        public Object process(Object input);

        public List<String> getCompletionChoices();
        public Object getSourceOfCompletion();
    }

    // RepeatInputComboBoxDialog members

    // usual JOptionPane static dialog parameters
    protected Component                 parentComponent   = null;
    protected Object                    message           = null;
    protected Object[]                  items             = new Object[] {};
    protected String                    title             = null;
    protected int                       optionType        = JOptionPane.OK_CANCEL_OPTION;
    protected int                       messageType       = JOptionPane.PLAIN_MESSAGE;
    protected Icon                      icon              = null;
    // some extra parameters for customizing process button
    // protected String processButtonLabel;
    // protected Icon processButonIcon;

    // members for tracking input history
    protected static Object             lastInput         = null;
    protected static LinkedList<Object> inputHistory      = new LinkedList<Object>();
    protected static HashSet<Object>    pastInputs        = new HashSet<Object>();
    protected static LinkedList<Object> choices           = new LinkedList<Object>();
    protected static int                maxChoices        = 20;

    /**
     * callback for processing input
     */
    protected Processor                 processor         = null;

    protected static EditableListPanel  editableListPanel = null;

    protected boolean                   cancelSelected    = false;

    // protected ComponentAdapter shownListener = new ComponentAdapter() {
    // @Override
    // public void componentShown( ComponentEvent ce ) {
    // if ( editableListPanel != null ) editableListPanel.setUpFocus();
    // }
    // };
    // ComponentAdapter hiddenListener = new ComponentAdapter() {
    // @Override
    // public void componentHidden( ComponentEvent ce ) {
    // if ( editableListPanel != null ) editableListPanel.focusSet = false;
    // }
    // };

    /**
     * @param message
     * @param title
     * @param processor
     */
    public RepeatInputComboBoxDialog(Object message, String title, Processor processor) {
        super();
        this.message = message;
        this.title = title;
        this.processor = processor;
    }

    /**
     * Initialize options but do not create the dialog.
     * 
     * @param parentComponent
     * @param message
     * @param items
     * @param title
     * @param optionType
     * @param messageType
     * @param icon
     * @param choices
     * @param maxChoices
     *            max number of previous inputs to remember and provide as
     *            choices in a combo box
     * @param processor
     *            callback for processing input
     */
    public RepeatInputComboBoxDialog(Component parentComponent, Object message, Object[] items, String title,
            int optionType, int messageType, Icon icon, LinkedList<Object> choices, int maxChoices,
            Processor processor) {
        super();
        this.parentComponent = parentComponent;
        this.message = message;
        this.items = items;
        this.title = title;
        this.optionType = optionType;
        this.messageType = messageType;
        this.icon = icon;
        RepeatInputComboBoxDialog.choices = choices;
        RepeatInputComboBoxDialog.maxChoices = maxChoices;
        this.processor = processor;
    }

    public static void showRepeatInputComboBoxDialog(Object message, String title, Processor processor) {
        RepeatInputComboBoxDialog dialog = new RepeatInputComboBoxDialog(message, title, processor);
        dialog.show();
    }

    public static void showRepeatInputComboBoxDialog(Component parentComponent, Object message,
            Object[] items, String title, int optionType, int messageType, Icon icon,
            LinkedList<Object> choices, int maxChoices, Processor processor) {
        RepeatInputComboBoxDialog dialog = new RepeatInputComboBoxDialog(parentComponent, message, items,
                title, optionType, messageType, icon, choices, maxChoices, processor);
        dialog.show();
    }

    public void show() {
        try {
            SwingUtilities.invokeLater(this);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Window w = RequestFocusListener.getWindow(editableListPanel);
        // Window w = getTopComponentOfType( dialog.editableListPanel,
        // Window.class );//SwingUtilities.getWindowAncestor(
        // dialog.editableListPanel );
        if (RequestFocusListener.locationOnClose != null)
            w.setLocation(RequestFocusListener.locationOnClose);// else
                                                                // w.setLocation(1000,1000);
        if (RequestFocusListener.sizeOnClose != null)
            w.setSize(RequestFocusListener.sizeOnClose);// else
                                                        // w.setLocation(1000,1000);
        // if ( RequestFocusListener.size != null ) w.setSize(
        // RequestFocusListener.size );
        if (w instanceof Dialog)
            ((Dialog)w).setResizable(true);
        // Debug.outln("w=" + w);
    }

    @Override
    public void run() {
        Object selectedItem = "";
        editableListPanel = null;

        while (selectedItem != null) {
            if (editableListPanel == null) {
                editableListPanel = new EditableListPanel((String)message, choices.toArray());
                message = editableListPanel;
            } else {
                editableListPanel.setItems(choices.toArray());
            }

            if (lastInput != null) {
                Object result = RepeatInputComboBoxDialog.this.processor.process(lastInput);
                editableListPanel.setResultPanel(result);
            }
            Debug.outln("lastInput = " + lastInput);

            editableListPanel.setVisible(true);
            int option = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType,
                    messageType, icon);
            selectedItem = getSelectedItem(message);
            if (option == JOptionPane.CANCEL_OPTION) {
                selectedItem = null;
                Debug.outln//        ((BorderLayout)contentPane.getLayout()).setVgap( ((BorderLayout)contentPane.getLayout()).getVgap()*2 + 5 );
//              ((BorderLayout)contentPane.getLayout()).setHgap( ((BorderLayout)contentPane.getLayout()).getHgap()*2 + 10 );
("CANCEL! EditableListPanel value: " + selectedItem);
            } else if (option == JOptionPane.CLOSED_OPTION) {
                selectedItem = null;
                Debug.outln("CLOSED! EditableListPanel value: " + selectedItem);
            } else {
                Debug.outln("EditableListPanel value: " + selectedItem);
            }

            if (selectedItem == null || Utils2.isNullOrEmpty(selectedItem.toString())) {
                selectedItem = null;
            } else {
                lastInput = selectedItem;
                inputHistory.push(selectedItem);
                if (pastInputs.contains(selectedItem)) {
                    choices.remove(selectedItem);
                } else {
                    pastInputs.add(selectedItem);
                }
                choices.push(selectedItem);
                while (choices.size() > maxChoices) {
                    choices.pollLast();
                }
            }
        }
    }

    public static Object[] getSelectedObjects(Component c) {
        if (c instanceof EditableListPanel) {
            return new Object[] {((EditableListPanel)c).getValue()};
        }
        if (c instanceof ItemSelectable) {
            return ((ItemSelectable)c).getSelectedObjects();
        }
        if (c instanceof Container) {
            for (Component sub: ((Container)c).getComponents()) {
                Object[] selection = getSelectedObjects(sub);
                if (selection != null)
                    return selection;
            }
        }
        return null;
    }

    public static Object getSelectedItem(Object component) {
        Object selectedItem = null;
        if (component instanceof EditableListPanel) {
            selectedItem = ((EditableListPanel)component).getValue();
        } else if (component instanceof Component) {
            Object[] selection = getSelectedObjects((Component)component);
            if (selection.length == 1) {
                selectedItem = selection[0];
            } else {
                selectedItem = selection;
            }
        }
        return selectedItem;
    }

    public static class EditableListPanel extends JPanel {

        private static final long serialVersionUID = 8166263196543269359L;

        public JComboBox          jcb              = null;
        public JComponent         resultPane       = null;
        public JComponent         completionsPane  = null;
        public JScrollPane        resultScrollPane = null;
        public JScrollPane        completionsScrollPane = null;
        JLabel label = null;
        JLabel resultLabel = null;
//        JLabel completionsLabel = null;

        public EditableListPanel(String msg, Object[] items) { // , String
                                                               // processButtonLabel,
                                                               // Icon
                                                               // processButtonIcon
                                                               // ) {
            super(new SpringLayout());
            SpringLayout layout = (SpringLayout)getLayout();
            setItems(items);

            label = new JLabel(msg);
            resultLabel = new JLabel("Result of evaluation:");
//            completionsLabel = new JLabel("Operations:");
            
            resultPane = createEditorPane("<br>");
            completionsPane = createEditorPane("<br>");
            resultScrollPane = new JScrollPane(resultPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            completionsScrollPane = new JScrollPane(completionsPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//            JPanel completionsPane = new JPanel();
//            completionsPane.add( completionsLabel );
//            completionsPane.add( completionsScrollPane );
            JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, resultScrollPane, completionsScrollPane );
            
            add(label);
            add(jcb);
            add(resultLabel);
            add(splitPane);
//            add(resultScrollPane);
//            add(completionsScrollPane);
            // putConstraint(e1, c1, pad, e2, c2): value(e1, c1) := value(e2, c2) + pad
            layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, this);
            
            layout.putConstraint(SpringLayout.NORTH, jcb, 5, SpringLayout.SOUTH, label);
            layout.putConstraint(SpringLayout.WEST, jcb, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.EAST, jcb, -5, SpringLayout.EAST, this);

            layout.putConstraint(SpringLayout.NORTH, resultLabel, 5, SpringLayout.SOUTH, jcb);
            layout.putConstraint(SpringLayout.WEST, resultLabel, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.EAST, resultLabel, -5, SpringLayout.EAST, this);
            
//            layout.putConstraint(SpringLayout.NORTH, resultScrollPane, 5, SpringLayout.SOUTH, jcb);
//            layout.putConstraint(SpringLayout.WEST, resultScrollPane, 5, SpringLayout.WEST, this);
//            layout.putConstraint(SpringLayout.EAST, resultScrollPane, -5, SpringLayout.EAST, this);
////            layout.putConstraint(SpringLayout.SOUTH, resultScrollPane, -5, SpringLayout.NORTH, completionsScrollPane);
//
//            layout.putConstraint(SpringLayout.NORTH, completionsScrollPane, 5, SpringLayout.SOUTH, resultScrollPane);
//            layout.putConstraint(SpringLayout.WEST, completionsScrollPane, 5, SpringLayout.WEST, this);
//            layout.putConstraint(SpringLayout.EAST, completionsScrollPane, -5, SpringLayout.EAST, this);
//            layout.putConstraint(SpringLayout.SOUTH, completionsScrollPane, -5, SpringLayout.SOUTH, this);

            layout.putConstraint(SpringLayout.NORTH, splitPane, 5, SpringLayout.SOUTH, resultLabel);
            layout.putConstraint(SpringLayout.WEST, splitPane, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.EAST, splitPane, -5, SpringLayout.EAST, this);
            layout.putConstraint(SpringLayout.SOUTH, splitPane, -5, SpringLayout.SOUTH, this);
            // super( new BorderLayout( 5, 5 ) );
            // setItems( items );
            // add( new JLabel( msg ), BorderLayout.NORTH );
            // add( jcb, BorderLayout.NORTH );

            resultScrollPane.setMinimumSize(new Dimension(100, 50));
            completionsScrollPane.setMinimumSize(new Dimension(100, 50));

            // add( resultScrollPane, BorderLayout.CENTER );
            addAncestorListener(new RequestFocusListener());
        }

        private JEditorPane createEditorPane(String html) {
            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.setText(html);
            editorPane.setContentType("text/html");
            editorPane.setBackground( new Color( 245, 255, 245 ) );
            return editorPane;
        }

        public void setItems(Object[] items) {
            getJcb().setModel(new DefaultComboBoxModel(items));
        }

        public JComboBox getJcb() {
            if (jcb == null) {
                jcb = new JComboBox();
                jcb.setEditable(true);
                ComboBoxEditor editor = jcb.getEditor();
                Component cmp = editor.getEditorComponent();
                if (cmp instanceof JTextField) {
                    JTextField field = (JTextField)cmp;
                    field.addAncestorListener(new RequestFocusListener());
                }
                jcb.addAncestorListener(new RequestFocusListener());
            }
            return jcb;
        }

        // public String toHtml( String s ) {
        // if ( s.contains( "<html>" ) ) return s;
        // return "<html>" + s.replaceAll( "\\n", "<br>\\n" ) + "</html>";
        // }

        // private Component makeComponent( Object result ) {
        // if ( result instanceof Component ) {
        // return (Component)result;
        // }
        // if ( result instanceof Icon ) {
        // return new JLabel( (Icon)result );
        // }
        // return new JTextArea( toHtml(result.toString()) );
        // }

        public void setTextInPanel(JComponent targetPane, JScrollPane targetScrollPane, Object newText) {
            if (targetPane instanceof JEditorPane) {
                if (newText == null)
                    newText = "null";
                ((JEditorPane)targetPane).setText( // toHtml(
                        newText.toString());
            } else {
                JComponent newResultPane = null;
                if (newText instanceof JComponent) {
                    newResultPane = (JComponent)newText;
                } else if (newText instanceof Icon) {
                    newResultPane = new JLabel((Icon)newText);
                } else {
                    newResultPane = createEditorPane(newText == null ? "null" : newText.toString());
                }
                if (newResultPane != null) {
                    targetScrollPane.remove(targetPane);
                    targetPane = newResultPane;
                    targetScrollPane.add(targetPane);
                }
            }
            if (this.isVisible()) {
                setVisible(false);
                setVisible(true);
            }
        }

        public void setResultPanel(Object result) {
            Debug.outln("setResultPanel(" + result + ")");
            setTextInPanel( resultPane, resultScrollPane, result );
        }

        public String getValue() {
            return (String)jcb.getSelectedItem();
        }

        public ComboBoxEditor getEditor() {
            return jcb.getEditor();
        }

        public void setCompletions( List< String > completionStrings, Object completionSource ) {
            String newText = "<br>"; // empty text -- need something to avoid weird ghost bullet artifact
            if ( !Utils2.isNullOrEmpty( completionStrings ) ) {
                newText = ( "completion choices for " + completionSource + ":<br>"
                            + MoreToString.Helper.toString( completionStrings,
                                                            false, true, null,
                                                            null, "<ul><li>",
                                                            "<li>", "</ul>",
                                                            false ) );
            }
            setTextInPanel( completionsPane, completionsScrollPane, newText );
        }

    }

    /**
     * http://tips4java.wordpress.com/2010/03/14/dialog-focus/
     * 
     * Convenience class to request focus on a component.
     * 
     * When the component is added to a realized Window then component will
     * request focus immediately, since the ancestorAdded event is fired
     * immediately.
     * 
     * When the component is added to a non realized Window, then the focus
     * request will be made once the window is realized, since the ancestorAdded
     * event will not be fired until then.
     * 
     * Using the default constructor will cause the listener to be removed from
     * the component once the AncestorEvent is generated. A second constructor
     * allows you to specify a boolean value of false to prevent the
     * AncestorListener from being removed when the event is generated. This
     * will allow you to reuse the listener each time the event is generated.
     */
    public static class RequestFocusListener implements AncestorListener {
        private boolean         removeListener;
        public static Dimension size            = new Dimension(500, 300);
        public static Point     location        = null;
        public static Point     locationOnClose = null;
        public static Dimension sizeOnClose     = null;

        public static Dialog getDialog(ComponentEvent e) {
            Window w = getWindow(e.getComponent());
            Dialog d = (Dialog)(e.getComponent() instanceof Dialog ? e.getComponent() : (w instanceof Dialog
                    ? w : null));
            return d;
        }

        public class WinListener implements WindowListener {

            @Override
            public void windowOpened(WindowEvent e) {
                Debug.outln("windowOpened, size = " + size + ", location = " + location);
                // Window w = RequestFocusListener.getWindow( e.getComponent()
                // );
                Dialog d = getDialog(e);// (Dialog)( e.getComponent() instanceof
                                        // Dialog ? e.getComponent() : ( w
                                        // instanceof Dialog ? w : null ) );
                if (d != null) {
                    if (!d.isResizable()) {
                        d.setResizable(true);
                    }
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                Debug.outln("before windowClosing, size = " + size + ", location = " + location);
                // //Window w = SwingUtilities.getWindowAncestor(
                // e.getComponent() );
                // Window w = getTopComponentOfType( e.getComponent(),
                // Window.class );
                Window w = getWindow(e.getComponent());
                size = w.getSize();
                // location = e.getComponent().getLocation();
                location = w.getLocation();
                // locationOnClose = SwingUtilities.getWindowAncestor(
                // e.getComponent() ).getLocation();
                locationOnClose = new Point(location);
                sizeOnClose = new Dimension(size);
                Debug.outln("windowClosing, size = " + size + ", location = " + location);
                Debug.outln("w=" + w);
                Debug.outln("e=" + w);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                Debug.outln("before windowClosed, size = " + size + ", location = " + location);
                // //Window w = SwingUtilities.getWindowAncestor(
                // e.getComponent() );
                // Window w = getTopComponentOfType( e.getComponent(),
                // Window.class );
                Window w = getWindow(e.getComponent());
                size = w.getSize();
                // location = e.getComponent().getLocation();
                location = w.getLocation();
                // locationOnClose = SwingUtilities.getWindowAncestor(
                // e.getComponent() ).getLocation();
                locationOnClose = new Point(location);
                sizeOnClose = new Dimension(size);
                Debug.outln("windowClosed, size = " + size + ", location = " + location);
                Debug.outln("w=" + w);
                Debug.outln("e=" + w);
            }

            @Override
            public void windowIconified(WindowEvent e) {
                Debug.outln("windowIconified, size = " + size + ", location = " + location);
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                Debug.outln("windowDeiconified, size = " + size + ", location = " + location);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                Debug.outln("windowActivated, size = " + size + ", location = " + location);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                Debug.outln("windowDeactivated, size = " + size + ", location = " + location);
            }

        }

        public class SizeListener implements ComponentListener {
            protected Window window = null;
            
            public void setWindow( Window w ) {
                window = w;
            }
            public Window getWindow() {
                return window;
            }
            
            @Override
            public void componentResized(ComponentEvent e) {
                //Debug.outln("before componentResized, size = " + size + ", location = " + location);

                // //Window w = SwingUtilities.getWindowAncestor(
                // e.getComponent() );
                // Window w = getTopComponentOfType( e.getComponent(),
                // Window.class );
                if ( getWindow() == null ) {
                    setWindow( RequestFocusListener.getWindow(e.getComponent()) );
                }
                Window w = getWindow();
                if ( e.getComponent() != w ) return;
                
                size = w.getSize();
                location = w.getLocation();
                // size = e.getComponent().getSize();
                // location = e.getComponent().getLocation();
                Debug.outln("componentResized, size = " + size + ", location = " + location);
                Debug.outln("w=" + w);
                Debug.outln("e=" + w);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                //Debug.outln("before componentMoved, size = " + size + ", location = " + location);

                // //Window w = SwingUtilities.getWindowAncestor(
                // e.getComponent() );
                // Window w = getTopComponentOfType( e.getComponent(),
                // Window.class );
                if ( getWindow() == null ) {
                    setWindow( RequestFocusListener.getWindow(e.getComponent()) );
                }
                Window w = getWindow();
                if ( e.getComponent() != w ) return;
                
                size = w.getSize();
                location = w.getLocation();
                // size = e.getComponent().getSize();
                // location = e.getComponent().getLocation();
                Debug.outln("componentMoved, size = " + size + ", location = " + location);
                Dialog d = getDialog(e);
                if (d != null) {
                    if (!d.isResizable()) {
                        d.setResizable(true);
                    }
                }
                Debug.outln("w=" + w);
                Debug.outln("e=" + w);
                // if ( e.getComponent() instanceof Dialog ) {
                // if ( !( (Dialog)e.getComponent() ).isResizable() ) {
                // ( (Dialog)e.getComponent() ).setResizable( true );
                // }
                // }
            }

            @Override
            public void componentShown(ComponentEvent e) {
                //Debug.outln("componentShown, size = " + size + ", location = " + location);

                // //Window w = SwingUtilities.getWindowAncestor(
                // e.getComponent() );
                // Window w = getTopComponentOfType( e.getComponent(),
                // Window.class );
                if ( getWindow() == null ) {
                    setWindow( RequestFocusListener.getWindow(e.getComponent()) );
                }
                Window w = getWindow();
                if ( e.getComponent() != w ) return;
                if (locationOnClose != null) {
                    w.setLocation(locationOnClose);
                } // else w.setLocation(1000,1000);
                if (sizeOnClose != null) {
                    w.setSize(sizeOnClose);
                }
                Dialog d = getDialog(e);
                if (d != null) {
                    if (!d.isResizable()) {
                        d.setResizable(true);
                    }
                }
                // if ( w instanceof Dialog ) {
                // if ( !( (Dialog)w ).isResizable() ) {
                // ( (Dialog)w ).setResizable( true );
                // }
                // }
                // // if ( e.getComponent() instanceof Dialog ) {
                // // if ( !( (Dialog)e.getComponent() ).isResizable() ) {
                // // ( (Dialog)e.getComponent() ).setResizable( true );
                // // }
                // // }
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                Debug.outln("componentHidden, size = " + size + ", location = " + location);
                // Window w = SwingUtilities.getWindowAncestor( e.getComponent()
                // );
                //Window w = getTopComponentOfType(e.getComponent(), Window.class);
                // size = e.getComponent().getSize();
                // location = e.getComponent().getLocation();
                Dialog d = getDialog(e);
                if (d != null) {
                    if (!d.isResizable()) {
                        d.setResizable(true);
                    }
                }

                if ( getWindow() == null ) {
                    setWindow( RequestFocusListener.getWindow(e.getComponent()) );
                }
                Window w = getWindow();
                if ( e.getComponent() != w ) return;
                
                locationOnClose = new Point(w.getLocation());
                sizeOnClose = new Dimension(w.getSize());
                // if ( w instanceof Dialog ) {
                // if ( !( (Dialog)w ).isResizable() ) {
                // ( (Dialog)w ).setResizable( true );
                // }
                // }
                // // if ( e.getComponent() instanceof Dialog ) {
                // // if ( !( (Dialog)e.getComponent() ).isResizable() ) {
                // // ( (Dialog)e.getComponent() ).setResizable( true );
                // // }
                // // }
            }
        }

        /*
         * Convenience constructor. The listener is only used once and then it
         * is removed from the component.
         */
        public RequestFocusListener() {
            this(false);
        }

        public static Window getWindow(Component component) {
            JWindow top = getTopComponentOfType(component, JWindow.class);
            JDialog dialog = getTopComponentOfType(component, JDialog.class);
            Window win = (dialog == null ? top : dialog);
            return win;
        }

        /*
         * Constructor that controls whether this listen can be used once or
         * multiple times.
         * 
         * @param removeListener when true this listener is only invoked once
         * otherwise it can be invoked multiple times.
         */
        public RequestFocusListener(boolean removeListener) {
            this.removeListener = removeListener;
        }

        @Override
        public void ancestorAdded(AncestorEvent e) {
            Debug.outln("ancestorAdded(" + e + ")");
            JComponent component = e.getComponent();
            component.requestFocusInWindow();
            if (component instanceof JTextField) {
                ((JTextField)component).selectAll();
            }

            JWindow top = getTopComponentOfType(component, JWindow.class);
            JDialog dialog = getTopComponentOfType(component, JDialog.class);
            Window win = (dialog == null ? top : dialog);
            // Window w = SwingUtilities.getWindowAncestor( e.getComponent() );
            Window w = win;
            // Debug.outln("w=" + w);

            if (win != w) {
                Debug.error(false, false, "win != w");
                if (w != null) {
                    win = w;
                    if (w instanceof JDialog)
                        dialog = (JDialog)w;
                }
            }
            try {
                if (win != null) {
                    //GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    //        .getDefaultScreenDevice();
                    // Dimension screenSize =
                    // Toolkit.getDefaultToolkit().getScreenSize();
                    // Dimension screenSize = new Dimension(
                    // gd.getDisplayMode().getWidth(),
                    // gd.getDisplayMode().getHeight() );
                    // if ( !win.getMaximumSize().equals( screenSize ) ) {
                    // win.setMaximumSize( screenSize );
                    // }
                    // if ( !win.getPreferredSize().equals( size ) ) {
                    // win.setPreferredSize( size );
                    // }
                    Debug.outln("location=" + location);
                    Debug.outln("locationOnClose=" + locationOnClose);
                    Debug.outln("size=" + size);
                    Debug.outln("sizeOnClose=" + sizeOnClose);
//                    if (locationOnClose != null) {
//                        win.setLocation(locationOnClose);
//                    } else if (location != null && !win.getLocation().equals(location)) {
//                        win.setLocation(location);
//                    }
//                    if (sizeOnClose != null) {
//                        win.setSize(sizeOnClose);
//                    }
                    win.setMinimumSize(new Dimension(300, 200));

                    // add listeners
                    boolean found = false;
                    for (WindowListener wl: win.getWindowListeners()) {
                        if (wl.getClass().equals(WinListener.class)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        win.addWindowListener(new WinListener());
                    for (ComponentListener wl: win.getComponentListeners()) {
                        if (wl.getClass().equals(SizeListener.class)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        win.addComponentListener(new SizeListener());

                    found = false;
                    if (dialog != null) {
                        dialog.setResizable(true);
                        Debug.outln("dialog = " + dialog.toString());
                    } else if (top != null) {
                        Debug.outln("rootPane = " + component.getRootPane().toString());
                        Debug.outln("top = " + top.toString());
                    }
                }
            } catch (NullPointerException npe) {
                Debug.errln(npe.getMessage());
            }

            if (removeListener)
                component.removeAncestorListener(this);
        }

        @Override
        public void ancestorMoved(AncestorEvent e) {
        }

        @Override
        public void ancestorRemoved(AncestorEvent e) {
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Debug.turnOn();
        RepeatInputComboBoxDialog.Processor processor = new RepeatInputComboBoxDialog.Processor() {
            @Override
            public Object process(Object input) {
                System.out.println("processing " + input);
                return "processed " + input;
            }

            @Override
            public List< String > getCompletionChoices() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object getSourceOfCompletion() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        RepeatInputComboBoxDialog.showRepeatInputComboBoxDialog("Enter an OCL expression:", "OCL Evaluation",
                processor);
    }

    /**
     * @param component
     * @param type
     * @return the highest [grand]parent of component of the specified type or,
     *         if no such parent exists, component
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T getTopComponentOfType(Component component, Class<T> type) {
        if (component == null)
            return null;
        if (type == null)
            type = (Class<T>)Component.class;
        Container parent = component.getParent();
        while (parent != null) {// && !( type.isInstance( parent ) ) ) {
            if (type.isInstance(parent)) {
                return getTopComponentOfType(parent, type);
            }
            parent = parent.getParent();
        }
        return (T)(type.isInstance(component) ? component : null);
    }

}
