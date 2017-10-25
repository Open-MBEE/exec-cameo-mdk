package gov.nasa.jpl.mbee.mdk.api.docgen.uml.classes;

import java.util.function.Supplier;

/**
 * Created by igomes on 8/2/16.
 */
public enum PresentationElementClasses implements Supplier<PresentationElementClass> {
    EQUATION(new EquationClass()),
    FIGURE(new FigureClass()),
    IMAGE(new ImageClass()),
    LIST(new ListClass()),
    OPAQUE_FIGURE(new OpaqueFigureClass()),
    OPAQUE_IMAGE(new OpaqueImageClass()),
    OPAQUE_LIST(new OpaqueListClass()),
    OPAQUE_PARAGRAPH(new OpaqueParagraphClass()),
    OPAQUE_SECTION(new OpaqueSectionClass()),
    OPAQUE_TABLE(new OpaqueTableClass()),
    PARAGRAPH(new ParagraphClass()),
    SECTION(new SectionClass()),
    TABLE(new TableClass());

    private PresentationElementClass presentationElementClass;

    PresentationElementClasses(PresentationElementClass presentationElementClass) {
        this.presentationElementClass = presentationElementClass;
    }

    @Override
    public PresentationElementClass get() {
        return presentationElementClass;
    }
}