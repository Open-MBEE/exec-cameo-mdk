package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;

import org.apache.http.Consts;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static gov.nasa.jpl.mbee.mdk.mms.MMSUtils.sendMMSRequest;

public class MMSTWCLoginEndpoint extends MMSEndpoint {
    public MMSTWCLoginEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.TWC_LOGIN_ENDPOINT);
        uriBuilder.clearParameters();
    }

    public String buildTWCLoginRequest(Project project, String twcServer, String authToken,
            ProgressStatus progressStatus) throws IOException, URISyntaxException, ServerException {
        // build request
        URI requestDest = uriBuilder.build();
        HttpRequestBase request = new HttpGet(requestDest);
        request.addHeader(MDKConstants.CONTENT_TYPE, MDKConstants.APPLICATION_JSON);
        request.addHeader(MDKConstants.CHARSET, (Consts.UTF_8).displayName());
        request.addHeader(MDKConstants.TWC_HEADER, twcServer);
        request.addHeader(MDKConstants.AUTHORIZATION, "Token :" + authToken);

        // do request
        ObjectNode responseJson = JacksonUtils.getObjectMapper().createObjectNode();
        sendMMSRequest(project, request, progressStatus, responseJson);
        if (responseJson != null && responseJson.get(MMSEndpointConstants.AUTHENTICATION_RESPONSE_JSON_KEY) != null
                && responseJson.get(MMSEndpointConstants.AUTHENTICATION_RESPONSE_JSON_KEY).isTextual()) {
            return responseJson.get(MMSEndpointConstants.AUTHENTICATION_RESPONSE_JSON_KEY).asText();
        }
        return null;
    }
}
