package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.http.HttpDeleteWithBody;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class MMSEndpoint {
    protected URIBuilder uriBuilder;
    protected String baseUri;

    public MMSEndpoint(String baseUri) throws URISyntaxException {
        uriBuilder = new URIBuilder(baseUri);
        this.baseUri = baseUri;
    }

    public abstract void prepareUriPath();

    public URIBuilder getEndpoint() { return uriBuilder; }

    public void setParameter(String key, String value) {
        uriBuilder.setParameter(key, value);
    }

    /**
     * Simplified call that does not send data.
     */
    public HttpRequestBase buildRequest(MMSUtils.HttpRequestType type, Project project) throws IOException, URISyntaxException {
        return buildRequest(type, null, null, project);
    }

    /**
     * Simplified call that only does JSON as the content type.
     */
    public HttpRequestBase buildRequest(MMSUtils.HttpRequestType type, File sendData, Project project) throws IOException, URISyntaxException {
        return buildRequest(type, sendData, ContentType.APPLICATION_JSON, project);
    }

    /**
     * General purpose method for making http requests. Type of request is specified in method call.
     *
     * @param type       Type of request, as selected from one of the options in the inner enum.
     * @param sendData   Data to send as an entity/body along with the request, if desired. Support for GET and DELETE
     *                   with body is included.
     * @return a generic http request
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
            request.addHeader("charset", (contentType != null ? contentType.getCharset() : Consts.UTF_8).displayName());
            if (sendData != null) {
                if (contentType != null) {
                    request.addHeader(HTTP.CONTENT_TYPE, contentType.getMimeType());
                }
                FileEntity reqEntity = new FileEntity(sendData, contentType);
                reqEntity.setChunked(true);
                ((HttpEntityEnclosingRequest) request).setEntity(reqEntity);
            }
            return request;
        }
        return null;
    }

    /**
     * Convenience method useful for replacing some placeholder string in the URI path. This assumes the passed placeholder
     * is actually in the URI.
     *
     * @param placeholder some placeholder that presumably exists within the URI's path.
     * @param replacement some string that replaces the placeholder.
     * @throws URISyntaxException could not adjust the URI path, this implies the path will be wrong later.
     */
    protected void replaceUriPlaceholder(String placeholder, String replacement) throws URISyntaxException {
        String path = uriBuilder.getPath();
        int i = path.indexOf(placeholder);
        if(i >= 0) {
            uriBuilder.setPath(path.substring(0, i) + replacement + path.substring(i + placeholder.length()));
        } else {
            String message = "Cannot replace the placeholder \"" + placeholder + "\" in the given URI.";
            throw new URISyntaxException(path, message);
        }
    }
}
