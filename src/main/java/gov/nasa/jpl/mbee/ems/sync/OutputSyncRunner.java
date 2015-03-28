package gov.nasa.jpl.mbee.ems.sync;

import org.apache.log4j.Logger;

import com.nomagic.magicdraw.core.Application;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;

public class OutputSyncRunner implements Runnable {
    public static Logger log = Logger.getLogger(OutputSyncRunner.class);
    
    @Override
    public void run() {
        log.info("sync runner started");
        OutputQueue q = OutputQueue.getInstance();
        while(true) {
            Request r;
            try {
                r = q.take();
                log.info("got a request");
                if (r.getMethod().equals("LOG"))
                    Utils.guilog(r.getJson());
                else if (r.getMethod().equals("DELETE"))
                    ExportUtility.delete(r.getUrl(), r.isFeedback());
                else if (r.getMethod().equals("DELETEALL"))
                    ExportUtility.deleteWithBody(r.getUrl(), r.getJson(), r.isFeedback());
                else if (r.getPm() != null)
                    ExportUtility.send(r.getUrl(), r.getPm());
                else
                    ExportUtility.send(r.getUrl(), r.getJson(), null, false, r.isSuppressGui());
            } catch (Exception e) {
                log.error("", e);
            }
        }
        
    }

}
