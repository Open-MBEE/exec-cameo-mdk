package gov.nasa.jpl.mgss.mbee.docgen.sync;

public class SyncedComment {

	public enum Action { ADD, MODIFY, DELETE; };

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
