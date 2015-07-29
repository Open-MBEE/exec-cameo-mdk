package gov.nasa.jpl.mbee.ems.validation.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.KeyStroke;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

public class ExportOwnedAttribute extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

	private static final long serialVersionUID = 1L;
	private Element element;
	
	public ExportOwnedAttribute(Element e) {
		super("ExportOwnedAttribute", "Commit Owned Attributes", null, null);
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
			elements.add(ExportUtility.fillOwnedAttribute(e, null));
		}
		
		commit(elements);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!ExportUtility.okToExport(element))
			return;
		
		JSONArray elements = new JSONArray();
		elements.add(ExportUtility.fillOwnedAttribute(element, null));
		
		commit(elements);
	}
	
}
