package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ValidateModelRunner;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.systemsreasoner.validation.SpecializeValidationSuite;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.ComponentRealization;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses.AssociationClass;
import com.nomagic.magicdraw.copypaste.CopyPasteManager;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Interaction;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.OpaqueBehavior;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationConstraint;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeConstraint;
import com.nomagic.uml2.ext.magicdraw.interactions.mdfragments.InteractionConstraint;

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
}
