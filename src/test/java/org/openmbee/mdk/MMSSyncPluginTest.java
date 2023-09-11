package org.openmbee.mdk;

import org.openmbee.mdk.test.framework.ApplicationStartClassRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationStartClassRunner.class)
public class MMSSyncPluginTest {

    private static MMSSyncPlugin plugin;

    @BeforeClass
    public static void setup() {
        plugin = MMSSyncPlugin.getInstance();
    }

    @Test
    public void initConfigurations() {
        assertTrue("MMSSyncPlugin shall be installed in MagicDraw.", plugin != null);
    }

    @Test
    public void getInstance() {
        assertTrue("MMSSyncPlugin shall return a single instance.", plugin != null && MMSSyncPlugin.getInstance() == plugin);
    }

    @Test
    public void getLocalDeltaProjectEventListenerAdapter() {
        assertTrue("MMSSyncPlugin shall return a LocalDeltaProjectEventListenerAdapter.", plugin != null && MMSSyncPlugin.getInstance().getLocalDeltaProjectEventListenerAdapter() != null);
    }

    @Test
    public void getMmsSyncProjectEventListenerAdapter() {
        assertTrue("MMSSyncPlugin shall return a MmsDeltaProjectEventListenerAdapter.", plugin != null && MMSSyncPlugin.getInstance().getMmsDeltaProjectEventListenerAdapter() != null);
    }

    @Test
    public void getDeltaSyncProjectEventListenerAdapter() {
        assertTrue("MMSSyncPlugin shall return a DeltaSyncProjectEventListenerAdapter.", plugin != null && MMSSyncPlugin.getInstance().getDeltaSyncProjectEventListenerAdapter() != null);
    }

    @Test
    public void getCoordinatedSyncProjectEventListenerAdapter() {
        assertTrue("MMSSyncPlugin shall return a CoordinatedSyncProjectEventListenerAdapter.", plugin != null && MMSSyncPlugin.getInstance().getCoordinatedSyncProjectEventListenerAdapter() != null);
    }

    @Test
    public void getSyncStatusProjectEventListenerAdapter() {
        assertTrue("MMSSyncPlugin shall return a SyncStatusProjectEventListenerAdapter.", plugin != null && MMSSyncPlugin.getInstance().getSyncStatusProjectEventListenerAdapter() != null);
    }

}
