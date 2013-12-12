package gov.nasa.jpl.mbee.alfresco.validation.actions;

import gov.nasa.jpl.mbee.alfresco.validation.ResultHolder;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class FixModelOwner extends MDAction implements AnnotationAction{

    private static final long serialVersionUID = 1L;
    private Element element;
    private Element owner;
    
    public FixModelOwner(Element e, Element owner) {
        super("FixModelOwner", "Fix model owner", null, null);
        this.element = e;
        this.owner = owner;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject result = ResultHolder.lastResults;
        SessionManager.getInstance().createSession("Change Owners");
        Project prj = Application.getInstance().getProject();
        try {
            for (Annotation anno: annos) {
                Element e = (Element)anno.getTarget();
                if (!e.isEditable()) {
                    continue;
                }
                String ownerID = (String)((JSONObject)((JSONObject)result.get("elements")).get(e.getID())).get("owner");
                if (ownerID == null)
                    continue;
                Element own = (Element)prj.getElementByID(ownerID);
                if (own != null)
                    e.setOwner(own);
            }
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession("Change Owner");
        try {
            if (element.isEditable())
                element.setOwner(owner);
            else
                Application.getInstance().getGUILog().log("[ERROR] Element is not editable!");
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
    }
}
