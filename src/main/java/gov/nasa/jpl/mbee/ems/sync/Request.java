package gov.nasa.jpl.mbee.ems.sync;

public class Request {

    private String json;
    private String url;
    private String method = "POST";
    
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
}
