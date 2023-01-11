package gov.nasa.jpl.mbee.mdk.model.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import gov.nasa.jpl.mbee.mdk.docgen.validation.Suite;
import gov.nasa.jpl.mbee.mdk.model.DocGenValidationDBSwitch;
import gov.nasa.jpl.mbee.mdk.model.UserScript;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunUserValidationScriptAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private UserScript scripti;
    public static final String DEFAULT_ID = "RunValidationScript";

    public RunUserValidationScriptAction(UserScript us) {
        super(null, "Run Validation Script", null, null);
        scripti = us;
        String name = scripti.getStereotypeName();
        if (name != null) {
            this.setName("Run " + name + " Validation");
        }
    }

    public RunUserValidationScriptAction(UserScript us, boolean useid) {
        super(DEFAULT_ID, "Run Validation Script", null, null);
        scripti = us;
        String name = scripti.getStereotypeName();
        if (name != null) {
            this.setName("Run " + name + " Validation");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent event) {
        GUILog log = Application.getInstance().getGUILog();
        /*
         * String fix = "FixNone"; List<String> fixes = new ArrayList<String>();
         * fixes.add("FixSelected"); fixes.add("FixAll"); fixes.add("FixNone");
         * fix = Utils.getUserDropdownSelectionForString("Choose Fix Mode",
         * "Choose Fix Mode", fixes, fixes); if (fix == null) fix = "FixNone";
         * Map<String, Object> inputs = new HashMap<String, Object>();
         * inputs.put("FixMode", fix);
         */
        Map<String, Object> inputs = new HashMap<>();
        Map<?, ?> o = scripti.getScriptOutput(inputs);
        if (o != null && o.containsKey("DocGenValidationOutput")) {
            Object l = o.get("DocGenValidationOutput");
            if (l instanceof List) {
                Utils.displayValidationWindow(Application.getInstance().getProject(), (List<ValidationSuite>) l, "User Validation Script Results");
            }
        }
        else if (o != null && o.containsKey("docgenValidationOutput")) {
            Object l = o.get("docgenValidationOutput");
            if (l instanceof List) {
                DocGenValidationDBSwitch s = new DocGenValidationDBSwitch();
                List<ValidationSuite> vs = new ArrayList<ValidationSuite>();
                for (Object object : (List<?>) l) {
                    if (object instanceof Suite) {
                        vs.add((ValidationSuite) s.doSwitch((Suite) object));
                    }
                }
                Utils.displayValidationWindow(Application.getInstance().getProject(), vs, "User Validation Script Results");
            }
        }
        else {
            log.log("script has no validation output!");
        }

    }
}
