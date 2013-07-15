package gov.nasa.jpl.mgss.mbee.docgen;

import gov.nasa.jpl.mbee.servlet.ModelServlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;

import com.nomagic.magicdraw.utils.MDLog;

/**
 * Tomcat embedded webserver
 * @author cinyoung
 *
 */
public class DocGenEmbeddedTomcatServer implements DocGenEmbeddedServer {
	private static Tomcat					tomcat 		= null;
	private static int						PORT		= 8080;
	
	public DocGenEmbeddedTomcatServer() {
	}
		
	@Override
	public void setPort(int port) {
		tomcat.setPort(port);
	}

	/**
	 * Setup utility for configuring the embedded Tomcat instance, we'll need to parameterize this
	 * 
	 * Currently things that are required are (from http://tomcat.apache.org/download-70.cgi - embedded):
	 * tomcat-embed/	
	 *   tomcat-embed-core.jar (7.0.16)
	 *     - Core tomcat classes that contains server
	 *   tomcat-embed-logging-log4j.jar
	 *     - Log4j logging library integration
	 *   tomcat-embed-jasper.jar
	 *     - Tomcat Jasper engine for serving JSP
     *  slf4j-api-1.7.2.jar/slf4j-log4j12-1.7.2
     *    - SLF4j required for logging with Spring 3.0 - needs LocationAwareLogger
     *  
     *  NOTE: Since there are different versions of the servlet-api already loaded, the plugin
     *        needs to be configured with a private class loader. This is done by specifying
     *        ownClassLoader="true" and class-lookup="LocalFirst"
     *  
	 * @throws Throwable
	 */
	@Override
	public void setup() throws Throwable {
		String currentDir = new File(".").getCanonicalPath();

		// Tomcat directory simply for logging
		String tomcatDir  = currentDir + File.separatorChar + "tomcat";    

		tomcat = new Tomcat();
		tomcat.setPort(PORT);
		tomcat.setBaseDir(tomcatDir);
		
// Can use web.xml for startup - in WEB-INF directory of webRoot
// Spring doesn't load properly in plugin, so do everything programmatically
//		boolean runSesame = false;
//		if (runSesame) {
//			String webRoot    = "/Users/cinyoung/workspaces/yoxos_workspace/embedded/webapp"; // path to the expanded web archive directory (it looks for web.xml file)
//			tomcat.addWebapp("/editor", webRoot);
//		} else {
//			String webRoot    = "/Users/cinyoung/workspaces/yoxos_workspace/embedded/src/main/resources"; // path to the expanded web archive directory (it looks for web.xml file)
//			tomcat.addWebapp("/", webRoot);
//		}
//		
		
	    Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());  
	    Tomcat.addServlet(ctx, "model", new ModelServlet());
	    ctx.addServletMapping("/*", "model"); 
	    
		tomcat.start();
		// no need to wait for session
	}

	/**
	 * Tear down the Tomcat instance cleanly
	 * @throws Throwable
	 */
	@Override
	public void teardown() throws Throwable {
		if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
			if (tomcat.getServer().getState() != LifecycleState.STOPPED){
				tomcat.stop();
			}
			tomcat.destroy();
		}
	}

	// test code for printing out all the URLs of the classloaders
	private static void printURLs(ClassLoader classLoader) {
		if (classLoader != null) {
			System.out.println("ClassLoader:" + classLoader);
			if (classLoader instanceof URLClassLoader) {
				URL[] urls = ((URLClassLoader) classLoader).getURLs();
				for (int i = 0; i < urls.length; ++i) {
					System.out.println(urls[i].toExternalForm());
				}
			}
			printURLs(classLoader.getParent());
		}
	}
	
	// test method for finding the URLs that provide the specified class
	private static void findClass(ClassLoader classLoader, String className) {
		if (classLoader != null) {
			System.out.println("ClassLoader:" + classLoader);
			if (classLoader instanceof URLClassLoader) {
				URL[] urls = ((URLClassLoader) classLoader).getURLs();
				for (int i = 0; i < urls.length; ++i) {
					try {
						String jarFilename = urls[i].getFile();
//						MDLog.getPluginsLog().error("Loading jar file " + jarFilename);
						JarFile jar = new JarFile(jarFilename);
						// entries are "/" separated rather than "."
						JarEntry entry = jar.getJarEntry(className.replace(".", "/") + ".class");
						if (entry != null) {
							MDLog.getPluginsLog().error("Loaded from jar: " + jarFilename + " => class: " + className);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
//						return;
					}
				}
			}
			findClass(classLoader.getParent(), className);
		}
	}
}

