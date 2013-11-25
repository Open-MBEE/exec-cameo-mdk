package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.model.MissionMapping;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;

public class MapMissionAction extends MDAction {

    private MissionMapping mapping;

    public MapMissionAction(MissionMapping table) {
        super(null, "Map Mission", null, null);
        mapping = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (mapping.init()) {
            // mapping.dump();

            mapping.showChooser();
        } else
            Application.getInstance().getGUILog().log("Missing imports");
    }
}
