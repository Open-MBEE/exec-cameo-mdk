/*******************************************************************************
 * Copyright (c) <2016>, California Institute of Technology ("Caltech").
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

package gov.nasa.jpl.mbee.mdk.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.GenerateViewPresentationAction;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * This class exposes MDK operations for use in external programs.
 */
@Deprecated
public class MDKHelper {

    /**
     * Sets boolean that can disabled popups and redirect their messages to the GUI log.
     *
     * @param disabled true to redirect popups to gui log, false to renable normal popup behavior
     */
    public static void setPopupsDisabled(boolean disabled) {
        Utils.setPopupsDisabled(disabled);
    }

    /************************************************************
     *
     * MMS Login Methods
     *
     ************************************************************/

    /**
     * Sets the supplied username and password in memory. Does not validate its accuracy.
     *
     * @param username Username for MMS login
     * @param password Password for MMS login
     */
    public static void setMMSLoginCredentials(String username, String password) {
        TicketUtils.setUsernameAndPassword(username, password);
    }

    /**********************************************************************************
     *
     * MDK User Actions
     *
     **********************************************************************************/

    /**
     * Executes "Generate View(s)" MDK element action on the selected element
     *
     * @param doc     Document element
     * @param recurse Select true to generate all views under this element, false to
     *                generate only the view for the selected element
     */
    public static void generateViews(Element doc, Boolean recurse) {
        new GenerateViewPresentationAction(Collections.singleton(doc), recurse).updateAction();

    }

    /**********************************************************************************
     *
     * MMS REST Interactions
     *
     **********************************************************************************/

    public static Collection<Element> getElement(Element element, Project project)
            throws IOException, ServerException, URISyntaxException {
        if (element == null) {
            return null;
        }
        Collection<String> collection = new ArrayList<>(1);
        collection.add(Converters.getElementToIdConverter().apply(element));
        return getElementsById(collection, project, null);
    }

    public static Collection<Element> getElementsById(Collection<String> elementIds, Project project, ProgressStatus progressStatus)
            throws IOException, ServerException, URISyntaxException {
        if (elementIds == null || elementIds.isEmpty()) {
            return null;
        }

        // create requests json
        final ObjectNode requests = JacksonUtils.getObjectMapper().createObjectNode();
        // put elements array inside request json, keep reference
        File sendData = MMSUtils.createEntityFile(MDKHelper.class, ContentType.APPLICATION_JSON, elementIds, MMSUtils.JsonBlobType.ELEMENT_ID);

        URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
        if (requestUri == null) {
            return null;
        }

        //do cancellable request if progressStatus exists
        Utils.guilog("[INFO] Searching for " + elementIds.size() + " elements from server.");
        File responseFile;
        if (progressStatus != null) {
            responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri, sendData, ContentType.APPLICATION_JSON), progressStatus);
        }
        else {
            responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri, sendData, ContentType.APPLICATION_JSON));
        }
        LinkedList<Element> elementsList = new LinkedList<>();
        try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
            for (ObjectNode elementJson : JacksonUtils.parseJsonResponseToObjectList(jsonParser, null)) {
                JsonNode value;
                if ((value = elementJson.get(MDKConstants.ID_KEY)) != null && value.isTextual()) {
                    elementsList.add(Converters.getIdToElementConverter().apply(value.asText(), project));
                }
            }
        }
        return elementsList;
    }
}