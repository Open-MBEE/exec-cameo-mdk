package gov.nasa.jpl.mbee.web.sync;

public class CommentSyncFailure extends Exception {
    private static final long serialVersionUID = 1L;

    public CommentSyncFailure(String message) {
        super(message);
    }

    public CommentSyncFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
