package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * this should collect all the queries that can result in some context menu being displayed when user right clicks on the view
 * userscripts, editable tables, etc
 * @author dlam
 *
 */
public class CollectActionsVisitor extends AbstractModelVisitor {

	private List<UserScript> userEditableTables;
	private List<UserScript> userScripts;
	private List<UserScript> userValidationScripts;
	private List<WorkpackageTable> workpackageTables;
	private List<PropertiesTableByAttributes> propertiesTables;
	private List<MissionMapping> missionmappings;
	private List<LibraryMapping> libraryMappings;
	
	public CollectActionsVisitor() {
		userEditableTables = new ArrayList<UserScript>();
		userScripts = new ArrayList<UserScript>();
		userValidationScripts = new ArrayList<UserScript>();
		workpackageTables = new ArrayList<WorkpackageTable>();
		propertiesTables = new ArrayList<PropertiesTableByAttributes>();
		missionmappings = new ArrayList<MissionMapping>();
		libraryMappings = new ArrayList<LibraryMapping>();
	}
	
	@Override
	public void visit(BillOfMaterialsTable bom) {
		workpackageTables.add(bom);
	}
	
	@Override
	public void visit(WorkpackageAssemblyTable wpa) {
		workpackageTables.add(wpa);
	}
	
	@Override
	public void visit(DeploymentTable dt) {
		workpackageTables.add(dt);
	}
	
	@Override
	public void visit(UserScript us) {
		Element action = us.getDgElement();
		userScripts.add(us);
		if (StereotypesHelper.hasStereotypeOrDerived(action, DocGen3Profile.editableTableStereotype) || 
				((action instanceof CallBehaviorAction) && ((CallBehaviorAction)action).getBehavior() != null && 
				StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)action).getBehavior(), DocGen3Profile.editableTableStereotype))) {
			userEditableTables.add(us);
		}
		if (StereotypesHelper.hasStereotypeOrDerived(action, DocGen3Profile.validationScriptStereotype) || 
				((action instanceof CallBehaviorAction) && ((CallBehaviorAction)action).getBehavior() != null && 
				StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)action).getBehavior(), DocGen3Profile.validationScriptStereotype))) {
			userValidationScripts.add(us);
		}
	}
	
	@Override
	public void visit(PropertiesTableByAttributes p) {
		propertiesTables.add(p);
	}

	public List<UserScript> getUserEditableTables() {
		return userEditableTables;
	}

	public List<UserScript> getUserScripts() {
		return userScripts;
	}

	public List<UserScript> getUserValidationScripts() {
		return userValidationScripts;
	}

	public List<WorkpackageTable> getWorkpackageTables() {
		return workpackageTables;
	}

	public List<PropertiesTableByAttributes> getPropertiesTables() {
		return propertiesTables;
	}

	public List<MissionMapping> getMissionMappings() {
		return missionmappings;
	}
	
	public List<LibraryMapping> getLibraryMappings() {
		return libraryMappings;
	}
	
	@Override
	public void visit(MissionMapping cm) {
		missionmappings.add(cm);
	}
	
	@Override
	public void visit(LibraryMapping lm) {
		libraryMappings.add(lm);
	}
	
}
