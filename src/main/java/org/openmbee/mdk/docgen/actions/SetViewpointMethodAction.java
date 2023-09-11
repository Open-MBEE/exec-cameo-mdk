package org.openmbee.mdk.docgen.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Operation;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import org.openmbee.mdk.actions.ViewpointAdditionalDrawAction;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.validation.IRuleViolationAction;
import org.openmbee.mdk.validation.RuleViolationAction;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SetViewpointMethodAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction, Runnable {
    private static String DEFAULT_ID = SetViewpointMethodAction.class.getSimpleName();

    private final Class viewpoint;
    private final Behavior behavior;

    public SetViewpointMethodAction(Class viewpoint, Behavior behavior, String name) {
        super(DEFAULT_ID, name, null, null);
        this.viewpoint = viewpoint;
        this.behavior = behavior;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        Project project = Project.getProject(viewpoint);
        SessionManager.getInstance().createSession(project, DEFAULT_ID);
        try {
            run();
        }
        finally {
            if (SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().closeSession(project);
            }
        }
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        Project project = Project.getProject(viewpoint);
        SessionManager.getInstance().createSession(project, DEFAULT_ID);
        try {
            annotations.stream().flatMap(annotation -> annotation.getActions().stream()).filter(action -> action instanceof SetViewpointMethodAction).map(action -> (SetViewpointMethodAction) action).collect(Collectors.toMap(SetViewpointMethodAction::getViewpoint, Function.identity(), (oldValue, newValue) -> oldValue)).values().forEach(SetViewpointMethodAction::run);
        }
        finally {
            if (SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().closeSession(project);
            }
        }
    }

    @Override
    public boolean canExecute(Collection<Annotation> collection) {
        return true;
    }

    @Override
    public void run() {
        if (!viewpoint.isEditable()) {
            Application.getInstance().getGUILog().log("[WARNING] " + Converters.getElementToHumanNameConverter().apply(viewpoint) + " is not editable. Aborting setting nested behavior as viewpoint method.");
        }
        Operation operation = ViewpointAdditionalDrawAction.createOperation(viewpoint);
        operation.getMethod().add(behavior);
    }

    public Class getViewpoint() {
        return viewpoint;
    }

    public Behavior getBehavior() {
        return behavior;
    }
}
