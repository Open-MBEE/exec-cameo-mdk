package gov.nasa.jpl.mbee.mdk.options;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.ProjectOptions;
import gov.nasa.jpl.mbee.mdk.MDKProjectOptionsConfigurator;
import gov.nasa.jpl.mbee.mdk.options.listener.MDKProjectEventListener;

public class ConfigureProjectOptions {

    public ConfigureProjectOptions() {
    }

    public void configure() {
        MDKProjectOptionsConfigurator mdkProjectOptionsConfigurator = new MDKProjectOptionsConfigurator();
        ProjectOptions.addConfigurator(mdkProjectOptionsConfigurator);
        Application.getInstance().addProjectEventListener(MDKProjectEventListener.getInstance());
    }
}
