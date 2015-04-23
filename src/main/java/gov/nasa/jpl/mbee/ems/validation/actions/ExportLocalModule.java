package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ModelExporter;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.ci.persistence.IProject;
import com.nomagic.ci.persistence.mounting.IMountPoint;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ExportLocalModule extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public class ModuleExportRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus arg0) {
            GUILog gl = Application.getInstance().getGUILog();
            JSONObject tosend = new JSONObject();
            JSONArray array = new JSONArray();
            tosend.put("elements", array);
            tosend.put("source", "magicdraw");
            JSONObject ob = ExportUtility.getProjectJsonForProject(module);
            array.add(ob);
            String url = ExportUtility.getUrl();
            if (url == null)
                return;
            String purl = url + "/workspaces/master/sites/" + siteName + "/projects";
            gl.log("Initializing module");
            if (ExportUtility.send(purl, tosend.toJSONString(), null, false, false) == null)
                return;
            
            ModelExporter me = new ModelExporter(mounts, 0, false, module);
            JSONObject result = me.getResult();
            String json = result.toJSONString();
            gl.log("Number of Elements: " + me.getNumberOfElements());
            if (ExportUtility.send(url + "/workspaces/master/sites/" + siteName + "/elements?background=true", json) != null)
                gl.log("You'll receive an email when the module has finished loading.");
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

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        ProgressStatusRunner.runWithProgressStatus(new ModuleExportRunner(), "Exporting Module", true, 0);
    }
}

