package gov.nasa.jpl.mbee.mdk.ems;

public class ServerException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String response;
    private int code;

    public ServerException(String response, int code) {
        this.setResponse(response);
        this.setCode(code);
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}
