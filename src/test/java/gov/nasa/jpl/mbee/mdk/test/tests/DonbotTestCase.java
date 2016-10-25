package gov.nasa.jpl.mbee.mdk.test.tests;

import com.nomagic.magicdraw.tests.MagicDrawTestCase;
import com.nomagic.runtime.ApplicationExitedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by igomes on 10/22/16.
 */
public class DonbotTestCase extends MagicDrawTestCase {
    private static final String FRAMEWORK_LAUNCHER_CLASS = System.getProperty("com.nomagic.osgi.launcher", "com.nomagic.osgi.launcher.ProductionFrameworkLauncher");

    public DonbotTestCase() {
        super();

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
            }
        }, "Start Application");
        thread.start();
        try (Socket socket = new ServerSocket(9001).accept(); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            String message = ois.readUTF();
            System.out.println("Message: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*while (new ServerSocket(9001).accept()) {
            System.out.println("Waiting..." + JUnitCoreWrapper.START_COMPLETE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        System.out.println("DONE WAITING");
        /*try {
            this.invokeMainMethod(FRAMEWORK_LAUNCHER_CLASS);
        } catch (Exception | Error e) {
            throw new InitializationError(e);
        }*/
    }

    private void invokeMainMethod(String clazzName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ApplicationExitedException {
        Class<?> clazz = Class.forName(clazzName, true, this.getClass().getClassLoader());
        Method mainMethod = clazz.getMethod("main", String[].class);
        System.out.println("Original com.nomagic.magicdraw.launcher: " + System.getProperty("com.nomagic.magicdraw.launcher"));
        System.setProperty("com.nomagic.magicdraw.launcher", "gov.nasa.jpl.mbee.mdk.test.JUnitCoreWrapper");
        mainMethod.invoke(null, new Object[]{new String[]{"blah"}});
    }
}
