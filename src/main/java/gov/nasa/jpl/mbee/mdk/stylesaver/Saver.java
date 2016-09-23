/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.mdk.stylesaver;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

/**
 * A class that saves style information corresponding to stereotyped elements.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class Saver extends MDAction {
    private static final long serialVersionUID = 1L;

    /**
     * Initializes the Saver.
     *
     * @param id       The ID of the action.
     * @param value    The name of the action.
     * @param elem     The element to be "saved."
     * @param mnemonic The mnemonic key of the action.
     * @param group    The name of the related commands group.
     */
    public Saver(String id, String value, int mnemonic, String group) {
        super(id, value, null, null);
    }

    /**
     * Saves the style of elements on the active diagram by storing relevant
     * style information in a JSON-formatted string to a property named "style"
     * in each respective element.
     *
     * @param e The ActionEvent that fired this method.
     */
    @Override
    @SuppressWarnings("unchecked")
    // for JSObject put() method
    public void actionPerformed(ActionEvent e) {
        Project proj = Application.getInstance().getProject(); // get the
        // project
        GUILog gl = Application.getInstance().getGUILog();

        DiagramPresentationElement diagram;

        // try to load the active diagram
        try {
            diagram = proj.getActiveDiagram();
        } catch (NullPointerException ex) {
            gl.log("Plugin usage invalid -- There are no active diagrams to save.");
            return;
        }

        // get all the elements in the active diagram and store them into a list
        List<PresentationElement> list;

        try {
            list = diagram.getPresentationElements();
        } catch (NullPointerException ex) {
            gl.log("Plugin usage invalid -- There are no elements in the diagram to save.");
            return;
        }

        String JSONStr = "";
        JSONObject mainStore = new JSONObject();
        String styleStr;

        // get JSON style string from each element and store them into a main
        // store for this diagram
        for (PresentationElement diagElem : list) {
            try {
                styleStr = getStyle(diagElem);
                mainStore.put(diagElem.getID(), styleStr);
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            } catch (MissingResourceException ex) {
                ex.printStackTrace();
            }
        }

        // convert the main entry store to a JSON-formatted string
        JSONStr = mainStore.toJSONString();

        // make a block in the current project
        makeBlock(JSONStr, proj, diagram);
    }

    /**
     * Makes a block with a property storing the string passed in.
     *
     * @param style A JSON-formatted style string to load into the created block's
     *              property.
     * @param proj  The project the diagram is in.
     * @param diag  The diagram to save.
     */
    private static void makeBlock(String style, Project proj, DiagramPresentationElement diag) {
        SessionManager.getInstance().createSession("Adding a block");

        // get a factory so we can create a class
        ElementsFactory factory = Application.getInstance().getProject().getElementsFactory();
        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class cl = factory.createClassInstance();

        // create a style property of this class
        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property prop = factory.createPropertyInstance();

        // set its name, type, and default value
        prop.setName("Style");
        Classifier classifier = ModelHelper.findDataTypeFor(proj, "String", null);
        prop.setType(classifier);
        LiteralString str = proj.getElementsFactory().createLiteralStringInstance();
        str.setValue(style);
        prop.setDefaultValue(str);

        // set the name of the block to diagramName.diagramID.Style
        cl.setName(diag.getName() + "." + diag.getElement().getID() + ".Style");

        // add the block stereotype to the class
        Stereotype blockStereo = StereotypesHelper.getStereotype(proj, "Block");
        if (blockStereo != null) {
            StereotypesHelper.addStereotype(cl, blockStereo);
        }

        // add the property to the class, then add the class to the diagram's
        // parent
        try {
            ModelElementsManager.getInstance().addElement(prop, cl);
            ModelElementsManager.getInstance().addElement(cl, diag.getElement().getOwner());
        } catch (ReadOnlyElementException ex) {
        }

        SessionManager.getInstance().closeSession();
    }

    /**
     * Get a JSON-formatted string representing the style properties of the
     * parameterized Presentation element.
     *
     * @param elem The element to get style properties from.
     * @return A JSON-formatted string.
     */
    @SuppressWarnings("unchecked")
    // for JSObject put() method
    private static String getStyle(PresentationElement elem) {
        // utility variables
        String JSONStr = "";
        PropertyManager propMan;
        JSONObject entry = new JSONObject();

        // get a property manager to retrieve style properties
        propMan = elem.getPropertyManager();
        List<Property> propList = propMan.getProperties();

        // iterate over each property in the list
        Iterator<Property> iter = propList.iterator();
        Property prop;

        while (iter.hasNext()) {
            prop = iter.next();
            entry.put(prop.getID(), prop.toString());
        }

        // convert the main entry store to a JSON-formatted string
        JSONStr = entry.toJSONString();

        return JSONStr;
    }
}
