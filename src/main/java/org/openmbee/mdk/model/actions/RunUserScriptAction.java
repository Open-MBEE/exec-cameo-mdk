package org.openmbee.mdk.model.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.openmbee.mdk.docgen.view.ViewElement;
import org.openmbee.mdk.model.UserScript;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class RunUserScriptAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private UserScript scripti;
    public static final String DEFAULT_ID = "RunUserScript";

    public RunUserScriptAction(UserScript e) {
        super(null, "Run User Script", null, null);
        scripti = e;
        String name = e.getStereotypeName();
        if (name != null) {
            this.setName("Run " + name);
        }
    }

    public RunUserScriptAction(UserScript e, boolean useid) {
        super(DEFAULT_ID, "Run User Script", null, null);
        scripti = e;
        String name = e.getStereotypeName();
        if (name != null) {
            this.setName("Run " + name);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        GUILog log = Application.getInstance().getGUILog();
        Map<?, ?> o = scripti.getScriptOutput(null);
        if (o != null) {
            log.log("output from script: " + o.toString());
            /*
             * for (Object key: o.keySet()) { try { log.log("key: " +
             * key.toString() + " value: " + o.get(key).toString()); } catch
             * (Exception e) {
             *
             * }
             *
             * }
             */
            if (o.containsKey("docgenOutput")) {
                Object result = o.get("docgenOutput");
                if (result instanceof List) {
                    for (Object res : (List<?>) result) {
                        if (res instanceof NamedElement) {
                            log.log(((NamedElement) res).getName());
                        }
                        else if (res instanceof ViewElement) {
                            log.log(res.toString());
                        }
                    }
                }
            }

        }
        else {
            log.log("script has no output!");
        }

    }
}
