package org.openmbee.mdk.options;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.ProjectOptions;
import org.openmbee.mdk.MDKProjectOptionsConfigurator;
import org.openmbee.mdk.options.listener.MDKProjectEventListener;

public class ConfigureProjectOptions {

    public ConfigureProjectOptions() {
    }

    public void configure() {
        MDKProjectOptionsConfigurator mdkProjectOptionsConfigurator = new MDKProjectOptionsConfigurator();
        ProjectOptions.addConfigurator(mdkProjectOptionsConfigurator);
        Application.getInstance().addProjectEventListener(MDKProjectEventListener.getInstance());
    }
}
