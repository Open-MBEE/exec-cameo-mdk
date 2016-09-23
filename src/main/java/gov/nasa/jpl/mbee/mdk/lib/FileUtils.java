/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.mdk.lib;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class FileUtils {

    public static boolean exists(String path) {
        File f = existingFile(path);
        return (f != null);
    }

    public static String existingPath(String path) {
        if (path != null && exists(path)) {
            return path;
        }
        return null;
    }

    public static String existingPath(URL url) {
        if (url != null && exists(url.getFile())) {
            return url.getFile();
        }
        return null;
    }

    public static File existingFile(String path) {
        if (path == null) {
            return null;
        }
        File f = new File(path);
        if (f.exists()) {
            return f;
        }
        return null;
    }

    public static File existingFile(URL url) {
        if (url == null) {
            return null;
        }
        return existingFile(url.getFile());
    }

    public static String existingFolder(String path) {
        File f = existingFile(path);
        if (f == null) {
            return null;
        }
        if (f.isDirectory()) {
            return f.getPath();
        }
        return f.getParent();
    }

    protected static String existingFolder(URL url) {
        File f = existingFile(url);
        if (f == null) {
            return null;
        }
        if (f.isDirectory()) {
            return f.getPath();
        }
        return f.getParent();
    }

    protected static String getCurrentWorkingDirectory() {
        String curDir = System.getProperty("user.dir");
        return curDir;
    }

    // These require org.eclipse plugins
    // protected static String getWorkspaceDirectory() {
    // return getWorkspaceFile().getAbsolutePath();
    // }
    // protected static File getWorkspaceFile() {
    // //get object which represents the workspace
    // IWorkspace workspace = ResourcesPlugin.getWorkspace();
    // //get location of workspace (java.io.File)
    // File workspaceDirectory = workspace.getRoot().getLocation().toFile();
    // return workspaceDirectory;
    // }
    //
    // protected static String getProjectDirectory() {
    // return getProjectFile().getAbsolutePath();
    //
    // }
    // protected static File getProjectFile() {
    // ResourcesPlugin.getWorkspace().getRoot().getProjects();
    // }

    /**
     * Find a file with the name, {@code fileName}, in the current working
     * directory or some subdirectory.
     *
     * @param fileName the name of the file to find
     * @return an existing {@code File} named {@code fileName}
     */
    public static File findFile(final String fileName) {
        File file = existingFile(fileName);
        if (file == null) {
            File cwd = new File(getCurrentWorkingDirectory());
            assert cwd.exists();
            List<File> q = new ArrayList<File>();
            List<File> files = new ArrayList<File>();
            q.add(cwd);
            while (!q.isEmpty()) {
                File f = q.get(0);
                q.remove(0);
                if (f.isDirectory()) {
                    // is the file in this directory?
                    String fileInDirString = f.getAbsolutePath() + File.separator + fileName;
                    File fileInDir = new File(fileInDirString);
                    if (fileInDir.exists()) {
                        files.add(fileInDir);
                        break;
                    }
                    // check other files/directories
                    File[] dirFiles = f.listFiles();
                    q.addAll(Arrays.asList(dirFiles));
                }
                int lengthDiff = f.getAbsolutePath().length() - fileName.length();
                if (lengthDiff >= 0 && f.getAbsolutePath().endsWith(fileName)
                    // uncomment lines below -- untested but it makes this fcn
                    // correct
                    // && ( lengthDiff == 0
                    // || f.getAbsolutePath().charAt( lengthDiff - 1 )
                    // == File.separatorChar )
                        ) {
                    files.add(f);
                }
            }
            long latestModified = 0;
            File latestModifiedFile = null;
            for (File f : files) {
                long t = f.lastModified();
                if (t > latestModified) {
                    latestModified = t;
                    latestModifiedFile = f;
                }
            }
            file = latestModifiedFile;
        }
        return file;
    }

    public static File[] filesInDirectory(final String fileName) {
        File[] dirFiles = null;
        File f = existingFile(fileName);
        if (f == null) {
            f = findFile(fileName);
        }
        if (f != null && f.isDirectory()) {
            dirFiles = f.listFiles();
        }
        return dirFiles;
    }

    public static String removeFileExtension(String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos >= 0) {
            return fileName.substring(0, pos);
        }
        return fileName;
    }

    public static String getExtension(String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos >= 0 && pos < fileName.length() - 1) {
            return fileName.substring(pos + 1);
        }
        return "";
    }

    public static String fixFileName(String fileName) {
        String newName = fileName.replaceAll("[:;+]", "-");
        newName = fileName.replaceAll("[+*&%#@^(){}]+", "_");
        return newName;
    }

    public static String fileToString(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        return fileToString(file);
    }

    public static String fileToString(File file) throws FileNotFoundException {
        String s = null;
        try {
            s = new Scanner(file).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static void stringToFile(String s, String fileName) {
        File file = new File(fileName);
        stringToFile(s, file);
    }

    public static void stringToFile(String s, File file) {
        FileWriter outFile = null;
        try {
            outFile = new FileWriter(file);
            PrintWriter out = new PrintWriter(outFile);
            out.print(s);
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getFileStringContent(String filename) throws FileNotFoundException {
        String text = new Scanner(new File(filename)).useDelimiter("\\A").next();
        return text;
    }
}
