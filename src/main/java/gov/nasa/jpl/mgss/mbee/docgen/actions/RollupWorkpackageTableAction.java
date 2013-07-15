package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.model.BillOfMaterialsTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.DeploymentTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.WorkpackageTable;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;

@SuppressWarnings("serial")
public class RollupWorkpackageTableAction extends MDAction {

private WorkpackageTable cba;
	
	public RollupWorkpackageTableAction(WorkpackageTable e, int i) {
		super("wptablerollup" + i, "Rollup Deployment Table " + i, null, null);
		if (e instanceof DeploymentTable)
			this.setName("Rollup Deployment Table");
		if (e instanceof BillOfMaterialsTable)
			this.setName("Rollup Bill of Materials Table");	 
		cba = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		int validate = JOptionPane.showConfirmDialog(null, "Validate Only?", "Validate only?", JOptionPane.YES_NO_OPTION);
		boolean fix = true;
		if (validate == JOptionPane.YES_OPTION)
			fix = false;
		try {
			SessionManager.getInstance().createSession("mass rollup");
			if (cba instanceof DeploymentTable) {
				((DeploymentTable)cba).getRollup(fix);
			} else if (cba instanceof BillOfMaterialsTable) {
				((BillOfMaterialsTable)cba).getRollup(fix);
			}
			gl.log("Done");
			SessionManager.getInstance().closeSession();
		} catch (Exception ex) {
			SessionManager.getInstance().cancelSession();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			gl.log(sw.toString()); // stack trace as a string
		}
	}  
}
