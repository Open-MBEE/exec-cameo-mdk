package gov.nasa.jpl.mbee.ems.sync;

import java.util.concurrent.atomic.AtomicReference;

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
        while(true) {
            //Request r;
            try {
                final Request r = q.take();
                log.info("got a request");
                if (r.getMethod().equals("LOG"))
                    Utils.guilog(r.getJson());
                else {
                    SendThread st = new SendThread(r);
                    st.setName("SendThread");
                    st.start();
                    st.join(r.getWait());
                    while (st.isAlive()) {
                        Application.getInstance().getGUILog().log("[INFO] A send request did not finish within expected time.");
                        log.warn("A queued send request didn't complete within wait time: " + r.toString());
                        final AtomicReference<Boolean> userwait = new AtomicReference<Boolean>();
                        SwingUtilities.invokeAndWait(new Runnable() {
                               @Override
                                public void run() {
                                     Boolean wait = Utils.getUserYesNoAnswer("The current send request did not finish within the timeout: " + r.getWait()/60000 + " min, do you want to wait longer?");
                                     if (wait == null)
                                         userwait.set(false);
                                     else
                                         userwait.set(wait);
                                }
                           });
                        Boolean result = userwait.get();
                        if (result != null && result)
                            st.join(r.getWait());
                        else
                            break;
                    }
                }
                /*else if (r.getMethod().equals("DELETE"))
                    ExportUtility.delete(r.getUrl(), r.isFeedback());
                else if (r.getMethod().equals("DELETEALL"))
                    ExportUtility.deleteWithBody(r.getUrl(), r.getJson(), r.isFeedback());
                else if (r.getPm() != null)
                    ExportUtility.send(r.getUrl(), r.getPm());
                else
                    ExportUtility.send(r.getUrl(), r.getJson(), null, false, r.isSuppressGui());*/
            } catch (Exception e) {
                log.error("", e);
            }
            if (q.isEmpty()) {
                Application.getInstance().getGUILog().log("[INFO] Finished processing queued requests.");
            }
        }
        
    }

}
