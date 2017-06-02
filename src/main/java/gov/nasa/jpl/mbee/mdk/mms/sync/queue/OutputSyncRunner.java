package gov.nasa.jpl.mbee.mdk.mms.sync.queue;

import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Pair;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class OutputSyncRunner implements Runnable {
    public static Logger log = Logger.getLogger(OutputSyncRunner.class);
    public static int id = 0; //used as thread id(counter)

    private static Pair<Request, Exception> lastException = null;

    public static Pair<Request, Exception> getLastExceptionPair() {
        return lastException;
    }

    public static void clearLastExceptionPair() {
        lastException = null;
    }

    public class SendThread extends Thread {
        Request r;

        SendThread(Request r) {
            this.r = r;
        }

        public void run() {
            try {
                MMSUtils.sendMMSRequest(r.getProject(), r.getRequest());
            } catch (IOException | ServerException | URISyntaxException e) {
                lastException = new Pair<>(r, e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        OutputQueue outputQueue = OutputQueue.getInstance();
        SwingUtilities.invokeLater(() -> OutputQueueStatusConfigurator.getOutputQueueStatusAction().update());
        while (true) {
            try {
                if (outputQueue.isEmpty()) {
                    SwingUtilities.invokeLater(() -> OutputQueueStatusConfigurator.getOutputQueueStatusAction().update());
                }
                final Request request = outputQueue.take();
                outputQueue.setCurrent(request);
                SwingUtilities.invokeLater(() -> OutputQueueStatusConfigurator.getOutputQueueStatusAction().update());
                SendThread sendThread = new SendThread(request);
                sendThread.setName(sendThread.toString() + " - #" + id++);
                sendThread.start();
                sendThread.join(0);
                while (sendThread.isAlive() && request == outputQueue.getCurrent()) {
                    Thread.sleep(1000);
                }
                if (request.getCompletionDelay() > 0) {
                    int slept = 0;
                    do {
                        int duration = Math.min(request.getCompletionDelay() - slept, 1000);
                        Thread.sleep(duration);
                        slept += duration;
                    } while (slept < request.getCompletionDelay() && request == outputQueue.getCurrent());
                }
                outputQueue.setCurrent(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (outputQueue.isEmpty()) {
                Utils.guilog("[INFO] Finished processing queued requests.");
            }

        }
    }
}
