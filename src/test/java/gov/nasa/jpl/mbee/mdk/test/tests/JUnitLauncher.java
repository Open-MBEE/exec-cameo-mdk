package gov.nasa.jpl.mbee.mdk.test.tests;

/**
 * Created by igomes on 10/21/16.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class JUnitLauncher {
    private static final String FRAMEWORK_LAUNCHER_CLASS = System.getProperty("com.nomagic.osgi.launcher", "com.nomagic.osgi.launcher.ProductionFrameworkLauncher");
    private final String[] arguments;

    protected JUnitLauncher(String... arguments) {
        this.arguments = arguments;
    }

    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Must provide test class as argument.");
        }
        System.out.println("Arguments: " + Arrays.toString(args));
        System.out.println("java.class.path: " + System.getProperty("java.class.path"));
        (new JUnitLauncher(args)).launch();
    }

    protected void launch() throws Exception {
        this.launchFramework();
    }

    protected void launchFramework() throws Exception {
        System.setProperty("com.nomagic.magicdraw.launcher", arguments[0]);
        this.invokeMainMethod(FRAMEWORK_LAUNCHER_CLASS);
    }

    private void invokeMainMethod(String clazzName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName(clazzName, true, this.getClass().getClassLoader());
        Method mainMethod = clazz.getMethod("main", String[].class);
        mainMethod.invoke(null, new Object[]{Arrays.copyOfRange(arguments, 1, arguments.length)});
    }
}

