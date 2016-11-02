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

public class ApplicationStartClassRunner extends BlockJUnit4ClassRunner {
    //private static final String FRAMEWORK_LAUNCHER_CLASS = System.getProperty("com.nomagic.osgi.launcher", "com.nomagic.osgi.launcher.ProductionFrameworkLauncher");

    private final Class<?> testClass;

    public ApplicationStartClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        testClass = clazz;

        try {
            Application.getInstance().start(true, true, false, new String[]{"TESTER"}, null);
        } catch (ApplicationExitedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}