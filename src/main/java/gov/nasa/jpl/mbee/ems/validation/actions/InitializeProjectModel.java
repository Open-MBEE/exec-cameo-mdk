package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ModelExportRunner;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class InitializeProjectModel extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
   
    public InitializeProjectModel() {
        super("InitializeProjectModel", "Initialize Project and Model", null, null);
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    
    @Override
    public void execute(Collection<Annotation> annos) {
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        JSONObject result = new JSONObject();
        result.put("name", Application.getInstance().getProject().getName());
        String json = result.toJSONString();
        //gl.log(json);
        String url = ExportUtility.getUrl();
        if (url == null) {
            return;
        }
        url += "/javawebscripts/sites/europa/projects/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
        if (!ExportUtility.send(url, json))
            return;
        ProgressStatusRunner.runWithProgressStatus(new ModelExportRunner(Application.getInstance().getProject().getModel(), 0, false), "Exporting Model", true, 0);
    }
}

