package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;

import java.awt.event.ActionEvent;

public class AddInheritanceToAssociationAction extends SRAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String actionid = "Add inheritance to assocation.";
    private Association association;
    private Association superAssociation;

    public AddInheritanceToAssociationAction(String actionid) {
        super(actionid);

    }

    public AddInheritanceToAssociationAction(String actionid, Element element) {
        super(actionid, element);

    }

    public AddInheritanceToAssociationAction(Association association, Association superAssociation) {
        super(actionid);
        this.association = association;
        this.superAssociation = superAssociation;

    }

    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession(actionid);
        if (association != null) {
            if (superAssociation != null) {
                Generalization gen = UMLFactory.eINSTANCE.createGeneralization();
                gen.setGeneral(superAssociation);
                gen.setSpecific(association);
                this.association.getGeneralization().add(gen);
                Application.getInstance().getGUILog().log("Inheritance added to association" + association.getName());
            }
            else {
                Application.getInstance().getGUILog().log("Association on super element missing.");
            }
        }
        else {
            Application.getInstance().getGUILog().log("Association on inheriting element missing.");
        }
        SessionManager.getInstance().closeSession();
    }
}
