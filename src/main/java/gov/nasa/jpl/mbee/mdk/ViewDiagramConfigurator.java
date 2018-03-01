package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.TargetElementAMConfigurator;
import com.nomagic.magicdraw.ui.diagrams.BaseCustomizableDiagramAction;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import gov.nasa.jpl.mbee.mdk.actions.ViewpointAdditionalDrawAction;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ViewDiagramConfigurator implements AMConfigurator, TargetElementAMConfigurator {
    public static final String DIAGRAM_NAME = "View Diagram";

    public static final List<String> VIEWPOINT_ALIASES = Collections.singletonList("Viewpoint");

    @Override
    public void configure(ActionsManager actionsManager) {
        Iterator<NMAction> iterator = actionsManager.getAllActions().iterator();

        while (iterator.hasNext()) {
            NMAction action = iterator.next();
            String name = action.getName();
            if (action instanceof BaseCustomizableDiagramAction && VIEWPOINT_ALIASES.contains(name)) {
                BaseCustomizableDiagramAction diagramAction = (BaseCustomizableDiagramAction) action;
                diagramAction.setCustomAdditionalDrawAction(new ViewpointAdditionalDrawAction());
            }
        }
    }

    @Override
    public int getPriority() {
        return AMConfigurator.MEDIUM_PRIORITY;
    }

    @Override
    public void configure(ActionsManager actionsManager, PresentationElement presentationElement, String s) {
        configure(actionsManager);
    }
}
