package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import gov.nasa.jpl.mbee.mdk.api.function.TriFunction;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;

import java.util.*;

@SuppressWarnings("unused")
public class TomSawyerDiagram extends Query {
    private DiagramType diagramType;
    private boolean collectRelatedElements;

    @Override
    public void initialize() {
        Object o = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tomSawyerDiagramStereotype, "diagramType", DocGenProfile.PROFILE_NAME, null);
        if (o instanceof String) {
            setDiagramType(DiagramType.valueOf(o.toString()));
        }
        if (o instanceof EnumerationLiteral) {
            Arrays.stream(DiagramType.values()).filter(dt -> dt.getName().equals(((EnumerationLiteral) o).getName())).findAny().ifPresent(this::setDiagramType);
        }
        if (diagramType == null) {
            Application.getInstance().getGUILog().log("[WARNING] No diagram type specified for " + Converters.getElementToHumanNameConverter().apply(dgElement) + ". Skipping diagram generation.");
            return;
        }
        Object o2 = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tomSawyerDiagramStereotype, "collectRelatedElements", DocGenProfile.PROFILE_NAME, false);
        if (o2 instanceof Boolean) {
            collectRelatedElements = (Boolean) o2;
        }
    }

    @SuppressWarnings("unchecked")
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
        try {
            TriFunction<TomSawyerDiagram, Collection<Element>, TomSawyerDiagram.DiagramType, List<DocumentElement>> generator = (TriFunction<TomSawyerDiagram, Collection<Element>, TomSawyerDiagram.DiagramType, List<DocumentElement>>) java.lang.Class.forName("gov.nasa.jpl.mbee.mdk.tomsawyer.api.DocumentElementGenerator").getConstructor().newInstance();
            return generator.apply(this, elements, diagramType);
        } catch (ReflectiveOperationException | ClassCastException e) {
            Application.getInstance().getGUILog().log("[WARNING] MDK DocGen TomSawyer plugin is not installed. TomSawyerDiagram DocGen activity skipped.");
        }
        return Collections.emptyList();
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    public boolean isCollectRelatedElements() {
        return collectRelatedElements;
    }

    public void setCollectRelatedElements(boolean collectRelatedElements) {
        this.collectRelatedElements = collectRelatedElements;
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
