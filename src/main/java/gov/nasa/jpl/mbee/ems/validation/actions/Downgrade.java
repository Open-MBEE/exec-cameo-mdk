package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


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
    public void execute(Collection<Annotation> annos) {
        JSONObject send = new JSONObject();
        JSONArray infos = new JSONArray();
        Set<Element> set = new HashSet<Element>();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            set.add(e);
            infos.add(ExportUtility.fillDoc(e, null));
        }
        if (!ExportUtility.okToExport(set))
            return;
        send.put("elements", infos);
        send.put("source", "magicdraw");
        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        Application.getInstance().getGUILog().log("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, send.toJSONString(), annos.size()));
        /*if (ExportUtility.send(url, send.toJSONString()) != null) {
            this.removeViolationsAndUpdateWindow(annos);
        }*/
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        //if (!ExportUtility.okToExport(element))
        //    return;
        JSONArray elements = new JSONArray();
        JSONObject send = new JSONObject();
        JSONObject spec = (JSONObject)web.get("specialization");
        if (spec != null && spec.containsKey("type"))
            spec.put("type", "View");
        spec.remove("view2view");
        web.remove("read");
        elements.add(web);
        send.put("elements", elements);
        send.put("source", "magicdraw");

        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        Application.getInstance().getGUILog().log("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, send.toJSONString()));
        /*if (ExportUtility.send(url, send.toJSONString()) != null) {
            this.removeViolationsAndUpdateWindow(annos);
        }*/
    }
}

