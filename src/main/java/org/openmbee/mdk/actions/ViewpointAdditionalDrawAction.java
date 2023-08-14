package org.openmbee.mdk.actions;

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

    public static Operation createOperation(Class viewpoint) {
        ElementsFactory elementsFactory = Project.getProject(viewpoint).getElementsFactory();
        Operation operation = elementsFactory.createOperationInstance();
        operation.setOwner(viewpoint);
        operation.setName("View");
        Stereotype createStereotype = StandardProfile.getInstance(viewpoint).getCreate();
        if (createStereotype != null) {
            StereotypesHelper.addStereotype(operation, createStereotype);
        }

        return operation;
    }

    @Override
    public void afterExecute(PresentationElement presentationElement, Point point) {

    }
}
