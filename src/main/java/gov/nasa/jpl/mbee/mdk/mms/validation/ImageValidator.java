package gov.nasa.jpl.mbee.mdk.mms.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.ExportImage;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ImageValidator {

    private Map<String, ObjectNode> images;
    private ValidationSuite suite = new ValidationSuite("images");
    private ValidationRule rule = new ValidationRule("Image Outdated", "Diagram is outdated", ViolationSeverity.ERROR);
    private Map<String, ObjectNode> allImages;

    public ImageValidator(Map<String, ObjectNode> images, Map<String, ObjectNode> allImages) {
        this.images = images;
        this.allImages = allImages;
        if (allImages == null) {
            this.allImages = new HashMap<>();
        }
        this.allImages.putAll(images);
        suite.addValidationRule(rule);
    }

    public void validate(Project project) {
        for (String key : images.keySet()) {
            // customize request
            URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
            if (requestUri == null) {
                return;
            }
            Element e = Converters.getIdToElementConverter().apply(key, project);
            String id = key.replace(".", "%2E");
            requestUri.setPath(requestUri.getPath() + "/" + id);

            JsonNode value;

            String cs = "";
            if ((value = images.get(key).get("cs")) != null && value.isTextual()) {
                cs = value.asText();
            }
            requestUri.setParameter("cs", cs);

            String extension = "svg";
            if ((value = images.get(key).get("extension")) != null && value.isTextual()) {
                extension = value.asText();
            }
            requestUri.setParameter("extension", extension);

            File responseFile;
            ObjectNode response;
            // do request
            try {
                HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri);
                request.setHeader("Accept", "image/" + extension);
                responseFile = MMSUtils.sendMMSRequest(project, request);
                try (JsonParser responseParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                    response = JacksonUtils.parseJsonObject(responseParser);
                    if (((value = response.get("message")) == null || !value.isTextual() || value.asText().contains("not found"))
                            || ((value = response.get("cs")) == null || !value.isTextual() || !value.asText().equals(cs))) {
                        ValidationRuleViolation v = new ValidationRuleViolation(e, "[IMAGE] This image is outdated on the web.");
                        v.addAction(new ExportImage(e, allImages));
                        rule.addViolation(v);
                    }
                }
            } catch (IOException | URISyntaxException | ServerException e1) {
                Application.getInstance().getGUILog().log("[ERROR] An error occurred while validating image " + key + ". Image validation aborted. Reason: " + e1.getMessage());
                e1.printStackTrace();
                continue;
            }
        }
    }

    public ValidationSuite getSuite() {
        return suite;
    }

    public ValidationRule getRule() {
        return rule;
    }
}
