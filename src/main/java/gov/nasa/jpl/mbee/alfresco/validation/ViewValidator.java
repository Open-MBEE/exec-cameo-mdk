package gov.nasa.jpl.mbee.alfresco.validation;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpl.mbee.alfresco.validation.actions.ExportView;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ViewValidator {

    private ValidationSuite suite = new ValidationSuite("View Sync");
    private ValidationRule exists = new ValidationRule("Does Not Exist", "view doesn't exist yet", ViolationSeverity.ERROR);
    private ValidationRule match = new ValidationRule("View content", "view contents have changed", ViolationSeverity.ERROR);
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
        this.recurse = recursive;
    }
    
    public void validate() {
        //check to see if view exists on alfresco, if not, export view, handle recursing?
        boolean exist = false;
        if (!exist) {
            ValidationRuleViolation v = new ValidationRuleViolation(view, "[EXIST] This view doesn't exist on view editor yet");
            v.addAction(new ExportView(view));
            exists.addViolation(v);
        } else {
            //get the elements of the view from alfresco
            //as JSONobject
            //check if the view elements number matches, if not, the view is out of date
            JSONObject results = new JSONObject(); //replace jsonobject with alfresco info
            boolean matches = false;
            if (!matches) {
                ValidationRuleViolation v = new ValidationRuleViolation(view, "[CONTENT] The view editor has an outdated version");
                v.addAction(new ExportView(view));
                match.addViolation(v);
            }
            //check the elements that got returned
            ResultHolder.lastResults = results;
            ModelValidator mv = new ModelValidator(view, results);
            mv.validate();
            modelSuite = mv.getSuite();
        }
    }
    
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        if (modelSuite != null)
            vss.add(modelSuite);
        Utils.displayValidationWindow(vss, "View Web Difference Validation");
    }
}
