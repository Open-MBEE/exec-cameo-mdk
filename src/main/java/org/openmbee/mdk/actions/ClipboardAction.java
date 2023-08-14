package org.openmbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;

import javax.annotation.CheckForNull;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

/**
 * Created by igomes on 10/12/16.
 */
public class ClipboardAction extends MDAction {
    public static final String DEFAULT_ID = ClipboardAction.class.getSimpleName();

    private final String text;

    public ClipboardAction(String name, String text) {
        super(DEFAULT_ID + "_" + name.replace(" ", ""), name, null, null);
        this.text = text;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }
}
