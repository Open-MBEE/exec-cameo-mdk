package gov.nasa.jpl.mbee.mdk.test;


import com.nomagic.magicdraw.tests.MagicDrawTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Created by brower on 7/21/16.
 */
public class OpenMagicDrawTest extends MagicDrawTestCase{

    public static void main(String[] args) throws Exception
    {
        //parseArgs(args);
        System.out.println("In main method");
        JUnitCore jCore = new JUnitCore();
        Result results = jCore.run(OpenMagicDrawTest.class);
        //printJunitResults(results);
        System.exit(0);
    }

    public OpenMagicDrawTest (String testMethodToRun, String testName)
    {
        super(testMethodToRun, testName);
        //setDoNotUseSilentMode(false);
        System.setProperty("jsse.enableSNIExtension", "false");
        setSkipMemoryTest(true);
        //setDoNotUseSilentMode(true);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        suite.addTest(new OpenMagicDrawTest("doNothing", "does nothing"));
        return suite;
    }

    public void doNothing() {
        System.out.println("nothing done!\n");
    }

}
