/*
 * Copyright 2010, by the California Institute of Technology. ALL RIGHTS RESERVED. 
 * United States Government Sponsorship acknowledged. Any commercial use must be negotiated 
 * with the Office of Technology Transfer at the California Institute of Technology.

 * This software may be subject to U.S. export control laws. By accepting this software, 
 * the user agrees to comply with all applicable U.S. export laws and regulations. 
 * User has the responsibility to obtain export licenses, or other export authority 
 * as may be required before exporting such information to foreign countries or 
 * providing access to foreign persons.
 */

package gov.nasa.jpl.mgss.mbee.docgen;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import gov.nasa.jpl.magicdraw.qvto.QVTOUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.patternloader.PatternLoaderConfigurator;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.DgviewPackage;
import gov.nasa.jpl.mgss.mbee.docgen.sync.ApplicationSyncEventSubscriber;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.evaluation.EvaluationConfigurator;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.uml.DiagramTypeConstants;

public class DocGenPlugin extends Plugin {
	// Variables for running embedded web server for exposing services
	private DocGenEmbeddedServer	embeddedServer;
	private boolean					runEmbeddedServer = false;
	protected OclEvaluatorPlugin oclPlugin = null;
	public static URLClassLoader extensionsClassloader = null;
	
	public DocGenPlugin() {
	  super();
	  Debug.outln( "constructed DocGenPlugin!" );
	}
	
	@Override
	public boolean close() {
		if (runEmbeddedServer) {
			try {
				embeddedServer.teardown();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public void init() {
		ActionsConfiguratorsManager acm = ActionsConfiguratorsManager.getInstance();
		
		DocGenConfigurator dgc = new DocGenConfigurator();
		acm.addContainmentBrowserContextConfigurator(dgc);
		acm.addBaseDiagramContextConfigurator("Class Diagram", dgc);
		acm.addBaseDiagramContextConfigurator("Activity Diagram", dgc);
		acm.addBaseDiagramContextConfigurator("SysML Package Diagram", dgc);
		
		PatternLoaderConfigurator plc = new PatternLoaderConfigurator();
		acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, plc);
		
		EvaluationConfigurator.getInstance().registerBinaryImplementers(DocGenPlugin.class.getClassLoader());

    getOclPlugin().init();

		ApplicationSyncEventSubscriber.subscribe();
		
		getEmbeddedSystemProperty();
		if (runEmbeddedServer) {
			try {
				embeddedServer = new DocGenEmbeddedTomcatServer();
				embeddedServer.setup();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		QVTOUtils.loadMetamodelPackage(DgviewPackage.class);
		QVTOUtils.loadMetamodelPackage(DgvalidationPackage.class);
		loadExtensionJars(); //people can actaully just create a new plugin and let magicdraw's classloader load it?
		//QVTOUtils.registerMetamodel("http:///gov/nasa/jpl/mgss/mbee/docgen/dgview.ecore", "gov.nasa.jpl.mgss.mbee.docgen.dgview.DgviewFactory");
	}

	public OclEvaluatorPlugin getOclPlugin() {
	  if ( oclPlugin == null ) {
	    oclPlugin = new OclEvaluatorPlugin();
	  }
	  return oclPlugin;
	}
	
	@Override
	public boolean isSupported() {
		return true;
	}

	/**
	 * Overrides the embedded server flag based on system property being set.
	 */
	private void getEmbeddedSystemProperty() {
		String embedded = System.getProperty("mdk.embeddedserver");
		if (embedded != null) {
			if (embedded.equalsIgnoreCase("true")) {
				runEmbeddedServer = true;
			} else if (embedded.equalsIgnoreCase("false")) {
				runEmbeddedServer = false;
			}
		}
	}
	
	private void loadExtensionJars() {
	    File extensionDir = new File(getDescriptor().getPluginDirectory(), "extensions");
	    if (!extensionDir.exists())
	        return;
	    List<URL> extensions = new ArrayList<URL>();
	  
        for (File file : extensionDir.listFiles()) {
            try {
                extensions.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }  
        }
        extensionsClassloader = new URLClassLoader(extensions.toArray(new URL[]{}), DocGenPlugin.class.getClassLoader());
	}
}
