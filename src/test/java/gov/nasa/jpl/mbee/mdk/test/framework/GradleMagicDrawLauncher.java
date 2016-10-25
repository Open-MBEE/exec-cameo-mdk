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
        System.out.println("STARTED");
        new GradleMagicDrawLauncher().run(args);
    }

    public void run(String... args) throws Exception {
        urlClassLoader = (URLClassLoader) getClass().getClassLoader();
        parseArgs(args);

        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();

        for (int i = 0; i < jvmArgs.size(); i++) {
            System.out.println(jvmArgs.get(i));
        }
        System.out.println(" -classpath " + System.getProperty("java.class.path"));
        // print the non-JVM command line arguments
        // print name of the main class with its arguments, like org.ClassName param1 param2
        System.out.println(" " + System.getProperty("sun.java.command"));

        Thread thread = new Thread(() -> {
            try {
                System.out.println("THREADED!");
                invokeMainMethod(FRAMEWORK_LAUNCHER_CLASS);
            } catch (Exception | Error e) {
                e.printStackTrace();
                System.exit(1);
            }
        }, "Start Application");
        thread.start();
        try (Socket socket = new ServerSocket(9001).accept(); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            String message = ois.readUTF();
            System.out.println("Message: " + message);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("DONE WAITING");
        /*try {
            this.invokeMainMethod(FRAMEWORK_LAUNCHER_CLASS);
        } catch (Exception | Error e) {
            throw new InitializationError(e);
        }*/
    }

    private void invokeMainMethod(String clazzName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ApplicationExitedException {
        Class<?> clazz = Class.forName(clazzName, true, urlClassLoader);
        Method mainMethod = clazz.getMethod("main", String[].class);
        System.out.println("Original com.nomagic.magicdraw.launcher: " + System.getProperty("com.nomagic.magicdraw.launcher"));
        System.setProperty("com.nomagic.magicdraw.launcher", mainClass);
        //System.setProperty("com.nomagic.magicdraw.launcher", "gov.nasa.jpl.mbee.mdk.test.tests.MD_Tests_JUnit4");
        mainMethod.invoke(null, new Object[]{mainClassArgs});
    }

    private void parseArgs(String... args) throws IOException {
        System.out.println("PARSING ARGS");
        int mainClassIndex = -1;
        List<URL> cpElements = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            System.out.println("ARG: " + arg);
            if ((arg.equals("-cp") || arg.equals("-classpath")) && i + 1 < args.length) {
                String newClassPath = args[++i];
                StringTokenizer tokenizer = new StringTokenizer(newClassPath, File.pathSeparator);
                while(tokenizer.hasMoreTokens()) {
                    String cpElement = tokenizer.nextToken();
                    if (cpElement.endsWith("gradle-worker.jar")) {
                        System.out.println("SKIPPING gradle-worker.jar");
                        continue;
                    }
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
                System.out.println(key + "=" + value);
                continue;
            }
            if (arg.contains("org.gradle")) {
                mainClass = arg;
                mainClassIndex = i;
                break;
            }
        }
        if (1 < 0 && mainClassIndex >= 0) {
            mainClassArgs = Arrays.copyOfRange(args, mainClassIndex + 1, args.length);
            System.out.println("MAIN CLASS ARGS: " + Arrays.toString(mainClassArgs));
        }
        else {
            mainClassArgs = new String[]{"DEVELOPER"};
        }

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
            System.out.println("More classpath: " + cpElement);
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

        URL runtimeJar = getClass().getProtectionDomain().getCodeSource().getLocation();
        System.out.println("RUNTIME JAR: " + runtimeJar);
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
            System.out.println("Classpath: " + Arrays.toString(cpElements.toArray()));

            /*try {
                Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
                for (URL file : cpElements) {
                    addUrlMethod.invoke(urlClassLoader, file.toURI().toURL());
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not initialise system classpath.", e);
            }*/

            urlClassLoader = new URLClassLoader(cpElements.toArray(new URL[cpElements.size()]));
            System.out.println("java.class.path: " + System.getProperty("java.class.path"));
        }
    }
}