package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.util.TaskRunner;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

public class ExportImage extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element element;
    private Map<String, ObjectNode> images;

    public ExportImage(Element e, Map<String, ObjectNode> images) {
        super("ExportImage", "Commit image", null, null);
        this.element = e;
        this.images = images;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    public static boolean postImage(Project project, String key, Map<String, ObjectNode> is) {
        if (is == null || is.get(key) == null) {
            Utils.guilog("[ERROR] Image data with id " + key + " not found.");
            return false;
        }

        URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
        if (requestUri == null) {
            return false;
        }

        String id = key.replace(".", "%2E");
        requestUri.setPath(requestUri.getPath() + "/" + id);

        JsonNode value;

        String cs = "";
        if ((value = is.get(key).get("cs")) != null && value.isTextual()) {
            cs = value.asText();
        }
        requestUri.setParameter("cs", cs);

        String extension = "";
        if ((value = is.get(key).get("extension")) != null && value.isTextual()) {
            extension = value.asText();
        }
        requestUri.setParameter("extension", extension);

        String filename = "";
        if ((value = is.get(key).get("abspath")) != null && value.isTextual()) {
            filename = value.asText();
        }
        File file = new File(filename);

        try {
            HttpRequestBase request = MMSUtils.buildImageRequest(requestUri, file);
            TaskRunner.runWithProgressStatus(progressStatus -> {
                try {
                    MMSUtils.sendMMSRequest(project, request, progressStatus);
                } catch (IOException | ServerException | URISyntaxException e) {
                    // TODO Implement error handling that was previously not possible due to OutputQueue implementation
                    e.printStackTrace();
                }
            }, "Image Create/Update", true, TaskRunner.ThreadExecutionStrategy.SINGLE);
        } catch (IOException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to commit image " + filename + ". Reason: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        for (Annotation anno : annos) {
            Element e = (Element) anno.getTarget();
            String key = Converters.getElementToIdConverter().apply(e);
            postImage(Project.getProject(e), key, images);
        }
        Utils.guilog("[INFO] Requests are added to queue.");

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String key = Converters.getElementToIdConverter().apply(element);
        if (postImage(Project.getProject(element), key, images)) {
            Utils.guilog("[INFO] Request is added to queue.");
        }
    }
}
