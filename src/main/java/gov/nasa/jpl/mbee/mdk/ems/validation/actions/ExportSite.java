package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExportSite extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Package element;

    public ExportSite(Package e) {
        //JJS--MDEV-567 fix: changed 'Export' to 'Commit'
        //
        super("ExportSite", "Commit site", null, null);
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
            if (e instanceof Package) {
                set.add(e);
                JSONObject elementOb = ExportUtility.fillId(e, null);
                ExportUtility.fillPackage((Package) e, elementOb);
                infos.add(elementOb);
            }
        }
        commit(infos, "Site");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.okToExport(element)) {
            return;
        }
        JSONArray elements = new JSONArray();
        JSONObject elementOb = ExportUtility.fillId(element, null);
        ExportUtility.fillPackage(element, elementOb);
        elements.add(elementOb);
        commit(elements, "Site");
    }
}
