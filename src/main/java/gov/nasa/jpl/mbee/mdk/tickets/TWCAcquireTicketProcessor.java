package gov.nasa.jpl.mbee.mdk.tickets;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.esi.EsiServerActionsExecuter;
import com.nomagic.task.ProgressStatus;
import com.nomagic.ui.ProgressStatusRunner;

import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSEndpointType;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSTWCLoginEndpoint;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;

public class TWCAcquireTicketProcessor extends AbstractAcquireTicketProcessor {
    public TWCAcquireTicketProcessor(AbstractAcquireTicketProcessor processor) {
        super(processor);
    }

    private static final Logger logger = LoggerFactory.getLogger(TWCAcquireTicketProcessor.class);

    public static String getConnectedUser() {
        return (EsiUtils.getTeamworkService() == null || EsiUtils.getTeamworkService().getConnectedUser() == null)
                ? null
                : EsiUtils.getTeamworkService().getConnectedUser();
    }

    public static String getTeamworkCloudServer() {
        String twcServer = null;
        if (EsiUtils.getTeamworkService() != null && EsiUtils.getTeamworkService().getLastUsedLoginInfo() != null
                && EsiUtils.getTeamworkService().getLastUsedLoginInfo().server != null) {
            twcServer = EsiUtils.getTeamworkService().getLastUsedLoginInfo().server;
            if (twcServer.indexOf(':') > -1) {
                twcServer = twcServer.substring(0, twcServer.indexOf(':'));
            }
        }
        return twcServer;
    }

    public static String getSecondaryAuthToken() {
        String secondaryAuthToken = null;
        try {
            secondaryAuthToken = EsiServerActionsExecuter.getSecondaryAuthToken("MAGICDRAW");
        } catch (ConnectException e) {
            logger.error("Error while generating secondary auth token: " + e.getMessage());
        }
        return secondaryAuthToken != null ? "Token :" + secondaryAuthToken : null;
    }

    public static String getTicketUsingTWCToken(Project project, String twcServerUrl, String authToken,
            ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException, GeneralSecurityException {

        HttpRequestBase request = MMSUtils.prepareEndpointBuilderBasicGet(MMSTWCLoginEndpoint.builder(), project).build();
        if(request == null) {
            return null;
        }
        request.addHeader(MDKConstants.TWC_HEADER, twcServerUrl);
        request.addHeader(MDKConstants.AUTHORIZATION, authToken);
        // do request
        ObjectNode responseJson = JacksonUtils.getObjectMapper().createObjectNode();
        MMSUtils.sendMMSRequest(project, request, progressStatus, responseJson);
        if (responseJson.get(MMSEndpointType.AUTHENTICATION_RESPONSE_JSON_KEY) != null
                && responseJson.get(MMSEndpointType.AUTHENTICATION_RESPONSE_JSON_KEY).isTextual()) {
            return responseJson.get(MMSEndpointType.AUTHENTICATION_RESPONSE_JSON_KEY).asText();
        }
        return null;
    }

    @Override
    public boolean acquireMmsTicket(Project project) {
        String username = getConnectedUser();
        String twcServerUrl = getTeamworkCloudServer();
        String authToken = getSecondaryAuthToken();
        if (username == null || twcServerUrl == null || authToken == null) {
            return super.acquireMmsTicket(project);
        }

        // do request
        ProgressStatusRunner.runWithProgressStatus(progressStatus -> {
            String ticket;
            try {
                ticket = getTicketUsingTWCToken(project, twcServerUrl, authToken, progressStatus);
            } catch (IOException | URISyntaxException | ServerException | GeneralSecurityException e) {
                Application.getInstance().getGUILog()
                        .log("[ERROR] An error occurred while acquiring credentials. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            if (ticket != null) {
                TicketUtils.putTicketMapping(project, username, ticket);
            }
        }, "Logging in to MMS", true, 0);
        if (TicketUtils.isTicketSet(project)) {
            return true;
        }
        Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS with TWC.");
        return super.acquireMmsTicket(project);
    }

    @Override
    public void reset() {
        super.reset();
    }
}