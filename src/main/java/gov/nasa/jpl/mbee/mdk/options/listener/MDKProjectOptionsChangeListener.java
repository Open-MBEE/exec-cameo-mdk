package gov.nasa.jpl.mbee.mdk.options.listener;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import gov.nasa.jpl.mbee.mdk.options.MDKProjectOptions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MDKProjectOptionsChangeListener implements PropertyChangeListener {

    private static MDKProjectOptionsChangeListener INSTANCE;

    public MDKProjectOptionsChangeListener() {
    }

    public static MDKProjectOptionsChangeListener getInstance() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new MDKProjectOptionsChangeListener();
            }
        } catch (IllegalStateException var0) {
            throw InstanceNotFound(var0);
        }

        return INSTANCE;
    }


    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(MDKProjectOptions.GROUP)) {
            Project project = Application.getInstance().getProject();

            if (MDKProjectOptions.getMbeeEnabled(project)) {
                MDKProjectOptions.validate(project);
            }

        }
    }


    private static IllegalStateException InstanceNotFound(IllegalStateException var0) {
        return var0;
    }

}