package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.ScriptRunner;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

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
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}


}
