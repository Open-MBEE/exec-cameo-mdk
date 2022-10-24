package gov.nasa.jpl.mbee.mdk.options;

import com.nomagic.magicdraw.core.Application;

public class ConfigureEnvironmentOptions {

    public ConfigureEnvironmentOptions() {}

    public void configure() {
        Application.getInstance().getEnvironmentOptions().addGroup(MDKEnvironmentOptionsGroup.getInstance());
    }
}
