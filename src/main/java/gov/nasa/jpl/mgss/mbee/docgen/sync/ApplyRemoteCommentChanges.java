package gov.nasa.jpl.mgss.mbee.docgen.sync;

import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.AUTHOR;
import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.DOCUMENT_COMMENT;
import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.MODIFIED_TIMESTAMP;

import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

public class ApplyRemoteCommentChanges extends ChangeTheModel {

    private final Element              owner;
    private final Map<String, Comment> localComments;
    private final List<SyncedComment>  added;
    private final List<SyncedComment>  modified;
    private final List<SyncedComment>  deleted;
    private final Stereotype           stereotype;
    private final ElementsFactory      factory;
    private final GUILog               log;

    private int                        successfulAdds     = 0;
    private int                        successfulModifies = 0;
    private int                        successfulDeletes  = 0;
    private int                        failures           = 0;

    public ApplyRemoteCommentChanges(Element owner, Map<String, Comment> localComments,
            List<SyncedComment> added, List<SyncedComment> modified, List<SyncedComment> deleted) {
        this.owner = owner;
        this.localComments = localComments;
        this.added = added;
        this.modified = modified;
        this.deleted = deleted;
        this.stereotype = getStereotype(DOCUMENT_COMMENT);
        this.factory = Application.getInstance().getProject().getElementsFactory();
        this.log = Application.getInstance().getGUILog();
    }

    @Override
    public String getDescription() {
        return "Add Comments";
    }

    @Override
    protected void makeChange() {
        // Disable local timestamp updates for the duration of this operation.
        // Re-enable them later in cleanUp().
        CommentChangeListener.disable();

        if (stereotype == null) {
            fail("Couldn't find " + DOCUMENT_COMMENT + " stereotype");
            return;
        }
        for (SyncedComment imported: added) {
            add(imported);
        }
        for (SyncedComment imported: modified) {
            modify(imported);
        }
        for (SyncedComment imported: deleted) {
            delete(imported);
        }
        if (successfulAdds == 0 && successfulModifies == 0 && successfulDeletes == 0) {
            if (failures > 0) {
                log("Comment import failed");
                JOptionPane.showMessageDialog(null, "Could not import changes."
                        + "\nSee messages window for details.");
            } else {
                log("No comment changes to import");
            }
        } else {
            if (failures > 0) {
                log(String.format("Imported comments (%d added, %d modified, %d deleted)"
                        + " - %d CHANGES FAILED", successfulAdds, successfulModifies, successfulDeletes,
                        failures));
                JOptionPane.showMessageDialog(null,
                        "Some changes could not be imported.\nSee messages window for details.");
            } else {
                log(String.format("Imported comments (%d added, %d modified, %d deleted)", successfulAdds,
                        successfulModifies, successfulDeletes));
            }
        }
    }

    @Override
    protected void cleanUp() {
        CommentChangeListener.enable();
    }

    private void add(SyncedComment imported) {
        Comment c = factory.createCommentInstance();
        if (!StereotypesHelper.canApplyStereotype(c, stereotype)) {
            log("Error adding comment: can't apply stereotype");
            ++failures;
            return;
        }
        c.setBody(imported.getBody());
        StereotypesHelper.addStereotype(c, stereotype);
        StereotypesHelper.setStereotypePropertyValue(c, stereotype, MODIFIED_TIMESTAMP,
                imported.getTimestamp());
        StereotypesHelper.setStereotypePropertyValue(c, stereotype, AUTHOR, imported.getAuthor());
        c.setOwningElement(owner);

        ++successfulAdds;
        log(String.format("Added comment: [%s %s] %s", imported.getAuthor(), imported.getTimestamp(),
                CommentUtil.truncateBody(imported.getBody())));
    }

    private void modify(SyncedComment imported) {
        Comment c = localComments.get(imported.getId());
        if (c == null) {
            log("Can't find local comment to apply imported modification: " + imported.getId() + " "
                    + CommentUtil.truncateBody(imported.getBody()));
            ++failures;
            return;
        }
        c.setBody(imported.getBody());
        StereotypesHelper.setStereotypePropertyValue(c, stereotype, MODIFIED_TIMESTAMP,
                imported.getTimestamp());
        StereotypesHelper.setStereotypePropertyValue(c, stereotype, AUTHOR, imported.getAuthor());

        ++successfulModifies;
        log(String.format("Modified comment: [%s %s] %s", imported.getAuthor(), imported.getTimestamp(),
                CommentUtil.truncateBody(imported.getBody())));
    }

    private void delete(SyncedComment imported) {
        Comment c = localComments.get(imported.getId());
        if (c == null) {
            // This can happen when comments are created and deleted
            // on DocWeb before the import happens.
            // log("Can't find local comment to apply imported deletion: "
            // + imported.getId() + " "
            // + CommentUtil.truncateBody(imported.getBody()));
            return; // close enough
        }
        try {
            ModelElementsManager.getInstance().removeElement(c);
        } catch (ReadOnlyElementException e) {
            log(String.format("Can't delete, marked as read-only: %s (ID %s)",
                    CommentUtil.truncateBody(imported.getBody()), imported.getId()));
            ++failures;
            return;
        }
        ++successfulDeletes;
        log(String.format("Deleted comment: [%s %s] %s", imported.getAuthor(), imported.getTimestamp(),
                CommentUtil.truncateBody(imported.getBody())));
    }

    private void log(String message) {
        log.log(message);
    }

}
