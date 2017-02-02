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
package gov.nasa.jpl.mbee.mdk.lib;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.ui.browser.BrowserTabTree;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A collection of utility functions for accessing the MagicDraw (MD)
 * application.
 */
public class MDUtils {

    /**
     * @return true iff MD was started with the DEVELOPER option at the command
     * line.
     */
    public static boolean isDeveloperMode() {
        return Boolean.getBoolean("DEVELOPER");
    }

    /**
     * @param event
     * @return the {@link Element}s selected in MD's GUI.
     */
    public static Collection<Element> getSelection(ActionEvent event) {
        return getSelection(event, true);
    }

    /**
     * @param event
     * @param fromDiagram
     * @return the {@link Element}s selected in MD's GUI.
     */
    public static Collection<Element> getSelection(ActionEvent event, boolean fromDiagram) {
        // TODO -- the input event seems useless since its not unique to the
        // context
        Collection<Element> coll = Utils2.getEmptyList();
        boolean b = getActiveBrowser(false) != null;
        boolean d = getActiveDiagram(false) != null;
        // Component focus =
        // Application.getInstance().getMainFrame().getFocusOwner();
        // System.out.println("focus = " + focus);
        if (b && d) {
            if (fromDiagram) {
                coll = getSelectionInDiagram();
            }
            else {
                coll = getSelectionInContainmentBrowser();
            }
        }
        else {
            // Frame frame =
            // Application.getInstance().getMainFrame().getActiveFrame();
            if (d && fromDiagram) {
                coll = getSelectionInDiagram();
            }
            if (b && (!d || Utils2.isNullOrEmpty(coll))) {
                coll = getSelectionInContainmentBrowser();
            }
        }
        return coll;
    }

    /**
     * @param complain if true, any Throwable will be caught and printed with the
     *                 stack trace to stderr (or the MD message window)
     * @return the browser (e.g., containment tree) currently active in the MD
     * GUI
     */
    public static BrowserTabTree getActiveBrowser(boolean complain) {
        try {
            return Application.getInstance().getMainFrame().getBrowser().getActiveTree();
        } catch (Throwable e) {
            if (complain) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param complain if true, any Throwable will be caught and printed with the
     *                 stack trace to stderr (or the MD message window)
     * @return the diagram currently active in the MD GUI
     */
    public static DiagramPresentationElement getActiveDiagram(boolean complain) {
        try {
            Project project = Application.getInstance().getProject();
            DiagramPresentationElement diagram = null;
            if (project != null) {
                // Debug.outln( EmfUtils.spewContents( (Object)project, 0, 3,
                // false ) );
                diagram = project.getActiveDiagram();
            }
            return diagram;
        } catch (Throwable e) {
            if (complain) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @return the {@link Element}s selected in the containment tree browser of
     * MD's GUI.
     */
    public static Collection<Element> getSelectionInContainmentBrowser() {
        Collection<Element> coll = Utils2.getEmptyList();
        BrowserTabTree tree = getActiveBrowser(false);
        if (tree == null) {
            return coll;
        }
        Node[] nodes = tree.getSelectedNodes();
        if (Utils2.isNullOrEmpty(nodes)) {
            return coll;
        }
        coll = new ArrayList<Element>();
        for (Node selectedNode : nodes) {
            Object selected = selectedNode.getUserObject();
            if (selected instanceof Element) {
                coll.add((Element) selected);
            }
        }
        return coll;
    }

    /**
     * @return the {@link Element}s selected in the active diagram of MD's GUI.
     */
    public static Collection<Element> getSelectionInDiagram() {
        DiagramPresentationElement diagram = getActiveDiagram(false);
        List<PresentationElement> selectedList = null;
        if (diagram != null) {
            selectedList = diagram.getSelected();
        }
        // Get the elements without their presentation. (REVIEW -- why?)
        ArrayList<Element> selectedElements = new ArrayList<Element>();
        if (selectedList != null) {
            for (PresentationElement pe : selectedList) {
                Element e = pe.getElement();
                if (e != null) {
                    selectedElements.add(e);
                }
            }
        }
        return selectedElements;
    }



    public static Class<?> getType(BaseElement elem) {
        Class<?> type = elem.getClassType();

        return type;
    }

    // public static Object getValue( BaseElement elem ) {
    // Object res = null;
    // if ( elem instanceof EObject ) {
    // res = EmfUtils.getValue( (EObject)elem );
    // }
    // Assert.assertFalse( true ); // TODO
    // return res;
    // }

    // public static Constraint getConstraint( Element elemt ) {
    // Constraint c = null;
    // elemt.get_constraintOfConstrainedElement();
    // return c;
    // }

    public static String getWorkspace(Project project) {
        String twbranch = getRemoteBranchPath(project);
        if (twbranch == null) {
            return "master";
        }
        twbranch = "master/" + twbranch;
      //  String projId = Application.getInstance().getProject().getPrimaryProject().getProjectID();

        //TODO @donbot imported from ExportUtility, update to finish the import
//        Map<String, String> wsmap = wsIdMapping.get(projId);
//        if (wsmap == null) {
//            updateWorkspaceIdMapping();
//            wsmap = wsIdMapping.get(projId);
//        }
//        if (wsmap != null) {
//            String id = wsmap.get(twbranch);
//            if (id == null) {
//                updateWorkspaceIdMapping();
//                id = wsmap.get(twbranch);
//            }
//            if (id != null) {
//                return id;
//            }
//        }
//        Utils.guilog("[ERROR]: Cannot lookup workspace on server that corresponds to this project branch");
        return twbranch;
    }

    public static String getRemoteBranchPath(Project project) {
        if (!project.isRemote()) {
            return null;
        }
        return getRemoteBranchPath(ProjectDescriptorsFactory.createAnyRemoteProjectDescriptor(project).getURI());
    }

    public static String getRemoteBranchPath(URI uri) {
        return ProjectDescriptorsFactory.getProjectBranchPath(uri);
    }

    public static int getRemoteVersion(Project project) {
        if (!project.isRemote()) {
            return -1;
        }
        return getRemoteVersion(ProjectDescriptorsFactory.createAnyRemoteProjectDescriptor(project).getURI());
    }

    public static int getRemoteVersion(URI uri) {
        return ProjectDescriptorsFactory.getRemoteVersion(uri);
    }

    public static int getLatestEsiVersion(Project project) {
        if (!project.isRemote()) {
            return -1;
        }
        return getLatestEsiVersion(ProjectDescriptorsFactory.createAnyRemoteProjectDescriptor(project));
    }


    // TODO Switch to convenience method in 18.5 @donbot
    public static int getLatestEsiVersion(ProjectDescriptor projectDescriptor) {
        final int[] version = new int[]{-1};
        EsiUtils.getVersions(projectDescriptor).stream().max(ProjectUtilities::compareVersions).ifPresent(iVersionDescriptor -> version[0] = ProjectUtilities.versionToInt(iVersionDescriptor.getName()));
        return version[0];
    }


}
