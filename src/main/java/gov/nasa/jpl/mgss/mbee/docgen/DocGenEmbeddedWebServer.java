package gov.nasa.jpl.mgss.mbee.docgen;

import java.io.File;

import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;

/**
 * Singleton class for embedded web server
 * 
 * @author cinyoung
 * 
 */
@Deprecated
public class DocGenEmbeddedWebServer {
    private static DocGenEmbeddedWebServer INSTANCE = null;
    private static Tomcat                  tomcat   = null;
    private static int                     PORT     = 8080;

    private DocGenEmbeddedWebServer() {
        // prevent instantiation
    }

    public static DocGenEmbeddedWebServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DocGenEmbeddedWebServer();
        }
        return INSTANCE;
    }

    public void setPort(int port) {
        tomcat.setPort(port);
    }

    /**
     * Setup utility for configuring the embedded Tomcat instance, we'll need to
     * parameterize this
     * 
     * Currently things that are required are (from
     * http://tomcat.apache.org/download-70.cgi - embedded): tomcat-embed/
     * tomcat-embed-core.jar (7.0.16) - Core tomcat classes that contains server
     * tomcat-embed-logging-log4j.jar - Log4j logging library integration
     * tomcat-embed-jasper.jar - Tomcat Jasper engine for serving JSP
     * tomcat-7.0/ servlet-api.jar (2.5 or later, using 3.0) - Servlet jar
     * jsp-api.jar - for JSP support when using spring
     * slf4j-api-1.7.2.jar/slf4j-log4j12-1.7.2 - SLF4j required for logging with
     * Spring 3.0 - needs LocationAwareLogger
     * 
     * TODO: There are issues with the existing MD classpath that is used for
     * the IMCE-CI build that need to be resolved. In otherwords the JARs listed
     * below need to be removed from the classpath.
     * 
     * MD_HOME/ lib/ jboss* Interferes with the servlet APIs slf4j* plugins/
     * com.nomagic.magicdraw.automaton/engines/groovy-1.7.10/lib
     * servlet-api-2.4.jar jsp-api-2.0.jar
     * 
     * @throws Throwable
     */
    @SuppressWarnings("serial")
    public void setupTomcat() throws Throwable {
        String currentDir = new File(".").getCanonicalPath();
        // Tomcat directory simply for logging
        String tomcatDir = currentDir + File.separatorChar + "tomcat";
        String webRoot = "/Users/cinyoung/workspaces/yoxos_workspace/embedded/webapp"; 
        tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(tomcatDir);
        tomcat.addWebapp("/editor", webRoot);

        tomcat.start();
    }

    /**
     * Tear down the Tomcat instance cleanly
     * 
     * @throws Throwable
     */
    public void teardownTomcat() throws Throwable {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }

}
