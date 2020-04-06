package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.http.HttpDeleteWithBody;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class MMSEndpoint {
    protected URIBuilder uriBuilder;
    protected String baseUri;

    public MMSEndpoint(String baseUri) {
        try {
            uriBuilder = new URIBuilder(baseUri);
            this.baseUri = baseUri;
        } catch (URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error in generation of MMS URL for project. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public abstract void prepareUriPath();

    public URIBuilder getEndpoint() { return uriBuilder; }

    /**
     * General purpose method for making http requests for JSON objects. Type of request is specified in method call.
     *
     * @param type       Type of request, as selected from one of the options in the inner enum.
     * @param sendData   Data to send as an entity/body along with the request, if desired. Support for GET and DELETE
     *                   with body is included.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public HttpRequestBase buildRequest(MMSUtils.HttpRequestType type, File sendData, ContentType contentType, Project project) throws IOException, URISyntaxException {
        if(uriBuilder != null) {
            // build specified request type
            // assume that any request can have a body, and just build the appropriate one
            URI requestDest = uriBuilder.build();
            final HttpRequestBase request;
            // bulk GETs are not supported in MMS, but bulk PUTs are. checking and and throwing error here in case
            if (type == MMSUtils.HttpRequestType.GET && sendData != null) {
                throw new IOException("GETs with body are not supported");
            }
            switch (type) {
                case DELETE:
                    request = new HttpDeleteWithBody(requestDest);
                    break;
                case GET:
                default:
                    request = new HttpGet(requestDest);
                    break;
                case POST:
                    request = new HttpPost(requestDest);
                    break;
                case PUT:
                    request = new HttpPut(requestDest);
                    break;
            }
            request.addHeader("Authorization", "Bearer " + TicketUtils.getTicket(project));
            request.addHeader("Content-Type", "application/json"); // is this good logic?
            request.addHeader("charset", (contentType != null ? contentType.getCharset() : Consts.UTF_8).displayName());
            if (sendData != null) {
                if (contentType != null) {
                    request.addHeader("Content-Type", contentType.getMimeType());
                }
                HttpEntity reqEntity = new FileEntity(sendData, contentType);
                //reqEntity.setChunked(true);
                ((HttpEntityEnclosingRequest) request).setEntity(reqEntity);
            }
            return request;
        }
        return null;
    }
}
