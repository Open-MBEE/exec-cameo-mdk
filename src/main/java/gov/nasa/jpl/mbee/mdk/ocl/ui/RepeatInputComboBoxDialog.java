package gov.nasa.jpl.mbee.mdk.ocl.ui;

import gov.nasa.jpl.mbee.mdk.util.Debug;
import gov.nasa.jpl.mbee.mdk.util.MoreToString;
import gov.nasa.jpl.mbee.mdk.util.Utils2;
import gov.nasa.jpl.mbee.mdk.ocl.OclEvaluator;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class RepeatInputComboBoxDialog implements Runnable {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 6516421214781803070L;

    /**
     * A callback interface for processing input and returning results for
     * display in the dialog.
     */
    public interface Processor {
        Object process(Object input);

        List<String> getCompletionChoices();

        Object getSourceOfCompletion();
    }

    // RepeatInputComboBoxDialog members

    // usual JOptionPane static dialog parameters
    protected Component parentComponent = null;
    protected Object message = null;
    protected Object[] items = new Object[]{};
    protected String title = null;
    protected int optionType = JOptionPane.OK_CANCEL_OPTION;
    protected int messageType = JOptionPane.PLAIN_MESSAGE;
    protected Icon icon = null;
    // some extra parameters for customizing process button
    // protected String processButtonLabel;
    // protected Icon processButonIcon;

    // members for tracking input history
    protected static Object lastInput = null;
    protected static Object lastResult = null;
    protected static LinkedList<Object> inputHistory = new LinkedList<Object>();
    protected static HashSet<Object> pastInputs = new HashSet<Object>();
    protected static LinkedList<Object> choices = new LinkedList<Object>();
    protected static int maxChoices = 20;

    /**
     * callback for processing input
     */
    protected Processor processor = null;

    protected static EditableListPanel editableListPanel = null;

    protected boolean cancelSelected = false;

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
     * @param maxChoices      max number of previous inputs to remember and provide as
     *                        choices in a combo box
     * @param processor       callback for processing input
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
        if (RequestFocusListener.locationOnClose != null) {
            w.setLocation(RequestFocusListener.locationOnClose);// else
        }
        // w.setLocation(1000,1000);
        if (RequestFocusListener.sizeOnClose != null) {
            w.setSize(RequestFocusListener.sizeOnClose);// else
        }
        // w.setLocation(1000,1000);
        // if ( RequestFocusListener.size != null ) w.setSize(
        // RequestFocusListener.size );
        if (w instanceof Dialog) {
            ((Dialog) w).setResizable(true);
        }
        // Debug.outln("w=" + w);
    }

    @Override
    public void run() {
        Object selectedItem = "";
        editableListPanel = null;
        boolean wasVerbose = OclEvaluator.isVerboseDefault;
        OclEvaluator.setVerboseDefault(true);

        while (selectedItem != null) {
            if (editableListPanel == null) {
                editableListPanel = new EditableListPanel((String) message, choices.toArray());
                message = editableListPanel;
            }
            else {
                editableListPanel.setItems(choices.toArray());
            }

            if (lastInput != null) {
                Object result = RepeatInputComboBoxDialog.this.processor.process(lastInput);
                editableListPanel.setResult(result);
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
            }
            else if (option == JOptionPane.CLOSED_OPTION) {
                selectedItem = null;
                Debug.outln("CLOSED! EditableListPanel value: " + selectedItem);
            }
            else {
                Debug.outln("EditableListPanel value: " + selectedItem);
            }

            if (selectedItem == null || Utils2.isNullOrEmpty(selectedItem.toString())) {
                selectedItem = null;
            }
            else {
                lastInput = selectedItem;
                inputHistory.push(selectedItem);
                if (pastInputs.contains(selectedItem)) {
                    choices.remove(selectedItem);
                }
                else {
                    pastInputs.add(selectedItem);
                }
                choices.push(selectedItem);
                while (choices.size() > maxChoices) {
                    choices.pollLast();
                }
            }
        }
        OclEvaluator.setVerboseDefault(wasVerbose);
    }

    public static Object[] getSelectedObjects(Component c) {
        if (c instanceof EditableListPanel) {
            return new Object[]{((EditableListPanel) c).getValue()};
        }
        if (c instanceof ItemSelectable) {
            return ((ItemSelectable) c).getSelectedObjects();
        }
        if (c instanceof Container) {
            for (Component sub : ((Container) c).getComponents()) {
                Object[] selection = getSelectedObjects(sub);
                if (selection != null) {
                    return selection;
                }
            }
        }
        return null;
    }

    public static Object getSelectedItem(Object component) {
        Object selectedItem = null;
        if (component instanceof EditableListPanel) {
            selectedItem = ((EditableListPanel) component).getValue();
        }
        else if (component instanceof Component) {
            Object[] selection = getSelectedObjects((Component) component);
            if (selection.length == 1) {
                selectedItem = selection[0];
            }
            else {
                selectedItem = selection;
            }
        }
        return selectedItem;
    }

    public static class EditableListPanel extends JPanel {

        private static final long serialVersionUID = 8166263196543269359L;

        /*public JComboBox          jcb              = null;
        public JComponent         resultPane       = null;
        public JComponent         completionsPane  = null;
        public JScrollPane        resultScrollPane = null;
        public JScrollPane        completionsScrollPane = null;
        JLabel label = null;
        JLabel resultLabel = null;*/

        public JComboBox<String> historyComboBox;
        //public JPanel queryPanel;
        public JTextArea queryTextArea;
        public JEditorPane resultEditorPane, completionEditorPane;
//        JLabel completionsLabel = null;

        public EditableListPanel(String msg, Object[] items) {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            final JPanel queryContainer = new JPanel();
            queryContainer.setLayout(new BoxLayout(queryContainer, BoxLayout.Y_AXIS));

            final JLabel queryLabel = new JLabel("Query");
            queryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            queryContainer.add(queryLabel);

            final JPanel queryPanel = new JPanel();
            //queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));
            queryPanel.setLayout(new GridBagLayout());
            final JLabel historyLabel = new JLabel("History");
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0d;
            c.weighty = 0d;
            //historyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            queryPanel.add(historyLabel, c);
            c.gridx = 1;
            //c.gridy = 0;
            c.weightx = 1d;
            historyComboBox = new JComboBox<String>();
            historyComboBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (historyComboBox.getSelectedItem() != null) {
                        queryTextArea.setText(historyComboBox.getSelectedItem().toString());
                    }
                    historyComboBox.setSelectedIndex(-1);
                }

            });
            queryPanel.add(historyComboBox, c);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            //c.gridheight = GridBagConstraints.REMAINDER;
            c.weighty = 1d;
            c.fill = GridBagConstraints.BOTH;
            queryPanel.add(createScrollPane(queryTextArea = new JTextArea(1, 50)), c);

            queryContainer.add(queryPanel);

            final JPanel resultPanel = new JPanel();
            resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
            final JLabel resultLabel = new JLabel("Result");
            // MDEV 1221
            final JCheckBox resultFormat = new JCheckBox("Render HTML", true);
            //resultLabel.setBackground(Color.RED);
            //resultLabel.setBorder(BorderFactory.createLineBorder(Color.black));
            resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultFormat.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultPanel.add(resultLabel);
            resultPanel.add(resultFormat);
            resultPanel.add(createScrollPane(resultEditorPane = createEditorPane("")));
            resultFormat.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        resultEditorPane.setContentType("text/html");
                    }
                    else {
                        resultEditorPane.setContentType("text/plain");
                    }

                    setResult(lastResult);
                }

            });

            final JPanel completionPanel = new JPanel();
            completionPanel.setLayout(new BoxLayout(completionPanel, BoxLayout.Y_AXIS));
            final JLabel completionLabel = new JLabel("Completion");
            completionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            completionPanel.add(completionLabel);
            completionPanel.add(createScrollPane(completionEditorPane = createEditorPane("")));

            final JSplitPane firstSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultPanel, completionPanel);
            final JSplitPane secondSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryContainer, firstSplitPane);
            this.add(secondSplitPane);
        }

        /*public EditableListPanel(String msg, Object[] items, String ignored) { // , String
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
            
            final JTextArea textArea = new JTextArea();
            
            add(label);
            add(jcb);
            add(textArea);
            add(resultLabel);
            add(splitPane);
//            add(resultScrollPane);
//            add(completionsScrollPane);
            // putConstraint(e1, c1, pad, e2, c2): value(e1, c1) := value(e2, c2) + pad
            layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, this);
            
            /*layout.putConstraint(SpringLayout.NORTH, jcb, 5, SpringLayout.SOUTH, label);
            layout.putConstraint(SpringLayout.WEST, jcb, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.EAST, jcb, -5, SpringLayout.EAST, this);*
            
            layout.putConstraint(SpringLayout.NORTH, textArea, 5, SpringLayout.SOUTH, label);
            layout.putConstraint(SpringLayout.WEST, textArea, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.EAST, textArea, -5, SpringLayout.EAST, this);

            /*layout.putConstraint(SpringLayout.NORTH, resultLabel, 5, SpringLayout.SOUTH, jcb);
            layout.putConstraint(SpringLayout.WEST, resultLabel, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.EAST, resultLabel, -5, SpringLayout.EAST, this);*
            
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
        }*/

        public JScrollPane createScrollPane(final Component c) {
            final JScrollPane scrollPane = new JScrollPane(c, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setPreferredSize(new Dimension((int) c.getPreferredSize().getWidth() + 5, 50));
            return scrollPane;
        }

        private JEditorPane createEditorPane(String html) {
            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.setText(html);
            editorPane.setContentType("text/html");
            editorPane.setBackground(new Color(245, 255, 245));
            return editorPane;
        }

        public void setItems(Object[] items) {
            historyComboBox.setModel(new DefaultComboBoxModel(items));
            if (items.length == 0) {
                historyComboBox.setEnabled(false);
            }
        }

        /*public JComboBox getHistoryComboBox() {
            if (historyComboBox == null) {
            	historyComboBox = new JComboBox();
                //jcb.setEditable(true);
                ComboBoxEditor editor = jcb.getEditor();
                Component cmp = editor.getEditorComponent();
                if (cmp instanceof JTextField) {
                    JTextField field = (JTextField)cmp;
                    field.addAncestorListener(new RequestFocusListener());
                }
                jcb.addAncestorListener(new RequestFocusListener());
            }
            return historyComboBox;
        }*/

        public void setTextInPanel(JComponent targetPane, Object newText) {
            if (targetPane instanceof JEditorPane) {
                if (newText == null) {
                    newText = "null";
                }
                ((JEditorPane) targetPane).setText(newText.toString());
            }
            else if (targetPane instanceof JTextArea) {
                ((JTextArea) targetPane).setText(newText.toString());
            }
            if (this.isVisible()) {
                setVisible(false);
                setVisible(true);
            }
        }

        public void setResult(Object result) {
            Debug.outln("setResultPanel(" + result + ")");
            lastResult = result;
            setTextInPanel(resultEditorPane, result);
        }

        public String getValue() {
            return queryTextArea.getText();
        }

        public String getCompletionHeader(Object completionSource) {
            return "Completion choices for " + completionSource + "<br>";
        }

        public void setCompletions(List<String> completionStrings, Object completionSource) {
            setCompletions(completionStrings, completionSource, true);
        }

        public void setCompletions(List<String> completionStrings, Object completionSource, boolean addHeader) {
            String newText = "<br>"; // empty text -- need something to avoid weird ghost bullet artifact
            if (!Utils2.isNullOrEmpty(completionStrings)) {
                newText = ((addHeader ? getCompletionHeader(completionSource) : "")
                        + MoreToString.Helper.toString(completionStrings,
                        false, true, null,
                        null, "<ul><li>",
                        "<li>", "</ul>",
                        false));
            }
            setTextInPanel(completionEditorPane, newText);
        }

        public String getQuery() {
            return queryTextArea.getText();
        }

    }

    /**
     * http://tips4java.wordpress.com/2010/03/14/dialog-focus/
     * <p>
     * Convenience class to request focus on a component.
     * <p>
     * When the component is added to a realized Window then component will
     * request focus immediately, since the ancestorAdded event is fired
     * immediately.
     * <p>
     * When the component is added to a non realized Window, then the focus
     * request will be made once the window is realized, since the ancestorAdded
     * event will not be fired until then.
     * <p>
     * Using the default constructor will cause the listener to be removed from
     * the component once the AncestorEvent is generated. A second constructor
     * allows you to specify a boolean value of false to prevent the
     * AncestorListener from being removed when the event is generated. This
     * will allow you to reuse the listener each time the event is generated.
     */
    public static class RequestFocusListener implements AncestorListener {
        private boolean removeListener;
        public static Dimension size = new Dimension(500, 300);
        public static Point location = null;
        public static Point locationOnClose = null;
        public static Dimension sizeOnClose = null;

        public static Dialog getDialog(ComponentEvent e) {
            Window w = getWindow(e.getComponent());
            Dialog d = (Dialog) (e.getComponent() instanceof Dialog ? e.getComponent() : (w instanceof Dialog
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

            public void setWindow(Window w) {
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
                if (getWindow() == null) {
                    setWindow(RequestFocusListener.getWindow(e.getComponent()));
                }
                Window w = getWindow();
                if (e.getComponent() != w) {
                    return;
                }

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
                if (getWindow() == null) {
                    setWindow(RequestFocusListener.getWindow(e.getComponent()));
                }
                Window w = getWindow();
                if (e.getComponent() != w) {
                    return;
                }

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
                if (getWindow() == null) {
                    setWindow(RequestFocusListener.getWindow(e.getComponent()));
                }
                Window w = getWindow();
                if (e.getComponent() != w) {
                    return;
                }
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

                if (getWindow() == null) {
                    setWindow(RequestFocusListener.getWindow(e.getComponent()));
                }
                Window w = getWindow();
                if (e.getComponent() != w) {
                    return;
                }

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
                ((JTextField) component).selectAll();
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
                    if (w instanceof JDialog) {
                        dialog = (JDialog) w;
                    }
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
                    for (WindowListener wl : win.getWindowListeners()) {
                        if (wl.getClass().equals(WinListener.class)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        win.addWindowListener(new WinListener());
                    }
                    for (ComponentListener wl : win.getComponentListeners()) {
                        if (wl.getClass().equals(SizeListener.class)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        win.addComponentListener(new SizeListener());
                    }

                    found = false;
                    if (dialog != null) {
                        dialog.setResizable(true);
                        Debug.outln("dialog = " + dialog.toString());
                    }
                    else if (top != null) {
                        Debug.outln("rootPane = " + component.getRootPane().toString());
                        Debug.outln("top = " + top.toString());
                    }
                }
            } catch (NullPointerException npe) {
                Debug.errln(npe.getMessage());
            }

            if (removeListener) {
                component.removeAncestorListener(this);
            }
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
            public List<String> getCompletionChoices() {
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
     * if no such parent exists, component
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T getTopComponentOfType(Component component, Class<T> type) {
        if (component == null) {
            return null;
        }
        if (type == null) {
            type = (Class<T>) Component.class;
        }
        Container parent = component.getParent();
        while (parent != null) {// && !( type.isInstance( parent ) ) ) {
            if (type.isInstance(parent)) {
                return getTopComponentOfType(parent, type);
            }
            parent = parent.getParent();
        }
        return (T) (type.isInstance(component) ? component : null);
    }

}
