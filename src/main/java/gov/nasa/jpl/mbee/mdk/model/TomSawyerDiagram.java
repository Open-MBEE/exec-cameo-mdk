package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.ProjectWindow;
import com.nomagic.magicdraw.ui.WindowComponentInfo;
import com.nomagic.magicdraw.ui.WindowsManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.tomsawyer.canvas.TSViewportCanvas;
import com.tomsawyer.canvas.image.svg.TSSVGImageCanvas;
import com.tomsawyer.canvas.image.svg.TSSVGImageCanvasPreferenceTailor;
import com.tomsawyer.canvas.rendering.TSRenderingPreferenceTailor;
import com.tomsawyer.magicdraw.action.TSActionConstants;
import com.tomsawyer.util.preference.TSPreferenceData;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBImage;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBTomSawyerDiagram;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.tomsawyer.DocGenTSGenerateDiagramDelegate;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class TomSawyerDiagram extends Query {
    private DiagramType diagramType;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        Object enumLiteral = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tomSawyerDiagramStereotype, "diagram_type", DocGenProfile.PROFILE_NAME, false);
        if (enumLiteral instanceof String) {
            setDiagramType(DiagramType.valueOf(enumLiteral.toString()));
        }
        Enumeration enumeration;
        if (enumLiteral instanceof EnumerationLiteral && (enumeration = ((EnumerationLiteral) enumLiteral).getEnumeration()) != null) {
            Arrays.stream(DiagramType.values()).filter(dt -> dt.getName().equals(enumeration.getName())).findAny().ifPresent(this::setDiagramType);
        }
    }


    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        if (diagramType == null) {
            return Collections.emptyList();
        }
        List<Element> elements = new ArrayList<>();
        for (Object ob : this.getTargets()) {
            if (ob instanceof Element) {
                elements.add((Element) ob);
            }
        }
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }
        Element context = null;
        switch (diagramType) {
            case INTERNAL_BLOCK:
            case PARAMETRIC:
                context = elements.stream().filter(element -> element instanceof Class).findFirst().orElse(null);
                break;
        }
        DocGenTSGenerateDiagramDelegate delegate = new DocGenTSGenerateDiagramDelegate(context, diagramType);
        delegate.getElements().addAll(elements);
        DBTomSawyerDiagram diagram = new DBTomSawyerDiagram();
        String id = UUID.randomUUID().toString();

        diagram.setType(diagramType);
        delegate.init(id, id, diagramType.getName());
        try {
            delegate.loadData();
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage());
        }
        Set<Element> viewElements;

        if (forViewEditor) {
            viewElements = delegate.getDataModelElementIds();
            diagram.setContext(delegate.getContext());
            diagram.setElements(viewElements);
            return Collections.singletonList(diagram);
        }
        else {
            if (MDUtils.isDeveloperMode()) {
                TSStandardWindowComponentContent windowComponentContent = new TSStandardWindowComponentContent(delegate);
                ProjectWindow window = new ProjectWindow(new WindowComponentInfo(delegate.getId(), delegate.getName(), TSActionConstants.WINDOW_ICON, WindowsManager.SIDE_EAST, WindowsManager.STATE_DOCKED, false), windowComponentContent);
                Application.getInstance().getMainFrame().getProjectWindowsManager().addWindow(window);
                SwingUtilities.invokeLater(() -> windowComponentContent.setDividerLocation(0.7));
            }

            TSViewportCanvas canvas = delegate.getDiagramDrawing().getCanvas();
            TSPreferenceData preferenceData = new TSPreferenceData();

            TSSVGImageCanvasPreferenceTailor imageCanvasTailor = new TSSVGImageCanvasPreferenceTailor(preferenceData);

            imageCanvasTailor.setExportAll();
            imageCanvasTailor.setScaleByZoomLevel();
            imageCanvasTailor.setScalingZoomLevel(1.0);
            imageCanvasTailor.setWidth(-1);
            imageCanvasTailor.setHeight(-1);

            TSRenderingPreferenceTailor renderingTailor = new TSRenderingPreferenceTailor(preferenceData);

            boolean isDrawNodesBeforeEdges = new TSRenderingPreferenceTailor(canvas.getPreferenceData()).isDrawNodesBeforeEdges();
            renderingTailor.setDrawNodesBeforeEdges(isDrawNodesBeforeEdges);

            renderingTailor.setDrawSelectedOnly(false);
            renderingTailor.setDrawSelectionState(false);
            renderingTailor.setDrawHighlightState(false);
            renderingTailor.setDrawHoverState(false);

            FileOutputStream stream;
            String fileName = id + ".svg";
            File file = new File(outputDir, fileName);

            try {
                stream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
            TSSVGImageCanvas imageCanvas = new TSSVGImageCanvas(canvas.getGraphManager(), stream);

            imageCanvas.setDisplayCanvas(canvas);
            imageCanvas.setPreferenceData(preferenceData);
            imageCanvas.paint();

            DBImage myImage = new DBImage();
            myImage.setCaption(delegate.getName());
            myImage.setTitle(delegate.getName());
            myImage.setIsTomSawyerImage(true);
            myImage.setOutputDir(outputDir);
            myImage.setImageFileName(fileName);

            return Collections.singletonList(myImage);
        }
    }

    public void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    public enum DiagramType {
        BLOCK_DEFINITION("Block Definition Diagram", "BDD"),
        INTERNAL_BLOCK("Internal Block Diagram", "IBD"),
        STATE_MACHINE("State Machine", "STM"),
        ACTIVITY("Activity Diagram", "ACT"),
        SEQUENCE("Sequence Diagram", "SD"),
        PACKAGE("Package Diagram", "PKG"),
        PARAMETRIC("Parametric Diagram", "PAR"),
        REQUIREMENT("Requirement Diagram", "REQ"),
        USE_CASE("Use Case Diagram", "UC");

        private final String name;
        private final String abbreviation;

        DiagramType(String name, String abbreviation) {
            this.name = name;
            this.abbreviation = abbreviation;
        }

        public String getName() {
            return name;
        }

        public String getAbbreviation() {
            return abbreviation;
        }
    }
}
