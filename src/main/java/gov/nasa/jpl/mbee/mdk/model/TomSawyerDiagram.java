package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.ProjectWindow;
import com.nomagic.magicdraw.ui.WindowComponentInfo;
import com.nomagic.magicdraw.ui.WindowsManager;
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
    private DiagramType type;

    public void setType(DiagramType type) {
        this.type = type;
    }

    public enum DiagramType {
        BLOCK_DEFINITION_DIAGRAM("Block Definition Diagram", "BDD"),
        INTERNAL_BLOCK_DIAGRAM("Internal Block Diagram", "IBD"),
        STATE_MACHINE_DIAGRAM("State Machine", "STM"),
        ACTIVITY_DIAGRAM("Activity Diagram", "ACT"),
        SEQUENCE_DIAGRAM("Sequence Diagram", "SD");

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

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        Object enumLiteral = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tomSawyerDiagramStereotype, "diagram_type", DocGenProfile.PROFILE_NAME, false);
        if (enumLiteral instanceof String) {
            setType(DiagramType.valueOf(enumLiteral.toString()));
        }
        Enumeration enumeration;
        if (enumLiteral instanceof EnumerationLiteral && (enumeration = ((EnumerationLiteral) enumLiteral).getEnumeration()) != null) {
            Arrays.stream(DiagramType.values()).filter(dt -> dt.getName().equals(enumeration.getName())).findAny().ifPresent(this::setType);
        }
    }


    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        if (type == null) {
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
        DocGenTSGenerateDiagramDelegate delegate = new DocGenTSGenerateDiagramDelegate(elements.get(0), type.getName()); //first arg should be the context element if ibd, par, or want to generate bdd
        delegate.addObjectsToShow(elements);
        DBTomSawyerDiagram dbts = new DBTomSawyerDiagram();
        String id = UUID.randomUUID().toString();

        dbts.setType(type);
        delegate.init(id, id, type.getName());
        try {
            delegate.loadData();
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage());
        }
        Set<String> viewElements;

        if (forViewEditor) {
            viewElements = delegate.postLoadDataGetUUID();
            if (type.equals(TomSawyerDiagram.DiagramType.INTERNAL_BLOCK_DIAGRAM)) {
                dbts.setContext(delegate.getContextElement());
            }
            dbts.setElements(viewElements);
            return Collections.singletonList(dbts);
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

            FileOutputStream stream = null;
            String fileName = id + ".svg";
            File file = new File(outputDir, fileName);

            try {
                stream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
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
}
