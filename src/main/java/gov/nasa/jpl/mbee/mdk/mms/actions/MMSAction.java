package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;

import javax.swing.*;

public class MMSAction extends MDAction {

    private static boolean disabled = false;

    public MMSAction(String arg0, String arg1, KeyStroke arg2, String arg3) {
        super(arg0, arg1, arg2, arg3);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void updateState() {
        setEnabled(TicketUtils.isTicketSet(Application.getInstance().getProject()) && !disabled);
    }

    public static void setDisabled(boolean set) {
        disabled = set;
    }

    public static boolean isDisabled() {
        return disabled;
    }


}
