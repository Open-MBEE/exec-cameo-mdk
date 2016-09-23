package gov.nasa.jpl.mbee.mdk.api.docgen.presentation_elements;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import gov.nasa.jpl.mbee.mdk.api.docgen.ElementReference;
import gov.nasa.jpl.mbee.mdk.api.docgen.presentation_elements.properties.PresentationElementProperty;
import gov.nasa.jpl.mbee.mdk.api.docgen.presentation_elements.properties.PresentationElementPropertyEnum;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by igomes on 8/23/16.
 */
public abstract class PresentationElement extends ElementReference<Classifier> {

    private static final Set<PresentationElementProperty> PROPERTIES = new HashSet<>(2);

    static {
        PROPERTIES.add(PresentationElementPropertyEnum.GENERATED_FROM_ACTION.get());
        PROPERTIES.add(PresentationElementPropertyEnum.GENERATED_FROM_VIEW.get());
    }

    @Override
    public Class<Classifier> getElementClass() {
        return Classifier.class;
    }

    public Set<PresentationElementProperty> getProperties() {
        return PROPERTIES;
    }
}
