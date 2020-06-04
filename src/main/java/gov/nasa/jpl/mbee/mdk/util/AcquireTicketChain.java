package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Project;

public class AcquireTicketChain {
    AbstractAcquireTicketProcessor chain;

    public AcquireTicketChain() {
        buildChain();
    }

    private void buildChain() {
        chain = new TWCUtils(new TicketUtils(null));
    }

    public boolean acquireMmsTicket(Project project) {
        return chain.acquireMmsTicket(project);
    }
}