package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSEndpointFactory {
    public static MMSEndpoint getMMSEndpoint(String baseUri, String endpointType) {
        MMSEndpoint mmsEndpoint = null;

        switch(endpointType) {
            case MMSEndpointConstants.LOGIN_CASE: {
                mmsEndpoint = new MMSLoginEndpoint(baseUri);
                break;
            }
            case MMSEndpointConstants.ORGS_CASE: {
                mmsEndpoint = new MMSOrgsEndpoint(baseUri);
                break;
            }
            case MMSEndpointConstants.PROJECTS_CASE: {
                mmsEndpoint = new MMSProjectsEndpoint(baseUri);
                break;
            }
            case MMSEndpointConstants.REFS_CASE: {
                mmsEndpoint = new MMSRefsEndpoint(baseUri);
                break;
            }
            case MMSEndpointConstants.ELEMENTS_CASE: {
                mmsEndpoint = new MMSElementsEndpoint(baseUri);
            }
            default:break;
        }

        return mmsEndpoint;
    }
}
