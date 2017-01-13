package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import gov.nasa.jpl.mbee.mdk.ems.*;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ExportLocalModule extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private final IProject module;
    private final Set<Element> mounts;
    private final String siteName;
    private final Project project;

    public ExportLocalModule(IProject module, Set<Element> mounts, String siteName, Project project) {
        super("ExportModule", "Export Module", null, null);
        this.module = module;
        this.mounts = mounts;
        this.siteName = siteName;
        this.project = project;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProgressStatusRunner.runWithProgressStatus(new ModuleExportRunner(), "Exporting Module", true, 0);
    }

    public class ModuleExportRunner implements RunnableWithProgress {
        @Override
        public void run(ProgressStatus progressStatus) {
            ObjectNode requestData = JacksonUtils.getObjectMapper().createObjectNode();
            ArrayNode elementsArrayNode = JacksonUtils.getObjectMapper().createArrayNode();
            requestData.set("elements", elementsArrayNode);
            requestData.put("source", "magicdraw");
            requestData.put("mdkVersion", MDKPlugin.VERSION);
            ObjectNode projectObjectNode = MMSUtils.getProjectObjectNode(module);
            elementsArrayNode.add(projectObjectNode);

            URIBuilder requestUri = MMSUtils.getServiceSitesProjectsUri(project);
            if (requestUri == null) {
                return;
            }
            Utils.guilog("Initializing module");
            try {
                ObjectNode response = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, requestData));
            } catch (IOException | URISyntaxException | ServerException e) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected error occurred when initializing module.");
                e.printStackTrace();
                return;
            }

            // should be safe to pass projects here since the expectation is that all it is used for is to build urls
            ProgressStatusRunner.runWithProgressStatus(new ManualSyncActionRunner<>(CommitClientElementAction.class, mounts, project, true, -1), "Model Initialization", true, 0);
        }
    }
}

