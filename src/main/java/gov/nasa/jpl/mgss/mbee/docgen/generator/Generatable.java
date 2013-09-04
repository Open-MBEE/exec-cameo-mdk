package gov.nasa.jpl.mgss.mbee.docgen.generator;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * <p>Hopefully this provides a unified way of dealing with presentation elements.</p>
 * <p>Should perhaps be renamed "Presentable" or something like that.</p>
 * @author bcompane
 *
 */
public interface Generatable {
	
	public void initialize();
	public void parse();
	public List<DocumentElement> visit(boolean forViewEditor);
	
}
