package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSEndpointConstants {
    public static final String LOGIN_CASE = "mmslogin";
    public static final String LOGIN_ENDPOINT = "/authentication";

    public static final String ORGS_CASE = "mmsorgs";
    public static final String ORGS_ENDPOINT = "/orgs";

    public static final String PROJECTS_CASE = "mmsprojects";
    public static final String PROJECTS_ENDPOINT = "/projects";

    public static final String REFS_CASE = "mmsrefs";
    public static final String REFS_ENDPOINT = "/projects/~project_id~/refs";

    public static final String ELEMENTS_CASE = "mmselements";
    public static final String ELEMENTS_ENDPOINT = "/projects/~project_id~/refs/~ref_id~/elements";

    public static final String PROJECT_ID_PLACEHOLDER = "~project_id~";
    public static final String REF_ID_PLACEHOLDER = "~ref_id~";

    public static final String IMPROPER_URI_ERROR_PREFIX = "[ERROR] Unexpected error in generation of MMS URL for ";
    public static final String IMPROPER_URI_ERROR_PROJECT_SUFFIX = "project. Reason: Improper base URI.";
    public static final String IMPROPER_URI_ERROR_REF_SUFFIX = "branch. Reason: Improper base URI.";
}
