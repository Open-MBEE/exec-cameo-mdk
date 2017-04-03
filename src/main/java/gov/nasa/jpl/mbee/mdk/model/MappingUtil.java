package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.impl.ElementsFactory;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.util.HashMap;
import java.util.Map;

public class MappingUtil {
    private static final String DEPPREFIX = "zz_";

    /**
     * Utility for refactoring properties between the library and mission
     * characterizations (e.g, lib properties are mandatory for mc)
     *
     * @param lib Library characterization
     * @param mc  Concrete characterization
     * @param ef  ElementsFactory
     */
    public static void refactorProperties(Element lib, Element mc, ElementsFactory ef) {
        GUILog log = Application.getInstance().getGUILog();
        Map<String, Property> mprops = new HashMap<String, Property>();
        Map<String, Property> lprops = new HashMap<String, Property>();

        // get the property sets for the mission characterization and the
        // library characterization
        for (Element e : mc.getOwnedElement()) {
            if (e instanceof Property) {
                Property p = (Property) e;
                mprops.put(p.getName(), p);
            }
        }
        for (Element e : lib.getOwnedElement()) {
            if (e instanceof Property) {
                Property p = (Property) e;
                lprops.put(p.getName(), p);
            }
        }

        for (Property mprop : mprops.values()) {
            if (lprops.containsKey(mprop.getName().replace(DEPPREFIX, ""))) {
                if (mprop.getName().startsWith(DEPPREFIX)) {
                    mprop.setName(mprop.getName().replace(DEPPREFIX, ""));
                    mprops.put(mprop.getName(), mprop); // so it's not recreated
                    // in next pass
                    log.log("Property undeprecated: " + mprop.getName() + " undeprecated in "
                            + mc.getHumanName());
                }
            }
            else {
                if (!mprop.getName().startsWith(DEPPREFIX)) {
                    mprop.setName(DEPPREFIX + mprop.getName());
                    log.log("Property deprecated: " + mprop.getName() + " deprecated in " + mc.getHumanName());
                }
            }
        }
        for (Property lprop : lprops.values()) {
            if (!mprops.containsKey(lprop.getName())) {
                Property np = ef.createPropertyInstance();
                np.setName(lprop.getName());
                np.setOwner(mc);
                np.setType(lprop.getType());
                np.setAggregation(lprop.getAggregation());
                np.getRedefinedProperty().add(lprop);
                Utils.copyStereotypes(lprop, np);
                log.log("Property created: " + np.getName() + " added to " + mc.getHumanName());
            }
        }
    }
}
