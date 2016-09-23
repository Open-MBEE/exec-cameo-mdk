package gov.nasa.jpl.mbee.mdk.validation;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressMonitorHelper;

import java.util.Collection;

public abstract class IndeterminateProgressMonitorProxy extends GenericRuleViolationAction implements RunnableWithProgress {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IndeterminateProgressMonitorProxy(final String name) {
        super(name);
    }

    public static IndeterminateProgressMonitorProxy wrap(final GenericRuleViolationAction action) {
        return new IndeterminateProgressMonitorProxy(action.getName()) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void run(ProgressStatus progressStatus) {
                Application.getInstance().getGUILog().log("PROGRESS STATUS!");
                progressStatus.setIndeterminate(true);
                run();
            }

            @Override
            public void run() {
                action.run();
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.actionPerformed(e);
            }

            @Override
            public boolean canExecute(Collection<Annotation> annotations) {
                return action.canExecute(annotations);
            }

            @Override
            public void execute(Collection<Annotation> annotations) {
                action.execute(annotations);
            }

            @Override
            public String getName() {
                return action.getName();
            }

            @Override
            public String getSessionName() {
                return action.getSessionName();
            }

        };
    }

    public static void execute(final IndeterminateProgressMonitorProxy proxy, final String description) {
        ProgressMonitorHelper.executeWithProgress(proxy, description, false, 0);
    }

    public static void execute(final GenericRuleViolationAction action, final String description) {
        execute(wrap(action), description);
    }

    public static GenericRuleViolationAction wrap(final IndeterminateProgressMonitorProxy action, final String description) {
        return new GenericRuleViolationAction(action.getName()) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void run() {
                action.run();
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.actionPerformed(e);
            }

            @Override
            public boolean canExecute(Collection<Annotation> annotations) {
                return action.canExecute(annotations);
            }

            @Override
            public void execute(Collection<Annotation> annotations) {
                action.execute(annotations);
            }

            @Override
            public String getName() {
                return action.getName();
            }

            @Override
            public String getSessionName() {
                return action.getSessionName();
            }

        };
    }

    public static GenericRuleViolationAction doubleWrap(final GenericRuleViolationAction action, final String description) {
        return action;
    }

    public static GenericRuleViolationAction doubleWrapp(final GenericRuleViolationAction action, final String description) {
        return new GenericRuleViolationAction(action.getName()) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void run() {
                action.run();
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.actionPerformed(e);
            }

            @Override
            public boolean canExecute(Collection<Annotation> annotations) {
                return action.canExecute(annotations);
            }

            @Override
            public void execute(Collection<Annotation> annotations) {
                action.execute(annotations);
            }

            @Override
            public String getName() {
                return action.getName();
            }

            @Override
            public String getSessionName() {
                return action.getSessionName();
            }

        };
    }

    private class SpoofedRunnableWithProgress implements RunnableWithProgress {

        private boolean isCompleted;

        @Override
        public void run(ProgressStatus progressStatus) {
            progressStatus.setIndeterminate(true);
            while (!isCompleted) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
