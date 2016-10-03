package gov.nasa.jpl.mbee.mdk.api.stream;

/**
 * Created by igomes on 10/3/16.
 */
public class MDKCollectors {
    public static ArrayNodeCollector toArrayNode(){
        return new ArrayNodeCollector();
    }
}
