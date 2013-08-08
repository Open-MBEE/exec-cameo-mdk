package gov.nasa.jpl.mgss.mbee.docgen.generator;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public interface Generatable {
	
	public void initialize(ActivityNode an, List<Element> in);
	public void parse();
	public void visit(boolean forViewEditor);
	
}
