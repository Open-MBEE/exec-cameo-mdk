package gov.nasa.jpl.mbee.alfresco.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.alfresco.ExportUtility;
import gov.nasa.jpl.mbee.alfresco.validation.actions.ExportHierarchy;
import gov.nasa.jpl.mbee.alfresco.validation.actions.ExportView;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ViewValidator {

    private ValidationSuite suite = new ValidationSuite("View Sync");
    private ValidationRule exists = new ValidationRule("Does Not Exist", "view doesn't exist yet", ViolationSeverity.ERROR);
    private ValidationRule match = new ValidationRule("View content", "view contents have changed", ViolationSeverity.ERROR);
    private ValidationRule hierarchy = new ValidationRule("View Hierarchy", "view hierarchy", ViolationSeverity.INFO);
    private ValidationSuite modelSuite;
    private Project prj;
    private Element view;
    private JSONObject result;
    private boolean recurse;
    
    public ViewValidator(Element view, boolean recursive) {
        this.view = view;
        prj = Application.getInstance().getProject();
        suite.addValidationRule(exists);
        suite.addValidationRule(match);
        suite.addValidationRule(hierarchy);
        this.recurse = recursive;
    }
    
    public void validate() {
        DocumentGenerator dg = new DocumentGenerator(view, null, null);
        Document dge = dg.parseDocument(true, recurse);
        (new PostProcessor()).process(dge);
        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null)
            return;
        DBAlfrescoVisitor visitor2 = new DBAlfrescoVisitor(recurse);
        book.accept(visitor2);
        JSONObject results = new JSONObject();
        JSONArray resultElements = new JSONArray();
        results.put("elements", resultElements);
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGen3Profile.documentViewStereotype, "Document Profile");
        String url = ViewEditUtils.getUrl(false);
        if (url == null)
            return;//return; //do some error
        for (Object viewid: visitor2.getViews().keySet()) {
            boolean doc = false;
            Element currentView = (Element)Application.getInstance().getProject().getElementByID((String)viewid);
            if (StereotypesHelper.hasStereotypeOrDerived(currentView, documentView))
                doc = true;
            //check to see if view exists on alfresco, if not, export view?
            String existurl = url + "/javawebscripts/newviews/" + viewid;
            String response = ExportUtility.get(existurl);
            if (response == null || !response.contains("contains")) {
                ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[EXIST] This view doesn't exist on view editor yet");
                v.addAction(new ExportView(currentView, false));
                v.addAction(new ExportView(currentView, true));
                exists.addViolation(v);
            } else {
                String viewElementsUrl = existurl + "/elements";
                JSONArray localElements = (JSONArray)((JSONObject)visitor2.getViews().get(viewid)).get("displayedElements");
                String viewelements = ExportUtility.get(viewElementsUrl);
                if (viewelements == null)
                    continue;
                JSONObject viewresults = (JSONObject)JSONValue.parse(viewelements);
                boolean matches = viewElementsMatch(localElements, viewresults);
                if (!matches) {
                    ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[CONTENT] The view editor has an outdated version");
                    v.addAction(new ExportView(currentView, false));
                    v.addAction(new ExportView(currentView, true));
                    
                    v.addAction(new ExportHierarchy(currentView));
                    match.addViolation(v);
                } else {
                    ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[Hierarchy]");
                    v.addAction(new ExportHierarchy(currentView));
                    hierarchy.addViolation(v);
                }
                resultElements.addAll((JSONArray)viewresults.get("elements")); //need cinyoung's side
            }
        }
        ResultHolder.lastResults = results;
        ModelValidator mv = new ModelValidator(view, results, false);
        mv.validate();
        modelSuite = mv.getSuite();
    }
    
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        if (modelSuite != null)
            vss.add(modelSuite);
        Utils.displayValidationWindow(vss, "View Web Difference Validation");
    }
    
    private boolean viewElementsMatch(JSONArray viewDisplayedElements, JSONObject veResults) {
        Set<String> webElements = new HashSet<String>(viewDisplayedElements);
        Set<String> local = new HashSet<String>();
        for (Object o: (JSONArray)veResults.get("elements")) {
            local.add((String)((JSONObject)o).get("id"));
        }
        if (webElements.containsAll(local) && local.containsAll(webElements))
            return true;
        return false;
    }
}
