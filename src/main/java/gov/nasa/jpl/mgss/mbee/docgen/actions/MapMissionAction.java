package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.model.MissionMapping;
import gov.nasa.jpl.mgss.mbee.docgen.model.PropertiesTableByAttributes;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

public class MapMissionAction extends MDAction {

	private MissionMapping mapping;
	
	public MapMissionAction(MissionMapping table) {
		super(null, "Map Mission", null, null);
		mapping = table;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (mapping.init()) {
			//mapping.dump();
		
			mapping.showChooser();
		} else
			Application.getInstance().getGUILog().log("Missing imports");
	}  
}
