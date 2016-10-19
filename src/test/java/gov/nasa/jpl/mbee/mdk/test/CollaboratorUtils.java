package gov.nasa.jpl.mbee.mdk.test;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;

/**
 * Created by brower on 7/14/16.
 */
public class CollaboratorUtils {

    private static String username = "username";
    private static String password = "password";
    private static String alfresco_repository_url = "https://cae-teamworkcloud-uat.jpl.nasa.gov";
    private static String location_root = "Repository/Shared";
    private static String full_document_name = "MyDocument";
    private static String overwrite_existing_document = "true";
    private static String comments_enabled = "true";
    private static String diagram_image_type = "PNG";
    private static String project_location = "/Users/cmcmilla/Documents/TestProjects/TestProject_2.mdzip";
    private static String scope = "Model";
    private static String template_document_name = "Entire Model";
    private static String site_url;

    private static boolean teamworkProject = false;
    private static boolean twcProject = false;
    private static String server_username = "";
    private static String server_password = "";
    private static String server_url = "";
    private static String project_version = null;
    private static String branch_name = null;

    private static boolean failed = false; // used to set a flag if the publish command failed
    public static StringBuilder publish(String cmd, String dir) throws IOException {
        StringBuilder diff = new StringBuilder();

//        String ref = reference.getAbsolutePath();
//        String out = output.getAbsolutePath();
//        String name = output.getName();

        String ref = "";
        String out = "";
        String name = "";

        System.out.println("running command: " + cmd);



       // cmd = "echo 'hello world'";
        String[] command = cmd.split(" ");



        ProcessBuilder p = new ProcessBuilder(command);
        p.directory(new File(dir));
        //p.redirectErrorStream();
        Process p2 = p.start();



        BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p2.getInputStream()));
        //BufferedReader stdOutput = new BufferedReader(new )
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p2.getErrorStream()));

        /*try {
            p2.waitFor();
        }catch (Exception e) {
            System.out.println("Error waiting for process");
        }*/

        System.out.println("Standard output " + stdOutput.readLine());

        // read the output from the command
        String s = null;
        while ((s = stdOutput.readLine()) != null) {
            diff.append("stdOut: " + s + "\n");
            if (s.contains("Error: Could not complete publishing:")) {
                failed = true;
            }
        }

        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            diff.append("ERROR: " + s + "\n");
            if (s.contains("Error: Could not complete publishing:")) {
                failed = true;
            }
        }

        System.out.println(diff);

        stdOutput.close();

        assertFalse("Error: Could not complete publishing", failed);

        return diff;
    }

    public static void writeTemplate(String fileName) throws Exception{
        String testRoot;

       testRoot = Paths.get("").toAbsolutePath().toString();
        File properties = new File(testRoot + "/mdk/resource/collaborator/template.properties");

        System.out.println("\nOpening file " + testRoot + "/mdk/resource/collaborator/template.properties");

        BufferedReader reader = new BufferedReader(new FileReader(properties));
        StringBuffer buffer = new StringBuffer();
        String line;

        while((line = reader.readLine()) != null)
        {
            buffer.append(line);
            buffer.append("\r\n");
        }
        reader.close();


        Map<String, String> replacementMap = new HashMap<String, String>();

        replacementMap.put("USERNAME", username);
        replacementMap.put("PASSWORD", password);
        replacementMap.put("ALFRESCO_URL", alfresco_repository_url);

        replacementMap.put("BASE_REPOSITORY", location_root);
        if (location_root.equals("Repository/Site")) {
            replacementMap.put("#site_url", "site_url");
            replacementMap.put("URL_SITE", site_url);
        }
        replacementMap.put("EXAMPLE_DOCUMENT_NAME", full_document_name);
        replacementMap.put("OVERWRITE_DOC_OPTION", overwrite_existing_document);
        replacementMap.put("COMMENT_OPTION", comments_enabled);

        validateDiagramFileType();
        replacementMap.put("DIAGRAM_FILE_TYPE", diagram_image_type);
        replacementMap.put("PROJECT_FILE_PATH", project_location);
        replacementMap.put("MODEL_SCOPE", scope);
        replacementMap.put("DOCUMENT_TEMPLATE", template_document_name);

        if (teamworkProject || twcProject) {
            replacementMap.put("#server_username", "server_username");
            replacementMap.put("USERNAME_SERVER", server_username);

            replacementMap.put("#server_password", "server_password");
            replacementMap.put("PASSWORD_SERVER", server_password);

            replacementMap.put("#server_url", "server_url");
            replacementMap.put("EXAMPLE_SERVER_URL", server_url);

            replacementMap.put("#server_type", "server_type");
            if (teamworkProject) {
                replacementMap.put("EXAMPLE_SERVER_TYPE", "Teamwork");
            }else {
                replacementMap.put("EXAMPLE_SERVER_TYPE", "TWC");
            }

            if (project_version != null) {
                replacementMap.put("#project_version", "project_version");
                replacementMap.put("EXAMPLE_PROJECT_VERSION", project_version);
            }
            if (branch_name != null) {
                replacementMap.put("#branch_name", "branch_name");
                replacementMap.put("EXAMPLE_BRANCH", branch_name);
            }

        }

        String toWrite = buffer.toString();            //replace all parameter variables with values
        for (Map.Entry<String, String> entry : replacementMap.entrySet())
        {
            toWrite = toWrite.replaceAll(entry.getKey(), entry.getValue());           //create new publishing options
        }


        FileWriter writer = new FileWriter(fileName);
        writer.write(toWrite);
        writer.close();
    }


    public static String getUsername() {
        return username;
    }

    public static void setUsername(String newUsername) {
        username = newUsername;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String pword) {
        password = pword;
    }

    public static String getAlfresco_repository_url() {
        return alfresco_repository_url;
    }

    public static String getLocation_root() {
        return location_root;
    }

    public static String getFull_document_name() {
        return full_document_name;
    }

    public static String getOverwrite_existing_document() {
        return overwrite_existing_document;
    }

    public static String getComments_enabled() {
        return comments_enabled;
    }

    public static String getDiagram_image_type() {
        return diagram_image_type;
    }

    public static String getProject_location() {
        return project_location;
    }

    public static String getScope() {
        return scope;
    }

    public static String getTemplate_document_name() {
        return template_document_name;
    }

    public boolean isTeamworkProject() {
        return teamworkProject;
    }

    public boolean isTwcProject() {
        return twcProject;
    }

    public static String getServer_username() {
        return server_username;
    }

    public static String getServer_password() {
        return server_password;
    }

    public static String getServer_url() {
        return server_url;
    }

    public static String getProject_version() {
        return project_version;
    }

    public static String getBranch_name() {
        return branch_name;
    }

    public static void setAlfresco_repository_url(String alfresco_repository_url) {
        CollaboratorUtils.alfresco_repository_url = alfresco_repository_url;
    }

    public static void setLocation_root(String location_root) throws Exception{
        try {
            validateLocationRoot(location_root);
            CollaboratorUtils.location_root = location_root;
        } catch (Exception e) {
            throw e;
        }
    }

    public static void setFull_document_name(String full_document_name) {
        CollaboratorUtils.full_document_name = full_document_name;
    }

    public static void setOverwrite_existing_document(String overwrite_existing_document) {
        CollaboratorUtils.overwrite_existing_document = overwrite_existing_document;
    }

    public static void setComments_enabled(String comments_enabled) {
        CollaboratorUtils.comments_enabled = comments_enabled;
    }

    public static void setDiagram_image_type(String diagram_image_type) {
        CollaboratorUtils.diagram_image_type = diagram_image_type;
    }

    public static void setProject_location(String project_location) {
        CollaboratorUtils.project_location = project_location;
    }

    public static void setScope(String scope) {
        CollaboratorUtils.scope = scope;
    }

    public static void setTemplate_document_name(String template_document_name) {
        CollaboratorUtils.template_document_name = template_document_name;
    }

    public static void setTeamworkProject(boolean teamworkProject) {
        CollaboratorUtils.teamworkProject = teamworkProject;
    }

    public static void setTwcProject(boolean twcProject) {
        CollaboratorUtils.twcProject = twcProject;
    }

    public static void setServer_username(String server_username) {
        CollaboratorUtils.server_username = server_username;
    }

    public static void setServer_password(String server_password) {
        CollaboratorUtils.server_password = server_password;
    }

    public static void setServer_url(String server_url) {
        CollaboratorUtils.server_url = server_url;
    }

    public static void setProject_version(String project_version) {
        CollaboratorUtils.project_version = project_version;
    }

    public static String getSite_url() {
        return site_url;
    }

    public static void setSite_url(String site_url) {
        CollaboratorUtils.site_url = site_url;
    }

    public static void setBranch_name(String branch_name) {
        CollaboratorUtils.branch_name = branch_name;
    }

    private static void validateLocationRoot(String root) throws Exception{

        if (root.equals("Repository")) {
            location_root = root;
        } else if (root.equals("Repository/Shared")) {
            location_root = root;
        } else if (root.equals("Repository/Site")) {
            location_root = root;
        } else if (root.equals("Repository/Guest Home")) {
            location_root = root;
        } else if (root.equals("Repository/User Home")) {
            location_root = root;
        } else {
            throw new Exception("Invalid location root");
        }
    }

    private static void validateDiagramFileType() throws Exception{
        if (!diagram_image_type.equals("PNG") && !diagram_image_type.equals("SVG") && !diagram_image_type.equals("SVG_PNG")) {
            throw new Exception("Invalid image type");
        }
    }

    public static String getMDInstallDirectory() {
        try {
            String testRoot = Paths.get("").toAbsolutePath().toString();
            File mdInstallRootFile = new File(testRoot + "/mdk/resource/collaborator/mdinstall.txt");

            BufferedReader reader = new BufferedReader(new FileReader(mdInstallRootFile));
            String buffer = "";
            String line;

            while ((line = reader.readLine()) != null) {
                buffer += line;
            }
            reader.close();

            return buffer;

        }catch (Exception e) {
            System.out.println("There was an error reading the file containing the MagicDraw installation directory");
            return null;
        }
    }




}
