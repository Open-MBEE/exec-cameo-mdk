package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.manipulators.drawactions.AdditionalDrawAction;
import com.nomagic.uml2.StandardProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Operation;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import java.awt.*;


public class ViewpointAdditionalDrawAction extends AdditionalDrawAction {
    public boolean execute(PresentationElement presentationElement, Point point) {
        if (!(presentationElement.getElement() instanceof Class)) {
            return false;
        }
        createOperation((Class) presentationElement.getElement());
        return true;
    }

    private Operation createOperation(Class clazz) {
        ElementsFactory elementsFactory = Project.getProject(clazz).getElementsFactory();
        Operation operation = elementsFactory.createOperationInstance();
        operation.setOwner(clazz);
        operation.setName("View");
        Stereotype createStereotype = StandardProfile.getInstance(clazz).getCreate();
        if (createStereotype != null) {
            StereotypesHelper.addStereotype(operation, createStereotype);
        }

        return operation;
    }

    @Override
    public void afterExecute(PresentationElement presentationElement, Point point) {

    }
}
