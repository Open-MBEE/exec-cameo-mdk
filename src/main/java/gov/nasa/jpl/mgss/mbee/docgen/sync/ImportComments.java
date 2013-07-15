package gov.nasa.jpl.mgss.mbee.docgen.sync;

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
	private NamedElement documentView;
	private CommentRepository repository;
	
	public ImportComments(NamedElement selectedElement) {
		super("ImportCommentsFromDocWeb", "Import Comments from DocWeb", null, null);
		this.documentView = selectedElement;
		setEnabled(selectedElement.isEditable());
	}

	//this is for export comments action to call so make sure ppl import first than export
	public ImportComments(NamedElement ne, CommentRepository re) {
		super("ImportComments", "Import Comments", null, null);
		documentView = ne;
		repository = re;
	}
	
	@Override
	public void actionPerformed(ActionEvent ac) {
		if (!documentView.isEditable()) {
			JOptionPane.showMessageDialog(Application.getInstance().getMainFrame(),
					"You must lock document for editing",
					"Permission Denied", JOptionPane.WARNING_MESSAGE);
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
		Map<String,Comment> localComments = null;

		Stereotype s = StereotypesHelper.getStereotype(
				Application.getInstance().getProject(),
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
		int answer = JOptionPane.showConfirmDialog(
				Application.getInstance().getMainFrame(),
				"Would you like to update the project first?",
				"Update Remote Project",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
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
