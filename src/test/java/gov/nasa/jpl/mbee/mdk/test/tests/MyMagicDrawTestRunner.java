package gov.nasa.jpl.mbee.mdk.test.tests;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.tests.MagicDrawTestRunner;
import com.nomagic.runtime.ApplicationExitedException;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by igomes on 10/21/16.
 */

public class MyMagicDrawTestRunner extends BlockJUnit4ClassRunner {
    //private static final String FRAMEWORK_LAUNCHER_CLASS = System.getProperty("com.nomagic.osgi.launcher", "com.nomagic.osgi.launcher.ProductionFrameworkLauncher");

    private final Class<?> testClass;

    public MyMagicDrawTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        testClass = clazz;

        try {
            Application.getInstance().start(true, true, false, new String[]{"TESTER"}, null);
        } catch (ApplicationExitedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //throw new InitializationError("Testing class: " + clazz.getCanonicalName());

        /*RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();

        for (int i = 0; i < jvmArgs.size(); i++) {
            System.out.println( jvmArgs.get( i ) );
        }
        System.out.println(" -classpath " + System.getProperty("java.class.path"));
        // print the non-JVM command line arguments
        // print name of the main class with its arguments, like org.ClassName param1 param2
        System.out.println(" " + System.getProperty("sun.java.command"));

        testClass = clazz;
        Thread thread = new Thread(() -> {
            try {
                System.out.println("THREADED!");
                invokeMainMethod(FRAMEWORK_LAUNCHER_CLASS);
            } catch (Exception | Error e) {
                e.printStackTrace();
            }
        }, "Start Application");
        thread.start();
        while (!JUnitCoreWrapper.START_COMPLETE) {
            System.out.println("Waiting..." + JUnitCoreWrapper.START_COMPLETE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("DONE WAITING");*/
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
        mainMethod.invoke(null, new Object[]{new String[]{testClass.getCanonicalName()}});
    }

    /*@Override
    protected List<FrameworkMethod> computeTestMethods() {
        System.out.println("HERE!");
        List<FrameworkMethod> blah = new ArrayList<>();
        blah.add(new FrameworkMethod(null));
        return blah;
        /*return Arrays.stream(this.getTestClass().getJavaClass().getMethods()).filter(method ->
                Arrays.stream(method.getAnnotations()).anyMatch(annotation -> annotation.getClass().getCanonicalName().equals("org.junit.Test"))).map(FrameworkMethod::new).collect(Collectors.toList());
        //return this.getTestClass().getAnnotatedMethods(Test.class);
    }*/

    /*@Override
    protected List<TestRule> getTestRules(Object var1) {
        return addMDTestRules(var1, super.getTestRules(var1));
    }

    private static List<TestRule> addMDTestRules(Object var0, List<TestRule> var1) {
        ArrayList<TestRule> var2 = new ArrayList<>(var1);
        var2.add(new RunWithApplicationRule(var0));
        return var2;
    }*/
}