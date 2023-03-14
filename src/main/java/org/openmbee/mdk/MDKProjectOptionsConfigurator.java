package org.openmbee.mdk;

import com.nomagic.magicdraw.core.options.ProjectOptions;
import com.nomagic.magicdraw.core.options.ProjectOptionsConfigurator;
import org.openmbee.mdk.options.MDKProjectOptions;
import org.openmbee.mdk.options.listener.MDKProjectOptionsChangeListener;
import org.openmbee.mdk.options.listener.MDKProjectPartLoadedListener;

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
