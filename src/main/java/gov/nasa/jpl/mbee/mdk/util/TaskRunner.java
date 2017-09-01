package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.task.BackgroundTaskRunner;
import com.nomagic.task.EmptyProgressStatus;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskRunner {
    public static Future<?> runWithProgressStatus(Runnable runnable, String title, ThreadExecutionStrategy strategy) {
        return runWithProgressStatus(runnable, title, strategy, false);
    }

    public static Future<?> runWithProgressStatus(Runnable runnable, String title, ThreadExecutionStrategy strategy, boolean silent) {
        return runWithProgressStatus(progressStatus -> {
            progressStatus.setIndeterminate(true);
            runnable.run();
        }, title, false, strategy, silent);
    }

    public static Future<?> runWithProgressStatus(RunnableWithProgress runnableWithProgress, String title, boolean allowCancel, ThreadExecutionStrategy strategy) {
        return runWithProgressStatus(runnableWithProgress, title, allowCancel, strategy, false);
    }

    public static Future<?> runWithProgressStatus(RunnableWithProgress runnableWithProgress, String title, boolean allowCancel, ThreadExecutionStrategy strategy, boolean silent) {
        AtomicReference<ProgressStatus> progressStatusAtomicReference = new AtomicReference<>();
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Runnable runnable = () -> {
            lock.lock();
            try {
                while (progressStatusAtomicReference.get() == null) {
                    condition.await();
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
            ProgressStatus progressStatus = progressStatusAtomicReference.get();
            ProgressStatusProxy progressStatusProxy = new ProgressStatusProxy(progressStatus, title);
            progressStatusProxy.setLocked(false);
            progressStatusProxy.setDescription(null);
            runnableWithProgress.run(progressStatusProxy);
        };

        Future<?> future = strategy.getExecutor().submit(runnable);
        if (!silent) {
            BackgroundTaskRunner.runWithProgressStatus(progressStatus -> {
                progressStatus.setLocked(true);
                lock.lock();
                progressStatusAtomicReference.set(progressStatus);
                try {
                    condition.signal();
                } finally {
                    lock.unlock();
                }
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }, title + " | Waiting...", allowCancel);
        }
        else {
            lock.lock();
            progressStatusAtomicReference.set(EmptyProgressStatus.getDefault());
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
        return future;
    }

    public enum ThreadExecutionStrategy {
        SINGLE(new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>())),
        POOLED(new ThreadPoolExecutor(Math.min(Runtime.getRuntime().availableProcessors(), 4), 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>())),
        NONE(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>()));

        private final ThreadPoolExecutor executor;

        ThreadExecutionStrategy(ThreadPoolExecutor executor) {
            this.executor = executor;
        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }
    }

    private static class ProgressStatusProxy implements ProgressStatus {
        private final ProgressStatus progressStatus;
        private final String title;

        ProgressStatusProxy(ProgressStatus progressStatus, String title) {
            this.progressStatus = progressStatus;
            this.title = title;
        }


        @Override
        public void setMin(long l) {
            progressStatus.setMin(l);
        }

        @Override
        public void setMax(long l) {
            progressStatus.setMax(l);
        }

        @Override
        public void setCurrent(long l) {
            progressStatus.setCurrent(l);
        }

        @Override
        public void setDescription(String s) {
            progressStatus.setDescription(title + (s != null && !s.isEmpty() ? " | " + s : ""));
        }

        @Override
        public int getPercentage() {
            return progressStatus.getPercentage();
        }

        @Override
        public long getMin() {
            return progressStatus.getMin();
        }

        @Override
        public long getMax() {
            return progressStatus.getMax();
        }

        @Override
        public long getCurrent() {
            return progressStatus.getCurrent();
        }

        @Override
        public String getDescription() {
            return progressStatus.getDescription();
        }

        @Override
        public void reset() {
            progressStatus.reset();
        }

        @Override
        public void increase() {
            progressStatus.increase();
        }

        @Override
        public void init(String s, long l, long l1, long l2) {
            progressStatus.init(s, l, l1, l2);
        }

        @Override
        public void init(String s, long l, long l1) {
            progressStatus.init(s, l, l1);
        }

        @Override
        public void init(String s, long l) {
            progressStatus.init(s, l);
        }

        @Override
        public boolean isCompleted() {
            return progressStatus.isCompleted();
        }

        @Override
        public void setIndeterminate(boolean b) {
            progressStatus.setIndeterminate(b);
        }

        @Override
        public boolean isIndeterminate() {
            return progressStatus.isIndeterminate();
        }

        @Override
        public void setLocked(boolean b) {
            progressStatus.setLocked(b);
        }

        @Override
        public boolean isLocked() {
            return progressStatus.isLocked();
        }

        @Override
        public void setCancel(boolean b) {
            progressStatus.setCancel(b);
        }

        @Override
        public boolean isCancel() {
            return progressStatus.isCancel();
        }
    }
}
