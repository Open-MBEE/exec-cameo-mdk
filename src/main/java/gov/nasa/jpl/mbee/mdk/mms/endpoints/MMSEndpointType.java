package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum MMSEndpointType {
    LOGIN("mmslogin", "/authentication", MMSLoginEndpoint.class),
    ORGS("mmsorgs", "/orgs", MMSOrgsEndpoint.class),
    PROJECTS("mmsprojects", "/projects", MMSProjectsEndpoint.class),
    PROJECT("mmsproject", "/projects/~project_id~", MMSProjectEndpoint.class),
    REFS("mmsrefs", "/projects/~project_id~/refs", MMSRefsEndpoint.class),
    REF("mmsref", "/projects/~project_id~/refs/~ref_id~", MMSRefEndpoint.class),
    COMMITS("mmscommits", "/projects/~project_id~/refs/~ref_id~/commits", MMSCommitsEndpoint.class),
    COMMIT("mmscommit", "/projects/~project_id~/commits/~commit_id~", MMSCommitEndpoint.class),
    ELEMENTS("mmselements", "/projects/~project_id~/refs/~ref_id~/elements", MMSElementsEndpoint.class),
    SEARCH("mmssearch", "/projects/~project_id~/refs/~ref_id~/search", MMSSearchEndpoint.class);

    private String name;
    private String path;
    private Class<? extends MMSEndpoint> instance;

    MMSEndpointType(String name, String path, Class<? extends MMSEndpoint> instance) {
        this.name = name;
        this.path = path;
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
            return path;
        }

    public Constructor<? extends MMSEndpoint> getInstance() {
        try {
            return instance.getDeclaredConstructor(String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final String PROJECT_ID_PLACEHOLDER = "~project_id~";
    public static final String REF_ID_PLACEHOLDER = "~ref_id~";
    public static final String COMMIT_ID_PLACEHOLDER = "~commit_id~";

    public static final String IMPROPER_URI_ERROR_PREFIX = "[ERROR] Unexpected error in generation of MMS URL for ";
    public static final String IMPROPER_URI_ERROR_PROJECT_SUFFIX = "project. Reason: Improper base URI.";
    public static final String IMPROPER_URI_ERROR_REF_SUFFIX = "branch. Reason: Improper base URI.";

    public static final String AUTHENTICATION_RESPONSE_JSON_KEY = "token";
}
