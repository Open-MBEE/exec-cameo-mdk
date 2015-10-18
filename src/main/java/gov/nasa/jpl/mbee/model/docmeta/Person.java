package gov.nasa.jpl.mbee.model.docmeta;

public class Person {

    private String firstname;
    private String lastname;
    private String title;
    private String orgname;
    private String orgdiv;
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getOrgname() {
        return orgname;
    }
    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }
    public String getOrgdiv() {
        return orgdiv;
    }
    public void setOrgdiv(String orgdiv) {
        this.orgdiv = orgdiv;
    }
}
