package gov.nasa.jpl.mbee.mdk.tickets;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.options.MDKProjectOptions;

public abstract class AbstractAcquireTicketProcessor {
    private AbstractAcquireTicketProcessor processor;

    public AbstractAcquireTicketProcessor(AbstractAcquireTicketProcessor processor) {
        this.processor = processor;
    }

    public boolean acquireMmsTicket(Project project) {
        if (MDKProjectOptions.getMmsUrl(project) == null) {
            Application.getInstance().getGUILog().log("[ERROR] MMS url is not specified. Skipping login.");
            return false;
        }
        if (processor != null) {
            return processor.acquireMmsTicket(project);
        }
        return false;
    }

    public void reset() {
        if (processor != null) {
            processor.reset();
        }
    }
}