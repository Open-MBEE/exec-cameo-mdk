package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.ems.ExportUtility;

public class OutputSyncRunner implements Runnable {

    @Override
    public void run() {
        OutputQueue q = OutputQueue.getInstance();
        while(true) {
            Request r;
            try {
                r = q.take();
                if (r.getMethod().equals("DELETE"))
                    ExportUtility.delete(r.getUrl(), r.isFeedback());
                else if (r.getMethod().equals("DELETEALL"))
                    ExportUtility.deleteWithBody(r.getUrl(), r.getJson());
                else if (r.getPm() != null)
                    ExportUtility.send(r.getUrl(), r.getPm());
                else
                    ExportUtility.send(r.getUrl(), r.getJson(), null, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

}
