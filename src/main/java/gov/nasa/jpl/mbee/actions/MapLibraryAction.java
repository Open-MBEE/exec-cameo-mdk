package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.model.LibraryMapping;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;

public class MapLibraryAction extends MDAction {

    private LibraryMapping mapping;

    public MapLibraryAction(LibraryMapping table) {
        super(null, "Map Library", null, null);
        mapping = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (mapping.init()) {
            mapping.showChooser();
        } else
            Application.getInstance().getGUILog().log("Missing imports");
    }
}
