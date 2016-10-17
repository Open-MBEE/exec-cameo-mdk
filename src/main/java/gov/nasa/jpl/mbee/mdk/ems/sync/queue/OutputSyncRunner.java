package gov.nasa.jpl.mbee.mdk.ems.sync.queue;

import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.batik.apps.svgbrowser.Application;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class OutputSyncRunner implements Runnable {
    public static Logger log = Logger.getLogger(OutputSyncRunner.class);

    public static int id = 0; //used as thread id(counter)

    public class SendThread extends Thread {
        Request r;

        SendThread(Request r) {
            this.r = r;
        }

        public void run() {
            try {
                MMSUtils.sendMMSRequest(r.getRequest());
            } catch (IOException | ServerException e) {
                log.info("[ERROR] Exception occurred during request processing. Reason: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        log.info("sync runner started");
        OutputQueue q = OutputQueue.getInstance();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
            }
        });
        while (true) {
            try {
                if (q.isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
                        }
                    });
                }
                final Request r = q.take();
                q.setCurrent(r);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
                    }
                });
                SendThread st = new SendThread(r);
                st.setName("Send#" + id++);
                st.start();

                //if background on server, then wait maximumWaitTime
                //if not background and r.getWait() > defaultWaitTime, then use defaultWaitTime, otherwise r.wait ( = num of elements per seconds + 2 mins)
                final int maximumWaitTime = 5; //second
                final int waitTime = (r.isBackground() ? maximumWaitTime * 1000 : (r.getWait() > maximumWaitTime * 1000 ? maximumWaitTime * 1000 : r.getWait()));
                st.join(waitTime);

                //Utils.guilog("[INFO] A send request did not finish within expected time. So keep waiting..."); //until a user press cancel
                //log.info(st.getName() + " A send request did not finish within expected time.");
                while (st.isAlive() && r == q.getCurrent()) { // r is not current if "cancel" in queue dialog is pressed
                    //log.info(st.getName() + " Did not finish yet so waiting another 5 sec.");
                    Thread.sleep(5000);
                } //end of while
                log.info(st.getName() + " received response or cancel is pressed.");
                q.setCurrent(null);

            } catch (Exception e) {
                log.error("", e);
            }
            if (q.isEmpty()) {
                Utils.guilog("[INFO] Finished processing queued requests.");
            }

        }
    }
}
