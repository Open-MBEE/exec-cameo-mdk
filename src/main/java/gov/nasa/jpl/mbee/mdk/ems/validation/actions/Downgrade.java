package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;

@Deprecated 
//TODO possible removal @donbot
public class Downgrade extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element element;
    private JSONObject web;

    public Downgrade(Element e, JSONObject web) {            //
        super("Downgrade", "Remove product aspect from server element", null, null);
        this.element = e;
        this.web = web;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        JSONArray elements = new JSONArray();
        if (web != null && web.containsKey("type")) {
            web.put("type", "View");
        }
        web.remove("view2view");
        web.remove("read");
        elements.add(web);
        commit(elements, "Product Downgrade");
    }

    @Override
    public void execute(Collection<Annotation> arg0) {

    }
}

