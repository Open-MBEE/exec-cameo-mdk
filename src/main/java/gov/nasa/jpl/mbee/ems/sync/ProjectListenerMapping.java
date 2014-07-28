package gov.nasa.jpl.mbee.ems.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nomagic.magicdraw.core.Project;

public class ProjectListenerMapping extends ConcurrentHashMap<Project, Map<String, Object>>{

    private final static ProjectListenerMapping instance = new ProjectListenerMapping();
    
    private ProjectListenerMapping() {
        super();
    }
    
    public static ProjectListenerMapping getInstance() {
        return instance;
    }
}
