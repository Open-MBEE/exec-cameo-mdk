package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.DgviewDBSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.MDEditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.UserScript;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

@SuppressWarnings("serial")
public class RunUserEditableTableAction extends MDAction {
    private UserScript scripti;

    public RunUserEditableTableAction(UserScript us) {
        super(null, "Run Editable Script", null, null);
        scripti = us;
        String name = scripti.getStereotypeName();
        if (name != null)
            this.setName("Edit " + name + " Table");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        GUILog log = Application.getInstance().getGUILog();
        Map<?, ?> o = scripti.getScriptOutput(null);
        if (o != null && o.containsKey("EditableTable")) {
            Object l = ((Map<?, ?>)o).get("EditableTable");
            if (l instanceof EditableTable) {
                ((EditableTable)l).showTable();
            }
        } else if (o != null && o.containsKey("editableTable")) {
            if (o.get("editableTable") instanceof List) {
                for (Object object: (List<?>)o.get("editableTable")) {
                    if (object instanceof MDEditableTable) {
                        DgviewDBSwitch.convertEditableTable((MDEditableTable)object).showTable();
                    }
                }
            }
        } else
            log.log("script has no editable table output!");
    }
}
