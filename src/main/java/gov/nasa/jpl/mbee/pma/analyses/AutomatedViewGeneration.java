package gov.nasa.jpl.mbee.pma.analyses;

import com.nomagic.magicdraw.commandline.CommandLine;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.ITeamworkService;
import com.nomagic.magicdraw.teamwork2.ServerLoginInfo;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.MagicDrawHelper;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.*;

// TODO Redo in @donbot
public class AutomatedViewGeneration extends CommandLine {

    // needed because CommandLine redirects it, and we want the output
    private static final PrintStream stdout = System.out;

    // cancel handler stuff
    // cancel is set when the cancelHandler is triggered. it is used as a flag so the running
    //    program can discontinue normal flow, throw a cancel exception, and notify the
    //    waiting cancelHandler
    // running is used to indicate whether we are in docweb scope or not
    //    this is needed because the cancel handler will trigger before we're fully loaded
    //    and then be stuck waiting for notification that never comes
    private static boolean debug = false,
            cancel = false,
            running = false,
            twLogin = false,
            twLoaded = false;

    private static byte error = 0;

    private static int argIndex = 0,
            applicationAccounts = 1;

    private static String testRoot = "",
            credentialsLocation = "",
            teamworkUsername = "",
            teamworkPassword = "",
            teamworkServer = "",
            teamworkPort = "",
            teamworkProject = "",
            teamworkBranchName = "master";

    private static final List<String> viewList = new ArrayList<>(),
            messageLog = new ArrayList<>();

    private static InterruptTrap cancelHandler;

    protected static final Object lock = new Object();

    /*//////////////////////////////////////////////////////////////
     *
     * Execution methods
     *
    /*//////////////////////////////////////////////////////////////

    @Override
    protected byte execute() {
        // send output back to stdout
        System.setOut(AutomatedViewGeneration.stdout);
        System.setErr(AutomatedViewGeneration.stdout);

        // disable logJson, in case it's on, to make the logs not hideous
        MDKOptionsGroup.getMDKOptions().setLogJson(false);

        // start the cancel handler so we don't terminate in the middle of a view sync operation
        // and so we can force logout if logged in to teamwork
        cancelHandler = new InterruptTrap();
        Runtime.getRuntime().addShutdownHook(cancelHandler);

        running = true;
        try {
            String msg = "Performing automated view generation";
            System.out.println(msg);
            messageLog.add(msg);
            // login TeamworkCloud, set MMS credentials
            loginTeamwork();
            // open project
            loadTeamworkProject();
            // confirm MMS write permissions
            checkSiteEditPermission();
            // generate views and commit images
            generateViewsForDocList();
            // logout in finally
        } catch (Error err) {
            error = 99;
            System.out.println(err.toString());
            err.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (twLoaded) {
                // close project
                System.out.println("[OPERATION] Closing open project");
                Application.getInstance().getProjectsManager().closeProject();
            }
            if (twLogin) {
                // logout
                System.out.println("[OPERATION] Logging out of teamwork");
                EsiUtils.getTeamworkService().logout();
            }

            try {
                checkFailure();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return error;
    }

    /*////////////////////////////////////////////////////////////////
     *
     * "User Operation" methods
     *
    /*////////////////////////////////////////////////////////////////

    /**
     * Logs in to teamwork using the initially loaded teamwork account. If that account
     * is already in use, will load/generate and attempt to log in with additional sets
     * of credentials, up to the limit specified in applicationAccounts.
     *
     * @throws FileNotFoundException missing credentialsLocation
     * @throws UnsupportedEncodingException logMessage failures
     * @throws InterruptedException cancel triggered and caught by cancel handler
     * @throws IllegalAccessException access failure with loaded credentials
     */

    private void loginTeamwork()
            throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IllegalAccessException {
        // disable all mdk popup warnings
        MDKHelper.setPopupsDisabled(true);

        String message = "[OPERATION] Logging in to Teamwork";
        logMessage(message);

        ITeamworkService twcService = EsiUtils.getTeamworkService();
        for (int i = 1; i <= applicationAccounts; i++) {
            try {
                String appendage = (i == 1 ? "" : Integer.toString(i));
                loadCredentials(appendage);
                // LOG: credentials have loaded from credentialsLocation
            } catch (IOException e) {
                error = 100;
                message = "[FAILURE] Unable to find credentials at location " + Paths.get(credentialsLocation).toAbsolutePath().toString();
                logMessage(message);
                throw new IllegalStateException(message, e);
            }
            if (i == 1) {
                try {
                    reportStatus("running", debug);
                } catch (IOException e) {
                    throw new IllegalAccessException("Automated View Generation failed - User " + teamworkUsername + " can not edit site (check Alfresco site membership).");
                }
            }

            twcService.login(new ServerLoginInfo(teamworkServer + ":" + Integer.parseInt(teamworkPort),
                    teamworkUsername, teamworkPassword, true), true);
            if (twcService.isConnected()) {
                // setting MMS credentials after successful teamwork login since we don't know which account was used before hand
                MDKHelper.setMMSLoginCredentials(teamworkUsername, teamworkPassword);
                message = "Logged in to Teamwork as " + teamworkUsername + " on " + teamworkServer + ":" + teamworkPort;
                logMessage(message);
                // LOG: successfully logged in to the teamwork cloud server
                break;
            }

            message = "Unable to log in to Teamwork as " + teamworkUsername + " on " + teamworkServer + ":" + teamworkPort;
            logMessage(message);
            // LOG: this user failed to log in
        }

        if (!twcService.isConnected()) {
            error = 101;
            message = "[FAILURE] Unable to log in to Teamwork as available account(s).";
            logMessage(message);
            throw new IllegalStateException(message);
        }
        twLogin = true;
        checkCancel();
    }

    /**
     * Loads the Teamwork project. Complains if it fails.
     *
     * @throws FileNotFoundException can't find teamwork project or branch
     * @throws UnsupportedEncodingException logMessage failures
     * @throws InterruptedException cancel triggered and caught by cancel handler
     * @throws IllegalAccessException access failure with loaded credentials
     * @throws RemoteException error getting the projectDescriptor back from the twUtil
     */
    private void loadTeamworkProject()
            throws FileNotFoundException, UnsupportedEncodingException, RemoteException, IllegalAccessException, InterruptedException {
        String message;
        ProjectDescriptor projectDescriptor;

        if (teamworkProject.startsWith("PROJECT-")) {
            teamworkProject = teamworkProject.substring(8);
        }
        int index = teamworkProject.indexOf("ID_");
        if (index > 0) {
            teamworkProject = teamworkProject.substring(index);
        }

        // get the descriptor of the project trunk
        try {
            projectDescriptor = EsiUtils.getTeamworkService().getProjectDescriptorById(teamworkProject);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }

        // if trunk descriptor is null, error out and indicate projectId fail
        if (projectDescriptor == null) {
            message = "[FAILURE] Unable to find TeamworkCloud projectId " + teamworkProject;
            logMessage(message);
            error = 102;
            throw new FileNotFoundException(message);
        }

        // if we need a branch, update the descriptor to the branch descriptor
        if (!teamworkBranchName.isEmpty() && !teamworkBranchName.equals("master")) {
            //TODO verify this with a TWC server with branches and whatever (need projectID and some config still)
            projectDescriptor = EsiUtils.getDescriptorForBranch(projectDescriptor, teamworkBranchName);

            // if updated projectDescriptor is now null, error out and indicate branch problem
            if (projectDescriptor == null) {
                message = "[FAILURE] Unable to find TeamworkCloud project branch " + projectDescriptor.getRepresentationString() + "/" + teamworkBranchName;;
                logMessage(message);
                error = 102;
                throw new FileNotFoundException(message);
            }
        }

        // we have a valid project descriptor, so load the associated project
        message = "[OPERATION] Loading TeamworkCloud project " + projectDescriptor.getRepresentationString();
        logMessage(message);
        Application.getInstance().getProjectsManager().loadProject(projectDescriptor, true);

        // if not access to project, loaded project will be null, so error out
        if (Application.getInstance().getProject() == null) {
            message = "[FAILURE] User does not have access to " + teamworkProject;
            logMessage(message);
            error = 102;
            throw new IllegalAccessException(message);
        }
        twLoaded = true;

        // move the stored message log into the MD notification window. This will mess up the time stamps, but will
        // keep all of the messages in the same place
        while (!messageLog.isEmpty()) {
            Application.getInstance().getGUILog().log(messageLog.remove(0));
        }
        message = "Opened TeamworkCloud project";
        logMessage(message);
        // LOG: successfully opened the Teamwork project

        checkCancel();
    }

    /**
     * Checks to ensure that the logged in user has edit permissions to site.
     *
     * @throws IOException either a logMessage failure or a isSiteEditable failure
     * @throws InterruptedException cancel triggered and caught by cancel handler
     * @throws IllegalAccessException access failure with loaded credentials
     * @throws ServerException server error
     * @throws URISyntaxException uri addressing error
     */
    private void checkSiteEditPermission()
            throws IllegalAccessException, InterruptedException, ServerException, URISyntaxException, IOException {
        if (!MDKHelper.loginToMMS(teamworkUsername, teamworkPassword)) {
            String message = "[FAILURE] User " + teamworkUsername + " failed to login to MMS.";
            logMessage(message);
            error = 103;
            throw new IllegalAccessException("Automated View Generation failed - User " + teamworkUsername + " can not log in to MMS server.");
            // LOG: Invalid account
        }
        if (!MDKHelper.isSiteEditable()) {
            String message = "[FAILURE] User " + teamworkUsername + " does not have permission to MMS site or MMS is unsupported version.";
            logMessage(message);
            error = 103;
            throw new IllegalAccessException("Automated View Generation failed - User " + teamworkUsername + " can not edit site (check Alfresco site membership) or MMS version < 2.3.8.");
            // LOG: Account lacks write permissions or mms < v2.3.8
        }
        checkCancel();
    }

    /**
     * Generates views and commits images for each document / view in the docList
     * sequentially. If an element is not found, skips generation and continues
     * through list, and will throw an exception at the end.
     *
     * @throws FileNotFoundException one or more documents not found in project, or logMessage failure
     * @throws InterruptedException cancel triggered and caught by cancel handler
     * @throws UnsupportedEncodingException logMessage failure
     */
    private void generateViewsForDocList()
            throws FileNotFoundException, InterruptedException, UnsupportedEncodingException {
        String msg = "[OPERATION] Triggering view generation on MMS";
        logMessage(msg);
        boolean failedDocs = false;
        for (String elementID : viewList) {
            NamedElement document = (NamedElement) Converters.getIdToElementConverter()
                    .apply(elementID, Application.getInstance().getProject());
            if (document == null) {
                msg = "[FAILURE] Unable to find element \"" + elementID + "\"";
                logMessage(msg);
                // LOG: the element which caused a failure and didnd't generate
                failedDocs = true;
            }
            else {
                msg = "Generating views for \"" + document.getName() + "\".";
                logMessage(msg);
                // LOG: the element which is being generated currently
                MDKHelper.generateViews(document, true);
                // required for the auto-image commit wait, and probably not harmful in other circumstances
                MDKHelper.mmsUploadWait();
            }
        }
        if (failedDocs) {
            error = 104;
            throw new FileNotFoundException("Automated View Generation Failed - Unable to find Document(s)");
            // LOG: AVG FAILED AT THIS POINT
        }
        checkCancel();
    }

    /*//////////////////////////////////////////////////////////////////
     *
     * Helper methods
     *
    /*//////////////////////////////////////////////////////////////////

    /**
     * parses arguments passed in from command line
     *
     * @param args Argument string array from the console
     */
    @Override
    protected void parseArgs(String[] args) {
        // iteration of argIndex is handled by following code to account for
        // variable length arguments with whitespace
        for (argIndex = 0; argIndex < args.length; ) {
            if (args[argIndex].startsWith("-")) {
                switch (args[argIndex]) {
                    case "-crdlc":
                        credentialsLocation = buildArgString(args);
                        break;
                    case "-debug":
                        debug = true;
                        argIndex++;
                        break;
                    case "-doclist":
                        String csvDocumentList = buildArgString(args);
                        Collections.addAll(viewList, csvDocumentList.split(" && "));
                        System.out.println();
                        break;
                    case "-tstrt":
                        testRoot = buildArgString(args);
                        testRoot = testRoot + (testRoot.length() > 0 && testRoot.charAt(testRoot.length() - 1) == '/' ? "" : "/");
                        if (testRoot.equals("/")) {
                            testRoot = "";
                        }
                        break;
                    case "-twbrn":
                        teamworkBranchName = buildArgString(args);
                        break;
                    case "-twprj":
                        teamworkProject = buildArgString(args);
                        if (teamworkProject.startsWith("twcloud:/")) {
                            teamworkProject = teamworkProject.substring(9);
                        }
                        break;
                    case "-wkspc":
                        teamworkBranchName = buildArgString(args);
                        break;
                    default:
                        System.out.println("Invalid flag passed: " + argIndex + " " + args[argIndex++]);
                }
            }
            else {
                System.out.println("Invalid parameter passed: " + argIndex + " " + args[argIndex]);
            }
            argIndex++;
        }
        if (!Paths.get(credentialsLocation).toFile().exists()) {
            credentialsLocation = testRoot + credentialsLocation;
        }
    }

    private String buildArgString(String[] args) {
        StringBuilder spacedArgument = new StringBuilder("");
        while ((argIndex + 1) < args.length && !args[argIndex + 1].startsWith("-")) {
            spacedArgument.append(args[++argIndex]);
            spacedArgument.append(" ");
        }
        if (spacedArgument.length() > 0) {
            spacedArgument.setLength(spacedArgument.length() - 1);
        }
        return spacedArgument.toString();
    }

    private void loadCredentials(String append) throws IOException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(credentialsLocation)) {
            prop.load(input);
            if (prop.containsKey("app.accounts")) {
                try {
                    applicationAccounts = Integer.parseInt(prop.getProperty("app.accounts"));
                } catch (NumberFormatException nfe) {
                    applicationAccounts = 1;
                    System.out.println("[WARNING] Unable to parse number specified for app.accounts. Using default.");
                }
            }
            teamworkServer = prop.getProperty("tw.url");
            if (teamworkServer.contains("//")) {
                teamworkServer = teamworkServer.substring(teamworkServer.indexOf("//") + 2);
            }
            if (teamworkServer.lastIndexOf(':') != -1) {
                teamworkServer = teamworkServer.substring(0, teamworkServer.lastIndexOf(':'));
            }
            teamworkPort = prop.getProperty("tw.port");
            if (prop.containsKey("tw.user" + append)) {
                teamworkUsername = prop.getProperty("tw.user" + append);
                teamworkPassword = prop.getProperty("tw.pass" + append);
            }
            else {
                teamworkUsername = prop.getProperty("tw.user") + append;
                teamworkPassword = prop.getProperty("tw.pass") + append;
            }
        }
    }

    private void checkCancel() throws InterruptedException {
        synchronized (lock) {
            if (cancel) {
                error = 127;
                throw new InterruptedException("Cancel signal received.");
            }
        }
    }

    private void checkFailure() throws IOException {
        synchronized (lock) {
            running = false;
            if (cancel) {
                lock.notify();
            }
            else {
                Runtime.getRuntime().removeShutdownHook(cancelHandler);
                if (error == 0) {
                    reportStatus("completed", debug);
                    System.out.println("Automated View Generation completed without errors.\n");
                } else {
                    reportStatus("failed", debug);
                    System.out.println("Automated View Generation did not finish successfully. Operations were logged in MDNotificationWindowText.html.\n");
                }
//                System.exit(error);
            }
        }
    }

    private void logMessage(String msg) throws FileNotFoundException, UnsupportedEncodingException {
        if (messageLog.size() > 0) {
            if (!msg.isEmpty()) {
                messageLog.add(msg);
                System.out.println(msg);
            }
            exportMessageLog();
        }
        else {
            if (!msg.isEmpty()) {
                MagicDrawHelper.generalMessage(msg);
            }
            exportGUILog();
        }
    }

    private void exportGUILog() throws FileNotFoundException, UnsupportedEncodingException {
        String guiLog = Application.getInstance().getGUILog().getLoggedMessages();
        try (PrintWriter writer = new PrintWriter(testRoot + "MDNotificationWindowText" + ".html", "UTF-8")) {
            writer.println(guiLog);
        }
    }

    private void exportMessageLog() throws FileNotFoundException, UnsupportedEncodingException {
        String guiLog = generateLog();
        try (PrintWriter writer = new PrintWriter(testRoot + "MDNotificationWindowText" + ".html", "UTF-8")) {
            writer.println(guiLog);
        }
    }

    private String generateLog() {
        StringBuilder sb = new StringBuilder("");
        sb.append("<html>\n")
                .append("\t<head>\n")
                .append("\t</head>\n")
                .append("\n")
                .append("\t<body>\n")
                .append("\t\t<table cellpadding=\"0\" cellspacing=\"0\">\n");
        for (String msg : messageLog) {
            sb.append("\t\t\t<tr>\n").append("\t\t\t\t<td>\n")
                    .append("\t\t\t\t\t<div class=\"info\">\n")
                    .append("\t\t\t\t\t\t")
                    .append(msg)
                    .append("\n")
                    .append("\t\t\t\t\t</div>\n")
                    .append("\t\t\t\t</td>\n")
                    .append("\t\t\t</tr>\n");
        }
        sb.append("\n")
                .append("\t\t</table>\n")
                .append("\t</body>\n")
                .append("</html>");
        return sb.toString();
    }

    private void reportStatus(String status, boolean verbose) throws IOException {
        System.out.println("Updating status: " + status);
        Map<String, String> envvars = System.getenv();
        String JOB_ID;
        String MMS_SERVER;
        if (!(envvars.containsKey("MMS_SERVER") && envvars.containsKey("JOB_ID"))) {
            System.out.println("MMS_SERVER or JOB_ID not specified");
            return;
        }
        else {
            JOB_ID = envvars.get("JOB_ID");
            MMS_SERVER = envvars.get("MMS_SERVER");
        }

        URL url = new URL(MMS_SERVER + "/alfresco/service/workspaces/" + teamworkBranchName + "/jobs");
        String data = "{\"jobs\":[{\"sysmlid\":\"" + JOB_ID + "\", \"status\":\"" + status + "\"}]}";
        byte[] postData = data.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        String auth = teamworkUsername + ":" + teamworkPassword;
        String encodedAuth = "Basic " + DatatypeConverter.printBase64Binary(auth.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", encodedAuth);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(postData.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postData);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        for (int c; (c = in.read()) >= 0; ) {
            if (verbose) {
                System.out.print((char) c);
            }
        }
    }

    private class InterruptTrap extends Thread {
        @Override
        public void run() {
            if (running) {
                cancel = true;
                try {
                    reportStatus("aborting", debug);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    System.setOut(AutomatedViewGeneration.stdout);
                    String msg = "Cancel received. Will complete current operation, logout, and terminate (max delay: 15min).";
                    try {
                        logMessage(msg);
                    } catch (FileNotFoundException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        int mins = 15;
                        for (int i = 0; i < mins * 12; i++) {
                            if (!running) {
                                break;
                            }
                            lock.wait(5000);
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                try {
                    reportStatus("aborted", debug);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runtime.getRuntime().removeShutdownHook(cancelHandler);
                Runtime.getRuntime().halt(error);
            }
        }
    }
}
