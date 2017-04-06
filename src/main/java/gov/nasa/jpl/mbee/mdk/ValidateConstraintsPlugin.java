package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.NMAction;
import gov.nasa.jpl.mbee.mdk.actions.ValidateConstraints;

import java.lang.reflect.Method;

public class ValidateConstraintsPlugin extends MDPlugin {

    public ValidateConstraintsPlugin() {
        this(ValidateConstraints.class);
    }

    public ValidateConstraintsPlugin(Class<? extends NMAction> cls) {
        super(cls);
    }

    @Override
    public void initConfigurations() {
        Method method = getNmActionMethod();

        String category = "MDK";
        String diagramContext = "BaseDiagramContext";
        addConfiguration("MainMenu", "", ValidateConstraints.actionText, category, method, this);
        addConfiguration("ContainmentBrowserContext", "", ValidateConstraints.actionText, category, method,
                this);
        addConfiguration(diagramContext, "Class Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration(diagramContext, "Activity Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration(diagramContext, "SysML Block Definition Diagram",
                ValidateConstraints.actionText, category, method, this);
        addConfiguration(diagramContext, "SysML Internal Block Diagram",
                ValidateConstraints.actionText, category, method, this);
        addConfiguration(diagramContext, "DocGen 3 View Diagram", ValidateConstraints.actionText,
                category, method, this);
        addConfiguration(diagramContext, "DocGen 3 Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration(diagramContext, "View Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration(diagramContext, "DocumentView", ValidateConstraints.actionText, category,
                method, this);
    }

}
