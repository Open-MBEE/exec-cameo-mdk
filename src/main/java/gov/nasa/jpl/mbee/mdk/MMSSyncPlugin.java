package gov.nasa.jpl.mbee.mdk;

import com.nomagic.magicdraw.cookies.CloseCookie;
import com.nomagic.magicdraw.cookies.CookieSet;
import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.mdk.mms.sync.coordinated.CoordinatedSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.DeltaSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.queue.OutputQueueCloseCookie;
import gov.nasa.jpl.mbee.mdk.mms.sync.status.SyncStatusProjectEventListenerAdapter;

/*
 * This class is responsible for performing automatic syncs with
 * MMS whenever any type of commit is executed.
 * This class is also responsible for start the REST web services.
 */
public class MMSSyncPlugin extends MDPlugin {
    private static MMSSyncPlugin instance;
    private LocalSyncProjectEventListenerAdapter localSyncProjectEventListenerAdapter;
    private JMSSyncProjectEventListenerAdapter jmsSyncProjectEventListenerAdapter;
    private DeltaSyncProjectEventListenerAdapter deltaSyncProjectEventListenerAdapter;
    private CoordinatedSyncProjectEventListenerAdapter coordinatedSyncProjectEventListenerAdapter;
    private SyncStatusProjectEventListenerAdapter syncStatusProjectEventListenerAdapter;

    public static MMSSyncPlugin getInstance() {
        if (instance == null) {
            instance = new MMSSyncPlugin();
        }
        return instance;
    }

    public LocalSyncProjectEventListenerAdapter getLocalSyncProjectEventListenerAdapter() {
        return localSyncProjectEventListenerAdapter;
    }

    public JMSSyncProjectEventListenerAdapter getJmsSyncProjectEventListenerAdapter() {
        return jmsSyncProjectEventListenerAdapter;
    }

    public DeltaSyncProjectEventListenerAdapter getDeltaSyncProjectEventListenerAdapter() {
        return deltaSyncProjectEventListenerAdapter;
    }

    public CoordinatedSyncProjectEventListenerAdapter getCoordinatedSyncProjectEventListenerAdapter() {
        return coordinatedSyncProjectEventListenerAdapter;
    }

    public SyncStatusProjectEventListenerAdapter getSyncStatusProjectEventListenerAdapter() {

        return syncStatusProjectEventListenerAdapter;
    }

    @Override
    public void initConfigurations() {
        System.out.println("Initializing MMSSyncPlugin.");
        // Order matters!
        Application.getInstance().getProjectsManager().addProjectListener(coordinatedSyncProjectEventListenerAdapter = new CoordinatedSyncProjectEventListenerAdapter());
        Application.getInstance().getProjectsManager().addProjectListener(deltaSyncProjectEventListenerAdapter = new DeltaSyncProjectEventListenerAdapter());
        // Common and MMS clear their respective inMemoryChangelogs on save, so it needs to go after coordinated and delta which use it.
        Application.getInstance().getProjectsManager().addProjectListener(localSyncProjectEventListenerAdapter = new LocalSyncProjectEventListenerAdapter());
        Application.getInstance().getProjectsManager().addProjectListener(jmsSyncProjectEventListenerAdapter = new JMSSyncProjectEventListenerAdapter());
        Application.getInstance().getProjectsManager().addProjectListener(syncStatusProjectEventListenerAdapter = new SyncStatusProjectEventListenerAdapter());

        CookieSet cookieSet = Application.getInstance().getCookieSet();
        CloseCookie closeCookie = (CloseCookie) cookieSet.getCookie(CloseCookie.class);
        if (closeCookie != null) {
            cookieSet.remove(closeCookie);
        }
        cookieSet.add(new OutputQueueCloseCookie(closeCookie));
    }

    public boolean isSupported() {
        return true;
    }
}
