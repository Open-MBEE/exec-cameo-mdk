package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.mms.sync.manual.ManualSyncRunner;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

public class ValidateElementDepthAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Collection<Element> start;
    private Project project;
    public static final String DEFAULT_ID = "ValidateElementDepth";
    private int depth = -2;
    private boolean cancel = false;

    public ValidateElementDepthAction(Element e, String name, int depth) {
        super(DEFAULT_ID, name, null, null);
        this.start = new ArrayList<Element>();
        this.start.add(e);
        this.project = Project.getProject(e);
    }

    public ValidateElementDepthAction(Collection<Element> e, String name, int depth) {
        super(DEFAULT_ID, name, null, null);
        this.start = e;
        this.project = Project.getProject(e.iterator().next());
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
            ManualSyncRunner manualSyncRunner = new ManualSyncRunner(start, Application.getInstance().getProject(), depth);
            ProgressStatusRunner.runWithProgressStatus(manualSyncRunner, "Manual Sync (depth: " + Integer.toString(depth) + ")", true, 0);
            if (manualSyncRunner.getValidationSuite() != null && manualSyncRunner.getValidationSuite().hasErrors()) {
                Utils.displayValidationWindow(project, manualSyncRunner.getValidationSuite(), manualSyncRunner.getValidationSuite().getName());
            }
            else {
                Application.getInstance().getGUILog().log("[INFO] All validated elements are equivalent.");
            }
        }
        else {
            Application.getInstance().getGUILog().log("[INFO] Cancel pressed. Aborting validation.");
        }
    }
}
