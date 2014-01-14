package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.JsonRequestEntity;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ExportDoc extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element element;
    private GUILog gl = Application.getInstance().getGUILog();
    
    public ExportDoc(Element e) {
        super("ExportDoc", "Export doc", null, null);
        this.element = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject send = new JSONObject();
        JSONArray infos = new JSONArray();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            JSONObject info = new JSONObject();
            info.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(e)));
            info.put("id", e.getID());
            infos.add(info);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        JSONObject info = new JSONObject();
        JSONArray elements = new JSONArray();
        JSONObject send = new JSONObject();
        info.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(element)));
        info.put("id", element.getID());
        elements.add(info);
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
