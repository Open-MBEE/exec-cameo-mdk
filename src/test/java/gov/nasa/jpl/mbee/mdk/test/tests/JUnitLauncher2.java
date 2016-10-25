package gov.nasa.jpl.mbee.mdk.test.tests;

/**
 * Created by igomes on 10/21/16.
 */
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class JUnitLauncher2 {
    private static final String JAVA_CLASS_PATH_PROPERTY = "java.class.path";
    private static final String INSTALL_ROOT_PROPERTY = "install.root";
    private static final String COM_NOMAGIC_MAGICDRAW_LAUNCHER_PROPERTY = "com.nomagic.magicdraw.launcher";
    private static final String COM_NOMAGIC_OSGI_LAUNCHER_PROPERTY = "com.nomagic.osgi.launcher";
    private static final String JAR_FILE_PROTOCOL = "jar:file:";
    private static final String IDEA_APP_MAIN = ".AppMain";
    private static final String LIB_IDE_SUBDIRS = "lib/ide";
    private static final String IDEA_WRAPPER_MAIN = ".CommandLineWrapper";
    private static final String FRAMEWORK_LAUNCHER_CLASS = System.getProperty("com.nomagic.osgi.launcher", "com.nomagic.osgi.launcher.ProductionFrameworkLauncher");
    private final String[] originalArgs;
    protected URLClassLoader appClassLoader;
    protected final List<String> appArgs;
    private final String installRoot;
    private boolean useWrapper;

    protected JUnitLauncher2(String[] originalArgs) {
        this.originalArgs = originalArgs;
        this.appClassLoader = null;
        this.appArgs = new ArrayList();
        this.installRoot = detectInstallRoot();
        if(this.installRoot == null || !(new File(this.installRoot)).isDirectory()) {
            throw new IllegalStateException("The property install.root was not specified");
        }
    }

    private static String detectInstallRoot() {
        String installRoot = System.getProperty("install.root");
        if(installRoot != null) {
            return installRoot;
        } else {
            URL classUrl = JUnitLauncher.class.getResource('/' + JUnitLauncher.class.getName().replace('.', '/') + ".class");
            return detectInstallRootFromClassUrl(classUrl);
        }
    }

    protected static String detectInstallRootFromClassUrl(URL classUrl) {
        if(!classUrl.toString().startsWith("jar:file:")) {
            return null;
        } else {
            try {
                JarURLConnection e = (JarURLConnection)classUrl.openConnection();
                URL jarUrl = e.getJarFileURL();
                Path jarDirPath = Paths.get(jarUrl.toURI()).getParent();
                return jarDirPath.endsWith(Paths.get("lib/ide", new String[0]))?jarDirPath.getParent().getParent().toString():null;
            } catch (URISyntaxException | IOException var4) {
                return null;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        (new JUnitLauncher(args)).launch();
    }

    protected void launch() throws Exception {
        this.setPredefinedProperties();
        this.parseVMArgsAndOptions();
        if(this.useWrapper) {
            this.launchWrapper();
        } else {
            this.launchFramework();
        }

    }

    protected void setPredefinedProperties() {
        setSystemPropertyIfMissing("md.class.path", "$java.class.path");
        this.setInstallRootRelativePropertyIfMissing("com.nomagic.osgi.config.dir", "configuration");
        this.setInstallRootRelativePropertyIfMissing("esi.system.config", "data/application.conf");
        this.setInstallRootRelativePropertyIfMissing("logback.configurationFile", "data/logback.xml");
        this.setInstallRootRelativePropertyIfMissing("esi.system.config", "data/application.conf");
    }

    private void setInstallRootRelativePropertyIfMissing(String property, String relPath) {
        setSystemPropertyIfMissing(property, (new File(this.installRoot, relPath)).getPath());
    }

    private static void setSystemPropertyIfMissing(String key, String value) {
        System.setProperty(key, System.getProperty(key, value));
    }

    protected void parseVMArgsAndOptions() throws MalformedURLException {
        this.appArgs.clear();
        int i = 0;
        int optionStart = -1;

        for(int cpPos = -1; i < this.originalArgs.length; ++i) {
            String arg = this.originalArgs[i];
            if(cpPos == i) {
                this.setClassPath(arg);
            } else {
                if(optionStart < 0 && !arg.startsWith("-")) {
                    optionStart = i;
                }

                if(optionStart < 0) {
                    if(arg.startsWith("-D")) {
                        setSystemPropertyFromArg(arg);
                    } else if(arg.startsWith("-cp") || arg.startsWith("-classpath")) {
                        cpPos = i + 1;
                    }
                } else {
                    if(i == optionStart) {
                        if(arg.endsWith(".CommandLineWrapper")) {
                            this.useWrapper = true;
                        } else {
                            /*if(!arg.endsWith(".AppMain")) {
                                throw new UnsupportedOperationException();
                            }*/

                            this.useWrapper = false;
                        }
                    }

                    this.appArgs.add(arg);
                }
            }
        }

    }

    private void setClassPath(String arg) throws MalformedURLException {
        ArrayList cpElements = new ArrayList();
        StringTokenizer currentJavaClassPath = new StringTokenizer(arg, File.pathSeparator);

        while(currentJavaClassPath.hasMoreTokens()) {
            String cpElement = currentJavaClassPath.nextToken();
            cpElements.add((new File(".")).toURI().resolve((new File(cpElement)).toURI()).toURL());
        }

        this.appClassLoader = new URLClassLoader((URL[])cpElements.toArray(new URL[cpElements.size()]));
        String currentJavaClassPath1 = System.getProperty("java.class.path");
        if(currentJavaClassPath1 != null) {
            System.setProperty("java.class.path", currentJavaClassPath1 + File.pathSeparatorChar + arg);
        } else {
            System.setProperty("java.class.path", arg);
        }

    }

    private static void setSystemPropertyFromArg(String arg) {
        int eqPos = arg.indexOf(61);
        String key;
        String value;
        if(eqPos >= 0) {
            key = arg.substring(2, eqPos);
            value = arg.substring(eqPos + 1);
        } else {
            key = arg.substring(2);
            value = "";
        }

        System.setProperty(key, value);
    }

    protected void launchWrapper() throws Exception {
        this.insertArgsForWrapper();
        this.invokeMainMethod((String)this.appArgs.remove(0));
    }

    protected void insertArgsForWrapper() {
        this.appArgs.add(2, JUnitLauncher.class.getName());
    }

    protected void launchFramework() throws Exception {
        System.setProperty("com.nomagic.magicdraw.launcher", (String)this.appArgs.remove(0));
        this.invokeMainMethod(FRAMEWORK_LAUNCHER_CLASS);
    }

    private void invokeMainMethod(String clazzName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = Class.forName(clazzName, true, (ClassLoader)(this.appClassLoader != null?this.appClassLoader:this.getClass().getClassLoader()));
        Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});
        mainMethod.invoke((Object)null, new Object[]{this.appArgs.toArray(new String[this.appArgs.size()])});
    }
}
