package gov.nasa.jpl.mbee.ems;

import org.apache.commons.httpclient.methods.PostMethod;

public class GetMethodWithEntity extends PostMethod {
    public final static String METHOD_NAME = "GET";
    
    public GetMethodWithEntity(String url) {
        super(url);
    }
    
    @Override
    public String getName() {
        return METHOD_NAME;
    }
}