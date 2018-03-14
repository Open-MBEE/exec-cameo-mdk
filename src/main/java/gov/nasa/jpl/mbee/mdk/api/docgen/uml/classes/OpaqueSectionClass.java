package gov.nasa.jpl.mbee.mdk.api.docgen.uml.classes;

import gov.nasa.jpl.mbee.mdk.api.docgen.uml.classes.properties.PresentationElementClassProperty;
import gov.nasa.jpl.mbee.mdk.api.docgen.uml.classes.properties.PresentationElementClassProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by igomes on 8/23/16.
 */
public class OpaqueSectionClass extends PresentationElementClass {
    private static final Set<PresentationElementClassProperty> PROPERTIES = new HashSet<>(1);

    static {
        PROPERTIES.add(PresentationElementClassProperties.GENERATED_FROM_ELEMENT.get());
    }

    @Override
    public String getID() {
        return "_17_0_5_1_407019f_1430628211976_255218_12002";
    }

    @Override
    public String getQualifiedName() {
        return "SysML Extensions::DocGen::Presentation Elements::OpaqueSection";
    }

    @Override
    public Set<PresentationElementClassProperty> getProperties() {
        Set<PresentationElementClassProperty> superProperties = super.getProperties(),
                combinedProperties = new HashSet<>(superProperties.size() + PROPERTIES.size());
        combinedProperties.addAll(superProperties);
        combinedProperties.addAll(PROPERTIES);
        return combinedProperties;
    }
}
