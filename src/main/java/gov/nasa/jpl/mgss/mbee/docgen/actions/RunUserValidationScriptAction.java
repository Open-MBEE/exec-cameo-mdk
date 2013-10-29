package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DgvalidationDBSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite;
import gov.nasa.jpl.mgss.mbee.docgen.model.UserScript;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

@SuppressWarnings("serial")
public class RunUserValidationScriptAction extends MDAction {
	private UserScript scripti;
	public static final String actionid = "RunValidationScript";
	public RunUserValidationScriptAction(UserScript us) {
		super(null, "Run Validation Script", null, null);
		scripti = us;
		String name = scripti.getStereotypeName();
		if (name != null)
			this.setName("Run " + name + " Validation");
	}
	
	public RunUserValidationScriptAction(UserScript us, boolean useid) {
        super(actionid, "Run Validation Script", null, null);
        scripti = us;
        String name = scripti.getStereotypeName();
        if (name != null)
            this.setName("Run " + name + " Validation");
    }
	
	@SuppressWarnings("rawtypes")
	public void actionPerformed(ActionEvent event) {
		GUILog log = Application.getInstance().getGUILog();
		/*String fix = "FixNone";
		List<String> fixes = new ArrayList<String>();
		fixes.add("FixSelected");
		fixes.add("FixAll");
		fixes.add("FixNone");
	    fix = Utils.getUserDropdownSelectionForString("Choose Fix Mode", "Choose Fix Mode", fixes, fixes);
		if (fix == null)
			fix = "FixNone";
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("FixMode", fix);
	*/
		Map<String, Object> inputs = new HashMap<String, Object>();
		Map o = scripti.getScriptOutput(inputs);
		if (o != null && o.containsKey("DocGenValidationOutput")) {
			Object l = o.get("DocGenValidationOutput");
			if (l instanceof List) {
				Utils.displayValidationWindow((Collection<ValidationSuite>)l, "User Validation Script Results");
			}
		} else if (o != null && o.containsKey("docgenValidationOutput")) {
			Object l = o.get("docgenValidationOutput");
			if (l instanceof List) {
				DgvalidationDBSwitch s = new DgvalidationDBSwitch();
				List<ValidationSuite> vs = new ArrayList<ValidationSuite>();
				for (Object object: (List)l) {
					if (object instanceof Suite)
						vs.add((ValidationSuite)s.doSwitch((Suite)object));
				}
				Utils.displayValidationWindow(vs, "User Validation Script Results");
			}
		} else
			log.log("script has no validation output!");
		
	}  
}
