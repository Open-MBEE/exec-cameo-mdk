package worker.org.gradle.process.internal.worker.child;

import worker.org.gradle.process.internal.streams.EncodedStream;

import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permission;

/**
 * Created by igomes on 10/22/16.
 */

/**
 * This is a hack to enable our OSGi hack and counter Gradle's hack for Window's classpath limitation by dynamically loading the classpath using a SecurityManager.
 * It passes all the necessary libraries for the bootstrap classpath, namely the ones necessary for {@link worker.org.gradle.process.internal.worker.GradleWorkerMain},
 * but we have overridden that for other reasons anyway.
 *
 * This should be able to be reverted in Java 9 since Java 9 will break Gradle's hack.
 * https://discuss.gradle.org/t/classcastexception-from-org-gradle-process-internal-child-bootstrapsecuritymanager/2443/11
 *
 * @author igomes
 */
// This is a hack to enable our OSGi hack and counter Gradle's hack for Window's classpath limitation by dynamically loading the classpath using a SecurityManager
// This should be able to be reverted in Java 9 since Java 9 will break Gradle's hack.
// https://discuss.gradle.org/t/classcastexception-from-org-gradle-process-internal-child-bootstrapsecuritymanager/2443/11
public class BootstrapSecurityManager extends SecurityManager {
    private boolean initialised;
    private final URLClassLoader target;

    public BootstrapSecurityManager() {
        this(null);
    }

    BootstrapSecurityManager(URLClassLoader target) {
        this.target = target;
    }

    public void checkPermission(Permission permission) {
        synchronized (this) {
            if (this.initialised) {
                return;
            }
            if (System.in == null) {
                return;
            }

            this.initialised = true;
        }
        System.clearProperty("java.security.manager");
        System.setSecurityManager(null);

        URLClassLoader systemClassLoader = this.target != null ? this.target : (URLClassLoader) getClass().getClassLoader();
        String securityManagerType;
        try {
            //Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            //addUrlMethod.setAccessible(true);

            DataInputStream inputStream = new DataInputStream(new EncodedStream.EncodedInput(System.in));
            int count = inputStream.readInt();
            //StringBuilder classpathStr = new StringBuilder();
            for (int i = 0; i < count; i++) {
                /*String entry = */inputStream.readUTF();
                /*File file = new File(entry);
                addUrlMethod.invoke(systemClassLoader, file.toURI().toURL());
                if (i > 0) {
                    classpathStr.append(File.pathSeparator);
                }
                classpathStr.append(file.toString());*/
            }
            //System.setProperty("java.class.path", classpathStr.toString());
            securityManagerType = inputStream.readUTF();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialise system classpath.", e);
        }

        if (securityManagerType.length() > 0) {
            System.setProperty("java.security.manager", securityManagerType);
            SecurityManager securityManager;
            try {
                Class aClass = systemClassLoader.loadClass(securityManagerType);
                securityManager = (SecurityManager) aClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not create an instance of '" + securityManagerType + "' specified for system SecurityManager.", e);
            }
            System.setSecurityManager(securityManager);
        }
    }
}
