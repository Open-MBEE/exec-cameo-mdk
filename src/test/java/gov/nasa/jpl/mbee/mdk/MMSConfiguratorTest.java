package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.NMAction;
import com.nomagic.actions.ActionsManager;
import gov.nasa.jpl.mbee.mdk.test.framework.ApplicationStartClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

//@RunWith(ApplicationStartClassRunner.class)
public class MMSConfiguratorTest {

    @Test
    public void configure() {
        ActionsManager actionsManager = new ActionsManager();
        new MMSConfigurator().configure(actionsManager);
        NMAction category = actionsManager.getActionFor("MMSMAIN");
            assertTrue("MMSSyncPlugin shall return a single instance.", category != null);
    }

}