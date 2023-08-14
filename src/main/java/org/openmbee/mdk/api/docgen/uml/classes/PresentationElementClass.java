package org.openmbee.mdk.api.docgen.uml.classes;

import org.openmbee.mdk.api.docgen.uml.classes.properties.PresentationElementClassProperty;
import org.openmbee.mdk.api.docgen.uml.classes.properties.PresentationElementClassProperties;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import org.openmbee.mdk.api.util.ElementReference;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by igomes on 8/23/16.
 */
public abstract class PresentationElementClass extends ElementReference<Classifier> {

    private static final Set<PresentationElementClassProperty> PROPERTIES = new HashSet<>(2);

    static {
        PROPERTIES.add(PresentationElementClassProperties.GENERATED_FROM_ACTION.get());
        PROPERTIES.add(PresentationElementClassProperties.GENERATED_FROM_VIEW.get());
    }

    @Override
    public Class<Classifier> getElementClass() {
        return Classifier.class;
    }

    public Set<PresentationElementClassProperty> getProperties() {
        return PROPERTIES;
    }
}
