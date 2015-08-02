package gov.nasa.jpl.mbee.ems.sync;

import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

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
}