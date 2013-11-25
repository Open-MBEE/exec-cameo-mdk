/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
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
package gov.nasa.jpl.mbee.lib;

import gov.nasa.jpl.imce.owl2.Ontology;
import gov.nasa.jpl.imce.owl2.api.converters.OWL2XMIExportToOWL2;
import gov.nasa.jpl.imce.owl2.metadata.OWL2Package;
import gov.nasa.jpl.imce.owl2.util.CatalogResolvingResourceSetOWLOntologyIRIMapper;
import gov.nasa.jpl.imce.owl2.util.OWL2Resource;
import gov.nasa.jpl.imce.owl2.util.OWL2Util;
import gov.nasa.jpl.magicdraw.qvto.QVTOUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.m2m.internal.qvt.oml.ast.env.ModelExtentContents;
import org.eclipse.m2m.internal.qvt.oml.ast.env.ModelParameterExtent;
import org.eclipse.m2m.internal.qvt.oml.ast.env.QvtOperationalEnv;
import org.eclipse.m2m.internal.qvt.oml.ast.env.QvtOperationalEvaluationEnv;
import org.eclipse.m2m.internal.qvt.oml.evaluator.ModelInstance;
import org.eclipse.m2m.internal.qvt.oml.evaluator.TransformationInstance;
import org.eclipse.m2m.internal.qvt.oml.expressions.ModelParameter;
import org.eclipse.m2m.internal.qvt.oml.expressions.OperationalTransformation;
import org.eclipse.m2m.qvt.oml.BasicModelExtent;
import org.eclipse.m2m.qvt.oml.ExecutionContext;
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl;
import org.eclipse.xtext.util.Strings;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.modules.ReadOnlyModuleException;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.qvt.jsr223.CanceledQVTOTransformationException;
import com.nomagic.magicdraw.qvt.jsr223.QVTEngineConstants;
import com.nomagic.magicdraw.qvt.jsr223.QVTORuntimeException;
import com.nomagic.magicdraw.qvt.jsr223.QVTOTransformationURIException;
import com.nomagic.magicdraw.qvt.jsr223.QVTScriptEngineFactory;
import com.nomagic.magicdraw.qvt.proxy.QVTScriptingEngineLogFactory;
import com.nomagic.magicdraw.ui.DiagramWindowPanel;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.BrowserTabTree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * Modified from Nick's QVTScriptRunner (as of 4/20/2013) to work with DocGen,
 * instead of extending Groovy, users don't need a groovy script but just needs
 * the qvto and qvtox compiled file in DocGenUserScript directory, similar to
 * how jython or groovy user scripts are done. DocGenTargets are set as
 * "selectedElements" input, the model itself is passed as "currentModel", any
 * other stereotype tags are passed as configuration properties.
 * 
 * Original description from Nick: This class extends Groovy's Script class to
 * provide reusable mechanisms for launching QVTO transformations as
 * JSR233-compatible scripts.
 * 
 * <p>
 * There are two important conventions:
 * 
 * 1) A QVTO transformation <T> must be located as a file below a relative path
 * specified by: getQVTORelativeDirectoryPathSegments(). By default,
 * getQVTORelativeDirectoryPathSegments() = "transforms".
 * 
 * If you need to change this convention, override the method to specify a
 * different relative path to search for QVTO transformation files.
 * 
 * @see com.nomagic.magicdraw.qvt.QVTOLaunchScript.getQVToTransformationScript()
 * 
 *      2) The name of the QVTO transformation that a Groovy script specializing
 *      QVTOLaunchScript must have the same basename as the groovy script. That
 *      is, a Groovy script <T>.groovy will match a QVTO transformation:
 *      [(<T>.groovy)]/getQVTORelativeDirectoryPathSegments()/<T>.qvto
 *      [(<T>.groovy)]/../getQVTORelativeDirectoryPathSegments()/<T>.qvto
 *      [(<T>.groovy)]/../../getQVTORelativeDirectoryPathSegments()/<T>.qvto ..
 *      and so on.
 * 
 *      To write the Groovy QVTO launch script, it is necessary to define:
 * 
 * @see 
 *      com.nomagic.magicdraw.qvt.QVTOLaunchScript.configureTransformationParameters
 *      ()
 * 
 *      If the QVTO transformation has "out" parameters, the following method
 *      must be defined:
 * 
 * @see com.nomagic.magicdraw.qvt.QVTOLaunchScript.saveOutputParameters()
 * 
 *      In some special cases, it may be useful to override the following
 *      methods:
 * 
 *      configureQVTEngineExecutionContext(); createResourceSet();
 * 
 *      In the MD environment, the MD automaton and qvt plugins are loaded in
 *      MD's default plugin classloader There are problems accessing clases
 *      defined in the qvt plugin; why, this is a real classloader mystery.
 * @see https://support.nomagic.com/browse/MDUMLCS-2667
 * 
 *      DMME-488: Cleanup ResourceSet mess in QVTO
 *      https://imce-env.jpl.nasa.gov/jira/browse/DMME-488 Add support for
 *      extending the Catalog-resolving ResourceSet with a RewriteURI
 *      CatalogEntry. Add support for extending the Catalog-resolving
 *      ResourceSet with a dynamically-defined, javaless EPackage (metamodel,
 *      profile, schema)
 * 
 * @author nicolas.f.rouquette@jpl.nasa.gov
 */
public class QVTScriptRunner {

    protected final SessionManager       sm  = SessionManager.getInstance();
    protected final ModelElementsManager mem = ModelElementsManager.getInstance();
    protected final Application          a   = Application.getInstance();
    protected final GUILog               log = a.getGUILog();
    protected final StringWriter         sw;
    protected final PrintWriter          pw;
    protected final Logger               qvtoLogger;

    protected Map<String, Object>        inputs;
    protected File                       script;
    protected File[]                     binDirs;

    public QVTScriptRunner(Map<String, Object> inputs, File script, File[] binDirs) {
        this.sw = createStringWriter();
        this.pw = createPrintWriter();
        this.qvtoLogger = createQVTOLogger();
        this.inputs = inputs;
        this.script = script;
        this.binDirs = binDirs;
        this.transformationURI = URI.createFileURI(script.getAbsolutePath());
    }

    protected StringWriter createStringWriter() {
        return new StringWriter();
    }

    protected PrintWriter createPrintWriter() {
        return new PrintWriter(sw);
    }

    protected Logger createQVTOLogger() {
        return QVTScriptingEngineLogFactory.getQVTOLogger();
    }

    protected void showException(String message, Exception e) {
        e.fillInStackTrace();

        sw.getBuffer().setLength(0);
        sw.append("**** " + e.getClass().getCanonicalName());
        sw.append(e.getMessage());
        if (message != null && message.length() > 0)
            sw.append("*** " + message);
        sw.append(Strings.newLine());
        e.printStackTrace(pw);

        log.openLog();
        log.log(sw.toString());

        qvtoLogger.error(sw.toString());
        sw.getBuffer().setLength(0);
    }

    protected void showMessage(String message) {
        log.openLog();
        log.log(message);
        qvtoLogger.info(message);
    }

    protected void showError(String message) {
        log.openLog();
        log.log(message);
        qvtoLogger.error(message);
    }

    protected static String QVTOX_FILE_EXTENSION = "qvtox";

    /**
     * @pre this.getProperty("javax.script.filename") must be the path to the
     *      Groovy script.
     * 
     *      sets qvtoSourceFile and qvtoxCompiledFile accordingly.
     */
    protected void getQVToTransformationScript() {
        qvtoxCompiledFile = script.getAbsolutePath();
    }

    protected String       qvtoxCompiledFile;

    /**
     * Use this attribute to set the values for each QVTO transformation
     * parameter. In general, if a transformation T has parameters:
     * T(&lt;direction> &lt;parameter>:&lt;type>, ...) then the Groovy launch
     * script for T must configure each parameter as follows:
     * qvtoEngine.put("&lt;parameter>", &lt;value>);
     * <p>
     * &lt;value> can be one of the following:
     * <p>
     * <ul>
     * <li>"md:model" if the parameter value is intended to get to the currently
     * active MagicDraw project model;</li>
     * <li>"md:currentElement" if the parameter value is intended to get the
     * currently selected element in a diagram (if any) or in the browser (if
     * any), or none. Note that "md:currentElement" will get the first selected
     * element if multiple elements are selected.</li>
     * <li>"md:currentSelection" if the parameter value is intended to get the
     * collection of all selected elements in the diagram (if any) and in the
     * browser (if any), or none.</li>
     * <li>null, if the parameter direction in the QVTO transformation is "out"</li>
     * </ul>
     */
    protected ScriptEngine qvtoEngine;

    /**
     * @pre null != qvtoFile
     * @pre null == qvtoEngine
     * @post qvtoEngine instanceof ScriptEngine
     * 
     *       Create a JSR233-compatible QVTO scripting engine for qvtoFile.
     */
    public ScriptEngine createQVTOScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        qvtoEngine = manager.getEngineByName(QVTScriptEngineFactory.SHORT_NAME);
        if (null == qvtoEngine) {
            throw new RuntimeException("QVTPlugin -- could not find JSR233 engine for: "
                    + QVTScriptEngineFactory.SHORT_NAME);
        }
        qvtoEngine.put(ScriptEngine.FILENAME, script.getAbsolutePath());
        return qvtoEngine;
    }

    public ScriptEngine getQVTOEngine() {
        return qvtoEngine;
    }

    /**
     * @pre null != qvtoEngine
     * @post result = qvtoEngine.getContext().getAttribute(QVTEngineConstants.
     *       QVTO_EXECUTION_CONTEXT, ScriptContext.ENGINE_SCOPE)
     * 
     *       Configures the qvtoEngine's execution context with a QVTo log
     *       routed to MagicDraw's GUILog (i.e. message window) Override this
     *       method to remove the QVTo log or route it elsewhere.
     */
    public ExecutionContextImpl configureQVTEngineExecutionContext() {
        ExecutionContextImpl context = new ExecutionContextImpl();
        for (Map.Entry<String, Object> entry: inputs.entrySet()) {
            if (entry.getKey().equals("DocGenTargets"))
                continue;
            context.setConfigProperty(entry.getKey(), entry.getValue());
        }
        // put in non model stereotype tag values here as config params - to qvt
        // config properties
        ScriptContext sc = qvtoEngine.getContext();
        sc.setAttribute(QVTEngineConstants.QVTO_EXECUTION_CONTEXT, context, ScriptContext.ENGINE_SCOPE);
        sc.setAttribute(QVTEngineConstants.QVTO_SCRIPT_ENGINE_LOG_FACTORY_NAME,
                "com.nomagic.magicdraw.qvt.proxy.QVTScriptingEngineLogFactory", ScriptContext.ENGINE_SCOPE);
        sc.setAttribute(QVTEngineConstants.QVTO_SCRIPT_EXECUTION_JUNIT_RESULT_LOG_FACTORY_NAME,
                "com.nomagic.magicdraw.qvt.proxy.QVTScriptingExecutionJUnitResultLogFactory",
                ScriptContext.ENGINE_SCOPE);
        return context;
    }

    protected File findGeneratedBundleDirectory(File dir) {
        Assert.isTrue(dir.exists() && dir.isDirectory());
        final String[] generated = {null};
        dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File d, String name) {
                if (name.matches("generated")) {
                    Assert.isTrue(null == generated[0]);
                    generated[0] = name;
                    return true;
                }
                return false;
            }
        });
        Assert.isNotNull(generated[0]);
        File generatedDir = new File(dir.getAbsoluteFile() + File.separator + generated[0]);
        Assert.isTrue(generatedDir.exists() && generatedDir.isDirectory() && generatedDir.canRead());
        final String[] buildNumber = {null};
        generatedDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File d, String name) {
                if (name.matches("[1-9][0-9]+") || "latest".equals(name)
                        || "lastSuccessfulBuild".equals(name)) {
                    Assert.isTrue(null == buildNumber[0]);
                    buildNumber[0] = name;
                    return true;
                }
                return false;
            }
        });
        Assert.isNotNull(buildNumber[0]);
        File bundleDir = new File(generatedDir.getAbsoluteFile() + File.separator + buildNumber[0]);
        return bundleDir;
    }

    public CatalogManager getCatalogManager() {
        CatalogResolvingResourceSetOWLOntologyIRIMapper _rs = getResourceSet();
        return _rs.getCatalogManager();
    }

    public CatalogResolver getCatalogResolver() {
        CatalogResolvingResourceSetOWLOntologyIRIMapper _rs = getResourceSet();
        return _rs.getCatalogResolver();
    }

    public Catalog getCatalog() {
        CatalogResolvingResourceSetOWLOntologyIRIMapper _rs = getResourceSet();
        return _rs.getCatalog();
    }

    public void parseCatalog(URL catalogURL) {
        try {
            getCatalog().parseCatalog(catalogURL);
        } catch (IOException e) {
            throw new QVTORuntimeException(
                    "Failed to parse catalog: " + catalogURL + " -- " + e.getMessage(), e);
        }
    }

    public void parseCatalog(String catalogPath) {
        try {
            getCatalog().parseCatalog(catalogPath);
        } catch (MalformedURLException e) {
            throw new QVTORuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new QVTORuntimeException(e.getMessage(), e);
        }
    }

    public String getLocalOntologyRoot() {
        return ApplicationEnvironment.getInstallRoot() + File.separator + "ontologies";
    }

    protected URI        transformationURI;

    public static String IMCE_LOCAL_CATALOG = "imce.local.catalog.xml";

    /**
     * @param catalogRelativePath
     *            based on <install.root>/ontologies; e.g.,
     *            QVTOLaunchScript.IMCE_LOCAL_CATALOG
     */
    public void parseLocalOntologyCatalog(String catalogRelativePath) {
        parseCatalog(getLocalOntologyRoot() + File.separator + catalogRelativePath);
    }

    protected CatalogResolvingResourceSetOWLOntologyIRIMapper rs = null;

    /**
     * Override this method to create a particular kind of ResourceSet.
     * 
     * @throws IOException
     * 
     * @pre null == rs
     * @post rs instanceof ResourceSet
     */
    public void createResourceSet() {
        if (rs == null) {
            try {
                rs = new CatalogResolvingResourceSetOWLOntologyIRIMapper();
                rs.setLog(QVTScriptingEngineLogFactory.makeQVTLog());
            } catch (IOException e) {
                throw new QVTORuntimeException("Cannot create the catalog-resolving ResourceSet: "
                        + e.getMessage(), e);
            }
        }

        ScriptContext sc = qvtoEngine.getContext();
        sc.setAttribute(QVTEngineConstants.QVTO_RESOURCE_SET, this.rs, ScriptContext.ENGINE_SCOPE);
    }

    /**
     * @return a non-null CatalogResolvingResourceSetOWLOntologyIRIMapper
     * @throws QVTORuntimeException
     *             if there is no such resource set.
     */
    public CatalogResolvingResourceSetOWLOntologyIRIMapper getResourceSet() {
        if (null == rs)
            createResourceSet();
        if (null == rs)
            throw new QVTORuntimeException("There should be a non-null ResourceSet");

        return rs;
    }

    /**
     * Add to the OASIS XML Catalog a new RewriteURI CatalogEntry of the form:
     * 
     * <rewriteURI uriStartString="[fromURIStartString]"
     * rewritePrefix="[toRewritePrefix]" />
     * 
     * @param fromURIStartString
     * @param toRewritePrefix
     */
    public void addRewriteURICatalogEntry(String fromURIStartString, String toRewritePrefix) {
        Vector<String> args = new Vector<String>();
        args.add(fromURIStartString);
        args.add(toRewritePrefix);
        CatalogEntry ce = null;
        try {
            ce = new CatalogEntry(Catalog.REWRITE_URI, args);
        } catch (CatalogException e) {
            throw new QVTORuntimeException("addRewriteURICatalogEntry(uriStartString='" + fromURIStartString
                    + "', rewritePrefix='" + toRewritePrefix + "') : " + e.getMessage());
        }
        rs.getCatalog().addEntry(ce);
    }

    public EPackage loadEPackage(String packageURI) {
        URI uri = URI.createURI(packageURI);
        Resource r = rs.getResource(uri, true);
        if (null == r)
            throw new QVTORuntimeException("loadEPackage(packageURI='" + packageURI + "') : not found!");

        EList<EObject> contents = r.getContents();
        if (contents.size() != 1)
            throw new QVTORuntimeException("loadEPackage(packageURI='" + packageURI
                    + "') : the Resource should have a single toplevel EPackage!");

        EObject top = contents.get(0);
        if (!(top instanceof EPackage))
            throw new QVTORuntimeException("loadEPackage(packageURI='" + packageURI
                    + "') : the Resource should have a single toplevel EPackage!");

        EPackage ePkg = (EPackage)top;
        return ePkg;
    }

    public void registerEPackage(EPackage ePkg) {
        String nsURI = ePkg.getNsURI();
        if (null == nsURI || nsURI.length() == 0)
            throw new QVTORuntimeException("registerEPackage() : the EPackage must have a non-null nsURI!");

        EcoreUtil.resolveAll(ePkg);

        String message = "";
        if (rs.getPackageRegistry().containsKey(nsURI)) {
            message = "(overwrite) ";
        }
        message += " Registering EPacakge with nsURI=" + nsURI;
        showMessage(message);
        rs.getPackageRegistry().put(nsURI, ePkg);
    }

    public URIConverter getResourceSetURIConverter() {
        if (null == rs)
            throw new QVTORuntimeException(
                    "The ResourceSet is not created yet; perhaps this method is called before createResourceSet()");

        URIConverter uriConverter = rs.getURIConverter();
        return uriConverter;
    }

    public Map<URI, URI> getResourceSetURIConverterMap() {
        URIConverter uriConverter = getResourceSetURIConverter();
        Map<URI, URI> uriMap = uriConverter.getURIMap();
        return uriMap;
    }

    protected BasicModelExtent getResourceModelExtent(URI resourceURI) {
        showMessage("resource URI: " + resourceURI);
        Resource r = rs.getResource(resourceURI, true);
        if (null == r)
            throw new QVTORuntimeException("Cannot find resource from URI: " + resourceURI);

        showMessage("Resolving resource: " + r.getURI());
        EcoreUtil.resolveAll(r.getResourceSet());
        EList<Resource.Diagnostic> errors = r.getErrors();
        if (!errors.isEmpty()) {
            for (Resource.Diagnostic error: errors) {
                showError(error.toString());
            }
            throw new QVTORuntimeException("Errors in resolving the resource from URI: " + resourceURI);
        }
        BasicModelExtent extent = new BasicModelExtent(r.getContents());
        return extent;
    }

    protected BasicModelExtent wrapInModelExtent(Element e) {
        return new BasicModelExtent(Collections.singletonList(e));
    }

    public void prependURIHandler(URIHandler uriHandler) {
        URIConverter uriConverter = getResourceSetURIConverter();
        uriConverter.getURIHandlers().add(0, uriHandler);
    }

    public Object run() {
        getQVToTransformationScript();

        createQVTOScriptEngine();

        createResourceSet();

        {
            ExecutionContextImpl context_ = configureQVTEngineExecutionContext();
            if (null == context_)
                throw new CanceledQVTOTransformationException(transformationURI);
            ScriptContext sc = qvtoEngine.getContext();
            if (null == sc)
                throw new QVTOTransformationURIException("QVTEngine has null context!", transformationURI);

            sc.setAttribute(QVTEngineConstants.QVTO_SCRIPT_IS_COMPILED, true, ScriptContext.ENGINE_SCOPE);
            ExecutionContext executionContext = (ExecutionContext)sc.getAttribute(
                    QVTEngineConstants.QVTO_EXECUTION_CONTEXT, ScriptContext.ENGINE_SCOPE);
            if (!context_.equals(executionContext))
                throw new QVTOTransformationURIException(
                        "QVTOLaunchScript: post-condition violation in configureQVTEngineExecutionContext()",
                        transformationURI);
        }

        configureTransformationParameters();

        Project project = Application.getInstance().getProjectsManager().getActiveProject();
        DiagramPresentationElement diagram = (null == project) ? null : project.getActiveDiagram();
        DiagramWindowPanel panel = (null == diagram) ? null : diagram.getPanel();
        Boolean diagramHasFocus = (null == panel) ? false : panel.getDrawArea().getCanvas().isFocusOwner();

        Browser b = (null == project) ? null : project.getBrowser();
        BrowserTabTree tab = (null == b) ? null : b.getActiveTree();
        Boolean browserHasFocus = (null == tab) ? false : tab.getTree().isFocusOwner();

        qvtoEngine.put("DIAGRAM_HAS_FOCUS", diagramHasFocus);
        qvtoEngine.put("BROWSER_HAS_FOCUS", browserHasFocus);

        // final String SUBTASK_PREFIX = (true) ? "Executing" : "Compiling";
        // final String WORKED_PREFIX = (true) ? "[QVTOX]" : "[QVTO]";

        /*
         * ProgressStatusRunner.runWithProgressStatus(new RunnableWithProgress()
         * { public void run(final ProgressStatus progressStatus) { try { final
         * EvaluationMonitor qvtoEvaluationMonitor = new EvaluationMonitor() {
         * boolean canceled = false;
         * 
         * public void cancel() { canceled = true; } public boolean isCanceled()
         * { return canceled || progressStatus.isCancel(); } };
         * 
         * final IProgressMonitor qvtoCompilationMonitor = new
         * IProgressMonitor() {
         * 
         * boolean canceled = false; Stack<String> compilationUnits = new
         * Stack<String>(); Stack<Long> compilationStartTimes = new
         * Stack<Long>();
         * 
         * @Override public void setCanceled(boolean canceled) { this.canceled =
         * this.canceled || canceled; }
         * 
         * @Override public boolean isCanceled() { return canceled ||
         * progressStatus.isCancel(); }
         * 
         * @Override public void beginTask(String compilationUnit, int
         * workEstimate) { }
         * 
         * @Override public void subTask(String compilationUnit) {
         * compilationUnits.add(compilationUnit);
         * progressStatus.setDescription(SUBTASK_PREFIX + " [depth=" +
         * compilationUnits.size() + "] " + compilationUnit);
         * compilationStartTimes.add(System.currentTimeMillis()); }
         * 
         * @Override public void worked(int worked) { Long stopTime =
         * System.currentTimeMillis(); Long startTime =
         * compilationStartTimes.pop(); Long delay = stopTime - startTime;
         * String compilationUnit = compilationUnits.pop(); String report =
         * String.format(WORKED_PREFIX + " took %tMm %tSs %tms for: %s", delay,
         * delay, delay, compilationUnit); showMessage(report); }
         * 
         * @Override public void done() { }
         * 
         * @Override public void internalWorked(double worked) { }
         * 
         * @Override public void setTaskName(String task) { }
         * 
         * };
         * 
         * qvtoEngine.put(QVTEngineConstants.QVTO_PROGRESS_STATUS,
         * progressStatus);
         * qvtoEngine.put(QVTEngineConstants.QVTO_EVALUATION_MONITOR,
         * qvtoEvaluationMonitor);
         * qvtoEngine.put(QVTEngineConstants.QVTO_COMPILATION_MONITOR,
         * qvtoCompilationMonitor); ScriptContext sc = qvtoEngine.getContext();
         * ExecutionContextImpl executionContext = (ExecutionContextImpl)
         * sc.getAttribute(QVTEngineConstants.QVTO_EXECUTION_CONTEXT,
         * ScriptContext.ENGINE_SCOPE);
         * executionContext.setMonitor(qvtoEvaluationMonitor);
         */
        try {
            runQVTO();
            // saveOutputParameters();

            return qvtoEngine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
            // return qvtoEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        } catch (ScriptException e) {
            QVTORuntimeException qvtoRE = new QVTORuntimeException(e.getMessage(), e);
            qvtoRE.setStackTrace(e.getStackTrace());
            throw qvtoRE;
        } finally {
            showMessage("Unloading: " + rs.getResources().size() + " resources...");
            for (Resource r: rs.getResources()) {
                showMessage("Unloading: " + r.getURI());
                r.unload();
            }
            showMessage("Resources unloaded.");
        }
        /*
         * } }, script.getName(), true, 0);
         */
        // return null;
    }

    protected void keepTrace(URI traceURI) {
        qvtoEngine.put(QVTEngineConstants.QVTO_TRACE_URI, traceURI);
    }

    /**
     * This method must be explicitly defined in a Groovy script to launch a
     * QVTO transformation T. This method must provide a value for each of T's
     * parameters, regardless of the QVTO transformation parameter direction
     * (i.e., in, inout, out).
     * 
     * Optionally, turn on the flag to keep the QVTO trace in a file:
     * 
     * keepTrace(URI traceURI);
     */
    @SuppressWarnings("unchecked")
    protected void configureTransformationParameters() {
        List<Element> targets = (List<Element>)inputs.get("DocGenTargets");
        if (!targets.isEmpty()) {
            BasicModelExtent basicModelExtent = new BasicModelExtent(targets);
            qvtoEngine.put("selectedElements", basicModelExtent);
        }
        Model currentModel = Application.getInstance().getProject().getModel();
        BasicModelExtent model = new BasicModelExtent();
        model.add(currentModel);
        qvtoEngine.put("currentModel", model);
    }

    /**
     * Run the QVTO transformation.
     * 
     * @pre null != qvtoEngine
     */
    protected void runQVTO() throws ScriptException {
        qvtoEngine.eval((Reader)null);
    }

    protected File chooseDirectory(String title) {
        final Project project = a.getProject();
        String dir = ProjectsManager.getRecentFilePath();
        if (null == dir || dir.length() == 0) {
            if (null != project)
                dir = project.getFile().getParent();
        }

        if (null == dir || dir.length() == 0) {
            dir = System.getProperty("user.dir");
        }

        if (dir == null || dir.length() == 0) {
            dir = "./";
        }

        File d = new File(dir);
        if (!d.isDirectory()) {
            d = d.getParentFile();
        }
        if (!d.canWrite())
            dir = "";

        JFileChooser fileDialog = new JFileChooser(dir);
        fileDialog.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory() && f.canWrite())
                    return true;
                return false;
            }
        });
        fileDialog.setDialogTitle(title);
        fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retVal = fileDialog.showOpenDialog(a.getMainFrame());
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File selectedDir = fileDialog.getSelectedFile();
        return selectedDir;
    }

    protected URI chooseDirectoryForSavingModelResource(String modelResourceDescription,
            String modelResourceFilename) {
        File directory = chooseDirectory("Select a directory to save " + modelResourceDescription);
        if (directory == null) {
            throw new CanceledQVTOTransformationException(transformationURI);
        }

        URI modelFileResourceURI = URI.createFileURI(directory.getAbsolutePath() + File.separator
                + modelResourceFilename);
        return modelFileResourceURI;
    }

    protected URI chooseModelResourceFile(String modelResourceDescription, String modelResourceFileExtension) {
        final Project project = a.getProject();
        String dir = ProjectsManager.getRecentFilePath();
        if (null == dir || dir.length() == 0) {
            if (null != project)
                dir = project.getFile().getParent();
        }

        if (null == dir || dir.length() == 0) {
            dir = System.getProperty("user.dir");
        }

        if (dir == null || dir.length() == 0) {
            dir = "./";
        }

        File d = new File(dir);
        if (!d.isDirectory()) {
            d = d.getParentFile();
        }
        if (!d.canWrite())
            dir = "";

        final String dotFileExtension = (modelResourceFileExtension.startsWith(".")
                ? modelResourceFileExtension : "." + modelResourceFileExtension);
        JFileChooser fileDialog = new JFileChooser(dir);
        fileDialog.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return false;
                } else {
                    String name = f.getName();
                    if (name.endsWith(dotFileExtension)) {
                        return true;
                    }
                    return false;
                }
            }
        });
        fileDialog.setDialogTitle("Select a(n) " + modelResourceDescription);
        int retVal = fileDialog.showOpenDialog(a.getMainFrame());
        File f = fileDialog.getSelectedFile();

        if (retVal != JFileChooser.APPROVE_OPTION) {
            throw new CanceledQVTOTransformationException(transformationURI);
        }
        if (null == f) {
            throw new CanceledQVTOTransformationException(transformationURI);
        }
        URI modelFileResourceURI = URI.createFileURI(f.getAbsolutePath());
        return modelFileResourceURI;
    }

    protected EList<EObject> chooseAndLoadModelResourceContents(String modelResourceDescription,
            String modelResourceFileExtension) {
        URI modelFileResourceURI = chooseModelResourceFile(modelResourceDescription,
                modelResourceFileExtension);
        return loadModelResourceContents(modelFileResourceURI);
    }

    protected EList<EObject> loadModelResourceContents(URI modelFileResourceURI) {

        Resource modelResource = this.rs.getResource(modelFileResourceURI, true);
        EList<Diagnostic> errors = modelResource.getErrors();
        if (!errors.isEmpty()) {
            StringBuffer buff = new StringBuffer();
            buff.append("Error loading: " + modelFileResourceURI);
            for (Diagnostic error: errors) {
                buff.append("\n" + error);
            }
            throw new QVTOTransformationURIException(buff.toString(), transformationURI);
        }
        return modelResource.getContents();
    }

    /**
     * 
     * @param configurationPropertyName
     *            the name of a QVTO configuration property
     * @return
     */
    protected Object getConfigurationPropertyValue(String configurationPropertyName) {
        ScriptContext sc = qvtoEngine.getContext();
        QvtOperationalEvaluationEnv evaluationEnv = (QvtOperationalEvaluationEnv)sc.getAttribute(
                QVTEngineConstants.QVTO_EVALUATION_ENV, ScriptContext.ENGINE_SCOPE);
        TransformationInstance transf = (TransformationInstance)evaluationEnv
                .getValueOf(QvtOperationalEnv.THIS);
        OperationalTransformation qvtoTransf = transf.getTransformation();

        EStructuralFeature configurationPropertyFeature = null;
        for (EStructuralFeature configFeature: qvtoTransf.getConfigProperty()) {
            if (configurationPropertyName.equals(configFeature.getName())) {
                configurationPropertyFeature = configFeature;
            }
        }
        if (null == configurationPropertyFeature)
            throw new QVTOTransformationURIException("QVTEngine (configuration property: "
                    + configurationPropertyName + ") -- not found", transformationURI);

        Object configurationPropertyValue = transf.eGet(configurationPropertyFeature);
        return configurationPropertyValue;
    }

    /**
     * @param configurationPropertyName
     *            the name of a QVTO configuration property
     * @param defaultValue
     *            the string value returned if the configuration property has no
     *            value
     * @return the string-value of configurationPropertyName (if not null),
     *         defaultValue otherwise
     * @throws IllegalArgumentException
     *             if the value of configurationPropertyName is not a string
     */
    protected String getConfigurationPropertyStringValueOrDefault(String configurationPropertyName,
            String defaultValue) {
        Object value = getConfigurationPropertyValue(configurationPropertyName);
        if (null == value)
            return defaultValue;
        if (value instanceof String)
            return (String)value;

        throw new IllegalArgumentException("no string-valued configuration property: "
                + configurationPropertyName + " (got a " + value.getClass().getCanonicalName() + ")");
    }

    /**
     * Override this method if the transformation has INOUT or OUT parameters
     * that need to be saved as resources. If the transformation has INOUT
     * parameters corresponding to MagicDraw elements in the model, such
     * parameters do not need to be saved.
     */
    protected void saveOutputParameters() {
    }

    /**
     * Save the model resource corresponding to a QVTO transformation INOUT or
     * OUT parameter as a resource file.
     * 
     * For registering a metamodel-specific file extension:
     * 
     * @see com.nomagic.magicdraw.qvt.QVTOUtils.addExtensionMap(Mapping)
     * 
     * @param outputParameterName
     *            must be a non-null string corresponding to a QVTO
     *            transformation INOUT or OUT parameter.
     * @param outputResourceNameConfigProperty
     *            must be a non-null string corresponding to a QVTO
     *            transformation configuration property whose value will be the
     *            name of the output resource.
     * @param extension
     *            must be non-null string corresponding to a registered
     *            extension for the output resource metamodel.
     * @return the resource containing the model extent correpsonding to the
     *         QVTO output parameter.
     * @throws IOException
     */
    public Resource saveOutputExtent(String outputParameterName, String outputResourceNameConfigProperty,
            String extension, String contentType) throws IOException {

        Object outputResourceNameValue = getConfigurationPropertyValue(outputResourceNameConfigProperty);
        if (!(outputResourceNameValue instanceof String))
            throw new QVTOTransformationURIException("QVTEngine (output parameter: " + outputParameterName
                    + ") -- no value for configuration property: " + outputResourceNameConfigProperty,
                    transformationURI);

        String outputResourceName = (String)outputResourceNameValue;
        URI outputResourceURI = URI.createFileURI(outputResourceName).appendFileExtension(extension);
        String outputResourceFilename = outputResourceURI.toFileString();

        String title = "Save resource for " + outputParameterName;
        String desc = extension;

        Set<String> extensions = new HashSet<String>();
        extensions.add(extension);

        File outputResourceFile = QVTOUtils.saveFile(null, title, desc, extensions, outputResourceFilename);
        if (null == outputResourceFile) {
            showError("QVTPlugin: no file saved for resource corresponding to output parameter: "
                    + outputParameterName);
            return null;
        }

        URI outputResourceFileURI = URI.createFileURI(outputResourceFile.getAbsolutePath());
        return saveOutputExtent(outputParameterName, outputResourceFileURI, contentType);
    }

    /**
     * Save the model resource corresponding to a QVTO transformation INOUT or
     * OUT parameter as a URI resource.
     * 
     * @param outputParameterName
     *            must be a non-null string corresponding to a QVTO
     *            transformation INOUT or OUT parameter.
     * @param outputURI
     *            the URI of the resource whose contents will be the contents of
     *            the model resource corresponding to the named QVTO
     *            transformation output parameter
     * @return the resource containing the model extent correpsonding to the
     *         QVTO output parameter.
     * @throws IOException
     */
    public Resource saveOutputExtent(String outputParameterName, URI outputURI, String contentType)
            throws IOException {
        Object result = qvtoEngine.get(outputParameterName);
        if (!(result instanceof List<?>))
            throw new QVTOTransformationURIException("QVTEngine (unknown output parameter: "
                    + outputParameterName + ") -- output artifact URI: " + outputURI, transformationURI);

        @SuppressWarnings("unchecked")
        List<EObject> outObjects = (List<EObject>)result;
        Resource r = rs.createResource(outputURI, contentType);
        if (null == r) {
            showError("QVTEngine (output parameter: " + outputParameterName
                    + ") -- No output resource created for uri: " + outputURI);
            return null;
        }

        saveContents(r, outObjects);

        showMessage("QVTPlugin: resource corresponding to output parameter: " + outputParameterName
                + " saved as: " + outputURI);

        return r;
    }

    public void exportOutputOntologyExtent(String outputParameterName,
            String ontologyPrefixConfigurationProperty) throws IOException {
        Object result = qvtoEngine.get(outputParameterName);
        if (!(result instanceof List<?>))
            throw new QVTOTransformationURIException("QVTEngine (unknown output parameter: "
                    + outputParameterName + ")", transformationURI);

        @SuppressWarnings("unchecked")
        List<EObject> outObjects = (List<EObject>)result;
        if (outObjects.size() != 1)
            throw new QVTOTransformationURIException("QVTEngine (output parameter: " + outputParameterName
                    + ") -- there should be one output element instead of " + outObjects.size(),
                    transformationURI);
        EObject outObject = outObjects.get(0);
        if (!(outObject instanceof Ontology))
            throw new QVTOTransformationURIException("QVTEngine (output parameter: " + outputParameterName
                    + ") -- there should be one output ontology; got instead a "
                    + outObject.eClass().getName(), transformationURI);

        Ontology ontologyModel = (Ontology)outObject;
        URI ontologyModelURI = OWL2Util.getOntologyURI(ontologyModel);
        if (null == ontologyModelURI)
            throw new QVTOTransformationURIException("QVTEngine (output parameter: " + outputParameterName
                    + ") -- the ontology does not have an ontologyID URI", transformationURI);
        String ontologyModelURIExtension = ontologyModelURI.fileExtension();
        if (null == ontologyModelURIExtension || ontologyModelURIExtension.length() == 0)
            ontologyModelURI = ontologyModelURI.appendFileExtension(OWL2Resource.FILE_EXTENSION);

        Resource r = rs.createResource(ontologyModelURI, OWL2Package.eCONTENT_TYPE);
        if (null == r)
            throw new QVTOTransformationURIException("QVTEngine (output parameter: " + outputParameterName
                    + ") -- No output resource created for uri: " + ontologyModelURI, transformationURI);
        saveContents(r, outObjects);

        String nsPrefix = ontologyModelURI.trimFileExtension().lastSegment();
        if (null != ontologyPrefixConfigurationProperty)
            nsPrefix = getConfigurationPropertyStringValueOrDefault(ontologyPrefixConfigurationProperty,
                    nsPrefix);

        CatalogResolvingResourceSetOWLOntologyIRIMapper crsOWL = getResourceSet();
        OWL2XMIExportToOWL2 exporter = new OWL2XMIExportToOWL2(crsOWL, ontologyModelURI, nsPrefix);
        exporter.logToPrintWriter(QVTScriptingEngineLogFactory.makeMDMessagePrintWriter());
        try {
            exporter.run();
        } catch (InvocationTargetException e) {
            throw new QVTOTransformationURIException("QVTEngine (output parameter: " + outputParameterName
                    + ") -- error while exporting ontology", transformationURI, e);
        } catch (InterruptedException e) {
            throw new QVTOTransformationURIException("QVTEngine (output parameter: " + outputParameterName
                    + ") -- error while exporting ontology", transformationURI, e);
        }
        showMessage("QVTPlugin: ontology model corresponding to output parameter: " + outputParameterName
                + " saved as: " + ontologyModelURI + " and exported as OWL2 ontology");

    }

    public com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package getTransformationParameterModelElement(
            String parameterName) {

        Bindings engineBindings = qvtoEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        QvtOperationalEvaluationEnv evaluationEnv = (QvtOperationalEvaluationEnv)engineBindings
                .get(QVTEngineConstants.QVTO_EVALUATION_ENV);

        TransformationInstance transformation = (TransformationInstance)evaluationEnv.getValueOf("this");
        OperationalTransformation operationalTransformation = transformation.getTransformation();
        EList<ModelParameter> parameters = operationalTransformation.getModelParameter();
        ModelParameter modelParameter = null;
        for (ModelParameter parameter: parameters) {
            if (parameterName.equals(parameter.getName())) {
                modelParameter = parameter;
                break;
            }
        }
        if (null == modelParameter)
            throw new QVTORuntimeException("There is no transformation model parameter named: "
                    + parameterName);
        ModelInstance parameterModel = transformation.getModel(modelParameter);
        ModelParameterExtent parameterModelExtent = parameterModel.getExtent();
        ModelExtentContents parameterModelContents = parameterModelExtent.getContents();
        List<EObject> rootElements = parameterModelContents.getAllRootElements();
        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package root = null;
        for (EObject rootElement: rootElements) {
            if (rootElement instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package) {
                root = (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package)rootElement;
                break;
            }
        }
        if (null == root)
            throw new QVTORuntimeException(
                    "No root-level Package element found in model transformation parameter: " + parameterName
                            + " amongst the " + rootElements.size() + " root elements");
        return root;
    }

    public void mdSharePackage(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package pkg, File moduleFile) {
        Project project = Project.getProject(pkg);
        ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
        projectsManager.sharePackage(project, Arrays.asList(pkg), pkg.getName());
    }

    public void mdExportPackage(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package pkg, File moduleFile) {
        Project project = Project.getProject(pkg);
        try {
            ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
            ProjectDescriptor moduleDescriptor = ProjectDescriptorsFactory.createProjectDescriptor(moduleFile
                    .toURI());
            projectsManager.exportModule(project, Arrays.asList(pkg), pkg.getName(), moduleDescriptor);
        } catch (IOException e) {
            throw new QVTORuntimeException(e);
        } catch (ReadOnlyModuleException e) {
            throw new QVTORuntimeException(e);
        }
    }

    /**
     * Adapted from:
     * 
     * @see 
     *      org.eclipse.m2m.internal.qvt.oml.ModelExtentHelper.saveContents(Resource
     *      , List<EObject>)
     * 
     *      Override as needed for specialized metamodels.
     * 
     * @param res
     *            the output resource to store the contents to
     * @param contents
     *            the contents of a model resource to save
     * @throws IOException
     */
    protected void saveContents(Resource r, List<EObject> contents) throws IOException {
        r.getContents().addAll(contents);
        rs.saveResourceToMappedURI(r, null);
        EList<Resource.Diagnostic> errors = r.getErrors();
        if (!errors.isEmpty()) {
            sw.getBuffer().setLength(0);
            sw.append(String.format("**** %d errors while saving model %s", errors.size(), r.getURI()));
            for (Resource.Diagnostic error: errors) {
                sw.append(Strings.newLine());
                sw.append(error.toString());
            }
            throw new QVTORuntimeException(sw.toString());
        }
    }

    /**
     * Adapted from:
     * 
     * @see 
     *      org.eclipse.m2m.internal.qvt.oml.ModelExtentHelper.isDynamic(EObject)
     * 
     *      Override as needed for specialized metamodels.
     * 
     * @param eObject
     *            an EMF EObject to be saved in a resource
     * @return whether the eObject is dynamic and requires outputting the schema
     *         location of its metamodel resource.
     */
    protected static boolean isDynamic(EObject eObject) {
        return eObject instanceof EStructuralFeature.Internal.DynamicValueHolder;
    }
}
