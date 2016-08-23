package gov.nasa.jpl.mbee.api.docgen.presentation_elements.properties;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.api.docgen.ElementReference;

/**
 * Created by igomes on 8/23/16.
 */
public abstract class PresentationElementProperty extends ElementReference<Property> {

    @Override
    public Class<Property> getElementClass() {
        return Property.class;
    }
}
