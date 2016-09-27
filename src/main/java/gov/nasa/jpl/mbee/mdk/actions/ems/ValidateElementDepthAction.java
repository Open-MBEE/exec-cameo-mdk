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
package gov.nasa.jpl.mbee.mdk.actions.ems;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.sync.manual.ManualSyncRunner;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

public class ValidateElementDepthAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Collection<Element> start;
    public static final String actionid = "ValidateElementDepth";
    private int depth = -2;
    private boolean cancel = false;

    public ValidateElementDepthAction(Element e, String name, int depth) {
        super(actionid, name, null, null);
        start = new ArrayList<Element>();
        start.add(e);
    }

    public ValidateElementDepthAction(Collection<Element> e, String name, int depth) {
        super(actionid, name, null, null);
        start = e;
    }

    public ValidateElementDepthAction(Element e, String name) {
        this(e, name, 1);
    }

    public ValidateElementDepthAction(Collection<Element> e, String name) {
        this(e, name, 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = "Choose a depth of containment to use for finding elements to validate.\n\nThis MUST be a non-negative integer, otherwise it will default to 1.\n";
        String message1 = "Nice try! To do a recursive validation, cancel this and use Validate Models.\n\nOr you can input a non-negative integer and continue.\n";
        String message2 = "You didn't input a non-negative integer!\n\nInput a non-negative integer this time or it will default to depth = 1.\n";
        String title = "Choose Depth";
        try {
            String input = (String) JOptionPane.showInputDialog(Application.getInstance().getMainFrame(), message, title, JOptionPane.INFORMATION_MESSAGE, null, null, 1);
            if (input == null) {
                cancel = true;
            }
            depth = Integer.parseInt(input);
        } catch (Exception ee) {
        }
        if (depth == -1 && !cancel) {
            try {
                String input = (String) JOptionPane.showInputDialog(Application.getInstance().getMainFrame(), message1, title, JOptionPane.WARNING_MESSAGE, null, null, 1);
                if (input == null) {
                    cancel = true;
                }
                depth = Integer.parseInt(input);
            } catch (Exception ee) {
            }
        }
        else if (depth < 0 && !cancel) {
            try {
                String input = (String) JOptionPane.showInputDialog(Application.getInstance().getMainFrame(), message2, title, JOptionPane.WARNING_MESSAGE, null, null, 1);
                if (input == null) {
                    cancel = true;
                }
                depth = Integer.parseInt(input);
            } catch (Exception ee) {
            }
        }
        if (depth < 0 && !cancel) {
            depth = 1;
            Application.getInstance().getGUILog().log("[WARN] Validate Models: Using a depth of 1 since the provided depth was not valid.");
        }

        if (!cancel) {
            ProgressStatusRunner.runWithProgressStatus(new ManualSyncRunner(start, Application.getInstance().getProject(), false, depth), "Manual Sync (depth: " + Integer.toString(depth) + ")", true, 0);
        }
        else {
            Application.getInstance().getGUILog().log("[INFO] Cancel pressed!!! Stopping validate.");
        }
    }
}
