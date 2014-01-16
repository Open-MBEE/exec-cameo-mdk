package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ExportElementComments extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element element;
    private GUILog gl = Application.getInstance().getGUILog();
    
    public ExportElementComments(Element e) {
        super("ExportElementComments", "Export element comments", null, null);
        this.element = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject send = new JSONObject();
        JSONArray infos = new JSONArray();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            for (Comment c: e.get_commentOfAnnotatedElement()) {
                if (ExportUtility.isElementDocumentation(c))
                    continue;
                JSONObject info = new JSONObject();
                ExportUtility.fillElement(c, info, null, null);
                infos.add(info);
            }
        }
        send.put("elements", infos);
        //gl.log(send.toJSONString());
        String url = ViewEditUtils.getUrl(false);
        if (url == null) {
            return;
        }
        url += ExportUtility.getPostElementsUrl("europa");
        if (ExportUtility.send(url, send.toJSONString())) {
            this.removeViolationsAndUpdateWindow(annos);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        JSONArray elements = new JSONArray();
        JSONObject send = new JSONObject();
        for (Comment c: element.get_commentOfAnnotatedElement()) {
            if (ExportUtility.isElementDocumentation(c))
                continue;
            JSONObject info = new JSONObject();
            ExportUtility.fillElement(c, info, null, null);
            elements.add(info);
        }
        send.put("elements", elements);
        //gl.log(send.toJSONString());

        String url = ViewEditUtils.getUrl(false);
        if (url == null) {
            return;
        }
       
        url += ExportUtility.getPostElementsUrl("europa");
        if (ExportUtility.send(url, send.toJSONString())) {
            this.removeViolationAndUpdateWindow();
        }
    }
}
