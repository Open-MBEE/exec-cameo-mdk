package gov.nasa.jpl.mbee.ems.sync.common;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.uml.transaction.MDTransactionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/28/16.
 */
public class CommonSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<String, CommonSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();

    @Override
    public void projectOpened(Project project) {
        projectClosed(project);
        CommonSyncTransactionCommitListener listener = new CommonSyncTransactionCommitListener();
        ((MDTransactionManager) project.getRepository().getTransactionManager()).addTransactionCommitListenerIncludingUndoAndRedo(listener);
        getProjectMapping(project).setCommonSyncTransactionCommitListener(listener);
    }

    @Override
    public void projectClosed(Project project) {
        CommonSyncProjectMapping commonSyncProjectMapping = getProjectMapping(project);
        if (commonSyncProjectMapping.getCommonSyncTransactionCommitListener() != null) {
            project.getRepository().getTransactionManager().removeTransactionCommitListener(commonSyncProjectMapping.getCommonSyncTransactionCommitListener());
        }
        projectMappings.remove(project.getID());
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        CommonSyncProjectMapping commonSyncProjectMapping = CommonSyncProjectEventListenerAdapter.getProjectMapping(project);

        CommonSyncTransactionCommitListener commonSyncTransactionCommitListener = commonSyncProjectMapping.getCommonSyncTransactionCommitListener();
        if (commonSyncTransactionCommitListener != null) {
            commonSyncTransactionCommitListener.getInMemoryLocalChangelog().clear();
        }
    }

    public static CommonSyncProjectMapping getProjectMapping(Project project) {
        CommonSyncProjectMapping commonSyncProjectMapping = projectMappings.get(project.getID());
        if (commonSyncProjectMapping == null) {
            projectMappings.put(project.getID(), commonSyncProjectMapping = new CommonSyncProjectMapping());
        }
        return commonSyncProjectMapping;
    }

    public static class CommonSyncProjectMapping {
        private CommonSyncTransactionCommitListener commonSyncTransactionCommitListener;

        public CommonSyncTransactionCommitListener getCommonSyncTransactionCommitListener() {
            return commonSyncTransactionCommitListener;
        }

        public void setCommonSyncTransactionCommitListener(CommonSyncTransactionCommitListener commonSyncTransactionCommitListener) {
            this.commonSyncTransactionCommitListener = commonSyncTransactionCommitListener;
        }
    }
}
