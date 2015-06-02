package gov.nasa.jpl.mbee.actions.systemsreasoner;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class SpecializeAction extends SRAction {
	
	public static final String actionid = "Specialize";
	public Element element;
	
<<<<<<< HEAD
	public SpecializeAction(Element element) {
        super(actionid, actionid, null, ActionsGroups.APPLICATION_RELATED);
        this.element = element;
=======
	public SpecializeAction(Class clazz) {
        super(actionid);
        this.clazz = clazz;
>>>>>>> 0f4609b... made a super class to handle the enabling and disabling
    }

	@Override
    public void actionPerformed(ActionEvent e) {
		if (!(element instanceof Class)) {
			return;
		}
		
		final Class clazz = (Class) element;
		//clazz.
		
        /*if (!ExportUtility.checkBaseline()) {    
            return;
        }
        ProgressStatusRunner.runWithProgressStatus(new ValidateModelRunner(start), "Validating Model", true, 0);*/
    }
	
}
