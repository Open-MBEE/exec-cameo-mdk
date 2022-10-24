package gov.nasa.jpl.mbee.mdk;

import com.nomagic.magicdraw.core.options.ProjectOptions;
import com.nomagic.magicdraw.core.options.ProjectOptionsConfigurator;
import gov.nasa.jpl.mbee.mdk.options.MDKProjectOptions;
import gov.nasa.jpl.mbee.mdk.options.listener.MDKProjectOptionsChangeListener;
import gov.nasa.jpl.mbee.mdk.options.listener.MDKProjectPartLoadedListener;

public class MDKProjectOptionsConfigurator implements ProjectOptionsConfigurator {

    public MDKProjectOptionsConfigurator() {
    }

    public void afterLoad(ProjectOptions projectOptions) {

    }

    public void configure(ProjectOptions projectOptions) {
        MDKProjectOptions.init(projectOptions);
        MDKProjectPartLoadedListener.getInstance();
        MDKProjectOptionsChangeListener.getInstance();

    }
}
