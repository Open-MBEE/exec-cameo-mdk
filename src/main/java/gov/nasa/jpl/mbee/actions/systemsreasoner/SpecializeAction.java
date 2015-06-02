package gov.nasa.jpl.mbee.actions.systemsreasoner;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class SpecializeAction extends MDAction {
	
	public static final String actionid = "Specialize";
	public Element element;
	
	public SpecializeAction(Element element) {
        super(actionid, actionid, null, ActionsGroups.APPLICATION_RELATED);
        this.element = element;
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
	
	@Override
	/**
	 * This override gives the SRConfigurator enable/disable control over each individual action
	 * Otherwise, this action would not be able to be enabled or disabled once set
	 */
	public void updateState() {
		if (this.isEnabled())
			this.setEnabled(false);
		else
			this.setEnabled(true);
	}
}
