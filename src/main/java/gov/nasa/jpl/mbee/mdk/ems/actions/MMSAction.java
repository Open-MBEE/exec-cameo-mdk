package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.magicdraw.actions.MDAction;
import gov.nasa.jpl.mbee.mdk.viewedit.ViewEditUtils;

import javax.swing.*;

public class MMSAction extends MDAction {

    public MMSAction(String arg0, String arg1, KeyStroke arg2, String arg3) {
        super(arg0, arg1, arg2, arg3);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void updateState() {
        if (ViewEditUtils.isPasswordSet()) {
            setEnabled(true);
        }
        else {
            setEnabled(false);
        }
    }


}
