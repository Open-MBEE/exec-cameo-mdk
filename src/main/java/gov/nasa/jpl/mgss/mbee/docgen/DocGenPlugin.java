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

import gov.nasa.jpl.magicdraw.qvto.QVTOUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.stylesaver.Configurator;
import gov.nasa.jpl.mbee.stylesaver.DiagramUtils;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.DgviewPackage;
import gov.nasa.jpl.mgss.mbee.docgen.sync.ApplicationSyncEventSubscriber;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.evaluation.EvaluationConfigurator;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.uml.DiagramTypeConstants;

import java.lang.reflect.*;

public class DocGenPlugin extends Plugin {
	// Variables for running embedded web server for exposing services
	private DocGenEmbeddedServer	embeddedServer;
	private boolean					runEmbeddedServer = false;
	protected OclEvaluatorPlugin oclPlugin = null;
	
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
		
    Configurator styleConfigurator = getStyleConfigurator();
    acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, styleConfigurator);

    EvaluationConfigurator.getInstance().registerBinaryImplementers(DocGenPlugin.class.getClassLoader());
		
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
		//QVTOUtils.registerMetamodel("http:///gov/nasa/jpl/mgss/mbee/docgen/dgview.ecore", "gov.nasa.jpl.mgss.mbee.docgen.dgview.DgviewFactory");
		
		getOclPlugin().init();
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
	
	/**
	 * Gets the configurator for the style saver/loader.
	 * 
	 * @return the configurator for the saver/loader.
	 */
	private Configurator getStyleConfigurator() {
		Configurator c = new Configurator();
		
		Method addSaveMethod = DiagramUtils.class.getDeclaredMethods()[0];
		Method addLoadMethod = DiagramUtils.class.getDeclaredMethods()[1];
		
		c.addConfiguration("BaseDiagramContext", "", "Save styling on diagram", "Style Saver/Loader", addSaveMethod);
		c.addConfiguration("BaseDiagramContext", "", "Load saved styling onto diagram", "Style Saver/Loader", addLoadMethod);

		return c;
	}
}
