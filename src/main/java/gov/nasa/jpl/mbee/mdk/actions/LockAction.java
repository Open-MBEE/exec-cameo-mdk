package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.teamwork2.locks.LockService;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by igomes on 12/5/16.
 */
public class LockAction extends MDAction implements AnnotationAction {
    private final Element element;
    private final boolean recursive;

    public LockAction(Element element, boolean recursive) {
        super("Lock" + (recursive ? " Recursively" : ""), "Lock" + (recursive ? " Recursively" : ""), null, null);
        this.element = element;
        this.recursive = recursive;
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        Set<Element> elements = new HashSet<>(annotations.size());
        for (Annotation annotation : annotations) {
            if (annotation.getTarget() instanceof Element) {
                elements.add((Element) annotation.getTarget());
            }
        }
        if (!elements.isEmpty()) {
            lockElements(elements, recursive);
        }
    }

    @Override
    public boolean canExecute(Collection<Annotation> annotations) {
        return true;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        super.actionPerformed(actionEvent);
        if (element == null) {
            return;
        }
        lockElements(Collections.singletonList(element), recursive);
    }

    private void lockElements(final Collection<Element> elements, boolean recursive) {
        if (elements.isEmpty()) {
            return;
        }
        ProgressStatusRunner.runWithProgressStatus(new RunnableWithProgress() {
            @Override
            public void run(ProgressStatus progressStatus) {
                LockService.getLockService(Project.getProject(elements.iterator().next())).lockElements(elements, recursive, progressStatus);
            }
        }, "Locking Elements", true, 0);
    }
}
