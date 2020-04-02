package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSEndpointFactory {
    public static MMSEndpoint getMMSEndpoint(String baseUri, String endpointType) {
        MMSEndpoint mmsEndpoint = null;

        switch(endpointType) {
            case "login": {
                mmsEndpoint = new MMSLoginEndpoint(baseUri);
                break;
            }
            case "orgs": {
                mmsEndpoint = new MMSOrgsEndpoint(baseUri);
            }
            default:break;
        }

        return mmsEndpoint;
    }
}
