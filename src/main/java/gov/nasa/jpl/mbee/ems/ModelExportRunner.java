package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ModelExportRunner implements RunnableWithProgress {

    private Element start;
    private int depth;
    private boolean packageOnly;
    
    public ModelExportRunner(Element start, int depth, boolean packageOnly) {
        this.start = start;
        this.depth = depth;
        this.packageOnly = packageOnly;
    }
    
    @Override
    public void run(ProgressStatus arg0) {
        ModelExporter me;
        GUILog gl = Application.getInstance().getGUILog();
        if (start instanceof Model) {
            me = new ModelExporter(Application.getInstance().getProject(), depth, packageOnly);
        } else {
            List<Element> root = new ArrayList<Element>();
            root.add(start);
            me = new ModelExporter(root, depth, packageOnly);
        }
        JSONObject result = me.getResult();
        String json = result.toJSONString();

        gl.log(json);
        gl.log("Number of Elements: " + me.getNumberOfElements());
        String url = ViewEditUtils.getUrl(false);
        if (url == null) {
            return;
        }
        url += "/javawebscripts/sites/europa/projects/" + Application.getInstance().getProject().getPrimaryProject().getProjectID() + "/elements";
       // gl.log("*** Starting export view comments ***");
        ExportUtility.send(url, json);
        
    }

}
