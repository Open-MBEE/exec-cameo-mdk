package gov.nasa.jpl.mbee.model.docmeta;

public class Revision {

    private String revNumber;
    private String date;
    private String firstName;
    private String lastName;
    private String remark;

    public String getRevNumber() {
        return revNumber;
    }

    public void setRevNumber(String revNumber) {
        this.revNumber = revNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
