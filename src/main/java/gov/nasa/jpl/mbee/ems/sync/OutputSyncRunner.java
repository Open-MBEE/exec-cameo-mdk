package gov.nasa.jpl.mbee.ems.sync;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.nomagic.magicdraw.core.Application;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;

public class OutputSyncRunner implements Runnable {
    public static Logger log = Logger.getLogger(OutputSyncRunner.class);

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
                ExportUtility.send(r.getUrl(), r.getJson(), null, false, r.isSuppressGui());
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
            // Request r;
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
                        OutputQueueStatusConfigurator.getOutputQueueStatusAction().update(true);
                    }
                });
                log.info("got a request");
                if (r.getMethod().equals("LOG"))
                    Utils.guilog(r.getJson());
                else {
                    SendThread st = new SendThread(r);
                    st.setName("SendThread");
                    st.start();
                    st.join(r.getWait());
                    // st.join(1);

                    if (st.isAlive()) {
                        Utils.guilog("[INFO] A send request did not finish within expected time.");
                        log.warn("A queued send request didn't complete within wait time: " + r.toString());
                        final AtomicReference<Boolean> userwait = new AtomicReference<Boolean>();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Boolean wait = Utils.getUserYesNoAnswer("The current send request did not finish within the timeout: " + r.getWait() / 60000 + " min, do you want to wait longer?");
                                if (wait == null)
                                    userwait.set(false);
                                else
                                    userwait.set(wait);
                            }
                        });
                        while (st.isAlive()) {
                            Thread.sleep(100);
                            final Boolean result = userwait.get();
                            if (result != null && result) {
                                st.join(r.getWait());
                                if (st.isAlive()) {
                                    userwait.set(null);
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            Boolean wait = Utils.getUserYesNoAnswer("The current send request did not finish within the timeout: "
                                                            + r.getWait() / 60000 + " min, do you want to wait longer?");
                                            if (wait == null)
                                                userwait.set(false);
                                            else
                                                userwait.set(wait);
                                        }
                                    });
                                }
                            } else if (result != null && !result)
                                break;
                            else {
                                continue;
                            }
                        }

                        if (!st.isAlive() && !GraphicsEnvironment.isHeadless() && JOptionPane.getRootFrame() != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.getRootFrame().dispose();
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
            if (q.isEmpty()) {
                Utils.guilog("[INFO] Finished processing queued requests.");
            }
            q.setCurrent(null);
        }
    }

}
