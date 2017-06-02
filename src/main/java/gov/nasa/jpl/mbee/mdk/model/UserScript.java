package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBText;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.docgen.validation.Suite;
import gov.nasa.jpl.mbee.mdk.docgen.view.ViewElement;
import gov.nasa.jpl.mbee.mdk.util.ScriptRunner;
import gov.nasa.jpl.mbee.mdk.model.actions.RunUserEditableTableAction;
import gov.nasa.jpl.mbee.mdk.model.actions.RunUserScriptAction;
import gov.nasa.jpl.mbee.mdk.model.actions.RunUserValidationScriptAction;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserScript extends Query {

    public String getStereotypeName() {
        Element e = this.dgElement;
        if (!StereotypesHelper.hasStereotypeOrDerived(this.dgElement, DocGenProfile.userScriptStereotype)) {
            if (this.dgElement instanceof CallBehaviorAction
                    && ((CallBehaviorAction) this.dgElement).getBehavior() != null
                    && StereotypesHelper.hasStereotypeOrDerived(
                    ((CallBehaviorAction) this.dgElement).getBehavior(),
                    DocGenProfile.userScriptStereotype)) {
                e = ((CallBehaviorAction) this.dgElement).getBehavior();
            }
        }
        Stereotype s = StereotypesHelper.checkForDerivedStereotype(e, DocGenProfile.userScriptStereotype);
        return s.getName();
    }

    public Map<?, ?> getScriptOutput(Map<String, Object> inputs) {
        try {
            Map<String, Object> inputs2 = new HashMap<String, Object>();
            if (this.targets != null) {
                inputs2.put("DocGenTargets", this.targets);
            }
            else {
                inputs2.put("DocGenTargets", new ArrayList<Element>());
            }
            if (inputs != null) {
                inputs2.putAll(inputs);
            }
            if (!inputs2.containsKey("md_install_dir")) {
                inputs2.put("md_install_dir", ApplicationEnvironment.getInstallRoot());
            }
            if (!inputs2.containsKey("docgen_output_dir")) {
                inputs2.put("docgen_output_dir", ApplicationEnvironment.getInstallRoot());
            }
            if (!inputs2.containsKey("ForViewEditor")) {
                inputs2.put("ForViewEditor", false);
            }
            Element e = this.dgElement;
            if (!StereotypesHelper.hasStereotypeOrDerived(e, DocGenProfile.userScriptStereotype)) {
                if (e instanceof CallBehaviorAction
                        && ((CallBehaviorAction) e).getBehavior() != null
                        && StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction) e).getBehavior(),
                        DocGenProfile.userScriptStereotype)) {
                    e = ((CallBehaviorAction) e).getBehavior();
                }
            }
            Object o = ScriptRunner.runScriptFromStereotype(e,
                    StereotypesHelper.checkForDerivedStereotype(e, DocGenProfile.userScriptStereotype),
                    inputs2);
            if (o != null && o instanceof Map) {
                return (Map<?, ?>) o;
            }

        } catch (ScriptException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Application.getInstance().getGUILog().log(sw.toString());
        }
        return null;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore()) {
            return res;
        }
        Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("FixMode", "FixNone");
        inputs.put("ForViewEditor", forViewEditor);
        inputs.put("DocGenTitles", getTitles());
        if (outputDir != null) {
            inputs.put("docgen_output_dir", outputDir);
        }
        inputs.put("md_install_dir", ApplicationEnvironment.getInstallRoot());
        Map<?, ?> o = getScriptOutput(inputs);
        if (o != null && o.containsKey("DocGenOutput")) {
            Object l = o.get("DocGenOutput");
            if (l instanceof List) {
                for (Object oo : (List<?>) l) {
                    if (oo instanceof DocumentElement) {
                        res.add((DocumentElement) oo);
                    }
                }
            }
        }
        if (o != null && o.containsKey("docgenOutput")) {
            Object result = o.get("docgenOutput");
            if (result instanceof List) {
                for (Object r : (List<?>) result) {
                    if (r instanceof NamedElement) {
                        res.add(new DBText(((NamedElement) r).getName()));
                    }
                    else if (r instanceof ViewElement) {
                        res.add(DocGenUtils.ecoreTranslateView((ViewElement) r, forViewEditor));
                    }
                }
            }
        }
        if (o != null && o.containsKey("DocGenValidationOutput")) {
            Object l = o.get("DocGenValidationOutput");
            if (l instanceof List) {
                for (Object oo : (List<?>) l) {
                    if (oo instanceof ValidationSuite) {
                        res.addAll(((ValidationSuite) oo).getDocBook());
                    }
                }
            }
        }
        if (o != null && o.containsKey("docgenValidationOutput")) {
            Object l = o.get("docgenValidationOutput");
            if (l instanceof List) {
                DocGenValidationDBSwitch s = new DocGenValidationDBSwitch();
                for (Object object : (List<?>) l) {
                    if (object instanceof Suite) {
                        res.addAll(((ValidationSuite) s.doSwitch((Suite) object)).getDocBook());
                    }
                }
            }
        }
        return res;
    }

    @Override
    public List<MDAction> getActions() {
        List<MDAction> res = new ArrayList<MDAction>();
        Element action = getDgElement();
        boolean added = false;
        if (StereotypesHelper.hasStereotypeOrDerived(action, DocGenProfile.editableTableStereotype)
                || ((action instanceof CallBehaviorAction)
                && ((CallBehaviorAction) action).getBehavior() != null && StereotypesHelper
                .hasStereotypeOrDerived(((CallBehaviorAction) action).getBehavior(),
                        DocGenProfile.editableTableStereotype))) {
            res.add(new RunUserEditableTableAction(this));
            added = true;
        }
        if (StereotypesHelper.hasStereotypeOrDerived(action, DocGenProfile.validationScriptStereotype)
                || ((action instanceof CallBehaviorAction)
                && ((CallBehaviorAction) action).getBehavior() != null && StereotypesHelper
                .hasStereotypeOrDerived(((CallBehaviorAction) action).getBehavior(),
                        DocGenProfile.validationScriptStereotype))) {
            res.add(new RunUserValidationScriptAction(this));
            added = true;
        }
        if (!added) {
            res.add(new RunUserScriptAction(this));
        }
        return res;
    }

}
