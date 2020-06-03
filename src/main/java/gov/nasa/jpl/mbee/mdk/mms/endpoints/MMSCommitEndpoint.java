package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSCommitEndpoint extends MMSProjectEndpoint {
    public MMSCommitEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new CommitBuilder();
    }

    public static class CommitBuilder extends ProjectBuilder {
        @Override
        public void prepareUriPath() {
            String suffix = getStringParam(MMSEndpointBuilderConstants.URI_COMMIT_SUFFIX);

            if(suffix.isEmpty()) {
                throw new NullPointerException();
            }

            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.COMMIT.getPath() + "/" + suffix);
        }
    }
}
