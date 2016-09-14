package gov.nasa.jpl.mbee.ems.validation;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SiteCharValidator {
    private ValidationSuite suite = new ValidationSuite("site char");
    private ValidationRule siteDiff = new ValidationRule("Site", "site existence", ViolationSeverity.ERROR);

    public void validate(ProgressStatus ps) {
        suite.addValidationRule(siteDiff);
        Set<Element> packages = new HashSet<Element>();
        Model m = Application.getInstance().getProject().getModel();
        getPackages(packages, m);
        packages.remove(m);
        JSONObject web = null;
        try {
            web = ModelValidator.getManyAlfrescoElements(packages, ps);
        } catch (ServerException ex) {
            return;
        }
        Map<String, JSONObject> elementsKeyed = new HashMap<String, JSONObject>();
        ModelValidator.updateElementsKeyed(web, elementsKeyed);
        for (Element e : packages) {
            if (elementsKeyed.containsKey(e.getID())) {
                JSONObject webO = elementsKeyed.get(e.getID());
                ValidationRuleViolation vrv = ModelValidator.siteDiff((Package) e, webO);
                if (vrv != null) {
                    siteDiff.addViolation(vrv);
                }
            }
        }
    }

    public ValidationSuite getSuite() {
        return suite;
    }

    private void getPackages(Set<Element> set, Element e) {
        if (ProjectUtilities.isElementInAttachedProject(e)) {
            return;
        }
        if (e instanceof Package) {
            set.add(e);
            for (Element ee : e.getOwnedElement()) {
                getPackages(set, ee);
            }
        }
    }
}
