package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;

abstract class AcquireTicketProcessor {
    private AcquireTicketProcessor processor;

    public AcquireTicketProcessor(AcquireTicketProcessor processor) {
        this.processor = processor;
    }

    public boolean acquireMmsTicket(Project project) {
        if (MMSUtils.getServerUrl(project) == null) {
            Application.getInstance().getGUILog().log("[ERROR] MMS url is not specified. Skipping login.");
            return false;
        }
        if (processor != null) {
            return processor.acquireMmsTicket(project);
        }
        return false;
    }
}