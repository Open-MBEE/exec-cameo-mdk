package gov.nasa.jpl.mbee;

import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.ems.sync.delta.DeltaSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.realtime.RealTimeSyncProjectEventListenerAdapter;

/*
 * This class is responsible for performing automatic syncs with
 * MMS whenever any type of commit is executed.
 * This class is also responsible for start the REST webservices.
 */
public class MMSSyncPlugin extends MDPlugin {
    private static MMSSyncPlugin instance;
    private CommonSyncProjectEventListenerAdapter commonSyncProjectEventListenerAdapter;
    private JMSSyncProjectEventListenerAdapter jmsSyncProjectEventListenerAdapter;
    private DeltaSyncProjectEventListenerAdapter deltaSyncProjectEventListenerAdapter;
    private RealTimeSyncProjectEventListenerAdapter realTimeSyncProjectEventListenerAdapter;

    public static MMSSyncPlugin getInstance() {
        if (instance == null) {
            instance = new MMSSyncPlugin();
        }
        return instance;
    }

    public CommonSyncProjectEventListenerAdapter getCommonSyncProjectEventListenerAdapter() {
        return commonSyncProjectEventListenerAdapter;
    }

    public JMSSyncProjectEventListenerAdapter getJmsSyncProjectEventListenerAdapter() {
        return jmsSyncProjectEventListenerAdapter;
    }

    public DeltaSyncProjectEventListenerAdapter getDeltaSyncProjectEventListenerAdapter() {
        return deltaSyncProjectEventListenerAdapter;
    }

    public RealTimeSyncProjectEventListenerAdapter getRealTimeSyncProjectEventListenerAdapter() {
        return realTimeSyncProjectEventListenerAdapter;
    }

    @Override
    public void initConfigurations() {
        System.out.println("Initializing MMSSyncPlugin.");
        // Order matters!
        Application.getInstance().getProjectsManager().addProjectListener(realTimeSyncProjectEventListenerAdapter = new RealTimeSyncProjectEventListenerAdapter());
        Application.getInstance().getProjectsManager().addProjectListener(deltaSyncProjectEventListenerAdapter = new DeltaSyncProjectEventListenerAdapter());
        // Common and JMS clear their respective inMemoryChangelogs on save, so it needs to go after realtime and delta which use it.
        Application.getInstance().getProjectsManager().addProjectListener(commonSyncProjectEventListenerAdapter = new CommonSyncProjectEventListenerAdapter());
        Application.getInstance().getProjectsManager().addProjectListener(jmsSyncProjectEventListenerAdapter = new JMSSyncProjectEventListenerAdapter());
    }

    public boolean isSupported() {
        return true;
    }
}
