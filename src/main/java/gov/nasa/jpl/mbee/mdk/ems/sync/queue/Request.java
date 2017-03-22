package gov.nasa.jpl.mbee.mdk.ems.sync.queue;

import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Request {

    private Project project = null;
    private boolean feedback = false;
    private HttpRequestBase request = null;
    private boolean suppressGui = false;
    private int wait = 60000;
    private String type = "Element";
    private int numElements = 1;
    private boolean background = false;


    public Request(Project project, MMSUtils.HttpRequestType method, URIBuilder uri, File data, ContentType contentType, boolean feedback)
            throws IOException, URISyntaxException {
        this.project = project;
        this.request = MMSUtils.buildRequest(method, uri, data, contentType);
        this.feedback = feedback;
        this.suppressGui = !feedback;
    }

    public Request(Project project, MMSUtils.HttpRequestType method, URIBuilder uri, File data, ContentType contentType, boolean feedback, int wait, String type)
            throws IOException, URISyntaxException {
        this.project = project;
        this.request = MMSUtils.buildRequest(method, uri, data, contentType);
        this.feedback = feedback;
        this.suppressGui = !feedback;
        this.wait = wait * 1000 + 120000;
        this.type = type;
        this.numElements = wait;
    }

    public Request(Project project, URIBuilder requestUri, File data, ContentType contentType, String type)
            throws IOException, URISyntaxException {
        this.project = project;
        this.request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, data, contentType);
        this.type = type;
    }

    public Request(Project project, URIBuilder requestUri, File uploadFile, String type)
            throws IOException, URISyntaxException {
        this.project = project;
        this.request = MMSUtils.buildRequest(requestUri, uploadFile);
        this.type = type;
    }

    public Request(Project project, URIBuilder requestUri, File data, ContentType contentType, int wait, String type, Boolean background)
            throws IOException, URISyntaxException {
        this.project = project;
        if (background != null && background) {
            requestUri.setParameter("background", "true");
        }
        this.request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, data, contentType);
        this.wait = wait * 1000 + 120000;
        this.type = type;
        this.numElements = wait;
        this.background = (background == null) ? false : background;
    }

    public Request() {
    }

    public HttpRequestBase getRequest() {
        return this.request;
    }

    public boolean isBackground() {
        return this.background;
    }

    public boolean isFeedback() {
        return this.feedback;
    }

    public void setFeedback(boolean feedback) {
        this.feedback = feedback;
    }

    public boolean isSuppressGui() {
        return this.suppressGui;
    }

    public void setSuppressGui(boolean suppressGui) {
        this.suppressGui = suppressGui;
    }

    public int getWait() {
        return this.wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNumElements() {
        return this.numElements;
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;
    }

    public Project getProject() {
        return this.project;
    }

    @Override
    public String toString() {
        String s = "";
        s += "url: " + request.getURI().toString();
        s += "\nmethod: " + request.getMethod();
        s += "\nwait: " + wait + "\n";
        return s;
    }
}
