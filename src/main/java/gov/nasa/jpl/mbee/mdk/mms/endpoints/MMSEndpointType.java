package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public enum MMSEndpointType {
    LOGIN("mmslogin", "/authentication"),
    ORGS("mmsorgs", "/orgs"),
    PROJECTS("mmsprojects", "/projects"),
    PROJECT("mmsproject", ""),
    REFS("mmsrefs", "/refs"),
    REF("mmsref", ""),
    COMMITS("mmscommits", "/commits"),
    COMMIT("mmscommit", "/commits"), // has same path as COMMITS due to current rest pattern
    ELEMENTS("mmselements", "/elements"),
    ELEMENT("mmselement", ""),
    SEARCH("mmssearch", "/search");

    private String name;
    private String path;

    MMSEndpointType(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
            return path;
        }

    public static final String IMPROPER_URI_ERROR_PREFIX = "[ERROR] Unexpected error in generation of MMS URL for ";
    public static final String IMPROPER_URI_ERROR_PROJECT_SUFFIX = "project. Reason: Improper base URI.";
    public static final String IMPROPER_URI_ERROR_REF_SUFFIX = "branch. Reason: Improper base URI.";

    public static final String AUTHENTICATION_RESPONSE_JSON_KEY = "token";
}
