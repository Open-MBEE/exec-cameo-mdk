package gov.nasa.jpl.mgss.mbee.docgen.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class FileCommentRepository implements CommentRepository {
    private final String filename;
    private File         file;

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
            if (fileReader != null)
                try {
                    fileReader.close();
                } catch (IOException e) { /* IGNORE */
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) { /* IGNORE */
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
            for (SyncedComment c: newComments) {
                incoming.put(c.getId(), c);
            }
        }
        if (modifiedComments != null) {
            for (SyncedComment c: modifiedComments) {
                incoming.put(c.getId(), c);
                seen.add(CommentUtil.checksum(c));
            }
        }
        if (deletedComments != null) {
            for (SyncedComment c: deletedComments) {
                incoming.put(c.getId(), c);
                seen.add(CommentUtil.checksum(c));
            }
        }
        List<SyncedComment> untouched = new ArrayList<SyncedComment>();
        for (SyncedComment c: getComments(document)) {
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
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) { /* IGNORE */
                }
        }
    }

    private void write(FileWriter writer, String documentId, Collection<SyncedComment> comments)
            throws IOException {
        if (comments == null) {
            return;
        }
        for (SyncedComment c: comments) {
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
        } else {
            return SyncedComment.found(fields[1], fields[2], fields[3], fields[4]);
        }
    }

    @Override
    public void close() {
    }

}
