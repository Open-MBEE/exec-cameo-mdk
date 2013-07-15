package gov.nasa.jpl.mgss.mbee.docgen.sync;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public interface CommentRepository {

	void connect() throws CommentSyncFailure;

	List<SyncedComment> getComments(NamedElement document)
			throws CommentSyncFailure;

	void sendComments(NamedElement document,
			List<SyncedComment> newComments,
			List<SyncedComment> modifiedComments,
			List<SyncedComment> deletedComments) throws CommentSyncFailure;

	void close() throws CommentSyncFailure;
}
