package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.ui.browser.BrowserTabTree;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;

/**
 * A collection of utility functions for accessing the MagicDraw (MD)
 * application.
 */
public class MDUtils {

    public static String SVG_ENRICHED_EXPORT_PROPERTY_NAME = "svg.enriched.export";

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
        Collection<Element> coll = Collections.emptyList();
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
        Collection<Element> coll = Collections.emptyList();
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

    public static String getBranchId(Project project) throws RuntimeException {
        EsiUtils.EsiBranchInfo branchInfo;
        if (project.isRemote() && (branchInfo = EsiUtils.getCurrentBranch(project.getPrimaryProject())) != null && !branchInfo.getName().equals("trunk")) {
            return branchInfo.getID().toString();
        }
        return "master";
    }

    public static long getRemoteVersion(Project project) {
        if (!project.isRemote()) {
            return -1;
        }
        return Long.valueOf(ProjectUtilities.getVersion(project.getPrimaryProject()).getName());
    }

    public static void exportSVG(File svgFile, DiagramPresentationElement diagramPresentationElement) throws IOException, TransformerException {
        String originalSvgEnrichedExportPropertyValue = System.getProperty(SVG_ENRICHED_EXPORT_PROPERTY_NAME);
        System.setProperty(SVG_ENRICHED_EXPORT_PROPERTY_NAME, Boolean.toString(!diagramPresentationElement.getDiagramType().getRootType().equals(com.nomagic.magicdraw.uml.DiagramTypeConstants.DEPENDENCY_MATRIX)));

        try (InputStream svgInputStream = new FileInputStream(svgFile); Writer svgWriter = new FileWriter(svgFile)) {
            ImageExporter.export(diagramPresentationElement, ImageExporter.SVG, svgFile, false, DocGenUtils.DOCGEN_DIAGRAM_DPI, DocGenUtils.DOCGEN_DIAGRAM_SCALE_PERCENT);
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            Document svg = f.createDocument(null, svgInputStream);
            XMLUtil.asList(svg.getElementsByTagName("g")).stream()
                    .filter(g -> g instanceof org.w3c.dom.Element)
                    .map(g -> (org.w3c.dom.Element) g)
                    .filter(g -> Objects.equals(g.getAttribute("class"), "element"))
                    .forEach(g -> g.setAttribute("stroke-width", "0px"));
            DOMSource source = new DOMSource(svg);
            StreamResult result = new StreamResult(svgWriter);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        } finally {
            if (originalSvgEnrichedExportPropertyValue != null) {
                System.setProperty(SVG_ENRICHED_EXPORT_PROPERTY_NAME, originalSvgEnrichedExportPropertyValue);
            }
            else {
                System.clearProperty(SVG_ENRICHED_EXPORT_PROPERTY_NAME);
            }
        }
    }

}
