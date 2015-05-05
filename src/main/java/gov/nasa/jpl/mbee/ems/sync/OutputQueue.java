package gov.nasa.jpl.mbee.ems.sync;

import java.util.concurrent.LinkedBlockingQueue;

public class OutputQueue extends LinkedBlockingQueue<Request> {
    private final static OutputQueue instance = new OutputQueue();
    private OutputQueue() {
        super();
    }
    
    public static OutputQueue getInstance() {
        return instance;
    }
}