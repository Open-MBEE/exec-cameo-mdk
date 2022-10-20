package gov.nasa.jpl.mbee.mdk.options.listener;

import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilitiesInternal;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.core.project.ProjectPartLoadedListener;
import gov.nasa.jpl.mbee.mdk.options.MDKProjectOptions;

public class MDKProjectEventListener extends ProjectEventListenerAdapter implements ProjectPartLoadedListener {

    private static MDKProjectEventListener INSTANCE;

    private final MDKProjectPartLoadedListener partLoadedListener;
    private final MDKProjectOptionsChangeListener projectOptionsChangeListener;

    public MDKProjectEventListener() {
        this.projectOptionsChangeListener = MDKProjectOptionsChangeListener.getInstance();
        this.partLoadedListener = MDKProjectPartLoadedListener.getInstance();
    }

    public static MDKProjectEventListener getInstance() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new MDKProjectEventListener();
            }
        } catch (IllegalStateException var0) {
            throw InstanceNotFound(var0);
        }
        return INSTANCE;
    }

    public void projectOpened(Project var1) {
        var1.getOptions().addPropertyChangeListener(this.projectOptionsChangeListener);
        MDKProjectOptions.validate(var1);
    }

    public void projectPreClosed(Project var1) {
        var1.getOptions().removePropertyChangeListener(this.projectOptionsChangeListener);
    }

    public void projectPartLoaded(Project var1, IProject var2) {
        MDKProjectPartLoadedListener.listenToProjectLoad(ProjectUtilitiesInternal.getRepresentation(var2));
    }

    private static IllegalStateException InstanceNotFound(IllegalStateException var0) {
        return var0;
    }

}