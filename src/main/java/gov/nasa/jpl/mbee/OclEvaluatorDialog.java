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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JDialog;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 *
 */
public class OclEvaluatorDialog extends JDialog {

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
        init();
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog(Frame owner) {
        super(owner, false);
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog(Dialog owner) {
        super(owner, false);
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog(Window owner) {
        super(owner, ModalityType.MODELESS);
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog(Frame owner, String title) {
        super(owner, title, false);
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog(Dialog owner, String title) {
        super(owner, title, false);
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog(Window owner, String title) {
        super(owner, title, ModalityType.MODELESS);
        init();
        // TODO Auto-generated constructor stub
    }

    protected void init() {
        editableListPanel = new RepeatInputComboBoxDialog.EditableListPanel("Enter an OCL expression:",
                choices.toArray());
        Collection<Element> selectedElements = MDUtils.getSelection(null, Configurator.lastContextIsDiagram);
        processor = new OclQueryAction.ProcessOclQuery(selectedElements);
        if (lastInput != null) {
            Object result = processor.process(lastInput);
            editableListPanel.setResultPanel(result);
        }
        Debug.outln("lastInput = " + lastInput);

        editableListPanel.setVisible(true);
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
