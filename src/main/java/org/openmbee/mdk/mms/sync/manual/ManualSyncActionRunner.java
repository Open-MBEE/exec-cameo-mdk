package org.openmbee.mdk.mms.sync.manual;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ManualSyncActionRunner<A extends NMAction & AnnotationAction> implements RunnableWithProgress {
    private final Class<A> actionClass;
    private final Project project;
    private final ManualSyncRunner manualSyncRunner;

    public ManualSyncActionRunner(Class<A> actionClass, Collection<Element> rootElements, Project project, int depth) {
        this.actionClass = actionClass;
        this.project = project;
        manualSyncRunner = new ManualSyncRunner(rootElements, project, depth);
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        manualSyncRunner.run(progressStatus);
        if (manualSyncRunner.getValidationSuite() != null && manualSyncRunner.getValidationSuite().hasErrors()) {
            List<Annotation> annotations = manualSyncRunner.getValidationSuite().getValidationRules().stream()
                    .flatMap(validationRule -> validationRule.getViolations().stream())
                    .flatMap(violation -> violation.getActions().stream()).filter(action -> actionClass.isAssignableFrom(action.getClass()))
                    .map(action -> new Annotation(Annotation.getSeverityLevel(project, Annotation.ERROR), action.toString(), "", null, Collections.singletonList(action))).collect(Collectors.toCollection(LinkedList::new));
            if (annotations.isEmpty()) {
                Application.getInstance().getGUILog().log("[INFO] No manual sync validation actions of type " + actionClass.getSimpleName() + ".");
                return;
            }
            AnnotationAction annotationAction = ((AnnotationAction) annotations.get(0).getActions().get(0));
            if (!annotationAction.canExecute(annotations)) {
                Application.getInstance().getGUILog().log("[WARNING] Cannot execute " + actionClass.getSimpleName() + ". Aborting action.");
                return;
            }
            annotationAction.execute(annotations);
            Application.getInstance().getGUILog().log("[INFO] Executed " + NumberFormat.getInstance().format(annotations.size()) + " " + actionClass.getSimpleName() + " actions.");
        }
        else {
            Application.getInstance().getGUILog().log("[INFO] All validated elements are equivalent.");
        }
    }
}
