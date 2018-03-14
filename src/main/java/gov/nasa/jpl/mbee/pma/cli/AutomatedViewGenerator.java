package gov.nasa.jpl.mbee.pma.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.commandline.CommandLineAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.ITeamworkService;
import com.nomagic.magicdraw.teamwork2.ServerLoginInfo;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.MDKHelper;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.TaskRunner;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomatedViewGenerator implements CommandLineAction {

    private org.apache.commons.cli.CommandLine parser;
    private org.apache.commons.cli.Options parserOptions;

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
    private String ticketStore;

    private InterruptTrap cancelHandler;

    private final Object lock = new Object();

    private final String MMS_HOST = "mmsHost",
            MMS_PORT = "mmsPort",
            MMS_USERNAME = "mmsUsername",
            MMS_PASSWORD = "mmsPassword",
            TWC_HOST = "twcHost",
            TWC_PORT = "twcPort",
            TWC_USERNAME = "twcUsername",
            TWC_PASSWORD = "twcPassword",
            PROJECT_ID = "projectId",
            REF_ID = "refId",
            TARGET_VIEW_ID = "targetViewId",
            GENERATE_RECURSIVELY = "generateRecursively",
            PMA_HOST = "pmaHost",
            PMA_PORT = "pmaPort",
            PMA_JOB_ID = "pmaJobId",
            HELP = "help",
            DEBUG = "debug",
            VERBOSE = "verbose";

    private String separator = "\n***************\n";

    private final int CANCEL_DELAY = 15;

    /*//////////////////////////////////////////////////////////////
     *
     * Execution methods
     *
    /*//////////////////////////////////////////////////////////////

    @Override
    public byte execute(String[] args) {
        // start the cancel handler so we don't terminate in the middle of a view sync operation and so we can force logout if logged in to teamwork
        cancelHandler = new InterruptTrap();
        Runtime.getRuntime().addShutdownHook(cancelHandler);

        try {
            if (!parseArgs(args) || parser.hasOption(HELP) || parser.hasOption('h') || !validateParser()) {
                displayHelp();
                return 1;
            }

            System.out.println(separator);
            System.out.println("[INFO] Performing automated view generation.");

            String mmsUrl = "https://" + parser.getOptionValue(MMS_HOST);
            if (parser.hasOption(MMS_PORT)) {
                try {
                    mmsUrl = mmsUrl + ":" + parser.getOptionValue(MMS_PORT);
                } catch (NumberFormatException nfe) {
                    String message = "[WARNING] Invalid mmsPort specified in options. Will attempt to access MMS without a port.";
                    logMessage(message);
                }
            }
            ticketStore = MMSUtils.getCredentialsTicket(mmsUrl, parser.getOptionValue(MMS_USERNAME), parser.getOptionValue(MMS_PASSWORD), null);
            reportStatus("Started");

            MDKOptionsGroup.getMDKOptions().setLogJson(parser.hasOption(DEBUG));
            if (parser.hasOption(DEBUG)) {
                System.out.println("[DEBUG] JSON will be saved. " + MDKOptionsGroup.getMDKOptions().isLogJson());
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
            if (error == 0) {
                error = 100;
            }
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
                            reportStatus("Completed");
                            System.out.println("[INFO] Automated View Generation completed without errors.");
                        }
                        else {
                            reportStatus("Failed");
                            System.out.println("[FAILURE] Automated View Generation did not finish successfully. Operations were logged in MDNotificationWindowText.html.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(separator);
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
            throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, IllegalAccessException, IllegalStateException {
        // disable all mdk popup warnings
        MDKHelper.setPopupsDisabled(true);

        String message = "[OPERATION] Logging in to Teamwork.";
        logMessage(message);

        ITeamworkService twcService = EsiUtils.getTeamworkService();
        twcService.login(new ServerLoginInfo(parser.getOptionValue(TWC_HOST) + ":" + parser.getOptionValue(TWC_PORT),
                parser.getOptionValue(TWC_USERNAME), parser.getOptionValue(TWC_PASSWORD), true), true);
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
            throws FileNotFoundException, UnsupportedEncodingException, RemoteException, IllegalAccessException, IllegalStateException, InterruptedException, URISyntaxException {
        String message;

        message = "[OPERATION] Specifying MMS credentials.";
        logMessage(message);
        MDKHelper.setMMSLoginCredentials(parser.getOptionValue(MMS_USERNAME), parser.getOptionValue(MMS_PASSWORD));

        message = "[OPERATION] Resolving Teamwork Cloud project URI parameters.";
        logMessage(message);

        String projectTwcId = "",
                branchTwcId = "";
        ObjectNode projectsNode = JacksonUtils.getObjectMapper().createObjectNode(),
                refsNode = JacksonUtils.getObjectMapper().createObjectNode();

        try {
            URIBuilder mmsProjectUri = MMSUtils.getServiceProjectsUri("https://" + parser.getOptionValue(MMS_HOST));
            mmsProjectUri.setParameter("alf_ticket", ticketStore).setPath(mmsProjectUri.getPath() + "/" + parser.getOptionValue(PROJECT_ID));
            MMSUtils.sendMMSRequest(null, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, mmsProjectUri), null, projectsNode);
            if (!parser.getOptionValue(REF_ID).equals("master")) {
                URIBuilder mmsRefUri = MMSUtils.getServiceProjectsRefsUri("https://" + parser.getOptionValue(MMS_HOST), parser.getOptionValue(PROJECT_ID));
                mmsRefUri.setParameter("alf_ticket", ticketStore).setPath(mmsRefUri.getPath() + "/" + parser.getOptionValue(REF_ID));
                MMSUtils.sendMMSRequest(null, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, mmsRefUri), null, refsNode);
            }
        } catch (IOException | ServerException e) {
            illegalStateFailure("[FAILURE] Unable to load project, exception occurred while resolving one of the required project URI parameters.");
            e.printStackTrace();
        }

        JsonNode arrayNode, idNode;
        if ((arrayNode = projectsNode.get("projects")) != null && arrayNode.isArray()) {
            for (JsonNode projectNode : arrayNode) {
                if (projectNode.isObject()
                        && (idNode = projectNode.get(MDKConstants.ID_KEY)) != null && idNode.isTextual() && idNode.asText().equals((parser.getOptionValue((PROJECT_ID))))
                        && (idNode = projectNode.get(MDKConstants.TWC_ID_KEY)) != null && idNode.isTextual()) {
                    projectTwcId = idNode.asText();
                }
            }
        }
        if ((arrayNode = refsNode.get("refs")) != null && arrayNode.isArray()) {
            for (JsonNode refNode : arrayNode) {
                if (refNode.isObject()
                        && (idNode = refNode.get(MDKConstants.ID_KEY)) != null && idNode.isTextual() && idNode.asText().equals((parser.getOptionValue((REF_ID))))
                        && (idNode = refNode.get(MDKConstants.TWC_ID_KEY)) != null && idNode.isTextual()) {
                    branchTwcId = idNode.asText();
                }
            }
        }

        if (projectTwcId.isEmpty()) {
            illegalStateFailure("[FAILURE] Unable to load project, failed to resolve project ID.");
        }

        ProjectDescriptor projectDescriptor = null;
        try {
            Collection<ProjectDescriptor> descriptors = EsiUtils.getRemoteProjectDescriptors();
            for (ProjectDescriptor descriptor : descriptors) {
                if (descriptor.getURI().getPath().startsWith("/" + projectTwcId)) {
                    projectDescriptor = descriptor;
                    break;
                }
            }
        } catch (Exception e1) {
            illegalStateFailure("[FAILURE] Unable to find project descriptor on Teamwork Cloud.");
        }
        if (projectDescriptor == null) {
            illegalStateFailure("[FAILURE] Unable to find project descriptor on Teamwork Cloud.");
        }
        assert projectDescriptor != null;

        // modify project descriptor with branch information
        if (!branchTwcId.isEmpty()) {

            projectDescriptor = EsiUtils.getDescriptorByBranchID(projectDescriptor, java.util.UUID.fromString(branchTwcId));
        }
        else if (!parser.getOptionValue(REF_ID).equals("master")) {
            projectDescriptor = EsiUtils.getDescriptorByBranchID(projectDescriptor, java.util.UUID.fromString(parser.getOptionValue(REF_ID)));
        }

        message = "[OPERATION] Loading Teamwork Cloud project.";
        logMessage(message);

        Application.getInstance().getProjectsManager().loadProject(projectDescriptor, true);

        // if not access to project, loaded project will be null, so error out
        if (Application.getInstance().getProject() == null) {
            illegalStateFailure("[FAILURE] User " + parser.getOptionValue(MMS_USERNAME) + " does not have permission to load " + projectDescriptor.getRepresentationString() + ".");
        }
        twLoaded = true;
        project = Application.getInstance().getProject();

        // move the stored message log into the MD notification window. This will mess up the time stamps, but will keep all of the messages in the same place
        while (!messageLog.isEmpty()) {
            Application.getInstance().getGUILog().log(messageLog.remove(0));
        }
        message = "[INFO] Opened Teamwork Cloud project. Note: preceding timestamps may be invalid.";
        logMessage(message);
        checkCancel();
    }

    /**
     * Generates views and commits images for target view, recursively if the option was specified.
     *
     * @throws FileNotFoundException        one or more documents not found in project, or logMessage failure
     * @throws InterruptedException         cancel triggered and caught by cancel handler
     * @throws UnsupportedEncodingException logMessage failure
     */
    private void generateViews() throws Exception {
        String message;
        if (!TicketUtils.isTicketSet(project)) {
            TicketUtils.setUsernameAndPassword(parser.getOptionValue(MMS_USERNAME), parser.getOptionValue(MMS_PASSWORD));
            if (!MMSLoginAction.loginAction(project)) {
                illegalStateFailure("[FAILURE] User " + parser.getOptionValue(MMS_USERNAME) + " was unable to login to MMS.");
            }
        }

        Element targetView = Converters.getIdToElementConverter().apply(parser.getOptionValue(TARGET_VIEW_ID), Application.getInstance().getProject());
        if (targetView == null) {
            illegalStateFailure("[ERROR] Unable to find element \"" + parser.getOptionValue(TARGET_VIEW_ID) + "\"");
        }
        assert targetView != null;
        MMSUtils.getLastException().set(null);
        message = "[OPERATION] Generating " + targetView.getHumanName() + (parser.hasOption(GENERATE_RECURSIVELY) ? " views recursively." : ".");
        logMessage(message);
        // LOG: the element which is being generated currently

        MDKHelper.generateViews(targetView, parser.hasOption(GENERATE_RECURSIVELY));
        while (true) {
            if (Arrays.stream(TaskRunner.ThreadExecutionStrategy.values()).allMatch(strategy -> strategy.getExecutor().getQueue().isEmpty() && strategy.getExecutor().getActiveCount() == 0)) {
                break;
            }
            Thread.sleep(1000);
        }
        Exception lastException = MMSUtils.getLastException().get();
        if (lastException != null) {
            if (lastException instanceof ServerException && ((ServerException) lastException).getCode() == 403) {
                message = "[ERROR] Unable to generate " + targetView.getHumanName() + ". User " + parser.getOptionValue(MMS_USERNAME) + " does not have permission to write to the MMS in this branch.";
                logMessage(message);
            }
            else {
                message = "[ERROR] Unexpected error while generating " + targetView.getHumanName() + ". Reason: " + lastException.getMessage();
                logMessage(message);
                throw lastException;
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
    private boolean parseArgs(String[] args) throws ParseException {
        Option helpOption = new Option("h", HELP, false, "print this message");

        Option mmsHostOption = new Option(MMS_HOST, true, "use value for the MMS host name");
        Option mmsPortOption = new Option(MMS_PORT, true, "use value for the MMS port number");
        Option mmsUsernameOption = new Option(MMS_USERNAME, true, "use value for the MMS username");
        Option mmsPasswordOption = new Option(MMS_PASSWORD, true, "use value for the MMS password");

        Option twcHostOption = new Option(TWC_HOST, true, "use value for the Teamwork Cloud host name");
        Option twcPortOption = new Option(TWC_PORT, true, "use value for the Teamwork Cloud port number");
        Option twcUsernameOption = new Option(TWC_USERNAME, true, "use value for the Teamwork Cloud username");
        Option twcPasswordOption = new Option(TWC_PASSWORD, true, "use value for the Teamwork Cloud password");

        Option projectIdOption = new Option(PROJECT_ID, true, "use value for the target project ID");
        Option refIdOption = new Option(REF_ID, true, "use value for  the target ref (branch) ID");
        Option targetViewIdOption = new Option(TARGET_VIEW_ID, true, "use value for the target view element ID");
        Option generateRecursivelyOption = new Option(GENERATE_RECURSIVELY, "generate child views of target recursively");

        Option pmaHostOption = new Option(PMA_HOST, true, "use value for the PMA server host name; a missing value will disable status reporting");
        Option pmaPortOption = new Option(PMA_PORT, true, "use value for the PMA server port number");
        Option pmaInstanceIdOption = new Option(PMA_JOB_ID, true, "use value for the PMA job instance element ID; a missing value will disable status reporting");

        Option debugOption = new Option(DEBUG, "print debug messages from cli program to console");

        // verbose option is understood by magicdraw to output information to command line. included here since the arg is also passed in here, and it causes errors if not expected
        Option verboseOption = new Option(VERBOSE, "\"-verbose\" - print all MagicDraw log messages to console");

        parserOptions = new Options();

        parserOptions.addOption(helpOption);

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

        parserOptions.addOption(pmaHostOption);
        parserOptions.addOption(pmaPortOption);
        parserOptions.addOption(pmaInstanceIdOption);

        parserOptions.addOption(debugOption);
        parserOptions.addOption(verboseOption);

        CommandLineParser commandLineParser = new BasicParser();
        try {
            parser = commandLineParser.parse(parserOptions, args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private void displayHelp() {
        HelpFormatter formatter = new HelpFormatter();
        String usage = separator + "$MAGICDRAW_HOME/bin/cli/automatedviewgenerator.sh";
        formatter.printHelp(usage, separator, parserOptions, separator, true);
    }

    private boolean validateParser() throws FileNotFoundException, UnsupportedEncodingException {
        List<String> requiredOptions = new LinkedList<>();
        requiredOptions.add(MMS_HOST);
        requiredOptions.add(MMS_USERNAME);
        requiredOptions.add(MMS_PASSWORD);
        requiredOptions.add(TWC_HOST);
        requiredOptions.add(TWC_PORT);
        requiredOptions.add(TWC_USERNAME);
        requiredOptions.add(TWC_PASSWORD);
        requiredOptions.add(PROJECT_ID);
        requiredOptions.add(REF_ID);
        requiredOptions.add(TARGET_VIEW_ID);

        List<String> missingOptions = new LinkedList<>();
        for (String option : requiredOptions) {
            if (!parser.hasOption(option)) {
                missingOptions.add(option);
            }
        }

        if (!missingOptions.isEmpty()) {
            String message = "[ERROR] The following options were missing: ";
            for (String missingOption : missingOptions) {
                message += missingOption + ", ";
            }
            message = message.substring(0, message.length() - 2) + ".";
            logMessage(message);
            return false;
        }
        return true;
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
        if (msg.isEmpty()) {
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
            if (parser.hasOption(DEBUG)) {
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

    private void reportStatus(String status) throws FileNotFoundException, UnsupportedEncodingException {
        String message;
        if (!parser.hasOption(PMA_HOST)) {
            message = "[WARNING] Unable to report status to PMA server, pmaHost is not defined.";
            logMessage(message);
            return;
        }
        String buildNumber = System.getenv("BUILD_NUMBER");
        if (buildNumber == null || buildNumber.isEmpty()) {
            message = "[WARNING] Unable to report status to PMA server, BUILD_NUMBER environment variable not available.";
            logMessage(message);
            return;
        }
        ObjectNode statusNode = JacksonUtils.getObjectMapper().createObjectNode();
        statusNode.put("ticket", ticketStore);
        statusNode.put("property", "jobStatus");
        statusNode.put("value", status);

        // http://{pmaHost}:{pmaPort}/projects/{projectId}/refs/{refId}/jobs/{jobElementId}/instances/{jenkinsBuildNumber}/{jobPropertyName}?mmsServer={mmsServer}
        URIBuilder pmaUri = new URIBuilder();
        pmaUri.setScheme("https");
        pmaUri.setHost(parser.getOptionValue(PMA_HOST));
        if (parser.hasOption(PMA_PORT)) {
            try {
                pmaUri.setPort(Integer.parseInt(parser.getOptionValue(PMA_PORT)));
            } catch (NumberFormatException nfe) {
                message = "[WARNING] Invalid pmaPort specified in options. Will attempt to report status without a port.";
                logMessage(message);
            }
        }
        String path = "/projects/" + parser.getOptionValue(PROJECT_ID) + "/refs/" + parser.getOptionValue(REF_ID)
                + "/jobs/" + parser.getOptionValue(PMA_JOB_ID) + "/instances/" + buildNumber + "/jobStatus";
        pmaUri.setPath(path);
        pmaUri.setParameter("mmsServer", parser.getOptionValue(MMS_HOST));
        StringEntity jsonBody;
        try {
            jsonBody = new StringEntity(statusNode.toString());
        } catch (UnsupportedEncodingException e) {
            message = "[WARNING] Unable to update PMA status, exception occurred while creating status update json.";
            logMessage(message);
            e.printStackTrace();
            return;
        }

        HttpPost request;
        try {
            request = new HttpPost(pmaUri.build());
            request.addHeader("Content-Type", "application/json");
            request.setEntity(jsonBody);
        } catch (URISyntaxException e) {
            message = "[WARNING] Unable to update PMA status, exception occurred while creating HTTP request.";
            logMessage(message);
            e.printStackTrace();
            return;
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(request);
             InputStream inputStream = response.getEntity().getContent()) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                message = "[WARNING] PMA status update failed, execution returned error status. Code: " + responseCode;
                logMessage(message);
                if (inputStream != null) {
                    message = "  Message: " + IOUtils.toString(inputStream);
                    logMessage(message);
                }
            }
        } catch (IOException e) {
            message = "[WARNING] PMA status update failed, exception occurred during HTTP connection.";
            logMessage(message);
            e.printStackTrace();
        }
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
                    reportStatus("Aborting...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    String msg = "Cancel received. Will complete current operation, logout, and terminate (max delay: " + CANCEL_DELAY + " min).";
                    try {
                        logMessage(msg);
                    } catch (FileNotFoundException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        for (int i = 0; i < CANCEL_DELAY * 10; i++) {
                            if (!running.get()) {
                                break;
                            }
                            lock.wait(6 * 1000);
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                try {
                    reportStatus("Aborted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Runtime.getRuntime().removeShutdownHook(cancelHandler);
            }
        }
    }
}
