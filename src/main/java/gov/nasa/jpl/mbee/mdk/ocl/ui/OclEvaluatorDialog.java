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
package gov.nasa.jpl.mbee.mdk.ocl.ui;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.MoreToString;
import gov.nasa.jpl.mbee.mdk.ocl.actions.OclQueryAction;
import gov.nasa.jpl.mbee.mdk.ocl.actions.OclQueryAction.ProcessOclQuery;
import org.eclipse.ocl.util.CollectionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


/**
 *
 */
public class OclEvaluatorDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -9114812582757129836L;

    private static OclEvaluatorDialog instance = null;
    // members for tracking input history
    protected static String query = null;
    //protected static LinkedList<Object>                          inputHistory      = new LinkedList<Object>();
    //protected static HashSet<Object>                             pastInputs        = new HashSet<Object>();
    protected static LinkedList<String> choices = new LinkedList<String>();
    protected static int maxChoices = 20;
    protected boolean isPressed = false;
    private volatile boolean isThreadRunning = false;
    /**
     * callback for processing input
     */
    protected RepeatInputComboBoxDialog.Processor processor;

    protected RepeatInputComboBoxDialog.EditableListPanel editableListPanel = null;

    protected boolean cancelSelected = false;

    List<Component> lastClickedComponents = new ArrayList<Component>();

    public JCheckBox diagramCB, browserCB;
    public JRadioButton objectRadioButton, eachRadioButton;
    public JToggleButton evalButton;
    private Thread queryThread;

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog(Window owner, String title) {
        super(owner, title, ModalityType.MODELESS);
        init(owner);
    }

    protected void init(Window owner) {
        editableListPanel = new RepeatInputComboBoxDialog.EditableListPanel("Enter an OCL expression:",
                choices.toArray());

        editableListPanel.setVisible(true);

        //Create and initialize the buttons.
        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand("Close");
        closeButton.addActionListener(this);
        //
        String evaluateButtonText = "Evaluate (Shift+Enter)";
        evalButton = new JToggleButton(evaluateButtonText);
        evalButton.setActionCommand("Evaluate");
        evalButton.addActionListener(this);
        evalButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Evaluate");
        // MDEV 1221
        evalButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "Evaluate");
        editableListPanel.queryTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK),
                new AbstractAction() {

                    /**
                     *
                     */
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        evalButton.doClick();
                    }

                }
        );

        //    getRootPane().setDefaultButton(evalButton);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(closeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(evalButton);

        // checkboxes for which selected components to include: those in diagram, those in browser.   
        JPanel checkBoxPane = new JPanel();
        //checkBoxPane.setLayout( new BorderLayout() );
        checkBoxPane.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        //c.weighty = 0d;

        diagramCB = new JCheckBox("Selection from diagram", true);
        checkBoxPane.add(diagramCB, c);

        c.gridy = 1;
        browserCB = new JCheckBox("Selection from browser", false);
        checkBoxPane.add(browserCB, c);

        c.gridx = 0;
        c.gridy = 0;
        final JLabel queryLabel = new JLabel("Apply query to");
        queryLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        checkBoxPane.add(queryLabel, c);

        final ButtonGroup buttonGroup = new ButtonGroup();

        c.gridy = 1;
        objectRadioButton = new JRadioButton("Selection as a single object");
        objectRadioButton.setSelected(true);
        buttonGroup.add(objectRadioButton);
        checkBoxPane.add(objectRadioButton, c);

        c.gridy = 2;
        eachRadioButton = new JRadioButton("Each selected item");
        buttonGroup.add(eachRadioButton);
        checkBoxPane.add(eachRadioButton, c);

        //iterateCB = new JCheckBox( "Iterate", false );
        //checkBoxPane.add( iterateCB, BorderLayout.PAGE_END );

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();

        contentPane.add(editableListPanel, BorderLayout.CENTER);
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        jp.add(checkBoxPane, BorderLayout.CENTER);
        jp.add(buttonPane);
        contentPane.add(jp, BorderLayout.PAGE_END);

        setMinimumSize(new Dimension(400, 500));
        //setSize( new Dimension( 400, 600 ) );

        //Initialize values.
        pack();
        if (owner != null) {
            setLocationRelativeTo(owner);
        }

        ItemListener itemListener = new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                int state = itemEvent.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    evalButton.setText("Stop evaluation");
                }
                else {
                    evalButton.setText(evaluateButtonText);
                }
            }
        };
        evalButton.addItemListener(itemListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Evaluate".equals(e.getActionCommand())) {
            if (isPressed) {
                //evalButton.setText("Stop evaluation!");
                isPressed = false;
                if (isThreadRunning) {
                    queryThread.stop();
                }
            }
            else {
                isPressed = true;
                editableListPanel.queryTextArea.setEnabled(false);
                //  evalButton.setText("Stop evaluation");
                queryThread = new Thread() {
                    public void run() {
                        runQuery();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                // evalButton.setText("Evaluate");
                                isThreadRunning = false;
                                evalButton.doClick();
                                isPressed = false;
                            }
                        });
                    }
                };
                isThreadRunning = true;
                queryThread.start();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        editableListPanel.queryTextArea.setEnabled(true);
                        editableListPanel.queryTextArea.requestFocusInWindow();
                    }
                });
            }
            evalButton.requestFocus();
        }
        else if ("Close".equals(e.getActionCommand())) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    evalButton.requestFocusInWindow();
                }

            });
            setVisible(false);
        }
        else {
            // BAD
        }
    }

    protected void runQuery() {
        Collection<Element> selectedElements = CollectionUtil.createNewSequence();
        if (diagramCB.isSelected()) {
            selectedElements.addAll(MDUtils.getSelectionInDiagram());
        }
        if (browserCB.isSelected()) {
            selectedElements.addAll(MDUtils.getSelectionInContainmentBrowser());
        }
        //selectedElements.add(null);
        //selectedElements = CollectionUtil.asSequence(selectedElements);
        //processor = new OclQueryAction.ProcessOclQuery(selectedElements);
        //processor = oclQueryProcessor;
        query = editableListPanel.getQuery();
        if (query != null) {
            Object result = null;
            if (objectRadioButton.isSelected()) {
                Object context = selectedElements;
                if (selectedElements.isEmpty()) {
                    context = null;
                }
                else if (selectedElements.size() == 1) {
                    context = selectedElements.iterator().next();
                }
                processor = new OclQueryAction.ProcessOclQuery(context);
                result = processor.process(query);
                String processedResult = processResults(result);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        editableListPanel.setResult(processedResult);
                        editableListPanel.setCompletions(processor.getCompletionChoices(),
                                ProcessOclQuery.toString(processor.getSourceOfCompletion())
                                        + " : "
                                        + ProcessOclQuery.getTypeName(processor.getSourceOfCompletion()));
                    }
                });
            }
            else {
                final List<Object> resultList = new ArrayList<Object>();
                final List<String> completionList = new ArrayList<String>();
                final List<Class<?>> classList = new ArrayList<Class<?>>();
                //editableListPanel.clearCompletions();
                for (final Object context : selectedElements) {
                    processor = new OclQueryAction.ProcessOclQuery(context);
                    result = processor.process(query);
                    String processedResult = processResults(result);
                    resultList.add(processedResult);

                    if (result != null && !classList.contains(result.getClass())) {
                        completionList.add(editableListPanel.getCompletionHeader(processor.getSourceOfCompletion()));
                        completionList.addAll(processor.getCompletionChoices());
                        classList.add(result.getClass());
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        editableListPanel.setResult(MoreToString.Helper.toString(resultList, false, true, null, null, "<ol><li>", "<li>", "</ol>", false));
                        editableListPanel.setCompletions(completionList,
                                ProcessOclQuery.toString(processor.getSourceOfCompletion())
                                        + " : " + ProcessOclQuery.getTypeName(processor.getSourceOfCompletion()));
                    }
                });
                //System.out.println("Completion List: " + completionList);
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    choices.push(query);
                    while (choices.size() > maxChoices) {
                        choices.pollLast();
                    }
                    editableListPanel.setItems(choices.toArray());
                }
            });
        }
        /*inputHistory.push(query);
        if (pastInputs.contains(query)) {
            choices.remove(query);
        } else {
            pastInputs.add(query);
        }
        choices.push(query);
        while (choices.size() > maxChoices) {
            choices.pollLast();
        }
        editableListPanel.setItems(choices.toArray());*/
    }

    private String processResults(Object oclObject) {
        if (oclObject == null) {
            return "null";
        }
        else if (oclObject instanceof org.eclipse.ocl.util.Bag<?>) {
            Object[] bag = ((org.eclipse.ocl.util.Bag<?>) oclObject).toArray();
            Set<Object> bagContents = new HashSet<>();
            List<String> bagStrings = new LinkedList<>();
            String s = "{";
            for (int i = 0; i < bag.length; i++) {
                if (bagContents.contains(bag[i])) {
                    continue;
                }
                bagContents.add(bag[i]);
                bagStrings.add(processResults(bag[i]) + "=" + ((org.eclipse.ocl.util.Bag<?>) oclObject).count(bag[i]));
            }
            Iterator<String> iter = bagStrings.iterator();
            while (iter.hasNext()) {
                String current = iter.next();
                s += current;
                if (iter.hasNext()) {
                    s += ", ";
                }
            }
            s += "}";
            return s;
        }
        else if (oclObject instanceof List<?>) {
            String s = "[";
            Iterator<?> iter = ((List<?>) oclObject).iterator();
            while (iter.hasNext()) {
                s += processResults(iter.next());
                if (iter.hasNext()) {
                    s += ", ";
                }
            }
            s += "]";
            return s;
        }
        else if (oclObject instanceof Set<?>) {
            String s = "[";
            Iterator<?> iter = ((Set<?>) oclObject).iterator();
            while (iter.hasNext()) {
                s += processResults(iter.next());
                if (iter.hasNext()) {
                    s += ", ";
                }
            }
            s += "]";
            return s;
        }
        else if (oclObject instanceof Element) {
            return ((Element) oclObject).getHumanName();
        }
        else if (oclObject instanceof String) {
            return (String) oclObject;
        }
        return oclObject.toString();
    }


    public static OclEvaluatorDialog getInstance() {
        return instance;
    }

    /**
     * @return the editableListPanel
     */
    public RepeatInputComboBoxDialog.EditableListPanel
    getEditableListPanel() {
        return editableListPanel;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        OclEvaluatorDialog dialog = new OclEvaluatorDialog(null, "testing");
        dialog.setVisible(true);
    }

}
