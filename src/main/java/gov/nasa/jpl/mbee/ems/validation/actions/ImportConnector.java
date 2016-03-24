package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ImportException;
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;

public class ImportConnector extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Connector element;
    private JSONObject spec;
    private JSONObject result;
    public ImportConnector(Connector e, JSONObject spec, JSONObject result) {
        //JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        //
        super("ImportConnector", "Accept connector", null, null);
        this.element = e;
        this.spec = spec;
        this.result = result;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        executeMany(annos, "Change Connectors");
    }
    
    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            Element e = (Element)anno.getTarget();
            String name = e.get_representationText();
            if (e instanceof NamedElement)
                name = ((NamedElement)e).getQualifiedName();
            if (!e.isEditable()) {
                Utils.guilog("[ERROR] " + name + " isn't editable");
                return false;
            }
            JSONObject resultOb = (JSONObject)((Map<String, JSONObject>)result.get("elementsKeyed")).get(e.getID());
            try {
                ImportUtility.setConnectorEnds((Connector)e, (JSONObject)resultOb.get("specialization"));
            } catch (ImportException ex) {
                Utils.guilog("[ERROR] " + name + " cannot be imported because it would create a model inconsistency due to one or both roles missing.");
            }
        } else {
            try {
                ImportUtility.setConnectorEnds(element, spec);
            } catch (ImportException ex) {
                Utils.guilog("[ERROR] " + element.getQualifiedName() + " cannot be imported because it would create a model inconsistency due to one or both roles missing.");
            }
        }
        return true;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!element.isEditable()) {
            Utils.guilog("[ERROR] Element is not editable!");
            return;
        }
        execute("Change Connector");
    }
}
