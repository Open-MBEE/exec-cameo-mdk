package gov.nasa.jpl.mgss.mbee.docgen.sync;

import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.AUTHOR;
import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.DOCUMENT_COMMENT;
import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.MODIFIED_TIMESTAMP;
import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.TIME_FORMAT;

import java.util.Date;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class UpdateCommentTags extends ChangeTheModel {
    private final Comment comment;

    public UpdateCommentTags(Comment comment) {
        this.comment = comment;
    }

    @Override
    public String getDescription() {
        return "Update DocumentComment Timestamp";
    }

    @Override
    protected void makeChange() {
        if (comment == null) {
            fail("Comment not properly set for action");
            return;
        }
        Stereotype stereotype = getStereotype(DOCUMENT_COMMENT);
        if (stereotype == null) {
            fail("Couldn't find " + DOCUMENT_COMMENT + " stereotype");
            return;
        }

        String timestamp = TIME_FORMAT.format(new Date());
        StereotypesHelper.setStereotypePropertyValue(comment, stereotype, MODIFIED_TIMESTAMP, timestamp);

        String username = getUsername();
        if (!username.isEmpty()) {
            StereotypesHelper.setStereotypePropertyValue(comment, stereotype, AUTHOR, username);
        }
    }
}
