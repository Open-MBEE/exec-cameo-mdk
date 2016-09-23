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
package gov.nasa.jpl.mbee.mdk.viewedit;

import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.model.AbstractModelVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.Section;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static gov.nasa.jpl.mbee.mdk.web.sync.CommentUtil.TIME_FORMAT;

/**
 * gets view comments and exports to view editor
 *
 * @author dlam
 */
@Deprecated
public class ViewCommentVisitor extends AbstractModelVisitor {
    /*
     * { view2comment: {viewid:[mdid], ...} comments: [{id: mdid, body: body,
     * author: user, modified: datetime}], comment2comment: {commentid:
     * [childids], ...} //not supported right now (for comment replies) }
     */
    private Map<String, JSONArray> view2comment;
    private JSONArray comments;
    private String user;

    public ViewCommentVisitor() {
        view2comment = new HashMap<String, JSONArray>();
        comments = new JSONArray();
        user = getCurrentUser();
    }

    @SuppressWarnings("unchecked")
    public String getJSON() {
        JSONObject out = new JSONObject();
        out.put("view2comment", view2comment);
        out.put("comments", comments);
        return out.toJSONString();
    }

    @Override
    public void visit(Document doc) {
        if (doc.getDgElement() != null) {
            handleView(doc.getDgElement());
        }
        visitChildren(doc);
    }

    @Override
    public void visit(Section sec) {
        if (sec.isView()) {
            handleView(sec.getDgElement());
        }
        visitChildren(sec);
    }

    @SuppressWarnings("unchecked")
    private void handleView(Element view) {
        JSONArray commentIds = new JSONArray();
        for (Comment c : view.get_commentOfAnnotatedElement()) {
            if (StereotypesHelper.hasStereotypeOrDerived(c, "DocumentComment")) {
                addComment(c);
                commentIds.add(c.getID());
            }
        }
        view2comment.put(view.getID(), commentIds);
    }

    @SuppressWarnings("unchecked")
    private void addComment(Comment comment) {
        JSONObject c = new JSONObject();
        c.put("body", Utils.stripHtmlWrapper(comment.getBody()));
        c.put("id", comment.getID());
        String user = (String) StereotypesHelper.getStereotypePropertyFirst(comment, "DocumentComment",
                "author");
        String time = (String) StereotypesHelper.getStereotypePropertyFirst(comment, "DocumentComment",
                "timestamp");
        if (user == null || user.equals("")) {
            user = this.user;
        }
        if (time == null || time.equals("")) {
            time = getCurrentTime();
        }
        c.put("modified", time);
        c.put("author", user);
        comments.add(c);
    }

    private String getCurrentTime() {
        return TIME_FORMAT.format(new Date());
    }

    private String getCurrentUser() {
        String username;
        String teamworkUsername = TeamworkUtils.getLoggedUserName();
        if (teamworkUsername != null) {
            username = teamworkUsername;
        }
        else {
            username = System.getProperty("user.name", "");
        }
        return username;
    }

}
