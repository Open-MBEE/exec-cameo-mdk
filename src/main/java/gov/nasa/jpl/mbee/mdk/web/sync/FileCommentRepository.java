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
package gov.nasa.jpl.mbee.mdk.web.sync;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import java.io.*;
import java.util.*;

@Deprecated
public class FileCommentRepository implements CommentRepository {
    private final String filename;
    private File file;

    public FileCommentRepository(String filename) {
        this.filename = filename;
    }

    @Override
    public void connect() throws CommentSyncFailure {
        file = new File(filename);
        if (!file.exists()) {
            throw new CommentSyncFailure("Comment file not found: " + filename);
        }
        if (!file.canRead()) {
            throw new CommentSyncFailure("No permission to read comment file: " + filename);
        }
    }

    @Override
    public List<SyncedComment> getComments(NamedElement document) throws CommentSyncFailure {
        if (file == null) {
            throw new CommentSyncFailure("Called before successful connect()");
        }
        List<SyncedComment> comments = new ArrayList<SyncedComment>();
        FileReader fileReader = null;
        BufferedReader reader = null;
        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                SyncedComment comment = read(line, document.getID());
                if (comment != null) { // check for blank lines and #...
                    comments.add(comment);
                }
            }
        } catch (IOException e) {
            throw new CommentSyncFailure("Error reading file " + filename + ": " + e.getMessage(), e);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) { /* IGNORE */
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) { /* IGNORE */
                }
            }
        }
        return comments;
    }

    @Override
    public void sendComments(NamedElement document, List<SyncedComment> newComments,
                             List<SyncedComment> modifiedComments, List<SyncedComment> deletedComments)
            throws CommentSyncFailure {
        if (file == null) {
            throw new CommentSyncFailure("Called before successful connect()");
        }
        Map<String, SyncedComment> incoming = new HashMap<String, SyncedComment>();
        Set<Integer> seen = new HashSet<Integer>();
        if (newComments != null) {
            for (SyncedComment c : newComments) {
                incoming.put(c.getId(), c);
            }
        }
        if (modifiedComments != null) {
            for (SyncedComment c : modifiedComments) {
                incoming.put(c.getId(), c);
                seen.add(CommentUtil.checksum(c));
            }
        }
        if (deletedComments != null) {
            for (SyncedComment c : deletedComments) {
                incoming.put(c.getId(), c);
                seen.add(CommentUtil.checksum(c));
            }
        }
        List<SyncedComment> untouched = new ArrayList<SyncedComment>();
        for (SyncedComment c : getComments(document)) {
            if (!incoming.containsKey(c.getId()) && !seen.contains(CommentUtil.checksum(c))) {
                untouched.add(c);
            }
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            write(writer, document.getID(), untouched);
            write(writer, document.getID(), incoming.values());
        } catch (IOException e) {
            throw new CommentSyncFailure("Error writing file " + filename + ": " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) { /* IGNORE */
                }
            }
        }
    }

    private void write(FileWriter writer, String documentId, Collection<SyncedComment> comments)
            throws IOException {
        if (comments == null) {
            return;
        }
        for (SyncedComment c : comments) {
            StringBuilder sb = new StringBuilder();
            sb.append(documentId).append('|');
            sb.append(c.getId()).append('|');
            sb.append(c.getAuthor()).append('|');
            sb.append(c.getTimestamp()).append('|');
            sb.append(c.getBody()).append('|');
            sb.append(c.isDeleted() ? "DELETED" : "").append('\n');
            writer.write(sb.toString());
        }
    }

    private SyncedComment read(String line, String documentId) throws CommentSyncFailure {
        if (line.startsWith("#") || line.isEmpty()) {
            return null;
        }
        String[] fields = line.split("\\|");
        if (fields.length != 6 && fields.length != 5) {
            throw new CommentSyncFailure("Row has wrong number of columns: " + line);
        }
        if (!documentId.equals(fields[0])) {
            return null;
        }
        if (fields.length == 6 && "DELETED".equals(fields[5])) {
            return SyncedComment.deleted(fields[1], fields[2], fields[3], fields[4]);
        }
        else {
            return SyncedComment.found(fields[1], fields[2], fields[3], fields[4]);
        }
    }

    @Override
    public void close() {
    }

}
