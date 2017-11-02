package gov.nasa.jpl.mbee.mdk.api.docgen.uml.classes.properties;

import java.util.function.Supplier;

/**
 * Created by igomes on 8/23/16.
 */
public enum PresentationElementClassProperties implements Supplier<PresentationElementClassProperty> {
    GENERATED_FROM_ACTION(new GeneratedFromActionProperty()),
    GENERATED_FROM_ELEMENT(new GeneratedFromElementProperty()),
    GENERATED_FROM_VIEW(new GeneratedFromViewProperty());

    private PresentationElementClassProperty presentationElementClassProperty;

    PresentationElementClassProperties(PresentationElementClassProperty presentationElementClassProperty) {
        this.presentationElementClassProperty = presentationElementClassProperty;
    }

    @Override
    public PresentationElementClassProperty get() {
        return presentationElementClassProperty;
    }
}
