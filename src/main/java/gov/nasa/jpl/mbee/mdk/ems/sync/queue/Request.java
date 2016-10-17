package gov.nasa.jpl.mbee.mdk.ems.sync.queue;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.omg.CORBA.Object;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Request {

//    private ObjectNode jsondata = null;
//    private String json = "";
//    private URIBuilder requestUri = null;
//    private String url = "";
//    private MMSUtils.HttpRequestType method = MMSUtils.HttpRequestType.POST;
//    private String method = "POST";
    private boolean feedback = false;
    private HttpRequestBase request = null;
//    private PostMethod pm = null;
    private boolean suppressGui = false;
    private int wait = 60000;
    private String type = "Element";
    private int numElements = 1;
    private boolean background = false;


    public Request(MMSUtils.HttpRequestType method, URIBuilder uri, ObjectNode data, boolean feedback)
            throws IOException, URISyntaxException {
        this.request = MMSUtils.buildRequest(method, uri, data);
        this.feedback = feedback;
        this.suppressGui = !feedback;
    }

    /*
    public Request(String url, String json, String method, boolean feedback) {
        this.url = url;
        this.json = json;
        this.method = method;
        this.feedback = feedback;
        this.suppressGui = !feedback;
    }
    */

    public Request(MMSUtils.HttpRequestType method, URIBuilder uri, ObjectNode data, boolean feedback, int wait, String type)
            throws IOException, URISyntaxException {
        this.request = MMSUtils.buildRequest(method, uri, data);
        this.feedback = feedback;
        this.suppressGui = !feedback;
        this.wait = wait * 1000 + 120000;
        this.type = type;
        this.numElements = wait;
    }

    /*
    public Request(String url, String json, String method, boolean feedback, int wait, String type) {
        this.url = url;
        this.json = json;
        this.method = method;
        this.feedback = feedback;
        this.suppressGui = !feedback;
        this.wait = wait * 1000 + 120000;
        this.type = type;
        this.numElements = wait;
    }
    */

    public Request(URIBuilder requestUri, ObjectNode data, String type)
            throws IOException, URISyntaxException {
        this.request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, data);
        this.type = type;
    }

    public Request(URIBuilder requestUri, File uploadFile, String type)
            throws IOException, URISyntaxException {
        this.request = MMSUtils.buildRequest(requestUri, uploadFile);
        this.type = type;
    }

    /*
    public Request(String url, String json, String type) {
        this.url = url;
        this.json = json;
        this.type = type;
    }
    */

    public Request(URIBuilder requestUri, ObjectNode data, int wait, String type, Boolean background)
            throws IOException, URISyntaxException {
        if (background != null && background) {
            requestUri.setParameter("background", "true");
        }
        this.request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, data);
        this.wait = wait * 1000 + 120000;
        this.type = type;
        this.numElements = wait;
        this.background = (background == null) ? false : background;
    }

    /*
    public Request(String url, String json, int wait, String type, Boolean background) {
        this.url = url;
        if (background != null && background) {
            this.url += "?background=true";
        }

        this.json = json;
        this.wait = wait * 1000 + 120000;
        this.type = type;
        this.numElements = wait;
        this.background = (background == null) ? false : background;
    }
    */

    /*
    public Request(String url, PostMethod pm, String type) {
        this.pm = pm;
        this.url = url;
        this.type = type;
    }
    */

    public Request() {
    }

    public HttpRequestBase getRequest() {
        return this.request;
    }

    public boolean isBackground() {
        return this.background;
    }

    /*
    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
    */

    /*
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    */


    public boolean isFeedback() {
        return this.feedback;
    }

    public void setFeedback(boolean feedback) {
        this.feedback = feedback;
    }

    /*
    public PostMethod getPm() {
        return pm;
    }

    public void setPm(PostMethod pm) {
        this.pm = pm;
    }
    */

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

    @Override
    public String toString() {
        String s = "";
        s += "url: " + request.getURI().toString();
        s += "\nmethod: " + request.getMethod();
        s += "\nwait: " + wait + "\n";
        return s;
    }
}
