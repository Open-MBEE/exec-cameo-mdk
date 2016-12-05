package gov.nasa.jpl.mbee.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.simulation.SimulationManager;
import com.nomagic.magicdraw.simulation.execution.session.SimulationSession;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

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
        for (Object o : getTargets()) {
            if (o instanceof Element) {
                long startTime = System.currentTimeMillis();
                Application.getInstance().getGUILog().log("[INFO] Simulation of " + ((Element) o).getHumanName() + " started.");
                SimulationSession simulationSession = SimulationManager.execute((Element) o, true, true);
                while (!simulationSession.isClosed() && (timeout < 0 || System.currentTimeMillis() - startTime < timeout * 1000)) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!simulationSession.isClosed()) {
                    Application.getInstance().getGUILog().log("[WARNING] Simulation of " + ((Element) o).getHumanName() + " timed out after " + NumberFormat.getInstance().format(timeout) + " seconds. Terminating.");
                    SimulationManager.terminateSession(simulationSession);
                    if (SessionManager.getInstance().isSessionCreated()) {
                        SessionManager.getInstance().cancelSession();
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
        Object o = StereotypesHelper.getStereotypePropertyFirst(dgElement, DocGen3Profile.simulateStereotype, "timeout");
        if (o != null && o instanceof Integer) {
            timeout = (Integer) o;
        }
    }
}
