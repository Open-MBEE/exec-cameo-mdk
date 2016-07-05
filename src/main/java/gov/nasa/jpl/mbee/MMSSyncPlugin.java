package gov.nasa.jpl.mbee;

import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.ems.sync.delta.DeltaSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncProjectEventListenerAdapter;

/*
 * This class is responsible for performing automatic syncs with
 * MMS whenever any type of commit is executed.
 * This class is also responsible for start the REST webservices.
 */
public class MMSSyncPlugin extends MDPlugin {
    private static MMSSyncPlugin instance;
    private CommonSyncProjectEventListenerAdapter commonSyncProjectEventListenerAdapter;
    private DeltaSyncProjectEventListenerAdapter deltaSyncProjectEventListenerAdapter;

    public static MMSSyncPlugin getInstance() {
        if (instance == null) {
            instance = new MMSSyncPlugin();
        }
        return instance;
    }

    public CommonSyncProjectEventListenerAdapter getCommonSyncProjectEventListenerAdapter() {
        return commonSyncProjectEventListenerAdapter;
    }

    public DeltaSyncProjectEventListenerAdapter getDeltaSyncProjectEventListenerAdapter() {
        return deltaSyncProjectEventListenerAdapter;
    }

    @Override
    public void initConfigurations() {
        System.out.println("Initializing MMSSyncPlugin.");
        Application.getInstance().getProjectsManager().addProjectListener(commonSyncProjectEventListenerAdapter = new CommonSyncProjectEventListenerAdapter());
        Application.getInstance().getProjectsManager().addProjectListener(deltaSyncProjectEventListenerAdapter = new DeltaSyncProjectEventListenerAdapter());
    }

    public boolean isSupported() {
        return true;
    }
}
