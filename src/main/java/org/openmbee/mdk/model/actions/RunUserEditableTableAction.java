package org.openmbee.mdk.model.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import org.openmbee.mdk.docgen.DocGenViewDBSwitch;
import org.openmbee.mdk.docgen.table.EditableTable;
import org.openmbee.mdk.docgen.view.MDEditableTable;
import org.openmbee.mdk.model.UserScript;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class RunUserEditableTableAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private UserScript scripti;

    public RunUserEditableTableAction(UserScript us) {
        super(null, "Run Editable Script", null, null);
        scripti = us;
        String name = scripti.getStereotypeName();
        if (name != null) {
            this.setName("Edit " + name + " Table");
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        GUILog log = Application.getInstance().getGUILog();
        Map<?, ?> o = scripti.getScriptOutput(null);
        if (o != null && o.containsKey("EditableTable")) {
            Object l = o.get("EditableTable");
            if (l instanceof EditableTable) {
                ((EditableTable) l).showTable();
            }
        }
        else if (o != null && o.containsKey("editableTable")) {
            if (o.get("editableTable") instanceof List) {
                for (Object object : (List<?>) o.get("editableTable")) {
                    if (object instanceof MDEditableTable) {
                        DocGenViewDBSwitch.convertEditableTable((MDEditableTable) object).showTable();
                    }
                }
            }
        }
        else {
            log.log("script has no editable table output!");
        }
    }
}
