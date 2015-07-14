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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ExportAggregation extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

	private static final long serialVersionUID = 1L;
	private Element element;
	
	public ExportAggregation(Element e) {
		super("ExportAggregation", "Commit Aggregation", null, null);
		this.element = e;
	}

	@Override
	public boolean canExecute(Collection<Annotation> arg0) {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public void commit(JSONArray elements) {
		JSONObject send = new JSONObject();
		send.put("elements", elements);
		send.put("source", "magicdraw");
		
		String url = ExportUtility.getPostElementsUrl();
		if (url == null) {
			return;
		}
		
		Application.getInstance().getGUILog().log("[INFO] Request is added to queue.");
		OutputQueue.getInstance().offer(new Request(url, send.toJSONString(), elements.size()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(Collection<Annotation> annos) {
		Set<Element> set = new HashSet<Element>();
		
		JSONArray elements = new JSONArray();
		for (Annotation anno: annos) {
			Element e = (Element)anno.getTarget();
			set.add(e);
            JSONObject elementOb = ExportUtility.fillId(e, null);
            elementOb.put("specialization", ExportUtility.fillAssociationSpecialization((Association)e, null));
            // is this even right?
            elementOb.put("property", ExportUtility.fillAggregationSpecialization((Association)e, null));
            elements.add(elementOb);
		}
		
		commit(elements);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!ExportUtility.okToExport(element))
			return;
		
		JSONArray elements = new JSONArray();
		
        JSONObject elementOb = ExportUtility.fillId(element, null);
        elementOb.put("specialization", ExportUtility.fillAssociationSpecialization((Association)e, null));
        // is this even right?
        elementOb.put("property", ExportUtility.fillAggregationSpecialization((Association)e, null));
        elements.add(elementOb);
		
		commit(elements);
	}
}
