package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.dgview.Paragraph;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement;
import gov.nasa.jpl.mgss.mbee.docgen.model.UserScript;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.qvt.oml.ModelExtent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

@SuppressWarnings("serial")
public class RunUserScriptAction extends MDAction {
    private UserScript         scripti;
    public static final String actionid = "RunUserScript";

    public RunUserScriptAction(UserScript e) {
        super(null, "Run User Script", null, null);
        scripti = e;
        String name = e.getStereotypeName();
        if (name != null)
            this.setName("Run " + name);
    }

    public RunUserScriptAction(UserScript e, boolean useid) {
        super(actionid, "Run User Script", null, null);
        scripti = e;
        String name = e.getStereotypeName();
        if (name != null)
            this.setName("Run " + name);
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
                    for (Object res: (List<?>)result) {
                        if (res instanceof NamedElement) {
                            log.log(((NamedElement)res).getName());
                        } else if (res instanceof ViewElement) {
                            log.log(res.toString());
                        }
                    }
                } else if (result instanceof ModelExtent) {
                    for (EObject object: ((ModelExtent)result).getContents()) {
                        if (object instanceof Paragraph) {
                            log.log(((Paragraph)object).getText());
                        }
                    }
                }
            }

        } else
            log.log("script has no output!");

    }
}
