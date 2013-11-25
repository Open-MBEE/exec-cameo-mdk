package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.model.PropertiesTableByAttributes;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

/**
 * docgen 3 version
 * 
 * @author dlam
 * 
 */
@SuppressWarnings("serial")
public class EditPropertiesTableAction extends MDAction {

    private PropertiesTableByAttributes npt;

    public EditPropertiesTableAction(PropertiesTableByAttributes table) {
        super(null, "Edit Properties Table", null, null);
        npt = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();

        try {
            EditableTable pt = npt.getEditableTable();
            pt.showTable();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
        }
    }
}
