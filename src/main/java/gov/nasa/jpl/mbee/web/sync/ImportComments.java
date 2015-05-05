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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

@SuppressWarnings("serial")
public class ImportComments extends MDAction {
    private NamedElement      documentView;
    private CommentRepository repository;

    public ImportComments(NamedElement selectedElement) {
    	//JJS--MDEV-567 fix: changed 'Import' to 'Accept'
    	//
        super("ImportCommentsFromDocWeb", "Accept Comments from DocWeb", null, null);
        this.documentView = selectedElement;
        setEnabled(selectedElement.isEditable());
    }

    // this is for export comments action to call so make sure ppl import first
    // than export
    public ImportComments(NamedElement ne, CommentRepository re) {
    	//JJS--MDEV-567 fix: changed 'Import' to 'Accept'
    	//
        super("ImportComments", "Accept Comments", null, null);
        documentView = ne;
        repository = re;
    }

    @Override
    public void actionPerformed(ActionEvent ac) {
        if (!documentView.isEditable()) {
            JOptionPane.showMessageDialog(Application.getInstance().getMainFrame(),
                    "You must lock document for editing", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!updateRemoteProject()) { // user can say yes/no/cancel to update
            return; // user canceled the operation
        }

        String url = CommentUtil.getDocwebUrl();
        if (url == null) {
            return;
        }
        repository = new DocwebCommentRepository(url);

        List<SyncedComment> added = new ArrayList<SyncedComment>();
        List<SyncedComment> modified = new ArrayList<SyncedComment>();
        List<SyncedComment> deleted = new ArrayList<SyncedComment>();
        Map<String, Comment> localComments = null;

        Stereotype s = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                CommentUtil.DOCUMENT_COMMENT);

        try {
            repository.connect();
            List<SyncedComment> importedComments = repository.getComments(documentView);
            localComments = CommentUtil.getLocalComments(documentView);
            Set<Integer> localChecksums = new HashSet<Integer>();
            for (Comment c: localComments.values()) {
                localChecksums.add(CommentUtil.checksum(c, s));
            }

            for (SyncedComment remote: importedComments) {
                if (remote.isDeleted()) {
                    deleted.add(remote);
                    continue;
                }
                String remoteId = remote.getId();
                if (remoteId == null || remoteId.isEmpty()) {
                    if (!localChecksums.contains(CommentUtil.checksum(remote))) {
                        added.add(remote);
                    }
                    continue;
                }
                Comment local = localComments.get(remote.getId());
                if (local != null && CommentUtil.isRemotelyModified(local, s, remote)) {
                    modified.add(remote);
                }
            }
            repository.close();
        } catch (CommentSyncFailure e) {
            fail(e.getMessage());
            return;
        }
        new ApplyRemoteCommentChanges(documentView, localComments, added, modified, deleted).run();
    }

    private boolean updateRemoteProject() {
        Project project = Application.getInstance().getProject();
        if (!project.isRemote()) {
            return true;
        }
        int answer = JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(),
                "Would you like to update the project first?", "Update Remote Project",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        switch (answer) {
            case JOptionPane.CANCEL_OPTION:
                return false;
            case JOptionPane.YES_OPTION:
                TeamworkUtils.updateProject(Application.getInstance().getProject());
        }
        return true;
    }

    private void fail(String reason) {
        JOptionPane.showMessageDialog(null, "Failed: " + reason);
    }
}
