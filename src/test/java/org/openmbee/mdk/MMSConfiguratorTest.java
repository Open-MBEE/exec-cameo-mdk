package org.openmbee.mdk;

import com.nomagic.actions.NMAction;
import com.nomagic.actions.ActionsManager;
import org.junit.Test;

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
