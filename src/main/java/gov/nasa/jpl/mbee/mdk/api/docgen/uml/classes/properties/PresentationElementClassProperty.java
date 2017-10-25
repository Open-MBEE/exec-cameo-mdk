package gov.nasa.jpl.mbee.mdk.api.docgen.uml.classes.properties;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.mdk.api.util.ElementReference;

/**
 * Created by igomes on 8/23/16.
 */
public abstract class PresentationElementClassProperty extends ElementReference<Property> {

    @Override
    public Class<Property> getElementClass() {
        return Property.class;
    }
}
