package gov.nasa.jpl.mbee.mdk.http;

import org.apache.commons.lang.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.Arrays;

public class ServerException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String response;
    private int code;

    public ServerException(String response, int code) {
        super(buildMessage(code));
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

    private static String buildMessage(int code) {
        Field codeConstantField = Arrays.stream(HttpURLConnection.class.getFields()).filter(field -> {
            if (!Modifier.isStatic(field.getModifiers()) || !Integer.TYPE.isAssignableFrom(field.getType())) {
                return false;
            }
            int constant;
            try {
                constant = field.getInt(null);
            } catch (IllegalAccessException e) {
                return false;
            }
            return code == constant;
        }).findAny().orElse(null);
        String message = Integer.toString(code);
        if (codeConstantField != null) {
            String status = codeConstantField.getName();
            String prefix = "HTTP_";
            if (status.startsWith(prefix)) {
                status = status.substring(prefix.length());
            }
            status = WordUtils.capitalizeFully(status, new char[]{'_'}).replace('_', ' ');
            message += " " + status;
        }
        return message;
    }
}
