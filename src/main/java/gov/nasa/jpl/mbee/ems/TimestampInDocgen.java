package gov.nasa.jpl.mbee.ems;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.validation.ResultHolder;
import gov.nasa.jpl.mbee.ems.validation.actions.InitializeProjectModel;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;

public class TimestampInDocgen {
    private JSONObject result;       

	// create Stereotype in Docgen
	// receive List from docgen
	// get time from stereotype
	// query server for timestamp version:  
	//https://fn-cae-ems.jpl.nasa.gov/alfresco/service/workspaces/master/tmt/elements/_18_0_2_baa02e2_1422996003330_165733_91914?timestamp=2015-12-13T16:21:06.797-0700
	// start session
	// create elements
	// ImportUtility.createElement
	// continue docgen
	// before end: delete elements.
	// cancel session. 
	 public boolean checkProject(ProgressStatus ps, List<Element> starts ) {
	        //if (ExportUtility.baselineNotSet)
	        //    baselineTag.addViolation(new ValidationRuleViolation(Project.getProject(start).getModel(), "The baseline tag isn't set, baseline check wasn't done."));
	        String projectUrl = ExportUtility.getUrlForProject();
	        if (projectUrl == null)
	            return false;
	        String globalUrl = ExportUtility.getUrl();
	        globalUrl += "/workspaces/master/elements/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
	        String globalResponse = null;
	        try {
	            globalResponse = ExportUtility.get(globalUrl, false);
	        } catch (ServerException ex) {
	            
	        }
	        String url = ExportUtility.getUrlWithWorkspace();
	        if (url == null)
	            return false;
	        if (globalResponse == null) {
	            ValidationRuleViolation v = null;
	            if (url.contains("master")) {
	                v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The project doesn't exist on the web.");
	                v.addAction(new InitializeProjectModel(false));
	            } else
	                v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The trunk project doesn't exist on the web. Export the trunk first.");
 	            return false;
	        }
	        String response = null;
	        try {
	            response = ExportUtility.get(projectUrl, false);
	        } catch (ServerException ex) {
	            
	        }
	        if (response == null || response.contains("Site node is null") || response.contains("Could not find project")) {//tears
	            
	            ValidationRuleViolation v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The project exists on the server already under a different site.");
	                //v.addAction(new InitializeProjectModel(false));
 	            
	            return false;
	        }
	        for (Element start: starts ) {
	            if (ProjectUtilities.isElementInAttachedProject(start)){
	                Utils.showPopupMessage("You should not validate or export elements not from this project! Open the right project and do it from there");
	                return false;
	            }
	        }
	        JSONArray elements = new JSONArray();
	        for (Element start: starts) {
	            String id = start.getID();
	            if (start == Application.getInstance().getProject().getModel())
	                id = Application.getInstance().getProject().getPrimaryProject().getProjectID();
	            id = id.replace(".", "%2E");
	            final String url2;
 	                url2 = url + "/elements/" + id + "?timestamp=" + "&qualified=false";
	             
	            GUILog log = Application.getInstance().getGUILog();
	            Utils.guilog("[INFO] Getting elements from server...");
	            
	            final AtomicReference<String> res = new AtomicReference<String>();
	            Thread t = new Thread(new Runnable() {
	                @Override
	                public void run() {
	                    String tres = null;
	                    try {
	                        tres = ExportUtility.get(url2, false);
	                    } catch (ServerException ex) {}
	                    res.set(tres);
	                }
	            });
	            t.start();
	            try {
	                t.join(10000);
	                while(t.isAlive()) {
	                    if (ps.isCancel()) {
	                        Utils.guilog("[INFO] Model validation canceled.");
	                        //clean up thread?
	                        return false;
	                    }
	                    t.join(10000);
	                }
	            } catch (Exception e) {
	                
	            }
	            //response = ExportUtility.get(url2, false);
	            response = res.get();
	            Utils.guilog("[INFO] Finished getting elements");
	            if (response == null) {
	                response = "{\"elements\": []}";
	            }
	            JSONObject partialResult = (JSONObject)JSONValue.parse(response);
	            if (partialResult != null && partialResult.containsKey("elements"))
	                elements.addAll((JSONArray)partialResult.get("elements"));
	        }
	        result = new JSONObject();
	        result.put("elements", elements);
	        ResultHolder.lastResults = result;
	        return true;
	    }
	  
}
