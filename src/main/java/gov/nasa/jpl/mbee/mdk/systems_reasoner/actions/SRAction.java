package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class SRAction extends MDAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String DEFAULT_ID;
    public Element element;

    public SRAction(String DEFAULT_ID) {
        super(DEFAULT_ID, DEFAULT_ID, null, ActionsGroups.APPLICATION_RELATED);
        this.DEFAULT_ID = DEFAULT_ID;
    }

    public SRAction(String DEFAULT_ID, Element element) {
        this(DEFAULT_ID);
        this.element = element;
    }

    public void disable() {
        setEnabled(false);
    }

    public void disable(String error) {
        this.setName(DEFAULT_ID + " [" + error + "]");
        disable();
    }

    public void enable() {
        setEnabled(true);
    }

    @Override
    public void updateState() {
    }

}
