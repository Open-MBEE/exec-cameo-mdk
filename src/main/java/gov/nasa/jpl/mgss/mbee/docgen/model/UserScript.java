package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.ScriptRunner;
import gov.nasa.jpl.mgss.mbee.docgen.DgvalidationDBSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class UserScript extends Query {

	public String getStereotypeName() {
		Element e = this.dgElement;
		if (!StereotypesHelper.hasStereotypeOrDerived(this.dgElement, DocGen3Profile.userScriptStereotype)) {
			if (this.dgElement instanceof CallBehaviorAction && ((CallBehaviorAction)this.dgElement).getBehavior() != null && 
					StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)this.dgElement).getBehavior(), DocGen3Profile.userScriptStereotype))
				e = ((CallBehaviorAction)this.dgElement).getBehavior();
		} 
		Stereotype s = StereotypesHelper.checkForDerivedStereotype(e, DocGen3Profile.userScriptStereotype);
		return s.getName();
	}
	
	public Map<?,?> getScriptOutput(Map<String, Object> inputs) {
		try {
			Map<String, Object> inputs2 = new HashMap<String, Object>();
			if (this.targets != null)
				inputs2.put("DocGenTargets", this.targets);
			else
				inputs2.put("DocGenTargets", new ArrayList<Element>());
			if (inputs != null)
				inputs2.putAll(inputs);
			if (!inputs2.containsKey("md_install_dir"))
				inputs2.put("md_install_dir", ApplicationEnvironment.getInstallRoot());
			if (!inputs2.containsKey("docgen_output_dir"))
				inputs2.put("docgen_output_dir", ApplicationEnvironment.getInstallRoot());
			if (!inputs2.containsKey("ForViewEditor"))
				inputs2.put("ForViewEditor", false);
			Element e = this.dgElement;
			if (!StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.userScriptStereotype))
				if (e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null && StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)e).getBehavior(), DocGen3Profile.userScriptStereotype))
					e = ((CallBehaviorAction)e).getBehavior();
			Object o = ScriptRunner.runScriptFromStereotype(e, StereotypesHelper.checkForDerivedStereotype(e, DocGen3Profile.userScriptStereotype), inputs2);
			if (o != null && o instanceof Map)
				return (Map<?, ?>)o;
	
								
		} catch (ScriptException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Application.getInstance().getGUILog().log(sw.toString()); // stack trace as a string
		}
		return null;
	}
	
	@Override
	public void visit(boolean forViewEditor, DBHasContent parent, String outputDir) {
		if (getIgnore())
			return;
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("FixMode", "FixNone");
		inputs.put("ForViewEditor", forViewEditor);
		inputs.put("DocGenTitles", getTitles());
		if (outputDir != null)
			inputs.put("docgen_output_dir", outputDir);
		inputs.put("md_install_dir", ApplicationEnvironment.getInstallRoot());
		Map<?,?> o = getScriptOutput(inputs);
		if (o != null && o.containsKey("DocGenOutput")) {
			Object l = o.get("DocGenOutput");
			if (l instanceof List) {
				for (Object oo: (List<?>)l) {
					if (oo instanceof DocumentElement)
						parent.addElement((DocumentElement)oo);
				}
			}
		}
		if (o != null && o.containsKey("docgenOutput")) {
			Object result = o.get("docgenOutput");
			if (result instanceof List) {
				for (Object res: (List<?>)result) {
					if (res instanceof NamedElement) {
						parent.addElement(new DBText(((NamedElement)res).getName())); 
					} else if (res instanceof ViewElement) {
						parent.addElement(DocGenUtils.ecoreTranslateView((ViewElement)res, forViewEditor));
					}
				}
			} 
		}
		if (o != null && o.containsKey("DocGenValidationOutput")) {
			Object l = o.get("DocGenValidationOutput");
			if (l instanceof List) {
				for (Object oo: (List<?>)l) {
					if (oo instanceof ValidationSuite)
						parent.addElements(((ValidationSuite)oo).getDocBook());
				}
			}
		}
		if (o != null && o.containsKey("docgenValidationOutput")) {
			Object l = o.get("docgenValidationOutput");
			if (l instanceof List) {
				DgvalidationDBSwitch s = new DgvalidationDBSwitch();
				for (Object object: (List<?>)l) {
					if (object instanceof Suite)
						parent.addElements(((ValidationSuite)s.doSwitch((Suite)object)).getDocBook());
				}
			}
		}
	}
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}


}
