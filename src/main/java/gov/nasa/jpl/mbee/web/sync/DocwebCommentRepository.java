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
package gov.nasa.jpl.mbee.web.sync;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.web.HttpsUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class DocwebCommentRepository implements CommentRepository {
    private final String baseUrlString;
    private final URL baseUrl;

    public DocwebCommentRepository(String baseUrl) {
        this.baseUrlString = baseUrl;
        URL tmpUrl = null;
        try {
            tmpUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            // leave baseUrl as null
        }
        this.baseUrl = tmpUrl;
    }

    @Override
    public void connect() throws CommentSyncFailure {
        if (baseUrl == null) {
            throw new CommentSyncFailure("Invalid base URL: " + baseUrlString);
        }
        // TODO: create a session to log in
    }

    @Override
    public List<SyncedComment> getComments(NamedElement document) throws CommentSyncFailure {
        HttpsUtils.allowSelfSignedCertificates();
        List<SyncedComment> comments = new ArrayList<SyncedComment>();
        JSONArray jsonList = null;
        GetMethod m = new GetMethod(baseUrlString + "/rest/documents/" + urlEncode(document.getID())
                + "/comments/" + "?package=" + urlEncode(document.getName()));
        try {
            HttpClient client = new HttpClient();
            int code = client.executeMethod(m);
            if (code != 200) {
                throw new CommentSyncFailure("Request failed: " + m.getStatusLine());
            }
            String response = m.getResponseBodyAsString();
            jsonList = (JSONArray) JSONValue.parse(response);
            for (Object obj : jsonList) {
                if (obj instanceof JSONObject) {
                    SyncedComment c = toComment((JSONObject) obj);
                    if (c != null) {
                        comments.add(c);
                    }
                }
                else {
                    Application.getInstance().getGUILog().showError("obj not JSONObject"); // DEBUG
                    continue;
                }
            }
        } catch (MalformedURLException e) {
            throw new CommentSyncFailure("url doesn't work");
        } catch (IOException e) {
            throw new CommentSyncFailure("cannot connect");
        } finally {
            m.releaseConnection();
        }
        return comments;
    }

    @Override
    public void sendComments(NamedElement document, List<SyncedComment> newComments,
                             List<SyncedComment> modifiedComments, List<SyncedComment> deletedComments)
            throws CommentSyncFailure {
        HttpsUtils.allowSelfSignedCertificates();

        JSONObject json = makeJsonForExport(newComments, modifiedComments, deletedComments);
        // Application.getInstance().getGUILog().log("Sending changes:\n" +
        // json); // DEBUG

        PostMethod m = new PostMethod(baseUrlString + "/rest/documents/" + urlEncode(document.getID())
                + "/comments/changes/" + "?package=" + urlEncode(document.getName()));
        try {
            m.setRequestEntity(JsonRequestEntity.create(json));
            m.setRequestHeader("Content-type", "text/json");
            HttpClient client = new HttpClient();
            int code = client.executeMethod(m);
            if (code != 200) {
                throw new CommentSyncFailure("Request failed: " + m.getStatusLine());
            }
        } catch (MalformedURLException e) {
            throw new CommentSyncFailure("url doesn't work");
        } catch (IOException e) {
            throw new CommentSyncFailure("cannot connect");
        } finally {
            m.releaseConnection();
        }
    }

    @Override
    public void close() {
        // TODO: delete session to log out
    }

    private String urlEncode(String documentId) {
        try {
            return URLEncoder.encode(documentId, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return documentId.replaceAll(" ", "%20");
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject makeJsonForExport(List<SyncedComment> newComments,
                                         List<SyncedComment> modifiedComments, List<SyncedComment> deletedComments) {
        JSONObject json = new JSONObject();
        json.put("new", makeJsonArray(newComments));
        json.put("modified", makeJsonArray(modifiedComments));
        json.put("deleted", makeJsonArray(deletedComments));
        return json;
    }

    @SuppressWarnings("unchecked")
    private JSONArray makeJsonArray(List<SyncedComment> comments) {
        JSONArray json = new JSONArray();
        for (SyncedComment c : comments) {
            json.add(makeJson(c));
        }
        return json;
    }

    /**
     * Convert from a SyncedComment object to JSON.
     *
     * @param comment
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject makeJson(SyncedComment comment) {
        JSONObject json = new JSONObject();
        json.put("id", comment.getId());
        json.put("body", Utils.stripHtmlWrapper(comment.getBody()));
        json.put("time", comment.getTimestamp());
        json.put("author", comment.getAuthor());
        json.put("deleted", comment.isDeleted());
        return json;
    }

    /**
     * Convert from JSON to a SyncedComment object.
     *
     * @param json
     * @return
     */
    private SyncedComment toComment(JSONObject json) {
        // Application.getInstance().getGUILog().log("incoming: " + json); //
        // DEBUG
        try {
            String id = (String) json.get("id");
            String author = (String) json.get("author");
            String timestamp = (String) json.get("time");
            String body = Utils.addHtmlWrapper((String) json.get("body"));
            Boolean deleted = (Boolean) json.get("deleted");

            if (author == null || timestamp == null) {
                Application.getInstance().getGUILog()
                        .showError("author=" + author + " timestamp=" + timestamp); // DEBUG
                return null;
            }
            if (deleted != null && deleted.booleanValue()) {
                return SyncedComment.deleted(id, author, timestamp, body);
            }
            else {
                return SyncedComment.found(id, author, timestamp, body);
            }
        } catch (ClassCastException e) {
            Application.getInstance().getGUILog().showError("ClassCaseException " + e.getMessage()); // DEBUG
            return null;
        }
    }
}
