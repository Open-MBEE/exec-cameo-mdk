package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.systemsreasoner.validation.SRValidationSuite;

import java.awt.event.ActionEvent;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;

public class ValidateAction extends SRAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String actionid = "Validate";
	public List<Classifier> classes;
	
	public ValidateAction(Classifier clazz) {
		this(Utils2.newList(clazz));
	}
	
	public ValidateAction(List<Classifier> classes) {
		super(actionid);
		this.classes = classes;
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		final SRValidationSuite svs = new SRValidationSuite(classes);
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
	
}
