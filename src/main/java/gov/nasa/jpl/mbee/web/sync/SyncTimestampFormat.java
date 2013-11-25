package gov.nasa.jpl.mbee.web.sync;

import java.text.SimpleDateFormat;

/**
 * Basically ISO 8601, UTC, no milliseconds, local timezone
 * (yyyy-mm-dd' 'hh:mm:ss)
 */
public class SyncTimestampFormat extends SimpleDateFormat {
    private static final long serialVersionUID = 1L;

    public SyncTimestampFormat() {
        super("yyyy-MM-dd' 'HH:mm:ss");
    }
}
