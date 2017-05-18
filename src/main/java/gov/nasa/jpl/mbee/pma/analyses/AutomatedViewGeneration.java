package gov.nasa.jpl.mbee.pma.analyses;

import com.nomagic.magicdraw.commandline.CommandLine;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.ITeamworkService;
import com.nomagic.magicdraw.teamwork2.ServerLoginInfo;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.mms.sync.queue.OutputSyncRunner;
import gov.nasa.jpl.mbee.mdk.mms.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.Pair;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomatedViewGeneration extends CommandLine {

    private org.apache.commons.cli.CommandLine parser;

    // needed because CommandLine redirects it, and we want the output
    private static final PrintStream stdout = System.out;

    private boolean twLogin = false,
            twLoaded = false;

    // cancel handler stuff
    // cancel is set when the cancelHandler is triggered. it is used as a flag so the running program can discontinue normal flow, throw a cancel exception, and notify the waiting cancelHandler
    // running is used to indicate whether we are in docweb scope or not. this is needed because the cancel handler will trigger before we're fully loaded and then be stuck waiting for notification that never comes
    private AtomicBoolean cancel = new AtomicBoolean(false),
            running = new AtomicBoolean(true);

    private byte error = 0;

    private Project project;

    private final List<String> messageLog = new ArrayList<>();

    private InterruptTrap cancelHandler;

    private final Object lock = new Object();

    private final int cancelDelay = 15;

    /*//////////////////////////////////////////////////////////////
     *
     * Execution methods
     *
    /*//////////////////////////////////////////////////////////////

    @Override
    protected byte execute() {
        try {
            // send output back to stdout
            System.setOut(AutomatedViewGeneration.stdout);

            // start the cancel handler so we don't terminate in the middle of a view sync operation and so we can force logout if logged in to teamwork
            cancelHandler = new InterruptTrap();
            Runtime.getRuntime().addShutdownHook(cancelHandler);

            System.out.println("\n**********************\n");
            System.out.println("[INFO] Performing automated view generation.");

            MDKOptionsGroup.getMDKOptions().setLogJson(parser.hasOption("debug"));
            if (parser.hasOption("debug")) {
                System.out.println("[DEBUG] JSON will be saved." + MDKOptionsGroup.getMDKOptions().isLogJson());
            }

            // login TeamworkCloud, set MMS credentials
            loginTeamwork();

            // open project
            loadTeamworkProject();

            // generate views and commit images
            generateViews();

            // logout in finally
        } catch (Error err) {
            error = 99;
            System.out.println(err.toString());
            err.printStackTrace();
        } catch (Exception e) {
            error = 100;
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
            running.set(false);
            try {
                synchronized (lock) {
                    if (cancel.get()) {
                        lock.notify();
                    }
                    else {
                        Runtime.getRuntime().removeShutdownHook(cancelHandler);
                        if (error == 0) {
                            reportStatus("completed");
                            System.out.println("[INFO] Automated View Generation completed without errors.");
                        }
                        else {
                            reportStatus("failed");
                            System.out.println("[WARNING] Automated View Generation did not finish successfully. Operations were logged in MDNotificationWindowText.html.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\n**********************\n");
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
     * @throws FileNotFoundException        missing credentialsLocation
     * @throws UnsupportedEncodingException logMessage failures
     * @throws InterruptedException         cancel triggered and caught by cancel handler
     * @throws IllegalAccessException       access failure with loaded credentials
     */

    private void loginTeamwork()
            throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IllegalAccessException {
        // disable all mdk popup warnings
        MDKHelper.setPopupsDisabled(true);

        String message = "[OPERATION] Logging in to Teamwork.";
        logMessage(message);

        ITeamworkService twcService = EsiUtils.getTeamworkService();
        if (!(parser.hasOption("twcHost") && parser.hasOption("twcPort") && parser.hasOption("twcUsername") && parser.hasOption("twcPassword"))) {
            illegalStateFailure("[FAILURE] Unable to log in to Teamwork Cloud, one or more of the required twc parameters were missing.");
        }

        twcService.login(new ServerLoginInfo(parser.getOptionValue("twcHost") + ":" + parser.getOptionValue("twcPort"),
                parser.getOptionValue("twcUsername") , parser.getOptionValue("twcPassword") , true), true);

        if (!twcService.isConnected()) {
            illegalStateFailure("[FAILURE] Unable to log in to Teamwork Cloud with specified credentials.");
        }
        twLogin = true;
        checkCancel();
    }


    /**
     * Loads the Teamwork project. Complains if it fails.
     *
     * @throws FileNotFoundException        can't find teamwork project or branch
     * @throws UnsupportedEncodingException logMessage failures
     * @throws InterruptedException         cancel triggered and caught by cancel handler
     * @throws IllegalAccessException       access failure with loaded credentials
     * @throws RemoteException              error getting the projectDescriptor back from the twUtil
     */
    private void loadTeamworkProject()
            throws FileNotFoundException, UnsupportedEncodingException, RemoteException, IllegalAccessException, InterruptedException, URISyntaxException {
        String message;

        if (!(parser.hasOption("mmsUsername") && parser.hasOption("mmsPassword"))) {
            illegalStateFailure("[FAILURE] Unable to specify MMS credentials, one or more of the required mms parameters were missing.");
        }
        message = "[OPERATION] Specifying MMS credentials.";
        logMessage(message);
        MDKHelper.setMMSLoginCredentials(parser.getOptionValue("mmsUsername"), parser.getOptionValue("mmsPassword"));

        if (!(parser.hasOption("projectId") && parser.hasOption("refId") && parser.hasOption("targetViewId"))) {
            illegalStateFailure("[FAILURE] Unable to load project, one or more of the required ID parameters were missing.");
        }
        message = "[OPERATION] Loading Teamwork Cloud project.";
        logMessage(message);

        String uri = "twcloud:/" + parser.getOptionValue("projectId") + "/" + parser.getOptionValue("refId");
        ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory.createProjectDescriptor(new java.net.URI(uri));
        // if updated projectDescriptor is now null, error out and indicate branch problem
        if (projectDescriptor == null) {
            illegalStateFailure("[FAILURE] Unable to find TeamworkCloud project " + uri + ".");
        }
        // we have a valid project descriptor, so load the associated project
        Application.getInstance().getProjectsManager().loadProject(projectDescriptor, true);

        // if not access to project, loaded project will be null, so error out
        if (Application.getInstance().getProject() == null) {
            illegalStateFailure("[FAILURE] User does not have permission to load " + projectDescriptor.getRepresentationString() + ".");
        }
        twLoaded = true;
        project = Application.getInstance().getProject();

        // move the stored message log into the MD notification window. This will mess up the time stamps, but will keep all of the messages in the same place
        while (!messageLog.isEmpty()) {
            Application.getInstance().getGUILog().log(messageLog.remove(0));
        }
        message = "Opened TeamworkCloud project (preceding timestamps may be invalid).";
        logMessage(message);
        checkCancel();
    }

    /**
     * Generates views and commits images for each document / view in the docList
     * sequentially. If an element is not found, skips generation and continues
     * through list, and will throw an exception at the end.
     *
     * @throws FileNotFoundException        one or more documents not found in project, or logMessage failure
     * @throws InterruptedException         cancel triggered and caught by cancel handler
     * @throws UnsupportedEncodingException logMessage failure
     */
    private void generateViews() throws Exception {
        String message;
        if (!TicketUtils.isTicketSet(project)) {
            TicketUtils.setUsernameAndPassword(parser.getOptionValue("mmsUsername"), parser.getOptionValue("mmsPassword"));
            if (!MMSLoginAction.loginAction(project)) {
                illegalStateFailure("[FAILURE] User " + parser.getOptionValue("mmsUsername") + " was unable to login to MMS.");
            }
        }

        Element targetView = Converters.getIdToElementConverter().apply(parser.getOptionValue("targetViewId"), Application.getInstance().getProject());
        if (targetView == null) {
            illegalStateFailure("[ERROR] Unable to find element \"" + parser.getOptionValue("targetViewId") + "\"");
        }
        OutputSyncRunner.clearLastExceptionPair();
        message = "[OPERATION] Generating " + targetView.getHumanName() + (parser.hasOption("generateRecursively") ? " views recursively." : ".");
        logMessage(message);
        // LOG: the element which is being generated currently

        MDKHelper.generateViews(targetView, parser.hasOption("generateRecursively"));
        // wait is required for the auto-image commit, and it helps tie exceptions in output queue to their document
        MDKHelper.mmsUploadWait();
        if (OutputSyncRunner.getLastExceptionPair() != null) {
            Pair<Request, Exception> current = OutputSyncRunner.getLastExceptionPair();
            Exception e = current.getValue();
            if (e instanceof ServerException && ((ServerException) e).getCode() == 403) {
                message = "[ERROR] Unable to generate " + targetView.getHumanName() + ". User " + parser.getOptionValue("mmsUsername") + " does not have permission to write to the MMS in this branch.";
                logMessage(message);
            }
            else {
                message = "[ERROR] Unexpected error while generating " + targetView.getHumanName() + ". Reason: " + e.getMessage();
                logMessage(message);
                throw e;
            }
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
    protected void parseArgs(String[] args) throws ParseException {
        Option mmsHostOption = new Option("mmsHost", true, "MMS host name.");
        Option mmsPortOption = new Option("mmsPort", true, "MMS port number.");
        Option mmsUsernameOption = new Option("mmsUsername", true, "MMS username.");
        Option mmsPasswordOption = new Option("mmsPassword", true, "MMS password.");

        Option twcHostOption = new Option("twcHost", true, "Teamwork Cloud host name.");
        Option twcPortOption = new Option("twcPort", true, "Teamwork Cloud port number.");
        Option twcUsernameOption = new Option("twcUsername", true, "Teamwork Cloud username.");
        Option twcPasswordOption = new Option("twcPassword", true, "Teamwork Cloud password.");

        Option projectIdOption = new Option("projectId", true, "Project ID.");
        Option refIdOption = new Option("refId", true, "Ref (branch) ID.");
        Option targetViewIdOption = new Option("targetViewId", true, "Target view element ID.");
        Option generateRecursivelyOption = new Option("generateRecursively", "Generate views or target children recursively?");

        Option jobElementIdOption = new Option("jobElementId", true, "");

        Option debugOption = new Option("debug", "");

        // verbose option is understood by magicdraw to output information to command line. included here since the arg is also passed in here, and it causes errors if not expected
        Option verboseOption = new Option("verbose", "");

        Options parserOptions = new Options();

        parserOptions.addOption(mmsHostOption);
        parserOptions.addOption(mmsPortOption);
        parserOptions.addOption(mmsUsernameOption);
        parserOptions.addOption(mmsPasswordOption);

        parserOptions.addOption(twcHostOption);
        parserOptions.addOption(twcPortOption);
        parserOptions.addOption(twcUsernameOption);
        parserOptions.addOption(twcPasswordOption);

        parserOptions.addOption(projectIdOption);
        parserOptions.addOption(refIdOption);
        parserOptions.addOption(targetViewIdOption);
        parserOptions.addOption(generateRecursivelyOption);

        parserOptions.addOption(jobElementIdOption);
        parserOptions.addOption(debugOption);
        parserOptions.addOption(verboseOption);

        CommandLineParser commandLineParser = new BasicParser();
        parser = commandLineParser.parse(parserOptions, args);
    }

    private void checkCancel() throws InterruptedException {
        synchronized (lock) {
            if (cancel.get()) {
                error = 127;
                throw new InterruptedException("Cancel signal received.");
            }
        }
    }

    private void logMessage(String msg) throws FileNotFoundException, UnsupportedEncodingException {
        if (msg.isEmpty()){
            return;
        }
        System.out.println(msg);
        String guiLog;
        if (!messageLog.isEmpty()) {
            messageLog.add(msg);
            guiLog = generateLog();
        }
        else {
            Application.getInstance().getGUILog().log(msg);
            guiLog = Application.getInstance().getGUILog().getLoggedMessages();
        }
        File file = new File("MDNotificationWindowText.html");
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println(guiLog);
            if (parser.hasOption("debug")) {
                System.out.println("[DEBUG] Operation log file: " + file.getAbsolutePath());
            }
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

    private void reportStatus(String status) throws IOException {
//        if (!(parser.hasOption("mmsHost") && parser.hasOption("mmsPort"))) {
//            illegalStateFailure("[FAILURE] Unable to specify MMS credentials, one or more of the required mms parameters were missing.");
//        }
//        System.out.println("Updating status: " + status);
//        Map<String, String> envvars = System.getenv();
//        String JOB_ID;
//        String MMS_SERVER;
//        if (!(envvars.containsKey("MMS_SERVER") && envvars.containsKey("JOB_ID"))) {
//            System.out.println("MMS_SERVER or JOB_ID not specified");
//            return;
//        }
//        else {
//            JOB_ID = envvars.get("JOB_ID");
//            MMS_SERVER = envvars.get("MMS_SERVER");
//        }
//
//        URL url = new URL(MMS_SERVER + "/alfresco/service/workspaces/" + teamworkBranchName + "/jobs");
//        String data = "{\"jobs\":[{\"sysmlid\":\"" + JOB_ID + "\", \"status\":\"" + status + "\"}]}";
//        byte[] postData = data.getBytes(StandardCharsets.UTF_8);
//
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        String auth = teamworkUsername + ":" + teamworkPassword;
//        String encodedAuth = "Basic " + DatatypeConverter.printBase64Binary(auth.getBytes(StandardCharsets.UTF_8));
//        conn.setRequestProperty("Authorization", encodedAuth);
//
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Content-Type", "application/json");
//        conn.setRequestProperty("Content-Length", String.valueOf(postData.length));
//        conn.setDoOutput(true);
//        conn.getOutputStream().write(postData);
//
//        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//        for (int c; (c = in.read()) >= 0; ) {
//            if (verbose) {
//                System.out.print((char) c);
//            }
//        }
    }

    private void illegalStateFailure(String message) throws IllegalStateException, FileNotFoundException, UnsupportedEncodingException {
        error = 127;
        logMessage(message);
        throw new IllegalStateException(message);
    }

    private class InterruptTrap extends Thread {
        @Override
        public void run() {
            if (running.get()) {
                cancel.set(true);
                try {
                    reportStatus("aborting");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    System.setOut(AutomatedViewGeneration.stdout);
                    String msg = "Cancel received. Will complete current operation, logout, and terminate (max delay: " + cancelDelay + " min).";
                    try {
                        logMessage(msg);
                    } catch (FileNotFoundException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        for (int i = 0; i < cancelDelay; i++) {
                            if (!running.get()) {
                                break;
                            }
                            lock.wait(60 * 1000);
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                try {
                    reportStatus("aborted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runtime.getRuntime().removeShutdownHook(cancelHandler);
//                Runtime.getRuntime().halt(error);
            }
        }
    }
}
