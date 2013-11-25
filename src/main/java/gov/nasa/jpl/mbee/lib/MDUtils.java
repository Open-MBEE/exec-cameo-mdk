package gov.nasa.jpl.mbee.lib;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.BrowserTabTree;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A collection of utility functions for accessing the MagicDraw (MD)
 * application.
 * 
 */
public class MDUtils {

    /**
     * @return true iff MD was started with the DEVELOPER option at the command
     *         line.
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
            if (fromDiagram)
                coll = getSelectionInDiagram();
            else
                coll = getSelectionInContainmentBrowser();
        } else {
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
     * @param complain
     *            if true, any Throwable will be caught and printed with the
     *            stack trace to stderr (or the MD message window)
     * @return the browser (e.g., containment tree) currently active in the MD
     *         GUI
     */
    public static BrowserTabTree getActiveBrowser(boolean complain) {
        try {
            return Application.getInstance().getMainFrame().getBrowser().getActiveTree();
        } catch (Throwable e) {
            if (complain)
                e.printStackTrace();
        }
        return null;
    }

    /**
     * @param complain
     *            if true, any Throwable will be caught and printed with the
     *            stack trace to stderr (or the MD message window)
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
            if (complain)
                e.printStackTrace();
        }
        return null;
    }

    /**
     * @return the {@link Element}s selected in the containment tree browser of
     *         MD's GUI.
     */
    public static Collection<Element> getSelectionInContainmentBrowser() {
        Collection<Element> coll = Utils2.getEmptyList();
        BrowserTabTree tree = getActiveBrowser(false);
        if (tree == null)
            return coll;
        Node[] nodes = tree.getSelectedNodes();
        if (Utils2.isNullOrEmpty(nodes))
            return coll;
        coll = new ArrayList<Element>();
        for (Node selectedNode: nodes) {
            Object selected = selectedNode.getUserObject();
            if (selected instanceof Element) {
                coll.add((Element)selected);
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
            for (PresentationElement pe: selectedList) {
                Element e = pe.getElement();
                if (e != null)
                    selectedElements.add(e);
            }
        }
        return selectedElements;
    }

    // /**
    // * THIS DOESN'T WORK!!
    // * @param event
    // * @return
    // */
    // public static boolean eventInDiagram( ActionEvent event ) {
    // Assert.fail();
    // Object source = event.getSource();
    // if ( source instanceof ActionsMenuCreator.CustomJMenuItem ) {
    // ActionsMenuCreator.CustomJMenuItem item =
    // (ActionsMenuCreator.CustomJMenuItem)source;
    // // ((MDAction)item.getAction()).
    // }
    // boolean calledFromDiagram =
    // ( source != null &&
    // source.getClass().getSimpleName().toLowerCase().contains( "diagram" ) );
    // return calledFromDiagram;
    // }

    // public gov.nasa.jpl.ae.event.Expression<?> toAeExpression(
    // ValueSpecification mdValueSpec ) {
    // Assert.assertFalse( true ); // TODO
    // return null;
    // }
    //
    // public gov.nasa.jpl.ae.event.Expression<?> toAeExpression( Expression
    // mdExpression ) {
    // Assert.assertFalse( true ); // TODO
    // gov.nasa.jpl.ae.event.Expression<?> aeExpr = null;
    // List< ValueSpecification > args = mdExpression.getOperand();
    // return aeExpr;
    // }

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

}
