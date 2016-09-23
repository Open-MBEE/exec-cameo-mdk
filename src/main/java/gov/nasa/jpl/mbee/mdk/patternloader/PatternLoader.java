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
package gov.nasa.jpl.mbee.mdk.patternloader;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.ui.BaseProgressMonitor;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.stylesaver.StyleSaverUtils;
import gov.nasa.jpl.mbee.mdk.stylesaver.ViewLoader;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A class used to load patterns onto the active diagram from other project
 * diagrams.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternLoader extends MDAction {
    private static final long serialVersionUID = 1L;
    private PresentationElement requester;

    /**
     * Initializes the Pattern Loader.
     *
     * @param id       the ID of the action.
     * @param value    the name of the action.
     * @param mnemonic the mnemonic key of the action.
     * @param group    the name of the related commands group.
     */
    public PatternLoader(String id, String value, int mnemonic, String group, PresentationElement requester) {
        super(id, value, mnemonic, group);

        this.requester = requester;
    }

    /**
     * Perform a pattern load on menu option mouse click.
     *
     * @param e the ActionEvent that fired this method.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession("Loading Pattern...");

        if (requester == null) {
            this.requester = Application.getInstance().getProject().getActiveDiagram();
        }

        // allow user to pick load or stamp operation
        try {
            delegateOperation();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            SessionManager.getInstance().cancelSession();
            return;
        }

        SessionManager.getInstance().closeSession();
    }

    /**
     * Allows the user to pick the load or stamp operation.
     *
     * @throws RuntimeException if a problem with loading or stamping occurs.
     */
    private void delegateOperation() throws RuntimeException {
        Object[] options = {"Load pattern", "Stamp pattern"};

        int opt = JOptionPane.showOptionDialog(null,
                "Would you like to load or stamp a pattern onto the diagram?\n", null,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        // if user presses no or closes the dialog, exit the program. else, add
        // view stereotype to diagram
        if ((opt == JOptionPane.CANCEL_OPTION) || (opt == JOptionPane.CLOSED_OPTION)) {
            throw new RuntimeException();
        }

        try {
            if ((opt == JOptionPane.YES_OPTION)) {
                prepAndRun(true);
            }
            else if ((opt == JOptionPane.NO_OPTION)) {
                prepAndRun(false);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void prepAndRun(boolean runLoad) throws RuntimeException {
        Project proj = Application.getInstance().getProject();

        // get the presentation elements of the requester - there should only be
        // one (the diagram)
        Element requesterElem = requester.getElement();

        // ensure the diagram is locked for edit
        if (!StyleSaverUtils.isDiagramLocked(proj, requester.getElement())) {
            JOptionPane.showMessageDialog(null,
                    "The target diagram is not locked for edit. Lock it before running this function.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Diagram requesterDiagramElem = (Diagram) proj.getElementByID(requesterElem.getID());

        DiagramPresentationElement patternDiagram = getPatternDiagram();
        DiagramPresentationElement targetDiagram = proj.getDiagram(requesterDiagramElem);

        if ((patternDiagram == null) || (targetDiagram == null)) {
            throw new RuntimeException();
        }
        else {
            patternDiagram.ensureLoaded();
            targetDiagram.ensureLoaded();
        }

        try {
            if (runLoad) {
                runLoadPattern(proj, targetDiagram, patternDiagram);
            }
            else {
                runStampPattern(proj, targetDiagram, patternDiagram);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * Runs the Pattern Loader with a progress bar.
     *
     * @throws RuntimeException if a problem with loading is encountered.
     */
    private void runLoadPattern(Project proj, DiagramPresentationElement targetDiagram,
                                DiagramPresentationElement patternDiagram) throws RuntimeException {
        List<PresentationElement> targetElements = targetDiagram.getPresentationElements();

        RunnablePatternLoaderWithProgress runnable = new RunnablePatternLoaderWithProgress(proj,
                patternDiagram, targetElements);
        BaseProgressMonitor.executeWithProgress(runnable, "Load Progress", false);

        if (runnable.getSuccess()) {
            JOptionPane.showMessageDialog(null, "Load complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(null, "Error occurred. Load cancelled.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            throw new RuntimeException();
        }
    }

    /**
     * Runs the Pattern Stamper.
     *
     * @throws RuntimeException if a problem with loading is encountered.
     */
    private void runStampPattern(Project proj, DiagramPresentationElement targetDiagram,
                                 DiagramPresentationElement patternDiagram) throws RuntimeException {
        List<PresentationElement> patternElements = patternDiagram.getPresentationElements();

        RunnablePatternStamperWithProgress runnable = new RunnablePatternStamperWithProgress(targetDiagram,
                patternElements);
        BaseProgressMonitor.executeWithProgress(runnable, "Load Progress", false);

        if (runnable.getSuccess()) {
            JOptionPane.showMessageDialog(null, "Stamp complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(null, "Error occurred. Stamp cancelled.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            throw new RuntimeException();
        }
    }

    /**
     * Provides an input dialog for the user to pick a diagram to load a pattern
     * from.
     *
     * @return the user-selected pattern diagram.
     */
    private DiagramPresentationElement getPatternDiagram() {
        // get all of the diagrams in the package for the user to pick from
        Collection<DiagramPresentationElement> diagCollection = PatternLoaderUtils
                .getPatternDiagrams(requester);
        Iterator<DiagramPresentationElement> diagIter = diagCollection.iterator();

        int numNames = diagCollection.size();
        String[] diagramNames = new String[numNames];

        for (int i = 0; diagIter.hasNext(); i++) {
            DiagramPresentationElement currDiag = diagIter.next();
            diagramNames[i] = currDiag.getName() + "  [" + currDiag.getHumanType() + "]";
        }

        // sort the diagram names for better UI
        Arrays.sort(diagramNames, String.CASE_INSENSITIVE_ORDER);
        String[] sortedNames = Arrays.copyOfRange(diagramNames, 0, diagramNames.length);

        String userInput;
        try {
            userInput = (String) JOptionPane.showInputDialog(null, "Choose a diagram to get a pattern from:",
                    null, JOptionPane.DEFAULT_OPTION, null, sortedNames, sortedNames[0]);
        } catch (HeadlessException e) {
            Application.getInstance().getGUILog()
                    .log("The Pattern Loader must be run in a graphical interface");
            return null;
        }

        // user cancelled input - return safely
        if (userInput == null) {
            return null;
        }

        // find and return the pattern diagram
        diagIter = diagCollection.iterator();
        while (diagIter.hasNext()) {
            DiagramPresentationElement currDiag = diagIter.next();

            String currDiagDescriptor = currDiag.getName() + "  [" + currDiag.getHumanType() + "]";

            if (currDiagDescriptor.equals(userInput)) {
                return currDiag;
            }
        }

        Application.getInstance().getGUILog()
                .log("There was a problem starting the Pattern Loader. The Pattern Loader is now exiting.");
        return null;
    }

    /**
     * Loads the style of elements on the diagram by gathering relevant style
     * information from the JSONObject.
     *
     * @param elemList the list of elements to load styles into.
     * @param pattern  the pattern to load.
     */
    public static void loadPattern(List<PresentationElement> elemList, JSONObject pattern,
                                   ProgressStatus progressStatus) {
        for (PresentationElement elem : elemList) {
            String elemStyle = (String) pattern.get(elem.getHumanType());

            if (elemStyle == null) {
                // there was no style pattern found for this element, load
                // children and then continue
                setStyleChildren(elem, pattern);
                continue;
            }

            // load the style of the diagram element re-using the
            // ViewLoader.setStyle() method
            ViewLoader.setStyle(elem, elemStyle);

            // then load the style of its children recursively
            setStyleChildren(elem, pattern);

            elem.getDiagramSurface().repaint();

            if (progressStatus != null) {
                progressStatus.increase();
            }
        }
    }

    /**
     * Recursively loads style information of owned elements.
     *
     * @param parent the parent element to recurse on.
     * @param style  the central style string holding all style properties.
     */
    private static void setStyleChildren(PresentationElement parent, JSONObject pattern) {
        List<PresentationElement> children = parent.getPresentationElements();

        // base case -- no children
        if (children.isEmpty()) {
            return;
        }

        // recursively load the style of the diagram element's children
        loadPattern(children, pattern, null);
    }
}
