package gov.nasa.jpl.mbee.mdk.test.tests;

/**
 * Created by igomes on 10/22/16.
 */

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.StartupParticipant;
import junit.runner.Version;
import org.junit.internal.RealSystem;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class JUnitCoreWrapper {
    public static volatile boolean START_COMPLETE;

    public JUnitCoreWrapper() {
    }

    public static void main(String[] args) throws Exception {
        /*if (args.length != 1) {
            System.err.println("Invalid number of arguments.");
            System.exit(1);
        }*/

        Application.getInstance().start(true, true, false, new String[]{"TESTER"}, new StartupParticipant() {
            @Override
            public void beforeMainWindow() {

            }

            @Override
            public void afterMainWindow() {
                try (Socket socket = new Socket(InetAddress.getLocalHost(), 9001); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                    oos.writeUTF("Successfully started Application.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //  runJUnit(args);
        //System.exit(result.wasSuccessful() ? 0 : 1);
        //System.exit(0);
    }

    private static Result runJUnit(String[] args) {
        JUnitCore core = new JUnitCore();
        RealSystem system = new RealSystem();
        System.out.println("JUnit version " + Version.id());
        String className = args[0];

        TextListener listener = new TextListener(system);
        core.addListener(listener);
        Result result;
        try {
            Class<?> clazz = JUnitCoreWrapper.class.getClassLoader().loadClass(className);
            try {
                result = core.run(Request.runner(new BlockJUnit4ClassRunner(clazz)));
            } catch (InitializationError initializationError) {
                throw new RuntimeException(initializationError);
            }
        } catch (ClassNotFoundException | RuntimeException e) {
            System.out.println("Could not find class: " + className);
            Description description = Description.createSuiteDescription(className);
            Failure failure = new Failure(description, e);
            result = new Result();
            result.getFailures().add(failure);
        }

        return result;
    }
}
