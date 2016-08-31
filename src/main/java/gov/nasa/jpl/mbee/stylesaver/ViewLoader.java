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
package gov.nasa.jpl.mbee.stylesaver;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.ui.BaseProgressMonitor;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * A class that loads style information into elements on the active diagram from
 * the "style" tag of the diagram's stereotype. The diagram must be stereotyped
 * View or derived.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class ViewLoader extends MDAction {
    private static final long serialVersionUID = 1L;

    /**
     * Initializes the Loader.
     * 
     * @param id
     *            the ID of the action.
     * @param value
     *            the name of the action.
     * @param mnemonic
     *            the mnemonic key of the action.
     * @param group
     *            the name of the related commands group.
     */
    public ViewLoader(String id, String value, int mnemonic, String group) {
        super(id, value, null, null);
    }

    /**
     * Opens up the active diagram and performs the load operation.
     * 
     * @param e
     *            the ActionEvent that fired this method.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession("Loading styles...");

        Project proj = Application.getInstance().getProject();
        DiagramPresentationElement diagram = proj.getActiveDiagram();
        JSONObject style = null;

        try {
            style = prep(proj, diagram);
        } catch (RuntimeException ex) {
            SessionManager.getInstance().cancelSession();
        }

        // run the loader with a progress bar
        RunnableLoaderWithProgress runnable = new RunnableLoaderWithProgress(
                diagram.getPresentationElements(), style);
        BaseProgressMonitor.executeWithProgress(runnable, "Load Progress", true);

        if (runnable.getSuccess()) {
            SessionManager.getInstance().closeSession();
            JOptionPane.showMessageDialog(null, "Load complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            SessionManager.getInstance().cancelSession();
            JOptionPane.showMessageDialog(null, "Load cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Prepares the active diagram for a load.
     * 
     * @param proj
     *            the project the diagram resides in.
     * @param diagram
     *            the diagram to prepare.
     * @return the style string from the style tag.
     * @throws RuntimeException
     *             if an error occurs.
     */
    private JSONObject prep(Project proj, DiagramPresentationElement diagram) throws RuntimeException {
        try {
            diagram.ensureLoaded();
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Exiting - there was an error loading the diagram.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!StyleSaverUtils.isDiagramLocked(proj, diagram.getElement())) {
            JOptionPane.showMessageDialog(null,
                    "This diagram is not locked for edit. Lock it before running this function.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Stereotype workingStereotype = StyleSaverUtils.getWorkingStereotype(proj);
        if (workingStereotype == null) {
            return null;
        }

        // ensure that this diagram is stereotyped with the working stereotype
        if (!StyleSaverUtils.isGoodStereotype(diagram, workingStereotype)) {
            Object[] options = {"Yes", "Cancel load"};

            int opt = JOptionPane.showOptionDialog(null, "This diagram is not stereotyped "
                    + workingStereotype.getName() + ". It must be stereotyped " + workingStereotype.getName()
                    + "\n" + "or derived to continue with the load.\n\n" + "Would you like to add the "
                    + workingStereotype.getName() + " stereotype to this diagram?", "Error",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            // if user presses no or closes the dialog, exit the program. else,
            // add view stereotype to diagram
            if ((opt == JOptionPane.NO_OPTION) || (opt == JOptionPane.CLOSED_OPTION)) {
                return null;
            } else {
                StereotypesHelper.addStereotype(diagram.getElement(), workingStereotype);
                JOptionPane.showMessageDialog(null, "Stereotype added.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // get the main style string from the view stereotype tag "style"
        Object tag = StereotypesHelper.getStereotypePropertyFirst(diagram.getElement(), workingStereotype,
                "style");
        String style = StereotypesHelper.getStereotypePropertyStringValue(tag);

        // return the parsed JSON Object
        return StyleSaverUtils.parse(style);
    }

    /**
     * Loads the style of elements on the diagram by gathering relevant style
     * information from the JSON style string. Monitors progress.
     * 
     * @param elemList
     *            the list of elements to load styles into.
     * @param style
     *            the style string.
     * @param progressStatus
     *            the status of the program status bar.
     * @return true if successful, false if the user cancelled load
     */
    public static boolean load(List<PresentationElement> elemList, JSONObject style,
            ProgressStatus progressStatus) {
        for (PresentationElement elem: elemList) {
            if (progressStatus.isCancel()) {
                return false;
            }

            // parse the style string for the correct style of the current
            // element
            String elemStyle = StyleSaverUtils.getStyleStringForElement(elem, style);

            if (elemStyle == null) {
                // there was no style string found for the current element, just
                // continue
                continue;
            }

            // load the style of the current element
            setStyle(elem, elemStyle);
            elem.getDiagramSurface().repaint();

            // recursively load the style of the element's children after
            // parents have been moved
            loadChildren(elem, style);

            style.remove(elem.getID());

            progressStatus.increase();
        }

        return true;
    }

    /**
     * Loads the style of elements on the diagram by gathering relevant style
     * information from the JSON style string. Does NOT monitor progress.
     * 
     * @param elemList
     *            the list of elements to load styles into.
     * @param style
     *            the style string.
     */
    public static void load(List<PresentationElement> elemList, JSONObject style) {
        for (PresentationElement elem: elemList) {
            // parse the style string for the correct style of the current
            // element
            String elemStyle = StyleSaverUtils.getStyleStringForElement(elem, style);

            if (elemStyle == null) {
                // there was no style string found for the current element, just
                // continue
                continue;
            }

            // load the style of the current element
            setStyle(elem, elemStyle);
            elem.getDiagramSurface().repaint();

            // recursively load the style of the element's children after parent
            // is moved
            loadChildren(elem, style);

            style.remove(elem.getID());
        }
    }

    /**
     * Recursively loads style information of owned elements.
     * 
     * @param parent
     *            the parent element to recurse on.
     * @param style
     *            the central style string holding all style properties.
     */
    private static void loadChildren(PresentationElement parent, JSONObject style) {
        List<PresentationElement> children = parent.getPresentationElements();

        // base case -- no children
        if (children.isEmpty()) {
            return;
        }

        // recursively load the style of the diagram element's children
        load(children, style);
    }

    /**
     * Sets style properties in the parameterized PresentationElement according
     * to the PresentationElement's style property.
     * 
     * @param elem
     *            the element to set style properties in.
     * @param style
     *            the JSON style string for the element.
     */
    public static void setStyle(PresentationElement elem, String styleStr) {
        PropertyManager propMan = elem.getPropertyManager();
        JSONObject styleObj = StyleSaverUtils.parse(styleStr);

        List<Property> propList = new ArrayList<Property>(propMan.getProperties());

        // no properties to load
        if (propList.isEmpty()) {
            return;
        }

        ListIterator<Property> iter = propList.listIterator();

        // iterate over all the current properties
        while (iter.hasNext()) {
            Property currProp = iter.next();
            String currPropID = currProp.getID();

            // generate the new property
            String newPropValue = (String)styleObj.get(currPropID);

            // select the correct set method for the property's type
            if (currProp.getValue() instanceof java.awt.Color) {
                ViewLoaderHelper.setColor(currProp, newPropValue, elem);
            } else if (currProp.getValue() instanceof java.awt.Font) {
                ViewLoaderHelper.setFont(currProp, newPropValue, elem);
            } else if (currProp.getValue() instanceof java.lang.Boolean) {
                ViewLoaderHelper.setBoolean(currProp, newPropValue, elem);
            } else if (currProp instanceof com.nomagic.magicdraw.properties.ChoiceProperty) {
                ViewLoaderHelper.setChoice(currProp, newPropValue, elem);
            }

            styleObj.remove(currPropID);
        }

        // deal with break points if the element is a PathElement
        if (elem instanceof PathElement) {
            ViewLoaderHelper.setBreakPoints((PathElement)elem, styleObj);
            ViewLoaderHelper.setLineWidth((PathElement)elem, styleObj);
        }

        // deal with bounds if the element is a ShapeElement
        if (elem instanceof ShapeElement) {
            ViewLoaderHelper.setBounds((ShapeElement)elem, styleObj);
        }
    }
}
