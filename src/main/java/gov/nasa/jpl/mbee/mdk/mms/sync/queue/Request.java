package gov.nasa.jpl.mbee.mdk.mms.sync.queue;

import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Request {

    private final Project project;
    private final HttpRequestBase request;
    private final int count, completionDelay;
    private final String name;

    public Request(Project project, MMSUtils.HttpRequestType method, URIBuilder uri, File file, ContentType contentType, int count, String name)
            throws IOException, URISyntaxException {
        this(project, method, uri, file, contentType, count, name, 0);
    }

    public Request(Project project, MMSUtils.HttpRequestType method, URIBuilder uri, File file, ContentType contentType, int count, String name, int completionDelay)
            throws IOException, URISyntaxException {
        this.project = project;
        this.request = MMSUtils.buildRequest(method, uri, file, contentType);
        this.count = count;
        this.name = name;
        this.completionDelay = completionDelay;
    }

    public Request(Project project, URIBuilder requestUri, File file, int count, String name)
            throws IOException, URISyntaxException {
        this.project = project;
        this.request = MMSUtils.buildImageRequest(requestUri, file);
        this.count = count;
        this.name = name;
        this.completionDelay = 0;
    }

    public Project getProject() {
        return project;
    }

    public HttpRequestBase getRequest() {
        return request;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public int getCompletionDelay() {
        return completionDelay;
    }

    @Override
    public String toString() {
        String s = "";
        s += "url: " + request.getURI().toString();
        s += "\nmethod: " + request.getMethod();
        return s;
    }
}
