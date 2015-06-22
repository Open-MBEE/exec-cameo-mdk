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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nomagic.magicdraw.automaton.AutomatonPlugin;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.pathvariables.PathVariablesResolver;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * runs a userscript in [md install dir]/DocGenUserScripts/...
 * 
 * @author dlam
 * 
 */
public class ScriptRunner {

    /**
     * runs the script with steroetype tag values as input to scriptInput
     * 
     * @param e
     * @param s
     * @return
     * @throws ScriptException
     */
    public static Object runScriptFromStereotype(Element e, Stereotype s) throws ScriptException {
        return runScriptFromStereotype(e, s, new HashMap<String, Object>());
    }

    static String userScriptDirectoryName = null;

    public static String getUserScriptDirectoryName() {
        if (userScriptDirectoryName == null) {
            userScriptDirectoryName = ApplicationEnvironment.getInstallRoot() + File.separator
                    + "DocGenUserScripts";
        }
        return userScriptDirectoryName;
    }

    static File userScriptDirectory = null;

    public static File getUserScriptDirectory() {
        if (userScriptDirectory == null) {
            userScriptDirectory = new File(getUserScriptDirectoryName());
        }
        return userScriptDirectory;
    }

    /**
     * runs the script with stereotype tag values plus additional inputs to
     * scriptInput
     * 
     * @param e
     * @param s
     * @param addInputs
     *            additional inputs to add to scriptInput
     * @return
     * @throws ScriptException
     */
    public static Object runScriptFromStereotype(Element e, Stereotype s, Map<String, Object> addInputs)
            throws ScriptException {
        Map<String, Object> inputs = new HashMap<String, Object>();
        List<Element> queries = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                "Expose", 1, false, 1);
        if (queries == null || queries.isEmpty()) {
            // For backward compatibility, also try Queries, the former name for
            // Expose.
            queries = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e, "Queries", 1,
                    false, 1);
        }
        for (NamedElement p: s.getInheritedMember()) {
            if (p instanceof Property) {
                inputs.put(p.getName(), StereotypesHelper.getStereotypePropertyValue(e, s, (Property)p));
            }
        }
        for (Property p: s.getOwnedAttribute()) {
            inputs.put(p.getName(), StereotypesHelper.getStereotypePropertyValue(e, s, p));
        }

        inputs.put("inputElement", e);
        File[] binDirs = new File[2];
        File binDir = getUserScriptDirectory();
        binDirs[0] = binDir;

        String sname = s.getName();
        String[] spaces = sname.split("\\.");
        String scriptFile = getUserScriptDirectoryName();
        for (String namespace: spaces)
            scriptFile += File.separator + namespace;

        String lang = "jython";
        String extension = ".py";
        Object language = StereotypesHelper.getStereotypePropertyFirst(s, "DocGenScript", "language");
        if (language != null && language instanceof EnumerationLiteral) {
            lang = ((EnumerationLiteral)language).getName();
            if (lang.equals("groovy"))
                extension = ".groovy";
            else if (lang.equals("qvt"))
                extension = ".qvto";
        }
        scriptFile += extension;
        File script = new File(scriptFile);
        binDirs[1] = new File(script.getParent());

        inputs.putAll(addInputs);
        if (!inputs.containsKey("DocGenTargets"))
            inputs.put("DocGenTargets", queries);
        return runScript(lang, inputs, script, binDirs);
    }

    /**
     * session will be created if it isn't already, session will surround the
     * script run so user don't have to manage sessions
     * 
     * @param language
     * @param inputs
     *            A map of key/value pair of script input (it can be anything
     *            really, as long as the script knows what to do with it. the
     *            inputs object will be passed to the script as 'scriptInput'
     * @param script
     *            File of the script file
     * @param binDir
     *            File of the script directory
     * @return a var called scriptInput will be accessible in the script, this
     *         is a map of key value pairs, keys will be based on what the
     *         script does and what the corresponding stereotype tags in md are,
     *         to return something from the script, assign a map to scriptOutput
     *         var in your script
     * @throws ScriptException
     */
    public static Object runScript(String language, Map<String, Object> inputs, File script, File[] binDirs)
            throws ScriptException {
        GUILog log = Application.getInstance().getGUILog();
        Object output = null;
        ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
        boolean sessionCreated = false;
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession(language + " script run");
            sessionCreated = true;
        }
        try {
            String scriptPath = script.getAbsolutePath();
            String scriptResolvedPath = PathVariablesResolver.getResolvedPath(scriptPath);

            URL[] urls = new URL[binDirs.length + 1];
            int count = 0;
            for (File binDir: binDirs) {
                urls[count] = binDir.toURI().toURL();
                count++;
            }
            urls[count] = (new File(ApplicationEnvironment.getInstallRoot() + File.separator + "plugins"
                    + File.separator + "com.nomagic.magicdraw.jpython" + File.separator + "jython"
                    + File.separator + "Lib")).toURI().toURL();

            URLClassLoader automatonClassLoaderWithBinDir = new URLClassLoader(urls,
                    AutomatonPlugin.class.getClassLoader());

            Thread.currentThread().setContextClassLoader(automatonClassLoaderWithBinDir);
            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine se = sem.getEngineByName(language);
            if (null == se)
            	throw new RuntimeException("Scripting language '" + language
            			+ "' not found for executing: " + script);

            ScriptContext sc = se.getContext();
            Bindings bindings = se.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put(ScriptEngine.FILENAME, scriptResolvedPath);
            // bindings.put(ACTION_EVENT, event);
            sc.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            // sc.setAttribute(ACTION_EVENT, event,
            // ScriptContext.ENGINE_SCOPE);
            sc.setAttribute(ScriptEngine.FILENAME, scriptResolvedPath, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute(ScriptEngine.FILENAME, scriptResolvedPath, ScriptContext.GLOBAL_SCOPE);
            se.put(ScriptEngine.FILENAME, scriptResolvedPath);

            se.put("scriptInput", inputs);
            FileReader fr = new FileReader(scriptResolvedPath);
            // se.put("scriptEngine", se);
            se.eval(fr, sc);
            output = se.get("scriptOutput");
            if (sessionCreated && SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().closeSession();
                sessionCreated = false;
            }
            return output;
        } catch (MalformedURLException e) {
            log.log(e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            log.log(e.getMessage());
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
            throw e; // if session is managed by some caller, they need to know
                     // the script failed so they can manage their sessions
        } finally {
            Thread.currentThread().setContextClassLoader(localClassLoader);
            if (sessionCreated && SessionManager.getInstance().isSessionCreated()) {
                // if we made the session, need to cancel due to script failure
                SessionManager.getInstance().cancelSession();
                sessionCreated = false;
            }
        }
        return output;
    }
}
