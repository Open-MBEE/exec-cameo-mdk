package gov.nasa.jpl.mbee;

/**
 * Simple interface for any embedded web server that is started in
 * {@link gov.nasa.jpl.mbee.DocGenPlugin}
 * 
 * @author cinyoung
 * 
 */
public interface DocGenEmbeddedServer {
    public void setPort(int port);

    public void setup() throws Throwable;

    public void teardown() throws Throwable;
}
