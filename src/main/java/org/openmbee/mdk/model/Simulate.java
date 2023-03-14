package org.openmbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.simulation.SimulationManager;
import com.nomagic.magicdraw.simulation.execution.SimulationResult;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.docgen.docbook.DocumentElement;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

/**
 * Created by igomes on 12/2/16.
 */
public class Simulate extends Query {
    // TODO pull default from profile
    private Integer timeout = 60;

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        super.visit(forViewEditor, outputDir);
        Project project = Application.getInstance().getProject();
        for (Object o : getTargets()) {
            if (o instanceof Element) {
                long startTime = System.currentTimeMillis();
                Application.getInstance().getGUILog().log("[INFO] Simulation of " + ((Element) o).getHumanName() + " started.");
                SimulationResult simulationResult;
                try {
                    simulationResult = SimulationManager.execute((Element) o, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Application.getInstance().getGUILog().log("[ERROR] Simulation of " + ((Element) o).getHumanName() + " encountered an unexpected exception: \"" + e.getMessage() + ".\" Terminating.");
                    if (SessionManager.getInstance().isSessionCreated(project)) {
                        SessionManager.getInstance().cancelSession(project);
                    }
                    continue;
                }
                while (!simulationResult.getMainSession().isClosed() && (timeout < 0 || System.currentTimeMillis() - startTime < timeout * 1000)) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!simulationResult.getMainSession().isClosed()) {
                    Application.getInstance().getGUILog().log("[WARNING] Simulation of " + ((Element) o).getHumanName() + " timed out after " + NumberFormat.getInstance().format(timeout) + " seconds. Terminating.");
                    SimulationManager.terminateSession(simulationResult.getMainSession());
                    if (SessionManager.getInstance().isSessionCreated(project)) {
                        SessionManager.getInstance().cancelSession(project);
                    }
                }
                else {
                    Application.getInstance().getGUILog().log("[INFO] Simulation of " + ((Element) o).getHumanName() + " completed.");
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void initialize() {
        super.initialize();
        Object o = StereotypesHelper.getStereotypePropertyFirst(dgElement, profile.simulate().getTimeoutProperty());
        if (o != null && o instanceof Integer) {
            timeout = (Integer) o;
        }
    }
}
