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

import gov.nasa.jpl.mbee.RepeatInputComboBoxDialog.Processor;
import gov.nasa.jpl.mbee.actions.OclQueryAction;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 *
 */
public class OclEvaluatorDialog extends JDialog implements ActionListener {

    private static final long                                    serialVersionUID  = -9114812582757129836L;

    private static OclEvaluatorDialog                            instance          = null;
    // members for tracking input history
    protected static Object                                      lastInput         = null;
    protected static LinkedList<Object>                          inputHistory      = new LinkedList<Object>();
    protected static HashSet<Object>                             pastInputs        = new HashSet<Object>();
    protected static LinkedList<Object>                          choices           = new LinkedList<Object>();
    protected static int                                         maxChoices        = 20;

    /**
     * callback for processing input
     */
    protected Processor                                          processor;

    protected static RepeatInputComboBoxDialog.EditableListPanel editableListPanel = null;

    protected boolean                                            cancelSelected    = false;

    /**
     * 
     */
    public OclEvaluatorDialog() {
        super();
        init(null);
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog(Frame owner) {
        super(owner, false);
        init(owner);
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog(Dialog owner) {
        super(owner, false);
        init(owner);
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog(Window owner) {
        super(owner, ModalityType.MODELESS);
        init(owner);
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog(Frame owner, String title) {
        super(owner, title, false);
        init(owner);
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog(Dialog owner, String title) {
        super(owner, title, false);
        init(owner);
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog(Window owner, String title) {
        super(owner, title, ModalityType.MODELESS);
        init(owner);
        // TODO Auto-generated constructor stub
    }

    protected void init( Window owner ) {
        editableListPanel = new RepeatInputComboBoxDialog.EditableListPanel("Enter an OCL expression:",
                choices.toArray());

        editableListPanel.setVisible(true);
        
        //Create and initialize the buttons.
        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand("Close");
        closeButton.addActionListener(this);
        //
        final JButton evalButton = new JButton("Evaluate");
        evalButton.setActionCommand("Evaluate");
        evalButton.addActionListener(this);
        getRootPane().setDefaultButton(evalButton);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(closeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(evalButton);
        
        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(editableListPanel, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
 
        //Initialize values.
        pack();
        if ( owner != null ) setLocationRelativeTo( owner );
        setVisible( true );
    }

    protected void runQuery() {
        Collection<Element> selectedElements = MDUtils.getSelection(null, Configurator.lastContextIsDiagram);
        processor = new OclQueryAction.ProcessOclQuery(selectedElements);
        lastInput = RepeatInputComboBoxDialog.getSelectedItem( editableListPanel );
        if (lastInput != null) {
            Object result = processor.process(lastInput);
            editableListPanel.setResultPanel(result);
        }
        inputHistory.push(lastInput);
        if (pastInputs.contains(lastInput)) {
            choices.remove(lastInput);
        } else {
            pastInputs.add(lastInput);
        }
        choices.push(lastInput);
        while (choices.size() > maxChoices) {
            choices.pollLast();
        }
        editableListPanel.setItems(choices.toArray());
    }
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        System.out.println("EVALUATE!");
        if ("Evaluate".equals(e.getActionCommand())) {
            runQuery();
        } else if ("Close".equals(e.getActionCommand())) {
            setVisible( false );
        } else {
            // BAD
        }
    }

    public static OclEvaluatorDialog getInstance() {
        return instance;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        OclEvaluatorDialog dialog = new OclEvaluatorDialog();
        dialog.setVisible(true);
    }

}
