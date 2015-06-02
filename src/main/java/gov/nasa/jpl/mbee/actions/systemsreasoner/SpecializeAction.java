package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ValidateModelRunner;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.systemsreasoner.validation.SpecializeValidationSuite;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class SpecializeAction extends MDAction {
	
	public static final String actionid = "Specialize";
	public Class clazz;
	
	public SpecializeAction(Class clazz) {
        super(actionid, actionid, null, null);
        this.clazz = clazz;
    }

	@Override
    public void actionPerformed(ActionEvent e) {
		final SpecializeValidationSuite svs = new SpecializeValidationSuite(clazz);
		svs.run();
		Utils.displayValidationWindow(svs, "Systems Reasoner Validation");
		/*for (final Generalization g : clazz.getGeneralization()) {
			if (g.getGeneral() != null)
				System.out.println(g.getGeneral());
			if (g.getGeneral() instanceof Class) {
				System.out.println(((Class) g.getGeneral()).getName());
			}
		}*/
		
		//ProgressStatusRunner.runWithProgressStatus(new ValidateModelRunner(start), "Validating Model", true, 0);
		
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
