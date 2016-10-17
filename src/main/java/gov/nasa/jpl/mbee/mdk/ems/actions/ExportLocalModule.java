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
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ModelExporter;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;

//@donbot
@Deprecated
public class ExportLocalModule extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public class ModuleExportRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus progressStatus) {
            ObjectNode requestData = JacksonUtils.getObjectMapper().createObjectNode();
            ArrayNode elementsArrayNode = JacksonUtils.getObjectMapper().createArrayNode();
            requestData.set("elements", elementsArrayNode);
            requestData.put("source", "magicdraw");
            requestData.put("mmsVersion", MDKPlugin.VERSION);
            ObjectNode projectObjectNode = ExportUtility.getProjectObjectNode(module);
            elementsArrayNode.add(projectObjectNode);

            URIBuilder requestUri = MMSUtils.getServiceWorkspacesSitesUri(project);
            if (requestUri == null) {
                return;
            }
            requestUri.setPath(requestUri.getPath() + "/projects");

            Utils.guilog("Initializing module");
            try {
                ObjectNode response = MMSUtils.sendMMSRequest(MMSUtils.HttpRequestType.POST, requestUri, requestData);
                //TODO response processing?
            } catch (IOException | URISyntaxException | ServerException e) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected error occurred when initializing module.");
                e.printStackTrace();
                return;
            }

            //@Deprecated @donbot
            ModelExporter me = new ModelExporter(mounts, 0, false, module);
            JSONObject result = me.getResult();
            String json = result.toJSONString();
            Utils.guilog("Number of Elements: " + me.getNumberOfElements());
            if (ExportUtility.send(url + "/workspaces/master/sites/" + siteName + "/elements?background=true", json) != null) {
                Utils.guilog("You'll receive an email when the module has finished loading.");
            }
        }
    }

    private static final long serialVersionUID = 1L;
    private IProject module;
    private Set<Element> mounts;
    private String siteName;

    public ExportLocalModule(IProject module, Set<Element> mounts, String siteName) {
        super("ExportModule", "Export Module", null, null);
        this.module = module;
        this.mounts = mounts;
        this.siteName = siteName;
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
}

