package gov.nasa.jpl.mbee.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExportProperty extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;

    public ExportProperty(Element e) {
        super("ExportProperty", "Commit property", null, null);
        this.element = e;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        JSONArray infos = new JSONArray();
        Set<Element> set = new HashSet<Element>();
        for (Annotation anno : annos) {
            Element e = (Element) anno.getTarget();
            set.add(e);
            JSONObject info = getInfo(e);
            infos.add(info);
        }
        if (!ExportUtility.okToExport(set)) {
            return;
        }
        commit(infos, "Property");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.okToExport(element)) {
            return;
        }
        JSONObject info = getInfo(element);
        JSONArray elements = new JSONArray();
        elements.add(info);
        commit(elements, "Property");
    }

    @SuppressWarnings("unchecked")
    private JSONObject getInfo(Element e) {
        JSONObject elementInfo = new JSONObject();
        ExportUtility.fillPropertySpecialization(e, elementInfo, false, true);
        elementInfo.put("sysmlId", ExportUtility.getElementID(e));
        return elementInfo;
    }
}
