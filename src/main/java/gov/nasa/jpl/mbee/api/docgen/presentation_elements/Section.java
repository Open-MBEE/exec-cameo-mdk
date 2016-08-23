package gov.nasa.jpl.mbee.api.docgen.presentation_elements;

import gov.nasa.jpl.mbee.api.docgen.presentation_elements.properties.PresentationElementProperty;
import gov.nasa.jpl.mbee.api.docgen.presentation_elements.properties.PresentationElementPropertyEnum;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by igomes on 8/23/16.
 */
public class Section extends PresentationElement {
    private static final Set<PresentationElementProperty> PROPERTIES = new HashSet<>(1);

    static {
        PROPERTIES.add(PresentationElementPropertyEnum.GENERATED_FROM_ELEMENT.get());
    }

    @Override
    public String getID() {
        return "_18_0_2_407019f_1435683487667_494971_14412";
    }

    @Override
    public String getQualifiedName() {
        return "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::Section";
    }

    @Override
    public Set<PresentationElementProperty> getProperties() {
        Set<PresentationElementProperty> superProperties = super.getProperties(),
                combinedProperties = new HashSet<>(superProperties.size() + PROPERTIES.size());
        combinedProperties.addAll(superProperties);
        combinedProperties.addAll(PROPERTIES);
        return combinedProperties;
    }
}
