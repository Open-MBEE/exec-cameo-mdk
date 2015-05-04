package gov.nasa.jpl.mbee.actions.docgen;

import gov.nasa.jpl.mbee.generator.ViewPresentationGenerator;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class GenerateViewPresentationAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private Element            doc;
    private boolean recurse;
    public static final String actionid = "GenerateViewPresentation";
    public static final String recurseActionid = "GenerateViewPresentationR";


    public GenerateViewPresentationAction(Element e, boolean recurse) {
        super(recurse ? recurseActionid : actionid, recurse ? "Generate View Presentations": "Generate View Presentation", null, null);
        doc = e;
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ViewPresentationGenerator vg = new ViewPresentationGenerator(doc, recurse);
        vg.generate();
    }
}
