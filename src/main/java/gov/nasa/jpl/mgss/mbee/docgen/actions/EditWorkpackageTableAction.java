package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.model.BillOfMaterialsTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.DeploymentTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.WorkpackageAssemblyTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.WorkpackageTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

@SuppressWarnings("serial")
public class EditWorkpackageTableAction extends MDAction {
	private WorkpackageTable cba;
	
	public EditWorkpackageTableAction(WorkpackageTable e, int i) {
		super("wptable" + i, "Edit Deployment Table " + i, null, null);
		if (e instanceof DeploymentTable)
			this.setName("Edit Deployment Table");
		if (e instanceof WorkpackageAssemblyTable)
			this.setName("Edit Workpackage Assembly Table");
		if (e instanceof BillOfMaterialsTable)
			this.setName("Edit Bill of Materials Table");	 
		cba = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		try {
			EditableTable et = null;
			if (cba instanceof DeploymentTable) {
				et = ((DeploymentTable)cba).getEditableTable();
			} else if (cba instanceof WorkpackageAssemblyTable) {
				et = ((WorkpackageAssemblyTable)cba).getEditableTable();
			} else if (cba instanceof BillOfMaterialsTable) {
				et = ((BillOfMaterialsTable)cba).getEditableTable();
			}
			if (et != null)
				et.showTable();
			else
				gl.log("Cannot find project or workpackage");
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			gl.log(sw.toString()); // stack trace as a string
		}
	}  
}
