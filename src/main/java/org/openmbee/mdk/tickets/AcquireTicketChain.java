package org.openmbee.mdk.tickets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.nomagic.magicdraw.core.Project;

import org.openmbee.mdk.options.MDKEnvironmentOptionsGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcquireTicketChain {
    AbstractAcquireTicketProcessor chain;
    private static final Logger logger = LoggerFactory.getLogger(AcquireTicketChain.class);

    public AcquireTicketChain() {
        buildChain();
    }

    private AbstractAcquireTicketProcessor recursiveBuildChain(List<String> authChain) throws NoSuchMethodException,
            ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (authChain.size() > 1) {
            Constructor c = Class.forName(authChain.get(0)).getConstructor(AbstractAcquireTicketProcessor.class);
            authChain.remove(0);
            return (AbstractAcquireTicketProcessor) c.newInstance(recursiveBuildChain(authChain));
        } else {
            return new AuthenticationChainError(null);
        }
    }

    private void buildChain() {
        List<String> authChain = MDKEnvironmentOptionsGroup.getInstance().getAuthenticationChain();
        try {
            chain = recursiveBuildChain(authChain);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Error in creating authentication chain" + e.getMessage());
        }
    }

    public boolean acquireMmsTicket(Project project) {
        return chain.acquireMmsTicket(project);
    }

    public void reset() {
        chain.reset();
    }
}
