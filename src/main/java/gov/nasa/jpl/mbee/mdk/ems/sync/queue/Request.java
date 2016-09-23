package gov.nasa.jpl.mbee.mdk.ems.sync.queue;

import org.apache.commons.httpclient.methods.PostMethod;

public class Request {

    private String json = "";
    private String url = "";
    private String method = "POST";
    private boolean feedback = false;
    private PostMethod pm = null;
    private boolean suppressGui = false;
    private int wait = 60000;
    private String type = "Element";
    private int numElements = 1;
    private boolean background = false;

    public Request(String url, String json, String method, boolean feedback) {
        this.url = url;
        this.json = json;
        this.method = method;
        this.feedback = feedback;
        this.suppressGui = !feedback;
    }

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

    public Request(String url, String json, String type) {
        this.url = url;
        this.json = json;
        this.type = type;
    }

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

    public Request(String url, PostMethod pm, String type) {
        this.pm = pm;
        this.url = url;
        this.type = type;
    }

    public Request() {
    }

    public boolean isBackground() {
        return background;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public boolean isFeedback() {
        return feedback;
    }

    public void setFeedback(boolean feedback) {
        this.feedback = feedback;
    }

    public PostMethod getPm() {
        return pm;
    }

    public void setPm(PostMethod pm) {
        this.pm = pm;
    }

    public boolean isSuppressGui() {
        return suppressGui;
    }

    public void setSuppressGui(boolean suppressGui) {
        this.suppressGui = suppressGui;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNumElements() {
        return numElements;
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;
    }

    @Override
    public String toString() {
        String s = "";
        s += "url: " + url;
        s += "\nmethod: " + method;
        s += "\nwait: " + wait + "\n";
        return s;
    }
}
