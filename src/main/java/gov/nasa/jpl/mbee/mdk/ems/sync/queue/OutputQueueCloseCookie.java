package gov.nasa.jpl.mbee.mdk.ems.sync.queue;

import com.nomagic.magicdraw.cookies.CloseCookie;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;

import javax.swing.*;

/**
 * Created by igomes on 9/1/16.
 */
public class OutputQueueCloseCookie implements CloseCookie {
    private final CloseCookie parentCloseCookie;

    public OutputQueueCloseCookie(CloseCookie closeCookie) {
        parentCloseCookie = closeCookie;
    }

    @Override
    public void close(byte closeStatus) {
        // (if background task is running, show the "waiting" dialog until the task is finished
        // then continue with the close)
        if (!OutputQueue.getInstance().isEmpty()) {
            ProgressStatusRunner.runWithProgressStatus(new OutputQueueCloseCookieRunnable(), "MDK Shutdown", true, 0);
        }
        if (parentCloseCookie != null) {
            parentCloseCookie.close(closeStatus);
        }
    }

    public static class OutputQueueCloseCookieRunnable implements RunnableWithProgress {
        @Override
        public void run(ProgressStatus progressStatus) {
            int initialSize = OutputQueue.getInstance().size() + (OutputQueue.getInstance().getCurrent() != null ? 1 : 0);
            progressStatus.setMax(initialSize + 1);
            while (!OutputQueue.getInstance().isEmpty() || OutputQueue.getInstance().getCurrent() != null) {
                int size = OutputQueue.getInstance().size() + (OutputQueue.getInstance().getCurrent() != null ? 1 : 0);
                if (size > initialSize) {
                    initialSize = size;
                    progressStatus.setMax(initialSize + 1);
                }
                progressStatus.setCurrent(initialSize - OutputQueue.getInstance().size());
                progressStatus.setDescription("Processing request queue (" + progressStatus.getCurrent() + "/" + initialSize + ")");

                if (progressStatus.isCancel()) {
                    int input = JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(), "There are still pending requests in the queue. \nCancelling may result in loss of model parity. \nAre you sure you wish to cancel?", "Cancel Requested", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (input == JOptionPane.YES_OPTION) {
                        break;
                    }
                    else {
                        progressStatus.setCancel(false);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
