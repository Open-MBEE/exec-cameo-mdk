/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package worker.org.gradle.process.internal.worker;

//import org.gradle.internal.classloader.FilteringClassLoader;
import org.gradle.process.internal.streams.EncodedStream;
import org.openmbee.mdk.test.framework.GradleMagicDrawLauncher;

import java.io.DataInputStream;
import java.util.concurrent.Callable;

/**
 * The main entry point for a worker process that is using the system ClassLoader strategy. Reads worker configuration and a serialized worker action from stdin,
 * sets up the worker ClassLoader, and then delegates to {org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker} to deserialize and execute the action.
 */

/**
 * Disables the Gradle dynamic classloading as its already done in {@link GradleMagicDrawLauncher}.
 * Additionally uses reflection to change the system class loader to the current OSGi one, since Gradle library uses it to load the JUnit test and potentially other necessary components that will not be available in the bootstrap class loader.
 *
 * @author igomes
 */
public class GradleWorkerMain {
    public void run() throws Exception {
        DataInputStream instr = new DataInputStream(new EncodedStream.EncodedInput(System.in));

        // Read shared packages
        /*int sharedPackagesCount = instr.readInt();
        List<String> sharedPackages = new ArrayList<String>(sharedPackagesCount);
        for (int i = 0; i < sharedPackagesCount; i++) {
            sharedPackages.add(instr.readUTF());
        }*/

        // Read worker implementation classpath
        /*int classPathLength = instr.readInt();
        URL[] implementationClassPath = new URL[classPathLength];
        for (int i = 0; i < classPathLength; i++) {
            String url = instr.readUTF();
            implementationClassPath[i] = new URL(url);
        }*/

        /*ClassLoader classLoader = getClass().getClassLoader();
        while (classLoader != null && !(classLoader instanceof URLClassLoader)) {
            classLoader = classLoader.getParent();
        }

        URLClassLoader systemClassLoader = (URLClassLoader) classLoader;
        //String securityManagerType;
        try {
            Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrlMethod.setAccessible(true);

            //DataInputStream inputStream = new DataInputStream(new EncodedStream.EncodedInput(System.in));
            int count = instr.readInt();

            //StringBuilder classpathStr = new StringBuilder();
            for (int i = 0; i < count; i++) {
                String entry = instr.readUTF();
                File file = new File(entry);
                addUrlMethod.invoke(systemClassLoader, file.toURI().toURL());
            }
            //System.setProperty("java.class.path", classpathStr.toString());
            //securityManagerType = inputStream.readUTF();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialise system classpath.", e);
        }

        // Set up worker ClassLoader
        /*FilteringClassLoader.Spec filteringClassLoaderSpec = new FilteringClassLoader.Spec();
        for (String sharedPackage : sharedPackages) {
            filteringClassLoaderSpec.allowPackage(sharedPackage);
        }
        FilteringClassLoader filteringClassLoader = new FilteringClassLoader(getClass().getClassLoader(), filteringClassLoaderSpec);
        URLClassLoader classLoader = new URLClassLoader(implementationClassPath, filteringClassLoader);*/

        //Field systemClassLoaderField = ClassLoader.class.getDeclaredField("scl");
        //systemClassLoaderField.setAccessible(true);
        //systemClassLoaderField.set(null, getClass().getClassLoader());

        //Class<? extends Callable> workerClass = ClassLoader.loadClass("org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker").asSubclass(Callable.class);
        //Class<? extends Callable> workerClass = Class.forName("org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker", false, getClass().getClassLoader()).asSubclass(Callable.class);
        Class<? extends Callable> workerClass = Class.forName("org.openmbee.mdk.test.framework.MagicDrawClassLoaderWorker", false, getClass().getClassLoader()).asSubclass(Callable.class);
        Callable<Void> main = workerClass.getConstructor(DataInputStream.class).newInstance(instr);
        main.call();
    }

    public static void main(String[] args) {
        try {
            new GradleWorkerMain().run();
            System.exit(0);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
