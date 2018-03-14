package gov.nasa.jpl.mbee.mdk.util;

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
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import javax.script.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * runs a userscript in [md install dir]/DocGenUserScripts/...
 *
 * @author dlam
 */
public class ScriptRunner {
    private static String userScriptDirectoryName;

    public static String getUserScriptDirectoryName() {
        if (userScriptDirectoryName == null) {
            userScriptDirectoryName = ApplicationEnvironment.getInstallRoot() + File.separator + "DocGenUserScripts";
        }
        return userScriptDirectoryName;
    }

    private static File userScriptDirectory;

    public static File getUserScriptDirectory() {
        if (userScriptDirectory == null) {
            userScriptDirectory = new File(getUserScriptDirectoryName());
        }
        return userScriptDirectory;
    }

    /**
     * runs the script with stereotype tag values plus additional inputs to scriptInput
     *
     * @param e
     * @param s
     * @param addInputs additional inputs to add to scriptInput
     * @return
     * @throws ScriptException
     */
    public static Object runScriptFromStereotype(Element e, Stereotype s, Map<String, Object> addInputs) throws ScriptException {
        GUILog log = Application.getInstance().getGUILog();

        Map<String, Object> inputs = new HashMap<>();
        List<Element> queries = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e, "Expose", 1, false, 1);
        if (queries == null || queries.isEmpty()) {
            // For backward compatibility, also try Queries, the former name for
            // Expose.
            queries = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e, "Queries", 1, false, 1);
        }
        for (NamedElement p : s.getInheritedMember()) {
            if (p instanceof Property) {
                inputs.put(p.getName(), StereotypesHelper.getStereotypePropertyValue(e, s, (Property) p));
            }
        }
        for (Property p : s.getOwnedAttribute()) {
            inputs.put(p.getName(), StereotypesHelper.getStereotypePropertyValue(e, s, p));
        }

        inputs.put("inputElement", e);
        File[] paths = MDKOptionsGroup.getMDKOptions().getCustomUserScriptDirectories();
        int numDirs = MDKOptionsGroup.getMDKOptions().getNumberOfCustomUserScriptDirectories();
        File[] binDirs = new File[2 + numDirs];

        for (int i = 0; i < numDirs; i++) {
            binDirs[i] = paths[i];
        }

        File binDir = getUserScriptDirectory();
        binDirs[numDirs] = binDir;
        boolean notFoundScript = true;
        int j = 0;
        File script = null;
        String lang = "jython";
        while (notFoundScript) {
            File scriptFile = binDirs[j];
            String filePath = scriptFile.getPath();
            String sname = s.getName();
            String[] spaces = sname.split("\\.");

            for (String namespace : spaces) {
                filePath += File.separator + namespace;
            }

            String extension = ".py";
            Object language = StereotypesHelper.getStereotypePropertyFirst(s, "DocGenScript", "language");
            if (language != null && language instanceof EnumerationLiteral) {
                lang = ((EnumerationLiteral) language).getName();
                if (lang.equals("groovy")) {
                    extension = ".groovy";
                }
                else if (lang.equals("qvt")) {
                    extension = ".qvto";
                }
            }
            filePath += extension;
            script = new File(filePath);
            if ((script.exists() & !script.isDirectory()) || j == numDirs) {
                notFoundScript = false;
            }
            j++;
        }
        binDirs[numDirs + 1] = new File(script.getParent());
        if (j > numDirs) {
            if (numDirs > 0) {
                log.log("Script not found on paths: ");
            }
            for (int i = 0; i < numDirs; i++) {
                log.log(paths[i].getPath());
            }
        }
        inputs.putAll(addInputs);
        if (!inputs.containsKey("DocGenTargets")) {
            inputs.put("DocGenTargets", queries);
        }
        inputs.put("__name__", "__main__");
        return runScript(lang, inputs, script, binDirs);
    }

    /**
     * session will be created if it isn't already, session will surround the script run so user don't have to manage sessions
     *
     * @param language
     * @param inputs   A map of key/value pair of script input (it can be anything really, as long as the script knows what to do with it. the inputs object will be passed to the script
     *                 as 'scriptInput'
     * @param script   File of the script file
     * @param binDirs  File of the script directory
     * @return a var called scriptInput will be accessible in the script, this is a map of key value pairs, keys will be based on what the script does and what the corresponding
     * stereotype tags in md are, to return something from the script, assign a map to scriptOutput var in your script
     * @throws ScriptException
     */
    public static Object runScript(String language, Map<String, Object> inputs, File script, File[] binDirs) throws ScriptException {
        GUILog log = Application.getInstance().getGUILog();
        Object output = null;
        ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
        boolean sessionCreated = false;
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession(language + " script run");
            sessionCreated = true;
        }
        String scriptResolvedPath = null;
        try {
            String scriptPath = script.getAbsolutePath();
            scriptResolvedPath = PathVariablesResolver.getResolvedPath(scriptPath);

            URL[] urls = new URL[binDirs.length + 1];
            int count = 0;
            for (File binDir : binDirs) {
                urls[count] = binDir.toURI().toURL();
                count++;
            }
            urls[count] = (new File(ApplicationEnvironment.getInstallRoot() + File.separator + "plugins" + File.separator + "com.nomagic.magicdraw.jpython" + File.separator
                    + "jython" + File.separator + "Lib")).toURI().toURL();

            URLClassLoader automatonClassLoaderWithBinDir = new URLClassLoader(urls, AutomatonPlugin.class.getClassLoader());

            Thread.currentThread().setContextClassLoader(automatonClassLoaderWithBinDir);

            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine se = sem.getEngineByName(language);
            if (null == se) {
                throw new RuntimeException("Scripting language '" + language + "' not found for executing: " + script);
            }

            ScriptContext sc = se.getContext();
            Bindings bindings = se.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put(ScriptEngine.FILENAME, scriptResolvedPath);
            // bindings.put("__name__", "__main__");
            // bindings.put(ACTION_EVENT, event);
            sc.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            // sc.setAttribute(ACTION_EVENT, event,
            // ScriptContext.ENGINE_SCOPE);
            sc.setAttribute(ScriptEngine.FILENAME, scriptResolvedPath, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute(ScriptEngine.FILENAME, scriptResolvedPath, ScriptContext.GLOBAL_SCOPE);
            // sc.setAttribute("__name__", "__main__", ScriptContext.ENGINE_SCOPE);
            se.put(ScriptEngine.FILENAME, scriptResolvedPath);
            se.put("__name__", "__main__");

            se.put("scriptInput", inputs);
            try (FileReader fr = new FileReader(scriptResolvedPath)) {
                // se.put("scriptEngine", se);
                se.eval(fr, sc);
            }
            output = se.get("scriptOutput");

            if (sessionCreated && SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().closeSession();
                sessionCreated = false;
            }
            return output;
        } catch (IOException | ScriptException | RuntimeException e) {
            Application.getInstance().getGUILog().log("An error occurred while attempting to run script" + (scriptResolvedPath != null ? ": " + scriptResolvedPath : "") + ". Reason: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(localClassLoader);
            if (sessionCreated && SessionManager.getInstance().isSessionCreated()) {
                // if we made the session, need to cancel due to script failure
                SessionManager.getInstance().cancelSession();
            }
        }
        return output;
    }
}
