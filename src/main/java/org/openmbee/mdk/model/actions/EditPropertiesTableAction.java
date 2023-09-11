package org.openmbee.mdk.model.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import org.openmbee.mdk.docgen.table.EditableTable;
import org.openmbee.mdk.model.PropertiesTableByAttributes;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * docgen 3 version
 *
 * @author dlam
 */
public class EditPropertiesTableAction extends MDAction {

    private static final long serialVersionUID = 1L;
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
