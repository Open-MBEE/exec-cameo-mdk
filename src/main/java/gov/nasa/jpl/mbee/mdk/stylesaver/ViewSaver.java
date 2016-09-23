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
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.uml.symbols.shapes.TextAreaView;
import com.nomagic.task.ProgressStatus;
import com.nomagic.ui.BaseProgressMonitor;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * A class that saves style information of elements on a diagram.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class ViewSaver extends MDAction {
    private static final long serialVersionUID = 1L;
    private static boolean suppressOutput;

    /**
     * Initializes the ViewSaver.
     *
     * @param id       the ID of the action.
     * @param value    the name of the action.
     * @param mnemonic the mnemonic key of the action.
     * @param group    the name of the related commands group.
     * @param s        if true, user-interactivity is suppressed.
     */
    public ViewSaver(String id, String value, int mnemonic, String group, boolean s) {
        super(id, value, null, null);

        suppressOutput = s;
    }

    /**
     * Opens up the active diagram and performs the save operation.
     *
     * @param e the ActionEvent that fired this method.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession("Saving styles...");

        Project proj = Application.getInstance().getProject();

        // try to load the active diagram
        DiagramPresentationElement diagram;
        try {
            diagram = proj.getActiveDiagram();
            diagram.ensureLoaded();
        } catch (NullPointerException ex) {
            if (!suppressOutput) {
                JOptionPane.showMessageDialog(null, "Exiting - there was an error loading the diagram.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // ensure the diagram is locked for edit
        if (!StyleSaverUtils.isDiagramLocked(proj, diagram.getElement())) {
            JOptionPane.showMessageDialog(null,
                    "This diagram is not locked for edit. Lock it before running this function.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // perform the save operation
        String JSONStr = save(proj, diagram, false);
        if (JSONStr != null) {
            SessionManager.getInstance().closeSession();
            JOptionPane.showMessageDialog(null, "Save complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            SessionManager.getInstance().cancelSession();
            JOptionPane.showMessageDialog(null, "Save cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Returns relevant style information in a JSON string.
     *
     * @param proj     the diagram's project.
     * @param diagram  the diagram to save.
     * @param suppress set to true to suppress swing dialog boxes.
     * @return null on error or the JSON style string for this diagram.
     */
    public static String save(Project proj, DiagramPresentationElement diagram, boolean suppress) {
        suppressOutput = suppress;

        // ensure diagram is open
        if (diagram == null) {
            if (!suppressOutput) {
                JOptionPane.showMessageDialog(null, "Please open a diagram first.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            return null;
        }
        else {
            diagram.ensureLoaded();
        }

        // get the proper stereotype for diagrams of this project
        Stereotype workingStereotype = StyleSaverUtils.getWorkingStereotype(proj);
        if (workingStereotype == null) {
            if (!suppressOutput) {
                JOptionPane.showMessageDialog(null,
                        "This diagram's stereotype is invalid for this function.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            return null;
        }

        // ensure that this diagram is stereotyped view
        if (!StyleSaverUtils.isGoodStereotype(diagram, workingStereotype)) {
            int opt;
            if (!suppressOutput) {
                Object[] options = {"Yes", "Cancel save"};

                opt = JOptionPane.showOptionDialog(null,
                        "This diagram is not stereotyped " + workingStereotype.getName()
                                + ". It must be stereotyped " + workingStereotype.getName() + "\n"
                                + "or derived to continue with the save.\n\n" + "Would you like to add the "
                                + workingStereotype.getName() + " stereotype to this diagram?", "Error",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                // if user presses no or closes the dialog, exit the program.
                // else, add view stereotype to diagram
                if ((opt == JOptionPane.NO_OPTION) || (opt == JOptionPane.CLOSED_OPTION)) {
                    return null;
                }
                else {
                    StereotypesHelper.addStereotype(diagram.getElement(), workingStereotype);
                }
            }
        }

        // get all the elements in the diagram and store them into a list
        List<PresentationElement> elemList = new ArrayList<PresentationElement>();
        try {
            for (PresentationElement pe : diagram.getPresentationElements()) {
                // TextAreViews are generated dynamically on diagram, so don't
                // save
                if (!(pe instanceof TextAreaView)) {
                    elemList.add(pe);
                }
            }
            Collections.sort(elemList, PresentationElementComparator);
        } catch (NullPointerException e) {
            if (!suppressOutput) {
                JOptionPane.showMessageDialog(null, "Save cancelled. There are no elements in the diagram.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }

            return null;
        }

        // get a JSON style string from each element and store them into a main
        // store for this diagram
        // Use LinkedHashMap instead of JSONObject so JSON is always serialized
        // in same order
        Map<String, String> mainStore = new LinkedHashMap<String, String>();

        // display a progress bar if output is not being suppressed, otherwise
        // just execute the save
        if (!suppressOutput) {
            RunnableSaverWithProgress runnable = new RunnableSaverWithProgress(mainStore, elemList,
                    diagram.getElement(), workingStereotype);

            BaseProgressMonitor.executeWithProgress(runnable, "Save Progress", true);

            if (runnable.getSuccess()) {
                return runnable.getStyleString();
            }
            else {
                return null;
            }
        }
        else {
            return executeSave(elemList, mainStore);
        }
    }

    /**
     * Generates the style string for the presentation elements on the diagram.
     *
     * @param elemList  the list of presentation elements on the diagram.
     * @param mainStore the JSONObject that will be converted into a JSON string.
     * @return the style string.
     */
    private static String executeSave(List<PresentationElement> elemList, Map<String, String> mainStore) {
        for (PresentationElement elem : elemList) {
            // save the element's style properties
            try {
                String styleStr = getStyle(elem);

                // if there is no style to save, continue to next element
                if (styleStr == null) {
                    continue;
                }

                mainStore.put(elem.getID(), styleStr);
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (MissingResourceException e) {
                e.printStackTrace();
            }

            // recursively save child elements' style properties (e.g. ports)
            getStyleChildren(elem, mainStore, null);
        }

        return JSONValue.toJSONString(mainStore);
    }

    /**
     * Get a JSON string representing the style properties of the parameterized
     * presentation element.
     *
     * @param elem the element to get style properties from.
     * @return the element's respective JSON style string.
     */
    @SuppressWarnings("unchecked")
    // for JSONObject put() method
    public static String getStyle(PresentationElement elem) {
        PropertyManager propMan;
        JSONObject entry = new JSONObject();

        // get a property manager to retrieve style properties
        propMan = elem.getPropertyManager();
        List<Property> propList = propMan.getProperties();

        // no properties to save
        if (propList.isEmpty()) {
            return null;
        }

        // iterate over each property in the list and store key/value pairs into
        // "entry"
        Iterator<Property> iter = propList.iterator();

        while (iter.hasNext()) {
            Property prop = iter.next();
            entry.put(prop.getID(), prop.toString());
        }

        // put element bounds into "entry" as well
        Rectangle rect = elem.getBounds();
        entry.put("rect_x", rect.getX());
        entry.put("rect_y", rect.getY());
        entry.put("rect_height", rect.getHeight());
        entry.put("rect_width", rect.getWidth());

        // save break points if the element is a PathElement
        if (elem instanceof PathElement) {
            List<Point> breakPoints = ((PathElement) elem).getAllBreakPoints();

            // add in the break points
            for (int i = 0; i < breakPoints.size(); i++) {
                entry.put("break_point_" + i, breakPoints.get(i).toString());
            }

            // add in the number of break points
            entry.put("num_break_points", breakPoints.size());

            Point supplierPt = ((PathElement) elem).getSupplierPoint();
            Point clientPt = ((PathElement) elem).getClientPoint();

            // add in the supplier and client points
            entry.put("supplier_point", supplierPt.toString());
            entry.put("client_point", clientPt.toString());

            int lineWidth = ((PathElement) elem).getLineWidth();

            entry.put("path_line_width", Integer.toString(lineWidth));
        }

        // convert the main entry store to a JSON string
        String JSONStr = entry.toJSONString();

        return JSONStr;
    }

    /**
     * Recursively saves style information of owned elements into the JSONObject
     * pointed to by mainStore.
     *
     * @param elem      the element to get style properties from.
     * @param mainStore the JSONObject to store style information into.
     */
    // for JSONObject put() method
    public static void getStyleChildren(PresentationElement parent, Map<String, String> mainStore,
                                        ProgressStatus progressStatus) {
        // get the parent element's children
        List<PresentationElement> children = new ArrayList<PresentationElement>(); // parent.getPresentationElements();
        for (PresentationElement pe : parent.getPresentationElements()) {
            // TextAreViews are generated dynamically on diagram, so don't save
            if (!(pe instanceof TextAreaView)) {
                children.add(pe);
            }
        }
        Collections.sort(children, PresentationElementComparator);

        // base case -- no children
        if (children.isEmpty()) {
            return;
        }

        // check to see if the user cancelled
        if (progressStatus != null) {
            if (progressStatus.isCancel()) {
                return;
            }
        }

        Iterator<PresentationElement> iter = children.iterator();

        // iterate over each owned element storing style properties
        while (iter.hasNext()) {
            PresentationElement currElem = iter.next();

            // recursively call on the current element
            getStyleChildren(currElem, mainStore, progressStatus);

            try {
                String styleStr = getStyle(currElem);

                // store style only if there is a string to store
                if (styleStr != null) {
                    mainStore.put(currElem.getID(), styleStr);
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (MissingResourceException e) {
                e.printStackTrace();
            }
        }
    }

    public static Comparator<PresentationElement> PresentationElementComparator = new Comparator<PresentationElement>() {
        @Override
        public int compare(
                PresentationElement pe1,
                PresentationElement pe2) {
            return pe1
                    .getID()
                    .compareTo(
                            pe2.getID());
        }
    };
}
