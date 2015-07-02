package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.systemsreasoner.validation.SRValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateAction extends SRAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String actionid = "Validate";
	public List<? extends Element> elements;
	
	public ValidateAction(Element element) {
		this(Utils2.newList(element));
	}
	
	public ValidateAction(List<? extends Element> elements) {
		super(actionid);
		this.elements = elements;
	}

	@Override
    public void actionPerformed(ActionEvent e) {
		final List<Element> elems = new ArrayList<Element>();
		elems.addAll(elements);
		final SRValidationSuite svs = new SRValidationSuite(elems);
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
