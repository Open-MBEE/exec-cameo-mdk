package gov.nasa.jpl.mbee.mdk.test.framework;

import com.nomagic.runtime.ApplicationExitedException;
import org.gradle.process.internal.streams.EncodedStream;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by igomes on 10/22/16.
 */
public class GradleMagicDrawLauncher {
    private static final String FRAMEWORK_LAUNCHER_CLASS = System.getProperty("com.nomagic.osgi.launcher", "com.nomagic.osgi.launcher.ProductionFrameworkLauncher");

    private URLClassLoader urlClassLoader;
    private String mainClass;
    private String[] mainClassArgs = new String[]{};

    public static void main(String... args) throws Exception {
        new GradleMagicDrawLauncher().run(args);
    }

    public void run(String... args) throws Exception {
        urlClassLoader = (URLClassLoader) getClass().getClassLoader();
        parseArgs(args);
        invokeMainMethod(FRAMEWORK_LAUNCHER_CLASS);
    }

    private void invokeMainMethod(String clazzName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ApplicationExitedException {
        Class<?> clazz = Class.forName(clazzName, true, urlClassLoader);
        Method mainMethod = clazz.getMethod("main", String[].class);
        System.setProperty("com.nomagic.magicdraw.launcher", mainClass);
        mainMethod.invoke(null, new Object[]{mainClassArgs});
    }

    private void parseArgs(String... args) throws IOException {
        int mainClassIndex = -1;
        List<URL> cpElements = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equals("-cp") || arg.equals("-classpath")) && i + 1 < args.length) {
                String newClassPath = args[++i];
                StringTokenizer tokenizer = new StringTokenizer(newClassPath, File.pathSeparator);
                while(tokenizer.hasMoreTokens()) {
                    String cpElement = tokenizer.nextToken();
                    try {
                        cpElements.add((new File(".")).toURI().resolve((new File(cpElement)).toURI()).toURL());
                    } catch (MalformedURLException ignored) {
                    }
                    String currentClassPath = System.getProperty("java.class.path");
                    System.setProperty("java.class.path", (currentClassPath != null ? currentClassPath + File.pathSeparatorChar + cpElement : cpElement));
                }
                continue;
            }
            if (arg.startsWith("-D")) {
                arg = arg.substring(2);
                String[] parts = arg.split("=");
                String key = parts[0];
                String value = parts.length > 1 ? "" : null;
                for (int j = 1; j < parts.length; j++) {
                    value += parts[j];
                }
                String ignored = value != null ? System.setProperty(key, value) : System.clearProperty(key);
                continue;
            }
            if (arg.contains("org.gradle")) {
                mainClass = arg;
                mainClassIndex = i;
                break;
            }
        }
        if (mainClassIndex >= 0) {
            mainClassArgs = Arrays.copyOfRange(args, mainClassIndex + 1, args.length);
        }

        /**
         * This is used to load all the necessary libraries into the classpath and is usually done in GradleWorkerMain.
         * However, we need them added to the classpath before launching the OSGi framework and additionally need to add them to java.class.path.
         * To achieve that we've overridden the usual GradleWorkerMain with one that excludes the classloading and done it here.
         * {@link worker.org.gradle.process.internal.worker.GradleWorkerMain}
         * Gradle likes it's class overriding hacks, so to avoid issues caused by ones we haven't discovered we're loading Gradle libraries first.
         */

        DataInputStream instr = new DataInputStream(new EncodedStream.EncodedInput(System.in));

        // Read shared packages
        int sharedPackagesCount = instr.readInt();
        List<String> sharedPackages = new ArrayList<>(sharedPackagesCount);
        for (int i = 0; i < sharedPackagesCount; i++) {
            sharedPackages.add(instr.readUTF());
        }

        // Read worker implementation classpath
        int classPathLength = instr.readInt();
        List<String> files = new ArrayList<>(classPathLength);
        List<URL> moreCpElements = new ArrayList<>(1 + cpElements.size() + classPathLength);
        for (int i = 0; i < classPathLength; i++) {
            String cpElement = instr.readUTF();
            files.add(cpElement);
            moreCpElements.add(new URL(cpElement));
        }
        moreCpElements.addAll(cpElements);
        cpElements = moreCpElements;

        if (!files.isEmpty()) {
            String currentClassPath = System.getProperty("java.class.path");
            String newClassPath = files.stream().collect(Collectors.joining(File.pathSeparator));
            System.setProperty("java.class.path", (currentClassPath != null ? newClassPath + File.pathSeparatorChar + currentClassPath : newClassPath));
        }

        /**
         * Adds our tests-hack.jar to the front of the classloader (and java.class.path) so our patches to Gradle's hacks can do their jobs.
         * For an example see {@link org.gradle.internal.logging.slf4j.Slf4jLoggingConfigurer}.
         */

        URL runtimeJar = getClass().getProtectionDomain().getCodeSource().getLocation();
        if (runtimeJar != null) {
            cpElements.add(0, runtimeJar);
            String currentClassPath = System.getProperty("java.class.path");
            try {
                String runtimeJarPath = runtimeJar.toURI().getPath();
                System.setProperty("java.class.path", (currentClassPath != null ? runtimeJarPath + File.pathSeparatorChar + currentClassPath : runtimeJarPath));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }


        if (!cpElements.isEmpty()) {
            urlClassLoader = new URLClassLoader(cpElements.toArray(new URL[cpElements.size()]));
        }
    }
}