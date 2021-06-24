package gov.nasa.jpl.mbee.mdk.tickets;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

public class AuthenticationChainError extends AbstractAcquireTicketProcessor {
    public AuthenticationChainError(AbstractAcquireTicketProcessor processor) {
        super(processor);
    }

    @Override
    public boolean acquireMmsTicket(Project project) {
        Application.getInstance().getGUILog().log("[ERROR] Unable to login to MMS with provided credentials.");
        return false;
    }
}