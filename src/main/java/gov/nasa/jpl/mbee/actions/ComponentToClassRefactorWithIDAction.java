package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.ems.sync.AutoSyncCommitListener;
import gov.nasa.jpl.mbee.ems.sync.ProjectListenerMapping;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.ConvertElementInfo;
import com.nomagic.magicdraw.uml.Refactoring;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;

public class ComponentToClassRefactorWithIDAction extends DefaultBrowserAction {

    Element element;
    public static final String actionid = "ConvertComponent";
    private static final long serialVersionUID = 1L;

    public ComponentToClassRefactorWithIDAction(Element e) {
        super(actionid, "Convert Component To Class", null, null);
        this.element = e;
    }

    public void actionPerformed(ActionEvent e) {
        Project project = Application.getInstance().getProject();
        Map<String, ?> projectInstances = ProjectListenerMapping.getInstance()
                .get(project);
        AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances
                .get("AutoSyncCommitListener");
        if (listener != null)
            listener.disable();

        String elementID = element.getID();

        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.createSession("Convert Component To Class");
        // Converts the element to an interface.
        ConvertElementInfo info = new ConvertElementInfo(
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
        // Preserves the old element ID for the new element.
        info.setPreserveElementID(false);
        try {
            Element conversionTarget = Refactoring.Converting.convert(element, info);
            Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
            // element.setID(elementID);
            conversionTarget.setID(elementID);
            // String newElementID = conversionTarget.getID();
        } catch (ReadOnlyElementException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        sessionManager.closeSession();

        if (listener != null)
            listener.enable();
    }

}
