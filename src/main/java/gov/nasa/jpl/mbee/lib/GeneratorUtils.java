package gov.nasa.jpl.mbee.lib;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class GeneratorUtils {
	
	public static Element findStereotypedRelationship(Element e, String s) {
		Stereotype stereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), s);
		List<Stereotype> ss = new ArrayList<Stereotype>();
		ss.add(stereotype);
		List<Element> es = Utils.collectDirectedRelatedElementsByRelationshipStereotypes(e, ss, 1, true, 1);
		if (es.size() > 0) {
			return es.get(0);
		}
		return null;
	}
	
	public static InitialNode findInitialNode(Element a) {
		for (Element e: a.getOwnedElement())
			if (e instanceof InitialNode)
				return (InitialNode)e;
		return null;
	}
	
	public static Object getObjectProperty(Element e, String stereotype, String property, Object defaultt) {
		Object value = StereotypesHelper.getStereotypePropertyFirst(e, stereotype, property);
		if (value == null && e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
			value = StereotypesHelper.getStereotypePropertyFirst(((CallBehaviorAction)e).getBehavior(), stereotype, property);
		}
		if (value == null)
			value = defaultt;
		return value;
	}
	
	public static List<? extends Object> getListProperty(Element e, String stereotype, String property, List<? extends Object> defaultt) {
		List<? extends Object> value = StereotypesHelper.getStereotypePropertyValue(e, stereotype, property);
		if ((value == null || value.isEmpty()) && e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
			value = StereotypesHelper.getStereotypePropertyValue(((CallBehaviorAction)e).getBehavior(), stereotype, property);
		}
		if (value == null || value.isEmpty())
			value = defaultt;
		return value;
	}
	
}
