package gov.nasa.jpl.mbee.web.sync;

import gov.nasa.jpl.mbee.DocGen3Profile;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class CommentUtil {
    /** stereotype name */
    public static final String        DOCUMENT_VIEW      = DocGen3Profile.documentViewStereotype;

    /** stereotype name */
    public static final String        DOCUMENT_COMMENT   = DocGen3Profile.documentCommentStereotype;

    /** tag for DocumentComment stereotype */
    public static final String        AUTHOR             = "author";

    /** tag for DocumentComment stereotype */
    public static final String        MODIFIED_TIMESTAMP = "timestamp";

    /**
     * Basically ISO 8601, no milliseconds, local timezone
     * (yyyy-mm-dd' 'hh:mm:ss)
     */
    public static SyncTimestampFormat TIME_FORMAT        = new SyncTimestampFormat();

    private static final String       DEFAULT_DOCWEB_URL = "https://docweb.jpl.nasa.gov/app";
    private static String             docwebUrl          = null;

    public static Stereotype getCommentStereotype() {
        Project project = Application.getInstance().getProject();
        return StereotypesHelper.getStereotype(project, DOCUMENT_COMMENT);
    }

    public static Stereotype getViewCommentStereotype() {
        Project project = Application.getInstance().getProject();
        return StereotypesHelper.getStereotype(project, DocGen3Profile.viewCommentStereotype);
    }

    public static String author(Comment c, Stereotype s) {
        return (String)StereotypesHelper.getStereotypePropertyFirst(c, s, AUTHOR);
    }

    public static String timestamp(Comment c, Stereotype s) {
        return (String)StereotypesHelper.getStereotypePropertyFirst(c, s, MODIFIED_TIMESTAMP);
    }

    public static boolean isDocumentComment(Element e) {
        if (!(e instanceof Comment))
            return false;
        return StereotypesHelper.isElementStereotypedBy(e, DOCUMENT_COMMENT);
    }

    public static Map<String, Comment> getLocalComments(Element documentView) {
        Map<String, Comment> comments = new HashMap<String, Comment>();
        for (Element e: documentView.getOwnedElement()) {
            if (CommentUtil.isDocumentComment(e)) {
                comments.put(e.getID(), (Comment)e);
            }
        }
        return comments;
    }

    public static boolean isLocallyModified(Comment local, Stereotype s, SyncedComment remote) {
        if (remote == null) {
            return false;
        }
        String localTimestamp = CommentUtil.timestamp(local, s);
        String remoteTimestamp = remote.getTimestamp();
        String localBody = local.getBody();
        String remoteBody = remote.getBody();

        if (localBody != null && localBody.equals(remoteBody)) {
            return false; // same content
        }
        if (localTimestamp != null && localTimestamp.compareTo(remoteTimestamp) <= 0) {
            return false; // local timestamp is earlier or identical
        }
        return true;
    }

    public static boolean isRemotelyModified(Comment local, Stereotype s, SyncedComment remote) {
        if (remote == null) {
            return false;
        }
        String localTimestamp = CommentUtil.timestamp(local, s);
        String remoteTimestamp = remote.getTimestamp();
        String localBody = local.getBody();
        String remoteBody = remote.getBody();

        if (remoteBody != null && remoteBody.equals(localBody)) {
            return false; // same content
        }
        if (remoteTimestamp != null && remoteTimestamp.compareTo(localTimestamp) <= 0) {
            return false; // remote timestamp is earlier or identical
        }
        return true;
    }

    /**
     * Use this to correlate comments that were created remotely without ID with
     * their local counterparts. This handles the situation where we don't have
     * the remote ID. For example, maybe a comment is created remotely,
     * imported, and then exported again. Or maybe a remote comment is imported
     * twice; we only want to add it once to the local comments.
     * 
     * @param c
     *            either local comment or converted from remote comment
     * @return
     */
    public static Integer checksum(SyncedComment c) {
        return (c.getAuthor() + "|" + c.getTimestamp() + "|" + c.getBody()).hashCode();
    }

    /**
     * Use this to correlate comments that were created remotely without ID with
     * their local counterparts. This handles the situation where we don't have
     * the remote ID. For example, maybe a comment is created remotely,
     * imported, and then exported again. Or maybe a remote comment is imported
     * twice; we only want to add it once to the local comments.
     * 
     * @param c
     *            local comment
     * @param s
     *            stereotype for DocumentComment, so we don't have to retrieve
     *            it
     * @return
     */
    public static Integer checksum(Comment c, Stereotype s) {
        return (author(c, s) + "|" + timestamp(c, s) + "|" + c.getBody()).hashCode();
    }

    public static String getDocwebUrl() {
        if (docwebUrl != null) {
            return docwebUrl;
        }
        String url = JOptionPane.showInputDialog("Enter the DocWeb URL:", DEFAULT_DOCWEB_URL);
        if (url == null) {
            return url;
        }
        url = url.replaceAll("/+$", "");
        docwebUrl = url;
        return url;
    }

    public static String truncateBody(String body) {
        String plaintext = body.replaceFirst("^(.*<body[^>]*>)?", "");
        plaintext = plaintext.replaceFirst("</\\s*body.*$", "");
        plaintext = plaintext.replaceFirst("</\\s*html.*$", ""); // could have
                                                                 // html element
                                                                 // without body
        plaintext = plaintext.trim();
        plaintext = plaintext.replaceAll("\\s+", " ");
        plaintext = plaintext.replaceAll("<[^>]*>", "");
        if (plaintext.length() > 40) {
            return plaintext.substring(0, 40) + "...";
        }
        return plaintext;
    }
}
