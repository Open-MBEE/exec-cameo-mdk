package org.openmbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.export.image.EnrichedSVGExporter;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.ui.browser.BrowserTabTree;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TaggedValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.docgen.DocGenUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import javax.annotation.CheckForNull;
import javax.xml.transform.TransformerException;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        try {
            // boolean svgEnrichedExportPropertyValue = originalSvgEnrichedExportPropertyValue != null
            //         && Boolean.getBoolean(originalSvgEnrichedExportPropertyValue)
            //         && !diagramPresentationElement.getDiagramType().getRootType().equals(DEPENDENCY_MATRIX);
            boolean svgEnrichedExportPropertyValue = true;
            System.setProperty(SVG_ENRICHED_EXPORT_PROPERTY_NAME, Boolean.toString(svgEnrichedExportPropertyValue));

            ImageExporter.export(diagramPresentationElement, ImageExporter.SVG, svgFile, false, DocGenUtils.DOCGEN_DIAGRAM_DPI, DocGenUtils.DOCGEN_DIAGRAM_SCALE_PERCENT);
            String svgString = readFileToString(svgFile, StandardCharsets.UTF_8);
            if (isEnriched(svgString)) {
                Project project = diagramPresentationElement.getProject();
                Pattern p = Pattern.compile("id=\"([a-z0-9-]+)\"");
                Matcher m = p.matcher(svgString);
                svgString = m.replaceAll(match -> {
                    String replace = "id=\"" + fixId(match.group(1), project) + "\"";
                    return replace;
                });
                StringBuilder sb = new StringBuilder(svgString);
                appendStyle(sb);
                EnrichedSVGExporter.rewriteFile(svgFile, sb, StandardCharsets.UTF_8);
            }
        } finally {
            if (originalSvgEnrichedExportPropertyValue != null) {
                System.setProperty(SVG_ENRICHED_EXPORT_PROPERTY_NAME, originalSvgEnrichedExportPropertyValue);
            }
            else {
                System.clearProperty(SVG_ENRICHED_EXPORT_PROPERTY_NAME);
            }
        }
    }

    private static String fixId (String id, Project project) {
        try {
            UUID.fromString(id);
            id = getEID(Converters.getIdToElementConverter().apply(id, project));
        } catch(IllegalArgumentException e) {
            //Do Nothing
        }
        return id;
    }

    public static String readFileToString(File file, @CheckForNull Charset charset) {
      ByteArrayOutputStream st = new ByteArrayOutputStream();

      try {
         copy(new FileInputStream(file), st);
      } catch (IOException e) {
         Application.getInstance().getGUILog().log("[ERROR] " + e.getMessage());
         e.printStackTrace();
      }

      return charset != null ? st.toString(charset) : st.toString();
   }

   private static void copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[512];
    int count = 0;

    while(count != -1) {
       count = input.read(buffer);
       if (count > 0) {
          output.write(buffer, 0, count);
       }
    }

    input.close();
    output.close();
 }

    private static void appendStyle(StringBuilder svgString) {
        int var1 = svgString.indexOf("<!--Generated by the Batik");
        if (var1 > 0) {
           svgString.insert(var1, "<style>.element path { stroke-width: 0px; }</style>");
        }
  
     }

    public static boolean isEnriched(String svgString) {
        int styleComment = svgString.indexOf("<!--STYLE -->");
        return styleComment > 0;
    }

    public static String getEID(EObject eObject) {
        if (eObject == null) {
            return null;
        }
        if (!(eObject instanceof Element)) {
            return EcoreUtil.getID(eObject);
        }
        Element element = (Element) eObject;
        Project project = Project.getProject(element);

        // custom handling of primary model id
        if (element instanceof Model && element == project.getPrimaryModel()) {
            return Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + MDKConstants.PRIMARY_MODEL_ID_SUFFIX;
        }

        // local projects don't properly maintain the ids of some elements. this id spoofing mitigates that for us, but can mess up the MMS delta counts in some cases (annoying, but ultimately harmless)
        // NOTE - this spoofing is replicated in LocalSyncTransactionListener in order to properly add / remove elements in the unsynched queue. any updates here should be replicated there as well.
        // there's no more instance spec that's a result of stereotyping, so instance spec should just have their normal id
        /*if (eObject instanceof TimeExpression && ((TimeExpression) eObject).get_timeEventOfWhen() != null) {
            return getEID(((TimeExpression) eObject).get_timeEventOfWhen()) + MDKConstants.TIME_EXPRESSION_ID_SUFFIX;
        }*/
        if (element instanceof ValueSpecification && ((ValueSpecification) element).getOwningSlot() != null) {
            ValueSpecification slotValue = (ValueSpecification) element;
            return getEID(slotValue.getOwningSlot()) + MDKConstants.SLOT_VALUE_ID_SEPARATOR + slotValue.getOwningSlot().getValue().indexOf(slotValue) + "-" + slotValue.eClass().getName().toLowerCase();
        }
        if (element instanceof TaggedValue) {
            TaggedValue slot = (TaggedValue) element;
            if (slot.getTaggedValueOwner() != null && slot.getTagDefinition() != null) {
                // add _asi to owner in constructed id to maintain continuity with 19.x slots
                return getEID(slot.getOwner()) + MDKConstants.APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX + MDKConstants.SLOT_ID_SEPARATOR + getEID(slot.getTagDefinition());
            }
        }
        if (element instanceof Slot) {
            Slot slot = (Slot) element;
            if (slot.getOwningInstance() != null && ((Slot) element).getDefiningFeature() != null) {
                return getEID(slot.getOwningInstance()) + MDKConstants.SLOT_ID_SEPARATOR + getEID(slot.getDefiningFeature());
            }
        }
        return element.getLocalID();
    }
}
