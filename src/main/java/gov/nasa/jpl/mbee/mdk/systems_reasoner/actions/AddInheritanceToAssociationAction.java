package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;

import java.awt.event.ActionEvent;

public class AddInheritanceToAssociationAction extends SRAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Add inheritance to assocation.";
    private Association association;
    private Association superAssociation;

    public AddInheritanceToAssociationAction(String id) {
        super(id);

    }

    public AddInheritanceToAssociationAction(String id, Element element) {
        super(id, element);

    }

    public AddInheritanceToAssociationAction(Association association, Association superAssociation) {
        super(DEFAULT_ID);
        this.association = association;
        this.superAssociation = superAssociation;

    }

    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession(DEFAULT_ID);
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
