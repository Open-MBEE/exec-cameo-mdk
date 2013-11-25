package gov.nasa.jpl.mbee.web.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;

/**
 * Subscribes event listeners to support DocGen/DocWeb sync features. Must be
 * initialized from the plugin initializer: <code>
 *     ApplicationSyncEventSubscriber.subscribe();
 * </code>
 */
public class ApplicationSyncEventSubscriber extends ProjectEventListenerAdapter {

    /** Makes sure application event listeners are only set up once. */
    private static final AtomicBoolean                           subscribed   = new AtomicBoolean(false);

    /** Keeps track of project-specific event subscribers */
    private static final Map<String, ProjectSyncEventSubscriber> projects     = new ConcurrentHashMap<String, ProjectSyncEventSubscriber>();
    private static final Map<String, Boolean>                    commitStates = new ConcurrentHashMap<String, Boolean>();

    /**
     * Sets up listeners, must be called from plugin initializer.
     */
    public static void subscribe() {
        // make sure we don't try subscribing twice within an application
        if (!subscribed.compareAndSet(false, true)) {
            return;
        }

        Application.getInstance().getProjectsManager()
                .addProjectListener(new ApplicationSyncEventSubscriber());
    }

    @Override
    public void projectOpened(Project project) {
        ProjectSyncEventSubscriber p = new ProjectSyncEventSubscriber(project);
        p.subscribe();
        projects.put(project.getID(), p);
        commitStates.put(project.getID(), Boolean.TRUE);
    }

    @Override
    public void projectClosed(Project project) {
        commitStates.remove(project.getID());
        ProjectSyncEventSubscriber p = projects.remove(project.getID());
        if (p != null) {
            p.unsubscribe();
        }
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        if (savedInServer || !project.isRemote()) {
            commitStates.put(project.getID(), Boolean.TRUE);
        }
    }

    public static boolean isCommitted(Project project) {
        if (project.isDirty()) {
            return false; // this case is unambiguous
        }

        // See if project is saved locally, but not committed to the server.
        Boolean committed = commitStates.get(project.getID());
        if (committed == null) { // shouldn't happen
            return false;
        }
        return committed;
    }
}
