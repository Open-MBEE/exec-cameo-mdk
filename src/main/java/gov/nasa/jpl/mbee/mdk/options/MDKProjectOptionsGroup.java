package gov.nasa.jpl.mbee.mdk.options;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.AbstractPropertyOptionsGroup;

public class MDKProjectOptionsGroup extends AbstractPropertyOptionsGroup {

        public static final String ID = "options.project.mdk";
        public static final String GROUP = "GROUP";
        private static gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup tempInstance = null;

        public static final String LOG_JSON_ID = "LOG_JSON_ID",
                PERSIST_CHANGELOG_ID = "PERSIST_CHANGELOG_ID",
                ENABLE_CHANGE_LISTENER_ID = "ENABLE_CHANGE_LISTENER_ID",
                ENABLE_COORDINATED_SYNC_ID = "ENABLE_COORDINATED_SYNC_ID",
                CUSTOM_USER_SCRIPT_DIRECTORIES_ID = "CUSTOM_USER_SCRIPT_DIRECTORIES_ID",
                MMS_AUTHENTICATION_CHAIN = "MMS_AUTHENTICATION_CHAIN",
                DOCBOOK_TO_PDF_STYLESHEET = "DOCBOOK_TO_PDF_STYLESHEET";

    public MDKProjectOptionsGroup() {
        super(ID);
    }

    public static MDKProjectOptionsGroup getMDKOptions() {
        MDKProjectOptionsGroup group = (MDKProjectOptionsGroup) Application.getInstance().getProject().getOptions().getGroup()
        if (group == null) {
            if (tempInstance == null) {
                tempInstance = new MDKOptionsGroup();
            }
            return tempInstance;
        }
        return group;
    }
}