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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

@SuppressWarnings("serial")
public class ExportComments extends MDAction {
    private NamedElement      documentView;
    private CommentRepository repository;

    public ExportComments(NamedElement selectedElement) {
    	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
    	//
        super("ExportCommentsFromDocWeb", "Commit Comments to DocWeb", null, null);
        this.documentView = selectedElement;
    }

    @Override
    public void actionPerformed(ActionEvent ac) {
        ImportComments ic = new ImportComments(documentView, repository);
        ic.actionPerformed(null); // this is a band-aid to force an import
                                  // before an export because of a bug
        // if ppl don't import comments before exporting those comments can get
        // deleted on docweb

        if (!okToProceed()) {
            return;
        }
        String url = CommentUtil.getDocwebUrl();
        if (url == null) { // user can cancel when asked for URL
            return;
        }
        repository = new DocwebCommentRepository(url);

        GUILog log = Application.getInstance().getGUILog();
        Project project = Application.getInstance().getProject();
        Stereotype stereotype = StereotypesHelper.getStereotype(project, CommentUtil.DOCUMENT_COMMENT);

        // Find all local comments for the selected document.
        Map<String, Comment> localComments = CommentUtil.getLocalComments(documentView);
        Map<Integer, Comment> localCommentsByChecksum = new HashMap<Integer, Comment>();
        for (Comment c: localComments.values()) {
            localCommentsByChecksum.put(CommentUtil.checksum(c, stereotype), c);
            // log.log("Local checksum " + CommentUtil.checksum(c, stereotype) +
            // " " + CommentUtil.truncateBody(c.getBody())); // DEBUG
        }

        try {
            // Make an index of all remote comments, actually two
            // because remote comments are created without ID.
            // They need to be imported and then exported
            // before they have an ID in the repository.
            // Meanwhile let's use a "checksum" to find identical local
            // comments, which have an ID generated automatically on import.
            Map<String, SyncedComment> remoteCommentsWithId = new HashMap<String, SyncedComment>();
            Map<Integer, SyncedComment> remoteCommentsWithoutId = new HashMap<Integer, SyncedComment>();
            for (SyncedComment c: repository.getComments(documentView)) {
                String id = c.getId();
                if (id != null && !id.isEmpty()) {
                    remoteCommentsWithId.put(id, c);
                    // log.log("Remote ID: " + id + " " +
                    // CommentUtil.truncateBody(c.getBody())); // DEBUG
                } else {
                    remoteCommentsWithoutId.put(CommentUtil.checksum(c), c);
                    // log.log("Remote checksum: " + CommentUtil.checksum(c)
                    // +" " + CommentUtil.truncateBody(c.getBody())); // DEBUG
                }
            }

            // Prepare lists of changes to send to the repository.
            List<SyncedComment> added = new ArrayList<SyncedComment>();
            List<SyncedComment> modified = new ArrayList<SyncedComment>();
            List<SyncedComment> deleted = new ArrayList<SyncedComment>();

            // Find comments that were added and modified locally.
            // Also check for incoming comments that were deleted.
            for (Comment local: localComments.values()) {
                SyncedComment remote = remoteCommentsWithId.get(local.getID());
                if (remote == null) {
                    // log.log("Local comment not found in remotes by ID: "
                    // +local.getID() +" " +
                    // CommentUtil.truncateBody(local.getBody())); // DEBUG
                    // Didn't find by ID, so try finding by checksum, for
                    // comments that were created remotely and haven't had
                    // their IDs sent back to the repository.
                    SyncedComment converted = convert(local, stereotype, SyncedComment.Action.MODIFY);
                    if (remoteCommentsWithoutId.containsKey(CommentUtil.checksum(converted))) {
                        // log.log("Local comment not found in remotes by checksum: "
                        // + CommentUtil.checksum(converted) +" " +
                        // CommentUtil.truncateBody(converted.getBody())); //
                        // DEBUG
                        modified.add(converted);
                    } else {
                        added.add(convert(local, stereotype, SyncedComment.Action.ADD));
                    }
                } else if (CommentUtil.isLocallyModified(local, stereotype, remote)) {
                    modified.add(convert(local, stereotype, SyncedComment.Action.MODIFY));
                }
            }

            // Find comments that were deleted locally.
            for (SyncedComment remote: remoteCommentsWithId.values()) {
                if (!remote.isDeleted() && !localComments.containsKey(remote.getId())) {
                    deleted.add(remote.deletedClone());
                }
            }
            for (SyncedComment remote: remoteCommentsWithoutId.values()) {
                if (!remote.isDeleted() && !localCommentsByChecksum.containsKey(CommentUtil.checksum(remote))) {
                    deleted.add(remote.deletedClone());
                }
            }

            // Submit changes to the repository.
            if (added.isEmpty() && modified.isEmpty() && deleted.isEmpty()) {
                log.log("Exported comments (no changes)");
            } else {
                repository.sendComments(documentView, added, modified, deleted);
                log.log(String.format("Exported comments (%d added, %d modified, %d deleted)", added.size(),
                        modified.size(), deleted.size()));
            }
        } catch (CommentSyncFailure e) {
            fail(e.getMessage());
        } finally {
            if (repository != null) // cleanup
                try {
                    repository.close();
                } catch (CommentSyncFailure e) { /* IGNORE */
                }
        }
    }

    /**
     * Verify that the project is committed to TeamWork (or saved for local
     * projects). Allow user to proceed anyway after a warning, if they promise
     * to be careful.
     * 
     * @return true if it is ok to proceed
     */
    private boolean okToProceed() {
        Project project = Application.getInstance().getProject();
        if (project == null) { // shouldn't happen
            return false;
        }
        if (ApplicationSyncEventSubscriber.isCommitted(project)) {
            return true;
        }
        String dirtyWord = project.isRemote() ? "uncommitted" : "unsaved";
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(Application.getInstance()
                .getMainFrame(), "Project has " + dirtyWord + " changes.\nDo you want to continue?",
                "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    private SyncedComment convert(Comment comment, Stereotype stereotype, SyncedComment.Action action) {
        return new SyncedComment(comment.getID(), CommentUtil.author(comment, stereotype),
                CommentUtil.timestamp(comment, stereotype), comment.getBody(), action);
    }

    private void fail(String reason) {
        JOptionPane.showMessageDialog(null, "Failed: " + reason);
    }

}
