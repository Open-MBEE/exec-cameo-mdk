package gov.nasa.jpl.mbee.ems;

import org.apache.commons.httpclient.methods.PostMethod;

public class DeleteMethodWithEntity extends PostMethod {
    public final static String METHOD_NAME = "DELETE";
    
    public DeleteMethodWithEntity(String url) {
        super(url);
    }
    
    @Override
    public String getName() {
        return METHOD_NAME;
    }
}
