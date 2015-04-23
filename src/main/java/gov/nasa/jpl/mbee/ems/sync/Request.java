package gov.nasa.jpl.mbee.ems.sync;

import org.apache.commons.httpclient.methods.PostMethod;

public class Request {

    private String json = "";
    private String url = "";
    private String method = "POST";
    private boolean feedback = false;
    private PostMethod pm = null;
    private boolean suppressGui = false;
    private int wait = 60000;
    
    public Request(String url, String json, String method, boolean feedback) {
        this.url = url;
        this.json = json;
        this.method = method;
        this.feedback = feedback;
        this.suppressGui = !feedback;
    }
    
    public Request(String url, String json, String method, boolean feedback, int wait) {
        this.url = url;
        this.json = json;
        this.method = method;
        this.feedback = feedback;
        this.suppressGui = !feedback;
        this.wait = wait*1000 + 120000;
    }
    
    public Request(String url, String json, String method) {
        this.url = url;
        this.json = json;
        this.method = method;
    }
    
    public Request(String url, String json) {
        this.url = url;
        this.json = json;
    }
    
    public Request(String url, String json, int wait) {
        this.url = url;
        this.json = json;
        this.wait = wait*1000 + 120000;
    }
    
    public Request(String url, PostMethod pm) {
        this.pm = pm;
        this.url = url;
    }
    
    public Request() {}
    
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
    
    @Override
    public String toString() {
        String s = "";
        s += "url: " + url;
        s += "\nmethod: " + method;
        s += "\nwait: " + wait + "\n";
        return s;
    }
}
