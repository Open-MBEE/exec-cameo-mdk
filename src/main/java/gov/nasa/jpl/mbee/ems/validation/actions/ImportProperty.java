package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;

public class ImportProperty extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    private JSONObject result;
    private JSONObject spec;
    
    public ImportProperty(Element e, JSONObject result, JSONObject spec) {
        //JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        //
        super("ImportProperty", "Accept property", null, null);
        this.element = e;
        this.result = result;
        this.spec = spec;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        executeMany(annos, "Change Properties");
    }
    
    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            Element e = (Element)anno.getTarget();
            if (!e.isEditable()) {
                Utils.guilog("[ERROR] " + e.get_representationText() + " isn't editable");
                return false;
            }
            Map<String, JSONObject> map = (Map<String, JSONObject>)result.get("elementsKeyed");
            JSONObject ptype = (JSONObject)((JSONObject)map.get(e.getID())).get("specialization");
            if (e instanceof Property && ptype.containsKey("propertyType")) {
                ImportUtility.setProperty((Property)e, ptype);
            } 
        } else {
            if (element instanceof Property) {
                ImportUtility.setProperty((Property)element, spec);
            }
        }
        return true;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!element.isEditable()) {
            Utils.guilog("[ERROR] " + element.getHumanName() + " is not editable!");
            return;
        }
        execute("Change Property");
    }
}
