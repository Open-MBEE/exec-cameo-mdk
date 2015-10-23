package gov.nasa.jpl.mbee.ems.sync;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

import gov.nasa.jpl.mbee.lib.Utils;

public class OutputQueue extends LinkedBlockingQueue<Request> {
    private final static OutputQueue instance = new OutputQueue();
    private Request current = null;
    private OutputQueue() {
        super();
    }
    
    @Override
    public boolean offer(Request e) {
    	boolean result = super.offer(e);
    	SwingUtilities.invokeLater(new Runnable() {
        	@Override
            public void run() {
        		OutputQueueStatusConfigurator.getOutputQueueStatusAction().update(true);
            }
        });
    	return result;
    }
    
    public static OutputQueue getInstance() {
        return instance;
    }
    
    public void setCurrent(Request r) {
        this.current = r;
    }
    
    public Request getCurrent() {
        return current;
    }
    public void remove(int _rowNum ){
    	//linkedQueue not contain current so the index of removing row is (_rowNum -1)
    	Request toBeRemoved = (Request) OutputQueue.getInstance().toArray()[_rowNum-1];
    	Utils.guilog("[INFO] Removing  a queue: " + toBeRemoved.getJson());
    	OutputQueue.getInstance().remove(toBeRemoved);
    }
    
}