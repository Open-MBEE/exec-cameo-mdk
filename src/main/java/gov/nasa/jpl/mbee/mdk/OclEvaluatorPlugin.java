package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.NMAction;
import gov.nasa.jpl.mbee.mdk.ocl.actions.OclQueryAction;

import java.lang.reflect.Method;

public class OclEvaluatorPlugin extends MDPlugin {

    public OclEvaluatorPlugin() {
        this(OclQueryAction.class);
    }

    public OclEvaluatorPlugin(Class<? extends NMAction> cls) {
        super(cls);
    }

    @Override
    public void initConfigurations() {
        Method method = getNmActionMethod();

        String category = "MDK";
        String diagramContext = "BaseDiagramContext";
        addConfiguration("MainMenu", "", OclQueryAction.actionText, category, method, this);
        addConfiguration("ContainmentBrowserContext", "", OclQueryAction.actionText, category, method, this);
        addConfiguration(diagramContext, "Class Diagram", OclQueryAction.actionText, category, method,
                this);
        addConfiguration(diagramContext, "Activity Diagram", OclQueryAction.actionText, category,
                method, this);
        addConfiguration(diagramContext, "SysML Block Definition Diagram", OclQueryAction.actionText,
                category, method, this);
        addConfiguration(diagramContext, "SysML Internal Block Diagram", OclQueryAction.actionText,
                category, method, this);
        addConfiguration(diagramContext, "DocGen 3 View Diagram", OclQueryAction.actionText, category,
                method, this);
        addConfiguration(diagramContext, "DocGen 3 Diagram", OclQueryAction.actionText, category,
                method, this);
        addConfiguration(diagramContext, "View Diagram", OclQueryAction.actionText, category, method,
                this);
        addConfiguration(diagramContext, "Viewpoint Method Diagram", OclQueryAction.actionText, category, method,
                this);
        addConfiguration(diagramContext, "DocumentView", OclQueryAction.actionText, category, method,
                this);
    }

}
