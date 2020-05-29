package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSEndpointFactory {
    public static MMSEndpoint getMMSEndpoint(String baseUri, MMSEndpointType endpointType) throws URISyntaxException {
        MMSEndpoint mmsEndpoint = null;

        switch(endpointType) {
            case LOGIN: {
                mmsEndpoint = new MMSLoginEndpoint(baseUri);
                break;
            }
            case ORGS: {
                mmsEndpoint = new MMSOrgsEndpoint(baseUri);
                break;
            }
            case PROJECTS: {
                mmsEndpoint = new MMSProjectsEndpoint(baseUri);
                break;
            }
            case PROJECT: {
                mmsEndpoint = new MMSProjectEndpoint(baseUri);
                break;
            }
            case REFS: {
                mmsEndpoint = new MMSRefsEndpoint(baseUri);
                break;
            }
            case REF: {
                mmsEndpoint = new MMSRefEndpoint(baseUri);
                break;
            }
            case COMMIT: {
                mmsEndpoint = new MMSCommitEndpoint(baseUri);
                break;
            }
            case ELEMENTS: {
                mmsEndpoint = new MMSElementsEndpoint(baseUri);
                break;
            }
            case COMMITS: {
                mmsEndpoint = new MMSCommitsEndpoint(baseUri);
                break;
            }
            case SEARCH: {
                mmsEndpoint = new MMSSearchEndpoint(baseUri);
                break;
            }
            default:break;
        }

        return mmsEndpoint;
    }
}
