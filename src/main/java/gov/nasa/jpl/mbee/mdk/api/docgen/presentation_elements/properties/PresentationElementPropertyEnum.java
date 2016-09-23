package gov.nasa.jpl.mbee.mdk.api.docgen.presentation_elements.properties;

import java.util.function.Supplier;

/**
 * Created by igomes on 8/23/16.
 */
public enum PresentationElementPropertyEnum implements Supplier<PresentationElementProperty> {
    GENERATED_FROM_ACTION(new GeneratedFromActionProperty()),
    GENERATED_FROM_ELEMENT(new GeneratedFromElementProperty()),
    GENERATED_FROM_VIEW(new GeneratedFromViewProperty());

    private PresentationElementProperty presentationElementProperty;

    PresentationElementPropertyEnum(PresentationElementProperty presentationElementProperty) {
        this.presentationElementProperty = presentationElementProperty;
    }

    @Override
    public PresentationElementProperty get() {
        return presentationElementProperty;
    }
}
