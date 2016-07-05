package gov.nasa.jpl.mbee;

import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.ems.sync.realtime.RealTimeSyncProjectEventListenerAdapter;

/*
 * MAGICDRAW-212
 */
public class RealTimeSyncPlugin extends MDPlugin {
    private static RealTimeSyncPlugin instance;

    public static RealTimeSyncPlugin getInstance() {
        if (instance == null) {
            instance = new RealTimeSyncPlugin();
        }
        return instance;
    }

    @Override
    public void initConfigurations() {
        Application.getInstance().getProjectsManager().addProjectListener(new RealTimeSyncProjectEventListenerAdapter());
    }
}
