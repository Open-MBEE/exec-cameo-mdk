package gov.nasa.jpl.mbee.ems.sync.queue;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;

public class OutputSyncRunner implements Runnable {
    public static Logger log = Logger.getLogger(OutputSyncRunner.class);

    public static int id = 0; //used as thread id(counter)

    public class SendThread extends Thread {
        Request r;

        SendThread(Request r) {
            this.r = r;
        }

        public void run() {
            if (r.getMethod().equals("DELETE"))
                ExportUtility.delete(r.getUrl(), r.isFeedback());
            else if (r.getMethod().equals("DELETEALL"))
                ExportUtility.deleteWithBody(r.getUrl(), r.getJson(), r.isFeedback());
            else if (r.getPm() != null)
                ExportUtility.send(r.getUrl(), r.getPm());
            else
                ExportUtility.send(r.getUrl(), r.getJson()/*, null*/, false, r.isSuppressGui(), this.getName()); //POST
        }
    }

    @Override
    public void run() {
        log.info("sync runner started");
        gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue q = gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue.getInstance();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gov.nasa.jpl.mbee.ems.sync.queue.OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
            }
        });
        while (true) {
            try {
                if (q.isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            gov.nasa.jpl.mbee.ems.sync.queue.OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
                        }
                    });
                }
                final Request r = q.take();
                q.setCurrent(r);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OutputQueueStatusConfigurator.getOutputQueueStatusAction().update(true);
                    }
                });
                if (r.getMethod().equals("LOG"))
                    Utils.guilog(r.getJson());
                else {
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
                }    //end of r == current

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
