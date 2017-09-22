package gov.nasa.jpl.mbee.mdk.api.docgen.presentation_elements;

import java.util.function.Supplier;

/**
 * Created by igomes on 8/2/16.
 */
public enum PresentationElementEnum implements Supplier<PresentationElement> {
    EQUATION(new Equation()),
    FIGURE(new Figure()),
    IMAGE(new Image()),
    LIST(new List()),
    OPAQUE_FIGURE(new OpaqueFigure()),
    OPAQUE_IMAGE(new OpaqueImage()),
    OPAQUE_LIST(new OpaqueList()),
    OPAQUE_PARAGRAPH(new OpaqueParagraph()),
    OPAQUE_SECTION(new OpaqueSection()),
    OPAQUE_TABLE(new OpaqueTable()),
    PARAGRAPH(new Paragraph()),
    SECTION(new Section()),
    TABLE(new Table());

    private PresentationElement presentationElement;

    PresentationElementEnum(PresentationElement presentationElement) {
        this.presentationElement = presentationElement;
    }

    @Override
    public PresentationElement get() {
        return presentationElement;
    }
}