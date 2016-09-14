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

@Deprecated
public class SyncedComment {

    public enum Action {
        ADD, MODIFY, DELETE
    }

    public static final boolean DELETED = true;

    private String id;
    private String author;
    private String timestamp;
    private String body;
    private Action action;

    public static SyncedComment added(String id, String author, String timestamp, String body) {
        return new SyncedComment(id, author, timestamp, body, Action.ADD);
    }

    public static SyncedComment modified(String id, String author, String timestamp, String body) {
        return new SyncedComment(id, author, timestamp, body, Action.MODIFY);
    }

    public static SyncedComment deleted(String id, String author, String timestamp, String body) {
        return new SyncedComment(id, author, timestamp, body, Action.DELETE);
    }

    public static SyncedComment found(String id, String author, String timestamp, String body) {
        return new SyncedComment(id, author, timestamp, body, null);
    }

    /**
     * Creates a non-deleted comment.
     *
     * @param id
     * @param author
     * @param timestamp
     * @param body
     */
    public SyncedComment(String id, String author, String timestamp, String body, Action action) {
        this.id = id;
        this.author = author;
        this.timestamp = timestamp;
        this.body = body;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getBody() {
        return body;
    }

    public Action getAction() {
        return action;
    }

    public boolean isAdded() {
        return action == Action.ADD;
    }

    public boolean isModified() {
        return action == Action.MODIFY;
    }

    public boolean isDeleted() {
        return action == Action.DELETE;
    }

    public SyncedComment deletedClone() {
        return new SyncedComment(id, author, timestamp, body, Action.DELETE);
    }

    @Override
    public String toString() {
        return String.format("%s: [%s %s] %s", id, author, timestamp, body);
    }
}
