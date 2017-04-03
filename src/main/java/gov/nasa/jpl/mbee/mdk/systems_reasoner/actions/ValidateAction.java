package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.lib.Utils2;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.validation.SRValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ValidateAction extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_ID = "Validate";
    public List<? extends Element> elements;

    public ValidateAction(Element element) {
        this(Utils2.newList(element));
    }

    public ValidateAction(List<? extends Element> elements) {
        super(DEFAULT_ID);
        this.elements = elements;
    }


    public static void validate(final List<? extends Element> elements) {
        final List<Element> elems = new ArrayList<>();
        Project project = Project.getProject(elements.iterator().next());
        elems.addAll(elements);
        final SRValidationSuite svs = new SRValidationSuite(elems);
        svs.run();
        Utils.displayValidationWindow(project, svs, "Systems Reasoner Validation");
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
    public void actionPerformed(ActionEvent e) {
        validate(elements);
    }

    public static void validate(InstanceSpecification instance) {
        ArrayList<InstanceSpecification> insts = new ArrayList<InstanceSpecification>();
        insts.add(instance);
        validate(insts);
    }

}
