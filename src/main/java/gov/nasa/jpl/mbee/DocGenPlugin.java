/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p>
 * - Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory,
 * nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.ems.sync.AutoSyncStatusConfigurator;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueueStatusConfigurator;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputSyncRunner;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.patternloader.PatternLoaderConfigurator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.evaluation.EvaluationConfigurator;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.uml.DiagramTypeConstants;

public class DocGenPlugin extends Plugin {
    private OclEvaluatorPlugin oclPlugin = null;
    private ValidateConstraintsPlugin vcPlugin = null;
    public static ClassLoader extensionsClassloader = null;

    public static final String VERSION = "2.3";

    public DocGenPlugin() {
        super();
        Debug.outln("constructed DocGenPlugin!");
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void init() {
        ActionsConfiguratorsManager acm = ActionsConfiguratorsManager.getInstance();
        System.setProperty("jsse.enableSNIExtension", "false");
        DocGenConfigurator dgc = new DocGenConfigurator();
        acm.addContainmentBrowserContextConfigurator(dgc);
        acm.addSearchBrowserContextConfigurator(dgc);
        acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, dgc);
        //acm.addBaseDiagramContextConfigurator("Class Diagram", dgc);
        //acm.addBaseDiagramContextConfigurator("Activity Diagram", dgc);
        //acm.addBaseDiagramContextConfigurator("SysML Package Diagram", dgc);

        PatternLoaderConfigurator plc = new PatternLoaderConfigurator();
        acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, plc);
        acm.addMainMenuConfigurator(new MMSConfigurator());
        EvaluationConfigurator.getInstance().registerBinaryImplementers(DocGenPlugin.class.getClassLoader());

        SRConfigurator srconfig = new SRConfigurator();
        acm.addSearchBrowserContextConfigurator(srconfig);
        acm.addContainmentBrowserContextConfigurator(srconfig);
        acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, srconfig);

        acm.addMainToolbarConfigurator(new OutputQueueStatusConfigurator());
        acm.addMainToolbarConfigurator(new AutoSyncStatusConfigurator());

        getOclPlugin().init();
        getVcPlugin().init();
        MMSSyncPlugin.getInstance().init();
        (new Thread(new OutputSyncRunner())).start();
        //ApplicationSyncEventSubscriber.subscribe(); //really old docweb sync, should remove related code

        loadExtensionJars(); // people can actually just create a new plugin and

        Application.getInstance().getEnvironmentOptions().addGroup(new MDKOptionsGroup());
    }

    public OclEvaluatorPlugin getOclPlugin() {
        if (oclPlugin == null) {
            oclPlugin = new OclEvaluatorPlugin();
        }
        return oclPlugin;
    }

    public ValidateConstraintsPlugin getVcPlugin() {
        if (vcPlugin == null) {
            vcPlugin = new ValidateConstraintsPlugin();
        }
        return vcPlugin;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    private void loadExtensionJars() {
        File extensionDir = new File(getDescriptor().getPluginDirectory(), "extensions");
        if (!extensionDir.exists()) {
            extensionsClassloader = DocGenPlugin.class.getClassLoader();
            return;
        }
        List<URL> extensions = new ArrayList<URL>();
        try {
            extensions.add(extensionDir.toURI().toURL());
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for (File file : extensionDir.listFiles()) {
            try {
                @SuppressWarnings("unused")
                JarFile jarFile = new JarFile(file);
                extensions.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        extensionsClassloader = new URLClassLoader(extensions.toArray(new URL[]{}),
                DocGenPlugin.class.getClassLoader());
    }
}
